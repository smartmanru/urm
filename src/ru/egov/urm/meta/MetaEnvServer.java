package ru.egov.urm.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.PropertySet;
import ru.egov.urm.meta.Metadata.VarDBMSTYPE;
import ru.egov.urm.meta.Metadata.VarDEPLOYTYPE;
import ru.egov.urm.meta.Metadata.VarOSTYPE;
import ru.egov.urm.meta.Metadata.VarSERVERTYPE;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.shell.Account;

public class MetaEnvServer {

	public MetaEnvDC dc;
	
	public String NAME;
	public String XDOC;
	private String BASELINE;
	public boolean OFFLINE;
	
	public VarSERVERTYPE TYPE;
	public String ROOTPATH;
	public String BINPATH;
	public String SERVICENAME;
	public int PORT;
	private String NLBSERVER;
	public MetaEnvServer nlbServer;
	private String PROXYSERVER;
	public MetaEnvServer proxyServer;
	private String STATICSERVER;
	public MetaEnvServer staticServer;
	private String SUBORDINATESERVERS;
	public MetaEnvServer[] subordinateServers;
	public int STARTTIME;
	public int STOPTIME;
	public VarDEPLOYTYPE DEPLOYTYPE;
	public String DEPLOYPATH;
	public String LINKFROMPATH;
	public String DEPLOYSCRIPT;
	private String HOTDEPLOYSERVER;
	public MetaEnvServer hotdeployServer;
	public String HOTDEPLOYPATH;
	public String HOTDEPLOYDATA;
	public String WEBDOMAIN;
	public String WEBMAINURL;
	public String APPSERVER;
	public String APPSERVERVERSION;
	public String LOGPATH;
	public String LOGFILEPATH;

	public VarDBMSTYPE DBMSTYPE;
	public String DBMSADDR;
	public String DATAGROUPS;
	public String ADMSCHEMA;
	public Map<String,MetaDatabaseDatagroup> datagroupMap;
	public MetaDatabaseSchema admSchema;
	
	public String ALIGNED;
	public String REGIONS;
	
	public VarOSTYPE OSTYPE;
	
	public PropertySet properties;

	List<MetaEnvServerDeployment> deployments;
	List<MetaEnvServerNode> nodes;
	
	public boolean primary;
	public MetaEnvStartGroup startGroup;
	
	public MetaEnvServer( MetaEnvDC dc ) {
		this.dc = dc;
		this.primary = false;
	}

	public String getFullId( ActionBase action ) throws Exception {
		return( dc.getFullId( action ) + "-" + NAME );
	}
	
	public boolean hasBaseline( ActionBase action ) throws Exception {
		if( BASELINE.isEmpty() )
			return( false );
		return( true );
	}
	
	public String getBaselineServer( ActionBase action ) throws Exception {
		return( BASELINE );
	}
	
	public void load( ActionBase action , Node node , boolean loadProps ) throws Exception {
		properties = new PropertySet( "server" , dc.properties );
		properties.loadFromAttributes( action , node );
		scatterSystemProperties( action );
		if( loadProps )
			properties.loadFromElements( action , node );

		loadNodes( action , node , loadProps );
		loadDeployments( action , node );
	}
	
	public String[] getPropertyList( ActionBase action ) throws Exception {
		return( properties.getOwnProperties( action ) );
	}
	
	public String getPropertyValue( ActionBase action , String var ) throws Exception {
		return( properties.getProperty( action , var ) );
	}
	
