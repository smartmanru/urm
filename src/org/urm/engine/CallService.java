package org.urm.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import org.urm.common.jmx.RemoteCall;
import org.urm.engine.action.ActionInit;
import org.urm.engine.data.EngineDirectory;
import org.urm.engine.data.EngineSettings;
import org.urm.engine.jmx.EngineCommandMBean;
import org.urm.meta.engine.AppProduct;

import com.sun.jdmk.comm.HtmlAdaptorServer;

public class CallService {

	class EngineMBean implements DynamicMBean {
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
				action.error( e.toString() );
			}

			String value = "" + sessionId;
			return( value );
		}

		@Override
		public synchronized MBeanInfo getMBeanInfo() {
			return( mbean );
		}
	}
	
	Engine engine;
	ActionInit action;
	
	private EngineMBean jmxOwner;
	private MBeanServer mbs = null;
	private MBeanInfo mbean = null;
	JMXConnectorServer jmxConnector;

	Map<Integer,List<String>> productObjects;
	
	public CallService( ActionInit action , Engine engine ) {
		this.action = action;
		this.engine = engine;
		productObjects = new HashMap<Integer,List<String>>();  
	}
	
	public void start() throws Exception {
		action.debug( "start JMX server ..." );
		
		jmxOwner = new EngineMBean();
		int port = action.context.CTX_PORT;
		if( port <= 0 ) {
			EngineSettings settings = action.getEngineSettings();
			port = settings.context.CONNECTION_JMX_PORT;
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
		jmxOwner = null;
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
			return( "failed: " + e.toString() );
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
				EngineMBean.class.getName() ,
				"URM server JMX" ,
	            null ,
	            null , 
	            new MBeanOperationInfo[] { opStatus , opStop } ,
	            null );

		// register server as mbean
		mbs.registerMBean( jmxOwner , object );
	}
	
	private void addHtmlAdapter( int port ) throws Exception {
		ObjectName adapterName = new ObjectName( "urm:name=HtmlAdapter" );
		
		HtmlAdaptorServer adapter = new HtmlAdaptorServer();
        adapter.setPort( port );
        mbs.registerMBean( adapter , adapterName );
        adapter.start();
	}
	
	public void addProducts() throws Exception {
		// create meta jmx for products
		EngineDirectory directory = action.actionInit.getEngineDirectory();
		for( String name : directory.getProductNames() ) {
			AppProduct product = directory.getProduct( name );
			if( product.isMatched() )
				addProduct( product );
		}
	}		
	
	public void addProduct( AppProduct product ) throws Exception {
		action.trace( "register jmx connector, product=" + product.NAME + " ..." );
		List<String> objects = new LinkedList<String>();
		productObjects.put( product.ID , objects );
		
		SessionService sessionController = engine.sessions;
		for( CommandMeta meta : sessionController.executors  ) {
			EngineCommandMBean bean = new EngineCommandMBean( action , action.executor.engine , this , product , meta );
			bean.createInfo();
			
			String name = RemoteCall.getCommandMBeanName( product.NAME , meta.name );
			ObjectName object = new ObjectName( name );
			mbs.registerMBean( bean , object );
			objects.add( name );
		}
	}

	public void deleteProduct( AppProduct product ) throws Exception {
		action.trace( "unregister jmx connector, product=" + product.NAME + " ..." );
		List<String> objects = productObjects.get( product.ID );
		if( objects == null )
			return;
		
		for( String name : objects ) {
			ObjectName object = new ObjectName( name );
			mbs.unregisterMBean( object );
		}
		
		productObjects.remove( product.ID );
	}
	
}
