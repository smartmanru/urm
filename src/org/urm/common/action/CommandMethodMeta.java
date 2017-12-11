package org.urm.common.action;

import org.urm.common.Common;
import org.urm.common.RunContext;

public class CommandMethodMeta {

	public enum SecurityAction {
		ACTION_SECURED ,
		ACTION_CONFIGURE ,
		ACTION_CODEBASE ,
		ACTION_RELEASE ,
		ACTION_DEPLOY ,
		ACTION_MONITOR ,
		ACTION_XDOC ,
		ACTION_ADMIN ,
		ACTION_EXECUTE
	};
	
	public enum ACTION_TYPE {
		INFO ,
		NORMAL ,
		CRITICAL ,
		STATUS ,
		INTERACTIVE
	};

	public enum ACTION_ACCESS {
		SERVER ,
		PRODUCT ,
		ENV
	};
	
	public CommandMeta command;
	public String name;
	public ACTION_TYPE type;
	public ACTION_ACCESS access;
	public boolean accessReadOnly;
	public SecurityAction security;
	
	public boolean top;
	public String help;
	public String[] vars;
	private String syntax;

	public static CommandMethodMeta newInfo( CommandMeta command , String name , ACTION_ACCESS access , boolean accessReadOnly , SecurityAction security , boolean top , String help , String varList , String syntax ) {
		return( newAction( command , name , ACTION_TYPE.INFO , access , accessReadOnly , security , top , help , varList , syntax ) );
	}
	
	public static CommandMethodMeta newNormal( CommandMeta command , String name , ACTION_ACCESS access , boolean accessReadOnly , SecurityAction security , boolean top , String help , String varList , String syntax ) {
		return( newAction( command , name , ACTION_TYPE.NORMAL , access , accessReadOnly , security , top , help , varList , syntax ) );
	}
	
	public static CommandMethodMeta newCritical( CommandMeta command , String name , ACTION_ACCESS access , boolean accessReadOnly , SecurityAction security , boolean top , String help , String varList , String syntax ) {
		return( newAction( command , name , ACTION_TYPE.CRITICAL , access , accessReadOnly , security , top , help , varList , syntax ) );
	}
	
	public static CommandMethodMeta newStatus( CommandMeta command , String name , ACTION_ACCESS access , boolean accessReadOnly , SecurityAction security , boolean top , String help , String varList , String syntax ) {
		return( newAction( command , name , ACTION_TYPE.STATUS , access , accessReadOnly , security , top , help , varList , syntax ) );
	}
	
	public static CommandMethodMeta newInteractive( CommandMeta command , String name , ACTION_ACCESS access , boolean accessReadOnly , SecurityAction security , boolean top , String help , String varList , String syntax ) {
		return( newAction( command , name , ACTION_TYPE.INTERACTIVE , access , accessReadOnly , security , top , help , varList , syntax ) );
	}
	
	private static CommandMethodMeta newAction( CommandMeta command , String name , ACTION_TYPE type , ACTION_ACCESS access , boolean accessReadOnly , SecurityAction security , boolean top , String help , String varList , String syntax ) {
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

	public boolean isVarApplicable( CommandVar var ) {
		if( var.isGeneric )
			return( true );
		
		for( String varName : vars )
			if( varName.equals( var.varName ) )
				return( true );
		return( false );
	}
	
	public boolean isOptionApplicable( CommandOption opt ) {
		return( isVarApplicable( opt.var ) );
	}
	
	public boolean isInteractive() {
		return( type == ACTION_TYPE.INTERACTIVE );
	}

	public String getSyntax( RunContext rc ) {
		if( rc.isLinux() )
			return( "./" + name + ".sh [OPTIONS] " + syntax );
		return( name + ".cmd [OPTIONS] " + syntax );
	}
	
}