	public void resolveLinks( ActionBase action ) throws Exception {
		if( NLBSERVER != null && !NLBSERVER.isEmpty() )
			nlbServer = dc.getServer( action , NLBSERVER );
		if( PROXYSERVER != null && !PROXYSERVER.isEmpty() )
			proxyServer = dc.getServer( action , PROXYSERVER );
		if( STATICSERVER != null && !STATICSERVER.isEmpty() )
			staticServer = dc.getServer( action , STATICSERVER );
		if( HOTDEPLOYSERVER != null && !HOTDEPLOYSERVER.isEmpty() )
			hotdeployServer = dc.getServer( action , HOTDEPLOYSERVER );
		if( SUBORDINATESERVERS != null && !SUBORDINATESERVERS.isEmpty() ) {
			String[] SERVERS = Common.splitSpaced( SUBORDINATESERVERS );
			subordinateServers = new MetaEnvServer[ SERVERS.length ];
			for( int k = 0; k < SERVERS.length; k++ )
				subordinateServers[ k ] = dc.getServer( action , SERVERS[ k ] );
		}
		
		// verify aligned
		if( TYPE == VarSERVERTYPE.DATABASE ) {
			for( String id : Common.splitSpaced( ALIGNED ) )
				action.meta.distr.database.alignedGetIDByBame( action , id );
		}
	}
	
	public Map<String,MetaEnvServer> getAssociatedServers( ActionBase action ) throws Exception {
		Map<String,MetaEnvServer> servers = new HashMap<String,MetaEnvServer>();
		if( nlbServer != null )
			servers.put( nlbServer.NAME , nlbServer );
		if( proxyServer != null )
			servers.put( proxyServer.NAME , proxyServer );
		if( staticServer != null )
			servers.put( staticServer.NAME , staticServer );
		if( subordinateServers != null ) {
			for( MetaEnvServer server : subordinateServers )
				servers.put( server.NAME , server );
		}
		return( servers );
	}
	
	public void setStartGroup( ActionBase action , MetaEnvStartGroup group ) throws Exception {
		primary = true;
		this.startGroup = group;
	}
	
	private void scatterSystemProperties( ActionBase action ) throws Exception {
		List<String> systemProps = new LinkedList<String>();
		datagroupMap = new HashMap<String,MetaDatabaseDatagroup>(); 
		
		NAME = properties.getSystemRequiredProperty( action , "name" , systemProps );
		action.trace( "load properties of server=" + NAME );
		
		BASELINE = properties.getSystemProperty( action , "configuration-baseline" , "" , systemProps ); 
		if( BASELINE.equals( "default" ) )
			BASELINE = NAME;
		
		TYPE = action.meta.getServerType( action , properties.getSystemRequiredProperty( action , "type" , systemProps ) );
		OSTYPE = action.meta.getOSType( action , properties.getSystemProperty( action , "ostype" , "unix" , systemProps ) );
		OFFLINE = properties.getSystemBooleanProperty( action , "offline" , false , systemProps );
		XDOC = properties.getSystemProperty( action , "xdoc" , NAME , systemProps );
		
		if( TYPE == VarSERVERTYPE.DATABASE ) {
			DBMSTYPE = action.meta.getDbmsType( action , properties.getSystemRequiredProperty( action , "dbmstype" , systemProps ) );
			DBMSADDR = properties.getSystemRequiredProperty( action , "dbmsaddr" , systemProps );
			DATAGROUPS = properties.getSystemRequiredProperty( action , "datagroups" , systemProps );
			ALIGNED = properties.getSystemProperty( action , "aligned" , "" , systemProps );
			REGIONS = properties.getSystemProperty( action , "regions" , "" , systemProps );
			ADMSCHEMA = properties.getSystemProperty( action , "admschema" , "" , systemProps );
			
			MetaDatabase database = action.meta.distr.database;
			for( String dg : Common.splitSpaced( DATAGROUPS ) ) {
				MetaDatabaseDatagroup datagroup = database.getDatagroup( action , dg );
				datagroupMap.put( datagroup.NAME , datagroup );
			}
			
			admSchema = database.getSchema( action , ADMSCHEMA );
		}
		else
		if( TYPE != VarSERVERTYPE.UNKNOWN ) {
			ROOTPATH = properties.getSystemProperty( action , "rootpath" , "" , systemProps );
			BINPATH = properties.getSystemProperty( action , "binpath" , "" , systemProps );
			SERVICENAME = properties.getSystemProperty( action , "servicename" , "" , systemProps );
			PORT = properties.getSystemIntProperty( action , "port" , 0 , systemProps );
			NLBSERVER = properties.getSystemProperty( action , "nlbserver" , "" , systemProps );
			PROXYSERVER = properties.getSystemProperty( action , "proxy-server" , "" , systemProps );
			STATICSERVER = properties.getSystemProperty( action , "static-server" , "" , systemProps );
			SUBORDINATESERVERS = properties.getSystemProperty( action , "subordinate-servers" , "" , systemProps );
			STARTTIME = properties.getSystemIntProperty( action , "starttime" , 0 , systemProps );
			STOPTIME = properties.getSystemIntProperty( action , "stoptime" , 0 , systemProps );
			DEPLOYTYPE = action.meta.getDeployType( action , properties.getSystemProperty( action , "deploytype" , "default" , systemProps ) );
			DEPLOYPATH = properties.getSystemProperty( action , "deploypath" , "" , systemProps );
			LINKFROMPATH = properties.getSystemProperty( action , "linkfrompath" , "" , systemProps );
			DEPLOYSCRIPT = properties.getSystemProperty( action , "deployscript" , "" , systemProps );
			HOTDEPLOYSERVER = properties.getSystemProperty( action , "hotdeployserver" , "" , systemProps );
			HOTDEPLOYPATH = properties.getSystemProperty( action , "hotdeploypath" , "" , systemProps );
			HOTDEPLOYDATA = properties.getSystemProperty( action , "hotdeploydata" , "" , systemProps );
			WEBDOMAIN = properties.getSystemProperty( action , "webdomain" , "" , systemProps );
			WEBMAINURL = properties.getSystemProperty( action , "webmainurl" , "" , systemProps );
			APPSERVER = properties.getSystemProperty( action , "appserver" , "" , systemProps );
			APPSERVERVERSION = properties.getSystemProperty( action , "appserver-version" , "" , systemProps );
			LOGPATH = properties.getSystemProperty( action , "logpath" , "" , systemProps );
			LOGFILEPATH = properties.getSystemProperty( action , "logfilepath" , "" , systemProps );
		}
		
		properties.checkUnexpected( action , systemProps );
	}
	
