package org.urm.common.jmx;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ReflectionException;

import org.urm.common.Common;
import org.urm.common.action.ActionData;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandMethod;
import org.urm.common.action.CommandMethod.ACTION_TYPE;
import org.urm.common.action.CommandOptions;
import org.urm.common.action.CommandVar;
import org.urm.common.action.CommandVar.FLAG;
import org.urm.server.MainServer;
import org.urm.server.ServerEngine;
import org.urm.server.action.ActionBase;

public class ServerCommandMBean implements DynamicMBean, NotificationBroadcaster {

	int notificationSequence = 0;
	NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport(); 
	MBeanNotificationInfo[] notifyInfo;
	
	public ActionBase action;
	public ServerMBean controller;
	public ServerEngine engine;
	public MainServer server;
	public String productDir;
	
	public CommandMeta meta;
	public MBeanInfo mbean;
	public CommandOptions options;
	
	public ServerCommandMBean( ActionBase action , ServerMBean controller , ServerEngine engine , String productDir , CommandMeta meta ) {
		this.action = action;
		this.controller = controller;
		this.engine = engine;
		this.productDir = productDir;
		this.meta = meta;
		
		server = controller.server;
	}

	public void createInfo() throws Exception {
		options = new CommandOptions();
		ActionData data = new ActionData( engine.execrc );
		options.setCommand( meta.name , data );
		
		// attributes
		List<MBeanAttributeInfo> attrs = new LinkedList<MBeanAttributeInfo>();
		for( CommandVar var : options.getDefinedVariables().values() ) {
			if( var.isGeneric && var.jmx ) {
				MBeanAttributeInfo attr = addGenericOption( action , var );
				attrs.add( attr );
			}
		}

		// operations
		List<MBeanOperationInfo> opers = new LinkedList<MBeanOperationInfo>();
		for( String methodName : Common.getSortedKeys( meta.actionsMap ) ) {
			CommandMethod method = meta.actionsMap.get( methodName ); 
			MBeanOperationInfo op = addOperation( action , method );
			opers.add( op );
		}
		
		// notifications
		MBeanNotificationInfo mbn = new MBeanNotificationInfo( new String[] { ActionNotification.EVENT } , 
				ActionNotification.class.getName() , "action event" );
		notifyInfo = new MBeanNotificationInfo[] { mbn };
		
		// register
		Collections.reverse( opers );
		mbean = new MBeanInfo(
			this.getClass().getName() ,
			"PRODUCT=" + productDir + ": actions for COMMAND TYPE=" + meta.name ,
            attrs.toArray( new MBeanAttributeInfo[0] ) ,
            null , 
            opers.toArray( new MBeanOperationInfo[0] ) ,
            notifyInfo );
	}

	private MBeanOperationInfo addOperation( ActionBase action , CommandMethod method ) throws Exception {
		List<MBeanParameterInfo> params = new LinkedList<MBeanParameterInfo>();
		
		MBeanParameterInfo args = new MBeanParameterInfo( "args" , "String" , "Command arguments" );
		params.add( args );
		
		// parameters
		for( String varName : method.vars ) {
			CommandVar var = options.getVar( varName );
			MBeanParameterInfo param = addParameter( action , var );
			params.add( param );
		}
		
		// operation
		int type = 0;
		if( method.type == ACTION_TYPE.INFO )
			type = MBeanOperationInfo.INFO;
		else 
		if( method.type == ACTION_TYPE.NORMAL || 
			method.type == ACTION_TYPE.INTERACTIVE || 
			method.type == ACTION_TYPE.CRITICAL )
			type = MBeanOperationInfo.ACTION;
		else 
		if( method.type == ACTION_TYPE.STATUS )
			type = MBeanOperationInfo.ACTION_INFO;
		else
			action.exitUnexpectedState();
		
		MBeanOperationInfo op = new MBeanOperationInfo( method.name ,
			method.help + "\\n\\n Syntax:\\n" + method.syntax ,
			params.toArray( new MBeanParameterInfo[0] ) , 
			"void" , 
			type );
		
		return( op );
	}

	private MBeanParameterInfo addParameter( ActionBase action , CommandVar var ) throws Exception {
		String type = getType( var );
		MBeanParameterInfo param = new MBeanParameterInfo(
			var.varName ,
			type ,
			var.help );
		
		return( param );
	}
	
	@Override
	public synchronized MBeanInfo getMBeanInfo() {
		return( mbean );
	}

	@Override
	public MBeanNotificationInfo[] getNotificationInfo() {
		return( notifyInfo );
	}

	@Override
	public void addNotificationListener( NotificationListener listener , NotificationFilter filter , Object handback ) {
		broadcaster.addNotificationListener( listener , filter , handback );  
	}
		                  
