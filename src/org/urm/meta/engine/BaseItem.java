package org.urm.meta.engine;

import org.urm.engine.properties.ObjectProperties;
import org.urm.meta.EngineObject;
import org.urm.db.core.DBEnums.*;

public class BaseItem extends EngineObject {

	public static String PROPERTY_NAME = "id";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_BASESRC_TYPE = "basesrc_type";
	public static String PROPERTY_BASESRCFORMAT_TYPE = "basesrcformat_type";
	public static String PROPERTY_OS_TYPE = "os_type";
	public static String PROPERTY_SERVERACCESS_TYPE = "serveraccess_type";
	public static String PROPERTY_BASENAME = "basename";
	public static String PROPERTY_BASEVERSION = "baseversion";
	public static String PROPERTY_SRCDIR = "srcdir";
	public static String PROPERTY_SRCFILE = "srcfile";
	public static String PROPERTY_SRCFILEDIR = "srcfiledir";
	public static String PROPERTY_INSTALLSCRIPT = "installscript";
	public static String PROPERTY_INSTALLPATH = "installpath";
	public static String PROPERTY_INSTALLLINK = "installlink";
	public static String PROPERTY_CHARSET = "charset";
	public static String PROPERTY_OFFLINE = "offline";
	
	public BaseGroup group;
	public int ID;
	public String NAME;
	public String DESC;
	public DBEnumBaseSrcType BASESRC_TYPE;
	public DBEnumBaseSrcFormatType BASESRCFORMAT_TYPE;
	public DBEnumOSType OS_TYPE;
	public DBEnumServerAccessType SERVERACCESS_TYPE;
	public String BASENAME;
	public String BASEVERSION;
	public String SRCDIR;
	public String SRCFILE;
	public String SRCFILEDIR;
	public String INSTALLSCRIPT;
	public String INSTALLPATH;
	public String INSTALLLINK;
	public String CHARSET;
	public boolean OFFLINE;
	public int CV;
	
	public ObjectProperties parameters;
	
	public BaseItem( BaseGroup group , ObjectProperties parameters ) {
		super( group );
		this.group = group;
		this.parameters = parameters;
		ID = -1;
		CV = 0;
	}

	@Override
	public String getName() {
		return( NAME );
	}
	
	public void createBaseItem( String name , String desc ) throws Exception {
		OFFLINE = false;
		modifyBaseItem( name , desc );
	}
	
	public void modifyBaseItem( String name , String desc ) throws Exception {
		this.NAME = name;
		this.DESC = desc;
		parameters.setStringProperty( PROPERTY_NAME , NAME );
		parameters.setStringProperty( PROPERTY_DESC , DESC );
		parameters.setBooleanProperty( PROPERTY_OFFLINE , OFFLINE );
	}
	
	public BaseItem copy( BaseGroup rgroup , ObjectProperties rparameters ) {
		BaseItem r = new BaseItem( rgroup , rparameters );
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.OFFLINE = OFFLINE;
		r.CV = CV;
		return( r );
	}

	public boolean isHostBound() {
		if( group.category.TYPE == DBEnumBaseCategoryType.HOST )
			return( true );
		return( false );
	}
	
	public boolean isAccountBound() {
		if( group.category.TYPE == DBEnumBaseCategoryType.ACCOUNT )
			return( true );
		return( false );
	}
	
	public boolean isAppBound() {
		if( group.category.TYPE == DBEnumBaseCategoryType.APP )
			return( true );
		return( false );
	}

	public boolean isValid() {
		if( NAME.isEmpty() || 
			BASESRC_TYPE == DBEnumBaseSrcType.UNKNOWN || 
			BASESRCFORMAT_TYPE == DBEnumBaseSrcFormatType.UNKNOWN ||
			OS_TYPE == DBEnumOSType.UNKNOWN ||
			SERVERACCESS_TYPE == DBEnumServerAccessType.UNKNOWN )
			return( false );
		return( true );
	}
	
}
