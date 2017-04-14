package org.urm.common.action;

import java.io.Serializable;

public class CommandVar implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 474594974763700358L;
	
	public String varName;
	public String userName;
	public String help;
	
	public boolean isParam = false;
	public boolean isString = false;
	public boolean isFlag = false;
	public boolean isEnum = false;
	public boolean isGeneric = false;
	public boolean isInteger = false;
	public boolean jmx = false;
	
	public static CommandVar newVar( String varName , boolean jmx , String userName , String help ) {
		CommandVar var = new CommandVar();
		var.help = help;
		var.varName = varName;
		var.userName = userName;
		var.isParam = true;
		var.isString = true;
		var.jmx = jmx;
		
		return( var );
	}

	public static CommandVar newIntVar( String varName , boolean jmx , String userName , String help ) {
		CommandVar var = new CommandVar();
		var.help = help;
		var.varName = varName;
		var.userName = userName;
		var.isParam = true;
		var.isInteger = true;
		var.jmx = jmx;
		
		return( var );
	}

	public static CommandVar newFlagVar( String varName , boolean jmx , String userName , String help ) {
		CommandVar var = new CommandVar();
		var.help = help;
		var.varName = varName;
		var.userName = userName;
		var.isFlag = true;
		var.jmx = jmx;

		return( var );
	}
	
	public static CommandVar newEnumVar( String varName , boolean jmx , String userName , String help ) {
		CommandVar var = new CommandVar();
		var.help = help;
		var.varName = varName;
		var.userName = userName;
		var.isEnum = true;
		var.jmx = jmx;
		
		return( var );
	}
	
	public void setGeneric() {
		isGeneric = true;
	}

}
