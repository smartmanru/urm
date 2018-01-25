package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.dist.VersionInfo;
import org.urm.engine.storage.FileInfo;
import org.urm.meta.Types.*;

public class MetaDistrBinaryItem {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_DISTITEMTYPE = "type";
	public static String PROPERTY_DISTNAME = "distname";
	public static String PROPERTY_DEPLOYNAME = "deployname";
	public static String PROPERTY_EXT = "extension";
	public static String PROPERTY_DEPLOYVERSIONTYPE = "deployversion";
	public static String PROPERTY_ITEMORIGIN = "source";
	public static String PROPERTY_SRCITEM_NAME = "srcitem";
	public static String PROPERTY_SRCDISTITEM_NAME = "srcdistitem";
	public static String PROPERTY_SRCITEMPATH = "srcpath";
	public static String PROPERTY_ARCHIVEFILES = "files";
	public static String PROPERTY_ARCHIVEEXCLUDE = "exclude";
	public static String PROPERTY_WARCONTEXT = "context";
	public static String PROPERTY_CUSTOMGET = "custom_get";
	public static String PROPERTY_CUSTOMDEPLOY = "custom_deploy";
	
	public Meta meta;
	public MetaDistrDelivery delivery;
	
	public int ID;
	public String NAME;
	public String DESC;
	public DBEnumDistItemType DISTITEM_TYPE;
	public String BASENAME_DIST;
	public String BASENAME_DEPLOY;
	public String EXT;
	public DBEnumDeployVersionType DEPLOYVERSION_TYPE;
	public DBEnumItemOriginType ITEMORIGIN_TYPE;
	public Integer SRCITEM_ID;
	public Integer SRC_BINARY_ID;
	public String SRC_ITEMPATH; 
	public String ARCHIVE_FILES;
	public String ARCHIVE_EXCLUDE;
	public String WAR_STATICEXT;
	public String WAR_CONTEXT;
	public boolean CUSTOM_GET;
	public boolean CUSTOM_DEPLOY;
	public int PV;

	public MetaSourceProjectItem sourceProjectItem;
	public MetaDistrBinaryItem srcDistItem;
	
	public MetaDistrBinaryItem( Meta meta , MetaDistrDelivery delivery ) {
		this.meta = meta;
		this.delivery = delivery;
		ID = -1;
		PV = -1;
	}

	public MetaDistrBinaryItem copy( Meta rmeta , MetaDistrDelivery rdelivery ) throws Exception {
		MetaDistrBinaryItem r = new MetaDistrBinaryItem( rmeta , rdelivery );
		
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.DISTITEM_TYPE = DISTITEM_TYPE;
		r.BASENAME_DIST = BASENAME_DIST;
		r.BASENAME_DEPLOY = BASENAME_DEPLOY;
		r.EXT = EXT;
		r.DEPLOYVERSION_TYPE = DEPLOYVERSION_TYPE;
		r.ITEMORIGIN_TYPE = ITEMORIGIN_TYPE;
		r.SRCITEM_ID = SRCITEM_ID;
		r.SRC_BINARY_ID = SRC_BINARY_ID;
		r.SRC_ITEMPATH = SRC_ITEMPATH; 
		r.ARCHIVE_FILES = ARCHIVE_FILES;
		r.ARCHIVE_EXCLUDE = ARCHIVE_EXCLUDE;
		r.WAR_STATICEXT = WAR_STATICEXT;
		r.WAR_CONTEXT = WAR_CONTEXT;
		r.CUSTOM_GET = CUSTOM_GET;
		r.CUSTOM_DEPLOY = CUSTOM_DEPLOY;
		r.PV = PV;
		
		return( r );
	}
	
	public void resolveReferences() throws Exception {
		srcDistItem = null;
		if( ITEMORIGIN_TYPE == DBEnumItemOriginType.DERIVED ) {
			MetaDistr distr = meta.getDistr();
			if( SRC_BINARY_ID == null )
				Common.exitUnexpected();
			srcDistItem = distr.getBinaryItem( SRC_BINARY_ID );
		}
		else
		if( ITEMORIGIN_TYPE == DBEnumItemOriginType.BUILD ) {
			MetaSources sources = meta.getSources();
			if( SRCITEM_ID == null )
				Common.exitUnexpected();
			sourceProjectItem = sources.getProjectItem( SRCITEM_ID );
			sourceProjectItem.setDistItem( this );
		}
	}

	public void createBinaryItem( String name , String desc ) throws Exception {
		modifyBinaryItem( name , desc );
	}
	
	public void modifyBinaryItem( String name , String desc ) throws Exception {
		this.NAME = name;
		this.DESC = desc;
	}
	
	public void createBinaryItem( String name , String desc , DBEnumDistItemType itemType , String distName , String deployName , String ext , DBEnumDeployVersionType versionType , String staticExt , String warContext , String files , String exclude ) throws Exception {
		modifyBinaryItem( name , desc , itemType , distName , deployName , ext , versionType , staticExt , warContext , files , exclude );
	}
	
