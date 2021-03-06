package org.urm.messenger.rocket;

import java.util.Properties;

public class RocketMain {

	public void execute( Properties props ) throws Exception {
		String server = props.getProperty( "server" );
		String account = props.getProperty( "account" );
		String password = props.getProperty( "password" );
		String chatInclude = props.getProperty( "include" );
		String chatExclude = props.getProperty( "exclude" );
		
		while( true ) {        
			RocketChatSet set = new RocketChatSet( server , account , password );
			try {
				set.setInclude( chatInclude );
				set.setExclude( chatExclude );
				set.executeChatSet();
			}
        	catch (Exception e) {
                e.printStackTrace();
                
        		if( set.first ) {
        			System.out.print( "unable to connect to server. Exiting" );
        			return;
        		}
			}
		}
	}

}
