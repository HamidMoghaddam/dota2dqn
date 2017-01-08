package hmm.dota2dqn;


import java.io.IOException;

import hmm.dota2dqn.bots.Lina;
import hmm.dota2dqn.common.Configs;
import se.lu.lucs.dota2.service.Dota2AIService;

public class App 
{
    public static void main( String[] args ) throws IOException
    {
    	Configs conf= new Configs();
    	Lina lina = new Lina(conf);    	
    	@SuppressWarnings("unused")
		Dota2AIService ss= new Dota2AIService(lina);
        
    }
}
