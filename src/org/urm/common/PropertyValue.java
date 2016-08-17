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
	
	public PropertyValue( PropertyValue src ) {
		this.property = src.property;
		this.origin = src.origin;
		this.originSet = src.originSet;
		this.type = src.type;
		this.data = src.data;
		this.resolved = src.resolved;
		this.system = src.system;
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
	}

	public void setSystem() {
		this.system = true;
	}
	
	public void setType( PropertyValueType type ) {
		this.type = type;
	}
	
	public void setValue( String value ) {
		this.data = value;
		this.resolved = true;
		
		int index = data.indexOf( '@' );
		if( index >= 0 ) {
			int index2 = data.indexOf( '@' , index + 1 );
			if( index2 > 0 && index2 != index + 1 )
				this.resolved = false;
		}
	}
	
	public void setData( PropertyValue value ) throws Exception {
		type = value.type;
		setValue( value.data );
	}
	
	public void setString( String value ) throws Exception {
		type = PropertyValueType.PROPERTY_STRING;
		if( value == null || value.isEmpty() )
			setValue( "" );
		else
			setValue( value );
	}
	
	public void setNumber( String value ) throws Exception {
		if( value == null || value.isEmpty() ) {
			type = PropertyValueType.PROPERTY_NUMBER;
			setValue( "" );
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
	
	public void setBool( String value ) throws Exception {
		if( value == null || value.isEmpty() ) {
			type = PropertyValueType.PROPERTY_BOOL;
			setValue( "" );
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
			setValue( "" );
		else
			setValue( Common.getLinuxPath( value ) );
		
		if( data.startsWith( "~/") )
			setValue( data = execrc.userHome + data.substring( 1 ) );
	}
	
}
