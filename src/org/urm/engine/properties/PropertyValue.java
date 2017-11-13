package org.urm.engine.properties;

import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.common._Error;
import org.urm.db.DBEnums.*;
import org.urm.engine.shell.ShellExecutor;

public class PropertyValue {
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
	
	private DBEnumParamValueType type;
	private String defaultValue;
	private String originalValue;
	private String finalValue;
	
	private boolean nullvalue;
	private boolean resolved;
	private boolean system;
	private boolean missing;
	private boolean templated;
	
	public PropertyValue( PropertyValue src ) {
		this.property = src.property;
		this.desc = src.desc;
		this.origin = src.origin;
		this.originSet = src.originSet;
		this.required = src.required;
		this.type = src.type;
		this.nullvalue = src.nullvalue;
		this.defaultValue = src.defaultValue;
		this.originalValue = src.originalValue;
		this.finalValue = src.finalValue;
		this.resolved = src.resolved;
		this.system = src.system;
		this.missing = src.missing;
		this.templated = src.templated;
	}

	public PropertyValue( String property , PropertyValueOrigin origin , PropertySet originSet , String desc ) {
		this.property = property;
		this.desc = desc;
		this.origin = origin;
		this.originSet = originSet;
		this.required = false;
		this.type = DBEnumParamValueType.STRING;
		this.resolved = true;
		this.system = false;
		this.missing = true;
		this.nullvalue = true;
		this.templated = false;
		this.defaultValue = "";
		this.originalValue = "";
		this.finalValue = "";
	}

	public boolean isNull() {
		return( nullvalue );
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
	
	public boolean isTemplated() {
		return( templated );
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
	
	public DBEnumParamValueType getType() {
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
	
	public void setTemplated() {
		this.templated = true;
	}
	
	public void setNull() {
		this.nullvalue = true;
		this.originalValue = "";
		this.finalValue = "";
		this.resolved = true;
	}
	
	public void setValue( PropertyValue pv ) {
		if( pv == null || pv.nullvalue ) {
			setNull();
			return;
		}
		
		this.nullvalue = false;
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
		if( value == null ) {
			nullvalue = true;
			originalValue = "";
		}
		else {
			nullvalue = false;
			if( type == DBEnumParamValueType.PATH )
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
	
	public void setType( DBEnumParamValueType type ) throws Exception {
		if( this.type != type ) {
			if( this.type != DBEnumParamValueType.STRING )
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
		if( finalValue.isEmpty() )
			finalValue = defaultValue;
		resolved = isFinal( finalValue );
	}
	
	public void setDefault( PropertyValue value ) {
		setDefault( value.getOriginalValue() );
	}
	
	public void setString( String value ) throws Exception {
		type = DBEnumParamValueType.STRING;
		setOriginalAndFinalValue( value );
	}
	
	public void setNumber( String value ) throws Exception {
		type = DBEnumParamValueType.NUMBER;
		setOriginalAndFinalValue( value );
	}
	
	public void setNumber( int value ) throws Exception {
		setNumber( "" + value );
	}

	public void setBool( boolean value ) throws Exception {
		type = DBEnumParamValueType.BOOL;
		setOriginalAndFinalValue( Common.getBooleanValue( value ) );
	}
	
	public void setBool( String value ) throws Exception {
		type = DBEnumParamValueType.BOOL;
		setOriginalAndFinalValue( value );
	}
	
	public void setPath( String value , ShellExecutor shell ) throws Exception {
		type = DBEnumParamValueType.PATH;
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
		if( type == DBEnumParamValueType.PATH ) {
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
				if( type == DBEnumParamValueType.NUMBER ) {
					try {
						Integer.parseInt( value );
					}
					catch( Throwable e ) {
						Common.exit1( _Error.InvalidNumberValue1 , "invalid number value=" + value , value );
					}
				}
				else
				if( type == DBEnumParamValueType.BOOL ) {
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

	public static DBEnumParamValueType getItemValueType( String TYPE , boolean required ) throws Exception {
		if( TYPE.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingItemValueType0 , "missing item value type" );
			return( DBEnumParamValueType.UNKNOWN );
		}
		
		DBEnumParamValueType value = null;		
		try {
			value = DBEnumParamValueType.valueOf( Common.xmlToEnumValue( TYPE ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidItemValueType1 , "invalid item value type=" + TYPE , TYPE );
		}
		
		return( value );
	}

}
