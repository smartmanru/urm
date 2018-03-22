package org.urm.action.release;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.action.ActionProductScopeMaker;
import org.urm.action.ActionReleaseScopeMaker;
import org.urm.action.ActionScope;
import org.urm.action.codebase.CodebaseCommand;
import org.urm.common.action.CommandMethodMeta.SecurityAction;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.dist.Dist;
import org.urm.engine.status.ScopeState;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.product.Meta;

public class ReleaseCommand {

	public ReleaseCommand() {
	}

	public void createProdInitial( ScopeState parentState , ActionBase action , Meta meta , String RELEASEVER ) throws Exception {
		ActionCreateMaster ma = new ActionCreateMaster( action , null , meta , RELEASEVER , false );
		ma.runSimpleProduct( parentState , meta.name , SecurityAction.ACTION_RELEASE , false );
	}
	
	public void createProdCopy( ScopeState parentState , ActionBase action , Meta meta , String RELEASEVER ) throws Exception {
		ActionCreateMaster ma = new ActionCreateMaster( action , null , meta , RELEASEVER , true );
		ma.runSimpleProduct( parentState , meta.name , SecurityAction.ACTION_RELEASE , false );
	}
	
	public void deleteProd( ScopeState parentState , ActionBase action , Meta meta ) throws Exception {
		Dist dist = action.getMasterDist( meta );
		ActionDeleteRelease ma = new ActionDeleteRelease( action , null , dist , true );
		ma.runSimpleProduct( parentState , meta.name , SecurityAction.ACTION_RELEASE , false );
	}
	
	public void prodStatus( ScopeState parentState , ActionBase action , Meta meta ) throws Exception {
		Dist dist = action.getMasterDist( meta );
		ActionPrintReleaseStatus ma = new ActionPrintReleaseStatus( action , null , dist );
		ma.runSimpleProduct( parentState , meta.name , SecurityAction.ACTION_RELEASE , true );
	}
	
	public void createRelease( ScopeState parentState , ActionBase action , Meta meta , String RELEASELABEL , Date releaseDate , ReleaseLifecycle lc ) throws Exception {
		ActionCreateRelease ma = new ActionCreateRelease( action , null , meta , RELEASELABEL , releaseDate , lc );
		ma.runSimpleProduct( parentState , meta.name , SecurityAction.ACTION_RELEASE , false );
	}

	public void modifyRelease( ScopeState parentState , ActionBase action , Dist dist , Date releaseDate , ReleaseLifecycle lc ) throws Exception {
		ActionModifyRelease ma = new ActionModifyRelease( action , null , dist , releaseDate , lc );
		ma.runSimpleProduct( parentState , dist.meta.name , SecurityAction.ACTION_RELEASE , false );
	}

	public void deleteRelease( ScopeState parentState , ActionBase action , Meta meta , String RELEASELABEL , boolean force ) throws Exception {
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		if( dist.isMaster() )
			action.exit0( _Error.CannotDropProd0 , "Cannot drop full production release, use prod command" );
		
		ActionDeleteRelease ma = new ActionDeleteRelease( action , null , dist , force );
		ma.runSimpleProduct( parentState , meta.name , SecurityAction.ACTION_RELEASE , false );
	}
	
	public void cleanupRelease( ScopeState parentState , ActionBase action , Meta meta , String RELEASELABEL ) throws Exception {
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		ActionForceCloseRelease ma = new ActionForceCloseRelease( action , null , dist );
		ma.runSimpleProduct( parentState , meta.name , SecurityAction.ACTION_RELEASE , false );
	}
	
	public void copyRelease( ScopeState parentState , ActionBase action , Meta meta , String RELEASESRC , String RELEASEDST , Date releaseDate , ReleaseLifecycle lc ) throws Exception {
		Dist distSrc = action.getReleaseDist( meta , RELEASESRC );
		ActionCopyRelease ma = new ActionCopyRelease( action , null , distSrc , RELEASEDST , releaseDate , lc );
		ma.runSimpleProduct( parentState , meta.name , SecurityAction.ACTION_RELEASE , false );
	}
	
	public void finishRelease( ScopeState parentState , ActionBase action , Meta meta , String RELEASELABEL ) throws Exception {
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		ActionFinishRelease ma = new ActionFinishRelease( action , null , dist );
		ma.runSimpleProduct( parentState , meta.name , SecurityAction.ACTION_RELEASE , false );
	}
	
