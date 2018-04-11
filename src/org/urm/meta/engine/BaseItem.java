package org.urm.meta.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.engine.data.EngineBase;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertySet;
import org.urm.meta.EngineObject;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;

public class BaseItem extends EngineObject {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_ADMIN = "admin";
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
	public boolean ADMIN;
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
	
	public ObjectProperties ops;
	
	List<String> depsDraft;
	Map<Integer,ObjectProperties> depsById;
	
	public BaseItem( BaseGroup group , ObjectProperties ops ) {
		super( group );
		this.group = group;
		this.ops = ops;
		ID = -1;
		CV = 0;
		
		depsDraft = new LinkedList<String>();
		depsById = new HashMap<Integer,ObjectProperties>(); 
	}

	@Override
	public String getName() {
		return( NAME );
	}
	
	public void scatterProperties() throws Exception {
		NAME = ops.getPropertyValue( BaseItem.PROPERTY_NAME );
		DESC = ops.getPropertyValue( BaseItem.PROPERTY_DESC );
		ADMIN = ops.getBooleanProperty( BaseItem.PROPERTY_ADMIN );
		BASESRC_TYPE = DBEnumBaseSrcType.getValue( ops.getIntProperty( BaseItem.PROPERTY_BASESRC_TYPE ) , false );
		BASESRCFORMAT_TYPE = DBEnumBaseSrcFormatType.getValue( ops.getIntProperty( BaseItem.PROPERTY_BASESRCFORMAT_TYPE ) , false );
		OS_TYPE = DBEnumOSType.getValue( ops.getIntProperty( BaseItem.PROPERTY_OS_TYPE ) , false );
		SERVERACCESS_TYPE = DBEnumServerAccessType.getValue( ops.getIntProperty( BaseItem.PROPERTY_SERVERACCESS_TYPE ) , false );
		BASENAME = ops.getPropertyValue( BaseItem.PROPERTY_BASENAME );
		BASEVERSION = ops.getPropertyValue( BaseItem.PROPERTY_BASEVERSION );
		SRCDIR = ops.getPropertyValue( BaseItem.PROPERTY_SRCDIR );
		SRCFILE = ops.getPropertyValue( BaseItem.PROPERTY_SRCFILE );
		SRCFILEDIR = ops.getPropertyValue( BaseItem.PROPERTY_SRCFILEDIR );
		INSTALLSCRIPT = ops.getPropertyValue( BaseItem.PROPERTY_INSTALLSCRIPT );
		INSTALLPATH = ops.getPropertyValue( BaseItem.PROPERTY_INSTALLPATH );
		INSTALLLINK = ops.getPropertyValue( BaseItem.PROPERTY_INSTALLLINK );
		CHARSET = ops.getPropertyValue( BaseItem.PROPERTY_CHARSET );
	}
	
	public ObjectProperties getParameters() {
		return( ops );
	}
	
	public void createBaseItem( String name , String desc ) throws Exception {
		modifyBaseItem( name , desc );
		setOffline( true );
	}
	
	public void modifyBaseItem( String name , String desc ) throws Exception {
		this.NAME = name;
		this.DESC = Common.nonull( desc );
		ops.setStringProperty( PROPERTY_NAME , NAME );
		ops.setStringProperty( PROPERTY_DESC , DESC );
	}
	
	public void setOffline( boolean offline ) throws Exception {
		this.OFFLINE = offline;
		ops.setBooleanProperty( PROPERTY_OFFLINE , OFFLINE );
	}
	