	public void modifyBinaryItem( String name , String desc , DBEnumDistItemType itemType , String distName , String deployName , String ext , DBEnumDeployVersionType versionType , String staticExt , String warContext , String files , String exclude ) throws Exception {
		this.NAME = name;
		this.DESC = desc;
		this.DISTITEM_TYPE = itemType;
		this.BASENAME_DIST = distName;
		this.BASENAME_DEPLOY = deployName;
		this.EXT = ext;
		this.WAR_STATICEXT = staticExt;
		this.WAR_CONTEXT = warContext;
		this.ARCHIVE_FILES = files;
		this.ARCHIVE_EXCLUDE = exclude;
	}
	
	public void setSource( DBEnumItemOriginType originType , Integer srcId , Integer srcBinaryId , String itemPath ) {
		this.ITEMORIGIN_TYPE = originType;
		this.SRCITEM_ID = srcId;
		this.SRC_BINARY_ID = srcBinaryId;
		this.SRC_ITEMPATH = itemPath; 
	}
	
	public void setCustom( boolean customGet , boolean customDeploy ) throws Exception {
		this.CUSTOM_GET = customGet; 
		this.CUSTOM_DEPLOY = customDeploy; 
	}
	
	public void setDelivery( MetaDistrDelivery deliveryNew ) throws Exception {
		this.delivery = deliveryNew; 
	}
	
	public void setDistData( DBEnumDistItemType itemType , String basename , String ext , String archiveFiles , String archiveExclude ) throws Exception {
		this.DISTITEM_TYPE = itemType;
		this.BASENAME_DIST = basename;
		this.EXT = ext;
		this.ARCHIVE_FILES = archiveFiles;
		this.ARCHIVE_EXCLUDE = archiveExclude;
		if( this.BASENAME_DIST.isEmpty() )
			this.BASENAME_DIST = NAME;
	}

	public void setDeployData( String deployname , DBEnumDeployVersionType versionType ) throws Exception {
		if( DISTITEM_TYPE == DBEnumDistItemType.BINARY && versionType == DBEnumDeployVersionType.UNKNOWN )
			Common.exitUnexpected();
		
		this.BASENAME_DEPLOY = deployname;
		this.DEPLOYVERSION_TYPE = versionType;
		if( this.BASENAME_DEPLOY.isEmpty() )
			this.BASENAME_DEPLOY = BASENAME_DIST;
	}

	public void setBuildOrigin( MetaSourceProjectItem sourceItem ) throws Exception {
		this.sourceProjectItem = sourceItem;
		this.srcDistItem = null;
		setSource( DBEnumItemOriginType.BUILD , sourceItem.ID , null , "" );
		sourceItem.setDistItem( this );
	}

	public void setDistOrigin( MetaDistrBinaryItem itemSrc , String srcPath ) throws Exception {
		this.sourceProjectItem = null;
		this.srcDistItem = itemSrc;
		setSource( DBEnumItemOriginType.DERIVED , null , itemSrc.ID , srcPath );
	}
	
	public void setManualOrigin() throws Exception {
		this.sourceProjectItem = null;
		this.srcDistItem = null;
		setSource( DBEnumItemOriginType.MANUAL , null , null , "" );
	}

	public void changeProjectToManual() throws Exception {
		if( ITEMORIGIN_TYPE != DBEnumItemOriginType.BUILD )
			Common.exitUnexpected();
			
		sourceProjectItem = null;
		SRCITEM_ID = null;
		ITEMORIGIN_TYPE = DBEnumItemOriginType.MANUAL;
	}
	
	public boolean isArchive() {
		if( DISTITEM_TYPE.isArchive() )
			return( true );
		return( false );
	}
	
	public String getBaseFile() {
		return( BASENAME_DIST + EXT );
	}

	public DBEnumDeployVersionType getVersionType( String deployBaseName , String fileName ) throws Exception {
		String baseName = ( deployBaseName.isEmpty() )? BASENAME_DEPLOY : deployBaseName;
		if( fileName.matches( baseName + EXT ) )
			return( DBEnumDeployVersionType.NONE );
		
		if( fileName.matches( ".*[0-9]-" + baseName + EXT ) )
			return( DBEnumDeployVersionType.PREFIX );
		
		if( fileName.matches( baseName + "-[0-9].*" + EXT ) )
			return( DBEnumDeployVersionType.MIDDASH );
		
		if( fileName.matches( baseName + "##[0-9].*" + EXT ) )
			return( DBEnumDeployVersionType.MIDPOUND );
		
		return( DBEnumDeployVersionType.UNKNOWN );
	}

