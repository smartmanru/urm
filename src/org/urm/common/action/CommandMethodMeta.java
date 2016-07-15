package org.urm.common.action;

import org.urm.common.Common;

public class CommandMethodMeta {

	public enum ACTION_TYPE {
		INFO ,
		NORMAL ,
		CRITICAL ,
		STATUS ,
		INTERACTIVE
	};

	public CommandMeta command;
	public String name;
	public ACTION_TYPE type;
	
	public boolean top;
	public String help;
	public String[] vars;
	public String syntax;

	public static CommandMethodMeta newInfo( CommandMeta command , String name , boolean top , String help , String varList , String syntax ) {
		return( newAction( command , name , ACTION_TYPE.INFO , top , help , varList , syntax ) );
	}
	
	public static CommandMethodMeta newNormal( CommandMeta command , String name , boolean top , String help , String varList , String syntax ) {
		return( newAction( command , name , ACTION_TYPE.NORMAL , top , help , varList , syntax ) );
	}
	
	public static CommandMethodMeta newCritical( CommandMeta command , String name , boolean top , String help , String varList , String syntax ) {
		return( newAction( command , name , ACTION_TYPE.CRITICAL , top , help , varList , syntax ) );
	}
	
	public static CommandMethodMeta newStatus( CommandMeta command , String name , boolean top , String help , String varList , String syntax ) {
		return( newAction( command , name , ACTION_TYPE.STATUS , top , help , varList , syntax ) );
	}
	
	public static CommandMethodMeta newInteractive( CommandMeta command , String name , boolean top , String help , String varList , String syntax ) {
		return( newAction( command , name , ACTION_TYPE.INTERACTIVE , top , help , varList , syntax ) );
	}
	
	private static CommandMethodMeta newAction( CommandMeta command , String name , ACTION_TYPE type , boolean top , String help , String varList , String syntax ) {
		CommandMethodMeta method = new CommandMethodMeta( command );
		method.name = name;
		method.type = type;
		method.top = top;
		method.help = help;
		method.vars = Common.split( varList , "," );
		method.syntax = syntax;
		
		return( method );
	}
	
	private CommandMethodMeta( CommandMeta command ) {
		this.command = command;
	}
	
	public boolean isOptionApplicable( CommandVar var ) {
		if( var.isGeneric )
			return( true );
		
		for( String option : vars )
			if( option.equals( var.varName ) )
				return( true );
		return( false );
	}
	
	public boolean isInteractive() {
		return( type == ACTION_TYPE.INTERACTIVE );
	}
	
}
