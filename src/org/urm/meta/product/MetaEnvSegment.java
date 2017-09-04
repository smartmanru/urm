package org.urm.meta.product;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertyController;
import org.urm.common.PropertySet;
import org.urm.engine.EngineTransaction;
import org.urm.engine.dist.Release;
import org.urm.engine.shell.Account;
import org.urm.meta.engine.ServerAccountReference;
import org.urm.meta.engine.ServerHostAccount;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaEnvSegment extends PropertyController {

	public Meta meta;
	public MetaEnv env;
	
	public String NAME;
	public String BASELINE;
	public boolean OFFLINE;
	public String DESC;
	public String SG;
	
	public MetaEnvDeployment deploy;
	public MetaEnvStartInfo startInfo;
	
	private List<MetaEnvServer> originalList;
	private Map<String,MetaEnvServer> serverMap;

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_BASELINE = "basesg";
	public static String PROPERTY_DC = "datacenter";
	public static String PROPERTY_OFFLINE = "offline";

	public static String ELEMENT_DEPLOYMENT = "deployment";
	public static String ELEMENT_STARTORDER = "startorder";
	public static String ELEMENT_SERVER = "server";
	
	public MetaEnvSegment( Meta meta , MetaEnv env ) {
		super( env , "sg" );
		this.meta = meta;
		this.env = env;
		
		originalList = new LinkedList<MetaEnvServer>();
		serverMap = new HashMap<String,MetaEnvServer>();
	}

	@Override
	public String getName() {
		return( NAME );
	}
	
	@Override
	public boolean isValid() {
		if( super.isLoadFailed() )
			return( false );
		return( true );
	}

	@Override
	public void scatterProperties( ActionBase action ) throws Exception {
		NAME = super.getStringPropertyRequired( action , PROPERTY_NAME );
		DESC = super.getStringProperty( action , PROPERTY_DESC );
		SG = super.getStringProperty( action , PROPERTY_DC );
		action.trace( "load properties of sg=" + NAME );
		
		BASELINE = super.getStringProperty( action , PROPERTY_BASELINE );
		if( BASELINE.equals( "default" ) )
			BASELINE = NAME;
		OFFLINE = super.getBooleanProperty( action , PROPERTY_OFFLINE , true );
		
		super.finishRawProperties();
	}
	
	public MetaEnvSegment copy( ActionBase action , Meta meta , MetaEnv env ) throws Exception {
		MetaEnvSegment r = new MetaEnvSegment( meta , env );
		r.initCopyStarted( this , env.getProperties() );
		
		if( deploy != null )
			r.deploy = deploy.copy( action , meta , r );
		for( MetaEnvServer server : originalList ) {
			MetaEnvServer rserver = server.copy( action , meta , r );
			r.addServer( rserver );
		}
		
		if( startInfo != null )
			r.startInfo = startInfo.copy( action , meta , r );
		
		r.scatterProperties( action );
		r.initFinished();
		return( r );
	}

	public void setProperties( EngineTransaction transaction , PropertySet props , boolean system ) throws Exception {
		super.updateProperties( transaction , props , system );
		scatterProperties( transaction.getAction() );
	}
	
	public String getFullId( ActionBase action ) throws Exception {
		return( env.ID + "-" + NAME );
	}
	
	public boolean hasBaseline( ActionBase action ) throws Exception {
		if( BASELINE.isEmpty() )
			return( false );
		return( true );
	}
	
	public String getBaselineSG( ActionBase action ) throws Exception {
		return( BASELINE );
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		if( !super.initCreateStarted( env.getProperties() ) )
			return;

		super.loadFromNodeAttributes( action , node , false );
		scatterProperties( action );
		
		super.loadFromNodeElements( action , node , true );
		super.resolveRawProperties();
		
		loadServers( action , node );
		loadStartOrder( action , node );
		loadDeployment( action , node );
		
		super.initFinished();
	}

	public void loadDeployment( ActionBase action , Node node ) throws Exception {
		deploy = new MetaEnvDeployment( meta , this );
		
		Node deployment = ConfReader.xmlGetFirstChild( node , ELEMENT_DEPLOYMENT );
		if( deployment == null )
			return;
		
		deploy.load( action , deployment );
	}
	
	public void loadServers( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , ELEMENT_SERVER );
		if( items == null )
			return;
		
		for( Node srvnode : items ) {
			MetaEnvServer server = new MetaEnvServer( meta , this );
			server.load( action , srvnode );
			addServer( server );
		}
	}
	
	private void addServer( MetaEnvServer server ) {
		serverMap.put( server.NAME , server );
		originalList.add( server );
	}
	
	public void resolveLinks( ActionBase action ) throws Exception {
		for( MetaEnvServer server : originalList )
			server.resolveLinks( action );
	}
	
	public void loadStartOrder( ActionBase action , Node node ) throws Exception {
		startInfo = new MetaEnvStartInfo( meta , this );
		
		Node startorder = ConfReader.xmlGetFirstChild( node , ELEMENT_STARTORDER );
		if( startorder == null )
			return;
		
		startInfo.load( action , startorder );
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
		return( originalList.toArray( new MetaEnvServer[0] ) );
	}

	public String[] getServerNames() {
		return( Common.getSortedKeys( serverMap ) );
	}
	
	public String getServerNodesByHost( ActionBase action , String host ) throws Exception {
		String s = "";
		for( MetaEnvServer server : originalList ) {
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
		for( MetaEnvServer server : originalList ) {
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
		for( MetaEnvServer server : originalList )
			if( server.isDatabase() )
				return( true );
		
		return( false );
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Element deployElement = Common.xmlCreateElement( doc , root , ELEMENT_DEPLOYMENT );
		deploy.save( action , doc , deployElement );
		Element startElement = Common.xmlCreateElement( doc , root , ELEMENT_STARTORDER );
		startInfo.save( action , doc , startElement );
		
		super.saveSplit( doc , root );
		for( MetaEnvServer server : originalList ) {
			Element serverElement = Common.xmlCreateElement( doc , root , ELEMENT_SERVER );
			server.save( action , doc , serverElement );
		}
	}
	
	public void createSegment( ActionBase action , String NAME , String DESC , String DC ) throws Exception {
		this.NAME = NAME;
		if( !super.initCreateStarted( env.getProperties() ) )
			return;

		super.setStringProperty( PROPERTY_NAME , NAME );
		super.setStringProperty( PROPERTY_DESC , DESC );
		super.setStringProperty( PROPERTY_DC , DC );
		super.finishProperties( action );
		super.initFinished();
		
		scatterProperties( action );
		
		deploy = new MetaEnvDeployment( meta , this );
		startInfo = new MetaEnvStartInfo( meta , this );
	}

	public void modifySegment( ActionBase action , String NAME , String DESC , String DC ) throws Exception {
		this.NAME = NAME;

		super.setStringProperty( PROPERTY_NAME , NAME );
		super.setStringProperty( PROPERTY_DESC , DESC );
		super.setStringProperty( PROPERTY_DC , DC );
		super.finishProperties( action );
		
		scatterProperties( action );
	}

	public void createServer( EngineTransaction transaction , MetaEnvServer server ) {
		addServer( server );
	}
	
	public void modifyServer( EngineTransaction transaction , MetaEnvServer server ) {
		for( Entry<String,MetaEnvServer> entry : serverMap.entrySet() ) {
			if( entry.getValue() == server ) {
				serverMap.remove( entry.getKey() );
				break;
			}
		}
		
		originalList.remove( server );
		addServer( server );
	}
	
	public void deleteServer( EngineTransaction transaction , MetaEnvServer server ) {
		int index = originalList.indexOf( server );
		if( index < 0 )
			return;
		
		originalList.remove( index );
		serverMap.remove( server.NAME );
		startInfo.removeServer( transaction , server );
	}
	
	public void setBaseline( EngineTransaction transaction , String baselineSG ) throws Exception {
		super.setSystemStringProperty( PROPERTY_BASELINE , baselineSG );
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
		for( MetaEnvServer server : originalList )
			server.getApplicationReferences( account , refs );
	}

	public void deleteHostAccount( EngineTransaction transaction , ServerHostAccount account ) throws Exception {
		super.deleteObject();
	}

	public void setStartInfo( EngineTransaction transaction , MetaEnvStartInfo startInfo ) throws Exception {
		this.startInfo = startInfo;
	}

	public boolean isConfUsed( MetaDistrConfItem item ) {
		for( MetaEnvServer server : originalList ) {
			if( server.hasConfItemDeployment( item ) )
				return( true );
		}
		return( false );
	}

	public boolean isReleaseApplicable( Release release ) {
		for( MetaEnvServer server : originalList ) {
			if( server.isReleaseApplicable( release ) )
				return( true );
		}
		return( false );
	}
	
}
