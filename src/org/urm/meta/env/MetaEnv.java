package org.urm.meta.env;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.properties.ObjectProperties;
import org.urm.meta.EngineData;
import org.urm.meta.EngineObject;
import org.urm.meta.MatchItem;
import org.urm.meta.engine.AccountReference;
import org.urm.meta.engine.AuthResource;
import org.urm.meta.engine.EngineInfrastructure;
import org.urm.meta.engine.EngineResources;
import org.urm.meta.engine.HostAccount;
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
	public static String PROPERTY_KEYFILE = "keyfile";
	public static String PROPERTY_DB_AUTHFILE = "db-authfile";
	public static String PROPERTY_DB_AUTH = "db-auth";
	public static String PROPERTY_SHOWONLY = "showonly";
	public static String PROPERTY_BACKUP = "backup";
	public static String PROPERTY_CONF_DEPLOY = "configuration-deploy";
	public static String PROPERTY_CONF_KEEPALIVE = "configuration-keepalive";

	public Meta meta;

	// table data
	private ObjectProperties ops;
	public int ID;
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

	public MetaEnv( ProductMeta storage , Meta meta ) {
		super( null );
		this.meta = meta;
		ID = -1;
		EV = -1;
		MATCHED = false;
		sgMap = new HashMap<String,MetaEnvSegment>();
		sgMapById = new HashMap<Integer,MetaEnvSegment>();
	}
	
	@Override
	public String getName() {
		return( NAME );
	}
	
	public MetaEnv copy( ProductMeta rstorage , Meta rmeta , ObjectProperties rparent ) throws Exception {
		MetaEnv r = new MetaEnv( rstorage , rmeta );
		
		r.ops = ops.copy( rparent );
		
		r.ID = ID;
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
		r.refreshPrimaryProperties();
		
		for( MetaEnvSegment sg : sgMap.values() ) {
			MetaEnvSegment rsg = sg.copy( rmeta , r );
			r.addSegment( rsg );
		}
		
		r.scatterExtraProperties();
		return( r );
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
	
	private void refreshPrimaryProperties() throws Exception {
		ops.clearProperties( DBEnumParamEntityType.ENV_PRIMARY );
		
		ops.setStringProperty( PROPERTY_NAME , NAME );
		ops.setStringProperty( PROPERTY_DESC , DESC );
		ops.setEnumProperty( PROPERTY_ENVTYPE , ENV_TYPE );
		
		if( BASELINE != null ) {
			ProductEnvs envs = meta.getEnviroments();
			MetaEnv env = envs.getMetaEnv( BASELINE );
			ops.setStringProperty( PROPERTY_BASELINE , env.NAME );
		}
		
		ops.setBooleanProperty( PROPERTY_OFFLINE , OFFLINE );
		
		if( ENVKEY != null ) {
			EngineData data = meta.getEngineData();
			EngineResources resources = data.getResources();
			AuthResource res = resources.getResource( ENVKEY );
			ops.setStringProperty( PROPERTY_ENVKEY , res.NAME );
		}
		
		ops.setBooleanProperty( PROPERTY_DISTR_REMOTE , DISTR_REMOTE );
		if( DISTR_REMOTE ) {
			EngineData data = meta.getEngineData();
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
		
		refreshPrimaryProperties();
	}

	public boolean isProd() {
		return( ENV_TYPE == DBEnumEnvType.PRODUCTION );
	}
	
	public boolean hasBaseline() {
		if( BASELINE == null )
			return( false );
		return( true );
	}
	
	public MetaEnv getBaseline() throws Exception {
		ProductEnvs envs = meta.getEnviroments();
		MetaEnv env = envs.getMetaEnv( BASELINE );
		return( env );
	}
	
	public void addSegment( MetaEnvSegment sg ) {
		sgMap.put( sg.NAME , sg );
		sgMapById.put( sg.ID , sg );
	}
	
	public void updateSegment( MetaEnvSegment sg ) throws Exception {
		Common.changeMapKey( sgMap , sg , sg.NAME );
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
	
	public MetaEnvSegment getMainSegment() throws Exception {
		if( sgMap.isEmpty() )
			Common.exit0( _Error.NoSegmentDefined0 , "no segment defined" );
		if( sgMap.size() > 1 )
			Common.exitUnexpected();
		for( MetaEnvSegment sg : sgMap.values() )
			return( sg );
		return( null );
	}
	
	public void removeSegment( MetaEnvSegment sg ) {
		sgMap.remove( sg.NAME );
	}
	
	public void setBaseline( MetaEnv env ) throws Exception {
		this.BASELINE = new MatchItem( env.ID );
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
		EngineData data = meta.getEngineData();
		EngineResources resources = data.getResources();
		return( resources.getResource( ENVKEY ) );
	}
	
	public HostAccount getDistrAccount() throws Exception {
		if( DISTR_ACCOUNT == null )
			return( null );
		EngineData data = meta.getEngineData();
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
	
	public MetaEnvServer getServer( int id ) throws Exception {
		for( MetaEnvSegment sg : sgMap.values() ) {
			MetaEnvServer server = sg.findServer( id );
			if( server != null )
				return( server );
		}
		
		Common.exitUnexpected();
		return( null );
	}
	
}
