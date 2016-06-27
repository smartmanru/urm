package org.urm.common.jmx;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ReflectionException;

import org.urm.common.Common;
import org.urm.common.action.ActionData;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandMethod;
import org.urm.common.action.CommandMethod.ACTION_TYPE;
import org.urm.common.action.CommandOptions;
import org.urm.common.action.CommandVar;
import org.urm.server.ServerEngine;
import org.urm.server.action.ActionBase;

public class ServerCommandMBean extends NotificationBroadcasterSupport implements DynamicMBean {

	int notificationSequence = 0;
	MBeanNotificationInfo[] notifyInfo;
	
	public ActionBase action;
	public ServerMBean controller;
	public ServerEngine engine;
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
	}

	public void createInfo() throws Exception {
		options = new CommandOptions();
		
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
		else if( method.type == ACTION_TYPE.NORMAL || method.type == ACTION_TYPE.CRITICAL )
			type = MBeanOperationInfo.ACTION;
		else if( method.type == ACTION_TYPE.STATUS )
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
    
	public void notifyLog( int sessionId , String msg ) {
		try {
			ServerCommandCall call = controller.getCall( sessionId );
			if( call == null )
				return;
			
			ActionNotification n = new ActionNotification( this , ++notificationSequence , sessionId , call.clientId , msg ); 
			sendNotification( n );
		}
		catch( Throwable e ) {
		}
	}
	
	public void notifyStop( int sessionId ) {
		try {
			ServerCommandCall call = controller.getCall( sessionId );
			if( call == null )
				return;
			
			ActionNotification n = new ActionNotification( this , ++notificationSequence , sessionId , call.clientId , "stop" ); 
			sendNotification( n );
		}
		catch( Throwable e ) {
		}
	}
	
	@Override
	public Object invoke( String name , Object[] args , String[] sig ) throws MBeanException, ReflectionException {
		int sessionId = -1;
		try {
			sessionId = notifyExecute( name , args );
		}
		catch( Throwable e ) {
			action.error( e.getMessage() );
		}

		String value = "" + sessionId;
		return( value );
	}

	private int notifyExecute( String name , Object[] args ) throws Exception {
		if( name.equals( "execute" ) ) {
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
			
			int sessionId = engine.createSessionId();
			action.debug( "operation invoked, sessionId=" + sessionId );
			
			ServerCommandCall thread = new ServerCommandCall( sessionId , clientId , this , actionName , data );
			thread.start();
			return( sessionId );
		}
		
		return( -1 );
	}

	@Override
	public synchronized MBeanInfo getMBeanInfo() {
		return( mbean );
	}

}
