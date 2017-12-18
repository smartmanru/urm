package org.urm.engine.properties;

import org.urm.common.Common;
import org.urm.db.core.DBEnumInterface;
import org.urm.db.core.DBEnums.*;
import org.urm.meta.EngineLoader;
import org.urm.meta.engine.AuthResource;
import org.urm.meta.engine.EngineResources;

public class EntityVar {

	public PropertyEntity entity;
	
	public int PARAM_ID;
	public int ENTITYCOLUMN;
	public String NAME;
	public String DBNAME;
	public String XMLNAME;
	public DBEnumParamValueType PARAMVALUE_TYPE;
	public DBEnumParamValueSubtype PARAMVALUE_SUBTYPE;
	public DBEnumObjectType OBJECT_TYPE;
	public String DESC;
	public boolean REQUIRED;
	public String EXPR_DEF;
	public int VERSION;
	
	public Class<?> enumClass;
	public int databaseColumn;
	
	public EntityVar() {
	}
	
	public EntityVar copy() {
		EntityVar r = new EntityVar();
		r.PARAM_ID = PARAM_ID;
		r.ENTITYCOLUMN = ENTITYCOLUMN;
		r.NAME = NAME;
		r.DBNAME = DBNAME;
		r.XMLNAME = XMLNAME;
		r.PARAMVALUE_TYPE = PARAMVALUE_TYPE;
		r.OBJECT_TYPE = OBJECT_TYPE;
		r.DESC = DESC;
		r.REQUIRED = REQUIRED;
		r.EXPR_DEF = EXPR_DEF;
		r.VERSION = VERSION;
		r.enumClass = enumClass;
		r.databaseColumn = databaseColumn;
		return( r );
	}

	public void modifyCustom( String name , String desc , String defvalue ) {
		this.NAME = name;
		this.DESC = Common.nonull( desc );
		this.EXPR_DEF = Common.nonull( defvalue );
	}
	
	public boolean isDefaultEmpty() {
		if( EXPR_DEF.isEmpty() )
			return( true );
		return( false );
	}
	
	public static EntityVar metaEnum( String propertyKey , String propertyDesc , boolean required , DBEnumInterface defValue ) throws Exception {
		return( metaEnumVar( propertyKey , propertyKey , propertyKey , propertyDesc , required , defValue ) ); 
	}
	
	public static EntityVar metaEnumDatabaseOnly( String propertyKey , String propertyDesc , boolean required , DBEnumInterface defValue ) throws Exception {
		return( metaEnumVar( propertyKey , propertyKey , null , propertyDesc , required , defValue ) ); 
	}
	
	public static EntityVar metaEnumXmlOnly( String propertyKey , String propertyDesc , boolean required , DBEnumInterface defValue ) throws Exception {
		return( metaEnumVar( propertyKey , null , propertyKey , propertyDesc , required , defValue ) ); 
	}
	
	public static EntityVar metaEnumVar( String propertyKey , String propertyDatabaseKey , String propertyXmlKey , String propertyDesc , boolean required , DBEnumInterface defValue ) throws Exception {
		if( defValue == null )
			Common.exitUnexpected();
		
		Enum<?> defEnumValue = ( Enum<?> )defValue;
		EntityVar var = meta( propertyKey , propertyDatabaseKey , propertyXmlKey , propertyDesc , DBEnumParamValueType.NUMBER , DBEnumParamValueSubtype.DEFAULT , DBEnumObjectType.UNKNOWN , required , null , defEnumValue.getClass() );
		return( var );
	}
	
	public static EntityVar metaString( String propertyKey , String propertyDesc , boolean required , String defValue ) {
		return( metaStringVar( propertyKey , propertyKey , propertyKey , propertyDesc , required , defValue ) ); 
	}
	
	public static EntityVar metaStringDatabaseOnly( String propertyKey , String propertyDesc , boolean required , String defValue ) {
		return( metaStringVar( propertyKey , propertyKey , null , propertyDesc , required , defValue ) ); 
	}
	
