package org.urm.meta.env;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.DataService;
import org.urm.engine.data.EngineInfrastructure;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.shell.Account;
import org.urm.meta.engine.AccountReference;
import org.urm.meta.engine.Datacenter;
import org.urm.meta.engine.HostAccount;
import org.urm.meta.loader.EngineObject;
import org.urm.meta.loader.MatchItem;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistrConfItem;

public class MetaEnvSegment extends EngineObject {

	// properties
	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_BASELINE = "basesg";
	public static String PROPERTY_DC = "datacenter";
	public static String PROPERTY_OFFLINE = "offline";

	public Meta meta;
	public MetaEnv env;
	
	// table data
	private ObjectProperties ops;
	public int ID;
	public String NAME;
	public String DESC;
	private MatchItem BASELINE;
	public boolean OFFLINE;
	private MatchItem DC;
	public int EV;
	
	private MetaEnvDeployment deployInfo;
	private MetaEnvStartInfo startInfo;
	
	private Map<String,MetaEnvServer> serverMap;
	private Map<Integer,MetaEnvServer> serverMapById;

	public MetaEnvSegment( Meta meta , MetaEnv env ) {
		super( env );
		this.meta = meta;
		this.env = env;
		
		serverMap = new HashMap<String,MetaEnvServer>();
		serverMapById = new HashMap<Integer,MetaEnvServer>();
		
		deployInfo = new MetaEnvDeployment( meta , this );
		startInfo = new MetaEnvStartInfo( meta , this );
	}

	@Override
	public String getName() {
		return( NAME );
	}
	
	public MetaEnvSegment copy( Meta rmeta , MetaEnv renv ) throws Exception {
		MetaEnvSegment r = new MetaEnvSegment( rmeta , renv );

		r.ops = ops.copy( renv.getProperties() );
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.BASELINE = MatchItem.copy( BASELINE );
		r.OFFLINE = OFFLINE;
		r.DC = MatchItem.copy( DC );
		r.EV = EV;
		
		r.deployInfo = deployInfo.copy( rmeta , r );
		
		for( MetaEnvServer server : serverMap.values() ) {
			MetaEnvServer rserver = server.copy( rmeta , r );
			r.addServer( rserver );
		}
		
		r.startInfo = startInfo.copy( rmeta , r );
		
		return( r );
	}

	public void copyResolveExternals() throws Exception {
		if( BASELINE != null ) {
			MetaEnvSegment sg = getBaseline();
			if( sg == null )
				Common.exitUnexpected();
			
			BASELINE.match( sg.ID );
		}
		
		refreshPrimaryProperties();
		ops.recalculateProperties();
		
		for( MetaEnvServer server : serverMap.values() )
			server.copyResolveExternals();
	}
	
	public void setSegmentPrimary( String name , String desc , MatchItem baselineMatchItem , boolean offline , MatchItem dcMatchItem ) throws Exception {
		if( !env.hasBaseline() )
			baselineMatchItem = null;
		
		if( dcMatchItem == null )
			Common.exitUnexpected();
		
		this.NAME = name;
		this.DESC = desc;
		this.BASELINE = MatchItem.copy( baselineMatchItem );
		this.OFFLINE = offline;
		this.DC = MatchItem.copy( dcMatchItem );
	}

	public void refreshProperties() throws Exception {
		refreshPrimaryProperties();
		for( MetaEnvServer server : serverMap.values() )
			server.refreshProperties();
	}
	
	public void createSettings( ObjectProperties ops ) throws Exception {
		this.ops = ops;
	}
	
	public ObjectProperties getProperties() {
		return( ops );
	}
	
	public MetaEnvSegment getBaselineSegment( MetaEnv baselineEnv ) throws Exception {
		return( baselineEnv.getSegment( BASELINE.FKID ) );
	}
	
	public String getEnvObjectName() {
		return( env.NAME + "::" + NAME );
	}
	
	public MetaEnvStartInfo getStartInfo() {
		return( startInfo );
	}
	
	public boolean hasBaseline() {
		if( BASELINE == null )
			return( false );
		return( true );
	}
	
	public MetaEnvSegment getBaseline() throws Exception {
		MetaEnv envBaseline = env.getBaseline();
		if( envBaseline == null )
			return( null );
		
		return( envBaseline.getSegment( BASELINE ) );
	}
	
	public void addServer( MetaEnvServer server ) {
		serverMap.put( server.NAME , server );
		serverMapById.put( server.ID , server );
	}
	
	public MetaEnvServer findServer( String name ) {
		return( serverMap.get( name ) );
	}
	
	public MetaEnvServer findServer( int id ) {
		return( serverMapById.get( id ) );
	}
	
	public MetaEnvServer getServer( String name ) throws Exception {
		MetaEnvServer server = serverMap.get( name );
		if( server == null )
			Common.exit1( _Error.UnknownServer1 , "unknown server=" + name , name );
		return( server );
	}
	
