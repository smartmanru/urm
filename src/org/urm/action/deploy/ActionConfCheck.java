package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ScopeState;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.common.Common;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaEnvServer;

public class ActionConfCheck extends ActionBase {

	boolean S_CONFCHECK_STATUS;
	
	MetaEnv baselineEnv;
	MetaEnvSegment baselineSG;
	MetaEnvServer baselineServer;
	
	public ActionConfCheck( ActionBase action , String stream ) {
		super( action , stream , "Check configuration parameters" );
	}
	
	@Override protected SCOPESTATE executeScope( ScopeState state , ActionScope scope ) throws Exception {
		info( "check configuration parameters in env=" + context.env.ID + " ..." );
		S_CONFCHECK_STATUS = true;

		// read properties
		executeEnv( scope );
		return( SCOPESTATE.NotRun );
	}
	
	@Override protected SCOPESTATE executeScopeSet( ScopeState state , ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		info( "check configuration parameters in segment=" + set.sg.NAME + " ..." );

		// read properties
		executeSG( set.sg );
		return( SCOPESTATE.NotRun );
	}
	
	@Override protected SCOPESTATE executeScopeTarget( ScopeState state , ActionScopeTarget target ) throws Exception {
		// read properties
		executeServer( target.envServer );
		return( SCOPESTATE.RunSuccess );
	}

	private void executeEnv( ActionScope scope ) throws Exception {
		// read env properties...
		String[] S_CONFCHECK_PROPLIST_ENV = context.env.getPropertyList();

		if( !isExecute() ) {
			// show values
			info( "============================================ show env properties ..." );
			for( String var : S_CONFCHECK_PROPLIST_ENV ) {
				String value = context.env.getPropertyValue( this , var );
				info( var + "=" + value );
			}
		}
		else {
			if( context.env.hasBaseline( this ) ) {
				String S_CONFCHECK_BASELINE_ENV = context.env.getBaselineFile( this );
				info( "============================================ check env properties baseline=" + S_CONFCHECK_BASELINE_ENV + " ..." );
				baselineEnv = scope.meta.getEnvData( this , S_CONFCHECK_BASELINE_ENV , true );
				checkConfEnv( context.env , baselineEnv , S_CONFCHECK_PROPLIST_ENV );
			}
			else
				trace( "ignore check env - no baseline defined" );
		}
	}

	private void executeSG( MetaEnvSegment sg ) throws Exception {
		// echo read data center=$SG properties...
		String[] S_CONFCHECK_PROPLIST_SG = sg.getPropertyList();

		if( !isExecute() ) {
			// show values
			info( "============================================ data center=" + sg.NAME + " properties ..." );
			for( String var : S_CONFCHECK_PROPLIST_SG ) {
				String value = sg.getPropertyValue( this , var );
				info( var + "=" + value );
			}
		}
		else {
			if( context.env.hasBaseline( this ) && sg.hasBaseline( this ) ) {
				String S_CONFCHECK_BASELINE_SG = sg.getBaselineSG( this );
				info( "============================================ check sg=" + sg.NAME + " properties baseline=" + S_CONFCHECK_BASELINE_SG + " ..." );
				baselineSG = baselineEnv.getSG( this , S_CONFCHECK_BASELINE_SG );
				checkConfSG( sg , baselineSG , S_CONFCHECK_PROPLIST_SG );
			}
			else
				trace( "ignore check sg=" + sg.NAME + " - no baseline defined" );
		}
	}

	private void executeServer( MetaEnvServer server ) throws Exception {
		// echo read server properties...
		String[] S_CONFCHECK_PROPLIST_SERVER = server.getPropertyList();

		if( !isExecute() ) {
			// show values
			info( "============================================ data center=" + server.sg.NAME + " server=" + server.NAME + " properties ..." );
			for( String var : S_CONFCHECK_PROPLIST_SERVER ) {
				String value = server.getPropertyValue( this , var );
				info( var + "=" + value );
			}
		}
		else {
			if( context.env.hasBaseline( this ) &&
			   server.sg.hasBaseline( this ) &&
			   server.hasBaseline( this ) ) {
				String S_CONFCHECK_BASELINE_SERVER = server.getBaselineServer( this );
				info( "============================================ check sg=" + server.sg.NAME + " server=" + server.NAME + " properties baseline=" + S_CONFCHECK_BASELINE_SERVER + " ..." );
				baselineServer = baselineSG.getServer( this , S_CONFCHECK_BASELINE_SERVER );
				checkConfServer( server , baselineServer , S_CONFCHECK_PROPLIST_SERVER );
			}
			else
				trace( "ignore check sg=" + server.sg.NAME + " server=" + server.NAME + " - no baseline defined" );
		}
	}

	private void checkConfServer( MetaEnvServer server , MetaEnvServer baseline , String[] propList ) throws Exception {
		String[] F_CONFCHECK_PROPLIST = baseline.getPropertyList(); 
		checkLists( "sg=" + server.sg.NAME + " server=" + server.NAME , propList , F_CONFCHECK_PROPLIST );
	}

	private void checkConfSG( MetaEnvSegment sg , MetaEnvSegment baseline , String[] propList ) throws Exception {
		String[] F_CONFCHECK_PROPLIST = baseline.getPropertyList(); 
		checkLists( "sg=" + sg.NAME , propList , F_CONFCHECK_PROPLIST );
	}

	private void checkConfEnv( MetaEnv env , MetaEnv baseline , String[] propList ) throws Exception {
		String[] F_CONFCHECK_PROPLIST = baseline.getPropertyList(); 
		checkLists( "environment" , propList , F_CONFCHECK_PROPLIST );
	}

	private void checkLists( String scope , String[] vars , String[] baseline ) throws Exception {
		// check env in base
		for( String var : vars ) {
			if( var.endsWith( "configuration-baseline" ) )
				continue;
		
			if( Common.findItem( var , baseline ) < 0 ) {
				String error = "unexpected variable=" + var + " in " + scope; 
			
				if( context.CTX_SHOWALL ) {
					error( error );
					S_CONFCHECK_STATUS = false;
				}
				else
					ifexit( _Error.UnexpectedScopeVariable2 , error , new String[] { scope , var } );
			}
			else {
				if( context.CTX_SHOWALL )
					info( "variable=" + var + " in " + scope + " - ok" );
			}
		}

		// check base in env
		for( String var : baseline ) {
			if( var.equals( "configuration-baseline" ) )
				continue;
		
			if( Common.findItem( var , vars ) < 0 ) {
				String error = "missing variable=" + var + " in " + scope;
			
				if( context.CTX_SHOWALL ) {
					error( error );
					S_CONFCHECK_STATUS = false;
				}
				else
					ifexit( _Error.MissingScopeVariable2 , error , new String[] { scope , var } );
			}
			else {
				if( context.CTX_SHOWALL )
					info( "variable=" + var + " in " + scope + " - ok" );
			}
		}
	}
	
}
