package org.urm.client;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;

public class ClientCallRemote {

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
		try {
			ObjectName mbeanName = new ObjectName( name );
			mbsc.invoke( mbeanName , builder.options.action , null , null );
		}
		catch( Throwable e ) {
			System.out.println( "unable to call operation: " + name );
			e.printStackTrace();
			return( false );
		}
		
		return( true );
	}
	
}
