package org.urm.meta.env;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.DataService;
import org.urm.engine.data.EngineInfrastructure;
import org.urm.engine.data.EngineResources;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.security.AuthResource;
import org.urm.meta.engine.AccountReference;
import org.urm.meta.engine.HostAccount;
import org.urm.meta.loader.EngineObject;
import org.urm.meta.loader.MatchItem;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.ProductMeta;

public class MetaEnv extends EngineObject {

	// table properties
	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_ENVTYPE = "envtype";
	public static String PROPERTY_BASELINE = "baseenv";
	public static String PROPERTY_OFFLINE = "offline";
	public static String PROPERTY_ENVKEY = "access";
	public static String PROPERTY_DISTR_REMOTE = "distr-remote";
	public static String PROPERTY_DISTR_HOSTLOGIN = "distr-hostlogin";
	public static String PROPERTY_DISTR_PATH = "distr-path";
	
	// object properties
	public static String PROPERTY_REDISTWIN_PATH = "redist-win-path";
	public static String PROPERTY_REDISTLINUX_PATH = "redist-linux-path";
	public static String PROPERTY_CHATROOM = "chatroom";
	public static String PROPERTY_DB_AUTHFILE = "db-authfile";
	public static String PROPERTY_DB_AUTH = "db-auth";
	public static String PROPERTY_SHOWONLY = "showonly";
	public static String PROPERTY_BACKUP = "backup";
	public static String PROPERTY_CONF_DEPLOY = "configuration-deploy";
	public static String PROPERTY_CONF_KEEPALIVE = "configuration-keepalive";

	public Meta meta;
	public ProductEnvs envs;

	// table data
	private ObjectProperties ops;
	public int ID;
	public Integer TRANSITION_META_ID;
	public boolean MATCHED;
	public String NAME;
	public String DESC;
	public DBEnumEnvType ENV_TYPE;
	private MatchItem BASELINE;
	public boolean OFFLINE;
	private MatchItem ENVKEY;
	public boolean DISTR_REMOTE;
	private MatchItem DISTR_ACCOUNT;
	public String DISTR_PATH;
	public int EV;
	
	// properties
	public boolean DBAUTH;
	public boolean SHOWONLY;
	public boolean BACKUP;
	public boolean CONF_DEPLOY;
	public boolean CONF_KEEPALIVE;
	public String DBAUTH_FILE;
	public String CHATROOM;
	public String REDISTWIN_PATH;
	public String REDISTLINUX_PATH;

	private Map<String,MetaEnvSegment> sgMap;
	private Map<Integer,MetaEnvSegment> sgMapById;
	private Map<String,MetaEnvDeployGroup> deployGroup;
	private Map<Integer,MetaEnvDeployGroup> deployGroupById;

	public MetaEnv( ProductMeta storage , Meta meta , ProductEnvs envs ) {
		super( null );
		this.meta = meta;
		this.envs = envs;
		
		ID = -1;
		EV = -1;
		MATCHED = false;
		deployGroup = new HashMap<String,MetaEnvDeployGroup>();
		deployGroupById = new HashMap<Integer,MetaEnvDeployGroup>(); 
		sgMap = new HashMap<String,MetaEnvSegment>();
		sgMapById = new HashMap<Integer,MetaEnvSegment>();
	}
	
	@Override
	public String getName() {
		return( NAME );
	}
	
	public MetaEnv copy( ProductMeta rstorage , Meta rmeta , ProductEnvs renvs , ObjectProperties rparent ) throws Exception {
		MetaEnv r = new MetaEnv( rstorage , rmeta , renvs );
		
		r.ops = ops.copy( rparent );
		
		r.ID = ID;
		r.TRANSITION_META_ID = TRANSITION_META_ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.ENV_TYPE = ENV_TYPE;
		r.BASELINE = MatchItem.copy( BASELINE );
		r.OFFLINE = OFFLINE;
		r.ENVKEY = MatchItem.copy( ENVKEY );
		r.DISTR_REMOTE = DISTR_REMOTE;
		r.DISTR_ACCOUNT = MatchItem.copy( DISTR_ACCOUNT );
		r.DISTR_PATH = DISTR_PATH;
		r.EV = EV;
		r.MATCHED = MATCHED;
		
		if( rmeta.isDraft() && r.isProd() )
			r.OFFLINE = true;

		for( MetaEnvDeployGroup group : deployGroupById.values() ) {
			MetaEnvDeployGroup rgroup = group.copy( rmeta , r );
			r.addDeployGroup( rgroup );
		}
		
		for( MetaEnvSegment sg : sgMap.values() ) {
			MetaEnvSegment rsg = sg.copy( rmeta , r );
			r.addSegment( rsg );
		}
		
		r.scatterExtraProperties();
		return( r );
	}

