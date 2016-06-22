package org.urm.common.action;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.common.action.CommandOptions.FLAG;

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

	public ActionData() {
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
		return( true );
	}

	public boolean addEnumOption( CommandVar var ) {
		if( enums.get( var.varName ) != null )
			return( false );
		
		enums.put( var.varName , var.varEnumValue );
		return( true );
	}

	public boolean addParamOption( CommandVar var , String value ) {
		if( params.get( var.varName ) != null )
			return( false );
		
		params.put( var.varName , value );
		return( true );
	}
	
	public boolean addArg( String value ) {
		args.add( value );
		return( true );
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
	
}
