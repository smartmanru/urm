package org.urm.server.meta;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertySet;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.Meta.VarSERVERTYPE;
import org.urm.server.storage.BaseRepository;
import org.urm.server.storage.RemoteFolder;
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
	
	protected Meta meta;
	public BaseRepository repo;
	public boolean primary;
	
	public PropertySet properties;
	public String ID;
	public VarBASESRCTYPE type;
	public boolean adm;
	public VarOSTYPE osType;
	public VarSERVERTYPE serverType;
	public Charset charset;

	public VarBASESRCFORMAT srcFormat;
	public String SRCFILE;
	public String SRCSTOREDIR;
	public String INSTALLPATH;
	public String INSTALLLINK;
	
	public List<String> dependencies;
	public Map<String,String> compatibilityMap;
	
	public MetaBase( Meta meta , BaseRepository repo , boolean primary ) {
		this.meta = meta;
		this.repo = repo;
		this.primary = primary;
	}
	
	public VarBASESRCTYPE getType( ActionBase action , String TYPE ) throws Exception {
		VarBASESRCTYPE value = null;		
		
		try {
			value = VarBASESRCTYPE.valueOf( Common.xmlToEnumValue( TYPE ) );
		}
		catch( IllegalArgumentException e ) {
			action.exit( "invalid basesrctype=" + TYPE );
		}
		
		if( value == null )
			action.exit( "unknown basesrctype=" + TYPE );
		
		return( value );
	}

	public VarBASESRCFORMAT getSrcFormat( ActionBase action , String TYPE ) throws Exception {
		VarBASESRCFORMAT value = null;		
		
		try {
			value = VarBASESRCFORMAT.valueOf( Common.xmlToEnumValue( TYPE ) );
		}
		catch( IllegalArgumentException e ) {
			action.exit( "invalid basesrcformat=" + TYPE );
		}
		
		if( value == null )
			action.exit( "unknown basesrcformat=" + TYPE );
		
		return( value );
	}

	public void load( ActionBase action , Node node , MetaEnvServerNode serverNode ) throws Exception {
		PropertySet meta = new PropertySet( "meta" , serverNode.server.base.properties );
		meta.loadRawFromNodeAttributes( node );
		scatterVariables( action , meta );
		
		meta.loadRawFromNodeElements( node );
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
				action.exit( "unexpected duplicate type=" + TYPE );
			
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
		type = getType( action , TYPE );
		adm = props.getSystemBooleanProperty( "adminstall" , false );
		
		String OSTYPE = props.getSystemStringProperty( "ostype" , null );
		osType = meta.getOSType( OSTYPE );

		String CHARSET = props.getSystemStringProperty( "charset" , "" );
		if( !CHARSET.isEmpty() ) {
			charset = Charset.forName( CHARSET );
			if( charset == null )
				action.exit( "unknown system files charset=" + CHARSET );
		}
		
		String SERVERTYPE = null;
		if( primary )
			SERVERTYPE = props.getSystemRequiredStringProperty( "servertype" );
		else
			SERVERTYPE = props.getSystemStringProperty( "servertype" , null );
		
		if( SERVERTYPE != null )
			serverType = meta.getServerType( SERVERTYPE );
		
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
		srcFormat = getSrcFormat( action , props.getSystemRequiredStringProperty( "srcformat" ) );
		SRCFILE = props.getSystemRequiredPathProperty( "srcfile" , action.session.execrc );
		SRCSTOREDIR = props.getSystemRequiredPathProperty( "srcstoreddir" , action.session.execrc );
		INSTALLPATH = props.getSystemRequiredPathProperty( "installpath" , action.session.execrc );
		INSTALLLINK = props.getSystemRequiredPathProperty( "installlink" , action.session.execrc );
	}
	
	private void scatterLinuxArchiveDirect( ActionBase action , PropertySet props ) throws Exception {
		srcFormat = getSrcFormat( action , props.getSystemRequiredStringProperty( "srcformat" ) );
		SRCFILE = props.getSystemRequiredPathProperty( "srcfile" , action.session.execrc );
		SRCSTOREDIR = props.getSystemRequiredPathProperty( "srcstoreddir" , action.session.execrc );
		INSTALLPATH = props.getSystemRequiredPathProperty( "installpath" , action.session.execrc );
	}

	private void scatterNoDist( ActionBase action , PropertySet props ) throws Exception {
	}

	private void scatterInstaller( ActionBase action , PropertySet props ) throws Exception {
		srcFormat = getSrcFormat( action , props.getSystemRequiredStringProperty( "srcformat" ) );
		SRCFILE = props.getSystemRequiredPathProperty( "srcfile" , action.session.execrc );
	}

	public String getItemPath( ActionBase action , String SRCFILE ) throws Exception {
		return( repo.getBaseItemPath( action , ID , SRCFILE ) );
	}

	public RemoteFolder getFolder( ActionBase action ) throws Exception {
		return( repo.getBaseFolder( action , ID ) );
	}
}
