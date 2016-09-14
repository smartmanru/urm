package org.urm.engine.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.ConfReader;
import org.urm.common.PropertySet;
import org.urm.engine.shell.Account;
import org.w3c.dom.Node;

public class MetaEnvDC {

	public Meta meta;
	public MetaEnv env;
	
	public String NAME;
	private String BASELINE;
	public PropertySet properties;
	
	public MetaEnvDeployment deploy;
	public MetaEnvStartInfo startInfo;
	public List<MetaEnvServer> originalList;
	public Map<String,MetaEnvServer> serverMap;
	
	public MetaEnvDC( Meta meta , MetaEnv env ) {
		this.meta = meta;
		this.env = env;
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
		properties = new PropertySet( "dc" , env.properties );
		properties.loadRawFromNodeAttributes( node );
		scatterSystemProperties( action );
		
		if( loadProps ) {
			properties.loadRawFromNodeElements( node );
			properties.resolveRawProperties();
		}
		
		loadServers( action , node , loadProps );
		loadStartOrder( action , node );
		loadDeployment( action , node );
	}

	public String[] getPropertyList( ActionBase action ) throws Exception {
		return( properties.getRunningProperties() );
	}

	public String getPropertyValue( ActionBase action , String var ) throws Exception {
		return( properties.getPropertyAny( var ) );
	}
	
	private void scatterSystemProperties( ActionBase action ) throws Exception {
		NAME = properties.getSystemRequiredStringProperty( "name" );
		action.trace( "load properties of dc=" + NAME );
		
		BASELINE = properties.getSystemStringProperty( "configuration-baseline" , "" );
		if( BASELINE.equals( "default" ) )
			BASELINE = NAME;
		
		properties.finishRawProperties();
	}
	
	public void loadDeployment( ActionBase action , Node node ) throws Exception {
		deploy = new MetaEnvDeployment( meta , this );
		
		Node deployment = ConfReader.xmlGetFirstChild( node , "deployment" );
		if( deployment == null )
			return;
		
		deploy.load( action , deployment );
	}
	
	public void loadServers( ActionBase action , Node node , boolean loadProps ) throws Exception {
		serverMap = new HashMap<String,MetaEnvServer>();
		originalList = new LinkedList<MetaEnvServer>();
		
		Node[] items = ConfReader.xmlGetChildren( node , "server" );
		if( items == null )
			return;
		
		for( Node srvnode : items ) {
			MetaEnvServer server = new MetaEnvServer( meta , this );
			server.load( action , srvnode , loadProps );
			serverMap.put( server.NAME , server );
			originalList.add( server );
		}
	}
	
	public void resolveLinks( ActionBase action ) throws Exception {
		for( MetaEnvServer server : originalList )
			server.resolveLinks( action );
	}
	
	public void loadStartOrder( ActionBase action , Node node ) throws Exception {
		startInfo = new MetaEnvStartInfo( meta , this );
		
		Node startorder = ConfReader.xmlGetFirstChild( node , "startorder" );
		if( startorder == null )
			return;
		
		startInfo.load( action , startorder );
	}

	public MetaEnvServer findServer( ActionBase action , String name ) throws Exception {
		return( serverMap.get( name ) );
	}
	
	public MetaEnvServer getServer( ActionBase action , String name ) throws Exception {
		MetaEnvServer server = serverMap.get( name );
		if( server == null )
			action.exit1( _Error.UnknownServer1 , "unknown server=" + name , name );
		return( server );
	}
	
	public Map<String,MetaEnvServer> getServerMap( ActionBase action ) throws Exception {
		return( serverMap );
	}

	public List<MetaEnvServer> getOriginalServerList( ActionBase action ) throws Exception {
		return( originalList );
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
			if( server.isDatabase( action ) )
				return( true );
		
		return( false );
	}
	
}