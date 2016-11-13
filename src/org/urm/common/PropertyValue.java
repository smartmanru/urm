package org.urm.common;

import org.urm.engine.shell.ShellExecutor;

public class PropertyValue {
	public enum PropertyValueType {
		UNKNOWN ,
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
		PROPERTY_CUSTOM
	};
	
	public String property;
	public String desc;
	public PropertyValueOrigin origin;
	public PropertySet originSet;
	public boolean required;
	
	private PropertyValueType type;
	private String defaultValue;
	private String originalValue;
	private String finalValue;
	
	private boolean resolved;
	private boolean system;
	private boolean missing;
	
	public PropertyValue( PropertyValue src ) {
		this.property = src.property;
		this.desc = src.desc;
		this.origin = src.origin;
		this.originSet = src.originSet;
		this.required = src.required;
		this.type = src.type;
		this.defaultValue = src.defaultValue;
		this.originalValue = src.originalValue;
		this.finalValue = src.finalValue;
		this.resolved = src.resolved;
		this.system = src.system;
		this.missing = src.missing;
	}

	public PropertyValue( String property , PropertyValueOrigin origin , PropertySet originSet , String desc ) {
		this.property = property;
		this.desc = desc;
		this.origin = origin;
		this.originSet = originSet;
		this.required = false;
		this.type = PropertyValueType.PROPERTY_STRING;
		this.resolved = true;
		this.system = false;
		this.missing = true;
		this.defaultValue = "";
		this.originalValue = "";
		this.finalValue = "";
	}

	public boolean isManual() {
		if( origin == PropertyValueOrigin.PROPERTY_MANUAL )
			return( true );
		return( false );
	}
	
	public boolean isCustom() {
		if( origin == PropertyValueOrigin.PROPERTY_CUSTOM )
			return( true );
		return( false );
	}
	
	public boolean isRequired() {
		return( required );
	}
	
	public boolean isDefault() {
		if( originalValue.isEmpty() || originalValue.equals( defaultValue ) )
			return( true );
		return( false );
	}

	public boolean isFinalEmpty() {
		if( finalValue.isEmpty() )
			return( true );
		return( false );
	}
	
	public boolean isOriginalEmpty() {
		if( originalValue.isEmpty() && defaultValue.isEmpty() )
			return( true );
		return( false );
	}
	
	public boolean isMissing() {
		return( missing );
	}
	
	public boolean isResolved() {
		return( resolved );
	}
	
	public boolean isSystem() {
		return( system );
	}

	public boolean isCorrect() {
		if( !isResolved() )
			return( false );
		if( required && finalValue.isEmpty() )
			return( false );
		return( true );
	}
	
	public PropertyValueType getType() {
		return( type );
	}
	
	public String getOriginalValue() {
		return( originalValue );
	}
	
	public String getFinalValue() {
		return( finalValue );
	}
	
	public void setName( String propNew ) {
		this.property = propNew;
	}
	
	public void setDesc( String descNew ) {
		this.desc = descNew;
	}
	
	public void setValue( PropertyValue pv ) {
		this.originalValue = pv.originalValue;
		this.finalValue = pv.originalValue;
		this.resolved = isFinal( finalValue );
	}
	
	public void setFinalFromOriginalValue() throws Exception {
		if( originalValue.isEmpty() )
			setFinalValueInternal( defaultValue );
		else
			setFinalValueInternal( originalValue );
	}
	
	public void setFinalValue( String value ) throws Exception {
		setFinalValueInternal( value );
	}
	
	public void setOriginalAndFinalValue( String value ) throws Exception {
		setOriginalValue( value );
		setFinalValueInternal( value );
	}
	
	public void setOriginalAndFinalValue( String originalValue , String finalValue ) throws Exception {
		setOriginalValue( originalValue );
		setFinalValueInternal( finalValue );
	}

	public void setOriginalValue( String value ) throws Exception {
		if( value == null )
			originalValue = "";
		else {
			if( type == PropertyValueType.PROPERTY_PATH )
				value = Common.getLinuxPath( value );
			originalValue = value;
		}
	}
	
	public void setSystem() {
		this.system = true;
	}
	
