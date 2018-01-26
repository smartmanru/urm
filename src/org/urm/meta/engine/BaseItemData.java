package org.urm.meta.engine;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.EngineTransaction;
import org.urm.engine.properties.PropertyController;
import org.urm.engine.properties.PropertySet;
import org.urm.engine.storage.BaseRepository;
import org.urm.engine.storage.RemoteFolder;
import org.urm.meta.env.MetaEnvServerNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class BaseItemData extends PropertyController {

	public BaseItem item;
	public BaseRepository repo;
	public MetaEnvServerNode serverNode;

	public String BASENAME;
	public String BASEVERSION;
	
	public DBEnumBaseSrcType BASESRC_TYPE;
	public DBEnumBaseSrcFormatType BASESRCFORMAT_TYPE;
	public String SRCFILE;
	public String SRCSTOREDIR;
	public String INSTALLPATH;
	public String INSTALLLINK;
	
	public List<String> dependencies;
	public Map<String,String> compatibilityMap;
	
	public boolean adm;
	public DBEnumOSType OS_TYPE;
	public DBEnumServerAccessType SERVERACCESS_TYPE;
	public Charset charset;

	public BaseItemData( BaseItem item , BaseRepository repo ) {
		super( null , "base" );
		this.item = item;
		this.repo = repo;
		create();
	}

	public BaseItemData( BaseItem item , BaseRepository repo , MetaEnvServerNode serverNode ) {
		super( serverNode , "final" );
		this.item = item;
		this.repo = repo;
		this.serverNode = serverNode;
		create();
	}

	@Override
	public String getName() {
		return( "server-base-item-data" );
	}
	
	public void create() {
		compatibilityMap = new HashMap<String,String>();
		dependencies = new LinkedList<String>();
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
		BASENAME = super.getStringPropertyRequired( action , "name" );
		BASEVERSION = super.getStringPropertyRequired( action , "version" );
		
		String TYPE = super.getStringPropertyRequired( action , "type" );
		BASESRC_TYPE = DBEnumBaseSrcType.getValue( TYPE , false );
		adm = super.getBooleanProperty( action , "adminstall" );
		
		String OSTYPE = super.getStringPropertyRequired( action , "ostype" );
		OS_TYPE = DBEnumOSType.getValue( OSTYPE , false );

		String CHARSET = super.getStringProperty( action , "charset" );
		if( !CHARSET.isEmpty() ) {
			charset = Charset.forName( CHARSET );
			if( charset == null )
				action.exit1( _Error.UnknownSystemFilesCharset1 , "unknown system files charset=" + CHARSET , CHARSET );
		}
		
		String SERVERTYPE = super.getStringProperty( action , "server-accesstype" );
		SERVERACCESS_TYPE = DBEnumServerAccessType.getValue( SERVERTYPE , false );
		
		// type properties
		if( isArchiveLink() )
			scatterArchiveLink( action );
		else
		if( isArchiveDirect() )
			scatterArchiveDirect( action );
		else
		if( isNoDist() )
			scatterNoDist( action );
		else
		if( isInstaller() )
			scatterInstaller( action );
		
		super.finishProperties( action );
	}

	public void create( ActionBase action ) throws Exception {
		super.initCreateStarted( null );
		BASENAME = "";
		BASEVERSION = "";
		
		SRCFILE = "";
		SRCSTOREDIR = "";
		INSTALLPATH = "";
		INSTALLLINK = "";
		
		adm = false;
		super.initFinished();
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		PropertySet parent = ( serverNode == null )? null : serverNode.getProperties();
		if( !super.initCreateStarted( parent ) )
			return;

		super.loadFromNodeAttributes( action , root , false );
		scatterProperties( action );
		
		loadCompatibility( action , root );
		loadDependencies( action , root );
		
		super.initFinished();
	}
	
	private void loadCompatibility( ActionBase action , Node node ) throws Exception {
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

	private void scatterArchiveLink( ActionBase action ) throws Exception {
		BASESRCFORMAT_TYPE = DBEnumBaseSrcFormatType.getValue( super.getStringPropertyRequired( action , "srcformat" ) , false );
		SRCFILE = super.getPathPropertyRequired( action , "srcfile" );
		SRCSTOREDIR = super.getPathPropertyRequired( action , "srcstoreddir" );
		INSTALLPATH = super.getPathPropertyRequired( action , "installpath" );
		INSTALLLINK = super.getPathPropertyRequired( action , "installlink" );
	}
	
	private void scatterArchiveDirect( ActionBase action ) throws Exception {
		BASESRCFORMAT_TYPE = DBEnumBaseSrcFormatType.getValue( super.getStringPropertyRequired( action , "srcformat" ) , false );
		SRCFILE = super.getPathPropertyRequired( action , "srcfile" );
		SRCSTOREDIR = super.getPathPropertyRequired( action , "srcstoreddir" );
		INSTALLPATH = super.getPathPropertyRequired( action , "installpath" );
	}

	private void scatterNoDist( ActionBase action ) throws Exception {
	}

	private void scatterInstaller( ActionBase action ) throws Exception {
		BASESRCFORMAT_TYPE = DBEnumBaseSrcFormatType.getValue( super.getStringPropertyRequired( action , "srcformat" ) , false );
		SRCFILE = super.getPathPropertyRequired( action , "srcfile" );
	}

	public String getItemPath( ActionBase action , String SRCFILE ) throws Exception {
		return( repo.getBaseItemPath( action , item.NAME , SRCFILE ) );
	}

	public RemoteFolder getFolder( ActionBase action ) throws Exception {
		return( repo.getBaseFolder( action , item.NAME ) );
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
	
	public void save( Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , BASENAME );
		Common.xmlSetElementAttr( doc , root , "version" , BASEVERSION );
	}

	public void setOptions( EngineTransaction transaction , String name , String version , DBEnumOSType ostype , DBEnumServerAccessType accessType , DBEnumBaseSrcType srcType , DBEnumBaseSrcFormatType srcFormat , String SRCFILE , String SRCSTOREDIR , String INSTALLPATH , String INSTALLLINK ) {
		this.BASENAME = name;
		this.BASEVERSION = version;
		
		this.OS_TYPE = ostype;
		this.SERVERACCESS_TYPE = accessType;
		this.BASESRC_TYPE = srcType;
		this.BASESRCFORMAT_TYPE = srcFormat;
		
		this.SRCFILE = SRCFILE;
		this.SRCSTOREDIR = SRCSTOREDIR;
		this.INSTALLPATH = INSTALLPATH;
		this.INSTALLLINK = INSTALLLINK;
	}
	
}
