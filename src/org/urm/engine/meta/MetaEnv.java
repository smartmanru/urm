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
	public String CONF_SECRETPROPERTYFILE;
	public String CONF_SECRETFILESPATH;
	public String CHATROOMFILE;
	public String KEYNAME;
	public String DB_AUTHFILE;
	public boolean PROD;
	
	// properties, affecting options
	public FLAG DB_AUTH;
	public FLAG OBSOLETE;
	public FLAG SHOWONLY;
	public FLAG BACKUP;
	public FLAG CONF_DEPLOY;
	public FLAG CONF_KEEPALIVE;

	// properties
	public String PROPERTY_ID;
	public String PROPERTY_BASELINE;
	public String PROPERTY_REDISTPATH;
	public String PROPERTY_DISTR_USELOCAL;
	public String PROPERTY_DISTR_HOSTLOGIN;
	public String PROPERTY_DISTR_PATH;
	public String PROPERTY_UPGRADE_PATH;
	public String PROPERTY_CONF_SECRETPROPERTYFILE;
	public String PROPERTY_CONF_SECRETFILESPATH;
	public String PROPERTY_CHATROOMFILE;
	public String PROPERTY_KEYNAME;
	public String PROPERTY_DB_AUTHFILE;
	public String PROPERTY_PROD;
	
	// properties, affecting options
	public String PROPERTY_DB_AUTH;
	public String PROPERTY_OBSOLETE;
	public String PROPERTY_SHOWONLY;
	public String PROPERTY_BACKUP;
	public String PROPERTY_CONF_DEPLOY;
	public String PROPERTY_CONF_KEEPALIVE;
	
	List<MetaEnvDC> originalList;
	Map<String,MetaEnvDC> dcMap;
	
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
	
	public MetaEnv copy( ActionBase action , Meta meta ) throws Exception {
		MetaEnv r = new MetaEnv( meta );
		r.initCopyStarted( this , meta.product.getProperties() );
		
		for( MetaEnvDC dc : originalList ) {
			MetaEnvDC rdc = dc.copy( action , meta , this );
			r.addDC( rdc );
		}
		
		r.scatterSystemProperties( action );
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
		if( !super.initCreateStarted( meta.product.getProperties() ) )
			return;

		loadProperties( action , root );
		loadDatacenters( action , root );
		resolveLinks( action );
		
		super.initFinished();
	}
	
	private void loadProperties( ActionBase action , Node node ) throws Exception {
		secretProperties = new PropertySet( "secret" , meta.product.getProperties() );
		properties = new PropertySet( "env" , secretProperties );
		properties.loadFromNodeAttributes( node );
		
		CONF_SECRETFILESPATH = properties.getSystemPathProperty( "configuration-secretfilespath" , "" , action.session.execrc );
		
		HiddenFiles hidden = action.artefactory.getHiddenFiles();
		String propFile = hidden.getSecretPropertyFile( action , CONF_SECRETFILESPATH );
		
		boolean loadProps = false;
		if( !propFile.isEmpty() ) {
			loadProps = ( action.shell.checkFileExists( action , propFile ) )? true : false;
			if( !loadProps )
				missingSecretProperties = true;
		}
			
		scatterSystemProperties( action );
		
		if( loadProps ) {
			loadSecretProperties( action );
			properties.loadFromNodeElements( node );
			properties.resolveRawProperties();
		}
	}

	private void loadSecretProperties( ActionBase action ) throws Exception {
		HiddenFiles hidden = action.artefactory.getHiddenFiles();
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
	
	private void scatterSystemProperties( ActionBase action ) throws Exception {
		ID = properties.getSystemRequiredStringProperty( "id" );
		action.trace( "load properties of env=" + ID );
		
		BASELINE = properties.getSystemStringProperty( "configuration-baseline" , "" );
		REDISTPATH = properties.getSystemPathProperty( "redist-path" , meta.product.CONFIG_REDISTPATH , action.session.execrc );
		DISTR_USELOCAL = properties.getSystemBooleanProperty( "distr-use-local" , true );
		if( DISTR_USELOCAL )
			DISTR_HOSTLOGIN = action.context.account.getFullName();
		else
			DISTR_HOSTLOGIN = properties.getSystemStringProperty( "distr-hostlogin" , meta.product.CONFIG_DISTR_HOSTLOGIN );
		
		DISTR_PATH = properties.getSystemPathProperty( "distr-path" , meta.product.CONFIG_DISTR_PATH , action.session.execrc );
		UPGRADE_PATH = properties.getSystemPathProperty( "upgrade-path" , meta.product.CONFIG_UPGRADE_PATH , action.session.execrc );
		CHATROOMFILE = properties.getSystemPathProperty( "chatroomfile" , "" , action.session.execrc );
		KEYNAME = properties.getSystemPathProperty( "keyname" , "" , action.session.execrc );
		DB_AUTHFILE = properties.getSystemPathProperty( "db-authfile" , "" , action.session.execrc );
		PROD = properties.getSystemBooleanProperty( "prod" , false );

		// affect runtime options
		DB_AUTH = getOptionFlag( action , "db-auth" );
		OBSOLETE = getOptionFlag( action , "obsolete" );
		SHOWONLY = getOptionFlag( action , "showonly" );
		BACKUP = getOptionFlag( action , "backup" );
		CONF_DEPLOY = getOptionFlag( action , "configuration-deploy" );
		CONF_KEEPALIVE = getOptionFlag( action , "configuration-keepalive" );

		properties.finishRawProperties();
	}

	private FLAG getOptionFlag( ActionBase action , String envParam ) throws Exception {
		String value = properties.getSystemStringProperty( envParam , null );
		
		FLAG retval;
		if( value == null || value.isEmpty() )
			retval = FLAG.DEFAULT;
		else {
			if( Common.getBooleanValue( value ) )
				retval = FLAG.YES;
			else
				retval = FLAG.NO;
		}
		
		return( retval );
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
	
}
