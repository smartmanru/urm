package org.urm.messenger;

import java.io.FileInputStream;
import java.util.Properties;

import org.urm.messenger.jabber.JabberMain;
import org.urm.messenger.rocket.RocketMain;
import org.urm.messenger.skype.SkypeMain;

public class ChatMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if( args.length < 2 ) {
			System.out.print( "run: ChatMain {skype|jabber|rocket} <path-to-config-file>" );
			return;
		}
		
		try {
			String type = args[ 0 ];
			String file = args[ 1 ];
        	
			Properties props = new Properties();
			FileInputStream streamIn = new FileInputStream( file );
			props.load( streamIn );
        	
			execute( type , props);
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}

	}

	public static void execute( String type , Properties props ) throws Exception {
		if( type.equals( "skype" ) ) {
			SkypeMain main = new SkypeMain();
			main.execute( props );
		}
		else if( type.equals( "jabber" ) ) {
			JabberMain main = new JabberMain();
			main.execute( props );
		}
		else if( type.equals( "rocket" ) ) {
			RocketMain main = new RocketMain();
			main.execute( props );
		}
		else {
			System.out.println( "unknown chat type=" + type );
		}
	}
		
}
