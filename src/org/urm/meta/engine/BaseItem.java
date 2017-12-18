package org.urm.meta.engine;

import org.urm.engine.properties.ObjectProperties;
import org.urm.meta.EngineObject;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;

public class BaseItem extends EngineObject {

	public static String PROPERTY_NAME = "name";
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
	
	public ObjectProperties p;
	
	public BaseItem( BaseGroup group , ObjectProperties p ) {
		super( group );
		this.group = group;
		this.p = p;
		ID = -1;
		CV = 0;
	}

	@Override
	public String getName() {
		return( NAME );
	}
	
	public void scatterProperties() throws Exception {
		NAME = p.getPropertyValue( BaseItem.PROPERTY_NAME );
		DESC = p.getPropertyValue( BaseItem.PROPERTY_DESC );
		BASESRC_TYPE = DBEnumBaseSrcType.getValue( p.getIntProperty( BaseItem.PROPERTY_BASESRC_TYPE ) , false );
		BASESRCFORMAT_TYPE = DBEnumBaseSrcFormatType.getValue( p.getIntProperty( BaseItem.PROPERTY_BASESRCFORMAT_TYPE ) , false );
		OS_TYPE = DBEnumOSType.getValue( p.getIntProperty( BaseItem.PROPERTY_OS_TYPE ) , false );
		SERVERACCESS_TYPE = DBEnumServerAccessType.getValue( p.getIntProperty( BaseItem.PROPERTY_SERVERACCESS_TYPE ) , false );
		BASENAME = p.getPropertyValue( BaseItem.PROPERTY_BASENAME );
		BASEVERSION = p.getPropertyValue( BaseItem.PROPERTY_BASEVERSION );
		SRCDIR = p.getPropertyValue( BaseItem.PROPERTY_SRCDIR );
		SRCFILE = p.getPropertyValue( BaseItem.PROPERTY_SRCFILE );
		SRCFILEDIR = p.getPropertyValue( BaseItem.PROPERTY_SRCFILEDIR );
		INSTALLSCRIPT = p.getPropertyValue( BaseItem.PROPERTY_INSTALLSCRIPT );
		INSTALLPATH = p.getPropertyValue( BaseItem.PROPERTY_INSTALLPATH );
		INSTALLLINK = p.getPropertyValue( BaseItem.PROPERTY_INSTALLLINK );
		CHARSET = p.getPropertyValue( BaseItem.PROPERTY_CHARSET );
	}
	
	public void createBaseItem( String name , String desc ) throws Exception {
		modifyBaseItem( name , desc );
		setOffline( true );
	}
	
	public void modifyBaseItem( String name , String desc ) throws Exception {
		this.NAME = name;
		this.DESC = Common.nonull( desc );
		p.setStringProperty( PROPERTY_NAME , NAME );
		p.setStringProperty( PROPERTY_DESC , DESC );
	}
	
	public void setOffline( boolean offline ) throws Exception {
		this.OFFLINE = offline;
		p.setBooleanProperty( PROPERTY_OFFLINE , OFFLINE );
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
		if( group.category.BASECATEGORY_TYPE == DBEnumBaseCategoryType.HOST )
			return( true );
		return( false );
	}
	
	public boolean isAccountBound() {
		if( group.category.BASECATEGORY_TYPE == DBEnumBaseCategoryType.ACCOUNT )
			return( true );
		return( false );
	}
	
	public boolean isAppBound() {
		if( group.category.BASECATEGORY_TYPE == DBEnumBaseCategoryType.APP )
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
	
	public boolean isNoDist() {
		if( BASESRC_TYPE == DBEnumBaseSrcType.NODIST )
			return( true );
		return( false );
	}
	
	public boolean isInstaller() {
		if( BASESRC_TYPE == DBEnumBaseSrcType.INSTALLER )
			return( true );
		return( false );
	}
	
	public boolean isArchiveLink() {
		if( BASESRC_TYPE == DBEnumBaseSrcType.ARCHIVE_LINK )
			return( true );
		return( false );
	}
	
	public boolean isPackage() {
		if( BASESRC_TYPE == DBEnumBaseSrcType.PACKAGE )
			return( true );
		return( false );
	}
	
	public boolean isArchiveDirect() {
		if( BASESRC_TYPE == DBEnumBaseSrcType.ARCHIVE_DIRECT )
			return( true );
		return( false );
	}

	public boolean isArchive() {
		if( isArchiveLink() ||
			isArchiveDirect() )
			return( true );
		return( false );
	}
	
	public void modifyData( String name , String version , DBEnumOSType ostype , DBEnumServerAccessType accessType , DBEnumBaseSrcType srcType , DBEnumBaseSrcFormatType srcFormat , String SRCFILE , String SRCFILEDIR , String INSTALLPATH , String INSTALLLINK ) throws Exception {
		this.BASENAME = Common.nonull( name );
		this.BASEVERSION = Common.nonull( version );
		
		this.OS_TYPE = ostype;
		this.SERVERACCESS_TYPE = accessType;
		this.BASESRC_TYPE = srcType;
		this.BASESRCFORMAT_TYPE = srcFormat;
		
		this.SRCFILE = Common.nonull( SRCFILE );
		this.SRCFILEDIR = Common.nonull( SRCFILEDIR );
		this.INSTALLPATH = Common.nonull( INSTALLPATH );
		this.INSTALLLINK = Common.nonull( INSTALLLINK );
		
		p.setEnumProperty( BaseItem.PROPERTY_BASESRC_TYPE , BASESRC_TYPE );
		p.setEnumProperty( BaseItem.PROPERTY_BASESRCFORMAT_TYPE , BASESRCFORMAT_TYPE );
		p.setEnumProperty( BaseItem.PROPERTY_OS_TYPE , OS_TYPE );
		p.setEnumProperty( BaseItem.PROPERTY_SERVERACCESS_TYPE , SERVERACCESS_TYPE );
		p.setProperty( BaseItem.PROPERTY_BASENAME , BASENAME );
		p.setProperty( BaseItem.PROPERTY_BASEVERSION , BASEVERSION );
		p.setProperty( BaseItem.PROPERTY_SRCDIR , SRCDIR );
		p.setProperty( BaseItem.PROPERTY_SRCFILE , SRCFILE );
		p.setProperty( BaseItem.PROPERTY_SRCFILEDIR , SRCFILEDIR );
		p.setProperty( BaseItem.PROPERTY_INSTALLSCRIPT , INSTALLSCRIPT );
		p.setProperty( BaseItem.PROPERTY_INSTALLPATH , INSTALLPATH );
		p.setProperty( BaseItem.PROPERTY_INSTALLLINK , INSTALLLINK );
		p.setProperty( BaseItem.PROPERTY_CHARSET , CHARSET );
	}
	
}