	@Override
	public void removeNotificationListener( NotificationListener listener ) throws ListenerNotFoundException {
		broadcaster.removeNotificationListener( listener );     
	}
	
	private String getType( CommandVar var ) {
		String type = "";
		
		if( var.isFlag )
			type = "Integer";
		else
		if( var.isInteger )
			type = "Integer";
		else
			type = "String";
		return( type );
	}
	
	public MBeanAttributeInfo addGenericOption( ActionBase action , CommandVar var ) throws Exception {
		String type = getType( var );
		
		MBeanAttributeInfo attr = new MBeanAttributeInfo( var.varName ,
			type ,
			var.help ,
			true ,
			true ,
			false );
		
		return( attr );
	}
	
	@Override
	public synchronized Object getAttribute( String name ) throws AttributeNotFoundException {
		if( name.equals( "args" ) )
			return( options.data.getArgsSet() );

		CommandVar var = options.getVar( name );
		if( var.isParam && var.isString )
			return( options.data.getParamValue( var.varName ) );

		if( var.isParam && var.isInteger ) {
			int value = options.data.getIntParamValue( var.varName , -1 );
			if( value < 0 )
				return( null );
			
			return( new Integer( value ) );
		}
		
		if( var.isFlag ) {
			FLAG flag = options.data.getFlagValue( name );
			if( flag == null )
				return( null );
			
			return( new Integer( ( flag == FLAG.YES )? 1 : 0 ) );
		}
		
		action.error( "unknown attr=" + name );
		return( null );
	}

	@Override
	public synchronized void setAttribute( Attribute attribute) throws InvalidAttributeValueException, MBeanException, AttributeNotFoundException {
		String name = attribute.getName();
		Object value = attribute.getValue();
		setOption( options , name , value );
	}
	
	public void setOption( CommandOptions setopts , String name , Object value ) {
		if( name.equals( "args" ) ) {
			setopts.setArgs( Common.splitSpaced( ( String )value ) );
			return;
		}
		
		CommandVar var = setopts.getVar( name );
		if( var.isParam && var.isString ) {
			setopts.setParam( var , ( String )value );
			return;
		}
		
		if( var.isParam && var.isInteger ) {
			Integer intvalue = ( Integer )value;
			if( intvalue == null )
				setopts.clearParam( var );
			else
				setopts.setParam( var , "" + intvalue.intValue() );
			return;
		}
		
		if( var.isFlag ) {
			Integer intvalue = ( Integer )value;
			if( intvalue == null )
				setopts.clearFlag( var );
			else
				setopts.setFlag( var , ( intvalue.intValue() == 1 )? true : false );
			return;
		}
		
		action.error( "unexpected var=" + var.varName );
	}

	@Override
	public synchronized AttributeList getAttributes(String[] names) {
		AttributeList list = new AttributeList();
		for( String name : names ) {
			try {
				Object value = getAttribute( name );
				Attribute attr = new Attribute( name , value );
				list.add( attr );
			}
			catch( Throwable e ) {
				action.error( "unexpected var=" + name );
			}
		}
		
        return( list );
	}

	@Override
	public synchronized AttributeList setAttributes(AttributeList list) {
		options.clearData();
		
    	Attribute[] attrs = (Attribute[]) list.toArray( new Attribute[0] );
    	AttributeList retlist = new AttributeList();
        
    	for (Attribute attr : attrs) {
    		try {
    			setAttribute( attr );
    		}
    		catch( Throwable e ) {
    			action.log( e );
    		}
    		retlist.add( attr );
    	}
        
    	return retlist;
	}

	public void notifyLog( int sessionId , Throwable e ) {
		String msg = "exception: " + e.getClass().getName();
		String em = e.getMessage();
		if( em != null && !em.isEmpty() )
			msg += ", " + em;
		
		notifyLog( sessionId , msg );
	}
	
	public void notifyLog( int sessionId , String msg ) {
		try {
			ServerCommandCall call = controller.server.getCall( sessionId );
			if( call == null )
				return;
			
			ActionNotification n = new ActionNotification( this , ++notificationSequence , sessionId , call.clientId , msg );
			n.setLogEvent();
			broadcaster.sendNotification( n );
		}
		catch( Throwable e ) {
		}
	}
	
	@Override
	public Object invoke( String name , Object[] args , String[] sig ) throws MBeanException, ReflectionException {
		String value = null;
		try {
			value = notifyExecute( name , args );
		}
		catch( Throwable e ) {
			action.error( e.getMessage() );
		}

		return( value );
	}

	public synchronized int getNextSequence() {
		return( ++notificationSequence );
	}
	
