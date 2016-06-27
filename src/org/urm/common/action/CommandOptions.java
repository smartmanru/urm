package org.urm.common.action;

import java.util.List;
import java.util.Map;

import org.urm.common.RunContext;
import org.urm.common.action.CommandVar.FLAG;

public class CommandOptions {

	public enum SQLMODE { UNKNOWN , APPLY , ANYWAY , CORRECT , ROLLBACK , PRINT };
	public enum SQLTYPE { UNKNOWN , SQL , CTL , PUB };

	public int optDefaultCommandTimeout = 10;
	
	// implementation
	public OptionsMeta meta;
	public String command;
	public String action;
	public ActionData data;

	public CommandOptions() {
		meta = new OptionsMeta();
	}

	public CommandOptions( OptionsMeta meta ) {
		this.meta = meta;
	}

	public void setCommand( String command , ActionData data ) {
		this.command = command;
		this.data = data;
	}
	
	public void setAction( String command , CommandMethod method , ActionData data ) {
		this.command = command;
		this.action = method.name;
		this.data = data;
	}
	
	public boolean parseArgs( RunContext clientrc , String[] cmdParams ) {
		if( cmdParams.length < 2 ) {
			command = "help";
			return( false );
		}
		
		// first item is command
		command = cmdParams[0];
		
		int k = 1;
		action = cmdParams[1];
		k++;

		// next items are options
		data = new ActionData( clientrc );
		for( ; k < cmdParams.length; k++ ) {
			String arg = cmdParams[k];
			if( !arg.startsWith( "-" ) )
				break;
			
			arg = arg.substring( 1 );
			if( isFlagOption( arg ) ) {
				if( !addFlagOption( arg ) )
					return( false );
			}
			else if( isEnumOption( arg ) ) {
				if( !addEnumOption( arg ) )
					return( false );
			}
			else if( isParamOption( arg ) ) {
				k++;
				if( k >= cmdParams.length ) {
					print( "invalid arguments - parameter=" + arg + " has no value defined" );
					return( false );
				}
				
				String value = cmdParams[k];
				if( !addParamOption( arg , value ) )
					return( false );
			}
			else {
				print( "invalid arguments - unknown option=" + arg );
				return( false );
			}
		}

		// next items are args
		for( ; k < cmdParams.length; k++ ) {
			String arg = cmdParams[k];
			if( !addArg( arg ) )
				return( false );
		}

		// check scripts
		return( true );
	}

	public CommandVar getVar( String varName ) {
		return( meta.getVar( varName ) );
	}
	
	public String getRunningOptions() {
		return( data.getRunningOptions() );
	}
	
	public String getOptionValue( CommandVar var ) {
		return( data.getOptionValue( var ) );
	}
	
	public boolean isValidVar( String var ) {
		return( meta.isValidVar( var ) );
	}
	
	public boolean isFlagOption( String opt ) {
		return( meta.isFlagOption( opt ) );
	}
	
	public boolean isFlagVar( String var ) {
		return( meta.isFlagVar( var ) );
	}
	
	public boolean isEnumOption( String opt ) {
		return( meta.isEnumOption( opt ) );
	}
	
	public boolean isEnumVar( String var ) {
		return( meta.isEnumVar( var ) );
	}
	
	public boolean isParamOption( String opt ) {
		return( meta.isParamOption( opt ) );
	}
	
	public boolean isParamVar( String var ) {
		return( meta.isParamVar( var ) );
	}
	
	public boolean addFlagOption( String opt ) {
		if( !isFlagOption( opt ) )
			throw new RuntimeException( "option=" + opt + " is not a flag" );
		
		CommandVar info = meta.getOption( opt );
		if( !data.addFlagOption( info ) ) {
			print( "flag=" + info.varName + " is already set" );
			return( false );
		}
		
		return( true );
	}

	public boolean addEnumOption( String opt ) {
		if( !isEnumOption( opt ) )
			throw new RuntimeException( "option=" + opt + " is not a enum" );
		
		CommandVar info = meta.getOption( opt );
		if( !data.addEnumOption( info ) ) {
			print( "enum=" + info.varName + " is already set" );
			return( false );
		}
		
		return( true );
	}

	public boolean addParamOption( String opt , String value ) {
		if( !isParamOption( opt ) )
			throw new RuntimeException( "option=" + opt + " is not a parameter" );
		
		CommandVar info = meta.getOption( opt );
		if( !data.addParamOption( info , value ) ) {
			print( "parameter=" + info.varName + " is already set" );
			return( false );
		}
		
		return( true );
	}

	public boolean addArg( String value ) {
		return( data.addArg( value ) );
	}
	
	public boolean getFlagValue( String var , boolean defValue ) {
		return( data.getFlagValue( var , defValue ) );
	}
	
	public String getEnumValue( String var ) {
		return( data.getEnumValue( var ) );
	}

	public String getParamValue( String var ) {
		return( data.getParamValue( var ) );
	}
	
	public int getIntParamValue( String var , int defaultValue ) {
		return( data.getIntParamValue( var , defaultValue ) );
	}
	
	public String getFlagsSet() {
		return( data.getFlagsSet() );
	}
	
	public String getParamsSet() {
		return( data.getParamsSet() );
	}

	public List<CommandVar> getOptionsSet() {
		return( data.getOptionsSet() );
	}
	
	public String getArgsSet() {
		return( data.getArgsSet() );
	}
	
	public String getArg( int pos ) {
		return( data.getArg( pos ) );
	}
	
	public int getArgCount() {
		return( data.getArgCount() );
	}
	
	public int getIntArg( int pos , int defValue ) {
		return( data.getIntArg( pos , defValue ) );
	}
	
	public String[] getArgList( int startFrom ) {
		return( data.getArgList( startFrom ) );
	}
	
	public boolean combineValue( String optVar , FLAG confValue , boolean defValue ) {
		return( data.combineValue( optVar , confValue , defValue ) );
	}
	
	public boolean checkValidOptions( CommandMethod commandAction ) {
		for( CommandVar var : data.getOptionsSet() ) {
			if( !commandAction.isOptionApplicable( var ) ) {
				print( "option " + var.varName + " is not applicable for action " + commandAction.name );
				return( false );
			}
		}

		// check defined options
		for( String varUsed : commandAction.vars ) {
			if( !isValidVar( varUsed ) ) {
				print( "unknown command var=" + varUsed );
				return( false );
			}
		}
		
		return( true );
	}
	
	private void print( String s ) {
		System.out.println( s );
	}

	public void showTopHelp( CommandBuilder builder , CommandMeta main , CommandMeta[] commands ) {
		meta.showTopHelp( builder , main , commands );
	}

	public void showCommandHelp( CommandBuilder builder , CommandMeta commandInfo , boolean main ) {
		meta.showCommandHelp( builder , commandInfo , main );
	}

	public void showActionHelp( CommandBuilder builder , CommandMethod action ) {
		meta.showActionHelp( builder , action );
	}

	public Map<String,CommandVar> getDefinedVariables() {
		return( meta.varByName );
	}

	public void setArgs( String[] args ) {
		data.setArgs( args );
	}

	public void setParam( CommandVar var , String value ) {
		data.setParam( var , value );
	}
	
	public void setFlag( CommandVar var , boolean value ) {
		data.setFlag( var , value );
	}
	
	public void clearFlag( CommandVar var ) {
		data.clearFlag( var );
	}
	
	public void clearParam( CommandVar var ) {
		data.clearParam( var );
	}
	
	public void clearData() {
		data.clear();
	}
	
}
