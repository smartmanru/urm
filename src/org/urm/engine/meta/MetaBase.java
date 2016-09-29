package org.urm.engine.meta;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.ConfReader;
import org.urm.common.PropertySet;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.engine.meta.Meta.VarSERVERACCESSTYPE;
import org.urm.engine.storage.BaseRepository;
import org.urm.engine.storage.RemoteFolder;
import org.w3c.dom.Node;

public class MetaBase {

	public enum VarBASESRCTYPE {
		LINUX_ARCHIVE_LINK ,
		LINUX_ARCHIVE_DIRECT ,
		ARCHIVE_DIRECT ,
		NODIST ,
		INSTALLER
	};
	
	public enum VarBASESRCFORMAT {
		TARGZ_SINGLEDIR ,
		ZIP_SINGLEDIR ,
		SINGLEFILE
	};
	
	public BaseRepository repo;
	public boolean primary;
	
	public PropertySet properties;
	public String ID;
	public VarBASESRCTYPE type;
	public boolean adm;
	public VarOSTYPE osType;
	public VarSERVERACCESSTYPE serverAccessType;
	public Charset charset;

	public VarBASESRCFORMAT srcFormat;
	public String SRCFILE;
	public String SRCSTOREDIR;
	public String INSTALLPATH;
	public String INSTALLLINK;
	
	public List<String> dependencies;
	public Map<String,String> compatibilityMap;
	
	public MetaBase( BaseRepository repo , boolean primary ) {
		this.repo = repo;
		this.primary = primary;
	}
	
	public void load( ActionBase action , Node node , MetaEnvServerNode serverNode ) throws Exception {
		PropertySet meta = new PropertySet( "meta" , serverNode.server.base.properties );
		meta.loadFromNodeAttributes( node );
		scatterVariables( action , meta );
		
		meta.loadFromNodeElements( node );
		meta.resolveRawProperties();
		meta.copyRunningPropertiesToRunning( serverNode.properties );

		properties = new PropertySet( "final" , null );
		properties.copyRunningPropertiesToRunning( meta );

		if( action.isDebug() )
			action.printValues( properties );
		
		loadCompatibility( action , node );
		loadDependencies( action , node );
	}

	private void loadCompatibility( ActionBase action , Node node ) throws Exception {
		compatibilityMap = new HashMap<String,String>();
		Node comp = ConfReader.xmlGetFirstChild( node , "compatibility" );
		if( comp == null )
			return;
		
		Node[] items = ConfReader.xmlGetChildren( comp , "os" );
		if( items == null )
			return;
		
		for( Node snnode : items ) {
			String TYPE = ConfReader.getRequiredAttrValue( snnode , "type" );
			String VERSION = ConfReader.getRequiredAttrValue( snnode , "version" );
			if( compatibilityMap.get( TYPE ) != null )
				action.exit1( _Error.DuplicateOSType1 , "unexpected duplicate OS type=" + TYPE , TYPE );
			
			compatibilityMap.put( TYPE , VERSION );
		}
	}
	
	private void loadDependencies( ActionBase action , Node node ) throws Exception {
		dependencies = new LinkedList<String>();
		Node deps = ConfReader.xmlGetFirstChild( node , "dependencies" );
		if( deps == null )
			return;
		
		Node[] items = ConfReader.xmlGetChildren( deps , "base" );
		if( items == null )
			return;
		
		for( Node snnode : items ) {
			String BASEID = ConfReader.getRequiredAttrValue( snnode , "id" );
			dependencies.add( BASEID );
		}
	}

	public boolean isNoDist() {
		if( type == VarBASESRCTYPE.NODIST )
			return( true );
		return( false );
	}
	
	public boolean isInstaller() {
		if( type == VarBASESRCTYPE.INSTALLER )
			return( true );
		return( false );
	}
	
	public boolean isLinuxArchiveLink() {
		if( type == VarBASESRCTYPE.LINUX_ARCHIVE_LINK )
			return( true );
		return( false );
	}
	
