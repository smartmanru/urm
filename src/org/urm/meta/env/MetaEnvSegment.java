package org.urm.meta.env;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.EngineTransaction;
import org.urm.engine.dist.Release;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertyController;
import org.urm.engine.properties.PropertySet;
import org.urm.engine.shell.Account;
import org.urm.meta.EngineData;
import org.urm.meta.EngineObject;
import org.urm.meta.MatchItem;
import org.urm.meta.engine.AccountReference;
import org.urm.meta.engine.Datacenter;
import org.urm.meta.engine.EngineInfrastructure;
import org.urm.meta.engine.HostAccount;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product._Error;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaEnvSegment extends EngineObject {

	// properties
	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_BASELINE = "basesg";
	public static String PROPERTY_DC = "datacenter";
	public static String PROPERTY_OFFLINE = "offline";

	public static String ELEMENT_DEPLOYMENT = "deployment";
	public static String ELEMENT_STARTORDER = "startorder";
	public static String ELEMENT_SERVER = "server";
	
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
		r.refreshProperties();
		
		r.deployInfo = deployInfo.copy( rmeta , r );
		
		for( MetaEnvServer server : serverMap.values() ) {
			MetaEnvServer rserver = server.copy( rmeta , r );
			r.addServer( rserver );
		}
		
		r.startInfo = startInfo.copy( rmeta , r );
		
		return( r );
	}

	public ObjectProperties getProperties() {
		return( ops );
	}
	
	public MetaEnvSegment getBaselineSegment( MetaEnv baselineEnv ) throws Exception {
		return( baselineEnv.getSegment( BASELINE.FKID ) );
	}
	
	public String getEnvObjectName() throws Exception {
		return( env.NAME + "-" + NAME );
	}
	
	public boolean hasBaseline() throws Exception {
		if( BASELINE == null )
			return( false );
		return( true );
	}
	
	public MetaEnvSegment getBaseline() throws Exception {
		MetaEnv envBaseline = env.getBaseline();
		if( envBaseline == null )
			Common.exitUnexpected();
		
		return( envBaseline.getSegment( BASELINE ) );
	}
	
	private void addServer( MetaEnvServer server ) {
		serverMap.put( server.NAME , server );
		serverMapById.put( server.ID , server );
	}
	
	public void resolveLinks() throws Exception {
		for( MetaEnvServer server : serverMap.values() )
			server.resolveLinks();
	}
	
	public MetaEnvServer findServer( String name ) {
		return( serverMap.get( name ) );
	}
	
	public MetaEnvServer getServer( ActionBase action , String name ) throws Exception {
		MetaEnvServer server = serverMap.get( name );
		if( server == null )
			action.exit1( _Error.UnknownServer1 , "unknown server=" + name , name );
		return( server );
	}
	
	public MetaEnvServer[] getServers() {
		return( serverMap.values().toArray( new MetaEnvServer[0] ) );
	}

	public String[] getServerNames() {
		return( Common.getSortedKeys( serverMap ) );
	}
	
	public String getServerNodesByHost( String host ) throws Exception {
		String s = "";
		for( String name : Common.getSortedKeys( serverMap ) ) {
			MetaEnvServer server = serverMap.get( name );
			String sn = server.getNodesAsStringByHost( host );
			if( !sn.isEmpty() ) {
				if( !s.isEmpty() )
					s += ", ";
				s += "server=" + server.NAME + " (" + sn + ")";
			}
		}
		return( s );
	}

	public String getServerNodesByAccount( Account account ) throws Exception {
		String s = "";
		for( String name : Common.getSortedKeys( serverMap ) ) {
			MetaEnvServer server = serverMap.get( name );
			String sn = server.getNodesAsStringByAccount( account );
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
			if( server.isDatabase() )
				return( true );
		
		return( false );
	}
	
	public void modifySegment( String name , String desc , MatchItem dcItem ) throws Exception {
		this.NAME = name;
		this.DESC = desc;
		this.DC = dcItem;

		refreshProperties();
	}
	
	private void refreshProperties() throws Exception {
		ops.setStringProperty( PROPERTY_NAME , NAME );
		ops.setStringProperty( PROPERTY_DESC , DESC );
		ops.setBooleanProperty( PROPERTY_OFFLINE , OFFLINE );
		
		MetaEnvSegment sgBaseline = getBaseline();
		if( sgBaseline != null )
			ops.setStringProperty( PROPERTY_BASELINE , sgBaseline.NAME );
		else
			ops.clearProperty( PROPERTY_BASELINE );
		
		Datacenter dc = getDatacenter();
		if( dc != null )
			ops.setStringProperty( PROPERTY_DC , dc.NAME );
		else
			ops.clearProperty( PROPERTY_DC );
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
		this.BASELINE = sgBaseline;
		refreshProperties();
	}
	
	public void setOffline( EngineTransaction transaction , boolean offline ) throws Exception {
		this.OFFLINE = offline;
		refreshProperties();
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

	public void setStartInfo( EngineTransaction transaction , MetaEnvStartInfo startInfo ) throws Exception {
		this.startInfo = startInfo;
		for( MetaEnvServer server : serverMap.values() )
			server.setStartGroup( null );
		
		for( MetaEnvStartGroup group : startInfo.getForwardGroupList() ) {
			for( MetaEnvServer server : group.getServers() )
				server.setStartGroup( group );
		}
	}

	public boolean isConfUsed( MetaDistrConfItem item ) {
		for( MetaEnvServer server : serverMap.values() ) {
			if( server.hasConfItemDeployment( item ) )
				return( true );
		}
		return( false );
	}

	public boolean isReleaseApplicable( Release release ) {
		for( MetaEnvServer server : serverMap.values() ) {
			if( server.isReleaseApplicable( release ) )
				return( true );
		}
		return( false );
	}

	public Datacenter getDatacenter() throws Exception {
		EngineData data = meta.getEngineData();
		EngineInfrastructure infra = data.getInfrastructure();
		return( infra.getDatacenter( DC ) );
	}

	public void scatterProperties( ActionBase action ) throws Exception {
		NAME = super.getStringPropertyRequired( action , PROPERTY_NAME );
		DESC = super.getStringProperty( action , PROPERTY_DESC );
		DC = super.getStringProperty( action , PROPERTY_DC );
		action.trace( "load properties of sg=" + NAME );
		
		BASELINE = super.getStringProperty( action , PROPERTY_BASELINE );
		if( BASELINE.equals( "default" ) )
			BASELINE = NAME;
		OFFLINE = super.getBooleanProperty( action , PROPERTY_OFFLINE , true );
		
		super.finishRawProperties();
	}
	
	public void loadStartOrder( ActionBase action , Node node ) throws Exception {
		startInfo = new MetaEnvStartInfo( meta , this );
		
		Node startorder = ConfReader.xmlGetFirstChild( node , ELEMENT_STARTORDER );
		if( startorder == null )
			return;
		
		startInfo.load( action , startorder );
	}

}