	public static EntityVar metaStringXmlOnly( String propertyKey , String propertyDesc , boolean required , String defValue ) {
		return( metaStringVar( propertyKey , null , propertyKey , propertyDesc , required , defValue ) ); 
	}
	
	public static EntityVar metaStringVar( String propertyKey , String propertyDatabaseKey , String propertyXmlKey , String propertyDesc , boolean required , String defValue ) {
		return( meta( propertyKey , propertyDatabaseKey , propertyXmlKey , propertyDesc , DBEnumParamValueType.STRING , DBEnumParamValueSubtype.DEFAULT , DBEnumObjectType.UNKNOWN , required , defValue , null ) );
	}
	
	public static EntityVar metaInteger( String propertyKey , String propertyDesc , boolean required , Integer defValue ) {
		return( metaIntegerVar( propertyKey , propertyKey , propertyKey , propertyDesc , required , defValue ) ); 
	}
	
	public static EntityVar metaIntegerDatabaseOnly( String propertyKey , String propertyDesc , boolean required , Integer defValue ) {
		return( metaIntegerVar( propertyKey , propertyKey , null , propertyDesc , required , defValue ) ); 
	}
	
	public static EntityVar metaIntegerXmlOnly( String propertyKey , String propertyDesc , boolean required , Integer defValue ) {
		return( metaIntegerVar( propertyKey , null , propertyKey , propertyDesc , required , defValue ) ); 
	}
	
	public static EntityVar metaIntegerVar( String propertyKey , String propertyDatabaseKey , String propertyXmlKey , String propertyDesc , boolean required , Integer defValue ) {
		String value = ( defValue == null )? null : "" + defValue;
		return( meta( propertyKey , propertyDatabaseKey , propertyXmlKey , propertyDesc , DBEnumParamValueType.NUMBER , DBEnumParamValueSubtype.DEFAULT , DBEnumObjectType.UNKNOWN , required , value , null ) );
	}
	
	public static EntityVar metaPathAbsolute( String propertyKey , String propertyDesc , boolean required , String defValue , DBEnumOSType ostype ) {
		return( metaPathAbsoluteVar( propertyKey , propertyKey , propertyKey , propertyDesc , required , defValue , ostype ) ); 
	}
	
	public static EntityVar metaPathAbsoluteDatabaseOnly( String propertyKey , String propertyDesc , boolean required , String defValue , DBEnumOSType ostype ) {
		return( metaPathAbsoluteVar( propertyKey , propertyKey , null , propertyDesc , required , defValue , ostype ) ); 
	}
	
	public static EntityVar metaPathAbsoluteXmlOnly( String propertyKey , String propertyDesc , boolean required , String defValue , DBEnumOSType ostype ) {
		return( metaPathAbsoluteVar( propertyKey , null , propertyKey , propertyDesc , required , defValue , ostype ) ); 
	}
	
	public static EntityVar metaPathAbsoluteVar( String propertyKey , String propertyDatabaseKey , String propertyXmlKey , String propertyDesc , boolean required , String defValue , DBEnumOSType ostype ) {
		DBEnumParamValueSubtype subtype = DBEnumParamValueSubtype.PATHABSOLUTE;
		if( ostype.isLinux() )
			subtype = DBEnumParamValueSubtype.PATHABSOLUTELINUX;
		else
		if( ostype.isWindows() )
			subtype = DBEnumParamValueSubtype.PATHABSOLUTEWINDOWS;
		return( meta( propertyKey , propertyDatabaseKey , propertyXmlKey , propertyDesc , DBEnumParamValueType.PATH , subtype , DBEnumObjectType.UNKNOWN , required , defValue , null ) );
	}
	
	public static EntityVar metaPathRelative( String propertyKey , String propertyDesc , boolean required , String defValue , DBEnumOSType ostype ) {
		return( metaPathRelativeVar( propertyKey , propertyKey , propertyKey , propertyDesc , required , defValue , ostype ) ); 
	}
	
