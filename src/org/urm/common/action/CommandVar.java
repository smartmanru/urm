package org.urm.common.action;

public class CommandVar {

	public enum FLAG { DEFAULT , YES , NO }; 
	
	public String optName;
	public String help;
	public String varName;
	public FLAG varValue;
	public String varEnumValue;
	public boolean jmx;
	
	public boolean isParam = false;
	public boolean isFlag = false;
	public boolean isEnum = false;
	public boolean isGeneric = false;
	public boolean isInteger = false;
	
	public static CommandVar newParam( String opt , String varName , boolean jmx , String help ) {
		CommandVar var = new CommandVar();
		var.optName = opt;
		var.help = help;
		var.varName = varName;
		var.varValue = FLAG.DEFAULT;
		var.isParam = true;
		var.jmx = jmx;
		
		return( var );
	}

	public static CommandVar newIntParam( String opt , String varName , boolean jmx , String help ) {
		CommandVar var = new CommandVar();
		var.optName = opt;
		var.help = help;
		var.varName = varName;
		var.varValue = FLAG.DEFAULT;
		var.isParam = true;
		var.jmx = jmx;
		var.isInteger = true;
		
		return( var );
	}

	public static CommandVar newFlagYesOption( String opt , String varName , boolean jmx , String help ) {
		CommandVar var = new CommandVar();
		var.optName = opt;
		var.help = help;
		var.varName = varName;
		var.varValue = FLAG.YES;
		var.isFlag = true;
		var.jmx = jmx;

		return( var );
	}
	
	public static CommandVar newFlagNoOption( String opt , String varName , boolean jmx , String help ) {
		CommandVar var = new CommandVar();
		var.optName = opt;
		var.help = help;
		var.varName = varName;
		var.varValue = FLAG.NO;
		var.isFlag = true;
		var.jmx = jmx;

		return( var );
	}

	public static CommandVar newFlagEnumOption( String opt , String enumValue , String varName , boolean jmx , String help ) {
		CommandVar var = new CommandVar();
		var.optName = opt;
		var.help = help;
		var.varName = varName;
		var.varEnumValue = enumValue;
		var.isEnum = true;
		var.jmx = jmx;
		
		return( var );
	}
	
	public void setGeneric() {
		isGeneric = true;
	}

}
