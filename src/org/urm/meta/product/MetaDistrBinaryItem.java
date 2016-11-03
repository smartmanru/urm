package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.custom.CommandCustom;
import org.urm.engine.storage.FileInfo;
import org.urm.meta.product.Meta.VarARCHIVETYPE;
import org.urm.meta.product.Meta.VarDISTITEMORIGIN;
import org.urm.meta.product.Meta.VarDISTITEMTYPE;
import org.urm.meta.product.Meta.VarITEMVERSION;
import org.urm.meta.product.Meta.VarNAMETYPE;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaDistrBinaryItem {

	public Meta meta;
	public MetaDistrDelivery delivery;
	
	public MetaSourceProjectItem sourceItem;
	public String KEY;
	public String EXT;
	public VarDISTITEMTYPE distItemType;
	public VarDISTITEMORIGIN distItemOrigin;
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

	public void load( ActionBase action , Node node ) throws Exception {
		KEY = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOTDASH );
	
		// read attrs
		distItemType = Meta.getItemDistType( ConfReader.getRequiredAttrValue( node , "type" ) , false );
		distItemOrigin = Meta.getItemDistSource( ConfReader.getRequiredAttrValue( node , "source" ) , false );
		if( distItemOrigin == VarDISTITEMORIGIN.DISTITEM ) {
			SRCDISTITEM = ConfReader.getAttrValue( node , "srcitem" );
			SRCITEMPATH = ConfReader.getAttrValue( node , "srcpath" );
		}
		
		DISTBASENAME = ConfReader.getAttrValue( node , "distname" , KEY );
		DEPLOYBASENAME = ConfReader.getAttrValue( node , "deployname" , DISTBASENAME );
		deployVersion = Meta.readItemVersionAttr( node , "deployversion" );
		BUILDINFO = ConfReader.getAttrValue( node , "buildinfo" );

		// binary item
		if( distItemType == VarDISTITEMTYPE.BINARY ) {
			EXT = ConfReader.getRequiredAttrValue( node , "extension" );
		}
		else
		// war item and static
		if( distItemType == VarDISTITEMTYPE.WAR ) {
			EXT = ".war";
	
			WAR_MRID = ConfReader.getAttrValue( node , "mrid" );
			WAR_CONTEXT = ConfReader.getAttrValue( node , "context" , DEPLOYBASENAME );
			WAR_STATICEXT = ConfReader.getAttrValue( node , "extension" , "-webstatic.tar.gz" );
		}
		else
		// archive item
		if( isArchive( action ) ) {
			EXT = ConfReader.getAttrValue( node , "extension" , ".tar.gz" );
			FILES = ConfReader.getAttrValue( node , "files" , "*" );
			EXCLUDE = ConfReader.getAttrValue( node , "exclude" );
		}
		else
		// nupkg item
		if( distItemType == VarDISTITEMTYPE.DOTNETPKG ) {
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
		if( distItemOrigin == VarDISTITEMORIGIN.DISTITEM ) {
			Common.xmlSetElementAttr( doc , root , "srcitem" , SRCDISTITEM );
			Common.xmlSetElementAttr( doc , root , "srcpath" , SRCITEMPATH );
		}
		
		Common.xmlSetElementAttr( doc , root , "distname" , DISTBASENAME );
		Common.xmlSetElementAttr( doc , root , "deployname" , DEPLOYBASENAME );
		Common.xmlSetElementAttr( doc , root , "deployversion" , Common.getEnumLower( deployVersion ) );
		Common.xmlSetElementAttr( doc , root , "buildinfo" , BUILDINFO );

		// binary item
		if( distItemType == VarDISTITEMTYPE.BINARY ) {
			Common.xmlSetElementAttr( doc , root , "extension" , EXT );
		}
		else
		// war item and static
		if( distItemType == VarDISTITEMTYPE.WAR ) {
			EXT = ".war";
	
			Common.xmlSetElementAttr( doc , root , "mrid" , WAR_MRID );
			Common.xmlSetElementAttr( doc , root , "context" , WAR_CONTEXT );
			Common.xmlSetElementAttr( doc , root , "extension" , WAR_STATICEXT );
		}
		else
		// archive item
		if( isArchive( action ) ) {
			Common.xmlSetElementAttr( doc , root , "extension" , EXT );
			Common.xmlSetElementAttr( doc , root , "files" , FILES );
			Common.xmlSetElementAttr( doc , root , "exclude" , EXCLUDE );
		}
		else
		// nupkg item
		if( distItemType == VarDISTITEMTYPE.DOTNETPKG ) {
			Common.xmlSetElementAttr( doc , root , "extension" , EXT );
		}
		
		Common.xmlSetElementAttr( doc , root , "customdeploy" , Common.getBooleanValue( CUSTOMDEPLOY ) );
	}
	
	public MetaDistrBinaryItem copy( ActionBase action , Meta meta , MetaDistrDelivery delivery ) throws Exception {
		MetaDistrBinaryItem r = new MetaDistrBinaryItem( meta , delivery );
		if( sourceItem != null ) {
			MetaSource source = meta.getSources( action );
			MetaSourceProject project = source.getProject( action , sourceItem.project.PROJECT );
			r.sourceItem = project.getItem( action , sourceItem.ITEMNAME );
		}
			
		r.KEY = KEY;
		r.EXT = EXT;
		r.distItemType = distItemType;
		r.distItemOrigin = distItemOrigin;
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
		if( distItemOrigin == VarDISTITEMORIGIN.DISTITEM ) {
			MetaDistr distr = meta.getDistr( action );
			srcDistItem = distr.getBinaryItem( action , SRCDISTITEM );
		}
		else
		if( distItemOrigin == VarDISTITEMORIGIN.BUILD ) {
			MetaSource sources = meta.getSources( action );
			sourceItem = sources.getProjectItem( action , SRCDISTITEM );
		}
	}
	
	public boolean isArchive( ActionBase action ) throws Exception {
		if( distItemType == VarDISTITEMTYPE.ARCHIVE_CHILD || 
			distItemType == VarDISTITEMTYPE.ARCHIVE_DIRECT || 
			distItemType == VarDISTITEMTYPE.ARCHIVE_SUBDIR )
			return( true );
		return( false );
	}
	
	public void setSource( ActionBase action , MetaSourceProjectItem sourceItem ) throws Exception {
		this.sourceItem = sourceItem;
	}

	public String getGrepMask( ActionBase action , String baseName , boolean addDotSlash ) throws Exception {
		if( addDotSlash )
			return( "./" + baseName + EXT + 
					"|./.*[0-9]-" + baseName + EXT + 
					"|./" + baseName + "-[0-9].*" + EXT +
					"|./" + baseName + "##[0-9].*" + EXT );
		return( baseName + EXT + 
				"|.*[0-9]-" + baseName + EXT + 
				"|" + baseName + "-[0-9].*" + EXT +
				"|" + baseName + "##[0-9].*" + EXT );
	}
	
	public String getBaseFile( ActionBase action ) throws Exception {
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
			String version = "";
			String deployNameNoVersion = name;
			return( new FileInfo( this , version , md5value , deployNameNoVersion , runtimeFile ) );
		}
		
		if( vtype == VarITEMVERSION.PREFIX ) {
			String version = Common.getPartBeforeFirst( name , "-" );
			String deployNameNoVersion = Common.getPartAfterFirst( name , "-" );
			return( new FileInfo( this , version , md5value , deployNameNoVersion , runtimeFile ) );
		}
		
		if( vtype == VarITEMVERSION.MIDDASH ) {
			String version = Common.getPartAfterLast( name , "-" );
			String deployNameNoVersion = Common.getPartBeforeLast( name , "-" );
			return( new FileInfo( this , version , md5value , deployNameNoVersion , runtimeFile ) );
		}
		
		if( vtype == VarITEMVERSION.MIDPOUND ) {
			String version = Common.getPartAfterLast( name , "##" );
			String deployNameNoVersion = Common.getPartBeforeLast( name , "##" );
			return( new FileInfo( this , version , md5value , deployNameNoVersion , runtimeFile ) );
		}
		
		action.exitUnexpectedState();
		return( null );
	}

	public boolean isDerived( ActionBase action ) throws Exception {
		if( srcDistItem != null )
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
}
