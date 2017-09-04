package org.urm.common.jmx;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.urm.common.action.CommandMeta;
import org.urm.engine.Engine;
import org.urm.engine.SessionController;
import org.urm.engine.action.ActionInit;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.UrmStorage;
import org.urm.meta.EngineLoader;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.EngineSettings;

import com.sun.jdmk.comm.HtmlAdaptorServer;

public class EngineMBean implements DynamicMBean {

	Engine engine;
	ActionInit action;
	
	private MBeanServer mbs = null;
	private MBeanInfo mbean = null;
	JMXConnectorServer jmxConnector;
	
	public EngineMBean( ActionInit action , Engine engine ) {
		this.action = action;
		this.engine = engine;
	}
	
	public void start() throws Exception {
		action.debug( "start JMX server ..." );
		int port = action.context.CTX_PORT;
		if( port <= 0 ) {
			EngineLoader loader = engine.getLoader( action );
			EngineSettings settings = loader.getServerSettings();
			port = settings.serverContext.CONNECTION_JMX_PORT;
			if( port <= 0 )
				port = RemoteCall.DEFAULT_SERVER_PORT;
		}
		
		// add mbeans
		mbs = MBeanServerFactory.createMBeanServer();
		addServer();
		addProducts();
		addHtmlAdapter( port + 1 );
		
        // create jmx connector
		String host = action.context.CTX_HOST;
		if( host.isEmpty() )
			host = "localhost";
		
		String URL = "service:jmx:jmxmp://" + host + ":" + port;
		action.debug( "register JMX on " + URL + " ..." );
		
        JMXServiceURL url = new JMXServiceURL( URL );
        jmxConnector = JMXConnectorServerFactory.newJMXConnectorServer( url , null , mbs );
        jmxConnector.start();
		action.debug( "JMX server has been started" );
	}

	public void stop() throws Exception {
		action.debug( "stop JMX server ..." );
		jmxConnector.stop();
		jmxConnector = null;
		mbean = null;
		mbs = null;
		action.debug( "JMX server has been stopped" );
	}
	
	private String status() {
		if( engine.isRunning() )
			return( "running" );
		return( "stopped" );
	}
	
	private String stopServer() {
		try {
			engine.stopServer();
		}
		catch( Throwable e ) {
			return( "failed: " + e.getMessage() );
		}
		
		return( "ok" );
	}
	
	private void addServer() throws Exception {
		String name = RemoteCall.getServerMBeanName();
		ObjectName object = new ObjectName( name );

		// produce operating information
		MBeanOperationInfo opStatus = new MBeanOperationInfo( "status" ,
				"get server operating status" ,
				null , 
				"String" , 
				MBeanOperationInfo.INFO );
		MBeanOperationInfo opStop = new MBeanOperationInfo( "stop" ,
				"stop server" ,
				null , 
				"String" , 
				MBeanOperationInfo.ACTION );
		mbean = new MBeanInfo(
				this.getClass().getName() ,
				"URM server JMX" ,
	            null ,
	            null , 
	            new MBeanOperationInfo[] { opStatus , opStop } ,
	            null );

		// register server as mbean
		mbs.registerMBean( this , object );
	}
	
	private void addHtmlAdapter( int port ) throws Exception {
		ObjectName adapterName = new ObjectName( "urm:name=HtmlAdapter" );
		
		HtmlAdaptorServer adapter = new HtmlAdaptorServer();
        adapter.setPort( port );
        mbs.registerMBean( adapter , adapterName );
        adapter.start();
	}
	
	private void addProducts() throws Exception {
		// create meta jmx for products
		UrmStorage urm = action.artefactory.getUrmStorage();
		LocalFolder products = urm.getServerProductsFolder( action );
		if( !products.checkExists( action ) )
			action.exit1( _Error.CannotFindDirectory1 , "cannot find directory: " + products.folderPath , products.folderPath );
		
		EngineDirectory directory = action.actionInit.getServerDirectory();
		for( String name : directory.getProducts() )
			addProduct( name );
	}		
	
	private void addProduct( String product ) throws Exception {
		SessionController sessionController = engine.sessionController;
		for( CommandMeta meta : sessionController.executors  ) {
			EngineCommandMBean bean = new EngineCommandMBean( action , action.executor.engine , this , product , meta );
			bean.createInfo();
			
			String name = RemoteCall.getCommandMBeanName( product , meta.name );
			ObjectName object = new ObjectName( name );
			mbs.registerMBean( bean , object );
		}
	}

	@Override
	public synchronized String getAttribute( String name ) throws AttributeNotFoundException {
		return( null );
	}

	@Override
	public synchronized void setAttribute( Attribute attribute) throws InvalidAttributeValueException, MBeanException, AttributeNotFoundException {
	}

	@Override
	public synchronized AttributeList getAttributes(String[] names) {
        return( null );
	}

	@Override
	public synchronized AttributeList setAttributes(AttributeList list) {
    	Attribute[] attrs = (Attribute[]) list.toArray( new Attribute[0] );
    	AttributeList retlist = new AttributeList();
        
    	for (Attribute attr : attrs) {
    		String name = attr.getName();
    		Object value = attr.getValue();
    		retlist.add( new Attribute(name, value) );
    	}
        
    	return retlist;
	}

	@Override
	public Object invoke( String name , Object[] args , String[] sig ) throws MBeanException, ReflectionException {
		int sessionId = -1;
		try {
			if( name.equals( "stop" ) )
				return( stopServer() );
			if( name.equals( "status" ) )
				return( status() );
			return( null );
		}
		catch( Throwable e ) {
			action.error( e.getMessage() );
		}

		String value = "" + sessionId;
		return( value );
	}

	@Override
	public synchronized MBeanInfo getMBeanInfo() {
		return( mbean );
	}

}
