package org.urm.common.jmx;

import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.urm.common.action.CommandMeta;
import org.urm.server.action.ActionBase;
import org.urm.server.action.main.MainServer;
import org.urm.server.storage.LocalFolder;
import org.urm.server.storage.UrmStorage;

import com.sun.jdmk.comm.HtmlAdaptorServer;

public class Controller {

	public static int DEFAULT_SERVER_PORT = 8800;
	
	ActionBase action;
	MainServer server;
	
	private MBeanServer mbs = null;
	JMXConnectorServer jmxConnector;
	Map<String,ServerCommandThread> threads;
	
	public Controller( ActionBase action , MainServer server ) {
		this.action = action;
		this.server = server;
		threads = new HashMap<String,ServerCommandThread>();
	}
	
	public void start() throws Exception {
		mbs = MBeanServerFactory.createMBeanServer();
		HtmlAdaptorServer adapter = new HtmlAdaptorServer();
		
		// create meta jmx for products
		UrmStorage urm = action.artefactory.getUrmStorage();
		LocalFolder products = urm.getServerProductsFolder( action );
		if( !products.checkExists( action ) )
			action.exit( "cannot find directory: " + products.folderPath );
		
		for( String productDir : products.getTopDirs( action ) )
			addProduct( productDir );
		
		int port = action.context.CTX_PORT;
		if( port <= 0 )
			port = DEFAULT_SERVER_PORT;
		
		ObjectName adapterName = new ObjectName( "urm:name=HtmlAdapter" );
        adapter.setPort( port + 1 );
        mbs.registerMBean( adapter , adapterName );
        
        adapter.start();
        
        // create jmx
		String host = action.context.CTX_HOST;
		if( host.isEmpty() )
			host = "localhost";
		
		String URL = "service:jmx:jmxmp://" + host + ":" + port;
		action.debug( "register JMX on " + URL + " ..." );
		
        JMXServiceURL url = new JMXServiceURL( URL );
        jmxConnector = JMXConnectorServerFactory.newJMXConnectorServer( url , null , mbs );
        jmxConnector.start();
	}

	private void addProduct( String productDir ) throws Exception {
		for( CommandMeta meta : server.executors  ) {
			ServerCommandMBean bean = new ServerCommandMBean( action , this , action.executor.engine , productDir , meta );
			bean.createInfo();
			
			String name = action.executor.commandInfo.builder.getCommandMBeanName( productDir , meta.name );
			ObjectName object = new ObjectName( name );
			mbs.registerMBean( bean , object );
		}
	}

	public synchronized void threadStarted( ServerCommandThread thread ) {
		threads.put( "" + thread.sessionId , thread );
		action.debug( "thread started: " + thread.sessionId );
	}

	public synchronized void threadStopped( ServerCommandThread thread ) {
		threads.remove( "" + thread.sessionId );
		action.debug( "thread stopped: " + thread.sessionId );
	}

}
