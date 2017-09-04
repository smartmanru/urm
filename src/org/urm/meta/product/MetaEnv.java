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
import org.urm.common.action.CommandOption.FLAG;
import org.urm.engine.EngineTransaction;
import org.urm.engine.storage.HiddenFiles;
import org.urm.meta.ProductMeta;
import org.urm.meta.EngineRef;
import org.urm.meta.engine.ServerAccountReference;
import org.urm.meta.engine.ServerHostAccount;
import org.urm.meta.Types;
import org.urm.meta.Types.*;
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
	public EngineRef<MetaEnv> baselineEnvRef;
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
	public VarENVTYPE envType;
	
	// properties, affecting options
	public FLAG DB_AUTH;
	public FLAG OBSOLETE;
	public FLAG SHOWONLY;
	public FLAG BACKUP;
	public FLAG CONF_DEPLOY;
	public FLAG CONF_KEEPALIVE;

	private List<MetaEnvSegment> originalList;
	private Map<String,MetaEnvSegment> sgMap;

	// properties
	public static String PROPERTY_ID = "id";
	public static String PROPERTY_ENVTYPE = "envtype";
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

	public static String ELEMENT_SEGMENT = "segment";
	
	public MetaEnv( ProductMeta storage , MetaProductSettings settings , Meta meta ) {
		super( storage , settings , "env" );
		this.meta = meta;
		originalList = new LinkedList<MetaEnvSegment>();
		sgMap = new HashMap<String,MetaEnvSegment>();
		baselineEnvRef = new EngineRef<MetaEnv>();
	}
	
	@Override
	public String getName() {
		return( ID );
	}
	
	@Override
	public boolean isValid() {
		if( super.isLoadFailed() )
			return( false );
		return( true );
	}
	
	@Override
	public void scatterProperties( ActionBase action ) throws Exception {
		ID = super.getStringPropertyRequired( action , PROPERTY_ID );
		action.trace( "load properties of env=" + ID + " ..." );
		
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
		String ENVTYPE = super.getStringProperty( action , PROPERTY_ENVTYPE , Common.getEnumLower( VarENVTYPE.DEVELOPMENT ) );
		envType = Types.getEnvType( ENVTYPE , true );

		// affect runtime options
		DB_AUTH = super.getOptionProperty( action , PROPERTY_DB_AUTH );
		OBSOLETE = super.getOptionProperty( action , PROPERTY_OBSOLETE );
		SHOWONLY = super.getOptionProperty( action , PROPERTY_SHOWONLY );
		BACKUP = super.getOptionProperty( action , PROPERTY_BACKUP );
		CONF_DEPLOY = super.getOptionProperty( action , PROPERTY_CONF_DEPLOY );
		CONF_KEEPALIVE = super.getOptionProperty( action , PROPERTY_CONF_KEEPALIVE );
		super.finishRawProperties();
		
		if( !isValid() )
			action.exit0( _Error.InconsistentVersionAttributes0 , "inconsistent version attributes" );
	}

	public void createEnv( ActionBase action , String ID , VarENVTYPE envType ) throws Exception {
		this.ID = ID;
		this.envType = envType;
		createProperties( action );
	}

	public MetaEnv copy( ActionBase action , Meta meta ) throws Exception {
		MetaProductSettings product = meta.getProductSettings( action );
		MetaEnv r = new MetaEnv( meta.getStorage( action ) , product , meta );
		r.initCopyStarted( this , product.getProperties() );
		
		for( MetaEnvSegment sg : originalList ) {
			MetaEnvSegment rsg = sg.copy( action , meta , r );
			r.addSG( rsg );
		}
		
		r.scatterProperties( action );
		r.initFinished();
		return( r );
	}

	public boolean isProd() {
		return( envType == VarENVTYPE.PRODUCTION );
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
		loadSegments( action , root );
		resolveLinks( action );
		
		super.initFinished();
	}
	
	private void loadProperties( ActionBase action , Node node ) throws Exception {
		super.loadFromNodeAttributes( action , node , false );
		
		CONF_SECRETFILESPATH = super.getPathProperty( action , PROPERTY_CONF_SECRETFILESPATH );
		
		HiddenFiles hidden = action.artefactory.getHiddenFiles( meta );
		String propFile = hidden.getSecretPropertyFile( action , CONF_SECRETFILESPATH );
		
		missingSecretProperties = false;
		if( !propFile.isEmpty() ) {
			if( !action.shell.checkFileExists( action , propFile ) )
				missingSecretProperties = true;
		}
			
		scatterProperties( action );
		if( !missingSecretProperties )
			loadSecretProperties( action );
		
		super.loadFromNodeElements( action , node , true );
		super.resolveRawProperties();
	}

	private void loadSecretProperties( ActionBase action ) throws Exception {
		HiddenFiles hidden = action.artefactory.getHiddenFiles( meta );
		String propFile = hidden.getSecretPropertyFile( action , CONF_SECRETFILESPATH );
		if( propFile.isEmpty() )
			return;
		
		secretProperties.loadFromPropertyFile( propFile , action.session.execrc , true );
		secretProperties.resolveRawProperties();
	}
	
	private void createProperties( ActionBase action ) throws Exception {
		MetaProductSettings product = meta.getProductSettings( action );
		secretProperties = new PropertySet( "secret" , product.getProperties() );
		if( !super.initCreateStarted( secretProperties ) )
			return;

		super.setStringProperty( PROPERTY_ID , ID );
		super.setStringProperty( PROPERTY_ENVTYPE , Common.getEnumLower( envType ) );
		super.finishProperties( action );
		super.initFinished();
		
		scatterProperties( action );
	}
	
	private void loadSegments( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , ELEMENT_SEGMENT );
		if( items == null )
			return;

		for( Node sgnode : items ) {
			MetaEnvSegment sg = new MetaEnvSegment( meta , this );
			sg.load( action , sgnode );
			addSG( sg );
		}
	}

	private void addSG( MetaEnvSegment sg ) {
		originalList.add( sg );
		sgMap.put( sg.NAME , sg );
	}
	
	private void resolveLinks( ActionBase action ) throws Exception {
		baselineEnvRef.set( meta.getEnv( action , BASELINE ) );
		for( MetaEnvSegment sg : originalList )
			sg.resolveLinks( action );
	}
	
	public MetaEnvSegment findSegment( String name ) {
		return( sgMap.get( name ) );
	}

	public MetaEnvSegment getSG( ActionBase action , String name ) throws Exception {
		MetaEnvSegment sg = sgMap.get( name );
		if( sg == null )
			action.exit1( _Error.UnknownSegment1 , "unknown segment=" + name , name );
		return( sg );
	}

	public String[] getSegmentNames() {
		return( Common.getSortedKeys( sgMap ) );
	}
	
	public MetaEnvSegment[] getSegments() {
		return( originalList.toArray( new MetaEnvSegment[0] ) );
	}
	
	public boolean isMultiSG( ActionBase action ) throws Exception {
		return( originalList.size() > 1 );
	}
	
	public MetaEnvSegment getMainSG( ActionBase action ) throws Exception {
		if( originalList.size() == 0 )
			action.exit0( _Error.NoSegmentDefined0 , "no segment defined" );
		if( originalList.size() > 1 )
			action.exitUnexpectedState();
		return( originalList.get( 0 ) );
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		super.saveSplit( doc , root );
		for( MetaEnvSegment sg : originalList ) {
			Element sgElement = Common.xmlCreateElement( doc , root , ELEMENT_SEGMENT );
			sg.save( action , doc , sgElement );
		}
	}
	
	public void createSegment( EngineTransaction transaction , MetaEnvSegment sg ) {
		addSG( sg );
	}
	
	public void deleteSegment( EngineTransaction transaction , MetaEnvSegment sg ) {
		int index = originalList.indexOf( sg );
		if( index < 0 )
			return;
		
		originalList.remove( index );
		sgMap.remove( sg.NAME );
	}
	
	public void setProperties( EngineTransaction transaction , PropertySet props , boolean system ) throws Exception {
		super.updateProperties( transaction , props , system );
		scatterProperties( transaction.getAction() );
	}
	
	public void setBaseline( EngineTransaction transaction , String baselineEnv ) throws Exception {
		super.setSystemStringProperty( PROPERTY_BASELINE , baselineEnv );
	}
	
	public void setOffline( EngineTransaction transaction , boolean offline ) throws Exception {
		super.setSystemBooleanProperty( PROPERTY_OFFLINE , offline );
	}
	
	public boolean isOffline() {
		return( OFFLINE );
	}

	public boolean isBroken() {
		return( super.isLoadFailed() );
	}

	public void getApplicationReferences( ServerHostAccount account , List<ServerAccountReference> refs ) {
		for( MetaEnvSegment sg : originalList )
			sg.getApplicationReferences( account , refs );
	}

	public void deleteHostAccount( EngineTransaction transaction , ServerHostAccount account ) throws Exception {
		for( MetaEnvSegment sg : originalList )
			sg.deleteHostAccount( transaction , account );
	}

	public boolean isConfUsed( MetaDistrConfItem item ) {
		for( MetaEnvSegment sg : originalList ) {
			if( sg.isConfUsed( item ) )
				return( true );
		}
		return( false );
	}
	
}
