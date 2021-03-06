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

import org.urm.client.ClientAuth;
import org.urm.client.ClientEngine;
import org.urm.common.RunContext;
import org.urm.common.action.ActionData;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandOptions;
import org.urm.common.action.OptionsMeta;

public class RemoteCall implements NotificationListener {

	public static String GENERIC_ACTION_NAME = "execute";
	public static String INPUT_ACTION_NAME = "input";
	public static String STOP_ACTION_NAME = "stop";
	public static String WAITCONNECT_ACTION_NAME = "waitconnect";
	
	public static String STATUS_ACTION_FAILED = "failed";
	public static String STATUS_ACTION_CONNECTED = "connected";
	
	public static int DEFAULT_SERVER_PORT = 8800;

	ClientEngine client;
	CommandOptions options;
	
	public String URL;
	ClientAuth auth;
	ObjectName mbeanName;
	JMXConnector jmxc = null;
	MBeanServerConnection mbsc = null;
	BufferedReader br = null;
	Thread mainThread;
	InputStreamReader isr;

	boolean finished = false;
	boolean connected = false;
	boolean stopwait = false;
	
	public static final String EXIT_COMMAND = "exit";

	private boolean trace;
	private int timeout;
	private long tsEvent = 0;
	
	public RemoteCall( ClientEngine client , CommandOptions options ) {
		this.client = client;
		this.options = options;
		
		String var = OptionsMeta.OPT_TRACE;
		trace = options.getFlagValue( var , false );
		var = OptionsMeta.OPT_TIMEOUT;
		timeout = options.getIntParamValue( var , options.optDefaultCommandTimeout );
	}
	
	public static String getCommandMBeanName( String productDir , String command ) {
		return( "urm-" + productDir + ":" + "name=" + command );
	}
	
	public static String getServerMBeanName() {
		return( "urm:name=server" );
	}
	
	private synchronized void println( String s ) {
		client.println( s );
	}
	
	public boolean runClient( CommandBuilder builder , CommandMeta commandInfo , ClientAuth auth ) throws Exception {
		if( !serverConnect( builder.execrc , auth ) ) {
			serverDisconnect();
			println( "unable to connect to: " + URL );
			return( false );
		}
		
		String name = getCommandMBeanName( builder.execrc.product , commandInfo.name );
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
	
	private boolean serverConnect( RunContext execrc , ClientAuth auth ) {
		return( serverConnect( execrc.serverHostPort , auth ) );
	}
	
	public boolean serverConnect( String serverHostPort , ClientAuth auth ) {
		this.auth = auth;
		
		try {
			URL = "service:jmx:jmxmp://" + serverHostPort;
			JMXServiceURL url = new JMXServiceURL( URL );
			jmxc = JMXConnectorFactory.connect( url , null );
			mbsc = jmxc.getMBeanServerConnection();
			if( mbsc == null )
				return( false );
		}
		catch( Throwable e ) {
			println( e.toString() );
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
			return( "error: " + e.toString() );
		}
	}
	
	private boolean serverCommandCall( CommandBuilder builder , String name ) {
		String sessionId;
		try {
			String clientId = options.method + "-" + System.currentTimeMillis();
			mbeanName = new ObjectName( name );
			RemoteCallFilter filter = new RemoteCallFilter( clientId );
			
			stopwait = false;
			mbsc.addNotificationListener( mbeanName , this , filter , clientId );
			sessionId = ( String )mbsc.invoke( mbeanName , GENERIC_ACTION_NAME , 
					new Object[] { options.method , options.data , clientId , auth.authUser , auth.authPassword } , 
					new String[] { String.class.getName() , ActionData.class.getName() , String.class.getName() , String.class.getName() , String.class.getName() } );
		}
		catch( Throwable e ) {
			println( "unable to call operation: " + name );
			client.output( e );
			return( false );
		}

		// silent wait for completion or allow to send input strings
		if( sessionId == null ) {
			println( "server rejected to call operation: " + name );
			return( false );
		}
		
		try {
			if( builder.isInteractive( options ) )
				waitInteractive( sessionId );
			else {
				synchronized( this ) {
					executeWait();
				}
			}
		}
		catch( Throwable e ) {
			return( false );
		}
		
		return( true );
	}

	private void waitInteractive( String sessionId ) throws Exception {
		mainThread = Thread.currentThread();
		
		// wait for connect to succeed
		if( !connectInteractive( sessionId ) )
			return;
		
		isr = new InputStreamReader( System.in );
		br = new BufferedReader( isr );
		println( "enter commands, or '" + EXIT_COMMAND + "' to quit:" );
		
		String input;
		while( !finished ) {
			try {
				System.out.print( "$ " );
				input = br.readLine();
			}
			catch( Throwable e ) {
				break;
			}
			
			if( finished ) {
				println( "session is closed" );
				break;
			}
			
			if( input.length() == EXIT_COMMAND.length() && input.toLowerCase().equals( EXIT_COMMAND ) )
				break;
			
			sendInput( sessionId , input );
		}

		println( "exiting ..." );
		mbsc.invoke( mbeanName , STOP_ACTION_NAME , 
				new Object[] { sessionId } , 
				new String[] { String.class.getName() } );
	}

	private boolean connectInteractive( String sessionId ) throws Exception {
		String ready = ( String )mbsc.invoke( mbeanName , WAITCONNECT_ACTION_NAME , 
				new Object[] { sessionId } , 
				new String[] { String.class.getName() } );
		
		synchronized( this ) {
			try {
				if( finished == false && connected == false )
					executeWait();
			}
			catch( Throwable e ) {
			}
		}

		if( !ready.equals( STATUS_ACTION_CONNECTED ) )
			connected = false;
		
		return( connected );
	}

	private void sendInput( String sessionId , String input ) throws Exception {
		stopwait = false;
		mbsc.invoke( mbeanName , INPUT_ACTION_NAME , 
				new Object[] { sessionId , input } , 
				new String[] { String.class.getName() , String.class.getName() } );
		
		synchronized( this ) {
			executeWait();
		}
	}
	
	public void handleNotification( Notification notif , Object handback ) {
		if( !notif.getType().equals( ActionNotification.EVENT ) )
			return;
		
		ActionNotification n = ( ActionNotification )notif;
		tsEvent = System.currentTimeMillis();
		
		if( n.isLog() )
			println( n.getMessage() );
		else
		if( n.isConnected() ) {
			println( n.getMessage() );
			synchronized( this ) {
				stopwait = true;
				connected = true;
				notifyAll();
			}
		}
		else
		if( n.isCommandFinished() ) {
			synchronized( this ) {
				stopwait = true;
				notifyAll();
			}
		}
		else
		if( n.isStop() ) {
			try {
				if( mainThread != null )
					mainThread.interrupt();
				println( n.getMessage() );
			}
			catch( Throwable e ) {
				if( trace )
					client.output( e );
			}
			
			synchronized( this ) {
				stopwait = true;
				finished = true;
				notifyAll();
			}
		}
	}
	
	private void executeWait() throws Exception {
		int tm = timeout;
		if( tm > 0 )
			tm += 5;

		while( true ) {
			try {
				wait( tm * 1000 );
				if( stopwait )
					return;
			}
			catch( Throwable e ) {
				if( stopwait )
					return;
				
				long tsCurrent = System.currentTimeMillis();
				if( tsCurrent - tsEvent > tm * 1000 )
					throw e;
			}
		}
	}
	
}