	public BaseItem copy( BaseGroup rgroup , EngineEntities entities , ObjectProperties rparameters ) throws Exception {
		BaseItem r = new BaseItem( rgroup , rparameters );
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.OFFLINE = OFFLINE;
		r.CV = CV;
		
		EngineBase rbase = rgroup.category.base;
		for( int depId : depsById.keySet() ) {
			BaseItem rdep = rbase.getItem( depId );
			r.addDepItem( entities , rdep );
		}
		
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

	public boolean isValidImplementation() {
		if( SRCDIR.isEmpty() ||
			NAME.isEmpty() || 
			BASESRC_TYPE == DBEnumBaseSrcType.UNKNOWN || 
			BASESRCFORMAT_TYPE == DBEnumBaseSrcFormatType.UNKNOWN ||
			OS_TYPE == DBEnumOSType.UNKNOWN ||
			SERVERACCESS_TYPE == DBEnumServerAccessType.UNKNOWN )
			return( false );
		if( isAccountBound() && ADMIN )
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
	
	public void modifyData( boolean admin , String name , String version , DBEnumOSType ostype , DBEnumServerAccessType accessType , DBEnumBaseSrcType srcType , DBEnumBaseSrcFormatType srcFormat , String SRCDIR , String SRCFILE , String SRCFILEDIR , String INSTALLSCRIPT , String INSTALLPATH , String INSTALLLINK ) throws Exception {
		this.ADMIN = admin;
		this.BASENAME = Common.nonull( name );
		this.BASEVERSION = Common.nonull( version );
		
		this.OS_TYPE = ostype;
		this.SERVERACCESS_TYPE = accessType;
		this.BASESRC_TYPE = srcType;
		this.BASESRCFORMAT_TYPE = srcFormat;
		
		this.SRCDIR = Common.nonull( SRCDIR );
		this.SRCFILE = Common.nonull( SRCFILE );
		this.SRCFILEDIR = Common.nonull( SRCFILEDIR );
		
		this.INSTALLSCRIPT = Common.nonull( INSTALLSCRIPT );
		this.INSTALLPATH = Common.nonull( INSTALLPATH );
		this.INSTALLLINK = Common.nonull( INSTALLLINK );
		
		ops.setBooleanProperty( BaseItem.PROPERTY_ADMIN , ADMIN );
		ops.setEnumProperty( BaseItem.PROPERTY_BASESRC_TYPE , BASESRC_TYPE );
		ops.setEnumProperty( BaseItem.PROPERTY_BASESRCFORMAT_TYPE , BASESRCFORMAT_TYPE );
		ops.setEnumProperty( BaseItem.PROPERTY_OS_TYPE , OS_TYPE );
		ops.setEnumProperty( BaseItem.PROPERTY_SERVERACCESS_TYPE , SERVERACCESS_TYPE );
		ops.setProperty( BaseItem.PROPERTY_BASENAME , BASENAME );
		ops.setProperty( BaseItem.PROPERTY_BASEVERSION , BASEVERSION );
		ops.setProperty( BaseItem.PROPERTY_SRCDIR , SRCDIR );
		ops.setProperty( BaseItem.PROPERTY_SRCFILE , SRCFILE );
		ops.setProperty( BaseItem.PROPERTY_SRCFILEDIR , SRCFILEDIR );
		ops.setProperty( BaseItem.PROPERTY_INSTALLSCRIPT , INSTALLSCRIPT );
		ops.setProperty( BaseItem.PROPERTY_INSTALLPATH , INSTALLPATH );
		ops.setProperty( BaseItem.PROPERTY_INSTALLLINK , INSTALLLINK );
		ops.setProperty( BaseItem.PROPERTY_CHARSET , CHARSET );
	}
	
	public Integer[] getDepItemIds() {
		return( depsById.keySet().toArray( new Integer[0] ) );
	}

	public ObjectProperties getDependencySettings( int depId ) throws Exception {
		ObjectProperties ops = depsById.get( depId );
		if( ops == null )
			Common.exit1( _Error.UnknownDependencyBaseItem1 , "Unknown dependency base item=" + depId , "" + depId );
		return( ops );
	}
	
	public String[] getDepItemNames() {
		EngineBase base = group.category.base;
		Map<String,BaseItem> map = new HashMap<String,BaseItem>();
		
		for( int itemId : depsById.keySet() ) {
			BaseItem item = base.findItem( itemId );
			map.put( item.NAME , item );
		}
		return( Common.getSortedKeys( map ) );
	}

	public BaseItem[] getDepItems() {
		return( depsById.values().toArray( new BaseItem[0] ) );
	}

	public String[] getDepItemDraftNames() {
		return( depsDraft.toArray( new String[0] ) );
	}

	public void addDepItem( EngineEntities entities , BaseItem dep ) throws Exception {
		ObjectProperties ops = entities.createBaseItemDependencyProps( this , dep );
		depsById.put( dep.ID , ops );
	}
	
	public void deleteDepItem( BaseItem dep ) {
		depsById.remove( dep.ID );
	}
	
	public void clearDrafts() {
		depsDraft.clear();
	}
	
	public BaseItem findDepItem( String depName ) {
		EngineBase base = group.category.base;
		for( int itemId : depsById.keySet() ) {
			BaseItem item = base.findItem( itemId );
			if( depName.equals( item.NAME ) )
				return( item );
		}
		return( null );
	}
	
	public void addDepDraft( String depName ) {
		depsDraft.add( depName );
	}
	
	public boolean checkDependencyItem( String depName ) {
		if( findDepItem( depName ) != null )
			return( true );
		return( false );
	}

	public boolean isValidConfiguration() {
		PropertySet set = ops.getProperties();
		if( !set.isCorrect() )
			return( false );
		return( true );
	}
	
	public boolean areValidDependencies() {
		EngineBase base = group.category.base;
		for( int itemId : depsById.keySet() ) {
			BaseItem item = base.findItem( itemId );
			if( item.OFFLINE )
				return( false );
		}
		return( true );
	}
	
	public boolean isValid() {
		if( isValidImplementation() && isValidConfiguration() && areValidDependencies() )
			return( true );
		return( false );
	}
	
}
