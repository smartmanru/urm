package org.urm.server.meta;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.Meta.VarARCHIVETYPE;
import org.urm.server.meta.Meta.VarDISTITEMSOURCE;
import org.urm.server.meta.Meta.VarDISTITEMTYPE;
import org.urm.server.meta.Meta.VarITEMVERSION;
import org.urm.server.meta.Meta.VarNAMETYPE;
import org.urm.server.storage.FileInfo;
import org.w3c.dom.Node;

public class MetaDistrBinaryItem {

	protected Meta meta;
	public MetaDistrDelivery delivery;
	public MetaSourceProjectItem sourceItem;

	public String KEY;
	public String EXT;
	public VarDISTITEMTYPE DISTTYPE;
	public VarDISTITEMSOURCE DISTSOURCE;
	public String SRCDISTITEM;
	public MetaDistrBinaryItem srcItem;
	public String SRCITEMPATH; 
	public String DISTBASENAME;
	public String DEPLOYBASENAME;
	public VarITEMVERSION DEPLOYVERSION;
	public String WAR_MRID;
	public String WAR_CONTEXT;
	public String WAR_STATICEXT;
	public String BUILDINFO;
	public String FILES;
	public String EXCLUDE;
	
	public boolean CUSTOMDEPLOY;
	Node node;
	
	public MetaDistrBinaryItem( Meta meta , MetaDistrDelivery delivery ) {
		this.meta = meta;
		this.delivery = delivery; 
	}

	public void load( ActionBase action , Node node ) throws Exception {
		this.node = node;
		KEY = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
	
		// read attrs
		DISTTYPE = meta.getItemDistType( ConfReader.getRequiredAttrValue( node , "type" ) );
		DISTSOURCE = meta.getItemDistSource( ConfReader.getRequiredAttrValue( node , "source" ) );
		if( DISTSOURCE == VarDISTITEMSOURCE.DISTITEM ) {
			SRCDISTITEM = ConfReader.getAttrValue( node , "srcitem" );
			SRCITEMPATH = ConfReader.getAttrValue( node , "srcpath" );
		}
		
		DISTBASENAME = ConfReader.getAttrValue( node , "distname" , KEY );
		DEPLOYBASENAME = ConfReader.getAttrValue( node , "deployname" , DISTBASENAME );
		DEPLOYVERSION = meta.readItemVersionAttr( node , "deployversion" );
		BUILDINFO = ConfReader.getAttrValue( node , "buildinfo" );

		// binary item
		if( DISTTYPE == VarDISTITEMTYPE.BINARY ) {
			EXT = ConfReader.getRequiredAttrValue( node , "extension" );
		}
		else
		// war item and static
		if( DISTTYPE == VarDISTITEMTYPE.WAR ) {
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
		if( DISTTYPE == VarDISTITEMTYPE.DOTNETPKG ) {
			EXT = ConfReader.getRequiredAttrValue( node , "extension" );
		}
		else {
			String distType = Common.getEnumLower( DISTTYPE );
			action.exit2( _Error.UnknownDistributiveItemType2 , "distribution item " + KEY + " has unknown type=" + distType , KEY , distType );
		}
		
		CUSTOMDEPLOY = ConfReader.getBooleanAttrValue( node , "customdeploy" , false );
		if( CUSTOMDEPLOY )
			action.custom.parseDistItem( action , this , node );
	}

	public void resolveReferences( ActionBase action ) throws Exception {
		if( DISTSOURCE == VarDISTITEMSOURCE.DISTITEM )
			srcItem = meta.distr.getBinaryItem( action , SRCDISTITEM ); 
	}
	
	public boolean isArchive( ActionBase action ) throws Exception {
		if( DISTTYPE == VarDISTITEMTYPE.ARCHIVE_CHILD || 
			DISTTYPE == VarDISTITEMTYPE.ARCHIVE_DIRECT || 
			DISTTYPE == VarDISTITEMTYPE.ARCHIVE_SUBDIR )
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
		if( srcItem != null )
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
