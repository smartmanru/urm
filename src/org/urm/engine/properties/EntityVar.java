package org.urm.engine.properties;

import org.urm.db.core.DBEnums.*;

public class EntityVar {

	public int ID;
	public DBEnumParamValueType PARAMVALUE_TYPE;
	public String NAME;
	public String DESC;
	public boolean REQUIRED;
	public String EXPR_DEF;
	public int VERSION;

	public EntityVar() {
	}
	
	public static EntityVar metaString( String propertyKey , String propertyDesc , boolean required , String defValue ) {
		return( meta( propertyKey , propertyDesc , DBEnumParamValueType.STRING , required , defValue ) );
	}
	
	public static EntityVar metaInteger( String propertyKey , String propertyDesc , boolean required , String defValue ) {
		return( meta( propertyKey , propertyDesc , DBEnumParamValueType.NUMBER , required , defValue ) );
	}
	
	public static EntityVar metaPathAbsolute( String propertyKey , String propertyDesc , boolean required , String defValue ) {
		return( meta( propertyKey , propertyDesc , DBEnumParamValueType.PATH , required , defValue ) );
	}
	
	public static EntityVar metaPathRelative( String propertyKey , String propertyDesc , boolean required , String defValue ) {
		return( meta( propertyKey , propertyDesc , DBEnumParamValueType.PATH , required , defValue ) );
	}
	
	public static EntityVar metaBool( String propertyKey , String propertyDesc , boolean required , String defValue ) {
		return( meta( propertyKey , propertyDesc , DBEnumParamValueType.BOOL , required , defValue ) );
	}
	
	public static EntityVar meta( String propertyKey , String propertyDesc , DBEnumParamValueType type , boolean required , String defValue ) {
		EntityVar var = new EntityVar();
		var.NAME = propertyKey;
		var.DESC = propertyDesc;
		var.PARAMVALUE_TYPE = type;
		var.REQUIRED = required;
		var.EXPR_DEF = defValue;
		return( var );
	}
	
}
