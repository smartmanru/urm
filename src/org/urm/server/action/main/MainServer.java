package org.urm.server.action.main;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;
import org.urm.server.action.ActionBase;
import org.urm.server.storage.LocalFolder;
import org.urm.server.storage.UrmStorage;

import com.sun.jdmk.comm.HtmlAdaptorServer;

public class MainServer {

	public static int DEFAULT_SERVER_PORT = 8800;
	
	private MBeanServer mbs = null;
	CommandMeta[] executors = null;
	JMXConnectorServer jmxConnector;
	
	public void start( ActionBase action ) throws Exception {
		mbs = MBeanServerFactory.createMBeanServer();
		HtmlAdaptorServer adapter = new HtmlAdaptorServer();
		
		CommandBuilder builder = new CommandBuilder( action.context.rc );
		executors = builder.getExecutors( true , true );
		
		// create meta jmx for products
		UrmStorage urm = action.artefactory.getUrmStorage();
		LocalFolder products = urm.getServerProductsFolder( action );
		if( !products.checkExists( action ) )
			action.exit( "cannot find directory: " + products.folderPath );
		
		for( String productDir : products.getTopDirs( action ) )
			addProduct( action , productDir );
		
		int port = action.context.CTX_PORT;
		if( port <= 0 )
			port = DEFAULT_SERVER_PORT;
		
		ObjectName adapterName = new ObjectName( "urm:name=HtmlAdapter" );
        adapter.setPort( port + 1 );
        mbs.registerMBean( adapter , adapterName );
        
        adapter.start();
        
        // create jmx
        JMXServiceURL URL = new JMXServiceURL( "service:jmx:rmi:///jndi/rmi://:" + port + "/jmxrmi"  );
        jmxConnector = JMXConnectorServerFactory.newJMXConnectorServer( URL , null , mbs );
        jmxConnector.start();
	}

	private void addProduct( ActionBase action , String productDir ) throws Exception {
		String nameBase = "urm-" + productDir + ":";
		for( CommandMeta meta : executors  ) {
			MainServerMBean bean = new MainServerMBean( action.executor.engine , productDir , meta );
			bean.createInfo( action );
			
			ObjectName name = new ObjectName( nameBase + "name=" + meta.name );
			mbs.registerMBean( bean , name );
		}
	}
	
}
