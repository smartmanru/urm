package org.urm.common.action;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.common.action.CommandOption.FLAG;

public class ActionData implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4728461832076034211L;

	// standard command parameters
	public RunContext clientrc;

	protected Map<String,FLAG> varFlags = new HashMap<String,FLAG>();
	protected Map<String,String> varEnums = new HashMap<String,String>();
	protected Map<String,String> varParams = new HashMap<String,String>();
	protected List<CommandOption> optionsSet = new LinkedList<CommandOption>();
	protected List<String> args = new LinkedList<String>();

	public ActionData( RunContext clientrc ) {
		this.clientrc = clientrc;
	}

	public String getVarValue( CommandVar var ) {
		String value;
		if( var.isFlag )
			value = Common.getEnumLower( varFlags.get( var.varName ) );
		else
		if( var.isEnum )
			value = varEnums.get( var.varName );
		else
			value = varParams.get( var.varName );
		return( var.varName + "=" + value );
	}

	public boolean addFlagOption( CommandOption opt ) {
		if( varFlags.get( opt.var.varName ) != null )
			return( false );
		
		varFlags.put( opt.var.varName , opt.varFlagValue );
		optionsSet.add( opt );
		return( true );
	}

	public boolean addEnumOption( CommandOption opt ) {
		if( varEnums.get( opt.var.varName ) != null )
			return( false );
		
		varEnums.put( opt.var.varName , opt.varEnumValue );
		optionsSet.add( opt );
		return( true );
	}

	public boolean addParamOption( CommandOption opt , String value ) {
		if( varParams.get( opt.var.varName ) != null )
			return( false );
		
		varParams.put( opt.var.varName , value );
		optionsSet.add( opt );
		return( true );
	}
	
	public boolean addArg( String value ) {
		args.add( value );
		return( true );
	}

	public FLAG getFlagValue( String var ) {
		FLAG val = varFlags.get( var );
		return( val );
	}
	
	public boolean getFlagValue( String var , boolean defValue ) {
		FLAG val = varFlags.get( var );
		if( val == null )
			return( defValue );
		
		if( val == FLAG.YES )
			return( true );
		
		return( false );
	}
	
	public String getEnumValue( String var ) {
		String val = varEnums.get( var );
		if( val == null )
			return( "" );
		return( val );
	}

	public String getParamValue( String var ) {
		String val = varParams.get( var );
		if( val == null )
			return( "" );
		return( val );
	}
	
	public int getIntParamValue( String var , int defaultValue ) {
		String val = varParams.get( var );
		if( val == null || val.isEmpty() )
			return( defaultValue );
		return( Integer.parseInt( val ) );
	}
	
	public String getArg( int pos ) {
		if( pos >= args.size() )
			return( "" );
		
		return( args.get( pos ) );
	}
	
	public int getArgCount() {
		return( args.size() );
	}
	
	public int getIntArg( int pos , int defValue ) {
		String value = getArg( pos );
		if( value.isEmpty() )
			return( defValue );
		return( Integer.parseInt( value ) );
	}
	
	public Date getDateArg( int pos ) {
		String value = getArg( pos );
		if( value.isEmpty() )
			return( null );
		return( Common.getDateValue( value ) );
	}
	
	public String[] getArgList( int startFrom ) {
		if( startFrom >= args.size() )
			return( new String[0] );
		
		String[] list = new String[ args.size() - startFrom ];
		for( int k = startFrom; k < args.size(); k++ )
			list[ k - startFrom ] = args.get( k );

		return( list );
	}
	
	public boolean combineValue( String optVar , FLAG confValue , boolean defValue ) {
		FLAG optValue = varFlags.get( optVar );

		// option always overrides
		if( optValue != null && optValue != FLAG.DEFAULT )
			return( optValue == FLAG.YES );
		
		// if configuration is present
		if( confValue != null && confValue != FLAG.DEFAULT )
			return( confValue == FLAG.YES );
		
		return( defValue );
	}
	
	public String getRunningInfo() {
		String values = "";
		for( CommandOption option : optionsSet ) {
			String value = getVarValue( option.var );
			values = Common.addToList( values , value , ", " );
		}
		
		String info = "execute options={" + values + "}, args={" + 
				Common.getList( args.toArray( new String[0] ) , ", " ) + "}";
		return( info );
	}
	
	public String getFlagsSet() {
		String s = "";
		for( int k = 0; k < optionsSet.size(); k++ ) {
			CommandOption opt = optionsSet.get( k );
			if( opt.var.isFlag ) {
				if( !s.isEmpty() )
					s += " ";
				s += opt.var.varName + "=" + varFlags.get( opt.var.varName );
			}
			else if( opt.var.isEnum ) {
				if( !s.isEmpty() )
					s += " ";
				s += opt.var.varName + "=" + varEnums.get( opt.var.varName );
			}
		}
		return( s );
	}
	
	public String getParamsSet() {
		String s = "";
		for( int k = 0; k < optionsSet.size(); k++ ) {
			CommandOption opt = optionsSet.get( k );
			if( opt.var.isFlag || opt.var.isEnum )
				continue;
			
			if( !s.isEmpty() )
				s += " ";
			s += opt.var.varName + "=" + varParams.get( opt.var.varName );
		}
		return( s );
	}

	public List<CommandOption> getOptionsSet() {
		return( optionsSet );
	}
	
	public String getArgsSet() {
		String s = "";
		for( int k = 0; k < args.size(); k++ ) {
			if( !s.isEmpty() )
				s += " ";
			s += args.get( k );
		}
		return( s );
	}

	public void setArgs( String[] xargs ) {
		args.clear();
		for( String arg : xargs )
			args.add( arg );
	}
	
	public void clearFlag( CommandVar var ) {
		varFlags.remove( var.varName );
		for( CommandOption option : optionsSet ) {
			if( option.var == var ) {
				optionsSet.remove( option.optName );
				break;
			}
		}
	}

	public void clearParam( CommandVar var ) {
		varParams.remove( var.varName );
		for( CommandOption option : optionsSet ) {
			if( option.var == var ) {
				optionsSet.remove( option.optName );
				break;
			}
		}
	}

	public void clear() {
		varParams.clear();
		varFlags.clear();
		varEnums.clear();
		optionsSet.clear();
		args.clear();
	}

	public void set( ActionData src ) {
		clear();
		
		varParams.putAll( src.varParams );
		varFlags.putAll( src.varFlags );
		varEnums.putAll( src.varEnums );
		optionsSet.addAll( src.optionsSet );
		args.addAll( src.args );
	}
	
}
