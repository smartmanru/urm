package org.urm.common.action;

import org.urm.common.Common;

public class CommandMethod {

	public String name;
	public boolean top;
	public String help;
	public String[] vars;
	public String syntax;

	public static CommandMethod newAction( String name , boolean top , String help , String varList , String syntax ) {
		CommandMethod method = new CommandMethod();
		method.name = name;
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
