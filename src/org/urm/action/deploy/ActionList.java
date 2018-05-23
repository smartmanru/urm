package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.action.deploy.ActionConfCheck.Facts;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.FACTVALUE;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.engine.HostAccount;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerNode;

public class ActionList extends ActionBase {

	String CMD;
	
	public ActionList( ActionBase action , String stream , String CMD ) {
		super( action , stream , "List environment structure" );
		this.CMD = CMD;
	}
	
	@Override 
	protected SCOPESTATE executeScope( ScopeState state , ActionScope scope ) throws Exception {
		info( "check configuration parameters in env=" + scope.env.NAME + " ..." );
		
		if ( CMD.equals( "params" ) ) 
			executeEnv( state , scope );
		
		return( SCOPESTATE.NotRun );
	}
	
	@Override 
	protected SCOPESTATE executeScopeSet( ScopeState state , ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		
		if ( CMD.equals( "params" ) ) 
			executeSG( state , set.sg );
		else
		if ( CMD.equals( "servers" ) ){
			String s = "servers of segment=" + set.sg.NAME + ":";
			info( s );
		}
		
		return( SCOPESTATE.NotRun );
	}
	
	@Override 
	protected SCOPESTATE executeScopeTarget( ScopeState state , ActionScopeTarget target ) throws Exception {
		
		if ( CMD.equals( "params" ) ) 
			executeServer( state , target.envServer );
		else
		if ( CMD.equals( "servers" ) ){
			showServerInfo( target.envServer );
		}
		
		return( SCOPESTATE.NotRun );
	}
	
	@Override 
	protected SCOPESTATE executeScopeTargetItem( ScopeState state , ActionScopeTarget target , ActionScopeTargetItem item ) throws Exception { 
		
		if ( CMD.equals( "params" ) ) 
			executeNode( state , item.envServerNode );
		else
		if ( CMD.equals( "servers" ) ){
			showNodeInfo( item.envServerNode );
		}
		
		return( SCOPESTATE.RunSuccess ); 
	}
	

	private void showServerInfo( MetaEnvServer server ) throws Exception {
		String s = "\tserver: " + server.NAME + " type=" + server.getServerTypeName();
		if( server.OFFLINE )
			s += " (offline)";
		info( s );
	}
	
	private void showNodeInfo( MetaEnvServerNode node ) throws Exception {
		HostAccount hostAccount = node.getHostAccount(); 
		String s = "\t\tnode " + node.POS + ": " + hostAccount.getFinalAccount();
		if( node.OFFLINE )
			s += " (offline)";
		info( s );
	}
	
	private void executeEnv( ScopeState state , ActionScope scope ) throws Exception {
		// read env properties...
		ObjectProperties ops = scope.env.getProperties();
		String[] S_CONFCHECK_PROPLIST_ENV = ops.getPropertyList();

		info( "============================================ show env properties ..." );
		for( String var : S_CONFCHECK_PROPLIST_ENV ) {
			String value = ops.getPropertyValue( var );
			info( var + "=" + value );
			state.addFact( Facts.ShowVariable , FACTVALUE.VARIABLENAME , var , FACTVALUE.VARIABLEVALUE , value );
		}
	}
	
	private void executeSG( ScopeState state , MetaEnvSegment sg ) throws Exception {
		// echo read data center=$SG properties...
		ObjectProperties ops = sg.getProperties();
		String[] S_CONFCHECK_PROPLIST_SG = ops.getPropertyList();
		
		info( "============================================ show segment properties ..." );
		for( String var : S_CONFCHECK_PROPLIST_SG ) {
			String value = ops.getPropertyValue( var );
			info( var + "=" + value );
			state.addFact( Facts.ShowVariable , FACTVALUE.VARIABLENAME , var , FACTVALUE.VARIABLEVALUE , value );
		}
	}
	
	private void executeServer( ScopeState state , MetaEnvServer server ) throws Exception {
		// echo read server properties...
		ObjectProperties ops = server.getProperties();
		String[] S_CONFCHECK_PROPLIST_SERVER = ops.getPropertyList();
		
		info( "============================================ show server properties ..." );
		for( String var : S_CONFCHECK_PROPLIST_SERVER ) {
			String value = ops.getPropertyValue( var );
			info( var + "=" + value );
			state.addFact( Facts.ShowVariable , FACTVALUE.VARIABLENAME , var , FACTVALUE.VARIABLEVALUE , value );
		}
	}
	
	private void executeNode( ScopeState state , MetaEnvServerNode node ) throws Exception {
		// echo read server properties...
		ObjectProperties ops = node.getProperties();
		String[] S_CONFCHECK_PROPLIST_NODE = ops.getPropertyList();
		
		info( "============================================ show node properties ..." );
		for( String var : S_CONFCHECK_PROPLIST_NODE ) {
			String value = ops.getPropertyValue( var );
			info( var + "=" + value );
			state.addFact( Facts.ShowVariable , FACTVALUE.VARIABLENAME , var , FACTVALUE.VARIABLEVALUE , value );
		}
	}
	
}
