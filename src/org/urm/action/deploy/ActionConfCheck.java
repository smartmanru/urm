package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.common.Common;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.FACTVALUE;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;

public class ActionConfCheck extends ActionBase {

	public enum Facts {
		UnexpectedVariable ,
		MatchedVariable ,
		MissingVariable ,
		ShowVariable
	};
	
	boolean S_CONFCHECK_STATUS;
	
	MetaEnv baselineEnv;
	MetaEnvSegment baselineSG;
	MetaEnvServer baselineServer;
	
	public ActionConfCheck( ActionBase action , String stream ) {
		super( action , stream , "Check configuration parameters" );
	}
	
	@Override 
	protected SCOPESTATE executeScope( ScopeState state , ActionScope scope ) throws Exception {
		info( "check configuration parameters in env=" + scope.env.NAME + " ..." );
		S_CONFCHECK_STATUS = true;

		// read properties
		executeEnv( state , scope );
		return( SCOPESTATE.NotRun );
	}
	
	@Override 
	protected SCOPESTATE executeScopeSet( ScopeState state , ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		info( "check configuration parameters in segment=" + set.sg.NAME + " ..." );

		// read properties
		executeSG( state , set.sg );
		return( SCOPESTATE.NotRun );
	}
	
	@Override 
	protected 
	SCOPESTATE executeScopeTarget( ScopeState state , ActionScopeTarget target ) throws Exception {
		// read properties
		executeServer( state , target );
		return( SCOPESTATE.RunSuccess );
	}

	private void executeEnv( ScopeState state , ActionScope scope ) throws Exception {
		// read env properties...
		ObjectProperties ops = scope.env.getProperties();
		String[] S_CONFCHECK_PROPLIST_ENV = ops.getPropertyList();

		if( !isExecute() ) {
			// show values
			info( "============================================ show env properties ..." );
			for( String var : S_CONFCHECK_PROPLIST_ENV ) {
				String value = ops.getPropertyValue( var );
				info( var + "=" + value );
			}
		}
		else {
			if( context.env.hasBaseline() ) {
				baselineEnv = scope.env.getBaseline();
				String S_CONFCHECK_BASELINE_ENV = baselineEnv.NAME;
				info( "============================================ check env properties baseline=" + S_CONFCHECK_BASELINE_ENV + " ..." );
				checkConfEnv( state , scope.env , baselineEnv , S_CONFCHECK_PROPLIST_ENV );
			}
			else
				trace( "ignore check env - no baseline defined" );
		}
	}

	private void executeSG( ScopeState state , MetaEnvSegment sg ) throws Exception {
		// echo read data center=$SG properties...
		ObjectProperties ops = sg.getProperties();
		String[] S_CONFCHECK_PROPLIST_SG = ops.getPropertyList();

		if( !isExecute() ) {
			// show values
			info( "============================================ data center=" + sg.NAME + " properties ..." );
			for( String var : S_CONFCHECK_PROPLIST_SG ) {
				String value = ops.getPropertyValue( var );
				info( var + "=" + value );
			}
		}
		else {
			if( context.env.hasBaseline() && sg.hasBaseline() ) {
				baselineSG = sg.getBaselineSegment( baselineEnv );
				String S_CONFCHECK_BASELINE_SG = baselineSG.NAME;
				info( "============================================ check sg=" + sg.NAME + " properties baseline=" + S_CONFCHECK_BASELINE_SG + " ..." );
				checkConfSG( state , sg , baselineSG , S_CONFCHECK_PROPLIST_SG );
			}
			else
				trace( "ignore check sg=" + sg.NAME + " - no baseline defined" );
		}
	}

	private void executeServer( ScopeState state , ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		
		// echo read server properties...
		ObjectProperties ops = server.getProperties();
		String[] S_CONFCHECK_PROPLIST_SERVER = ops.getPropertyList();

		if( !isExecute() ) {
			// show values
			info( "============================================ data center=" + server.sg.NAME + " server=" + server.NAME + " properties ..." );
			for( String var : S_CONFCHECK_PROPLIST_SERVER ) {
				String value = ops.getPropertyValue( var );
				info( var + "=" + value );
			}
		}
		else {
			if( context.env.hasBaseline() && server.sg.hasBaseline() && server.hasBaseline() ) {
				MetaEnvServer baselineServer = server.getBaseline();
				String S_CONFCHECK_BASELINE_SERVER = baselineServer.NAME;
				info( "============================================ check sg=" + server.sg.NAME + " server=" + server.NAME + " properties baseline=" + S_CONFCHECK_BASELINE_SERVER + " ..." );
				baselineServer = baselineSG.getServer( S_CONFCHECK_BASELINE_SERVER );
				checkConfServer( state , server , baselineServer , S_CONFCHECK_PROPLIST_SERVER );
			}
			else
				trace( "ignore check sg=" + server.sg.NAME + " server=" + server.NAME + " - no baseline defined" );
		}
	}

	private void checkConfServer( ScopeState state , MetaEnvServer server , MetaEnvServer baseline , String[] propList ) throws Exception {
		ObjectProperties ops = baseline.getProperties();
		String[] F_CONFCHECK_PROPLIST = ops.getPropertyList(); 
		checkLists( state , "sg=" + server.sg.NAME + " server=" + server.NAME , propList , F_CONFCHECK_PROPLIST );
	}

	private void checkConfSG( ScopeState state , MetaEnvSegment sg , MetaEnvSegment baseline , String[] propList ) throws Exception {
		ObjectProperties ops = baseline.getProperties();
		String[] F_CONFCHECK_PROPLIST = ops.getPropertyList(); 
		checkLists( state , "sg=" + sg.NAME , propList , F_CONFCHECK_PROPLIST );
	}

	private void checkConfEnv( ScopeState state , MetaEnv env , MetaEnv baseline , String[] propList ) throws Exception {
		ObjectProperties ops = baseline.getProperties();
		String[] F_CONFCHECK_PROPLIST = ops.getPropertyList(); 
		checkLists( state , "environment" , propList , F_CONFCHECK_PROPLIST );
	}

	private void checkLists( ScopeState state , String scope , String[] vars , String[] baseline ) throws Exception {
		// check env in base
		for( String var : vars ) {
			if( var.endsWith( "configuration-baseline" ) )
				continue;
		
			if( Common.findItem( var , baseline ) < 0 ) {
				String error = "unexpected variable=" + var + " in " + scope;
				state.addFact( Facts.UnexpectedVariable , FACTVALUE.VARIABLENAME , var );
			
				if( context.CTX_SHOWALL ) {
					error( error );
					S_CONFCHECK_STATUS = false;
				}
				else
					ifexit( _Error.UnexpectedScopeVariable2 , error , new String[] { scope , var } );
			}
			else {
				if( context.CTX_SHOWALL ) {
					state.addFact( Facts.MatchedVariable , FACTVALUE.VARIABLENAME , var );
					info( "variable=" + var + " in " + scope + " - ok" );
				}
			}
		}

		// check base in env
		for( String var : baseline ) {
			if( var.equals( "configuration-baseline" ) )
				continue;
		
			if( Common.findItem( var , vars ) < 0 ) {
				state.addFact( Facts.MissingVariable , FACTVALUE.VARIABLENAME , var );
				String error = "missing variable=" + var + " in " + scope;
			
				if( context.CTX_SHOWALL ) {
					error( error );
					S_CONFCHECK_STATUS = false;
				}
				else
					ifexit( _Error.MissingScopeVariable2 , error , new String[] { scope , var } );
			}
		}
	}
	
}
