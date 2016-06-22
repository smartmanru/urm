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

public class ClientCallRemote implements NotificationListener {

	public boolean runClient( CommandBuilder builder , CommandMeta commandInfo ) throws Exception {
		String URL = "service:jmx:rmi:///jndi/rmi://" + builder.execrc.serverHostPort + "/jmxrmi";
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
		
		String name = builder.getCommandMBeanName( builder.execrc.productDir , commandInfo.name );
		Object sessionId;
		try {
			ObjectName mbeanName = new ObjectName( name );
			mbsc.addNotificationListener( mbeanName , this , null , null );
			sessionId = mbsc.invoke( mbeanName , "execute" , 
					new Object[] { builder.options.data } , 
					new String[] { ActionData.class.getName() } );
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
		
		synchronized( this ) {
			wait();
		}
		
		return( true );
	}

	public void handleNotification( Notification notif , Object handback ) {
		if( notif.getType().equals( ActionLogNotification.EVENT ) ) {
			ActionLogNotification n = ( ActionLogNotification )notif;
			System.out.println( n.getMessage() );
		}
		else if( notif.getType().equals( ActionStopNotification.EVENT ) ) {
			ActionStopNotification n = ( ActionStopNotification )notif;
			System.out.println( n.getMessage() );
			notify();
		}
	}
	
}