	public EngineProduct getEngineProduct() {
		return( meta.ep );
	}
	
	public void createSettings( ObjectProperties ops ) throws Exception {
		this.ops = ops;
	}
	
	public ObjectProperties getProperties() {
		return( ops );
	}
	
	public void setMatched( boolean matched ) {
		this.MATCHED = matched;
	}
	
	public void refreshPrimaryProperties() throws Exception {
		ops.clearProperties( DBEnumParamEntityType.ENV_PRIMARY );
		
		ops.setStringProperty( PROPERTY_NAME , NAME );
		ops.setStringProperty( PROPERTY_DESC , DESC );
		ops.setEnumProperty( PROPERTY_ENVTYPE , ENV_TYPE );
		ops.setBooleanProperty( PROPERTY_OFFLINE , OFFLINE );
		
		if( ENVKEY != null ) {
			DataService data = meta.getEngineData();
			EngineResources resources = data.getResources();
			AuthResource res = resources.getResource( ENVKEY );
			ops.setStringProperty( PROPERTY_ENVKEY , res.NAME );
		}
		
		ops.setBooleanProperty( PROPERTY_DISTR_REMOTE , DISTR_REMOTE );
		if( DISTR_REMOTE ) {
			DataService data = meta.getEngineData();
			EngineInfrastructure infra = data.getInfrastructure();
			HostAccount account = infra.getHostAccount( DISTR_ACCOUNT );
			ops.setStringProperty( PROPERTY_DISTR_HOSTLOGIN , account.getFinalAccount() );
			ops.setStringProperty( PROPERTY_DISTR_PATH , DISTR_PATH );
		}
	}
	
	public void scatterExtraProperties() throws Exception {
		DBAUTH = ops.getBooleanProperty( PROPERTY_DB_AUTH );
		SHOWONLY = ops.getBooleanProperty( PROPERTY_SHOWONLY );
		BACKUP = ops.getBooleanProperty( PROPERTY_BACKUP );
		CONF_DEPLOY = ops.getBooleanProperty( PROPERTY_CONF_DEPLOY );
		CONF_KEEPALIVE = ops.getBooleanProperty( PROPERTY_CONF_KEEPALIVE );
		DBAUTH_FILE = ops.getPathProperty( PROPERTY_DB_AUTHFILE );
		CHATROOM = ops.getStringProperty( PROPERTY_CHATROOM );
		REDISTWIN_PATH = ops.getPathProperty( PROPERTY_REDISTWIN_PATH );
		REDISTLINUX_PATH = ops.getPathProperty( PROPERTY_REDISTLINUX_PATH );
	}

	public void setEnvPrimary( String name , String desc , DBEnumEnvType type , MatchItem baselineMatchItem , boolean offline , MatchItem envKeyMatchItem , boolean distRemote , MatchItem distAccountMatchItem , String distPath ) throws Exception {
		NAME = name;
		DESC = desc;
		ENV_TYPE = type;
		BASELINE = MatchItem.copy( baselineMatchItem );
		OFFLINE = offline;
		ENVKEY = MatchItem.copy( envKeyMatchItem );
		DISTR_REMOTE = distRemote;
		DISTR_ACCOUNT = MatchItem.copy( distAccountMatchItem );
		DISTR_PATH = distPath;
		
		if( meta.isDraft() && isProd() )
			OFFLINE = true;
	}

	public void setTransition( Integer id ) {
		this.TRANSITION_META_ID = id;
	}
	
	public boolean isOnline() {
		return( !OFFLINE );
	}
	
	public void refreshProperties() throws Exception {
		refreshPrimaryProperties();
		for( MetaEnvSegment sg : sgMap.values() )
			sg.refreshProperties();
	}
	
