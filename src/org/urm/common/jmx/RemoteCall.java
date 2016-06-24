package org.urm.common.jmx;

import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.urm.common.action.ActionData;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;

public class RemoteCall implements NotificationListener {

	public static String GENERIC_ACTION_NAME = "execute";

	public static String getCommandMBeanName( String productDir , String command ) {
		return( "urm-" + productDir + ":" + "name=" + command );
	}
	
	public static String getServerMBeanName() {
		return( "urm:name=server" );
	}
	
	public boolean runClient( CommandBuilder builder , CommandMeta commandInfo ) throws Exception {
		String URL = "service:jmx:jmxmp://" + builder.execrc.serverHostPort;
		JMXServiceURL url = new JMXServiceURL( URL );
		
		JMXConnector jmxc = null;
		MBeanServerConnection mbsc = null;
		
		try {
			jmxc = JMXConnectorFactory.connect( url , null );
			mbsc = jmxc.getMBeanServerConnection();
		}
		catch( Throwable e ) {
			System.out.println( "unable to connect to: " + URL );
			e.printStackTrace();
			return( false );
		}
		
		String name = getCommandMBeanName( builder.execrc.productDir , commandInfo.name );
		boolean res = makeCall( builder , name , mbsc );
		
		try {
			jmxc.close();
		}
		catch( Throwable e ) {
		}
		
		return( res );
	}

	private boolean makeCall( CommandBuilder builder , String name , MBeanServerConnection mbsc ) {
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
