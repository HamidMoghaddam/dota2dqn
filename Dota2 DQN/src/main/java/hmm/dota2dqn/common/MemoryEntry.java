package hmm.dota2dqn.common;

import java.io.Serializable;


public class MemoryEntry implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private transient State state;
	private transient int action;
	private transient double reward;
	private transient State observedState;
	private transient boolean isFinal;
	
	public MemoryEntry(State state, int action, State observation,double reward,boolean isfinal)  {
		this.state = new State(state);
		this.action = action;
		this.reward = reward;
		this.observedState = observation;
		this.isFinal = isfinal;
	}
	
	public int getAction() {
		return action;
	}
	
	public State getNewState() {
		return observedState;
	}
	
	public double getReward() {
		return reward;
	}
	
	public State getState() {
		return state;
	}		
	public boolean isFinal() {
		return isFinal;
	}
}
