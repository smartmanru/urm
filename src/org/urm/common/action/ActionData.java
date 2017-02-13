package org.urm.common.action;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.common.action.CommandVar.FLAG;

public class ActionData implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4728461832076034211L;

	// standard command parameters
	public RunContext clientrc;

	protected Map<String,FLAG> flags = new HashMap<String,FLAG>();
	protected Map<String,String> enums = new HashMap<String,String>();
	protected Map<String,String> params = new HashMap<String,String>();
	protected List<String> args = new LinkedList<String>();
	protected List<CommandVar> optionsSet = new LinkedList<CommandVar>();

	public ActionData( RunContext clientrc ) {
		this.clientrc = clientrc;
	}

	public String getOptionValue( CommandVar var ) {
		String value;
		if( var.isFlag )
			value = Common.getEnumLower( flags.get( var.varName ) );
		else
		if( var.isEnum )
			value = enums.get( var.varName );
		else
			value = params.get( var.varName );
		return( var.varName + "=" + value );
	}

	public boolean addFlagOption( CommandVar var ) {
		if( flags.get( var.varName ) != null )
			return( false );
		
		flags.put( var.varName , var.varValue );
		optionsSet.add( var );
		return( true );
	}

	public boolean addEnumOption( CommandVar var ) {
		if( enums.get( var.varName ) != null )
			return( false );
		
		enums.put( var.varName , var.varEnumValue );
		optionsSet.add( var );
		return( true );
	}

	public boolean addParamOption( CommandVar var , String value ) {
		if( params.get( var.varName ) != null )
			return( false );
		
		params.put( var.varName , value );
		optionsSet.add( var );
		return( true );
	}
	
	public boolean addArg( String value ) {
		args.add( value );
		return( true );
	}

	public FLAG getFlagValue( String var ) {
		FLAG val = flags.get( var );
		return( val );
	}
	
	public boolean getFlagValue( String var , boolean defValue ) {
		FLAG val = flags.get( var );
		if( val == null )
			return( defValue );
		
		if( val == FLAG.YES )
			return( true );
		
		return( false );
	}
	
	public String getEnumValue( String var ) {
		String val = enums.get( var );
		if( val == null )
			return( "" );
		return( val );
	}

	public String getParamValue( String var ) {
		String val = params.get( var );
		if( val == null )
			return( "" );
		return( val );
	}
	
	public int getIntParamValue( String var , int defaultValue ) {
		String val = params.get( var );
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
		FLAG optValue = flags.get( optVar );

		// option always overrides
		if( optValue != null && optValue != FLAG.DEFAULT )
			return( optValue == FLAG.YES );
		
		// if configuration is present
		if( confValue != null && confValue != FLAG.DEFAULT )
			return( confValue == FLAG.YES );
		
		return( defValue );
	}
	
	public String getRunningOptions() {
		String values = "";
		for( CommandVar option : optionsSet ) {
			String value = getOptionValue( option );
			values = Common.addToList( values , value , ", " );
		}
		
		String info = "execute options={" + values + "}, args={" + 
				Common.getList( args.toArray( new String[0] ) , ", " ) + "}";
		return( info );
	}
	
	public String getFlagsSet() {
		String s = "";
		for( int k = 0; k < optionsSet.size(); k++ ) {
			CommandVar var = optionsSet.get( k );
			if( var.isFlag ) {
				if( !s.isEmpty() )
					s += " ";
				s += var.varName + "=" + flags.get( var.varName );
			}
			else if( var.isEnum ) {
				if( !s.isEmpty() )
					s += " ";
				s += var.varName + "=" + enums.get( var.varName );
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
			s += var.varName + "=" + params.get( var.varName );
		}
		return( s );
	}

	public List<CommandVar> getOptionsSet() {
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
	
	public void setParam( CommandVar var , String value ) {
		if( value == null || value.isEmpty() ) {
			params.remove( var.varName );
			return;
		}
		
		params.put( var.varName , value );
	}
	
	public void setFlag( CommandVar var , boolean value ) {
		flags.put( var.varName , ( value )? FLAG.YES : FLAG.NO );
	}
	
	public void clearFlag( CommandVar var ) {
		flags.remove( var.varName );
	}

	public void clearParam( CommandVar var ) {
		params.remove( var.varName );
	}

	public void clear() {
		params.clear();
		flags.clear();
		enums.clear();
		args.clear();
		optionsSet.clear();
	}

	public void set( ActionData src ) {
		clear();
		
		params.putAll( src.params );
		flags.putAll( src.flags );
		enums.putAll( src.enums );
		args.addAll( src.args );
		optionsSet.addAll( src.optionsSet );
	}
	
}
