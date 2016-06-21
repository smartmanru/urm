package org.urm.client;

import java.util.Arrays;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.urm.common.ExitException;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;

public class ClientCallRemote {

	public boolean runClient( CommandBuilder builder , CommandMeta commandInfo ) throws Exception {
		String URL = "service:jmx:rmi:///jndi/rmi://" + builder.rc.serverHostPort + "/jmxrmi";
		JMXServiceURL url = new JMXServiceURL( URL );
		
		JMXConnector jmxc = null;
		MBeanServerConnection mbsc = null;
		
		try {
			jmxc = JMXConnectorFactory.connect( url , null );
			mbsc = jmxc.getMBeanServerConnection();
		}
		catch( Throwable e ) {
			System.out.println( "unable to connect to: " + URL );
		}
		
		System.out.println("\nDomains:");
		String domains[] = mbsc.getDomains();
		Arrays.sort(domains);
		for (String domain : domains) {
			System.out.println("\tDomain = " + domain);
		}
		
		throw new ExitException( "sorry, not implemented yet" );
	}
	
}