	public void completeRelease( ScopeState parentState , ActionBase action , Meta meta , String RELEASELABEL ) throws Exception {
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		ActionCompleteRelease ma = new ActionCompleteRelease( action , null , dist );
		ma.runSimpleProduct( parentState , meta.name , SecurityAction.ACTION_RELEASE , false );
	}
	
	public void reopenRelease( ScopeState parentState , ActionBase action , Meta meta , String RELEASELABEL ) throws Exception {
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		ActionReopenRelease ma = new ActionReopenRelease( action , null , dist );
		ma.runSimpleProduct( parentState , meta.name , SecurityAction.ACTION_RELEASE , false );
	}
	
	public void statusRelease( ScopeState parentState , ActionBase action , Meta meta , String RELEASELABEL ) throws Exception {
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		ActionPrintReleaseStatus ma = new ActionPrintReleaseStatus( action , null , dist );
		ma.runSimpleProduct( parentState , meta.name , SecurityAction.ACTION_RELEASE , false );
	}

	public void appendProd( ScopeState parentState , ActionBase action , Dist dist ) throws Exception {
		ActionAppendMaster ma = new ActionAppendMaster( action , null , dist );
		ma.runSimpleProduct( parentState , dist.meta.name , SecurityAction.ACTION_RELEASE , false );
	}
	
	private void addReleaseScope( ScopeState parentState , ActionBase action , Dist dist , ActionScope scope ) throws Exception {
		ActionAddScope ma = new ActionAddScope( action , null , dist );
		
		dist.openForDataChange( action );
		if( !ma.runAll( parentState , scope , null , SecurityAction.ACTION_RELEASE , false ) ) {
			dist.closeDataChange( action );
			dist.finishStatus( action );
			action.exit0( _Error.ReleaseSetChangeErrors0 , "release set is not changed because of errors" );
		}

		dist.saveReleaseXml( action );
		dist.createDeliveryFolders( action );
		dist.closeDataChange( action );
		dist.finishStatus( action );
		
		action.info( "scope (" + scope.getScopeInfo( action ) + ") - added to release" );
	}
	
	private void setScopeSpecifics( ScopeState parentState , ActionBase action , Dist dist , ActionScope scope ) throws Exception {
		ActionSetSpecifics ma = new ActionSetSpecifics( action , null , dist );
		
		dist.openForDataChange( action );
		if( !ma.runAll( null , scope , null , SecurityAction.ACTION_RELEASE , false ) ) {
			dist.closeDataChange( action );
			dist.finishStatus( action );
			action.exit0( _Error.ReleaseSetChangeErrors0 , "release set is not changed because of errors" );
		}

		dist.saveReleaseXml( action );
		dist.closeDataChange( action );
		dist.finishStatus( action );
		
		action.info( "scope (" + scope.getScopeInfo( action ) + ") - release sccope specifics updated" );
	}
	
	private void setReleaseScope( ScopeState parentState , ActionBase action , Dist dist , boolean source , String[] pathItems ) throws Exception {
		ActionSetScope ma = new ActionSetScope( action , null , dist , source , pathItems );
		
		dist.openForDataChange( action );
		if( !ma.runSimpleProduct( null , dist.meta.name , SecurityAction.ACTION_RELEASE , false ) ) {
			dist.closeDataChange( action );
			dist.finishStatus( action );
			action.exit0( _Error.ReleaseSetChangeErrors0 , "release set is not changed because of errors" );
		}

		dist.saveReleaseXml( action );
		dist.createDeliveryFolders( action );
		dist.closeDataChange( action );
		dist.finishStatus( action );
		
		action.info( "release scope has been changed" );
	}
	
