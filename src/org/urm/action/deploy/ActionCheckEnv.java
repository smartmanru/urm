package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.action.ScopeState;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.action.database.DatabaseClient;
import org.urm.action.monitor.DatacenterStatus;
import org.urm.action.monitor.NodeStatus;
import org.urm.action.monitor.ServerStatus;
import org.urm.common.Common;
import org.urm.common.SimpleHttp;
import org.urm.meta.engine.ServerMonitoring;
import org.urm.meta.product.MetaDistrComponentWS;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerDeployment;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.product.Meta.VarPROCESSMODE;

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
	
	DatacenterStatus dcStatus;
	int dcCaptureIndex;
	
	public ActionCheckEnv( ActionBase action , String stream ) {
		super( action , stream );
	}

	@Override protected void runBefore( ActionScope scope ) throws Exception {
		// check all processes
		infoAction( "check environment=" + context.env.ID + " ..." );
	}

	@Override protected void runAfter( ActionScope scope ) throws Exception {
		String status = "SUCCESSFUL";
		if( !S_CHECKENV_TOTAL_SERVERS_FAILED.isEmpty() )
			super.fail0( _Error.CheckenvFailed0 , "Checkenv failed" );
	
		if( super.isFailed() )
			status = "FAILED";
		
		infoAction( "total status is " + status );
	}
	
	@Override protected void runBefore( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		dcStatus = new DatacenterStatus( this , set.dc );
		dcCaptureIndex = super.logStartCapture();
		info( "execute datacenter=" + set.dc.NAME + " ..." );
	}

	@Override protected void runAfter( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		String F_STATUSOBJECT = set.dc.NAME;
		if( !S_CHECKENV_TOTAL_SERVERS_FAILED.isEmpty() )
			info( "## dc " + F_STATUSOBJECT + " check FAILED: issues on servers - {" + S_CHECKENV_TOTAL_SERVERS_FAILED + "}" );
		else
			info( "## dc " + F_STATUSOBJECT + " check OK" );
		
		dcStatus.setLog( super.logFinishCapture( dcCaptureIndex ) );
		super.eventSource.finishScopeItem( ServerMonitoring.EVENT_MONITORING_DATACENTER , dcStatus );
	}
	
	@Override protected SCOPESTATE executeScopeTarget( ActionScopeTarget target ) throws Exception {
		ActionScopeSet set = target.set;
		
		ScopeState parent = super.eventSource.findSetState( target.set );
		ServerStatus serverStatus = new ServerStatus( parent , target );
		int captureIndex = super.logStartCapture();
		try {
			S_CHECKENV_TARGET_FAILED = false;
			S_CHECKENV_TARGET_SERVERS_FAILED = "";
			S_CHECKENV_TARGET_NODES_FAILED = "";
			S_CHECKENV_TARGET_COMPS_FAILED = "";
			
			// execute server
			info( "============================================ check server=" + target.envServer.NAME + " ..." );
	
			checkOneServer( target , target.envServer , true , "main" , serverStatus );
			
			// check associated if specific servers and not specific nodes 
			if( set.setFull == false && target.itemFull == true ) {
				if( target.envServer.nlbServer != null ) 
					checkOneServer( target , target.envServer.nlbServer , false , "nlb" , serverStatus );
				if( target.envServer.staticServer != null ) 
					checkOneServer( target , target.envServer.staticServer , false , "static" , serverStatus );
				if( target.envServer.subordinateServers != null ) {
					for( MetaEnvServer server : target.envServer.subordinateServers ) 
						checkOneServer( target , server , false , "subordinate" , serverStatus );
				}
			}
		}
		catch( Throwable e ) {
			S_CHECKENV_TARGET_FAILED = true;
			handle( e );
		}
		
		// check status
		String F_STATUSOBJECT = set.dc.NAME + "." + target.envServer.NAME;
		if( !S_CHECKENV_TARGET_FAILED ) {
			info( "## server " + F_STATUSOBJECT + " check OK" );
		}
		else {
			String MSG = "## server " + F_STATUSOBJECT + " check FAILED:";
			if( !S_CHECKENV_TARGET_SERVERS_FAILED.isEmpty() )
				MSG = Common.addToList( MSG , "associated.failed={" + S_CHECKENV_TARGET_SERVERS_FAILED + "}" , " " );
			if( !S_CHECKENV_TARGET_NODES_FAILED.isEmpty() )
				MSG = Common.addToList( MSG , "nodes.failed={" + S_CHECKENV_TARGET_NODES_FAILED + "}" , " " );
			if( !S_CHECKENV_TARGET_COMPS_FAILED.isEmpty() )
				MSG = Common.addToList( MSG , "components.failed={" + S_CHECKENV_TARGET_COMPS_FAILED + "}" , " " );
			info( MSG );
			
			S_CHECKENV_TOTAL_SERVERS_FAILED = Common.addItemToUniqueSpacedList( S_CHECKENV_TOTAL_SERVERS_FAILED , target.NAME );
		}
		
		String[] log = super.logFinishCapture( captureIndex );
		serverStatus.setLog( log );
		super.eventSource.finishScopeItem( ServerMonitoring.EVENT_MONITORING_SERVER , serverStatus );
		
		return( SCOPESTATE.RunSuccess );
	}

	private void checkOneServer( ActionScopeTarget target , MetaEnvServer server , boolean main , String role , ServerStatus serverStatus ) throws Exception {
		S_CHECKENV_SERVER_FAILED = false;
		S_CHECKENV_SERVER_NODES_FAILED = "";
		S_CHECKENV_SERVER_COMPS_FAILED = "";

		// ignore offline server
		if( server.isOffline() ) {
			debug( "ignore offline server=" + server.NAME );
			return;
		}
		
		// ignore command servers except when specifically called 
		if( server.isCommand() ) {
			if( context.CTX_ALL == false || main == false ) {
				debug( "ignore command server=" + server.NAME );
				return;
			}
		}
		
		info( "check " + role + " server=" + server.NAME + " ..." );

		if( main ) {
			debug( "check nodes ..." );
			boolean someNodeAvailable = false;
			for( ActionScopeTargetItem node : target.getItems( this ) ) {
				checkOneServerNode( node , server , node.envServerNode , main , role , serverStatus );
				if( !S_CHECKENV_NODE_STOPPED )
					someNodeAvailable = true;
			}
			
			S_CHECKENV_TARGET_NODES_FAILED = S_CHECKENV_SERVER_NODES_FAILED;
			
			if( target.itemFull && someNodeAvailable )
				checkOneServerWhole( server , serverStatus );
			
			S_CHECKENV_TARGET_COMPS_FAILED = S_CHECKENV_SERVER_COMPS_FAILED;
		}
		else {
			for( MetaEnvServerNode node : server.getNodes() )
				checkOneServerNode( null , server , node , false , role , serverStatus );
		}
		
		// add server status to target
		if( !S_CHECKENV_SERVER_FAILED )
			return;
		
		S_CHECKENV_TARGET_FAILED = true;
		if( !main )
			S_CHECKENV_TARGET_SERVERS_FAILED = Common.addItemToUniqueSpacedList( S_CHECKENV_TARGET_SERVERS_FAILED , server.NAME );
	}

	private void checkOneServerWhole( MetaEnvServer server , ServerStatus serverStatus ) throws Exception {
		debug( "check whole server ..." );
		
		boolean wholeOk = true;
		
		if( server.isWebUser() ) {
			if( !server.WEBMAINURL.isEmpty() )
				if( !checkOneServerWholeUrl( server.WEBMAINURL , "main web url" , null , serverStatus ) )
					wholeOk = false;
		}

		if( server.isCallable() ) {
			if( !checkOneServerWholeComps( server , serverStatus ) )
				wholeOk = false;
		}

		if( server.isDatabase() ) {
			if( !checkOneServerWholeDatabase( server , serverStatus ) )
				wholeOk = false;
		}
		
		if( wholeOk )
			return;
		
		S_CHECKENV_SERVER_FAILED = true;
	}
	
	private boolean checkOneServerWholeUrl( String URL , String role , NodeStatus nodeStatus , ServerStatus serverStatus ) throws Exception {
		super.trace( role + ": check url=" + URL + " ..." );
		
		boolean res = SimpleHttp.check( this , URL );
		String ok = ( res )? "OK" : "FAILED";
		String msg = "check " + role + " " + URL + ": " + ok; 
		if( res )
			debug( msg );
		else
			info( msg );
		if( nodeStatus == null )
			serverStatus.addWholeUrlStatus( URL , role , res );
		else
			nodeStatus.addWholeUrlStatus( URL , role , res );
		return( res );
	}

	private boolean checkOneServerWholeComps( MetaEnvServer server , ServerStatus serverStatus ) throws Exception {
		if( server.WEBSERVICEURL.isEmpty() )
			return( true );
		
		return( checkOneServerWebServices( server , server.WEBSERVICEURL , null , serverStatus ) );
	}
	
	private boolean checkOneServerWebServices( MetaEnvServer server , String ACCESSPOINT , NodeStatus nodeStatus , ServerStatus serverStatus ) throws Exception {
		if( !server.hasWebServices( this ) )
			return( true );
		
		// by comps
		boolean ok = true;
		for( MetaEnvServerDeployment deployment : server.getDeployments() ) {
			if( deployment.comp == null )
				continue;
			
			for( MetaDistrComponentWS ws : deployment.comp.getWebServices() ) {
				String URL = ws.getURL( ACCESSPOINT ); 
				if( !checkOneServerWholeUrl( URL , "web service" , nodeStatus , serverStatus ) ) {
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
	
	private boolean checkOneServerWholeDatabase( MetaEnvServer server , ServerStatus serverStatus ) throws Exception {
		DatabaseClient process = new DatabaseClient();
		
		boolean res = true;
		if( !process.checkConnect( this , server ) ) {
			error( "database server=" + server.NAME + ": client is not available" );
			res = false;
		}
		
		serverStatus.addDatabaseStatus( res );
		return( res );
	}
	
	private void checkOneServerNode( ActionScopeTargetItem item , MetaEnvServer server , MetaEnvServerNode node , boolean main , String role , ServerStatus serverStatus ) {
		S_CHECKENV_NODE_FAILED = false;
		S_CHECKENV_NODE_STOPPED = false;

		NodeStatus nodeStatus = null;
		int captureIndex = 0;
		if( main ) {
			nodeStatus = new NodeStatus( serverStatus , item );
			captureIndex = super.logStartCapture();
		}
		
		info( "node " + node.POS + "=" + node.HOSTLOGIN );

		try {
			if( checkOneServerNodeStatus( server , node , nodeStatus ) ) {
				if( !checkOneServerNodeComps( server , node , nodeStatus ) ) {
					nodeStatus.setCompsFailed();
					S_CHECKENV_NODE_FAILED = true;
				}
			}
			else {
				S_CHECKENV_NODE_FAILED = true;
				S_CHECKENV_NODE_STOPPED = true;
			}
			
			// check proxy node
			if( main && server.proxyServer != null ) { 
				info( "check proxy node ..." );
				if( !checkOneServerNodeStatus( server.proxyServer , node.getProxyNode( this ) , null ) ) {
					S_CHECKENV_NODE_FAILED = true;
					nodeStatus.setProxyFailed( server.proxyServer );
				}
			}
		}
		catch( Throwable e ) {
			super.log( "check server node exception" , e );
		}

		// add to server
		if( S_CHECKENV_NODE_FAILED ) {
			S_CHECKENV_SERVER_FAILED = true;
			if( main )
				S_CHECKENV_SERVER_NODES_FAILED = Common.addItemToUniqueSpacedList( S_CHECKENV_SERVER_NODES_FAILED , "" + node.POS );
		}

		if( S_CHECKENV_NODE_FAILED )
			info( "## node " + node.POS + " check FAILED" );
		else
			info( "## node " + node.POS + " check OK" );
		
		if( main ) {
			String[] log = super.logFinishCapture( captureIndex );
			nodeStatus.setLog( log );
			super.eventSource.finishScopeItem( ServerMonitoring.EVENT_MONITORING_NODE , nodeStatus );
			serverStatus.addNodeStatus( nodeStatus ); 
		}
		else
			serverStatus.addRoleStatus( role , node , S_CHECKENV_NODE_FAILED );
	}
	
	private boolean checkOneServerNodeStatus( MetaEnvServer server , MetaEnvServerNode node , NodeStatus mainState ) throws Exception {
		if( server.isManual() ) {
			debug( "skip check process for manual server=" + server.NAME );
			if( mainState != null )
				mainState.setSkipManual();
			return( true );
		}
		
		ServerProcess process = new ServerProcess( server , node );
		try {
			process.gatherStatus( this );
		}
		catch( Throwable e ) {
			mainState.setUnknown( e.getMessage() );
			handle( e );
			return( false );
		}
		
		if( mainState != null )
			mainState.setProcessMode( process.mode );
		
		if( process.isStarted( this ) )
			return( true );
		
		if( process.mode == VarPROCESSMODE.ERRORS )
			info( node.HOSTLOGIN + ": status=errors (" + process.cmdValue + ")" ); 
		else
		if( process.mode == VarPROCESSMODE.STARTING )
			info( node.HOSTLOGIN + ": status=starting" );
		else
		if( process.mode == VarPROCESSMODE.STOPPED )
			info( node.HOSTLOGIN + ": status=stopped" );
		else
			this.exitUnexpectedState();
		return( false );
	}
	
	private boolean checkOneServerNodeComps( MetaEnvServer server , MetaEnvServerNode node , NodeStatus state ) throws Exception {
		String ACCESSPOINT = node.getAccessPoint( this );
		return( checkOneServerWebServices( server , ACCESSPOINT , state , null ) );
	}
	
}
