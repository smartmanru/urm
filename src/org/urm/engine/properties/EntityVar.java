package org.urm.engine.properties;

import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;

public class EntityVar {

	public PropertyEntity entity;
	
	public int ID;
	public DBEnumParamValueType PARAMVALUE_TYPE;
	public DBEnumObjectType OBJECT_TYPE;
	public String NAME;
	public String DESC;
	public boolean REQUIRED;
	public String EXPR_DEF;
	public int VERSION;

	public EntityVar() {
	}
	
	public static EntityVar metaString( String propertyKey , String propertyDesc , boolean required , String defValue ) {
		return( meta( propertyKey , propertyDesc , DBEnumParamValueType.STRING , DBEnumObjectType.UNKNOWN , required , defValue ) );
	}
	
	public static EntityVar metaInteger( String propertyKey , String propertyDesc , boolean required , Integer defValue ) {
		String value = ( defValue == null )? null : "" + defValue;
		return( meta( propertyKey , propertyDesc , DBEnumParamValueType.NUMBER , DBEnumObjectType.UNKNOWN , required , value ) );
	}
	
	public static EntityVar metaPathAbsolute( String propertyKey , String propertyDesc , boolean required , String defValue ) {
		return( meta( propertyKey , propertyDesc , DBEnumParamValueType.PATH , DBEnumObjectType.UNKNOWN , required , defValue ) );
	}
	
	public static EntityVar metaPathRelative( String propertyKey , String propertyDesc , boolean required , String defValue ) {
		return( meta( propertyKey , propertyDesc , DBEnumParamValueType.PATH , DBEnumObjectType.UNKNOWN , required , defValue ) );
	}
	
	public static EntityVar metaBoolean( String propertyKey , String propertyDesc , boolean required , boolean defValue ) {
		return( meta( propertyKey , propertyDesc , DBEnumParamValueType.BOOL , DBEnumObjectType.UNKNOWN , required , Common.getBooleanValue( defValue ) ) );
	}
	
	public static EntityVar metaObject( String propertyKey , String propertyDesc , DBEnumObjectType objectType , boolean required ) {
		return( meta( propertyKey , propertyDesc , DBEnumParamValueType.STRING , objectType , required , null ) );
	}
	
	public static EntityVar meta( String propertyKey , String propertyDesc , DBEnumParamValueType type , DBEnumObjectType objectType , boolean required , String defValue ) {
		EntityVar var = new EntityVar();
		var.NAME = propertyKey;
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
		if( entity.custom )
			return( false );
		return( true );
	}
	
	public boolean isCustom() {
		if( entity.custom )
			return( true );
		return( false );
	}
	
	public static String p( String var ) {
		return( "@" + var + "@" );
	}
	
}
