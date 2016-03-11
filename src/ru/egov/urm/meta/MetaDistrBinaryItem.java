package ru.egov.urm.meta;

import org.w3c.dom.Node;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.custom.ICustomDeploy;
import ru.egov.urm.meta.Metadata.VarDISTITEMSOURCE;
import ru.egov.urm.meta.Metadata.VarDISTITEMTYPE;
import ru.egov.urm.meta.Metadata.VarITEMVERSION;
import ru.egov.urm.meta.Metadata.VarNAMETYPE;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.storage.FileInfo;

public class MetaDistrBinaryItem {

	Metadata meta;
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
	ICustomDeploy deploy;
	
	public MetaDistrBinaryItem( Metadata meta , MetaDistrDelivery delivery ) {
		this.meta = meta;
		this.delivery = delivery; 
	}

	public void load( ActionBase action , Node node ) throws Exception {
		this.node = node;
		KEY = ConfReader.getNameAttr( action , node , VarNAMETYPE.ALPHANUMDOT );
	
		// read attrs
		DISTTYPE = meta.getItemDistType( action , ConfReader.getRequiredAttrValue( action , node , "type" ) );
		DISTSOURCE = meta.getItemDistSource( action , ConfReader.getRequiredAttrValue( action , node , "source" ) );
		if( DISTSOURCE == VarDISTITEMSOURCE.DISTITEM ) {
			SRCDISTITEM = ConfReader.getAttrValue( action , node , "srcitem" );
			SRCITEMPATH = ConfReader.getAttrValue( action , node , "srcpath" );
		}
		
		DISTBASENAME = ConfReader.getAttrValue( action , node , "distname" , KEY );
		DEPLOYBASENAME = ConfReader.getAttrValue( action , node , "deployname" , DISTBASENAME );
		DEPLOYVERSION = meta.readItemVersionAttr( action , node , "deployversion" );
		BUILDINFO = ConfReader.getAttrValue( action , node , "buildinfo" );

		// binary item
		if( DISTTYPE == VarDISTITEMTYPE.BINARY ) {
			EXT = ConfReader.getRequiredAttrValue( action , node , "extension" );
		}
		else
		// war item and static
		if( DISTTYPE == VarDISTITEMTYPE.WAR ) {
			EXT = ".war";
	
			WAR_MRID = ConfReader.getAttrValue( action , node , "mrid" );
			WAR_CONTEXT = ConfReader.getAttrValue( action , node , "context" , DEPLOYBASENAME );
			WAR_STATICEXT = ConfReader.getAttrValue( action , node , "extension" , "-webstatic.tar.gz" );
		}
		else
		// archive item
		if( isArchive( action ) ) {
			EXT = ConfReader.getAttrValue( action , node , "extension" , ".tar.gz" );
			FILES = ConfReader.getAttrValue( action , node , "files" , "*" );
			EXCLUDE = ConfReader.getAttrValue( action , node , "exclude" );
		}
		else
		// nupkg item
		if( DISTTYPE == VarDISTITEMTYPE.DOTNETPKG ) {
			EXT = ConfReader.getRequiredAttrValue( action , node , "extension" );
		}
		else
			action.exit( "distribution item " + KEY + " has unknown type=" + Common.getEnumLower( DISTTYPE ) );
		
		CUSTOMDEPLOY = ConfReader.getBooleanAttrValue( action , node , "customdeploy" , false );
		if( CUSTOMDEPLOY ) {
			String className = ConfReader.getRequiredAttrValue( action , node , "class" );
			deploy = Common.getDeployClass( action , className );
			deploy.parseDistItem( action , this , node );
		}
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
			action.exit( "unable to get version type of file=" + runtimeFile + ", deployName=" + specificDeployName );
		
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
	
}
