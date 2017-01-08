package hmm.dota2dqn.nn;

import java.io.File;
import java.io.IOException;


import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.rl4j.util.Constants;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;


import hmm.dota2dqn.common.Configs;

public class NeuralNetwork {
	private MultiLayerNetwork mln;
	private int inputs, outputs;
	private int[] hiddenLayers;
	public NeuralNetwork(){};
	public void createNN(Configs generalConf){
		
		double learningRate=generalConf.getLearningRate();
		this.inputs=generalConf.getStateSize();
		this.outputs=generalConf.getActionSize();
		this.hiddenLayers=generalConf.getHiddenLayers();
		MultiLayerConfiguration conf = null;
		if (hiddenLayers.length == 0) {
	        conf = new NeuralNetConfiguration.Builder()
	                .iterations(1)
	                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
	                .learningRate(learningRate)
                    .biasLearningRate(learningRate)
                    .biasInit(0)
	                .updater(Updater.RMSPROP)
	                .list()
	                .layer(0, new OutputLayer.Builder(LossFunction.MSE)
	                        .weightInit(WeightInit.XAVIER)
	                        .activation(Activation.IDENTITY).weightInit(WeightInit.XAVIER)
	                        .nIn(inputs).nOut(outputs).build())
	                .pretrain(false).backprop(true).build();
		} else {
			NeuralNetConfiguration.ListBuilder builder = new NeuralNetConfiguration.Builder()
	                .iterations(1)
	                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
	                .learningRate(learningRate)
                    .biasLearningRate(learningRate)
                    .biasInit(0)
	                .updater(Updater.RMSPROP)
	                .list()
	                .layer(0, new DenseLayer.Builder().nIn(inputs).nOut(hiddenLayers[0])
	                        .weightInit(WeightInit.XAVIER)
	                        .activation(Activation.RELU)
	                        .build());
	        for (int i = 0 ; i < hiddenLayers.length - 1 ; i++) {
	        	builder.layer(i+1, new DenseLayer.Builder().nIn(hiddenLayers[i]).nOut(hiddenLayers[i+1])
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .build());
	        }
	        conf =  builder.layer(hiddenLayers.length, new OutputLayer.Builder(LossFunction.MSE)
	                        .weightInit(WeightInit.XAVIER)
	                        .activation(Activation.IDENTITY).weightInit(WeightInit.XAVIER)
	                        .nIn(hiddenLayers[hiddenLayers.length - 1]).nOut(outputs).build())
	                .pretrain(false).backprop(true).build();
		}
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        
        model.setListeners(new ScoreIterationListener(Constants.NEURAL_NET_ITERATION_LISTENER));
        this.mln = model;
	}
	
	public void loadModel(Configs conf,String fileName) throws IOException {
		this.mln = ModelSerializer.restoreMultiLayerNetwork(fileName);
    	this.inputs=conf.getStateSize();
    	this.outputs=conf.getActionSize();
    }
	public void saveModel(String path) throws IOException {
		//Save the model
	    File locationToSave = new File(path);      
	    boolean saveUpdater = true;                                  
	    ModelSerializer.writeModel(this.mln, locationToSave, saveUpdater);
	}
	public synchronized INDArray predictAction(INDArray input) {               
        INDArray resultVector = mln.output(input);       
		return resultVector;
	}
	public void trainNetwork(DataSetIterator datasetIterator) {	
		
		mln.fit(datasetIterator);
	}
	
	
}