	public MetaEnvServer getServer( int id ) throws Exception {
		MetaEnvServer server = serverMapById.get( id );
		if( server == null )
			Common.exit1( _Error.UnknownServer1 , "unknown server=" + id , "" + id );
		return( server );
	}
	
	public MetaEnvServer findServer( MatchItem item ) throws Exception {
		if( item == null )
			return( null );
		if( item.MATCHED )
			return( findServer( item.FKID ) );
		return( findServer( item.FKNAME ) );
	}
	
	public MetaEnvServer getServer( MatchItem item ) throws Exception {
		if( item == null )
			return( null );
		MetaEnvServer server = findServer( item );
		if( server == null )
			Common.exitUnexpected();
		return( server );
	}
	
	public String getServerName( MatchItem item ) throws Exception {
		if( item == null )
			return( "" );
		MetaEnvServer server = getServer( item );
		return( server.NAME );
	}
	
	public MetaEnvServer[] getServers() {
		return( serverMap.values().toArray( new MetaEnvServer[0] ) );
	}

	public String[] getServerNames() {
		return( Common.getSortedKeys( serverMap ) );
	}
	
	public String getServerNodesByHost( ActionBase action , String host ) throws Exception {
		String s = "";
		for( String name : Common.getSortedKeys( serverMap ) ) {
			MetaEnvServer server = serverMap.get( name );
			String sn = server.getNodesAsStringByHost( action , host );
			if( !sn.isEmpty() ) {
				if( !s.isEmpty() )
					s += ", ";
				s += "server=" + server.NAME + " (" + sn + ")";
			}
		}
		return( s );
	}

	public String getServerNodesByAccount( ActionBase action , Account account ) throws Exception {
		String s = "";
		for( String name : Common.getSortedKeys( serverMap ) ) {
			MetaEnvServer server = serverMap.get( name );
			String sn = server.getNodesAsStringByAccount( action , account );
			if( !sn.isEmpty() ) {
				if( !s.isEmpty() )
					s += ", ";
				s += "server=" + server.NAME + " (" + sn + ")";
			}
		}
		return( s );
	}

	public boolean hasDatabaseServers() {
		for( MetaEnvServer server : serverMap.values() )
			if( server.isRunDatabase() )
				return( true );
		
		return( false );
	}
	
	public void modifySegment( String name , String desc , MatchItem dcItem ) throws Exception {
		this.NAME = name;
		this.DESC = desc;
		this.DC = dcItem;

		refreshPrimaryProperties();
	}
	
	public void refreshPrimaryProperties() throws Exception {
		ops.clearProperties( DBEnumParamEntityType.ENV_SEGMENT_PRIMARY );
		
		ops.setStringProperty( PROPERTY_NAME , NAME );
		ops.setStringProperty( PROPERTY_DESC , DESC );
		ops.setBooleanProperty( PROPERTY_OFFLINE , OFFLINE );
		
		MetaEnvSegment sgBaseline = getBaseline();
		if( sgBaseline != null )
			ops.setStringProperty( PROPERTY_BASELINE , sgBaseline.NAME );
		
		Datacenter dc = getDatacenter();
		if( dc != null )
			ops.setStringProperty( PROPERTY_DC , dc.NAME );
	}

	public void updateServer( MetaEnvServer server ) throws Exception {
		Common.changeMapKey( serverMap , server , server.NAME );
	}
	
	public void removeServer( MetaEnvServer server ) {
		serverMap.remove( server.NAME );
		serverMapById.remove( server.ID );
		startInfo.removeServer( server );
	}
	
	public void setBaseline( MatchItem sgBaseline ) throws Exception {
		this.BASELINE = MatchItem.copy( sgBaseline );
		refreshPrimaryProperties();
	}
	
	public void setOffline( boolean offline ) throws Exception {
		this.OFFLINE = offline;
		refreshPrimaryProperties();
	}
	
	public boolean checkMatched() {
		if( !MatchItem.isMatched( BASELINE ) )
			return( false );
		if( !MatchItem.isMatched( DC ) )
			return( false );
		
		for( MetaEnvServer server : serverMap.values() ) {
			if( !server.checkMatched() )
				return( false );
		}
		
		return( true );
	}

	public void getApplicationReferences( HostAccount account , List<AccountReference> refs ) {
		for( MetaEnvServer server : serverMap.values() )
			server.getApplicationReferences( account , refs );
	}

	public boolean isConfUsed( MetaDistrConfItem item ) throws Exception {
		for( MetaEnvServer server : serverMap.values() ) {
			if( server.hasConfItemDeployment( item ) )
				return( true );
		}
		return( false );
	}

	public Datacenter getDatacenter() throws Exception {
		DataService data = meta.getEngineData();
		EngineInfrastructure infra = data.getInfrastructure();
		return( infra.getDatacenter( DC ) );
	}

	public MatchItem getBaselineMatchItem() {
		return( BASELINE );
	}
	
	public MatchItem getDatacenterMatchItem() {
		return( DC );
	}

}