	private void loadNodes( ActionBase action , Node node , boolean loadProps ) throws Exception {
		nodes = new LinkedList<MetaEnvServerNode>(); 
		
		Node[] items = ConfReader.xmlGetChildren( action , node , "node" );
		if( items == null )
			return;
		
		int pos = 1;
		for( Node snnode : items ) {
			MetaEnvServerNode sn = new MetaEnvServerNode( this , pos );
			sn.load( action , snnode , loadProps );
			nodes.add( sn );
			pos++;
		}
	}
	
	private void loadDeployments( ActionBase action , Node node ) throws Exception {
		deployments = new LinkedList<MetaEnvServerDeployment>(); 
		
		Node[] items = ConfReader.xmlGetChildren( action , node , "deploy" );
		if( items == null )
			return;
		
		for( Node dpnode : items ) {
			MetaEnvServerDeployment dp = new MetaEnvServerDeployment( this );
			dp.load( action , dpnode );
			deployments.add( dp );
		}
	}

	public List<MetaEnvServerNode> getNodes( ActionBase action ) throws Exception {
		return( nodes );
	}

	public MetaEnvServerNode getNode( ActionBase action , int node ) throws Exception {
		if( node < 1 || node > nodes.size() )
			action.exit( "invalid node=" + node + ", server=" + NAME );
		return( nodes.get( node - 1 ) );
	}

	public boolean hasWebServices( ActionBase action ) throws Exception {
		for( MetaEnvServerDeployment deploy : deployments ) {
			if( deploy.comp != null )
				if( deploy.comp.hasWebServices( action ) )
					return( true );
		}
		return( false );
	}

	public List<MetaEnvServerDeployment> getDeployments( ActionBase action ) throws Exception {
		return( deployments );
	}

	public String getFullBinPath( ActionBase action ) throws Exception {
		action.checkRequired( !ROOTPATH.isEmpty() , "rootpath" );
		action.checkRequired( !BINPATH.isEmpty() , "binpath" );
		return( Common.getPath( ROOTPATH , BINPATH ) );
	}
	
