package org.urm.common.action;

import java.io.Serializable;

public class CommandOption implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2840654112004844114L;

	public enum FLAG { DEFAULT , YES , NO }; 
	
	public String optName;
	public String help;
	public boolean jmx;
	
	public CommandVar var;
	public FLAG varFlagValue;
	public String varEnumValue;
	
	public static CommandOption newParam( OptionsMeta meta , String optName , String varName , boolean jmx , String help ) {
		CommandOption opt = new CommandOption();
		opt.optName = optName;
		opt.var = meta.getParamVar( varName );
		opt.varFlagValue = FLAG.DEFAULT;
		opt.jmx = jmx;
		opt.help = help;
		
		return( opt );
	}

	public static CommandOption newIntParam( OptionsMeta meta , String optName , String varName , boolean jmx , String help ) {
		CommandOption opt = new CommandOption();
		opt.optName = optName;
		opt.var = meta.getIntParamVar( varName );
		opt.varFlagValue = FLAG.DEFAULT;
		opt.jmx = jmx;
		opt.help = help;
		
		return( opt );
	}

	public static CommandOption newFlagYesOption( OptionsMeta meta , String optName , String varName , boolean jmx , String help ) {
		CommandOption opt = new CommandOption();
		opt.optName = optName;
		opt.var = meta.getFlagVar( varName );
		opt.varFlagValue = FLAG.YES;
		opt.jmx = jmx;
		opt.help = help;

		return( opt );
	}
	
	public static CommandOption newFlagNoOption( OptionsMeta meta , String optName , String varName , boolean jmx , String help ) {
		CommandOption opt = new CommandOption();
		opt.optName = optName;
		opt.var = meta.getFlagVar( varName );
		opt.varFlagValue = FLAG.NO;
		opt.jmx = jmx;
		opt.help = help;

		return( opt );
	}

	public static CommandOption newFlagEnumOption( OptionsMeta meta , String optName , String enumValue , String varName , boolean jmx , String help ) {
		CommandOption opt = new CommandOption();
		opt.optName = optName;
		opt.var = meta.getEnumVar( varName );
		opt.varEnumValue = enumValue;
		opt.jmx = jmx;
		opt.help = help;
		
		return( opt );
	}
	
}
