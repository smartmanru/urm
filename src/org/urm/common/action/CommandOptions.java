package org.urm.common.action;

import java.util.Date;
import java.util.List;

import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.common.action.CommandOption.FLAG;

public class CommandOptions {

	public enum SQLMODE { UNKNOWN , APPLY , ANYWAY , CORRECT , ROLLBACK , PRINT };
	public enum SQLTYPE { UNKNOWN , SQL , CTL , PUB };

	public int optDefaultCommandTimeout = 10;
	
	// implementation
	public OptionsMeta meta;
	public String command;
	public String method;
	
	public ActionData data;

	public CommandOptions( OptionsMeta meta ) {
		this.meta = meta;
	}

	public void setMethod( String command , String method ) {
		this.command = command;
		this.method = method;
	}
	
	public void setCommand( String command , ActionData data ) {
		this.command = command;
		this.data = data;
	}

	public void setAction( CommandMethodMeta method , ActionData data ) {
		this.command = method.command.name;
		this.method = method.name;
		this.data = data;
	}
	
	public boolean setFromSystemProperties() {
		for( CommandVar var : meta.getVars() ) {
			String value = System.getProperty( var.varName );
			if( value != null )
				if( !setVarValue( var , value ) )
					return( false );
		}
		
		return( true );
	}
	
	public boolean parseArgs( RunContext clientrc , String[] cmdParams ) {
		if( cmdParams.length < 2 ) {
			command = "help";
			return( false );
		}
		
		// first item is command
		command = cmdParams[0];
		
		int k = 1;
		method = cmdParams[1];
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

		if( data.getFlagValue( OptionsMeta.OPT_TRACE ) == FLAG.YES ) {
			String ro = data.getRunningInfo();
			print( "current options=" + ro );
		}
		
		return( true );
	}

	private boolean setVarValue( CommandVar var , String value ) {
		if( var.isFlag ) {
			boolean x = ( value.isEmpty() )? true : Common.getBooleanValue( value );
			CommandOption info = meta.getVarFlagOption( var , x );
			return( addFlagOption( info.optName ) );
		}
		if( var.isEnum ) {
			CommandOption info = meta.getVarEnumOption( var , value );
			return( addEnumOption( info.optName ) );
		}
		if( var.isParam ) {
			CommandOption info = meta.getVarParamOption( var );
			return( addParamOption( info.optName , value ) );
		}
		return( false );
	}
	
	public CommandVar getVar( String varName ) {
		return( meta.getVar( varName ) );
	}
	
	public String getRunningOptions() {
		return( data.getRunningInfo() );
	}
	
	public String getVarValue( CommandVar var ) {
		return( data.getVarValue( var ) );
	}
	
	public boolean isFlagSet( String var ) {
		if( data.getFlagValue( var ) == FLAG.YES )
			return( true );
		return( false );
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
		
		CommandOption info = meta.getOption( opt );
		if( !data.addFlagOption( info ) ) {
			print( "var=" + info.var.varName + " is already set" );
			return( false );
		}
		
		return( true );
	}

	public boolean addEnumOption( String opt ) {
		if( !isEnumOption( opt ) )
			throw new RuntimeException( "option=" + opt + " is not a enum" );
		
		CommandOption info = meta.getOption( opt );
		if( !data.addEnumOption( info ) ) {
			print( "var=" + info.var.varName + " is already set" );
			return( false );
		}
		
		return( true );
	}

	public boolean addParamOption( String opt , String value ) {
		if( !isParamOption( opt ) )
			throw new RuntimeException( "option=" + opt + " is not a parameter" );
		
		CommandOption info = meta.getOption( opt );
		if( !data.addParamOption( info , value ) ) {
			print( "var=" + info.var.varName + " is already set" );
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

	public List<CommandOption> getOptionsSet() {
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
	
	public Date getDateArg( int pos ) {
		return( data.getDateArg( pos ) );
	}
	
	public String[] getArgList( int startFrom ) {
		return( data.getArgList( startFrom ) );
	}
	
	public boolean combineValue( String optVar , FLAG confValue , boolean defValue ) {
		return( data.combineValue( optVar , confValue , defValue ) );
	}
	
	public boolean checkValidOptions( CommandMethodMeta commandAction ) {
		for( CommandOption opt : data.getOptionsSet() ) {
			if( !commandAction.isOptionApplicable( opt ) ) {
				print( "option " + opt.optName + " is not applicable for action " + commandAction.name );
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
		meta.showTopHelp( builder , main , commands , this );
	}

	public void showCommandHelp( CommandBuilder builder , CommandMeta commandInfo , boolean main ) {
		meta.showCommandHelp( builder , commandInfo , main , this );
	}

	public void showActionHelp( CommandBuilder builder , CommandMethodMeta action ) {
		meta.showActionHelp( builder , action , this );
	}

	public CommandVar[] getDefinedVariables() {
		return( meta.varByName.values().toArray( new CommandVar[0] ) );
	}

	public void setArgs( String[] args ) {
		data.setArgs( args );
	}

	public CommandOption setParam( CommandVar var , String value ) {
		CommandOption info = meta.getVarParamOption( var );
		addParamOption( info.optName , value );
		return( info );
	}
	
	public CommandOption setFlag( CommandVar var , boolean value ) {
		CommandOption info = meta.getVarFlagOption( var , value );
		addFlagOption( info.optName );
		return( info );
	}
	
	public CommandOption setEnum( CommandVar var , String value ) {
		CommandOption info = meta.getVarEnumOption( var , value );
		addFlagOption( info.optName );
		return( info );
	}
	
	public void clearVar( CommandVar var ) {
		data.clearVar( var );
	}
	
	public void clearData() {
		data.clear();
	}
	
	public boolean isLocalRun() {
		return( getFlagValue( "OPT_LOCAL" , false ) );
	}

}
