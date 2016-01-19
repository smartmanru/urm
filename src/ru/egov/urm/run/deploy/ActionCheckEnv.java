package ru.egov.urm.run.deploy;

import ru.egov.urm.Common;
import ru.egov.urm.SimpleHttp;
import ru.egov.urm.meta.MetaDistrComponentWS;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerDeployment;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.Metadata.VarPROCESSMODE;
import ru.egov.urm.meta.Metadata.VarSERVERTYPE;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScope;
import ru.egov.urm.run.ActionScopeSet;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.run.ActionScopeTargetItem;
import ru.egov.urm.run.database.DatabaseClient;

public class ActionCheckEnv extends ActionBase {

	String S_CHECKENV_TOTAL_SERVERS_FAILED = "";

	boolean S_CHECKENV_TARGET_FAILED;
	String S_CHECKENV_TARGET_SERVERS_FAILED;
	String S_CHECKENV_TARGET_NODES_FAILED;
	String S_CHECKENV_TARGET_COMPS_FAILED;
	
	boolean S_CHECKENV_SERVER_FAILED;
	String S_CHECKENV_SERVER_NODES_FAILED;
	String S_CHECKENV_SERVER_COMPS_FAILED;

	boolean S_CHECKENV_NODE_FAILED;
	boolean S_CHECKENV_NODE_STOPPED;
	
	public ActionCheckEnv( ActionBase action , String stream ) {
		super( action , stream );
	}

	@Override protected void runBefore( ActionScope scope ) throws Exception {
		// check all processes
		logAction( "check environment=" + meta.env.ID + " ..." );
	}

	@Override protected void runAfter( ActionScope scope ) throws Exception {
		String status = "SUCCESSFUL";
		if( !S_CHECKENV_TOTAL_SERVERS_FAILED.isEmpty() )
			super.setFailed();
	
		if( super.isFailed() )
			status = "FAILED";
		
		logAction( "total status is " + status );
	}
	
	@Override protected void runBefore( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		log( "execute datacenter=" + set.dc.NAME + " ..." );
	}

	@Override protected void runAfter( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		String F_STATUSOBJECT = set.dc.NAME;
		if( !S_CHECKENV_TOTAL_SERVERS_FAILED.isEmpty() ) {
			log( "## dc " + F_STATUSOBJECT + " check FAILED: issues on servers - {" + S_CHECKENV_TOTAL_SERVERS_FAILED + "}" );
			return;
		}
		
		log( "## dc " + F_STATUSOBJECT + " check OK" );
	}
	
	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		ActionScopeSet set = target.set;
		
		try {
			S_CHECKENV_TARGET_FAILED = false;
			S_CHECKENV_TARGET_SERVERS_FAILED = "";
			S_CHECKENV_TARGET_NODES_FAILED = "";
			S_CHECKENV_TARGET_COMPS_FAILED = "";
			
			// execute server
			log( "============================================ check server=" + target.envServer.NAME + " ..." );
	
			checkOneServer( target , target.envServer , true , "main" );
			
			// check associated if specific servers and not specific nodes 
			if( set.setFull == false && target.itemFull == true ) {
				if( target.envServer.nlbServer != null ) 
					checkOneServer( target , target.envServer.nlbServer , false , "nlb" );
				if( target.envServer.staticServer != null ) 
					checkOneServer( target , target.envServer.staticServer , false , "static" );
				if( target.envServer.subordinateServers != null ) {
					for( MetaEnvServer server : target.envServer.subordinateServers ) 
						checkOneServer( target , server , false , "subordinate" );
				}
			}
		}
		catch( Throwable e ) {
			S_CHECKENV_TARGET_FAILED = true;
			log( e );
		}
		
		// check status
		String F_STATUSOBJECT = set.dc.NAME + "." + target.envServer.NAME;
		if( !S_CHECKENV_TARGET_FAILED ) {
			log( "## server " + F_STATUSOBJECT + " check OK" );
			return( true );
		}