	public FileInfo getFileInfo( String runtimeFile , String specificDeployName , String md5value ) throws Exception {
		DBEnumDeployVersionType vtype = getVersionType( specificDeployName , runtimeFile );
		if( vtype == DBEnumDeployVersionType.UNKNOWN )
			Common.exit2( _Error.UnableGetFileVersionType2 , "unable to get version type of file=" + runtimeFile + ", deployName=" + specificDeployName , runtimeFile , specificDeployName );
		
		String name = Common.getPartBeforeLast( runtimeFile , EXT );
				
		if( vtype == DBEnumDeployVersionType.NONE ) {
			String deployNameNoVersion = name;
			return( new FileInfo( this , null , md5value , deployNameNoVersion , runtimeFile ) );
		}
		
		if( vtype == DBEnumDeployVersionType.PREFIX ) {
			String fileVersion = Common.getPartBeforeFirst( name , "-" );
			VersionInfo version = VersionInfo.getFileVersion( fileVersion );
			String deployNameNoVersion = Common.getPartAfterFirst( name , "-" );
			return( new FileInfo( this , version , md5value , deployNameNoVersion , runtimeFile ) );
		}
		
		if( vtype == DBEnumDeployVersionType.MIDDASH ) {
			String fileVersion = Common.getPartAfterLast( name , "-" );
			VersionInfo version = VersionInfo.getFileVersion( fileVersion );
			String deployNameNoVersion = Common.getPartBeforeLast( name , "-" );
			return( new FileInfo( this , version , md5value , deployNameNoVersion , runtimeFile ) );
		}
		
		if( vtype == DBEnumDeployVersionType.MIDPOUND ) {
			String fileVersion = Common.getPartAfterLast( name , "##" );
			VersionInfo version = VersionInfo.getFileVersion( fileVersion );
			String deployNameNoVersion = Common.getPartBeforeLast( name , "##" );
			return( new FileInfo( this , version , md5value , deployNameNoVersion , runtimeFile ) );
		}
		
		Common.exitUnexpected();
		return( null );
	}

	public boolean isDerivedItem() {
		if( srcDistItem != null )
			return( true );
		return( false );
	}

	public boolean isManualItem() {
		if( ITEMORIGIN_TYPE == DBEnumItemOriginType.MANUAL )
			return( true );
		return( false );
	}
	
	public boolean isProjectItem() {
		if( ITEMORIGIN_TYPE == DBEnumItemOriginType.BUILD )
			return( true );
		return( false );
	}
	
	public VarARCHIVETYPE getArchiveType( ActionBase action ) throws Exception {
		if( EXT.equals( ".tar.gz" ) || EXT.equals( ".tgz" ) )
			return( VarARCHIVETYPE.TARGZ );
		
		if( EXT.equals( ".tar" ) )
			return( VarARCHIVETYPE.TAR );
		
		if( EXT.equals( ".zip" ) )
			return( VarARCHIVETYPE.ZIP );
		
		action.exit1( _Error.ArchiveTypeNotSupported1 , "not supported archive type=" + EXT , EXT );
		return( null );
	}
	
	public String getDeploySampleFile() {
		String value = BASENAME_DEPLOY;
		
		if( isArchive() )
			return( "(archive)" );
		
		if( DEPLOYVERSION_TYPE == DBEnumDeployVersionType.IGNORE ) {
			if( sourceProjectItem != null && sourceProjectItem.FIXED_VERSION.isEmpty() == false )
				value += "-" + sourceProjectItem.FIXED_VERSION;
		}
		else {
			String version = "1.0";
			if( sourceProjectItem != null && sourceProjectItem.FIXED_VERSION.isEmpty() == false )
				version = sourceProjectItem.FIXED_VERSION;
			
			if( DEPLOYVERSION_TYPE == DBEnumDeployVersionType.MIDDASH )
				value += "-" + version;
			else
			if( DEPLOYVERSION_TYPE == DBEnumDeployVersionType.MIDPOUND )
				value += "##" + version;
			else
			if( DEPLOYVERSION_TYPE == DBEnumDeployVersionType.PREFIX )
				value = version + "-" + value;
		}
		
		value += EXT;
		return( value );
	}
	
	public String[] getVersionPatterns() throws Exception {
		String basename = BASENAME_DIST;
		String ext = EXT;
		
		if( DEPLOYVERSION_TYPE == DBEnumDeployVersionType.IGNORE ) {
			String[] values = new String[1];
			values[0] = DEPLOYVERSION_TYPE.getVersionPattern( basename , ext );
			return( values );
		}

		String[] values = new String[4];
		values[0] = DBEnumDeployVersionType.NONE.getVersionPattern( basename , ext );
		values[1] = DBEnumDeployVersionType.MIDPOUND.getVersionPattern( basename , ext );
		values[2] = DBEnumDeployVersionType.MIDDASH.getVersionPattern( basename , ext );
		values[3] = DBEnumDeployVersionType.PREFIX.getVersionPattern( basename , ext );
		return( values );
	}

}
