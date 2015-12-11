package ru.egov.urm.messenger.jabber;

import java.io.FileInputStream;
import java.util.Properties;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;

public class JabberMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if( args.length < 1 ) {
			System.out.print( "run: JabberMain <path-to-config-file>" );
			return;
		}
		
		try {
			String file = args[ 0 ];
        	
			Properties props = new Properties();
			FileInputStream streamIn = new FileInputStream( file );
			props.load( streamIn );
        	
			execute(props);
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public static void execute(Properties props) {
		String account = props.getProperty( "account" );
		String server = props.getProperty( "server" );
		String password = props.getProperty( "password" );
		String conferenceserver = props.getProperty( "conferenceserver" );
		String chatInclude = props.getProperty( "include" );
		String chatExclude = props.getProperty( "exclude" );

		if( account == null || password == null || server == null ) {
			System.out.print( "account, password and server properties are required" );
			return;
		}
    	
		String[] items = account.split( "@" );
		if( items == null || items.length != 2 ) {
			System.out.print( "account should be user@domain" );
			return;
		}
    	
		String nick = items[0];
		String domain = items[1];
		int port = 5222;
        
		boolean first = true;
		while( true ) {        
       		ConnectionConfiguration connConfig = new ConnectionConfiguration(server, port, domain);
	       	XMPPConnection connection = new XMPPConnection(connConfig);

			try {
				int priority = 10;
				SASLAuthentication.supportSASLMechanism("PLAIN", 0);
				connection.connect();
				connection.login(nick, password);
				Presence presence = new Presence(Presence.Type.available);
				presence.setStatus("Available");
				connection.sendPacket(presence);
				presence.setPriority(priority);
            
				JabberChatSet set = new JabberChatSet( connection , conferenceserver );
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