	public boolean isArchiveDirect() {
		if( type == VarBASESRCTYPE.LINUX_ARCHIVE_DIRECT || 
			type == VarBASESRCTYPE.ARCHIVE_DIRECT )
			return( true );
		return( false );
	}

	public boolean isArchive() {
		if( isLinuxArchiveLink() ||
			isArchiveDirect() )
			return( true );
		return( false );
	}
	
	private void scatterVariables( ActionBase action , PropertySet props ) throws Exception {
		// unified properties
		ID = props.getSystemRequiredStringProperty( "id" );
		String TYPE = props.getSystemRequiredStringProperty( "type" );
		type = Meta.getBaseSrcType( action , TYPE );
		adm = props.getSystemBooleanProperty( "adminstall" );
		
		String OSTYPE = props.getSystemStringProperty( "ostype" );
		osType = Meta.getOSType( OSTYPE );

		String CHARSET = props.getSystemStringProperty( "charset" );
		if( !CHARSET.isEmpty() ) {
			charset = Charset.forName( CHARSET );
			if( charset == null )
				action.exit1( _Error.UnknownSystemFilesCharset1 , "unknown system files charset=" + CHARSET , CHARSET );
		}
		
		String SERVERTYPE = null;
		if( primary )
			SERVERTYPE = props.getSystemRequiredStringProperty( "server-accesstype" );
		else
			SERVERTYPE = props.getSystemStringProperty( "server-accesstype" );
		
		if( SERVERTYPE != null )
			serverAccessType = Meta.getServerAccessType( SERVERTYPE );
		
		// type properties
		if( isLinuxArchiveLink() )
			scatterLinuxArchiveLink( action , props );
		else
		if( isArchiveDirect() )
			scatterLinuxArchiveDirect( action , props );
		else
		if( isNoDist() )
			scatterNoDist( action , props );
		else
		if( isInstaller() )
			scatterInstaller( action , props );
		else
			action.exitUnexpectedState();
		
		props.finishRawProperties();
	}

	private void scatterLinuxArchiveLink( ActionBase action , PropertySet props ) throws Exception {
		srcFormat = Meta.getBaseSrcFormat( action , props.getSystemRequiredStringProperty( "srcformat" ) );
		SRCFILE = props.getSystemRequiredPathProperty( "srcfile" , action.session.execrc );
		SRCSTOREDIR = props.getSystemRequiredPathProperty( "srcstoreddir" , action.session.execrc );
		INSTALLPATH = props.getSystemRequiredPathProperty( "installpath" , action.session.execrc );
		INSTALLLINK = props.getSystemRequiredPathProperty( "installlink" , action.session.execrc );
	}
	
	private void scatterLinuxArchiveDirect( ActionBase action , PropertySet props ) throws Exception {
		srcFormat = Meta.getBaseSrcFormat( action , props.getSystemRequiredStringProperty( "srcformat" ) );
		SRCFILE = props.getSystemRequiredPathProperty( "srcfile" , action.session.execrc );
		SRCSTOREDIR = props.getSystemRequiredPathProperty( "srcstoreddir" , action.session.execrc );
		INSTALLPATH = props.getSystemRequiredPathProperty( "installpath" , action.session.execrc );
	}

	private void scatterNoDist( ActionBase action , PropertySet props ) throws Exception {
	}

	private void scatterInstaller( ActionBase action , PropertySet props ) throws Exception {
		srcFormat = Meta.getBaseSrcFormat( action , props.getSystemRequiredStringProperty( "srcformat" ) );
		SRCFILE = props.getSystemRequiredPathProperty( "srcfile" , action.session.execrc );
	}

	public String getItemPath( ActionBase action , String SRCFILE ) throws Exception {
		return( repo.getBaseItemPath( action , ID , SRCFILE ) );
	}

	public RemoteFolder getFolder( ActionBase action ) throws Exception {
		return( repo.getBaseFolder( action , ID ) );
	}
}