	public static EntityVar metaPathRelativeDatabaseOnly( String propertyKey , String propertyDesc , boolean required , String defValue , DBEnumOSType ostype ) {
		return( metaPathRelativeVar( propertyKey , propertyKey , null , propertyDesc , required , defValue , ostype ) ); 
	}
	
	public static EntityVar metaPathRelativeXmlOnly( String propertyKey , String propertyDesc , boolean required , String defValue , DBEnumOSType ostype ) {
		return( metaPathRelativeVar( propertyKey , null , propertyKey , propertyDesc , required , defValue , ostype ) ); 
	}
	
	public static EntityVar metaPathRelativeVar( String propertyKey , String propertyDatabaseKey , String propertyXmlKey , String propertyDesc , boolean required , String defValue , DBEnumOSType ostype ) {
		DBEnumParamValueSubtype subtype = DBEnumParamValueSubtype.PATHRELATIVE;
		if( ostype.isLinux() )
			subtype = DBEnumParamValueSubtype.PATHRELATIVELINUX;
		else
		if( ostype.isWindows() )
			subtype = DBEnumParamValueSubtype.PATHRELATIVEWINDOWS;
		return( meta( propertyKey , propertyDatabaseKey , propertyXmlKey , propertyDesc , DBEnumParamValueType.PATH , subtype , DBEnumObjectType.UNKNOWN , required , defValue , null ) );
	}
	
	public static EntityVar metaBoolean( String propertyKey , String propertyDesc , boolean required , boolean defValue ) {
		return( metaBooleanVar( propertyKey , propertyKey , propertyKey , propertyDesc , required , defValue ) ); 
	}
	
	public static EntityVar metaBooleanDatabaseOnly( String propertyKey , String propertyDesc , boolean required , boolean defValue ) {
		return( metaBooleanVar( propertyKey , propertyKey , null , propertyDesc , required , defValue ) ); 
	}
	
	public static EntityVar metaBooleanXmlOnly( String propertyKey , String propertyDesc , boolean required , boolean defValue ) {
		return( metaBooleanVar( propertyKey , null , propertyKey , propertyDesc , required , defValue ) ); 
	}
	
	public static EntityVar metaBooleanVar( String propertyKey , String propertyDatabaseKey , String propertyXmlKey , String propertyDesc , boolean required , boolean defValue ) {
		return( meta( propertyKey , propertyDatabaseKey , propertyXmlKey , propertyDesc , DBEnumParamValueType.BOOL , DBEnumParamValueSubtype.DEFAULT , DBEnumObjectType.UNKNOWN , required , Common.getBooleanValue( defValue ) , null ) );
	}
	
	public static EntityVar metaObject( String propertyKey , String propertyDesc , DBEnumObjectType objectType , boolean required ) {
		return( metaObjectVar( propertyKey , propertyKey , propertyKey , propertyDesc , objectType , required ) ); 
	}
	
	public static EntityVar metaObjectDatabaseOnly( String propertyKey , String propertyDesc , DBEnumObjectType objectType , boolean required ) {
		return( metaObjectVar( propertyKey , propertyKey , null , propertyDesc , objectType , required ) ); 
	}
	
	public static EntityVar metaObjectXmlOnly( String propertyKey , String propertyDesc , DBEnumObjectType objectType , boolean required ) {
		return( metaObjectVar( propertyKey , null , propertyKey , propertyDesc , objectType , required ) ); 
	}
	
	public static EntityVar metaObjectVar( String propertyKey , String propertyDatabaseKey , String propertyXmlKey , String propertyDesc , DBEnumObjectType objectType , boolean required ) {
		return( meta( propertyKey , propertyDatabaseKey , propertyXmlKey , propertyDesc , DBEnumParamValueType.NUMBER , DBEnumParamValueSubtype.DEFAULT , objectType , required , null , null ) );
	}
	
