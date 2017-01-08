package hmm.dota2dqn.common;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class State {
	private INDArray state;
	private int stateSize;
	public State(int statesize){
		this.stateSize=statesize;
		this.state=Nd4j.zeros(stateSize, stateSize);
	}
	public State(State original){
		this.state= original.state.dup();
	}
	public void addToState(int[] indexes, double val){		
		state.putScalar(indexes, val);
		
	}
	public INDArray getFlattenState(){
		return Nd4j.toFlattened(this.state);
	}
}
