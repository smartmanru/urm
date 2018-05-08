package org.urm.action.codebase;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.FACTVALUE;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.engine.vcs.ProjectVersionControl;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaSourceProject;

public class ActionCodebaseList extends ActionBase {

	public enum Facts {
		BRANCH ,
		TAG
	};
	
	public static String CMD_BRANCHES = "branches";
	public static String CMD_TAGS = "tags";
	
	Meta meta;
	String cmd;
	
	public ActionCodebaseList( ActionBase action , String stream , Meta meta , String cmd ) {
		super( action , stream , "execute command=" + cmd );
		this.meta = meta;
		this.cmd = cmd;
	}

	@Override 
	protected SCOPESTATE executeScopeTarget( ScopeState state , ActionScopeTarget scopeProject ) throws Exception {
		ProjectVersionControl vcs = new ProjectVersionControl( this );

		if( !scopeProject.sourceProject.isVCS( this ) ) {
			super.info( "skip non-vcs project=" + scopeProject.sourceProject.NAME );
			return( SCOPESTATE.RunSuccess );
		}
		
		if( cmd.equals( CMD_BRANCHES ) )
			showBranches( state , vcs , scopeProject.sourceProject );
		else
		if( cmd.equals( CMD_TAGS ) )
			showTags( state , vcs , scopeProject.sourceProject );
		return( SCOPESTATE.RunSuccess );
	}

	private void showBranches( ScopeState state , ProjectVersionControl vcs , MetaSourceProject project ) throws Exception {
		String[] branches = vcs.listBranches( project );
		
		super.info( "list branches of project=" + project.NAME + ":" );
		for( String value : branches ) {
			super.info( "\t" + value );
			state.addFact( Facts.BRANCH , FACTVALUE.BRANCHNAME , value ); 
		}
	}
	
	private void showTags( ScopeState state , ProjectVersionControl vcs , MetaSourceProject project ) throws Exception {
		String[] tags = vcs.listTags( project );
		
		super.info( "list tags of project=" + project.NAME + ":" );
		for( String value : tags ) {
			super.info( "\t" + value );
			state.addFact( Facts.TAG , FACTVALUE.TAGNAME , value ); 
		}
	}
	
}
