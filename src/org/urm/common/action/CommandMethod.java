package org.urm.common.action;

import org.urm.common.Common;

public class CommandMethod {

	public enum ACTION_TYPE {
		INFO ,
		NORMAL ,
		CRITICAL ,
		STATUS
	};
	
	public String name;
	public ACTION_TYPE type;
	
	public boolean top;
	public String help;
	public String[] vars;
	public String syntax;

	public static CommandMethod newInfo( String name , boolean top , String help , String varList , String syntax ) {
		return( newAction( name , ACTION_TYPE.INFO , top , help , varList , syntax ) );
	}
	
	public static CommandMethod newNormal( String name , boolean top , String help , String varList , String syntax ) {
		return( newAction( name , ACTION_TYPE.NORMAL , top , help , varList , syntax ) );
	}
	
	public static CommandMethod newCritical( String name , boolean top , String help , String varList , String syntax ) {
		return( newAction( name , ACTION_TYPE.CRITICAL , top , help , varList , syntax ) );
	}
	
	public static CommandMethod newStatus( String name , boolean top , String help , String varList , String syntax ) {
		return( newAction( name , ACTION_TYPE.STATUS , top , help , varList , syntax ) );
	}
	
	private static CommandMethod newAction( String name , ACTION_TYPE type , boolean top , String help , String varList , String syntax ) {
		CommandMethod method = new CommandMethod();
		method.name = name;
		method.type = type;
		method.top = top;
		method.help = help;
		method.vars = Common.split( varList , "," );
		method.syntax = syntax;
		
		return( method );
	}
	
	public boolean isOptionApplicable( CommandVar var ) {
		if( var.isGeneric )
			return( true );
		
		for( String option : vars )
			if( option.equals( var.varName ) )
				return( true );
		return( false );
	}
	
}
