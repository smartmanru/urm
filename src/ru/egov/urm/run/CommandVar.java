package ru.egov.urm.run;

import ru.egov.urm.run.CommandOptions.FLAG;

public class CommandVar {

	public String optName;
	public String help;
	public String varName;
	public FLAG varValue;
	public String varEnumValue;
	
	public boolean isParam = false;
	public boolean isFlag = false;
	public boolean isEnum = false;
	public boolean isGeneric = false;
	
	public static CommandVar newParam( String opt , String varName , String help ) {
		CommandVar var = new CommandVar();
		var.optName = opt;
		var.help = help;
		var.varName = varName;
		var.varValue = FLAG.DEFAULT;
		var.isParam = true;
		return( var );
	}

	public static CommandVar newFlagYesOption( String opt , String varName , String help ) {
		CommandVar var = new CommandVar();
		var.optName = opt;
		var.help = help;
		var.varName = varName;
		var.varValue = FLAG.YES;
		var.isFlag = true;
		return( var );
	}
	
	public static CommandVar newFlagNoOption( String opt , String varName , String help ) {
		CommandVar var = new CommandVar();
		var.optName = opt;
		var.help = help;
		var.varName = varName;
		var.varValue = FLAG.NO;
		var.isFlag = true;
		return( var );
	}

	public static CommandVar newFlagEnumOption( String opt , String enumValue , String varName , String help ) {
		CommandVar var = new CommandVar();
		var.optName = opt;
		var.help = help;
		var.varName = varName;
		var.varEnumValue = enumValue;
		var.isEnum = true;
		return( var );
	}
	
	public void setGeneric() {
		isGeneric = true;
	}

}
