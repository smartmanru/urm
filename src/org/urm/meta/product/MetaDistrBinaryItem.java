package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.EngineTransaction;
import org.urm.engine.custom.CommandCustom;
import org.urm.engine.dist.VersionInfo;
import org.urm.engine.storage.FileInfo;
import org.urm.meta.Types;
import org.urm.meta.Types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaDistrBinaryItem {

	public Meta meta;
	public MetaDistrDelivery delivery;
	
	public String KEY;
	public String EXT;
	public VarDISTITEMTYPE distItemType;
	public VarDISTITEMORIGIN distItemOrigin;
	public String SRCPROJECTITEM;
	public MetaSourceProjectItem sourceProjectItem;
	public String SRCDISTITEM;
	public MetaDistrBinaryItem srcDistItem;
	public String SRCITEMPATH; 
	public String DISTBASENAME;
	public String DEPLOYBASENAME;
	public VarITEMVERSION deployVersion;
	public String FILES;
	public String EXCLUDE;
	
	public String WAR_MRID;
	public String WAR_CONTEXT;
	public String WAR_STATICEXT;
	public String BUILDINFO;
	
	public boolean CUSTOMDEPLOY;
	
	public MetaDistrBinaryItem( Meta meta , MetaDistrDelivery delivery ) {
		this.meta = meta;
		this.delivery = delivery; 
	}

	public void createBinaryItem( EngineTransaction transaction , String key ) throws Exception {
		this.KEY = key;
		EXT = "";
		SRCPROJECTITEM = "";
		SRCDISTITEM = "";
		SRCITEMPATH = "";
		DISTBASENAME = "";
		DEPLOYBASENAME = "";
		FILES = "";
		EXCLUDE = "";
		
		WAR_MRID = "";
		WAR_CONTEXT = "";
		WAR_STATICEXT = "";
		BUILDINFO = "";
		CUSTOMDEPLOY = false;
	}
	
	public void setDelivery( EngineTransaction transaction , MetaDistrDelivery deliveryNew ) throws Exception {
		this.delivery = deliveryNew; 
	}
	
	public void changeProjectToManual( EngineTransaction transaction ) throws Exception {
		if( distItemOrigin != VarDISTITEMORIGIN.BUILD )
			transaction.exitUnexpectedState();
			
		sourceProjectItem = null;
		SRCPROJECTITEM = "";
		distItemOrigin = VarDISTITEMORIGIN.MANUAL;
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		KEY = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOTDASH );
	
		// read attrs
		distItemType = Types.getItemDistType( ConfReader.getRequiredAttrValue( node , "type" ) , false );
		distItemOrigin = Types.getItemDistOrigin( ConfReader.getRequiredAttrValue( node , "source" ) , false );
		if( distItemOrigin == VarDISTITEMORIGIN.BUILD )
			SRCPROJECTITEM = ConfReader.getAttrValue( node , "srcitem" );
		
		if( distItemOrigin == VarDISTITEMORIGIN.DERIVED ) {
			SRCDISTITEM = ConfReader.getAttrValue( node , "srcitem" );
			SRCITEMPATH = ConfReader.getAttrValue( node , "srcpath" );
		}
		
		DISTBASENAME = ConfReader.getAttrValue( node , "distname" , KEY );
		DEPLOYBASENAME = ConfReader.getAttrValue( node , "deployname" , DISTBASENAME );
		deployVersion = Types.readItemVersionAttr( node , "deployversion" );
		BUILDINFO = ConfReader.getAttrValue( node , "buildinfo" );

		// binary item
		if( distItemType == VarDISTITEMTYPE.BINARY ||
			distItemType == VarDISTITEMTYPE.PACKAGE ) {
			EXT = ConfReader.getRequiredAttrValue( node , "extension" );
		}
		else
		// war item and static
		if( distItemType == VarDISTITEMTYPE.STATICWAR ) {
			EXT = ".war";
	
			WAR_MRID = ConfReader.getAttrValue( node , "mrid" );
			WAR_CONTEXT = ConfReader.getAttrValue( node , "context" , DEPLOYBASENAME );
			WAR_STATICEXT = ConfReader.getAttrValue( node , "extension" , "-webstatic.tar.gz" );
		}
		else
		// archive item
		if( isArchive() ) {
			EXT = ConfReader.getAttrValue( node , "extension" , ".tar.gz" );
			FILES = ConfReader.getAttrValue( node , "files" , "*" );
			EXCLUDE = ConfReader.getAttrValue( node , "exclude" );
		}
		else
		// nupkg item
		if( distItemType == VarDISTITEMTYPE.PACKAGE ) {
			EXT = ConfReader.getRequiredAttrValue( node , "extension" );
		}
		else {
			String distType = Common.getEnumLower( distItemType );
			action.exit2( _Error.UnknownDistributiveItemType2 , "distribution item " + KEY + " has unknown type=" + distType , KEY , distType );
		}
		
		CUSTOMDEPLOY = ConfReader.getBooleanAttrValue( node , "customdeploy" , false );
		if( CUSTOMDEPLOY ) {
			CommandCustom custom = new CommandCustom( meta );
			custom.parseDistItem( action , this , node );
		}
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , KEY );
		
		// read attrs
		Common.xmlSetElementAttr( doc , root , "type" , Common.getEnumLower( distItemType ) );
		Common.xmlSetElementAttr( doc , root , "source" , Common.getEnumLower( distItemOrigin ) );
		
		if( distItemOrigin == VarDISTITEMORIGIN.BUILD )
			Common.xmlSetElementAttr( doc , root , "srcitem" , SRCPROJECTITEM );
		
		if( distItemOrigin == VarDISTITEMORIGIN.DERIVED ) {
			Common.xmlSetElementAttr( doc , root , "srcitem" , SRCDISTITEM );
			Common.xmlSetElementAttr( doc , root , "srcpath" , SRCITEMPATH );
		}
		
		Common.xmlSetElementAttr( doc , root , "distname" , DISTBASENAME );
		Common.xmlSetElementAttr( doc , root , "deployname" , DEPLOYBASENAME );
		Common.xmlSetElementAttr( doc , root , "deployversion" , Common.getEnumLower( deployVersion ) );
		Common.xmlSetElementAttr( doc , root , "buildinfo" , BUILDINFO );

		// binary item
		if( distItemType == VarDISTITEMTYPE.BINARY || 
			distItemType == VarDISTITEMTYPE.PACKAGE ) {
			Common.xmlSetElementAttr( doc , root , "extension" , EXT );
		}
		else
		// war item and static
		if( distItemType == VarDISTITEMTYPE.STATICWAR ) {
			EXT = ".war";
	
			Common.xmlSetElementAttr( doc , root , "mrid" , WAR_MRID );
			Common.xmlSetElementAttr( doc , root , "context" , WAR_CONTEXT );
			Common.xmlSetElementAttr( doc , root , "extension" , WAR_STATICEXT );
		}
		else
		// archive item
		if( isArchive() ) {
			Common.xmlSetElementAttr( doc , root , "extension" , EXT );
			Common.xmlSetElementAttr( doc , root , "files" , FILES );
			Common.xmlSetElementAttr( doc , root , "exclude" , EXCLUDE );
		}
		else
		// nupkg item
		if( distItemType == VarDISTITEMTYPE.PACKAGE ) {
			Common.xmlSetElementAttr( doc , root , "extension" , EXT );
		}
		
		Common.xmlSetElementAttr( doc , root , "customdeploy" , Common.getBooleanValue( CUSTOMDEPLOY ) );
	}
	
	public MetaDistrBinaryItem copy( ActionBase action , Meta meta , MetaDistrDelivery delivery ) throws Exception {
		MetaDistrBinaryItem r = new MetaDistrBinaryItem( meta , delivery );
		
		r.KEY = KEY;
		r.EXT = EXT;
		r.distItemType = distItemType;
		r.distItemOrigin = distItemOrigin;
		r.SRCPROJECTITEM = SRCPROJECTITEM;
		r.SRCDISTITEM = SRCDISTITEM;
		
		r.SRCITEMPATH = SRCITEMPATH; 
		r.DISTBASENAME = DISTBASENAME;
		r.DEPLOYBASENAME = DEPLOYBASENAME;
		r.deployVersion = deployVersion;
		r.WAR_MRID = WAR_MRID;
		r.WAR_CONTEXT = WAR_CONTEXT;
		r.WAR_STATICEXT = WAR_STATICEXT;
		r.BUILDINFO = BUILDINFO;
		r.FILES = FILES;
		r.EXCLUDE = EXCLUDE;
		
		r.CUSTOMDEPLOY = CUSTOMDEPLOY;
		
		return( r );
	}
	
	public void resolveReferences( ActionBase action ) throws Exception {
		if( distItemOrigin == VarDISTITEMORIGIN.DERIVED ) {
			MetaDistr distr = meta.getDistr();
			srcDistItem = distr.getBinaryItem( action , SRCDISTITEM );
		}
		else
		if( distItemOrigin == VarDISTITEMORIGIN.BUILD ) {
			MetaSource sources = meta.getSources();
			sourceProjectItem = sources.getProjectItem( action , SRCPROJECTITEM );
			sourceProjectItem.setDistItem( action , this );
		}
	}
	
	public boolean isArchive() {
		if( distItemType == VarDISTITEMTYPE.ARCHIVE_CHILD || 
			distItemType == VarDISTITEMTYPE.ARCHIVE_DIRECT || 
			distItemType == VarDISTITEMTYPE.ARCHIVE_SUBDIR )
			return( true );
		return( false );
	}
	
	public void setSource( ActionBase action , MetaSourceProjectItem sourceItem ) throws Exception {
		this.sourceProjectItem = sourceItem;
	}

	public String getBaseFile() {
		return( DISTBASENAME + EXT );
	}

	public VarITEMVERSION getVersionType( ActionBase action , String deployBaseName , String fileName ) throws Exception {
		String baseName = ( deployBaseName.isEmpty() )? DEPLOYBASENAME : deployBaseName;
		if( fileName.matches( baseName + EXT ) )
			return( VarITEMVERSION.NONE );
		
		if( fileName.matches( ".*[0-9]-" + baseName + EXT ) )
			return( VarITEMVERSION.PREFIX );
		
		if( fileName.matches( baseName + "-[0-9].*" + EXT ) )
			return( VarITEMVERSION.MIDDASH );
		
		if( fileName.matches( baseName + "##[0-9].*" + EXT ) )
			return( VarITEMVERSION.MIDPOUND );
		
		return( VarITEMVERSION.UNKNOWN );
	}

	public FileInfo getFileInfo( ActionBase action , String runtimeFile , String specificDeployName , String md5value ) throws Exception {
		VarITEMVERSION vtype = getVersionType( action , specificDeployName , runtimeFile );
		if( vtype == VarITEMVERSION.UNKNOWN )
			action.exit2( _Error.UnableGetFileVersionType2 , "unable to get version type of file=" + runtimeFile + ", deployName=" + specificDeployName , runtimeFile , specificDeployName );
		
		String name = Common.getPartBeforeLast( runtimeFile , EXT );
				
		if( vtype == VarITEMVERSION.NONE ) {
			String deployNameNoVersion = name;
			return( new FileInfo( this , null , md5value , deployNameNoVersion , runtimeFile ) );
		}
		
		if( vtype == VarITEMVERSION.PREFIX ) {
			String fileVersion = Common.getPartBeforeFirst( name , "-" );
			VersionInfo version = VersionInfo.getFileVersion( action , fileVersion );
			String deployNameNoVersion = Common.getPartAfterFirst( name , "-" );
			return( new FileInfo( this , version , md5value , deployNameNoVersion , runtimeFile ) );
		}
		
		if( vtype == VarITEMVERSION.MIDDASH ) {
			String fileVersion = Common.getPartAfterLast( name , "-" );
			VersionInfo version = VersionInfo.getFileVersion( action , fileVersion );
			String deployNameNoVersion = Common.getPartBeforeLast( name , "-" );
			return( new FileInfo( this , version , md5value , deployNameNoVersion , runtimeFile ) );
		}
		
		if( vtype == VarITEMVERSION.MIDPOUND ) {
			String fileVersion = Common.getPartAfterLast( name , "##" );
			VersionInfo version = VersionInfo.getFileVersion( action , fileVersion );
			String deployNameNoVersion = Common.getPartBeforeLast( name , "##" );
			return( new FileInfo( this , version , md5value , deployNameNoVersion , runtimeFile ) );
		}
		
		action.exitUnexpectedState();
		return( null );
	}

	public boolean isDerivedItem() {
		if( srcDistItem != null )
			return( true );
		return( false );
	}

	public boolean isManualItem() {
		if( distItemOrigin == VarDISTITEMORIGIN.MANUAL )
			return( true );
		return( false );
	}
	
	public boolean isProjectItem() {
		if( distItemOrigin == VarDISTITEMORIGIN.BUILD )
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
	
	public void setDistData( EngineTransaction transaction , VarDISTITEMTYPE itemType , String basename , String ext , String archiveFiles , String archiveExclude ) throws Exception {
		this.distItemType = itemType;
		this.DISTBASENAME = basename;
		this.EXT = ext;
		this.FILES = archiveFiles;
		this.EXCLUDE = archiveExclude;
		if( this.DISTBASENAME.isEmpty() )
			this.DISTBASENAME = KEY;
	}

	public void setDeployData( EngineTransaction transaction , String deployname , VarITEMVERSION versionType ) throws Exception {
		this.DEPLOYBASENAME = deployname;
		this.deployVersion = versionType;
		if( this.DEPLOYBASENAME.isEmpty() )
			this.DEPLOYBASENAME = DISTBASENAME;
	}

	public void setBuildOrigin( EngineTransaction transaction , MetaSourceProjectItem itemSrc ) throws Exception {
		this.distItemOrigin = VarDISTITEMORIGIN.BUILD;
		this.SRCPROJECTITEM = itemSrc.ITEMNAME;
		this.sourceProjectItem = itemSrc;
		this.SRCDISTITEM = "";
		this.srcDistItem = null;
		this.SRCITEMPATH = "";
		itemSrc.setDistItem( transaction.getAction() , this );
	}

	public void setDistOrigin( EngineTransaction transaction , MetaDistrBinaryItem itemSrc , String srcPath ) throws Exception {
		this.distItemOrigin = VarDISTITEMORIGIN.DERIVED;
		this.SRCPROJECTITEM = "";
		this.sourceProjectItem = null;
		this.SRCDISTITEM = itemSrc.KEY;
		this.srcDistItem = itemSrc;
		this.SRCITEMPATH = srcPath;
	}
	
	public void setManualOrigin( EngineTransaction transaction ) throws Exception {
		this.distItemOrigin = VarDISTITEMORIGIN.MANUAL;
		this.SRCPROJECTITEM = "";
		this.sourceProjectItem = null;
		this.SRCDISTITEM = "";
		this.srcDistItem = null;
		this.SRCITEMPATH = "";
	}

	public String getDeploySampleFile() {
		String value = DEPLOYBASENAME;
		
		if( isArchive() )
			return( "(archive)" );
		
		if( deployVersion == VarITEMVERSION.IGNORE ) {
			if( sourceProjectItem != null && sourceProjectItem.ITEMVERSION.isEmpty() == false )
				value += "-" + sourceProjectItem.ITEMVERSION;
		}
		else {
			String version = "1.0";
			if( sourceProjectItem != null && sourceProjectItem.ITEMVERSION.isEmpty() == false )
				version = sourceProjectItem.ITEMVERSION;
			
			if( deployVersion == VarITEMVERSION.MIDDASH )
				value += "-" + version;
			else
			if( deployVersion == VarITEMVERSION.MIDPOUND )
				value += "##" + version;
			else
			if( deployVersion == VarITEMVERSION.PREFIX )
				value = version + "-" + value;
		}
		
		value += EXT;
		return( value );
	}
	
}