	public boolean isProd() {
		return( ENV_TYPE == DBEnumEnvType.PRODUCTION );
	}
	
	public boolean isUAT() {
		return( ENV_TYPE == DBEnumEnvType.UAT );
	}
	
	public boolean isDEV() {
		return( ENV_TYPE == DBEnumEnvType.DEVELOPMENT );
	}
	
	public boolean hasBaseline() {
		if( BASELINE == null )
			return( false );
		return( true );
	}
	
	public MetaEnv getBaseline() throws Exception {
		MetaEnv env = envs.getProductEnv( BASELINE );
		return( env );
	}

	public void addDeployGroup( MetaEnvDeployGroup dg ) {
		deployGroup.put( dg.NAME , dg );
		deployGroupById.put( dg.ID , dg );
	}
	
	public void addSegment( MetaEnvSegment sg ) {
		sgMap.put( sg.NAME , sg );
		sgMapById.put( sg.ID , sg );
	}
	
	public void updateDeployGroup( MetaEnvDeployGroup dg ) throws Exception {
		Common.changeMapKey( deployGroup , dg , dg.NAME );
	}
	
	public void updateSegment( MetaEnvSegment sg ) throws Exception {
		Common.changeMapKey( sgMap , sg , sg.NAME );
	}

	public MetaEnvDeployGroup getDeployGroup( String name ) throws Exception {
		MetaEnvDeployGroup dg = deployGroup.get( name );
		if( dg == null )
			Common.exit1( _Error.UnknownDeployGroup1 , "unknown deploy group=" + name , name );
		return( dg );
	}
	
	public MetaEnvDeployGroup getDeployGroup( int id ) throws Exception {
		MetaEnvDeployGroup dg = deployGroupById.get( id );
		if( dg == null )
			Common.exit1( _Error.UnknownDeployGroup1 , "unknown deploy group=" + id , "" + id );
		return( dg );
	}
	
	public MetaEnvDeployGroup findDeployGroup( String name ) {
		return( deployGroup.get( name ) );
	}

	public MetaEnvDeployGroup findDeployGroup( int id ) {
		return( deployGroupById.get( id ) );
	}

	public MetaEnvSegment findSegment( MatchItem item ) {
		if( item == null )
			return( null );
		if( item.MATCHED )
			return( findSegment( item.FKID ) );
		return( findSegment( item.FKNAME ) );
	}

	public MetaEnvSegment findSegment( String name ) {
		return( sgMap.get( name ) );
	}

	public MetaEnvSegment findSegment( int id ) {
		return( sgMapById.get( id ) );
	}

	public MetaEnvSegment getSegment( String name ) throws Exception {
		MetaEnvSegment sg = sgMap.get( name );
		if( sg == null )
			Common.exit1( _Error.UnknownSegment1 , "unknown segment=" + name , name );
		return( sg );
	}

	public MetaEnvSegment getSegment( int id ) throws Exception {
		MetaEnvSegment sg = sgMapById.get( id );
		if( sg == null )
			Common.exit1( _Error.UnknownSegment1 , "unknown segment=" + id , "" + id );
		return( sg );
	}

	public MetaEnvSegment getSegment( MatchItem segment ) throws Exception {
		if( segment == null )
			return( null );
		if( segment.MATCHED )
			return( getSegment( segment.FKID ) );
		return( getSegment( segment.FKNAME ) );
	}
	
	public String getSegmentName( MatchItem item ) throws Exception {
		if( item == null )
			return( "" );
		MetaEnvSegment sg = getSegment( item );
		return( sg.NAME );
	}
	
	public String[] getDeployGroupNames() {
		return( Common.getSortedKeys( deployGroup ) );
	}
	
	public String[] getSegmentNames() {
		return( Common.getSortedKeys( sgMap ) );
	}
	
	public MetaEnvSegment[] getSegments() {
		List<MetaEnvSegment> list = new LinkedList<MetaEnvSegment>();
		for( String name : Common.getSortedKeys( sgMap ) ) {
			MetaEnvSegment sg = sgMap.get( name );
			list.add( sg );
		}
		return( list.toArray( new MetaEnvSegment[0] ) );
	}
	
