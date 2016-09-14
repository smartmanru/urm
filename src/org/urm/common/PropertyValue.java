package org.urm.common;

import org.urm.engine.shell.ShellExecutor;

public class PropertyValue {
	public enum PropertyValueType {
		PROPERTY_STRING ,
		PROPERTY_NUMBER ,
		PROPERTY_BOOL ,
		PROPERTY_PATH
	};

	public enum PropertyValueOrigin {
		PROPERTY_ORIGINAL ,
		PROPERTY_MANUAL ,
		PROPERTY_PARENT ,
		PROPERTY_EXTRA ,
	};
	
	public String property;
	public PropertyValueOrigin origin;
	public PropertySet originSet;
	
	public PropertyValueType type;
	private String data;
	private String defaultValue;
	public boolean resolved;
	public boolean system;
	public boolean missing;
	
	public PropertyValue( PropertyValue src ) {
		this.property = src.property;
		this.origin = src.origin;
		this.originSet = src.originSet;
		this.type = src.type;
		this.data = src.data;
		this.defaultValue = src.defaultValue;
		this.resolved = src.resolved;
		this.system = src.system;
		this.missing = src.missing;
	}

	public PropertyValue( String value ) {
		this.property = "";
		this.origin = PropertyValueOrigin.PROPERTY_MANUAL;
		this.originSet = null;
		this.type = PropertyValueType.PROPERTY_STRING;
		this.system = false;
		this.data = "";
		this.defaultValue = "";
		setValueInternal( value );
	}
	
	public PropertyValue( String property , PropertyValueOrigin origin , PropertySet originSet ) {
		this.property = property;
		this.origin = origin;
		this.originSet = originSet;
		this.type = PropertyValueType.PROPERTY_STRING;
		this.resolved = true;
		this.system = false;
		this.missing = true;
		this.data = "";
		this.defaultValue = "";
	}

	public String getData() {
		return( data );
	}
	
	public String getValue() {
		if( data.isEmpty() )
			return( defaultValue );
		return( data );
	}

	public boolean isDefault() {
		if( data.isEmpty() || data.equals( defaultValue ) )
			return( true );
		return( false );
	}

	public boolean isDataEmpty() {
		return( data.isEmpty() );
	}
	
	public boolean isEmpty() {
		if( data.isEmpty() && defaultValue.isEmpty() )
			return( true );
		return( false );
	}
	
	public boolean isMissing() {
		return( missing );
	}
	
	public void setSystem() {
		this.system = true;
	}
	
	public void setType( PropertyValueType type ) throws Exception {
		if( this.type != type ) {
			if( this.type != PropertyValueType.PROPERTY_STRING )
				Common.exit1( _Error.PropertyMismatchedType1 , "property is of mismatched type name=" + property , property );
			this.type = type;
		}
	}
	
	public static boolean isFinal( String s ) {
		int index = s.indexOf( '@' );
		if( index >= 0 ) {
			int index2 = s.indexOf( '@' , index + 1 );
			if( index2 > 0 && index2 != index + 1 )
				return( false );
		}
		return( true );
	}

	public void setDefault( String value ) {
		defaultValue = value;
		resolved = isFinal( defaultValue );
	}
	
	public void setDefault( PropertyValue value ) {
		defaultValue = value.getValue();
		resolved = isFinal( defaultValue );
	}
	
	public void setValueInternal( String value ) {
		if( value == null ) {
			this.data = "";
			this.missing = true;
		}
		else {
			this.data = value;
			this.resolved = isFinal( data );
			this.missing = false;
		}
	}
	
	public void setValue( PropertyValue value ) throws Exception {
		type = value.type;
		setValueInternal( value.data );
	}
	
	public void setString( String value ) throws Exception {
		type = PropertyValueType.PROPERTY_STRING;
		setValueInternal( value );
	}
	
	public void setNumber( String value ) throws Exception {
		if( value == null || value.isEmpty() ) {
			type = PropertyValueType.PROPERTY_NUMBER;
			setValueInternal( value );
			return;
		}
		
		try {
			Integer.parseInt( value );
		}
		catch( Throwable e ) {
			Common.exit1( _Error.InvalidNumberValue1 , "invalid number value=" + value , value );
		}
		
		type = PropertyValueType.PROPERTY_NUMBER;
		setValueInternal( value );
	}
	
	public void setNumber( int value ) {
		data = "" + value;
		type = PropertyValueType.PROPERTY_NUMBER;
	}

	public void setBool( boolean value ) {
		data = Common.getBooleanValue( value );
		type = PropertyValueType.PROPERTY_BOOL;
	}
	
	public void setBool( String value ) throws Exception {
		if( value == null || value.isEmpty() ) {
			type = PropertyValueType.PROPERTY_BOOL;
			setValueInternal( value );
			return;
		}
		
		try {
			Common.getBooleanValue( value );
		}
		catch( Throwable e ) {
			Common.exit1( _Error.InvalidNumberValue1 , "invalid boolean value=" + value , value );
		}
		
		type = PropertyValueType.PROPERTY_BOOL;
		setValueInternal( value );
	}
	
	public void setPath( String value , ShellExecutor shell ) throws Exception {
		type = PropertyValueType.PROPERTY_PATH;
		if( value == null || value.isEmpty() )
			setValueInternal( value );
		else
			setValueInternal( Common.getLinuxPath( value ) );
		
		if( data.startsWith( "~/") ) {
			if( shell != null )
				setValueInternal( data = shell.getHomePath() + data.substring( 1 ) );
			else
				setValueInternal( data = "@" + RunContext.PROPERTY_USER_HOME + "@" + data.substring( 1 ) );
		}
	}

	public boolean getBool() {
		if( data.isEmpty() ) {
			if( defaultValue.isEmpty() )
				return( false );
			return( Common.getBooleanValue( defaultValue ) );
		}
		return( Common.getBooleanValue( data ) );
	}

	public int getNumber() {
		if( data.isEmpty() ) {
			if( defaultValue.isEmpty() )
				return( 0 );
			return( Integer.parseInt( defaultValue ) );
		}
		return( Integer.parseInt( data ) );
	}
	
	public String getString() {
		if( data.isEmpty() )
			return( defaultValue );
		return( data );
	}

	public String getPath( boolean finalValue , boolean isWindows ) {
		return( getPathValue( getValue() , finalValue , isWindows ) );		
	}
	
	public String getPath( boolean isWindows ) {
		if( data.isEmpty() )
			return( getPathValue( defaultValue , false , isWindows ) );
		return( getPathValue( data , false , isWindows ) );
	}

	public static String getPathValue( String v , boolean finalValue , boolean isWindows ) {
		if( finalValue ) {
			if( isWindows == false )
				return( Common.getLinuxPath( v ) );
			if( isWindows == true )
				return( Common.getWinPath( v ) );
		}

		return( v );
	}
	
}
