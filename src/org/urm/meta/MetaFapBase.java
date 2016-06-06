package org.urm.meta;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.Common;
import org.urm.ConfReader;
import org.urm.PropertySet;
import org.urm.action.ActionBase;
import org.urm.meta.Metadata.VarOSTYPE;
import org.urm.meta.Metadata.VarSERVERTYPE;
import org.urm.storage.BaseRepository;
import org.urm.storage.RemoteFolder;
import org.w3c.dom.Node;

public class MetaFapBase {

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
	
	public Metadata meta;
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
	
	public MetaFapBase( Metadata meta , BaseRepository repo , boolean primary ) {
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
		meta.loadRawFromAttributes( action , node );
		scatterVariables( action , meta );
		
		meta.loadRawFromElements( action , node );
		meta.moveRawAsStrings( action );
		meta.copyProperties( action , serverNode.properties );

		properties = new PropertySet( "final" , null );
		properties.copyProperties( action , meta );

		if( action.isDebug() )
			properties.printValues( action );
		
		loadCompatibility( action , node );
		loadDependencies( action , node );
	}

	private void loadCompatibility( ActionBase action , Node node ) throws Exception {
		compatibilityMap = new HashMap<String,String>();
		Node comp = ConfReader.xmlGetFirstChild( action , node , "compatibility" );
		if( comp == null )
			return;
		
		Node[] items = ConfReader.xmlGetChildren( action , comp , "os" );
		if( items == null )
			return;
		
		for( Node snnode : items ) {
			String TYPE = ConfReader.getRequiredAttrValue( action , snnode , "type" );
			String VERSION = ConfReader.getRequiredAttrValue( action , snnode , "version" );
			if( compatibilityMap.get( TYPE ) != null )
				action.exit( "unexpected duplicate type=" + TYPE );
			
			compatibilityMap.put( TYPE , VERSION );
		}
	}
	
	private void loadDependencies( ActionBase action , Node node ) throws Exception {
		dependencies = new LinkedList<String>();
		Node deps = ConfReader.xmlGetFirstChild( action , node , "dependencies" );
		if( deps == null )
			return;
		
		Node[] items = ConfReader.xmlGetChildren( action , deps , "base" );
		if( items == null )
			return;
		
		for( Node snnode : items ) {
			String BASEID = ConfReader.getRequiredAttrValue( action , snnode , "id" );
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
		ID = props.getSystemRequiredStringProperty( action , "id" );
		String TYPE = props.getSystemRequiredStringProperty( action , "type" );
		type = getType( action , TYPE );
		adm = props.getSystemBooleanProperty( action , "adminstall" , false );
		
		String OSTYPE = props.getSystemStringProperty( action , "ostype" , null );
		osType = action.meta.getOSType( action , OSTYPE );

		String CHARSET = props.getSystemStringProperty( action , "charset" , "" );
		if( !CHARSET.isEmpty() ) {
			charset = Charset.forName( CHARSET );
			if( charset == null )
				action.exit( "unknown system files charset=" + CHARSET );
		}
		
		String SERVERTYPE = null;
		if( primary )
			SERVERTYPE = props.getSystemRequiredStringProperty( action , "servertype" );
		else
			SERVERTYPE = props.getSystemStringProperty( action , "servertype" , null );
		
		if( SERVERTYPE != null )
			serverType = action.meta.getServerType( action , SERVERTYPE );
		
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
		
		props.finishRawProperties( action );
	}

	private void scatterLinuxArchiveLink( ActionBase action , PropertySet props ) throws Exception {
		srcFormat = getSrcFormat( action , props.getSystemRequiredStringProperty( action , "srcformat" ) );
		SRCFILE = props.getSystemRequiredPathProperty( action , "srcfile" );
		SRCSTOREDIR = props.getSystemRequiredPathProperty( action , "srcstoreddir" );
		INSTALLPATH = props.getSystemRequiredPathProperty( action , "installpath" );
		INSTALLLINK = props.getSystemRequiredPathProperty( action , "installlink" );
	}
	
	private void scatterLinuxArchiveDirect( ActionBase action , PropertySet props ) throws Exception {
		srcFormat = getSrcFormat( action , props.getSystemRequiredStringProperty( action , "srcformat" ) );
		SRCFILE = props.getSystemRequiredPathProperty( action , "srcfile" );
		SRCSTOREDIR = props.getSystemRequiredPathProperty( action , "srcstoreddir" );
		INSTALLPATH = props.getSystemRequiredPathProperty( action , "installpath" );
	}

	private void scatterNoDist( ActionBase action , PropertySet props ) throws Exception {
	}

	private void scatterInstaller( ActionBase action , PropertySet props ) throws Exception {
		srcFormat = getSrcFormat( action , props.getSystemRequiredStringProperty( action , "srcformat" ) );
		SRCFILE = props.getSystemRequiredPathProperty( action , "srcfile" );
	}

	public String getItemPath( ActionBase action , String SRCFILE ) throws Exception {
		return( repo.getBaseItemPath( action , ID , SRCFILE ) );
	}

	public RemoteFolder getFolder( ActionBase action ) throws Exception {
		return( repo.getBaseFolder( action , ID ) );
	}
}
