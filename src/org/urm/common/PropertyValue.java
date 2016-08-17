package org.urm.common;

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
	public String data;
	public boolean resolved;
	public boolean system;
	public boolean missing;
	
	public PropertyValue( PropertyValue src ) {
		this.property = src.property;
		this.origin = src.origin;
		this.originSet = src.originSet;
		this.type = src.type;
		this.data = src.data;
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
		setValue( value );
	}
	
	public PropertyValue( String property , PropertyValueOrigin origin , PropertySet originSet ) {
		this.property = property;
		this.origin = origin;
		this.originSet = originSet;
		this.resolved = true;
		this.system = false;
		this.missing = true;
	}

	public boolean isMissing() {
		return( missing );
	}
	
	public void setSystem() {
		this.system = true;
	}
	
	public void setType( PropertyValueType type ) {
		this.type = type;
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
	
	public void setValue( String value ) {
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
	
	public void setData( PropertyValue value ) throws Exception {
		type = value.type;
		setValue( value.data );
	}
	
	public void setString( String value ) throws Exception {
		type = PropertyValueType.PROPERTY_STRING;
		setValue( value );
	}
	
	public void setNumber( String value ) throws Exception {
		if( value == null || value.isEmpty() ) {
			type = PropertyValueType.PROPERTY_NUMBER;
			setValue( value );
			return;
		}
		
		try {
			Integer.parseInt( value );
		}
		catch( Throwable e ) {
			throw new ExitException( "invalid number value=" + value );
		}
		
		type = PropertyValueType.PROPERTY_NUMBER;
		setValue( value );
	}
	
	public void setNumber( int value ) throws Exception {
		data = "" + value;
		type = PropertyValueType.PROPERTY_NUMBER;
	}
	
	public void setBool( String value ) throws Exception {
		if( value == null || value.isEmpty() ) {
			type = PropertyValueType.PROPERTY_BOOL;
			setValue( value );
			return;
		}
		
		try {
			Common.getBooleanValue( value );
		}
		catch( Throwable e ) {
			throw new ExitException( "invalid number value=" + value );
		}
		
		type = PropertyValueType.PROPERTY_BOOL;
		setValue( value );
	}
	
	public void setPath( String value , RunContext execrc ) throws Exception {
		type = PropertyValueType.PROPERTY_PATH;
		if( value == null || value.isEmpty() )
			setValue( value );
		else
			setValue( Common.getLinuxPath( value ) );
		
		if( data.startsWith( "~/") )
			setValue( data = execrc.userHome + data.substring( 1 ) );
	}
	
}
