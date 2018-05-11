package org.urm.engine.action;

import org.urm.action.ActionBase;
import org.urm.action.ActionEnvScopeMaker;
import org.urm.action.ActionScope;
import org.urm.common.action.CommandMethodMeta;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.ReleaseLabelInfo;
import org.urm.engine.status.ScopeState;
import org.urm.meta.product.Meta;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseRepository;

abstract public class CommandMethod {

	public CommandMethodMeta method;
	
	public abstract void run( ScopeState parentState , ActionBase action ) throws Exception;

	public void setMethod( CommandMethodMeta method ) {
		this.method = method;
	}

	public void wrongArgs( ActionBase action ) throws Exception {
		action.exit0( _Error.WrongArgs0 , "wrong args" );
	}

	public Release getRelease( ActionBase action , String RELEASELABEL ) throws Exception {
		Meta meta = action.getContextMeta();
		ReleaseRepository repo = meta.getReleaseRepository();
		ReleaseLabelInfo info = ReleaseLabelInfo.getLabelInfo( action , meta , RELEASELABEL );
		Release release = repo.findRelease( info.RELEASEVER );
		if( release == null )
			action.exit0( _Error.UnknownRelease1 , "unable to find release version=" + info.RELEASEVER );
		return( release );
	}
	
	public Dist getDist( ActionBase action , String RELEASELABEL ) throws Exception {
		Meta meta = action.getContextMeta();
		DistRepository distrepo = meta.getDistRepository();
		return( distrepo.getDistByLabel( action , RELEASELABEL ) );
	}
	
	protected ActionScope getServerScope( ActionBase action , int posFrom ) throws Exception {
		Release release = null;
		if( !action.context.CTX_RELEASELABEL.isEmpty() )
			release = getRelease( action , action.context.CTX_RELEASELABEL );
		
		return( getServerScope( action , posFrom , release ) );
	}

	protected ActionScope getServerScope( ActionBase action , int posFrom , Release release ) throws Exception {
		ActionEnvScopeMaker maker = new ActionEnvScopeMaker( action , action.context.env );
		
		String SERVER = getArg( action , posFrom );
		if( action.context.sg == null ) {
			if( !SERVER.equals( "all" ) )
				action.exit0( _Error.MissingSegmentName0, "Segment option is required to use specific server" );
			maker.addScopeEnv( null , release );
		}
		else {
			String s = getArg( action , posFrom + 1 );
			if( s.matches( "[0-9]+" ) ) {
				String[] NODES = getArgList( action , posFrom + 1 );
				maker.addScopeEnvServerNodes( action.context.sg , SERVER , NODES , release );
			}
			else {
				String[] SERVERS = getArgList( action , posFrom );
				maker.addScopeEnvServers( action.context.sg , SERVERS , release );
			}
		}

		return( maker.getScope() );
	}
	
	protected ActionScope getServerScope( ActionBase action ) throws Exception {
		return( getServerScope( action , 0 ) );
	}
	
	public String getArg( ActionBase action , int pos ) throws Exception {
		return( action.context.options.getArg( pos ) );
	}

	public String[] getArgList( ActionBase action , int pos ) throws Exception {
		return( action.context.options.getArgList( pos ) );
	}
	
}
