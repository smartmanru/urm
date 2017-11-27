package org.urm.engine.properties;

import org.urm.common.Common;
import org.urm.db.core.DBEnumInterface;
import org.urm.db.core.DBEnums.*;

public class EntityVar {

	public PropertyEntity entity;
	
	public int PARAM_ID;
	public DBEnumParamValueType PARAMVALUE_TYPE;
	public DBEnumObjectType OBJECT_TYPE;
	public String NAME;
	public String DBNAME;
	public String XMLNAME;
	public String DESC;
	public boolean REQUIRED;
	public String EXPR_DEF;
	public int VERSION;
	public Class<?> enumClass;
	
	public EntityVar() {
	}
	
	public EntityVar copy() {
		EntityVar r = new EntityVar();
		r.PARAM_ID = PARAM_ID;
		r.PARAMVALUE_TYPE = PARAMVALUE_TYPE;
		r.OBJECT_TYPE = OBJECT_TYPE;
		r.NAME = NAME;
		r.DBNAME = DBNAME;
		r.XMLNAME = XMLNAME;
		r.DESC = DESC;
		r.REQUIRED = REQUIRED;
		r.EXPR_DEF = EXPR_DEF;
		r.VERSION = VERSION;
		r.enumClass = enumClass;
		return( r );
	}

	public static EntityVar metaEnum( String propertyKey , String propertyDesc , boolean required , DBEnumInterface defValue ) {
		return( metaEnumVar( propertyKey , propertyKey , propertyKey , propertyDesc , required , defValue ) ); 
	}
	
	public static EntityVar metaEnumVar( String propertyKey , String propertyDatabaseKey , String propertyXmlKey , String propertyDesc , boolean required , DBEnumInterface defValue ) {
		Enum<?> defEnumValue = ( Enum<?> )defValue;
		EntityVar var = meta( propertyKey , propertyDatabaseKey , propertyXmlKey , propertyDesc , DBEnumParamValueType.NUMBER , DBEnumObjectType.UNKNOWN , required , null );
		var.enumClass = defEnumValue.getClass();
		return( var );
	}
	
	public static EntityVar metaString( String propertyKey , String propertyDesc , boolean required , String defValue ) {
		return( metaStringVar( propertyKey , propertyKey , propertyKey , propertyDesc , required , defValue ) ); 
	}
	
	public static EntityVar metaStringVar( String propertyKey , String propertyDatabaseKey , String propertyXmlKey , String propertyDesc , boolean required , String defValue ) {
		return( meta( propertyKey , propertyDatabaseKey , propertyXmlKey , propertyDesc , DBEnumParamValueType.STRING , DBEnumObjectType.UNKNOWN , required , defValue ) );
	}
	
	public static EntityVar metaInteger( String propertyKey , String propertyDesc , boolean required , Integer defValue ) {
		return( metaIntegerVar( propertyKey , propertyKey , propertyKey , propertyDesc , required , defValue ) ); 
	}
	
	public static EntityVar metaIntegerVar( String propertyKey , String propertyDatabaseKey , String propertyXmlKey , String propertyDesc , boolean required , Integer defValue ) {
		String value = ( defValue == null )? null : "" + defValue;
		return( meta( propertyKey , propertyDatabaseKey , propertyXmlKey , propertyDesc , DBEnumParamValueType.NUMBER , DBEnumObjectType.UNKNOWN , required , value ) );
	}
	
	public static EntityVar metaPathAbsolute( String propertyKey , String propertyDesc , boolean required , String defValue ) {
		return( metaPathAbsoluteVar( propertyKey , propertyKey , propertyKey , propertyDesc , required , defValue ) ); 
	}
	
	public static EntityVar metaPathAbsoluteVar( String propertyKey , String propertyDatabaseKey , String propertyXmlKey , String propertyDesc , boolean required , String defValue ) {
		return( meta( propertyKey , propertyDatabaseKey , propertyXmlKey , propertyDesc , DBEnumParamValueType.PATH , DBEnumObjectType.UNKNOWN , required , defValue ) );
	}
	
	public static EntityVar metaPathRelative( String propertyKey , String propertyDesc , boolean required , String defValue ) {
		return( metaPathRelativeVar( propertyKey , propertyKey , propertyKey , propertyDesc , required , defValue ) ); 
	}
	
	public static EntityVar metaPathRelativeVar( String propertyKey , String propertyDatabaseKey , String propertyXmlKey , String propertyDesc , boolean required , String defValue ) {
		return( meta( propertyKey , propertyDatabaseKey , propertyXmlKey , propertyDesc , DBEnumParamValueType.PATH , DBEnumObjectType.UNKNOWN , required , defValue ) );
	}
	
	public static EntityVar metaBoolean( String propertyKey , String propertyDesc , boolean required , boolean defValue ) {
		return( metaBooleanVar( propertyKey , propertyKey , propertyKey , propertyDesc , required , defValue ) ); 
	}
	
	public static EntityVar metaBooleanVar( String propertyKey , String propertyDatabaseKey , String propertyXmlKey , String propertyDesc , boolean required , boolean defValue ) {
		return( meta( propertyKey , propertyDatabaseKey , propertyXmlKey , propertyDesc , DBEnumParamValueType.BOOL , DBEnumObjectType.UNKNOWN , required , Common.getBooleanValue( defValue ) ) );
	}
	
	public static EntityVar metaObject( String propertyKey , String propertyDesc , DBEnumObjectType objectType , boolean required ) {
		return( metaObjectVar( propertyKey , propertyKey , propertyKey , propertyDesc , objectType , required ) ); 
	}
	
	public static EntityVar metaObjectVar( String propertyKey , String propertyDatabaseKey , String propertyXmlKey , String propertyDesc , DBEnumObjectType objectType , boolean required ) {
		return( meta( propertyKey , propertyDatabaseKey , propertyXmlKey , propertyDesc , DBEnumParamValueType.STRING , objectType , required , null ) );
	}
	
	public static EntityVar meta( String propertyKey , String propertyDatabaseKey , String propertyXmlKey , String propertyDesc , DBEnumParamValueType type , DBEnumObjectType objectType , boolean required , String defValue ) {
		EntityVar var = new EntityVar();
		var.NAME = propertyKey;
		var.DBNAME = propertyDatabaseKey;
		var.XMLNAME = propertyXmlKey;
		var.DESC = propertyDesc;
		var.PARAMVALUE_TYPE = type;
		var.OBJECT_TYPE = objectType;
		var.REQUIRED = required;
		var.EXPR_DEF = defValue;
		return( var );
	}

	public void setEntity( PropertyEntity entity ) {
		this.entity = entity;
	}

	public boolean isApp() {
		if( entity.CUSTOM )
			return( false );
		return( true );
	}
	
	public boolean isCustom() {
		if( entity.CUSTOM )
			return( true );
		return( false );
	}
	
	public static String p( String var ) {
		return( "@" + var + "@" );
	}
	
}
