package ru.egov.urm.messenger.rocket;

import java.util.Properties;

public class RocketMain {

	public void execute( Properties props ) throws Exception {
		String server = props.getProperty( "server" );
		String account = props.getProperty( "account" );
		String password = props.getProperty( "password" );
		String chatInclude = props.getProperty( "include" );
		String chatExclude = props.getProperty( "exclude" );
		
		boolean first = true;
		while( true ) {        
			try {
				RocketChatSet set = new RocketChatSet( server , account , password );
				set.setInclude( chatInclude );
				set.setExclude( chatExclude );
				set.executeChatSet();
			}
        	catch (Exception e) {
                e.printStackTrace();
                
        		if( first ) {
        			System.out.print( "unable to connect to server. Exiting" );
        			return;
        		}
			}
			
			first = false;
		}
	}

}
