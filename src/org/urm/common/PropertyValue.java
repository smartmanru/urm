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
	
	String property;
	PropertyValueOrigin origin;
	PropertySet originSet;
	
	PropertyValueType type;
	String data;
	
	public PropertyValue( PropertyValue src ) {
		this.property = src.property;
		this.origin = src.origin;
		this.originSet = src.originSet;
		this.type = src.type;
		this.data = src.data;
	}

	public PropertyValue( String property , PropertyValueOrigin origin , PropertySet originSet ) {
		this.property = property;
		this.origin = origin;
		this.originSet = originSet;
	}

	public void setData( PropertyValue value ) throws Exception {
		type = value.type;
		data = value.data;
	}
	
	public void setString( String value ) throws Exception {
		type = PropertyValueType.PROPERTY_STRING;
		if( value == null || value.isEmpty() )
			data = "";
		else
			data = value;
	}
	
	public void setNumber( String value ) throws Exception {
		if( value == null || value.isEmpty() ) {
			type = PropertyValueType.PROPERTY_NUMBER;
			data = "";
			return;
		}
		
		try {
			Integer.parseInt( value );
		}
		catch( Throwable e ) {
			throw new ExitException( "invalid number value=" + value );
		}
		
		type = PropertyValueType.PROPERTY_NUMBER;
		data = value;
	}
	
	public void setBool( String value ) throws Exception {
		if( value == null || value.isEmpty() ) {
			type = PropertyValueType.PROPERTY_BOOL;
			data = "";
			return;
		}
		
		try {
			Common.getBooleanValue( value );
		}
		catch( Throwable e ) {
			throw new ExitException( "invalid number value=" + value );
		}
		
		type = PropertyValueType.PROPERTY_BOOL;
		data = value;
	}
	
	public void setPath( String value , RunContext execrc ) throws Exception {
		type = PropertyValueType.PROPERTY_PATH;
		if( value == null || value.isEmpty() )
			data = "";
		else
			data = Common.getLinuxPath( value );
		
		if( data.startsWith( "~/") )
			data = execrc.userHome + data.substring( 1 );
	}
}