	public void setRequired() {
		this.required = true;
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
		if( originalValue.isEmpty() )
			finalValue = defaultValue;
		resolved = isFinal( finalValue );
	}
	
	public void setDefault( PropertyValue value ) {
		setDefault( value.getOriginalValue() );
	}
	
	public void setString( String value ) throws Exception {
		type = PropertyValueType.PROPERTY_STRING;
		setOriginalAndFinalValue( value );
	}
	
	public void setNumber( String value ) throws Exception {
		type = PropertyValueType.PROPERTY_NUMBER;
		setOriginalAndFinalValue( value );
	}
	
	public void setNumber( int value ) throws Exception {
		setNumber( "" + value );
	}

	public void setBool( boolean value ) throws Exception {
		type = PropertyValueType.PROPERTY_BOOL;
		setOriginalAndFinalValue( Common.getBooleanValue( value ) );
	}
	
	public void setBool( String value ) throws Exception {
		type = PropertyValueType.PROPERTY_BOOL;
		setOriginalAndFinalValue( value );
	}
	
	public void setPath( String value , ShellExecutor shell ) throws Exception {
		type = PropertyValueType.PROPERTY_PATH;
		if( value != null ) {
			value = Common.getLinuxPath( value );
		
			if( value.startsWith( "~/") ) {
				if( shell != null )
					value = shell.getHomePath() + value.substring( 1 );
				else
					value = "@" + RunContext.PROPERTY_USER_HOME + "@" + value.substring( 1 );
			}
		}
		
		setOriginalAndFinalValue( value );
	}

	public boolean getFinalBool() {
		if( finalValue.isEmpty() ) {
			if( defaultValue.isEmpty() )
				return( false );
			return( Common.getBooleanValue( defaultValue ) );
		}
		return( Common.getBooleanValue( finalValue ) );
	}

	public int getNumber() {
		if( finalValue.isEmpty() ) {
			if( defaultValue.isEmpty() )
				return( 0 );
			return( Integer.parseInt( defaultValue ) );
		}
		return( Integer.parseInt( finalValue ) );
	}
	
	public String getExpressionValue() {
		if( originalValue.isEmpty() )
			return( defaultValue );
		return( originalValue );
	}

	public String getString() {
		if( finalValue.isEmpty() )
			return( defaultValue );
		return( finalValue );
	}

	public String getPath( boolean isFinalValue , boolean isWindows ) {
		return( getPathValue( getFinalValue() , isFinalValue , isWindows ) );		
	}
	
	public String getPath( boolean isWindows ) {
		if( finalValue.isEmpty() )
			return( getPathValue( defaultValue , false , isWindows ) );
		return( getPathValue( finalValue , false , isWindows ) );
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
	
	private void setFinalValueInternal( String value ) throws Exception {
		if( type == PropertyValueType.PROPERTY_PATH ) {
			if( value != null )
				value = Common.getLinuxPath( value );
		}
			
		if( value == null ) {
			this.finalValue = "";
			this.resolved = true;
			this.missing = true;
		}
		else {
			this.finalValue = value;
			this.resolved = isFinal( value );
			this.missing = false;
			
			if( finalValue.isEmpty() )
				return;
			
			if( resolved ) {
				if( type == PropertyValueType.PROPERTY_NUMBER ) {
					try {
						Integer.parseInt( value );
					}
					catch( Throwable e ) {
						Common.exit1( _Error.InvalidNumberValue1 , "invalid number value=" + value , value );
					}
				}
				else
				if( type == PropertyValueType.PROPERTY_BOOL ) {
					try {
						Common.getBooleanValue( value );
					}
					catch( Throwable e ) {
						Common.exit1( _Error.InvalidNumberValue1 , "invalid boolean value=" + value , value );
					}
				}
			}
		}
	}

	public static PropertyValueType getItemValueType( String TYPE , boolean required ) throws Exception {
		if( TYPE.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingItemValueType0 , "missing item value type" );
			return( PropertyValueType.UNKNOWN );
		}
		
		PropertyValueType value = null;		
		try {
			value = PropertyValueType.valueOf( Common.xmlToEnumValue( TYPE ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidItemValueType1 , "invalid item value type=" + TYPE , TYPE );
		}
		
		return( value );
	}

}
