package ru.egov.urm.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.PropertySet;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.Metadata.VarSERVERTYPE;
import ru.egov.urm.storage.BaseRepository;
import ru.egov.urm.storage.RemoteFolder;

public class MetaFapBase {

	public enum VarBASESRCTYPE {
		LINUX_ARCHIVE_LINK ,
		LINUX_ARCHIVE_DIRECT
	};
	
	public enum VarBASESRCFORMAT {
		TARGZ_SINGLEDIR
	};
	
	public BaseRepository repo;
	public PropertySet properties;
	public String ID;
	public VarBASESRCTYPE type;
	public boolean adm;
	public VarSERVERTYPE serverType;

	public VarBASESRCFORMAT srcFormat;
	public String SRCFILE;
	public String SRCSTOREDIR;
	public String INSTALLPATH;
	public String INSTALLLINK;
	
	public List<String> dependencies;
	public Map<String,String> compatibilityMap;
	
	public MetaFapBase( BaseRepository repo ) {
		this.repo = repo;
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
		PropertySet resolveProps = new PropertySet( "resolve" , serverNode.server.base.properties );
		resolveProps.copyProperties( serverNode.properties );
		
		PropertySet metaAttrs = new PropertySet( "meta" , resolveProps );
		metaAttrs.loadFromAttributes( action , node );
		scatterVariables( action , metaAttrs );
		
		properties = new PropertySet( "final" , null );
		properties.copyProperties( metaAttrs );
		properties.loadFromElements( action , node );

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

	public boolean isLinuxArchiveLink() {
		if( type == VarBASESRCTYPE.LINUX_ARCHIVE_LINK )
			return( true );
		return( false );
	}
	
	public boolean isLinuxArchiveDirect() {
		if( type == VarBASESRCTYPE.LINUX_ARCHIVE_DIRECT )
			return( true );
		return( false );
	}
	
	private void scatterVariables( ActionBase action , PropertySet props ) throws Exception {
		List<String> systemProps = new LinkedList<String>();

		// unified properties
		ID = props.getSystemRequiredProperty( action , "id" , systemProps );
		String TYPE = props.getSystemRequiredProperty( action , "type" , systemProps );
		type = getType( action , TYPE );
		adm = props.getSystemBooleanProperty( action , "adminstall" , false , systemProps );
		String SERVERTYPE = props.getSystemRequiredProperty( action , "servertype" , systemProps ); 
		serverType = action.meta.getServerType( action , SERVERTYPE );
		
		// type properties
		if( isLinuxArchiveLink() )
			scatterLinuxArchiveLink( action , props , systemProps );
		else
		if( isLinuxArchiveDirect() )
			scatterLinuxArchiveDirect( action , props , systemProps );
		else
			action.exitUnexpectedState();
		
		props.checkUnexpected( action , systemProps );
	}

	private void scatterLinuxArchiveLink( ActionBase action , PropertySet props , List<String> systemProps ) throws Exception {
		srcFormat = getSrcFormat( action , props.getSystemRequiredProperty( action , "srcformat" , systemProps ) );
		SRCFILE = props.getSystemRequiredProperty( action , "srcfile" , systemProps );
		SRCSTOREDIR = props.getSystemRequiredProperty( action , "srcstoreddir" , systemProps );
		INSTALLPATH = props.getSystemRequiredProperty( action , "installpath" , systemProps );
		INSTALLLINK = props.getSystemRequiredProperty( action , "installlink" , systemProps );
	}
	
	private void scatterLinuxArchiveDirect( ActionBase action , PropertySet props , List<String> systemProps ) throws Exception {
		srcFormat = getSrcFormat( action , props.getSystemRequiredProperty( action , "srcformat" , systemProps ) );
		SRCFILE = props.getSystemRequiredProperty( action , "srcfile" , systemProps );
		SRCSTOREDIR = props.getSystemRequiredProperty( action , "srcstoreddir" , systemProps );
		INSTALLPATH = props.getSystemRequiredProperty( action , "installpath" , systemProps );
	}

	public String getItemPath( ActionBase action , String SRCFILE ) throws Exception {
		return( repo.getBaseItemPath( action , ID , SRCFILE ) );
	}

	public RemoteFolder getFolder( ActionBase action ) throws Exception {
		return( repo.getBaseFolder( action , ID ) );
	}
}