	public void addReleaseBuildProjects( ScopeState parentState , ActionBase action , Meta meta , String RELEASELABEL , String SET , String[] elements ) throws Exception {
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		if( dist.release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		ActionProductScopeMaker maker = new ActionProductScopeMaker( action , meta );
		maker.addScopeProductSet( SET , elements );
		ActionScope scope = maker.getScope();
		addReleaseScope( parentState , action , dist , scope );
	}

	public void setScopeSpecifics( ScopeState parentState , ActionBase action , Meta meta , String RELEASELABEL , String SET , String[] elements ) throws Exception {
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		if( dist.release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		ActionReleaseScopeMaker maker = new ActionReleaseScopeMaker( action , dist );
		maker.addScopeReleaseSet( SET , elements );
		ActionScope scope = maker.getScope();
		setScopeSpecifics( parentState , action , dist , scope );
	}

	public void addReleaseConfigItems( ScopeState parentState , ActionBase action , Meta meta , String RELEASELABEL , String[] elements ) throws Exception {
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		if( dist.release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		ActionProductScopeMaker maker = new ActionProductScopeMaker( action , meta );
		maker.addScopeProductCategory( DBEnumScopeCategoryType.CONFIG , elements );
		ActionScope scope = maker.getScope();
		addReleaseScope( parentState , action , dist , scope );
	}

	public void addReleaseDatabaseItems( ScopeState parentState , ActionBase action , Meta meta , String RELEASELABEL , String[] DELIVERIES ) throws Exception {
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		if( dist.release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		ActionProductScopeMaker maker = new ActionProductScopeMaker( action , meta );
		maker.addScopeProductCategory( DBEnumScopeCategoryType.DB , DELIVERIES );
		ActionScope scope = maker.getScope();
		addReleaseScope( parentState , action , dist , scope );
	}

	public void addReleaseBuildItems( ScopeState parentState , ActionBase action , Meta meta , String RELEASELABEL , String[] ITEMS ) throws Exception {
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		if( dist.release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		ActionProductScopeMaker maker = new ActionProductScopeMaker( action , meta );
		maker.addScopeProductDistItems( ITEMS );
		ActionScope scope = maker.getScope();
		addReleaseScope( parentState , action , dist , scope );
	}

	public void setScope( ScopeState parentState , ActionBase action , Meta meta , String RELEASELABEL , boolean source , String[] ITEMS ) throws Exception {
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		if( dist.release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		setReleaseScope( parentState , action , dist , source , ITEMS );
	}

	public void buildRelease( ScopeState parentState , ActionBase action , String SET , String[] PROJECTS , Dist dist ) throws Exception {
		if( dist.release.isCumulative() )
			action.exit0( _Error.CannotBuildCumulative0 , "cannot build cumulative release" );
		
		CodebaseCommand buildImpl = new CodebaseCommand();
		buildImpl.buildRelease( parentState , action , dist.meta , SET , PROJECTS , dist );
	}

	public void getAllRelease( ScopeState parentState , ActionBase action , String SET , String[] PROJECTS , Dist dist ) throws Exception {
		if( dist.release.isCumulative() )
			action.exit0( _Error.CannotDownloadCumulative0 , "cannot download cumulative release" );
		
		CodebaseCommand buildImpl = new CodebaseCommand();
		action.context.CTX_DIST = true;
		buildImpl.getAllRelease( parentState , action , SET , PROJECTS , dist );
	}

	public void getCumulativeRelease( ScopeState parentState , ActionBase action , Dist dist ) throws Exception {
		if( !dist.release.isCumulative() )
			action.exit0( _Error.NotCumulativeRelease0 , "should be cumulative release" );
		
		ActionGetCumulative ca = new ActionGetCumulative( action , null , dist.meta , dist );
		ca.runSimpleProduct( parentState , dist.meta.name , SecurityAction.ACTION_RELEASE , false );
	}

	private void descope( ScopeState parentState , ActionBase action , Dist dist , ActionScope scope ) throws Exception {
		if( dist.release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		ActionDescope ma = new ActionDescope( action , null , dist );
		
		dist.openForDataChange( action );
		if( !ma.runAll( parentState , scope , null , SecurityAction.ACTION_RELEASE , false ) ) {
			dist.closeDataChange( action );
			dist.finishStatus( action );
			action.exit0( _Error.ReleaseSetChangeErrors0 , "release set is not changed because of errors" );
		}

		dist.saveReleaseXml( action );
		dist.closeDataChange( action );
		dist.finishStatus( action );
		
		action.info( "scope (" + scope.getScopeInfo( action ) + ") - removed from release" );
	}
	
	public void descopeAll( ScopeState parentState , ActionBase action , Dist dist ) throws Exception {
		if( dist.release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		dist.openForDataChange( action );
		dist.descopeAll( action );
		dist.saveReleaseXml( action );
		dist.closeDataChange( action );
		dist.finishStatus( action );
		
		action.info( "entire scope has been removed from release" );
	}
	
	public void descopeConfComps( ScopeState parentState , ActionBase action , Dist dist , String[] COMPS ) throws Exception {
		if( dist.release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		ActionReleaseScopeMaker maker = new ActionReleaseScopeMaker( action , dist );
		maker.addScopeReleaseCategory( DBEnumScopeCategoryType.CONFIG , COMPS );
		ActionScope scope = maker.getScope();
		descope( parentState , action , dist , scope );
	}
	
	public void descopeManualItems( ScopeState parentState , ActionBase action , Dist dist , String[] ITEMS ) throws Exception {
		if( dist.release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		ActionReleaseScopeMaker maker = new ActionReleaseScopeMaker( action , dist );
		maker.addScopeReleaseCategory( DBEnumScopeCategoryType.MANUAL , ITEMS );
		ActionScope scope = maker.getScope();
		descope( parentState , action , dist , scope );
	}
	
	public void descopeBinary( ScopeState parentState , ActionBase action , Dist dist , String SET , String PROJECT , String[] ITEMS ) throws Exception {
		if( dist.release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		ActionReleaseScopeMaker maker = new ActionReleaseScopeMaker( action , dist );
		if( PROJECT.equals( "all" ) )
			maker.addScopeReleaseSet( SET , new String [] { "all" } );
		else
			maker.addScopeReleaseProjectItems( PROJECT , ITEMS );
		ActionScope scope = maker.getScope();
		descope( parentState , action , dist , scope );
	}
	
	public void descopeDatabase( ScopeState parentState , ActionBase action , Dist dist , String[] DELIVERIES ) throws Exception {
		if( dist.release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		ActionReleaseScopeMaker maker = new ActionReleaseScopeMaker( action , dist );
		maker.addScopeReleaseCategory( DBEnumScopeCategoryType.DB , DELIVERIES );
		ActionScope scope = maker.getScope();
		descope( parentState , action , dist , scope );
	}

	public void nextPhase( ScopeState parentState , ActionBase action , Dist dist ) throws Exception {
		ActionSchedulePhase ma = new ActionSchedulePhase( action , null , dist );
		ma.runSimpleProduct( parentState , dist.meta.name , SecurityAction.ACTION_RELEASE , false );
	}
	
	public void setPhaseDeadline( ScopeState parentState , ActionBase action , Dist dist , String PHASE , Date deadlineDate ) throws Exception {
		ActionSchedulePhase ma = new ActionSchedulePhase( action , null , dist , PHASE , deadlineDate );
		ma.runSimpleProduct( parentState , dist.meta.name , SecurityAction.ACTION_RELEASE , false );
	}
	
	public void setPhaseDuration( ScopeState parentState , ActionBase action , Dist dist , String PHASE , int duration ) throws Exception {
		ActionSchedulePhase ma = new ActionSchedulePhase( action , null , dist , PHASE , duration );
		ma.runSimpleProduct( parentState , dist.meta.name , SecurityAction.ACTION_RELEASE , false );
	}
	
	public void archiveRelease( ScopeState parentState , ActionBase action , Dist dist ) throws Exception {
		ActionArchiveRelease ma = new ActionArchiveRelease( action , null , dist );
		ma.runSimpleProduct( parentState , dist.meta.name , SecurityAction.ACTION_RELEASE , false );
	}
	
	public void touchRelease( ScopeState parentState , ActionBase action , Meta meta , String RELEASELABEL ) throws Exception {
		ActionTouchRelease ma = new ActionTouchRelease( action , null , meta , RELEASELABEL );
		ma.runSimpleProduct( parentState , meta.name , SecurityAction.ACTION_RELEASE , false );
	}
	
	public void setSchedule( ScopeState parentState , ActionBase action , Dist dist , Date[] dates ) throws Exception {
		ActionSchedulePhase ma = new ActionSchedulePhase( action , null , dist , dates );
		ma.runSimpleProduct( parentState , dist.meta.name , SecurityAction.ACTION_RELEASE , false );
	}
	
	public void executeTickets( ScopeState parentState , ActionBase action , Dist dist , String method , String[] args ) throws Exception {
		ActionTickets ma = new ActionTickets( action , null , dist , method , args );
		ma.runSimpleProduct( parentState , dist.meta.name , SecurityAction.ACTION_RELEASE , false );
	}
	
}
