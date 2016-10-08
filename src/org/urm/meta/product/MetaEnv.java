package org.urm.meta.product;

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
import org.urm.meta.ServerProductMeta;
import org.urm.meta.ServerRef;
import org.urm.meta.engine.ServerAccountReference;
import org.urm.meta.engine.ServerHostAccount;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaEnv extends PropertyController {

	public Meta meta;

	PropertySet secretProperties;
	
	public boolean missingSecretProperties = false; 
	
	public String ID;
	public String BASELINE;
	public boolean OFFLINE;
	public ServerRef<MetaEnv> baselineEnvRef;
	public String REDISTWIN_PATH;
	public String REDISTLINUX_PATH;
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
	public static String PROPERTY_BASELINE = "baseenv";
	public static String PROPERTY_OFFLINE = "offline";
	public static String PROPERTY_REDISTWIN_PATH = "redist-win-path";
	public static String PROPERTY_REDISTLINUX_PATH = "redist-linux-path";
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

	public static String ELEMENT_DATACENTER = "datacenter";
	
	public MetaEnv( ServerProductMeta storage , Meta meta ) {
		super( storage , "env" );
		this.meta = meta;
		originalList = new LinkedList<MetaEnvDC>();
		dcMap = new HashMap<String,MetaEnvDC>();
		baselineEnvRef = new ServerRef<MetaEnv>();
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
		
		MetaProductSettings product = meta.getProductSettings( action );
		BASELINE = super.getStringProperty( action , PROPERTY_BASELINE );
		OFFLINE = super.getBooleanProperty( action , PROPERTY_OFFLINE , true );
		REDISTWIN_PATH = super.getPathProperty( action , PROPERTY_REDISTWIN_PATH , product.CONFIG_REDISTWIN_PATH );
		REDISTLINUX_PATH = super.getPathProperty( action , PROPERTY_REDISTLINUX_PATH , product.CONFIG_REDISTLINUX_PATH );
		DISTR_USELOCAL = super.getBooleanProperty( action , PROPERTY_DISTR_USELOCAL , true );
		if( DISTR_USELOCAL )
			DISTR_HOSTLOGIN = action.context.account.getFullName();
		else
			DISTR_HOSTLOGIN = super.getStringProperty( action , PROPERTY_DISTR_HOSTLOGIN , product.CONFIG_DISTR_HOSTLOGIN );
		
		DISTR_PATH = super.getPathProperty( action , PROPERTY_DISTR_PATH , product.CONFIG_DISTR_PATH );
		UPGRADE_PATH = super.getPathProperty( action , PROPERTY_UPGRADE_PATH , product.CONFIG_UPGRADE_PATH );
		CONF_SECRETFILESPATH = super.getPathProperty( action , PROPERTY_CONF_SECRETFILESPATH );
		CHATROOMFILE = super.getPathProperty( action , PROPERTY_CHATROOMFILE );
		KEYFILE = super.getPathProperty( action , PROPERTY_KEYFILE );
		DB_AUTHFILE = super.getPathProperty( action , PROPERTY_DB_AUTHFILE );
		PROD = super.getBooleanProperty( action , PROPERTY_PROD , false );

		// affect runtime options
		DB_AUTH = super.getOptionProperty( action , PROPERTY_DB_AUTH );
		OBSOLETE = super.getOptionProperty( action , PROPERTY_OBSOLETE );
		SHOWONLY = super.getOptionProperty( action , PROPERTY_SHOWONLY );
		BACKUP = super.getOptionProperty( action , PROPERTY_BACKUP );
		CONF_DEPLOY = super.getOptionProperty( action , PROPERTY_CONF_DEPLOY );
		CONF_KEEPALIVE = super.getOptionProperty( action , PROPERTY_CONF_KEEPALIVE );
		properties.finishRawProperties();
		
		if( !isValid() )
			action.exit0( _Error.InconsistentVersionAttributes0 , "inconsistent version attributes" );
	}

	public void createEnv( ActionBase action , String ID , boolean PROD ) throws Exception {
		this.ID = ID;
		this.PROD = PROD;
		createProperties( action );
	}

	public MetaEnv copy( ActionBase action , Meta meta ) throws Exception {
		MetaEnv r = new MetaEnv( meta.getStorage( action ) , meta );
		MetaProductSettings product = meta.getProductSettings( action );
		r.initCopyStarted( this , product.getProperties() );
		
		for( MetaEnvDC dc : originalList ) {
			MetaEnvDC rdc = dc.copy( action , meta , r );
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
		MetaProductSettings product = meta.getProductSettings( action );
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
		
		CONF_SECRETFILESPATH = super.getPathProperty( action , PROPERTY_CONF_SECRETFILESPATH );
		
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
		MetaProductSettings product = meta.getProductSettings( action );
		secretProperties = new PropertySet( "secret" , product.getProperties() );
		if( !super.initCreateStarted( secretProperties ) )
			return;

		super.setStringProperty( PROPERTY_ID , ID );
		super.setBooleanProperty( PROPERTY_PROD , PROD );
		super.finishProperties( action );
		super.initFinished();
		
		scatterProperties( action );
	}
	
	private void loadDatacenters( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , ELEMENT_DATACENTER );
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
		baselineEnvRef.set( meta.getEnv( action , BASELINE ) );
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
		properties.saveSplit( doc , root );
		for( MetaEnvDC dc : originalList ) {
			Element dcElement = Common.xmlCreateElement( doc , root , ELEMENT_DATACENTER );
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
	
	public void setBaseline( ServerTransaction transaction , String baselineEnv ) throws Exception {
		properties.setStringProperty( PROPERTY_BASELINE , baselineEnv );
	}
	
	public void setOffline( ServerTransaction transaction , boolean offline ) throws Exception {
		properties.setBooleanProperty( PROPERTY_OFFLINE , offline );
	}
	
	public boolean isOffline() {
		return( OFFLINE );
	}

	public boolean isBroken() {
		return( super.isLoadFailed() );
	}

	public void getApplicationReferences( ServerHostAccount account , List<ServerAccountReference> refs ) {
		for( MetaEnvDC dc : originalList )
			dc.getApplicationReferences( account , refs );
	}

	public void deleteHostAccount( ServerTransaction transaction , ServerHostAccount account ) throws Exception {
		for( MetaEnvDC dc : originalList )
			dc.deleteHostAccount( transaction , account );
	}
	
}
