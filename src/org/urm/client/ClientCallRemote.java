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
		JMXServiceURL url = new JMXServiceURL( "service:jmx:rmi:///jndi/rmi://" + builder.rc.serverHostPort + "/jmxrmi" );
		JMXConnector jmxc = JMXConnectorFactory.connect( url , null );
		MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
		
		System.out.println("\nDomains:");
		String domains[] = mbsc.getDomains();
		Arrays.sort(domains);
		for (String domain : domains) {
			System.out.println("\tDomain = " + domain);
		}
		
		throw new ExitException( "sorry, not implemented yet" );
	}
	
}