	public boolean checkDeployBinaries( ActionBase action ) throws Exception {
		for( MetaEnvServerDeployment deploy : deployments )
			if( deploy.binaryItem != null )
				return( true );
		return( false );
	}

	public String getNodesAsStringByHost( ActionBase action , String host ) throws Exception {
		String s = "";
		for( MetaEnvServerNode node : nodes ) {
			Account account = action.getAccount( node );
			if( account.HOST.equals( host ) ) {
				if( !s.isEmpty() )
					s += " ";
				s += node.POS;
			}
		}
		return( s );
	}

	public String getNodesAsStringByAccount( ActionBase action , Account account ) throws Exception {
		String s = "";
		for( MetaEnvServerNode node : nodes ) {
			Account nodeAccount = action.getAccount( node );
			if( nodeAccount.HOSTLOGIN.equals( account.HOSTLOGIN ) ) {
				if( !s.isEmpty() )
					s += " ";
				s += node.POS;
			}
		}
		return( s );
	}

	public List<MetaEnvServerNode> getNodes( ActionBase action , String[] NODES ) throws Exception {
		List<MetaEnvServerNode> list = new LinkedList<MetaEnvServerNode>();
		for( String NODE : NODES ) {
			MetaEnvServerNode node = getNode( action , Integer.parseInt( NODE ) );
			list.add( node );
		}
		return( list );
	}
	
	public boolean isConfigurable( ActionBase action ) throws Exception {
		if( TYPE == VarSERVERTYPE.GENERIC_COMMAND || 
			TYPE == VarSERVERTYPE.GENERIC_SERVER ||
			TYPE == VarSERVERTYPE.GENERIC_WEB ||
			TYPE == VarSERVERTYPE.SERVICE ) 
			return( true );
		return( false );
	}
	
