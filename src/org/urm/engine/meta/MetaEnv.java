package org.urm.engine.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertyController;
import org.urm.common.PropertySet;
import org.urm.common.action.CommandVar.FLAG;
import org.urm.engine.ServerTransaction;
import org.urm.engine.storage.HiddenFiles;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaEnv extends PropertyController {

	public Meta meta;

	PropertySet secretProperties;
	
	public boolean missingSecretProperties = false; 
	
	public String ID;
	private String BASELINE;
	public String REDISTPATH;
	public boolean DISTR_USELOCAL;
	public String DISTR_HOSTLOGIN;
	public String DISTR_PATH;
	public String UPGRADE_PATH;
	public String CONF_SECRETFILESPATH;
	public String CHATROOMFILE;
	public String KEYFILE;
	public String DB_AUTHFILE;
	public boolean PROD;
	
	// properties, affecting options
	public FLAG DB_AUTH;
	public FLAG OBSOLETE;
	public FLAG SHOWONLY;
	public FLAG BACKUP;
	public FLAG CONF_DEPLOY;
	public FLAG CONF_KEEPALIVE;

	List<MetaEnvDC> originalList;
	Map<String,MetaEnvDC> dcMap;

	// properties
	public static String PROPERTY_ID = "id";
	public static String PROPERTY_PROD = "prod";
	public static String PROPERTY_BASELINE = "configuration-baseline";
	public static String PROPERTY_REDISTPATH = "redist-path";
	public static String PROPERTY_DISTR_USELOCAL = "distr-use-local";
	public static String PROPERTY_DISTR_HOSTLOGIN = "distr-hostlogin";
	public static String PROPERTY_DISTR_PATH = "distr-path";
	public static String PROPERTY_UPGRADE_PATH = "upgrade-path";
	public static String PROPERTY_CONF_SECRETFILESPATH = "secretfiles";
	public static String PROPERTY_CHATROOMFILE = "chatroomfile";
	public static String PROPERTY_KEYFILE = "keyfile";
	public static String PROPERTY_DB_AUTHFILE = "db-authfile";
	
	// properties, affecting options
	public static String PROPERTY_DB_AUTH = "db-auth";
	public static String PROPERTY_OBSOLETE = "obsolete";
	public static String PROPERTY_SHOWONLY = "showonly";
	public static String PROPERTY_BACKUP = "backup";
	public static String PROPERTY_CONF_DEPLOY = "configuration-deploy";
	public static String PROPERTY_CONF_KEEPALIVE = "configuration-keepalive";
	
	public MetaEnv( Meta meta ) {
		super( "env" );
		this.meta = meta;
		originalList = new LinkedList<MetaEnvDC>();
		dcMap = new HashMap<String,MetaEnvDC>();
	}
	
	@Override
	public boolean isValid() {
		if( super.isLoadFailed() )
			return( false );
		return( true );
	}
	
	@Override
	public void scatterProperties( ActionBase action ) throws Exception {
		ID = properties.getSystemRequiredStringProperty( PROPERTY_ID );
		action.trace( "load properties of env=" + ID );
		
		MetaProductSettings product = meta.getProduct( action );
		BASELINE = properties.getSystemStringProperty( PROPERTY_BASELINE , "" );
		REDISTPATH = properties.getSystemPathProperty( PROPERTY_REDISTPATH , product.CONFIG_REDISTPATH , action.session.execrc );
		DISTR_USELOCAL = properties.getSystemBooleanProperty( PROPERTY_DISTR_USELOCAL , true );
		if( DISTR_USELOCAL )
			DISTR_HOSTLOGIN = action.context.account.getFullName();
		else
			DISTR_HOSTLOGIN = properties.getSystemStringProperty( PROPERTY_DISTR_HOSTLOGIN , product.CONFIG_DISTR_HOSTLOGIN );
		
		DISTR_PATH = properties.getSystemPathProperty( PROPERTY_DISTR_PATH , product.CONFIG_DISTR_PATH , action.session.execrc );
		UPGRADE_PATH = properties.getSystemPathProperty( PROPERTY_UPGRADE_PATH , product.CONFIG_UPGRADE_PATH , action.session.execrc );
		CONF_SECRETFILESPATH = properties.getSystemPathProperty( PROPERTY_CONF_SECRETFILESPATH , "" , action.session.execrc );
		CHATROOMFILE = properties.getSystemPathProperty( PROPERTY_CHATROOMFILE , "" , action.session.execrc );
		KEYFILE = properties.getSystemPathProperty( PROPERTY_KEYFILE , "" , action.session.execrc );
		DB_AUTHFILE = properties.getSystemPathProperty( PROPERTY_DB_AUTHFILE , "" , action.session.execrc );
		PROD = properties.getSystemBooleanProperty( PROPERTY_PROD , false );

		// affect runtime options
		DB_AUTH = properties.getSystemOptionProperty( PROPERTY_DB_AUTH );
		OBSOLETE = properties.getSystemOptionProperty( PROPERTY_OBSOLETE );
		SHOWONLY = properties.getSystemOptionProperty( PROPERTY_SHOWONLY );
		BACKUP = properties.getSystemOptionProperty( PROPERTY_BACKUP );
		CONF_DEPLOY = properties.getSystemOptionProperty( PROPERTY_CONF_DEPLOY );
		CONF_KEEPALIVE = properties.getSystemOptionProperty( PROPERTY_CONF_KEEPALIVE );
		properties.finishRawProperties();
		
		if( !isValid() )
			action.exit0( _Error.InconsistentVersionAttributes0 , "inconsistent version attributes" );
	}

	@Override
	public void gatherProperties( ActionBase action ) throws Exception {
		if( !isValid() )
			action.exit0( _Error.InconsistentVersionAttributes0 , "inconsistent version attributes" );
	
		properties.setOriginalStringProperty( PROPERTY_ID , ID );
		properties.setOriginalStringProperty( PROPERTY_BASELINE , BASELINE );
		properties.setOriginalPathProperty( PROPERTY_REDISTPATH , REDISTPATH );
		properties.setOriginalBooleanProperty( PROPERTY_DISTR_USELOCAL , DISTR_USELOCAL );
		properties.setOriginalStringProperty( PROPERTY_DISTR_HOSTLOGIN , DISTR_HOSTLOGIN );
		properties.setOriginalPathProperty( PROPERTY_DISTR_PATH , DISTR_PATH );
		properties.setOriginalPathProperty( PROPERTY_UPGRADE_PATH , UPGRADE_PATH );
		properties.setOriginalPathProperty( PROPERTY_CONF_SECRETFILESPATH , CONF_SECRETFILESPATH );
		properties.setOriginalStringProperty( PROPERTY_CHATROOMFILE , CHATROOMFILE );
		properties.setOriginalPathProperty( PROPERTY_KEYFILE , KEYFILE );
		properties.setOriginalPathProperty( PROPERTY_DB_AUTHFILE , DB_AUTHFILE );
		properties.setOriginalBooleanProperty( PROPERTY_PROD , PROD );
		
		// properties, affecting options
		properties.setOriginalBooleanProperty( PROPERTY_DB_AUTH , DB_AUTH );
		properties.setOriginalBooleanProperty( PROPERTY_OBSOLETE , OBSOLETE );
		properties.setOriginalBooleanProperty( PROPERTY_SHOWONLY , SHOWONLY );
		properties.setOriginalBooleanProperty( PROPERTY_BACKUP , BACKUP );
		properties.setOriginalBooleanProperty( PROPERTY_CONF_DEPLOY , CONF_DEPLOY );
		properties.setOriginalBooleanProperty( PROPERTY_CONF_KEEPALIVE , CONF_KEEPALIVE );
		properties.finishRawProperties();
	}
	
	
	public void createEnv( ActionBase action ) throws Exception {
		createProperties( action );
	}

	public MetaEnv copy( ActionBase action , Meta meta ) throws Exception {
		MetaEnv r = new MetaEnv( meta );
		MetaProductSettings product = meta.getProduct( action );
		r.initCopyStarted( this , product.getProperties() );
		
		for( MetaEnvDC dc : originalList ) {
			MetaEnvDC rdc = dc.copy( action , meta , this );
			r.addDC( rdc );
		}
		
		r.scatterProperties( action );
		r.initFinished();
		return( r );
	}

	public boolean hasBaseline( ActionBase action ) throws Exception {
		if( BASELINE.isEmpty() )
			return( false );
		return( true );
	}
	
	public String getBaselineEnv( ActionBase action ) throws Exception {
		return( BASELINE );
	}
	
	public String getBaselineFile( ActionBase action ) throws Exception {
		return( BASELINE + ".xml" );
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		MetaProductSettings product = meta.getProduct( action );
		secretProperties = new PropertySet( "secret" , product.getProperties() );
		if( !super.initCreateStarted( secretProperties ) )
			return;

		loadProperties( action , root );
		loadDatacenters( action , root );
		resolveLinks( action );
		
		super.initFinished();
	}
	
	private void loadProperties( ActionBase action , Node node ) throws Exception {
		properties.loadFromNodeAttributes( node );
		
		CONF_SECRETFILESPATH = properties.getSystemPathProperty( "configuration-secretfilespath" , "" , action.session.execrc );
		
		HiddenFiles hidden = action.artefactory.getHiddenFiles( meta );
		String propFile = hidden.getSecretPropertyFile( action , CONF_SECRETFILESPATH );
		
		boolean loadProps = false;
		if( !propFile.isEmpty() ) {
			loadProps = ( action.shell.checkFileExists( action , propFile ) )? true : false;
			if( !loadProps )
				missingSecretProperties = true;
		}
			
		scatterProperties( action );
		
		if( loadProps ) {
			loadSecretProperties( action );
			properties.loadFromNodeElements( node );
			properties.resolveRawProperties();
		}
	}

	private void loadSecretProperties( ActionBase action ) throws Exception {
		HiddenFiles hidden = action.artefactory.getHiddenFiles( meta );
		String propFile = hidden.getSecretPropertyFile( action , CONF_SECRETFILESPATH );
		if( propFile.isEmpty() )
			return;
		
		secretProperties.loadFromPropertyFile( propFile , action.session.execrc );
		secretProperties.resolveRawProperties();
	}
	
	public String[] getPropertyList( ActionBase action ) throws Exception {
		return( properties.getRunningProperties() );
	}

	public String getPropertyValue( ActionBase action , String var ) throws Exception {
		return( properties.getPropertyAny( var ) );
	}
	
	private void createProperties( ActionBase action ) throws Exception {
		MetaProductSettings product = meta.getProduct( action );
		secretProperties = new PropertySet( "secret" , product.getProperties() );
		if( !super.initCreateStarted( secretProperties ) )
			return;

		gatherProperties( action );
		super.finishProperties( action );
		super.initFinished();
		
		scatterProperties( action );
	}
	
	private void loadDatacenters( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , "datacenter" );
		if( items == null )
			return;

		boolean loadProps = ( secretProperties == null )? false : true;
		for( Node dcnode : items ) {
			MetaEnvDC dc = new MetaEnvDC( meta , this );
			dc.load( action , dcnode , loadProps );
			addDC( dc );
		}
	}

	private void addDC( MetaEnvDC dc ) {
		originalList.add( dc );
		dcMap.put( dc.NAME , dc );
	}
	
	private void resolveLinks( ActionBase action ) throws Exception {
		for( MetaEnvDC dc : originalList )
			dc.resolveLinks( action );
	}
	
	public MetaEnvDC findDC( String name ) {
		return( dcMap.get( name ) );
	}

	public MetaEnvDC getDC( ActionBase action , String name ) throws Exception {
		MetaEnvDC dc = dcMap.get( name );
		if( dc == null )
			action.exit1( _Error.UnknownDatacenter1 , "unknown datacenter=" + name , name );
		return( dc );
	}

	public List<MetaEnvDC> getOriginalDCList( ActionBase action ) throws Exception {
		return( originalList );
	}
	
	public Map<String,MetaEnvDC> getDCMap( ActionBase action ) throws Exception {
		return( dcMap );
	}

	public boolean isMultiDC( ActionBase action ) throws Exception {
		return( originalList.size() > 1 );
	}
	
	public MetaEnvDC getMainDC( ActionBase action ) throws Exception {
		if( originalList.size() == 0 )
			action.exit0( _Error.NoDatacenterDefined0 , "no datacenter defined" );
		if( originalList.size() > 1 )
			action.exitUnexpectedState();
		return( originalList.get( 0 ) );
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		if( !super.isLoaded() )
			return;

		properties.saveSplit( doc , root );
		for( MetaEnvDC dc : originalList ) {
			Element dcElement = Common.xmlCreateElement( doc , root , "datacenter" );
			dc.save( action , doc , dcElement );
		}
	}
	
	public void createDC( ServerTransaction transaction , MetaEnvDC dc ) {
		addDC( dc );
	}
	
	public void deleteDC( ServerTransaction transaction , MetaEnvDC dc ) {
		int index = originalList.indexOf( dc );
		if( index < 0 )
			return;
		
		originalList.remove( index );
		dcMap.remove( dc.NAME );
	}
	
	public void setProperties( ServerTransaction transaction , PropertySet props , boolean system ) throws Exception {
		super.updateProperties( transaction , props , system );
		scatterProperties( transaction.getAction() );
	}
	
}
