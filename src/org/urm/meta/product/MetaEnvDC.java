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
import org.urm.engine.ServerTransaction;
import org.urm.engine.shell.Account;
import org.urm.meta.engine.ServerAccountReference;
import org.urm.meta.engine.ServerHostAccount;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaEnvDC extends PropertyController {

	public Meta meta;
	public MetaEnv env;
	
	public String NAME;
	public String BASELINE;
	public boolean OFFLINE;
	
	public MetaEnvDeployment deploy;
	public MetaEnvStartInfo startInfo;
	
	private List<MetaEnvServer> originalList;
	private Map<String,MetaEnvServer> serverMap;

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_BASELINE = "basedc";
	public static String PROPERTY_OFFLINE = "offline";

	public static String ELEMENT_DEPLOYMENT = "deployment";
	public static String ELEMENT_STARTORDER = "startorder";
	public static String ELEMENT_SERVER = "server";
	
	public MetaEnvDC( Meta meta , MetaEnv env ) {
		super( env , "dc" );
		this.meta = meta;
		this.env = env;
		
		originalList = new LinkedList<MetaEnvServer>();
		serverMap = new HashMap<String,MetaEnvServer>();
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
		action.trace( "load properties of dc=" + NAME );
		
		BASELINE = super.getStringProperty( action , PROPERTY_BASELINE );
		if( BASELINE.equals( "default" ) )
			BASELINE = NAME;
		OFFLINE = super.getBooleanProperty( action , PROPERTY_OFFLINE , true );
		
		super.finishRawProperties();
	}
	
	public MetaEnvDC copy( ActionBase action , Meta meta , MetaEnv env ) throws Exception {
		MetaEnvDC r = new MetaEnvDC( meta , env );
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

	public String getFullId( ActionBase action ) throws Exception {
		return( env.ID + "-" + NAME );
	}
	
	public boolean hasBaseline( ActionBase action ) throws Exception {
		if( BASELINE.isEmpty() )
			return( false );
		return( true );
	}
	
	public String getBaselineDC( ActionBase action ) throws Exception {
		return( BASELINE );
	}
	
	public void load( ActionBase action , Node node , boolean loadProps ) throws Exception {
		if( !super.initCreateStarted( env.getProperties() ) )
			return;

		super.loadFromNodeAttributes( action , node );
		scatterProperties( action );
		
		if( loadProps ) {
			super.loadFromNodeElements( action , node );
			super.resolveRawProperties();
		}
		
		loadServers( action , node , loadProps );
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
	
	public void loadServers( ActionBase action , Node node , boolean loadProps ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , ELEMENT_SERVER );
		if( items == null )
			return;
		
		for( Node srvnode : items ) {
			MetaEnvServer server = new MetaEnvServer( meta , this );
			server.load( action , srvnode , loadProps );
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

	public boolean hasDatabaseServers( ActionBase action ) throws Exception {
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
	
	public void createDC( ActionBase action , String NAME ) throws Exception {
		this.NAME = NAME;
		if( !super.initCreateStarted( env.getProperties() ) )
			return;

		super.setStringProperty( PROPERTY_NAME , NAME );
		super.setStringProperty( PROPERTY_BASELINE , BASELINE );
		super.finishProperties( action );
		super.initFinished();
		
		scatterProperties( action );
		
		deploy = new MetaEnvDeployment( meta , this );
		startInfo = new MetaEnvStartInfo( meta , this );
	}

	public void createServer( ServerTransaction transaction , MetaEnvServer server ) {
		addServer( server );
	}
	
	public void modifyServer( ServerTransaction transaction , MetaEnvServer server ) {
		for( Entry<String,MetaEnvServer> entry : serverMap.entrySet() ) {
			if( entry.getValue() == server ) {
				serverMap.remove( entry.getKey() );
				break;
			}
		}
		
		originalList.remove( server );
		addServer( server );
	}
	
	public void deleteServer( ServerTransaction transaction , MetaEnvServer server ) {
		int index = originalList.indexOf( server );
		if( index < 0 )
			return;
		
		originalList.remove( index );
		serverMap.remove( server.NAME );
		startInfo.removeServer( transaction , server );
	}
	
	public void setBaseline( ServerTransaction transaction , String baselineDC ) throws Exception {
		super.setSystemStringProperty( PROPERTY_BASELINE , baselineDC );
	}
	
	public void setOffline( ServerTransaction transaction , boolean offline ) throws Exception {
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

	public void deleteHostAccount( ServerTransaction transaction , ServerHostAccount account ) throws Exception {
		super.deleteObject();
	}

	public void setStartInfo( ServerTransaction transaction , MetaEnvStartInfo startInfo ) throws Exception {
		this.startInfo = startInfo;
	}
	
}
