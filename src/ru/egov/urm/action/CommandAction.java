package ru.egov.urm.run;

import ru.egov.urm.Common;

abstract public class CommandAction {

	public String name;
	public String help;
	public String[] vars;
	public String syntax;

	abstract public void run( ActionInit action ) throws Exception;
	
	public static CommandAction newAction( CommandAction method , String name , String help , String varList , String syntax ) {
		method.name = name;
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
