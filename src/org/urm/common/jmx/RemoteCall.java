package org.urm.common.jmx;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.urm.common.RunContext;
import org.urm.common.action.ActionData;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;

public class RemoteCall implements NotificationListener {

	public static String GENERIC_ACTION_NAME = "execute";
	public static String INPUT_ACTION_NAME = "input";
	public static int DEFAULT_SERVER_PORT = 8800;
	
	public String URL;
	ObjectName mbeanName;
	JMXConnector jmxc = null;
	MBeanServerConnection mbsc = null;

	public static final String EXIT_COMMAND = "exit";
	
	public static String getCommandMBeanName( String productDir , String command ) {
		return( "urm-" + productDir + ":" + "name=" + command );
	}
	
	public static String getServerMBeanName() {
		return( "urm:name=server" );
	}
	
	public boolean runClient( CommandBuilder builder , CommandMeta commandInfo ) throws Exception {
		if( !serverConnect( builder.execrc ) ) {
			System.out.println( "unable to connect to: " + URL );
			return( false );
		}
		
		String name = getCommandMBeanName( builder.execrc.productDir , commandInfo.name );
		boolean res = serverCommandCall( builder , name );

		serverDisconnect();
		return( res );
	}

	public void serverDisconnect() {
		try {
			if( mbsc != null )
				mbsc.removeNotificationListener( mbeanName , this );
			
			if( jmxc != null ) {
				jmxc.close();
				jmxc = null;
			}
		}
		catch( Throwable e ) {
		}
	}
	
	public boolean serverConnect( RunContext execrc ) {
		return( serverConnect( execrc.serverHostPort ) );
	}
	
	public boolean serverConnect( String serverHostPort ) {
		URL = "service:jmx:jmxmp://" + serverHostPort;
		
		try {
			JMXServiceURL url = new JMXServiceURL( URL );
			jmxc = JMXConnectorFactory.connect( url , null );
			mbsc = jmxc.getMBeanServerConnection();
		}
		catch( Throwable e ) {
			serverDisconnect();
			return( false );
		}
		
		return( true );
	}

	public String serverCall( String method ) throws Exception {
		String name = getServerMBeanName();
		try {
			ObjectName mbeanName = new ObjectName( name );
			String res = ( String )mbsc.invoke( mbeanName , method , null , null );
			return( res );
		}
		catch( Throwable e ) {
			return( "error: " + e.getMessage() );
		}
	}
	
	private boolean serverCommandCall( CommandBuilder builder , String name ) {
		String sessionId;
		try {
			String clientId = builder.options.action + "-" + System.currentTimeMillis();
			mbeanName = new ObjectName( name );
			RemoteCallFilter filter = new RemoteCallFilter( clientId );
			
			mbsc.addNotificationListener( mbeanName , this , filter , clientId );
			sessionId = ( String )mbsc.invoke( mbeanName , GENERIC_ACTION_NAME , 
					new Object[] { builder.options.action , builder.options.data , clientId } , 
					new String[] { String.class.getName() , ActionData.class.getName() , String.class.getName() } );
		}
		catch( Throwable e ) {
			System.out.println( "unable to call operation: " + name );
			e.printStackTrace();
			return( false );
		}

		// silent wait for completion or allow to send input strings
		if( sessionId == null ) {
			System.out.println( "server rejected to call operation: " + name );
			return( false );
		}
		
		try {
			if( builder.isInteractive() )
				waitInteractive( sessionId );
			else {
				synchronized( this ) {
					wait();
				}
			}
		}
		catch( Throwable e ) {
			return( false );
		}
		
		return( true );
	}

	private void waitInteractive( String sessionId ) throws Exception {
		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
		System.out.println( "enter commands, or '" + EXIT_COMMAND + "' to quit:" );
		
		while( true ) {
			System.out.print( "> " );
			String input = br.readLine();
			System.out.println( input );

			if( input.length() == EXIT_COMMAND.length() && input.toLowerCase().equals( EXIT_COMMAND ) ) {
				System.out.println( "exiting ..." );
				return;
			}
			
			mbsc.invoke( mbeanName , INPUT_ACTION_NAME , 
					new Object[] { sessionId , input } , 
					new String[] { String.class.getName() , String.class.getName() } );
		}
	}
	
	public void handleNotification( Notification notif , Object handback ) {
		if( !notif.getType().equals( ActionNotification.EVENT ) )
			return;
		
		ActionNotification n = ( ActionNotification )notif;
		if( n.logEvent )
			System.out.println( n.getMessage() );
		else
		if( n.stopEvent ) {
			synchronized( this ) {
				notifyAll();
			}
		}
	}
	
}