		String MSG = "## server " + F_STATUSOBJECT + " check FAILED:";
		if( !S_CHECKENV_TARGET_SERVERS_FAILED.isEmpty() )
			MSG = Common.addToList( MSG , "associated.failed={" + S_CHECKENV_TARGET_SERVERS_FAILED + "}" , " " );
		if( !S_CHECKENV_TARGET_NODES_FAILED.isEmpty() )
			MSG = Common.addToList( MSG , "nodes.failed={" + S_CHECKENV_TARGET_NODES_FAILED + "}" , " " );
		if( !S_CHECKENV_TARGET_COMPS_FAILED.isEmpty() )
			MSG = Common.addToList( MSG , "components.failed={" + S_CHECKENV_TARGET_COMPS_FAILED + "}" , " " );
		log( MSG );
		
		S_CHECKENV_TOTAL_SERVERS_FAILED = Common.addItemToUniqueSpacedList( S_CHECKENV_TOTAL_SERVERS_FAILED , target.NAME );
		return( true );
	}

	private void checkOneServer( ActionScopeTarget target , MetaEnvServer server , boolean main , String role ) throws Exception {
		S_CHECKENV_SERVER_FAILED = false;
		S_CHECKENV_SERVER_NODES_FAILED = "";
		S_CHECKENV_SERVER_COMPS_FAILED = "";

		// ignore command servers except when specifically called 
		if( server.TYPE == VarSERVERTYPE.GENERIC_COMMAND ) {
			if( options.OPT_ALL == false || main == false ) {
				debug( "ignore command server=" + server.NAME );
				return;
			}
		}
		
		log( "check " + role + " server=" + server.NAME + " ..." );

		if( main ) {
			debug( "check nodes ..." );
			boolean someNodeAvailable = false;
			for( ActionScopeTargetItem node : target.getItems( this ) ) {
				checkOneServerNode( server , node.envServerNode , main );
				if( !S_CHECKENV_NODE_STOPPED )
					someNodeAvailable = true;
			}
			
			S_CHECKENV_TARGET_NODES_FAILED = S_CHECKENV_SERVER_NODES_FAILED;
			
			if( target.itemFull && someNodeAvailable )
				checkOneServerWhole( server );
			
			S_CHECKENV_TARGET_COMPS_FAILED = S_CHECKENV_SERVER_COMPS_FAILED;
		}
		else {
			for( MetaEnvServerNode node : server.getNodes( this ) )
				checkOneServerNode( server , node , false );
		}
		
		// add server status to target
		if( !S_CHECKENV_SERVER_FAILED )
			return;
		
		S_CHECKENV_TARGET_FAILED = true;
		if( !main )
			S_CHECKENV_TARGET_SERVERS_FAILED = Common.addItemToUniqueSpacedList( S_CHECKENV_TARGET_SERVERS_FAILED , server.NAME );
	}

	private void checkOneServerWhole( MetaEnvServer server ) throws Exception {
		debug( "check whole server ..." );
		
		boolean wholeOk = true;
		
		VarSERVERTYPE F_SERVER_TYPE = server.TYPE;
		if( F_SERVER_TYPE == VarSERVERTYPE.GENERIC_WEB ) {
			if( !server.WEBMAINURL.isEmpty() )
				if( !checkOneServerWholeUrl( server.WEBMAINURL , "main web url" ) )
					wholeOk = false;
		}
		else 
		if( F_SERVER_TYPE == VarSERVERTYPE.SERVICE ||
			F_SERVER_TYPE == VarSERVERTYPE.GENERIC_SERVER || 
			F_SERVER_TYPE == VarSERVERTYPE.GENERIC_COMMAND ||
			F_SERVER_TYPE == VarSERVERTYPE.GENERIC_NOSSH ) {
			if( !checkOneServerWholeComps( server ) )
				wholeOk = false;
		}
		else 
		if( F_SERVER_TYPE == VarSERVERTYPE.DATABASE ) {
			if( !checkOneServerWholeDatabase( server ) )
				wholeOk = false;
		}
		else
			exit( "unknown server type=" + Common.getEnumLower( F_SERVER_TYPE ) );
		
		if( wholeOk )
			return;
		
		S_CHECKENV_SERVER_FAILED = true;
	}
	
	private boolean checkOneServerWholeUrl( String URL , String role ) throws Exception {
		boolean res = SimpleHttp.check( this , URL );
		String ok = ( res )? "OK" : "FAILED";
		String msg = "check " + role + " " + URL + ": " + ok; 
		if( res )
			debug( msg );
		else
			log( msg );
		return( res );
	}

	private boolean checkOneServerWholeComps( MetaEnvServer server ) throws Exception {
		if( server.WEBDOMAIN.isEmpty() )
			return( true );
		
		return( checkOneServerWebServices( server , server.WEBDOMAIN ) );
	}
	
	private boolean checkOneServerWebServices( MetaEnvServer server , String ACCESSPOINT ) throws Exception {
		if( !server.hasWebServices( this ) )
			return( true );
		
		// by comps
		boolean ok = true;
		for( MetaEnvServerDeployment deployment : server.getDeployments( this ) ) {
			if( deployment.comp == null )
				continue;
			
			for( MetaDistrComponentWS ws : deployment.comp.getWebServices( this ) ) {
				String URL = "http://" + ACCESSPOINT + "/" + ws.URL + "?wsdl";
				if( !checkOneServerWholeUrl( URL , "web service" ) ) {
					ok = false;
					S_CHECKENV_SERVER_COMPS_FAILED = Common.addItemToUniqueSpacedList( S_CHECKENV_SERVER_COMPS_FAILED , deployment.comp.NAME );
				}
			}
		}
		
		if( ok )
			return( true );
		
		S_CHECKENV_SERVER_FAILED = true;
		return( false );
	}
	
	private boolean checkOneServerWholeDatabase( MetaEnvServer server ) throws Exception {
		DatabaseClient process = new DatabaseClient( server );
		if( process.checkConnect( this ) )
			return( true );
		
		log( "database server=" + server.NAME + ": client is not available" );
		return( false );
	}
	
	private void checkOneServerNode( MetaEnvServer server , MetaEnvServerNode node , boolean main ) throws Exception {
		S_CHECKENV_NODE_FAILED = false;
		S_CHECKENV_NODE_STOPPED = false;
		
		log( "node " + node.POS + "=" + node.HOSTLOGIN );

		if( checkOneServerNodeStatus( server , node ) ) {
			if( !checkOneServerNodeComps( server , node ) )
				S_CHECKENV_NODE_FAILED = true;
		}
		else {
			S_CHECKENV_NODE_FAILED = true;
			S_CHECKENV_NODE_STOPPED = true;
		}
		
		// check proxy node
		if( main && server.proxyServer != null ) { 
			log( "check proxy node ..." );
			if( !checkOneServerNodeStatus( server.proxyServer , node.getProxyNode( this ) ) )
				S_CHECKENV_NODE_FAILED = true;
		}

		// add to server
		if( S_CHECKENV_NODE_FAILED ) {
			S_CHECKENV_SERVER_FAILED = true;
			if( main )
				S_CHECKENV_SERVER_NODES_FAILED = Common.addItemToUniqueSpacedList( S_CHECKENV_SERVER_NODES_FAILED , "" + node.POS );
		}
	}
	
	private boolean checkOneServerNodeStatus( MetaEnvServer server , MetaEnvServerNode node ) throws Exception {
		ServerProcess process = new ServerProcess( server , node );
		try {
			process.gatherStatus( this );
		}
		catch( Throwable e ) {
			log( e );
			return( false );
		}
		
		if( process.isStarted( this ) )
			return( true );
		
		if( process.mode == VarPROCESSMODE.ERRORS )
			log( node.HOSTLOGIN + ": status=errors (" + process.cmdValue + ")" ); 
		else
		if( process.mode == VarPROCESSMODE.STARTING )
			log( node.HOSTLOGIN + ": status=starting" );
		else
		if( process.mode == VarPROCESSMODE.STOPPED )
			log( node.HOSTLOGIN + ": status=stopped" );
		else
			this.exitUnexpectedState();
		return( false );
	}
	
	private boolean checkOneServerNodeComps( MetaEnvServer server , MetaEnvServerNode node ) throws Exception {
		String ACCESSPOINT = node.getAccessPoint( this );
		return( checkOneServerWebServices( server , ACCESSPOINT ) );
	}
	
}
