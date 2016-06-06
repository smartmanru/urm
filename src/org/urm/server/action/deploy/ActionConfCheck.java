package org.urm.server.action.deploy;

import org.urm.common.Common;
import org.urm.meta.MetaEnv;
import org.urm.meta.MetaEnvDC;
import org.urm.meta.MetaEnvServer;
import org.urm.server.action.ActionBase;
import org.urm.server.action.ActionScope;
import org.urm.server.action.ActionScopeSet;
import org.urm.server.action.ActionScopeTarget;

public class ActionConfCheck extends ActionBase {

	boolean S_CONFCHECK_STATUS;
	
	MetaEnv baselineEnv;
	MetaEnvDC baselineDC;
	MetaEnvServer baselineServer;
	
	public ActionConfCheck( ActionBase action , String stream ) {
		super( action , stream );
	}
	
	@Override protected boolean executeScope( ActionScope scope ) throws Exception {
		info( "check configuration parameters in env=" + context.env.ID + " ..." );
		S_CONFCHECK_STATUS = true;

		// read properties
		executeEnv();
		return( false );
	}
	
	@Override protected boolean executeScopeSet( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		info( "check configuration parameters in datacenter=" + set.dc.NAME + " ..." );

		// read properties
		executeDC( set.dc );
		return( false );
	}
	
	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		// read properties
		executeServer( target.envServer );
		return( true );
	}

	private void executeEnv() throws Exception {
		// read env properties...
		String[] S_CONFCHECK_PROPLIST_ENV = context.env.getPropertyList( this );

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
				baselineEnv = meta.loadEnvData( this , S_CONFCHECK_BASELINE_ENV , true );
				checkConfEnv( context.env , baselineEnv , S_CONFCHECK_PROPLIST_ENV );
			}
			else
				trace( "ignore check env - no baseline defined" );
		}
	}

	private void executeDC( MetaEnvDC dc ) throws Exception {
		// echo read data center=$DC properties...
		String[] S_CONFCHECK_PROPLIST_DC = dc.getPropertyList( this );

		if( !isExecute() ) {
			// show values
			info( "============================================ data center=" + dc.NAME + " properties ..." );
			for( String var : S_CONFCHECK_PROPLIST_DC ) {
				String value = dc.getPropertyValue( this , var );
				info( var + "=" + value );
			}
		}
		else {
			if( context.env.hasBaseline( this ) && dc.hasBaseline( this ) ) {
				String S_CONFCHECK_BASELINE_DC = dc.getBaselineDC( this );
				info( "============================================ check dc=" + dc.NAME + " properties baseline=" + S_CONFCHECK_BASELINE_DC + " ..." );
				baselineDC = baselineEnv.getDC( this , S_CONFCHECK_BASELINE_DC );
				checkConfDC( dc , baselineDC , S_CONFCHECK_PROPLIST_DC );
			}
			else
				trace( "ignore check dc=" + dc.NAME + " - no baseline defined" );
		}
	}

	private void executeServer( MetaEnvServer server ) throws Exception {
		// echo read server properties...
		String[] S_CONFCHECK_PROPLIST_SERVER = server.getPropertyList( this );

		if( !isExecute() ) {
			// show values
			info( "============================================ data center=" + server.dc.NAME + " server=" + server.NAME + " properties ..." );
			for( String var : S_CONFCHECK_PROPLIST_SERVER ) {
				String value = server.getPropertyValue( this , var );
				info( var + "=" + value );
			}
		}
		else {
			if( context.env.hasBaseline( this ) &&
			   server.dc.hasBaseline( this ) &&
			   server.hasBaseline( this ) ) {
				String S_CONFCHECK_BASELINE_SERVER = server.getBaselineServer( this );
				info( "============================================ check dc=" + server.dc.NAME + " server=" + server.NAME + " properties baseline=" + S_CONFCHECK_BASELINE_SERVER + " ..." );
				baselineServer = baselineDC.getServer( this , S_CONFCHECK_BASELINE_SERVER );
				checkConfServer( server , baselineServer , S_CONFCHECK_PROPLIST_SERVER );
			}
			else
				trace( "ignore check dc=" + server.dc.NAME + " server=" + server.NAME + " - no baseline defined" );
		}
	}

	private void checkConfServer( MetaEnvServer server , MetaEnvServer baseline , String[] propList ) throws Exception {
		String[] F_CONFCHECK_PROPLIST = baseline.getPropertyList( this ); 
		checkLists( "dc=" + server.dc.NAME + " server=" + server.NAME , propList , F_CONFCHECK_PROPLIST );
	}

	private void checkConfDC( MetaEnvDC dc , MetaEnvDC baseline , String[] propList ) throws Exception {
		String[] F_CONFCHECK_PROPLIST = baseline.getPropertyList( this ); 
		checkLists( "dc=" + dc.NAME , propList , F_CONFCHECK_PROPLIST );
	}

	private void checkConfEnv( MetaEnv env , MetaEnv baseline , String[] propList ) throws Exception {
		String[] F_CONFCHECK_PROPLIST = baseline.getPropertyList( this ); 
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
					ifexit( error );
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
					ifexit( error );
			}
			else {
				if( context.CTX_SHOWALL )
					info( "variable=" + var + " in " + scope + " - ok" );
			}
		}
	}
	
}
