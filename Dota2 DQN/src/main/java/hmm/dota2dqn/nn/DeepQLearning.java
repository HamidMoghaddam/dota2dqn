package hmm.dota2dqn.nn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.datasets.iterator.INDArrayDataSetIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import hmm.dota2dqn.common.Configs;
import hmm.dota2dqn.common.Memory;
import hmm.dota2dqn.common.MemoryEntry;
import hmm.dota2dqn.common.State;

public class DeepQLearning {
	private Random randomfunc = new Random();
	private NeuralNetwork nnet1;
	private NeuralNetwork nnet2;
	Configs conf;
	public DeepQLearning(Configs confInput){
		this.conf=confInput;
		this.nnet1= new NeuralNetwork();
		this.nnet2=new NeuralNetwork();
	}
	public void generateNNs(){
		nnet1.createNN(conf);
		nnet2.createNN(conf);
	}
	public void loadNNs(String filename) throws IOException{
		nnet1.loadModel(conf, filename+"_q1");
		nnet2.loadModel(conf, filename+"_q2");
	}
	public void saveNN(String filename) throws IOException{
		nnet1.saveModel(filename+"_q1");
		nnet2.saveModel(filename+"_q2");
	}
	public int selectAction(State state, double explorationRate) {		
		int action=0;
		INDArray qValues1 = nnet1.predictAction(state.getFlattenState());
    	INDArray qValues2 = nnet2.predictAction(state.getFlattenState());
    	INDArray qValues = qValues1.add(qValues2);    					
		double random = randomfunc.nextDouble();
		if (random < explorationRate) {
			action = randomfunc.nextInt(conf.getActionSize());	
		} else {			
			action=findMaxIndex(qValues);								
		}
		return action;
	}
	private int findMaxIndex(INDArray inputqValues){
		int returnq=0;
		double max = (double) inputqValues.maxNumber();
		for(int i=0;i<conf.getActionSize();i++){
			double qvalue= inputqValues.getDouble(i);
			if(max==qvalue){
				returnq=i;
				break;
			}
		}
		return returnq;
	}
	public void learnOnMemory(Memory memory) {
		List<MemoryEntry> miniBatch = memory.getMiniBatch();
		List<Pair<INDArray, INDArray>> pairedList= new ArrayList<Pair<INDArray, INDArray>>();
		double rand = randomfunc.nextDouble();
		for (MemoryEntry entry : miniBatch) {
			INDArray qValues = Nd4j.zeros(conf.getActionSize(),1);			
			INDArray qValuesObserved = Nd4j.zeros(conf.getActionSize(),1);
			INDArray lastState= entry.getState().getFlattenState();
			INDArray observedState =entry.getNewState().getFlattenState();
			if (rand < 0.5) {
				qValues = nnet1.predictAction(lastState);
				qValuesObserved = nnet2.predictAction(observedState);
			} else {
				qValues = nnet2.predictAction(lastState);
				qValuesObserved = nnet1.predictAction(observedState);
			}
			double targetValue = calculateOptimalQ(qValuesObserved, entry.getReward(), entry.isFinal());
			
			double oldvalue = qValues.getDouble(entry.getAction(), 1);
			double newValue=(1-conf.getLearningRate())*oldvalue+ targetValue;
			qValues.putScalar(entry.getAction(),1, newValue);
			Pair<INDArray,INDArray> pairedInputOutput= new Pair<INDArray, INDArray>(lastState, qValues);	
			pairedList.add(pairedInputOutput);
		}
		DataSetIterator datasetIterator = new INDArrayDataSetIterator(pairedList, conf.getMiniBatchSize());
		if (rand < 0.5) {
			nnet1.trainNetwork(datasetIterator);
		} else {
			nnet2.trainNetwork(datasetIterator);
		}
	}
	private double calculateOptimalQ(INDArray qValuesObservedState, double reward, boolean isDone) {		
		double maxQ= (double) qValuesObservedState.maxNumber();
        if (isDone)
            return reward;
        else
            return reward + conf.getGamma() * maxQ;
}

}
