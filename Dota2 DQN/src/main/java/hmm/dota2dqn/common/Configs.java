package hmm.dota2dqn.common;

public class Configs {
	private int miniBatchSize;
	private double heroRange;
	private double gameDimansion;
	private int oneDStateSize;	
	private int stateSize;
	private int actionSize;
	private int[] hiddenLayers;
	private double learningRate;
	private double gamma;
	private String heroName;
	private double explorationRate;
	private double explorationDecay;
	public Configs(){
		this.setMiniBatchSize(32);
		//The attacking range of Lina is 670
		this.setHeroRange(670);
		//The x and y dimansion of game's world is almost 7500
		this.setGameDimansion(7500);
		/*
		 * Size of navigation's state (Please check the Readme file for more information) 
		 */
		this.setOneDStateSize((int) Math.ceil(gameDimansion/heroRange));
		this.setStateSize((int) Math.pow(getOneDStateSize(),2));
		/*
		 * Number of navigation direction in game world is 8
		 */
		this.setActionSize(8);
		
		this.setHiddenLayers(new int[]{70,35,15});
		
		this.setLearningRate(0.01);
		
		this.setGamma(0.8);
		
		this.setHeroName("npc_dota_hero_lina");
		this.setExplorationRate(0.3);
		this.setExplorationDecay(0.95);
	}
	
	public int getMiniBatchSize() {
		return miniBatchSize;
	}
	public void setMiniBatchSize(int miniBatchSize) {
		this.miniBatchSize = miniBatchSize;
	}
	public double getHeroRange() {
		return heroRange;
	}
	public void setHeroRange(double heroRange) {
		this.heroRange = heroRange;
	}
	public double getGameDimansion() {
		return gameDimansion;
	}
	public void setGameDimansion(double gameDimansion) {
		this.gameDimansion = gameDimansion;
	}
	public int getOneDStateSize() {
		return oneDStateSize;
	}
	public void setOneDStateSize(int oneDStateSize) {
		this.oneDStateSize = oneDStateSize;
	}

	public int getStateSize() {
		return stateSize;
	}

	public void setStateSize(int stateSize) {
		this.stateSize = stateSize;
	}

	public int getActionSize() {
		return actionSize;
	}

	public void setActionSize(int actionSize) {
		this.actionSize = actionSize;
	}

	public int[] getHiddenLayers() {
		return hiddenLayers;
	}

	public void setHiddenLayers(int[] hiddenLayers) {
		this.hiddenLayers = hiddenLayers;
	}

	public double getLearningRate() {
		return learningRate;
	}

	public void setLearningRate(double learningRate) {
		this.learningRate = learningRate;
	}

	public double getGamma() {
		return gamma;
	}

	public void setGamma(double gamma) {
		this.gamma = gamma;
	}

	public String getHeroName() {
		return heroName;
	}

	public void setHeroName(String heroName) {
		this.heroName = heroName;
	}

	public double getExplorationRate() {
		return explorationRate;
	}

	public void setExplorationRate(double explorationRate) {
		this.explorationRate = explorationRate;
	}

	public double getExplorationDecay() {
		return explorationDecay;
	}

	public void setExplorationDecay(double explorationDecay) {
		this.explorationDecay = explorationDecay;
	}
}