	public boolean hasConfiguration( ActionBase action ) throws Exception {
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( deployment.confItem != null )
				return( true );
			if( deployment.comp != null )
				if( deployment.comp.hasConfItems( action ) )
					return( true );
		}
		return( false );
	}

	public boolean hasConfItemDeployment( ActionBase action , MetaDistrConfItem confItem ) throws Exception {
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( deployment.hasConfItemDeployment( action , confItem ) )
				return( true );
		}
		return( false );
	}

	public boolean hasBinaryItemDeployment( ActionBase action , MetaDistrBinaryItem binaryItem ) throws Exception {
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( deployment.hasBinaryItemDeployment( action , binaryItem ) )
				return( true );
		}
		return( false );
	}

	public boolean hasDatabaseItemDeployment( ActionBase action , MetaDatabaseDatagroup datagroup ) throws Exception {
		if( datagroupMap.containsKey( datagroup.NAME ) )
			return( true );
		return( false );
	}
	
	public boolean hasDatabaseItemDeployment( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		for( MetaDatabaseDatagroup datagroup : delivery.getDatabaseDatagroups( action ).values() ) {
			if( hasDatabaseItemDeployment( action , datagroup ) ) 
				return( true );
		}
		return( false );
	}
	
	public Map<String, MetaDistrConfItem> getConfItems( ActionBase action ) throws Exception {
		Map<String, MetaDistrConfItem> confs = new HashMap<String, MetaDistrConfItem>(); 
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( deployment.confItem != null ) {
				MetaDistrConfItem conf = deployment.confItem; 
				confs.put( conf.KEY , conf );
			}
			else
			if( deployment.comp != null ) {
				for( MetaDistrComponentItem item : deployment.comp.getConfItems( action ).values() ) {
					if( item.confItem != null ) {
						MetaDistrConfItem conf = item.confItem; 
						confs.put( conf.KEY , conf );
					}
				}
			}
		}
		return( confs );
	}

	public MetaEnvServerNode getPrimaryNode( ActionBase action ) throws Exception {
		for( MetaEnvServerNode node : nodes ) {
			if( !node.OFFLINE )
				return( node );
		}
		
		action.exit( "unable to find primary node of server=" + NAME );
		return( null );
	}

	public MetaEnvServerLocation[] getLocations( ActionBase action , boolean binary , boolean conf ) throws Exception {
		Map<String,MetaEnvServerLocation> locations = new HashMap<String,MetaEnvServerLocation>();
		for( MetaEnvServerDeployment deployment : deployments ) {
			String deployPath = deployment.getDeployPath( action );
			VarDEPLOYTYPE deployType = deployment.getDeployType( action );
			String key = Common.getEnumLower( deployType ) + "-" + deployPath;
			
			if( deployment.binaryItem != null ) {
				if( binary ) {
					MetaEnvServerLocation location = locations.get( key ); 
					if( location == null ) {
						location = new MetaEnvServerLocation( this , deployType , deployPath );
						locations.put( key , location );
					}
					location.addBinaryItem( action , deployment.binaryItem , "" );
				}
			}
			else
			if( deployment.confItem != null ) {
				if( conf ) {
					MetaEnvServerLocation location = locations.get( key ); 
					if( location == null ) {
						location = new MetaEnvServerLocation( this , deployType , deployPath );
						locations.put( key , location );
					}
					location.addConfItem( action , deployment.confItem );
				}
			}
			else
			if( deployment.comp != null ) {
				if( binary && deployment.comp.hasBinaryItems( action ) ) {
					MetaEnvServerLocation location = locations.get( key ); 
					if( location == null ) {
						location = new MetaEnvServerLocation( this , deployType , deployPath );
						locations.put( key , location );
					}
					
					for( MetaDistrComponentItem item : deployment.comp.mapBinaryItems.values() ) {
						if( item.binaryItem != null )
							location.addBinaryItem( action , item.binaryItem , item.DEPLOYNAME );
					}
				}
				
				if( conf && deployment.comp.hasConfItems( action ) ) {
					MetaEnvServerLocation location = locations.get( key ); 
					if( location == null ) {
						location = new MetaEnvServerLocation( this , deployType , deployPath );
						locations.put( key , location );
					}
					
					for( MetaDistrComponentItem item : deployment.comp.mapBinaryItems.values() ) {
						if( item.confItem != null )
							location.addConfItem( action , item.confItem );
					}
				}
			}
		}
		
		return( locations.values().toArray( new MetaEnvServerLocation[0] ) );
	}

	public boolean isDeployPossible( ActionBase action ) throws Exception {
		if( TYPE == VarSERVERTYPE.DATABASE || 
			TYPE == VarSERVERTYPE.GENERIC_NOSSH ||
			TYPE == VarSERVERTYPE.UNKNOWN ) {
			action.trace( "ignore due to server type=" + Common.getEnumLower( TYPE ) );
			return( false );
		}
		
		// ignore deployment type
		if( DEPLOYTYPE == VarDEPLOYTYPE.MANUAL ||
			DEPLOYTYPE == VarDEPLOYTYPE.NONE ) {
			action.trace( "ignore due to server deployment type=" + Common.getEnumLower( DEPLOYTYPE ) );
			return( false );
		}
		
		return( true );
	}

	public Map<String,MetaDatabaseSchema> getSchemaSet( ActionBase action ) throws Exception {
		Map<String,MetaDatabaseSchema> schemaMap = new HashMap<String,MetaDatabaseSchema>();
		for( MetaDatabaseDatagroup dg : datagroupMap.values() ) {
			for( MetaDatabaseSchema schema : dg.getSchemes( action ).values() )
				schemaMap.put( schema.SCHEMA , schema );
		}
		return( schemaMap );
	}

	public MetaEnvServerNode getStandbyNode( ActionBase action ) throws Exception {
		for( MetaEnvServerNode node : getNodes( action ) ) {
			if( node.STANDBY )
				return( node );
		}
		
		action.exit( "unable to find standby node" );
		return( null );
	}

	public MetaEnvServerNode getActiveNode( ActionBase action ) throws Exception {
		for( MetaEnvServerNode node : getNodes( action ) ) {
			if( !node.OFFLINE )
				return( node );
		}
		
		action.exit( "unable to find active node" );
		return( null );
	}
	
}
