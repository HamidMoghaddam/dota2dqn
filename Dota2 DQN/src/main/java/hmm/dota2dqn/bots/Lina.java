package hmm.dota2dqn.bots;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import hmm.dota2dqn.common.Configs;
import hmm.dota2dqn.common.Memory;
import hmm.dota2dqn.common.State;
import hmm.dota2dqn.nn.DeepQLearning;
import se.lu.lucs.dota2.framework.bot.BaseBot;
import se.lu.lucs.dota2.framework.bot.BotCommands.LevelUp;
import se.lu.lucs.dota2.framework.bot.BotCommands.Select;
import se.lu.lucs.dota2.framework.game.BaseEntity;
import se.lu.lucs.dota2.framework.game.BaseNPC;
import se.lu.lucs.dota2.framework.game.Hero;
import se.lu.lucs.dota2.framework.game.Tower;
import se.lu.lucs.dota2.framework.game.World;

public class Lina extends BaseBot{

	Configs conf;
	Memory memory;
	DeepQLearning dqn;
	boolean isStarting,isFinished;
	float[] goalPosition;
	float[] previousPosition;
	State lastState;
	World world;
	Hero lina;
	int steps,totalReward;
	double explorationRate,explorationDecay;
	public Lina(Configs inputConfig) throws IOException{
		this.conf=inputConfig;
		this.steps=0;
		this.totalReward=0;
		this.goalPosition= new float[2];
		this.previousPosition=new float[2];
		this.isStarting=true;
		this.explorationDecay=conf.getExplorationDecay();
		this.explorationRate=conf.getExplorationRate();
		
		//Create memory
		memory = new Memory();
		
		dqn = new DeepQLearning(conf);
		//Check whether the NNets are saved before
		File f1 = new File("Navigation_q1");
		File f2 = new File("Navigation_q2");		
		if(f1.exists()&&f2.exists())
			dqn.loadNNs("Navigation");
		else
			dqn.generateNNs();
			
	}
	@Override
	public LevelUp levelUp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Select select() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Command update(World inputWorld) {
		// TODO Auto-generated method stub
		//Steps shows the number of update function called to achieve the goal
		this.world=inputWorld;
		steps+=1;
		final int myIndex = world.searchIndexByName(conf.getHeroName());		
		this.lina = (Hero) world.getEntities().get( myIndex );
		if (myIndex < 0) {            
            System.out.println( "Lina is dead!" );            
            return NOOP;
        }
		float[] position= new float[2];
		position[0]=lina.getOrigin()[0];
		position[1]=lina.getOrigin()[1];
		
		/*
		 * Check whether the game is just started. It might be better to do this part in addon_game_mode.lua file but for now it
		 * works good enough.
		 */
		if(isStarting==true){
			lastState= new State(conf.getOneDStateSize());
			goalPosition=position.clone();
			isStarting=false;
		}
		/*
		 * The time between two update is 0.33 (Please check addon_game_mode.lua) and it is not enough for lina to complete an action.
		 * Although it is possible to increase the time, it might not good for other activities like attack. Thus, checking whether lina
		 * is complete its task before applying a new one is essential. In addition, sometimes lina get stuck behind world obstacles so
		 * it is necessary to check that situation in order to avoiding any infinite loop (It might be better to give some big negative
		 * reward to those action that make her getting stuck).
		 */
		if(Arrays.equals(goalPosition, position)||Arrays.equals(position, previousPosition)){
			previousPosition= position.clone();
			goalPosition=calcDestination(position).clone();
			if(isFinished==true){
				System.out.println(steps);
    			return NOOP;
			}
			MOVE.setX(goalPosition[0]);
    		MOVE.setY(goalPosition[1]);    		
    		return MOVE;
		}else{
			previousPosition= position.clone();
			//Lina has not complete her last act so no need for new act and just do some training. 
    		if(memory.getMemorySize()>32)
        		dqn.learnOnMemory(memory);    		
    		return NOOP;
		}				
	}
	private int choseAnAction() {
		int act=0;		
		State observation= new State(conf.getOneDStateSize());
		observation=generateNavigationState();
		int range=(int) conf.getHeroRange();
		int dimansion =(int) conf.getGameDimansion();
		final Set<BaseEntity> e = findEntitiesInRange( world, lina,range).stream().filter( p -> p instanceof BaseNPC )
                .filter( p -> ((BaseNPC) p).getTeam() == 3 ).collect( Collectors.toSet() );
		//Check whether lina reach her goal
        if (!e.isEmpty()&&isFinished==false) {        	
        	memory.addToMemory(observation,lastState,act,100,true); 
        	totalReward+=100;
        	System.out.println(totalReward);        	
        	try {
				dqn.saveNN("Navigation");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	isFinished=true;
        }else{
        	if(Math.abs(goalPosition[0])==dimansion|| Math.abs(goalPosition[1])==dimansion){
        		//AI should not choose a coordination out of game world
        		memory.addToMemory(observation,lastState,act,-1000,false);
        		totalReward-=1000;
        	}else{       		
	        	memory.addToMemory(observation,lastState,act,-1,false); 
	        	totalReward-=1;
        	}       
        	lastState= new State(observation);
        }
        act=dqn.selectAction(observation,explorationRate); 
        explorationRate *= explorationDecay;
		return act;
	}
	private float[] calcDestination(float[] position){
		int act=choseAnAction();		
		float[] destination=new float[2];
		int range= (int) conf.getHeroRange();
		int euclideanDistance= (int) (range/Math.sqrt(2));
		int dimansion = (int) conf.getGameDimansion();
		switch (act) {
 		case 0:
 			destination[0]=(position[0]+range)>dimansion ? dimansion:position[0]+range;
 			destination[1]=position[1];	 	
 			break;
 		case 1:
 			destination[0]=position[0];
 			destination[1]=position[1]+range>dimansion ? dimansion:position[1]+range;
 			break;
 		case 2:
 			destination[0]=position[0]-range<-dimansion ? -dimansion:position[0]-range;
 			destination[1]=position[1];	 		 			
 			break;
 		case 3:
 			destination[0]=position[0];
 			destination[1]=position[1]-range<-dimansion ? -dimansion:position[1]-range;
 			break;
 		case 4:
 			destination[0]=position[0]+euclideanDistance>dimansion ? dimansion:position[0]+euclideanDistance;
 			destination[1]=position[1]+euclideanDistance>dimansion ? dimansion:position[1]+euclideanDistance;
 			break;
 		case 5:
 			destination[0]=position[0]+euclideanDistance>dimansion ? dimansion:position[0]+euclideanDistance;
 			destination[1]=position[1]-euclideanDistance<-dimansion ? -dimansion:position[1]-euclideanDistance;
 			break;
 		case 6:
 			destination[0]=position[0]-euclideanDistance<-dimansion ? -dimansion:position[0]-euclideanDistance;
 			destination[1]=position[1]-euclideanDistance<-dimansion ? -dimansion:position[1]-euclideanDistance;
 			break;
 		case 7:
 			destination[0]=position[0]-euclideanDistance<-dimansion ? -dimansion:position[0]-euclideanDistance;
 			destination[1]=position[1]+euclideanDistance>dimansion ? dimansion:position[1]+euclideanDistance;
 			break;
 		default:
 			break;
		}
	
	return destination;
	}
	private State generateNavigationState(){
		State returnState = new State(conf.getOneDStateSize());
		int[] xAndy=new int[2];
		float[] position= new float[3];
		position=lina.getOrigin();
		xAndy=convertToMiniSize(position[0],position[1]);  
		returnState.addToState(xAndy,1);
		final Set<BaseEntity> en = findEntitiesInRange( world, lina, Float.POSITIVE_INFINITY ).stream().filter( p -> p instanceof BaseNPC )
                .filter( p -> ((BaseNPC) p).getTeam() == 3 ).collect( Collectors.toSet() );
		for (BaseEntity targetenitity : en) {
			position=targetenitity.getOrigin();
			xAndy=convertToMiniSize(position[0],position[1]);
    		switch (targetenitity.getClass().getName()) {
			case "se.lu.lucs.dota2.framework.game.Hero":
				returnState.addToState(xAndy,2);
				break;
			case "se.lu.lucs.dota2.framework.game.BaseNPC":
				returnState.addToState(xAndy,3);		    	
				break;
			case "se.lu.lucs.dota2.framework.game.Tower":
				returnState.addToState(xAndy,4);		    	
				break;
			case "se.lu.lucs.dota2.framework.game.Building":
				returnState.addToState(xAndy,5);
				break;
			default:
				break;
			}
    	}
    	final Set<BaseEntity> altower = findEntitiesInRange( world, lina, Float.POSITIVE_INFINITY ).stream().filter( p -> p instanceof BaseNPC )
                .filter( p -> ((BaseNPC) p).getTeam() == lina.getTeam() ).filter( p -> ((BaseNPC) p).getClass() == Tower.class ).collect( Collectors.toSet() );
    	for (BaseEntity targetenitity : altower) {
    		position=targetenitity.getOrigin();
			xAndy=convertToMiniSize(position[0],position[1]);
			returnState.addToState(xAndy,6);	    	
    	}
		return returnState;
	}
	private int[] convertToMiniSize(float x, float y){
		int[] totalarray= new int[2];
		double XandYofWorld= conf.getGameDimansion();
		int minisize= conf.getOneDStateSize();
		totalarray[0]=(int)(((x+XandYofWorld)*minisize)/(XandYofWorld*2));
		totalarray[1]=(int)(((XandYofWorld-y)*minisize)/(XandYofWorld*2));
		return totalarray;
	}
	private static Set<BaseEntity> findEntitiesInRange( World world, BaseEntity center, float range ) {
	        final Set<BaseEntity> result = world.getEntities().values().stream().filter( e -> distance( center, e ) < range ).collect( Collectors.toSet() );
	        result.remove( center );
	        return result;
	    }
	private static float distance( BaseEntity a, BaseEntity b ) {
	        final float[] posA = a.getOrigin();
	        final float[] posB = b.getOrigin();
	        return distance( posA, posB );
	    }
	private static float distance( float[] posA, float[] posB ) {
	        return (float) Math.hypot( posB[0] - posA[0], posB[1] - posA[1] );
	    }
}
