package org.urm.server.action.main;

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
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;

import org.urm.common.Common;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandMethod;
import org.urm.common.action.CommandMethod.ACTION_TYPE;
import org.urm.common.action.CommandOptions;
import org.urm.common.action.CommandVar;
import org.urm.server.ServerEngine;
import org.urm.server.action.ActionBase;

public class MainServerMBean implements DynamicMBean {

	ServerEngine engine;
	String productDir;
	
	CommandMeta meta;
	MBeanInfo mbean;
	CommandOptions options;
	
	public MainServerMBean( ServerEngine engine , String productDir , CommandMeta meta ) {
		this.engine = engine;
		this.productDir = productDir;
		this.meta = meta;
	}

	public void createInfo( ActionBase action ) throws Exception {
		options = new CommandOptions( engine.execrc );
		
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
		
		// register
		Collections.reverse( opers );
		mbean = new MBeanInfo(
			this.getClass().getName() ,
			"PRODUCT=" + productDir + ": actions for COMMAND TYPE=" + meta.name ,
            attrs.toArray( new MBeanAttributeInfo[0] ) ,
            null ,  // constructors
            opers.toArray( new MBeanOperationInfo[0] ) ,
            null ); // notifications
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
    
	public Object invoke(String name, Object[] args, String[] sig)
    	    throws MBeanException, ReflectionException {
		return( null );
    }

	public synchronized MBeanInfo getMBeanInfo() {
		return( mbean );
	}
	
}
