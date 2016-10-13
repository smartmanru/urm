package org.urm.meta.engine;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertyController;
import org.urm.common.PropertySet;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.engine.storage.BaseRepository;
import org.urm.engine.storage.RemoteFolder;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.product.Meta.VarBASESRCFORMAT;
import org.urm.meta.product.Meta.VarBASESRCTYPE;
import org.urm.meta.product._Error;
import org.urm.meta.product.Meta.VarSERVERACCESSTYPE;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerBaseItemData extends PropertyController {

	public ServerBaseItem item;
	public BaseRepository repo;
	public MetaEnvServerNode serverNode;

	public String NAME;
	public String VERSION;
	
	public VarBASESRCTYPE type;
	public VarBASESRCFORMAT srcFormat;
	public String SRCFILE;
	public String SRCSTOREDIR;
	public String INSTALLPATH;
	public String INSTALLLINK;
	
	public List<String> dependencies;
	public Map<String,String> compatibilityMap;
	
	public boolean adm;
	public VarOSTYPE osType;
	public VarSERVERACCESSTYPE serverAccessType;
	public Charset charset;

	public ServerBaseItemData( ServerBaseItem item , BaseRepository repo ) {
		super( null , "base" );
		this.item = item;
		this.repo = repo;
	}

	public ServerBaseItemData( ServerBaseItem item , BaseRepository repo , MetaEnvServerNode serverNode ) {
		super( serverNode , "final" );
		this.item = item;
		this.repo = repo;
		this.serverNode = serverNode;
	}

	@Override
	public boolean isValid() {
		if( super.isLoadFailed() )
			return( false );
		return( true );
	}
	
	@Override
	public void scatterProperties( ActionBase action ) throws Exception {
		// unified properties
		NAME = super.getStringPropertyRequired( action , "name" );
		VERSION = super.getStringPropertyRequired( action , "version" );
		
		String TYPE = super.getStringPropertyRequired( action , "type" );
		type = Meta.getBaseSrcType( action , TYPE );
		adm = super.getBooleanProperty( action , "adminstall" );
		
		String OSTYPE = super.getStringPropertyRequired( action , "ostype" );
		osType = Meta.getOSType( OSTYPE );

		String CHARSET = super.getStringProperty( action , "charset" );
		if( !CHARSET.isEmpty() ) {
			charset = Charset.forName( CHARSET );
			if( charset == null )
				action.exit1( _Error.UnknownSystemFilesCharset1 , "unknown system files charset=" + CHARSET , CHARSET );
		}
		
		String SERVERTYPE = super.getStringProperty( action , "server-accesstype" );
		if( SERVERTYPE != null )
			serverAccessType = Meta.getServerAccessType( SERVERTYPE );
		
		// type properties
		if( isLinuxArchiveLink() )
			scatterLinuxArchiveLink( action );
		else
		if( isArchiveDirect() )
			scatterLinuxArchiveDirect( action );
		else
		if( isNoDist() )
			scatterNoDist( action );
		else
		if( isInstaller() )
			scatterInstaller( action );
		else
			action.exitUnexpectedState();
		
		super.finishProperties( action );
	}

	public void load( ActionBase action , Node root ) throws Exception {
		PropertySet parent = ( serverNode == null )? null : serverNode.getProperties();
		if( !super.initCreateStarted( parent ) )
			return;

		properties.loadFromNodeAttributes( root );
		scatterProperties( action );
		
		loadCompatibility( action , root );
		loadDependencies( action , root );
		
		super.initFinished();
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

	private void scatterLinuxArchiveLink( ActionBase action ) throws Exception {
		srcFormat = Meta.getBaseSrcFormat( action , super.getStringPropertyRequired( action , "srcformat" ) );
		SRCFILE = super.getPathPropertyRequired( action , "srcfile" );
		SRCSTOREDIR = super.getPathPropertyRequired( action , "srcstoreddir" );
		INSTALLPATH = super.getPathPropertyRequired( action , "installpath" );
		INSTALLLINK = super.getPathPropertyRequired( action , "installlink" );
	}
	
	private void scatterLinuxArchiveDirect( ActionBase action ) throws Exception {
		srcFormat = Meta.getBaseSrcFormat( action , super.getStringPropertyRequired( action , "srcformat" ) );
		SRCFILE = super.getPathPropertyRequired( action , "srcfile" );
		SRCSTOREDIR = super.getPathPropertyRequired( action , "srcstoreddir" );
		INSTALLPATH = super.getPathPropertyRequired( action , "installpath" );
	}

	private void scatterNoDist( ActionBase action ) throws Exception {
	}

	private void scatterInstaller( ActionBase action ) throws Exception {
		srcFormat = Meta.getBaseSrcFormat( action , super.getStringPropertyRequired( action , "srcformat" ) );
		SRCFILE = super.getPathPropertyRequired( action , "srcfile" );
	}

	public String getItemPath( ActionBase action , String SRCFILE ) throws Exception {
		return( repo.getBaseItemPath( action , item.ID , SRCFILE ) );
	}

	public RemoteFolder getFolder( ActionBase action ) throws Exception {
		return( repo.getBaseFolder( action , item.ID ) );
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
	
	public void save( Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , NAME );
		Common.xmlSetElementAttr( doc , root , "version" , VERSION );
	}
	
}