	public void sendNotification( ActionNotification n ) throws Exception {
		broadcaster.sendNotification( n );
	}
	
	private String notifyExecute( String name , Object[] args ) throws Exception {
		if( name.equals( RemoteCall.GENERIC_ACTION_NAME ) ) {
			int sessionId = notifyExecuteGeneric( args );
			return( "" + sessionId );
		}
		
		if( name.equals( RemoteCall.INPUT_ACTION_NAME ) ) {
			notifyExecuteInput( args );
			return( "OK" );
		}
		
		if( name.equals( RemoteCall.STOP_ACTION_NAME ) ) {
			notifyExecuteStop( args );
			return( "OK" );
		}
		
		if( name.equals( RemoteCall.WAITCONNECT_ACTION_NAME ) ) {
			return( notifyExecuteWaitConnect( args ) );
		}
		
		int sessionId = notifyExecuteSpecific( name , args );
		if( sessionId < 0 )
			return( null );
		
		return( "" + sessionId );
	}
	
	private int notifyExecuteSpecific( String name , Object[] args ) throws Exception {
		action.debug( "operation invoked, name=" + name );
		
		// find action
		CommandMethod method = meta.getAction( name );
		if( args.length != ( 1 + method.vars.length ) )
			return( -1 );
		
		CommandOptions cmdopts = new CommandOptions( options.meta );
		ActionData data = new ActionData( engine.execrc );
		data.set( options.data );
		cmdopts.setAction( meta.name , method , data );
		
		data.setArgs( Common.splitSpaced( ( String )args[0] ) ); 
		for( int k = 0; k < method.vars.length; k++ ) {
			String varName = method.vars[ k ];
			setOption( cmdopts , varName , args[ k + 1 ] );
		}
		
		int sessionId = server.createSessionId();
		if( !server.runWebJmx( sessionId , productDir , meta , cmdopts ) )
			return( -1 );
		
		return( 0 );
	}

	private int notifyExecuteGeneric( Object[] args ) throws Exception {
		if( args.length != 3 ) {
			action.error( "missing args calling command=" + meta.name );
			return( -1 );
		}
		
		if( args[1].getClass() != ActionData.class || 
			args[0].getClass() != String.class ||
			args[2].getClass() != String.class ) {
			action.error( "invalid args calling command=" + meta.name );
			return( -1 );
		}
		
		String actionName = ( String )args[0];
		ActionData data = ( ActionData )args[1];
		String clientId = ( String )args[2];
		
		int sessionId = server.createSessionId();
		action.debug( "operation invoked, sessionId=" + sessionId );
		
		ServerCommandCall thread = new ServerCommandCall( sessionId , clientId , this , actionName , data );
		if( !thread.start() )
			return( -1 );
		
		return( sessionId );
	}
	
	private int notifyExecuteInput( Object[] args ) throws Exception {
		if( args.length != 2 ) {
			action.error( "missing args calling command=" + meta.name );
			return( -1 );
		}
		
		if( args[0].getClass() != String.class ||
			args[1].getClass() != String.class ) {
			action.error( "invalid args calling input for command=" + meta.name );
			return( -1 );
		}
		
		String sessionId = ( String )args[0];
		String input = ( String )args[1];
		
		try {
			server.addCallInput( sessionId , input );
		}
		catch( Throwable e ) {
			engine.serverAction.log( e );
			return( 1 );
		}
		
		return( 0 );
	}
	
	private void notifyExecuteStop( Object[] args ) throws Exception {
		if( args.length != 1 ) {
			action.error( "missing args calling command=" + meta.name );
			return;
		}
		
		if( args[0].getClass() != String.class ) {
			action.error( "invalid args calling input for command=" + meta.name );
			return;
		}
		
		String sessionId = ( String )args[0];
		
		try {
			server.stopSession( sessionId );
		}
		catch( Throwable e ) {
			engine.serverAction.log( e );
		}
	}
	
	private String notifyExecuteWaitConnect( Object[] args ) throws Exception {
		if( args.length != 1 ) {
			action.error( "missing args calling command=" + meta.name );
			return( RemoteCall.STATUS_ACTION_FAILED );
		}
		
		if( args[0].getClass() != String.class ) {
			action.error( "invalid args calling input for command=" + meta.name );
			return( RemoteCall.STATUS_ACTION_FAILED );
		}
		
		String sessionId = ( String )args[0];
		
		try {
			if( !server.waitConnect( sessionId ) )
				return( RemoteCall.STATUS_ACTION_FAILED );
		}
		catch( Throwable e ) {
			engine.serverAction.log( e );
			return( RemoteCall.STATUS_ACTION_FAILED );
		}
		
		return( RemoteCall.STATUS_ACTION_CONNECTED );
	}

}
