package org.urm.engine.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.ConfReader;
import org.urm.common.PropertyController;
import org.urm.engine.shell.Account;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaEnvDC extends PropertyController {

	public Meta meta;
	public MetaEnv env;
	
	public String NAME;
	private String BASELINE;
	
	public MetaEnvDeployment deploy;
	public MetaEnvStartInfo startInfo;
	public List<MetaEnvServer> originalList;
	public Map<String,MetaEnvServer> serverMap;
	
	public MetaEnvDC( Meta meta , MetaEnv env ) {
		super( "dc" );
		this.meta = meta;
		this.env = env;
	}

	@Override
	public boolean isValid() {
		if( super.isLoadFailed() )
			return( false );
		return( true );
	}
	
	public MetaEnvDC copy( ActionBase action , Meta meta , MetaEnv env ) throws Exception {
		MetaEnvDC r = new MetaEnvDC( meta , env );
		r.initCopyStarted( this , env.getProperties() );
		
		if( deploy != null )
			r.deploy = deploy.copy( action , meta , this );
		if( startInfo != null )
			r.startInfo = startInfo.copy( action , meta , this );
		for( MetaEnvServer server : originalList ) {
			MetaEnvServer rserver = server.copy( action , meta , this );
			r.addServer( rserver );
		}
		
		r.scatterSystemProperties( action );
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

		properties.loadFromNodeAttributes( node );
		scatterSystemProperties( action );
		
		if( loadProps ) {
			properties.loadFromNodeElements( node );
			properties.resolveRawProperties();
		}
		
		loadServers( action , node , loadProps );
		loadStartOrder( action , node );
		loadDeployment( action , node );
		
		super.initFinished();
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
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		if( !super.isLoaded() )
			return;
	}
	
}
