package org.urm.common.action;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;

public class CommandOptions {

	public enum FLAG { DEFAULT , YES , NO }; 
	public enum SQLMODE { UNKNOWN , APPLY , ANYWAY , CORRECT , ROLLBACK , PRINT };
	public enum SQLTYPE { UNKNOWN , SQL , CTL , PUB };

	public int optDefaultCommandTimeout = 10;
	
	// implementation
	public OptionsMeta meta;
	protected List<CommandVar> optionsSet = new LinkedList<CommandVar>();
	public String command;
	public String action;
	public ActionData data;

	public CommandOptions() {
		optionsSet = new LinkedList<CommandVar>();
		meta = new OptionsMeta();
	}

	public CommandOptions( OptionsMeta meta ) {
		optionsSet = new LinkedList<CommandVar>();
		this.meta = meta;
	}

	public void setAction( String command , String action , ActionData data ) {
		this.command = command;
		this.action = action;
		this.data = data;
	}
	
	public boolean parseArgs( String[] cmdParams ) {
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
		data = new ActionData();
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
		String values = "";
		for( CommandVar option : optionsSet ) {
			String value = getOptionValue( option );
			values = Common.addToList( values , value , ", " );
		}
		
		String info = "execute options={" + values + "}, args={" + 
				Common.getList( data.args.toArray( new String[0] ) , ", " ) + "}";
		return( info );
	}
	
	public String getOptionValue( CommandVar var ) {
		return( data.getOptionValue( var ) );
	}
	
	public boolean isValidVar( String var ) {
		return( meta.isValidVar( var ) );
	}
	
	public boolean isFlagOption( String opt ) {
		return( isFlagOption( opt ) );
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
		
		optionsSet.add( info );
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
		
		optionsSet.add( info );
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
		
		optionsSet.add( info );
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
		String s = "";
		for( int k = 0; k < optionsSet.size(); k++ ) {
			CommandVar var = optionsSet.get( k );
			if( var.isFlag ) {
				if( !s.isEmpty() )
					s += " ";
				s += var.varName + "=" + data.flags.get( var.varName );
			}
			else if( var.isEnum ) {
				if( !s.isEmpty() )
					s += " ";
				s += var.varName + "=" + data.enums.get( var.varName );
			}
		}
		return( s );
	}
	
	public String getParamsSet() {
		String s = "";
		for( int k = 0; k < optionsSet.size(); k++ ) {
			CommandVar var = optionsSet.get( k );
			if( var.isFlag || var.isEnum )
				continue;
			
			if( !s.isEmpty() )
				s += " ";
			s += var.varName + "=" + data.params.get( var.varName );
		}
		return( s );
	}

	public List<CommandVar> getOptionsSet() {
		return( optionsSet );
	}
	
	public String getArgsSet() {
		String s = "";
		for( int k = 0; k < data.args.size(); k++ ) {
			if( !s.isEmpty() )
				s += " ";
			s += data.args.get( k );
		}
		return( s );
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
		for( CommandVar var : optionsSet ) {
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
	
}