	public boolean isMultiSegment() throws Exception {
		return( sgMap.size() > 1 );
	}
	
	public void removeDeployGroup( MetaEnvDeployGroup dg ) {
		deployGroup.remove( dg.NAME );
		deployGroupById.remove( dg.ID );
	}
	
	public void removeSegment( MetaEnvSegment sg ) {
		sgMap.remove( sg.NAME );
		sgMapById.remove( sg.ID );
	}
	
	public void setBaseline( MatchItem envMatchItem ) throws Exception {
		this.BASELINE = MatchItem.copy( envMatchItem );
		refreshPrimaryProperties();
	}
	
	public void setOffline( boolean offline ) throws Exception {
		this.OFFLINE = offline;
		refreshPrimaryProperties();
	}
	
	public void getApplicationReferences( HostAccount account , List<AccountReference> refs ) {
		for( MetaEnvSegment sg : sgMap.values() )
			sg.getApplicationReferences( account , refs );
	}

	public boolean isConfUsed( MetaDistrConfItem item ) throws Exception {
		for( MetaEnvSegment sg : sgMap.values() ) {
			if( sg.isConfUsed( item ) )
				return( true );
		}
		return( false );
	}

	public MatchItem getBaselineMatchItem() {
		return( BASELINE );
	}
	
	public MatchItem getEnvKeyMatchItem() {
		return( ENVKEY );
	}

	public AuthResource getEnvKey() throws Exception {
		if( ENVKEY == null )
			return( null );
		DataService data = meta.getEngineData();
		EngineResources resources = data.getResources();
		return( resources.getResource( ENVKEY ) );
	}
	
	public HostAccount getDistrAccount() throws Exception {
		if( DISTR_ACCOUNT == null )
			return( null );
		DataService data = meta.getEngineData();
		EngineInfrastructure infra = data.getInfrastructure();
		return( infra.getHostAccount( DISTR_ACCOUNT ) );
	}
	
	public MatchItem getDistrAccountMatchItem() {
		return( DISTR_ACCOUNT );
	}
	
	public boolean checkMatched() {
		if( !MatchItem.isMatched( BASELINE ) )
			return( false );
		if( !MatchItem.isMatched( ENVKEY ) )
			return( false );
		if( !MatchItem.isMatched( DISTR_ACCOUNT ) )
			return( false );
		
		for( MetaEnvSegment sg : sgMap.values() ) {
			if( !sg.checkMatched() )
				return( false );
		}
		return( true );
	}

	public void copyResolveExternals() throws Exception {
		if( BASELINE != null ) {
			MetaEnv env = getBaseline();
			if( env == null )
				Common.exitUnexpected();
			
			BASELINE.match( env.ID );
		}
		
		refreshPrimaryProperties();
		ops.recalculateProperties();
		
		scatterExtraProperties();
		
		for( MetaEnvSegment sg : sgMap.values() )
			sg.copyResolveExternals();
	}

	public MetaEnvStartGroup getStartGroup( int id ) throws Exception {
		for( MetaEnvSegment sg : sgMap.values() ) {
			MetaEnvStartInfo startInfo = sg.getStartInfo();
			MetaEnvStartGroup startGroup = startInfo.findStartGroup( id );
			if( startGroup != null )
				return( startGroup );
		}
		
		Common.exitUnexpected();
		return( null );
	}
	
	public MetaEnvServer findServer( int id ) {
		for( MetaEnvSegment sg : sgMap.values() ) {
			MetaEnvServer server = sg.findServer( id );
			if( server != null )
				return( server );
		}
		return( null );
	}
	
	public MetaEnvServerNode findServerNode( int id ) {
		for( MetaEnvSegment sg : sgMap.values() ) {
			MetaEnvServerNode node = sg.findServerNode( id );
			if( node != null )
				return( node );
		}
		return( null );
	}
	
	public MetaEnvServer getServer( int id ) throws Exception {
		MetaEnvServer server = findServer( id );
		if( server == null )
			Common.exitUnexpected();
		return( server );
	}
	
	public MetaEnvServerNode getServerNode( int id ) throws Exception {
		MetaEnvServerNode node = findServerNode( id );
		if( node == null )
			Common.exitUnexpected();
		return( node );
	}
	
}
