package org.urm.common.jmx;

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
import org.urm.server.action.ActionBase;

public class RemoteCall implements NotificationListener {

	public static String GENERIC_ACTION_NAME = "execute";
	
	public String URL;
	JMXConnector jmxc = null;
	MBeanServerConnection mbsc = null;

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
			if( jmxc != null ) {
				jmxc.close();
				jmxc = null;
			}
		}
		catch( Throwable e ) {
		}
	}
	
	public boolean serverConnect( RunContext execrc ) {
		URL = "service:jmx:jmxmp://" + execrc.serverHostPort;
		
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

	public String serverCall( ActionBase action , String method ) throws Exception {
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
		Object sessionId;
		try {
			ObjectName mbeanName = new ObjectName( name );
			mbsc.addNotificationListener( mbeanName , this , null , null );
			sessionId = mbsc.invoke( mbeanName , GENERIC_ACTION_NAME , 
					new Object[] { builder.options.action , builder.options.data } , 
					new String[] { String.class.getName() , ActionData.class.getName() } );
		}
		catch( Throwable e ) {
			System.out.println( "unable to call operation: " + name );
			e.printStackTrace();
			return( false );
		}

		if( sessionId == null ) {
			System.out.println( "server rejected to call operation: " + name );
			return( false );
		}
		
		try {
			synchronized( this ) {
				wait();
			}
		}
		catch( Throwable e ) {
			return( false );
		}
		
		return( true );
	}
	
	public void handleNotification( Notification notif , Object handback ) {
		if( notif.getType().equals( ActionLogNotification.EVENT ) ) {
			ActionLogNotification n = ( ActionLogNotification )notif;
			System.out.println( n.getMessage() );
		}
		else if( notif.getType().equals( ActionStopNotification.EVENT ) ) {
			synchronized( this ) {
				notifyAll();
			}
		}
	}
	
}
