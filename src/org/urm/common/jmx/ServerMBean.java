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
import org.urm.server.SessionController;
import org.urm.server.action.ActionBase;
import org.urm.server.storage.LocalFolder;
import org.urm.server.storage.UrmStorage;

import com.sun.jdmk.comm.HtmlAdaptorServer;

public class ServerMBean implements DynamicMBean {

	ActionBase action;
	SessionController sessionController;
	
	private MBeanServer mbs = null;
	private MBeanInfo mbean = null;
	JMXConnectorServer jmxConnector;
	
	public ServerMBean( ActionBase action , SessionController server ) {
		this.action = action;
		this.sessionController = server;
	}
	
	public void start() throws Exception {
		int port = action.context.CTX_PORT;
		if( port <= 0 )
			port = RemoteCall.DEFAULT_SERVER_PORT;
		
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
	}

	private String stop() {
		try {
			sessionController.stop();
		}
		catch( Throwable e ) {
			return( "failed: " + e.getMessage() );
		}
		return( "ok" );
	}
	
	private String status() {
		if( sessionController.isRunning() )
			return( "running" );
		return( "stopped" );
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
			action.exit( "cannot find directory: " + products.folderPath );
		
		for( String name : action.engine.getProducts() )
			addProduct( name );
	}		
	
	private void addProduct( String product ) throws Exception {
		for( CommandMeta meta : sessionController.executors  ) {
			ServerCommandMBean bean = new ServerCommandMBean( action , this , action.executor.engine , product , meta );
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
				return( stop() );
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
