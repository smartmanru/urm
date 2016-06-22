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
	int invokeSequence = 0;
	
	public ActionBase action;
	public Controller controller;
	public ServerEngine engine;
	public String productDir;
	
	public CommandMeta meta;
	public MBeanInfo mbean;
	public CommandOptions options;
	
	public ServerCommandMBean( ActionBase action , Controller controller , ServerEngine engine , String productDir , CommandMeta meta ) {
		this.action = action;
		this.controller = controller;
		this.engine = engine;
		this.productDir = productDir;
		this.meta = meta;
	}

	public void createInfo() throws Exception {
		options = new CommandOptions( meta );
		
		// attributes
		List<MBeanAttributeInfo> attrs = new LinkedList<MBeanAttributeInfo>();
		for( CommandVar var : options.varByName.values() ) {
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
		MBeanNotificationInfo mbnLog = new MBeanNotificationInfo( new String[] { ActionLogNotification.EVENT } , 
				ActionLogNotification.class.getName() , "output of action executed" );
		MBeanNotificationInfo mbnStop = new MBeanNotificationInfo( new String[] { ActionStopNotification.EVENT } , 
				ActionStopNotification.class.getName() , "stop of action" );
		
		// register
		Collections.reverse( opers );
		mbean = new MBeanInfo(
			this.getClass().getName() ,
			"PRODUCT=" + productDir + ": actions for COMMAND TYPE=" + meta.name ,
            attrs.toArray( new MBeanAttributeInfo[0] ) ,
            null , 
            opers.toArray( new MBeanOperationInfo[0] ) ,
            new MBeanNotificationInfo[] { mbnLog , mbnStop } );
	}

	public MBeanOperationInfo addOperation( ActionBase action , CommandMethod method ) throws Exception {
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

	public MBeanParameterInfo addParameter( ActionBase action , CommandVar var ) throws Exception {
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
	
	public synchronized String getAttribute( String name ) throws AttributeNotFoundException {
		return( null );
	}

	public synchronized void setAttribute( Attribute attribute) throws InvalidAttributeValueException, MBeanException, AttributeNotFoundException {
	}

	public synchronized AttributeList getAttributes(String[] names) {
		AttributeList list = new AttributeList();
        return list;
	}

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
    
	public Object invoke( String name , Object[] args , String[] sig ) throws MBeanException, ReflectionException {
		String sessionId = null;
		try {
			sessionId = notifyExecute( name , args );
		}
		catch( Throwable e ) {
			action.error( e.getMessage() );
		}

		return( sessionId );
	}

	private synchronized String createSessionId( String name ) {
		invokeSequence++;
		return( name + "-" + invokeSequence );
	}
	
	public void notifyLog( String sessionId , String msg ) {
		try {
			ActionLogNotification n = new ActionLogNotification( this , ++notificationSequence , sessionId + ": " ); 
			sendNotification( n );
		}
		catch( Throwable e ) {
		}
	}
	
	public void notifyStop( String sessionId ) {
		try {
			ActionStopNotification n = new ActionStopNotification( this , ++notificationSequence , sessionId ); 
			sendNotification( n );
		}
		catch( Throwable e ) {
		}
	}
	
	private String notifyExecute( String name , Object[] args ) throws Exception {
		if( name.equals( "execute" ) ) {
			if( args.length < 1 ) {
				action.error( "missing args calling command=" + meta.name + ", action=" + name );
				return( null );
			}
			
			if( args[0].getClass() != ActionData.class ) {
				action.error( "invalid args calling command=" + meta.name + ", action=" + name + ", class=" + args[0].getClass().getName() );
				return( null );
			}
			
			String sessionId = createSessionId( name );
			action.debug( "operation invoked, sessionId=" + sessionId );
			
			ServerCommandThread thread = new ServerCommandThread( sessionId , this , ( ActionData )args[0] );
			thread.start();
			return( sessionId );
		}
		
		return( null );
	}
	
	public synchronized MBeanInfo getMBeanInfo() {
		return( mbean );
	}
	
}