	public static EntityVar meta( String propertyKey , String propertyDatabaseKey , String propertyXmlKey , String propertyDesc , DBEnumParamValueType type , DBEnumParamValueSubtype subtype , DBEnumObjectType objectType , boolean required , String defValue , Class<?> enumClass ) {
		EntityVar var = new EntityVar();
		var.NAME = propertyKey;
		var.DBNAME = Common.nonull( propertyDatabaseKey );
		var.XMLNAME = Common.nonull( propertyXmlKey );
		var.DESC = Common.nonull( propertyDesc );
		var.PARAMVALUE_TYPE = type;
		var.PARAMVALUE_SUBTYPE = subtype;
		var.OBJECT_TYPE = objectType;
		var.REQUIRED = required;
		var.EXPR_DEF = Common.nonull( defValue );
		var.enumClass = enumClass;
		return( var );
	}

	public void setEntity( PropertyEntity entity , int entityColumn , int databaseColumn ) {
		this.entity = entity;
		this.ENTITYCOLUMN = entityColumn;
		this.databaseColumn = databaseColumn;
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

	public boolean isString() {
		if( PARAMVALUE_TYPE == DBEnumParamValueType.STRING || PARAMVALUE_TYPE == DBEnumParamValueType.PATH )
			return( true );
		return( false );
	}
	
	public boolean isNumber() {
		if( enumClass == null && PARAMVALUE_TYPE == DBEnumParamValueType.NUMBER )
			return( true );
		return( false );
	}
	
	public boolean isBoolean() {
		if( PARAMVALUE_TYPE == DBEnumParamValueType.BOOL )
			return( true );
		return( false );
	}

	public boolean isPath() {
		if( PARAMVALUE_TYPE == DBEnumParamValueType.PATH )
			return( true );
		return( false );
	}

	public boolean isEnum() {
		if( enumClass != null )
			return( true );
		return( false );
	}

	public boolean isObject() {
		if( OBJECT_TYPE != DBEnumObjectType.UNKNOWN )
			return( true );
		return( false );
	}
	
	public boolean isDatabaseOnly() {
		if( DBNAME != null && XMLNAME == null )
			return( true );
		return( false );
	}
	
	public boolean isDatabase() {
		if( DBNAME != null )
			return( true );
		return( false );
	}
	
	public boolean isXmlOnly() {
		if( XMLNAME != null && DBNAME == null )
			return( true );
		return( false );
	}

	public boolean isXml() {
		if( XMLNAME != null )
			return( true );
		return( false );
	}

	public boolean isLinuxPath() {
		if( PARAMVALUE_TYPE == DBEnumParamValueType.PATH ) {
			if( PARAMVALUE_SUBTYPE == DBEnumParamValueSubtype.PATHABSOLUTELINUX || PARAMVALUE_SUBTYPE == DBEnumParamValueSubtype.PATHRELATIVELINUX )
				return( true );
		}
		return( false );
	}
	
	public boolean isWindowsPath() {
		if( PARAMVALUE_TYPE == DBEnumParamValueType.PATH ) {
			if( PARAMVALUE_SUBTYPE == DBEnumParamValueSubtype.PATHABSOLUTEWINDOWS || PARAMVALUE_SUBTYPE == DBEnumParamValueSubtype.PATHRELATIVEWINDOWS )
				return( true );
		}
		return( false );
	}
	
	public Integer importxmlObjectValue( EngineLoader loader , String value ) throws Exception {
		if( value.isEmpty() )
			return( null );
		
		if( OBJECT_TYPE == DBEnumObjectType.RESOURCE ) {
			EngineResources resources = loader.getResources();
			AuthResource resource = resources.getResource( value );
			return( resource.ID );
		}
		
		Common.exitUnexpected();
		return( null );
	}

	public String exportxmlObjectValue( EngineLoader loader , Integer value ) throws Exception {
		if( value == null )
			return( null );
		
		if( OBJECT_TYPE == DBEnumObjectType.RESOURCE ) {
			EngineResources resources = loader.getResources();
			AuthResource resource = resources.getResource( value );
			return( resource.NAME );
		}
		
		Common.exitUnexpected();
		return( null );
	}
	
}
