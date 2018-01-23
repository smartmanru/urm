package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.action.database.DatabaseClient;
import org.urm.common.Common;
import org.urm.common.SimpleHttp;
import org.urm.engine.status.EngineStatus;
import org.urm.engine.status.NodeStatus;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.SegmentStatus;
import org.urm.engine.status.ServerStatus;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.product.MetaDistrComponentItem;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerDeployment;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.Types.*;

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
	
	SegmentStatus sgStatus;
	int sgCaptureIndex;
	
	public ActionCheckEnv( ActionBase action , String stream ) {
		super( action , stream , "Check environment status" );
	}

	@Override protected void runBefore( ScopeState state , ActionScope scope ) throws Exception {
		// check all processes
		infoAction( "check environment=" + context.env.NAME + " ..." );
		EngineStatus status = super.getServerStatus();
		status.updateRunTime( this , scope.env );
	}

	@Override protected void runAfter( ScopeState state , ActionScope scope ) throws Exception {
		String value = "SUCCESSFUL";
		if( !S_CHECKENV_TOTAL_SERVERS_FAILED.isEmpty() )
			super.fail0( _Error.CheckenvFailed0 , "Checkenv failed" );
	
		if( super.isFailed() ) {
			value = "FAILED";
			errorAction( "total status is " + value );
		}
		else {
			infoAction( "total status is " + value );
		}
		EngineStatus status = super.getServerStatus();
		status.finishUpdate( this , scope.env );
	}
	
	@Override protected void runBefore( ScopeState state , ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		sgStatus = new SegmentStatus( set.sg );
		sgCaptureIndex = super.logStartCapture();
		info( "execute segment=" + set.sg.NAME + " ..." );
		EngineStatus status = super.getServerStatus();
		status.updateRunTime( this , set.sg );
	}

	@Override protected void runAfter( ScopeState state , ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		String F_STATUSOBJECT = set.sg.NAME;
		if( !S_CHECKENV_TOTAL_SERVERS_FAILED.isEmpty() )
			error( "## sg " + F_STATUSOBJECT + " check FAILED: issues on servers - {" + S_CHECKENV_TOTAL_SERVERS_FAILED + "}" );
		else
			info( "## sg " + F_STATUSOBJECT + " check OK" );
		
		sgStatus.setLog( super.logFinishCapture( sgCaptureIndex ) );
		EngineStatus status = super.getServerStatus();
		status.setSegmentStatus( this , set.sg , sgStatus );
		status.finishUpdate( this , set.sg );
	}
	
	@Override protected SCOPESTATE executeScopeTarget( ScopeState state , ActionScopeTarget target ) throws Exception {
		ActionScopeSet set = target.set;
		EngineStatus status = super.getServerStatus();
		status.updateRunTime( this , target.envServer );
		
		ServerStatus serverStatus = new ServerStatus( target.envServer );
		int captureIndex = super.logStartCapture();
		try {
			S_CHECKENV_TARGET_FAILED = false;
			S_CHECKENV_TARGET_SERVERS_FAILED = "";
			S_CHECKENV_TARGET_NODES_FAILED = "";
			S_CHECKENV_TARGET_COMPS_FAILED = "";
			
			// execute server
			info( "============================================ check server=" + target.envServer.NAME + " ..." );
	
			checkOneServer( target , state , target.envServer , true , "main" , serverStatus );
			
			// check associated if specific servers and not specific nodes 
			if( set.setFull == false && target.itemFull == true ) {
				if( target.envServer.nlbServer != null ) 
					checkOneServer( target , state , target.envServer.nlbServer , false , "nlb" , serverStatus );
				if( target.envServer.staticServer != null ) 
					checkOneServer( target , state , target.envServer.staticServer , false , "static" , serverStatus );
				if( target.envServer.subordinateServers != null ) {
					for( MetaEnvServer server : target.envServer.subordinateServers ) 
						checkOneServer( target , state , server , false , "subordinate" , serverStatus );
				}
			}
		}
		catch( Throwable e ) {
			S_CHECKENV_TARGET_FAILED = true;
			handle( e );
		}
		
		// check status
		String F_STATUSOBJECT = set.sg.NAME + "." + target.envServer.NAME;
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
			error( MSG );
			
			S_CHECKENV_TOTAL_SERVERS_FAILED = Common.addItemToUniqueSpacedList( S_CHECKENV_TOTAL_SERVERS_FAILED , target.NAME );
		}
		
		String[] log = super.logFinishCapture( captureIndex );
		serverStatus.setLog( log );
		status.setServerStatus( this , target.envServer , serverStatus );
		status.finishUpdate( this , target.envServer );
		
		return( SCOPESTATE.RunSuccess );
	}

	private void checkOneServer( ActionScopeTarget target , ScopeState state , MetaEnvServer server , boolean main , String role , ServerStatus serverStatus ) throws Exception {
		S_CHECKENV_SERVER_FAILED = false;
		S_CHECKENV_SERVER_NODES_FAILED = "";
		S_CHECKENV_SERVER_COMPS_FAILED = "";

		// ignore offline server
		if( server.OFFLINE ) {
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
			EngineStatus status = super.getServerStatus();
			
			boolean someNodeAvailable = false;
			for( ActionScopeTargetItem node : target.getItems( this ) ) {
				ScopeState nodeState = new ScopeState( state , node );
				status.updateRunTime( this , node.envServerNode );
				checkOneServerNode( node , server , node.envServerNode , nodeState , main , role , serverStatus );
				status.finishUpdate( this , node.envServerNode );
				if( !S_CHECKENV_NODE_STOPPED )
					someNodeAvailable = true;
			}
			
			S_CHECKENV_TARGET_NODES_FAILED = S_CHECKENV_SERVER_NODES_FAILED;
			
			if( target.itemFull && someNodeAvailable )
				checkOneServerWhole( server , state , serverStatus );
			
			S_CHECKENV_TARGET_COMPS_FAILED = S_CHECKENV_SERVER_COMPS_FAILED;
		}
		else {
			for( MetaEnvServerNode node : server.getNodes() ) {
				ScopeState nodeState = new ScopeState( state , node );
				checkOneServerNode( null , server , node , nodeState , false , role , serverStatus );
			}
		}
		
		// add server status to target
		if( !S_CHECKENV_SERVER_FAILED )
			return;
		
		S_CHECKENV_TARGET_FAILED = true;
		if( !main )
			S_CHECKENV_TARGET_SERVERS_FAILED = Common.addItemToUniqueSpacedList( S_CHECKENV_TARGET_SERVERS_FAILED , server.NAME );
	}

	private void checkOneServerWhole( MetaEnvServer server , ScopeState state , ServerStatus serverStatus ) throws Exception {
		debug( "check whole server ..." );
		
		boolean wholeOk = true;
		
		if( server.isWebUser() ) {
			if( !server.WEBMAINURL.isEmpty() )
				if( !checkOneServerWholeUrl( server.WEBMAINURL , "main web url" , state , null , serverStatus ) )
					wholeOk = false;
		}

		if( server.isCallable() ) {
			if( !checkOneServerWholeComps( server , state , serverStatus ) )
				wholeOk = false;
		}

		if( server.isDatabase() ) {
			if( !checkOneServerWholeDatabase( server , state , serverStatus ) )
				wholeOk = false;
		}
		
		if( wholeOk )
			return;
		
		S_CHECKENV_SERVER_FAILED = true;
	}
	
	private boolean checkOneServerWholeUrl( String URL , String role , ScopeState state , NodeStatus nodeStatus , ServerStatus serverStatus ) throws Exception {
		super.trace( role + ": check url=" + URL + " ..." );
		
		boolean res = SimpleHttp.check( this , URL );
		String ok = ( res )? "OK" : "FAILED";
		String msg = "check " + role + " " + URL + ": " + ok; 
		if( res )
			debug( msg );
		else
			error( msg );
		if( nodeStatus == null )
			serverStatus.addWholeUrlStatus( URL , role , res );
		else
			nodeStatus.addWholeUrlStatus( URL , role , res );
		return( res );
	}

	private boolean checkOneServerWholeComps( MetaEnvServer server , ScopeState state , ServerStatus serverStatus ) throws Exception {
		if( server.WEBSERVICEURL.isEmpty() )
			return( true );
		
		return( checkOneServerWebServices( server , state , server.WEBSERVICEURL , null , serverStatus ) );
	}
	
	private boolean checkOneServerWebServices( MetaEnvServer server , ScopeState state , String ACCESSPOINT , NodeStatus nodeStatus , ServerStatus serverStatus ) throws Exception {
		if( !server.hasWebServices() )
			return( true );
		
		// by comps
		boolean ok = true;
		for( MetaEnvServerDeployment deployment : server.getDeployments() ) {
			if( deployment.comp == null )
				continue;
			
			for( MetaDistrComponentItem ws : deployment.comp.getWebServices() ) {
				String URL = ws.getURL( ACCESSPOINT ); 
				if( !checkOneServerWholeUrl( URL , "web service" , state , nodeStatus , serverStatus ) ) {
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
	
	private boolean checkOneServerWholeDatabase( MetaEnvServer server , ScopeState state , ServerStatus serverStatus ) throws Exception {
		DatabaseClient process = new DatabaseClient();
		
		boolean res = true;
		if( !process.checkConnect( this , server ) ) {
			error( "database server=" + server.NAME + ": client is not available" );
			res = false;
		}
		
		serverStatus.addDatabaseStatus( res );
		return( res );
	}
	
	private void checkOneServerNode( ActionScopeTargetItem item , MetaEnvServer server , MetaEnvServerNode node , ScopeState state , boolean main , String role , ServerStatus serverStatus ) {
		S_CHECKENV_NODE_FAILED = false;
		S_CHECKENV_NODE_STOPPED = false;

		NodeStatus nodeStatus = null;
		int captureIndex = 0;
		if( main ) {
			nodeStatus = new NodeStatus( node );
			captureIndex = super.logStartCapture();
		}
		
		info( "node " + node.POS + "=" + node.HOSTLOGIN );

		try {
			if( checkOneServerNodeStatus( server , node , state , nodeStatus ) ) {
				if( !checkOneServerNodeComps( server , node , state , nodeStatus ) ) {
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
				if( !checkOneServerNodeStatus( server.proxyServer , node.getProxyNode( this ) , state , null ) ) {
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
			error( "## node " + node.POS + " check FAILED" );
		else
			info( "## node " + node.POS + " check OK" );
		
		if( main ) {
			String[] log = super.logFinishCapture( captureIndex );
			nodeStatus.setLog( log );
			EngineStatus status = super.getServerStatus();
			status.setServerNodeStatus( this , node , nodeStatus );
			serverStatus.addNodeStatus( nodeStatus ); 
		}
		else
			serverStatus.addRoleStatus( role , node , S_CHECKENV_NODE_FAILED );
	}
	
	private boolean checkOneServerNodeStatus( MetaEnvServer server , MetaEnvServerNode node , ScopeState state , NodeStatus mainState ) throws Exception {
		if( server.isManual() ) {
			debug( "skip check process for manual server=" + server.NAME );
			if( mainState != null )
				mainState.setSkipManual();
			return( true );
		}
		
		ServerProcess process = new ServerProcess( server , node , state );
		try {
			process.gatherStatus( this );
		}
		catch( Throwable e ) {
			mainState.setUnknown( e.toString() );
			handle( e );
			return( false );
		}
		
		if( mainState != null )
			mainState.setProcessMode( process.mode );
		
		if( process.isStarted( this ) )
			return( true );
		
		if( process.mode == VarPROCESSMODE.ERRORS )
			error( node.HOSTLOGIN + ": status=errors (" + process.cmdValue + ")" ); 
		else
		if( process.mode == VarPROCESSMODE.STARTING )
			error( node.HOSTLOGIN + ": status=starting" );
		else
		if( process.mode == VarPROCESSMODE.STOPPED )
			error( node.HOSTLOGIN + ": status=stopped" );
		else
		if( process.mode == VarPROCESSMODE.UNREACHABLE )
			error( node.HOSTLOGIN + ": status=unreachable" );
		else
			this.exitUnexpectedState();
		return( false );
	}
	
	private boolean checkOneServerNodeComps( MetaEnvServer server , MetaEnvServerNode node , ScopeState state , NodeStatus status ) throws Exception {
		String ACCESSPOINT = node.getAccessPoint( this );
		return( checkOneServerWebServices( server , state , ACCESSPOINT , status , null ) );
	}
	
}
