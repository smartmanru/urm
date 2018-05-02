package org.urm.action.release;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.action.ActionProductScopeMaker;
import org.urm.action.ActionReleaseScopeMaker;
import org.urm.action.ActionScope;
import org.urm.action.codebase.CodebaseCommand;
import org.urm.common.Common;
import org.urm.common.action.CommandMethodMeta.SecurityAction;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.dist.Dist;
import org.urm.engine.status.ScopeState;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.product.Meta;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseRepository;

public class ReleaseCommand {

	public static void createMasterInitial( ScopeState parentState , ActionBase action , Meta meta , String RELEASEVER ) throws Exception {
		ActionCreateMaster ma = new ActionCreateMaster( action , null , meta , RELEASEVER , false );
		ma.runSimpleProduct( parentState , meta , SecurityAction.ACTION_RELEASE , false );
	}
	
	public static void createMasterCopy( ScopeState parentState , ActionBase action , Meta meta , String RELEASEVER ) throws Exception {
		ActionCreateMaster ma = new ActionCreateMaster( action , null , meta , RELEASEVER , true );
		ma.runSimpleProduct( parentState , meta , SecurityAction.ACTION_RELEASE , false );
	}
	
	public static void deleteMaster( ScopeState parentState , ActionBase action , Meta meta ) throws Exception {
		ReleaseRepository repo = meta.getReleases();
		Release release = repo.findDefaultMaster();
		if( release == null )
			Common.exitUnexpected();
		
		ActionDeleteRelease ma = new ActionDeleteRelease( action , null , release , true );
		ma.runSimpleProduct( parentState , meta , SecurityAction.ACTION_RELEASE , false );
	}
	
	public static void masterStatus( ScopeState parentState , ActionBase action , Meta meta ) throws Exception {
		ReleaseRepository repo = meta.getReleases();
		Release release = repo.findDefaultMaster();
		ActionPrintReleaseStatus ma = new ActionPrintReleaseStatus( action , null , release );
		ma.runSimpleProduct( parentState , meta , SecurityAction.ACTION_RELEASE , true );
	}
	
	public static void createRelease( ScopeState parentState , ActionBase action , Meta meta , String RELEASELABEL , Date releaseDate , ReleaseLifecycle lc ) throws Exception {
		ActionCreateRelease ma = new ActionCreateRelease( action , null , meta , RELEASELABEL , releaseDate , lc );
		ma.runSimpleProduct( parentState , meta , SecurityAction.ACTION_RELEASE , false );
	}

	public static void importRelease( ScopeState parentState , ActionBase action , Meta meta , String RELEASELABEL ) throws Exception {
		ActionImportRelease ma = new ActionImportRelease( action , null , meta , RELEASELABEL );
		ma.runSimpleProduct( parentState , meta , SecurityAction.ACTION_RELEASE , false );
	}

	public static void modifyRelease( ScopeState parentState , ActionBase action , Release release , Date releaseDate , ReleaseLifecycle lc ) throws Exception {
		Meta meta = release.getMeta();
		ActionModifyRelease ma = new ActionModifyRelease( action , null , meta , release , releaseDate , lc );
		ma.runSimpleProduct( parentState , meta , SecurityAction.ACTION_RELEASE , false );
	}

	public static void deleteRelease( ScopeState parentState , ActionBase action , Release release , boolean force ) throws Exception {
		if( release.isMaster() )
			action.exit0( _Error.CannotDropProd0 , "Cannot drop full master release, use master command" );
		
		Meta meta = release.getMeta();
		ActionDeleteRelease ma = new ActionDeleteRelease( action , null , release , force );
		ma.runSimpleProduct( parentState , meta , SecurityAction.ACTION_RELEASE , false );
	}
	
	public static void cleanupDist( ScopeState parentState , ActionBase action , Dist dist ) throws Exception {
		ActionForceCloseDist ma = new ActionForceCloseDist( action , null , dist );
		ma.runSimpleProduct( parentState , dist.meta , SecurityAction.ACTION_RELEASE , false );
	}
	
	public static void copyRelease( ScopeState parentState , ActionBase action , Release releaseSrc , String RELEASEDST , Date releaseDate , ReleaseLifecycle lc ) throws Exception {
		Meta meta = releaseSrc.getMeta();
		ActionCopyRelease ma = new ActionCopyRelease( action , null , releaseSrc , RELEASEDST , releaseDate , lc );
		ma.runSimpleProduct( parentState , meta , SecurityAction.ACTION_RELEASE , false );
	}
	
	public static void finishRelease( ScopeState parentState , ActionBase action , Release release ) throws Exception {
		Meta meta = release.getMeta();
		ActionFinishRelease ma = new ActionFinishRelease( action , null , release );
		ma.runSimpleProduct( parentState , meta , SecurityAction.ACTION_RELEASE , false );
	}
	
	public static void completeRelease( ScopeState parentState , ActionBase action , Release release ) throws Exception {
		Meta meta = release.getMeta();
		ActionCompleteRelease ma = new ActionCompleteRelease( action , null , release );
		ma.runSimpleProduct( parentState , meta , SecurityAction.ACTION_RELEASE , false );
	}
	
	public static void reopenRelease( ScopeState parentState , ActionBase action , Release release ) throws Exception {
		Meta meta = release.getMeta();
		ActionReopenRelease ma = new ActionReopenRelease( action , null , release );
		ma.runSimpleProduct( parentState , meta , SecurityAction.ACTION_RELEASE , false );
	}
	
	public static void statusRelease( ScopeState parentState , ActionBase action , Release release ) throws Exception {
		Meta meta = release.getMeta();
		ActionPrintReleaseStatus ma = new ActionPrintReleaseStatus( action , null , release );
		ma.runSimpleProduct( parentState , meta , SecurityAction.ACTION_RELEASE , false );
	}

	public static void appendMaster( ScopeState parentState , ActionBase action , Release release ) throws Exception {
		ActionAppendMaster ma = new ActionAppendMaster( action , null , release );
		Meta meta = release.getMeta();
		ma.runSimpleProduct( parentState , meta , SecurityAction.ACTION_RELEASE , false );
	}
	
	private static void addReleaseScope( ScopeState parentState , ActionBase action , Release release , ActionScope scope ) throws Exception {
		ActionAddScope ma = new ActionAddScope( action , null , release );
		if( !ma.runAll( parentState , scope , null , SecurityAction.ACTION_RELEASE , false ) )
			action.exit0( _Error.ReleaseSetChangeErrors0 , "release set is not changed because of errors" );

		action.info( "scope (" + scope.getScopeInfo( action ) + ") - added to release" );
	}
	
	private static void setScopeSpecifics( ScopeState parentState , ActionBase action , Release release , ActionScope scope ) throws Exception {
		ActionSetSpecifics ma = new ActionSetSpecifics( action , null , release );
		if( !ma.runAll( null , scope , null , SecurityAction.ACTION_RELEASE , false ) )
			action.exit0( _Error.ReleaseSetChangeErrors0 , "release set is not changed because of errors" );
		
		action.info( "scope (" + scope.getScopeInfo( action ) + ") - release sccope specifics updated" );
	}
	
	private static void setReleaseScope( ScopeState parentState , ActionBase action , Release release , boolean source , String[] pathItems ) throws Exception {
		ActionSetScope ma = new ActionSetScope( action , null , release , source , pathItems );
		Meta meta = release.getMeta();
		if( !ma.runSimpleProduct( null , meta , SecurityAction.ACTION_RELEASE , false ) )
			action.exit0( _Error.ReleaseSetChangeErrors0 , "release set is not changed because of errors" );
		
		action.info( "release scope has been changed" );
	}
	
	public static void addReleaseBuildProjects( ScopeState parentState , ActionBase action , Release release , String SET , String[] elements ) throws Exception {
		if( release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		Meta meta = release.getMeta();
		ActionProductScopeMaker maker = new ActionProductScopeMaker( action , meta );
		maker.addScopeProductSet( SET , elements );
		ActionScope scope = maker.getScope();
		addReleaseScope( parentState , action , release , scope );
	}

	public static void setScopeSpecifics( ScopeState parentState , ActionBase action , Release release , String SET , String[] elements ) throws Exception {
		if( release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		ActionReleaseScopeMaker maker = new ActionReleaseScopeMaker( action , release );
		maker.addScopeReleaseSet( SET , elements );
		ActionScope scope = maker.getScope();
		setScopeSpecifics( parentState , action , release , scope );
	}

	public static void addReleaseConfigItems( ScopeState parentState , ActionBase action , Release release , String[] elements ) throws Exception {
		if( release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		Meta meta = release.getMeta();
		ActionProductScopeMaker maker = new ActionProductScopeMaker( action , meta );
		maker.addScopeProductCategory( DBEnumScopeCategoryType.CONFIG , elements );
		ActionScope scope = maker.getScope();
		addReleaseScope( parentState , action , release , scope );
	}

	public static void addReleaseDatabaseItems( ScopeState parentState , ActionBase action , Release release , String[] DELIVERIES ) throws Exception {
		if( release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		Meta meta = release.getMeta();
		ActionProductScopeMaker maker = new ActionProductScopeMaker( action , meta );
		maker.addScopeProductCategory( DBEnumScopeCategoryType.DB , DELIVERIES );
		ActionScope scope = maker.getScope();
		addReleaseScope( parentState , action , release , scope );
	}

	public static void addReleaseBuildItems( ScopeState parentState , ActionBase action , Release release , String[] ITEMS ) throws Exception {
		if( release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		Meta meta = release.getMeta();
		ActionProductScopeMaker maker = new ActionProductScopeMaker( action , meta );
		maker.addScopeProductDistItems( ITEMS );
		ActionScope scope = maker.getScope();
		addReleaseScope( parentState , action , release , scope );
	}

	public static void setScope( ScopeState parentState , ActionBase action , Release release , boolean source , String[] ITEMS ) throws Exception {
		if( release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		setReleaseScope( parentState , action , release , source , ITEMS );
	}

	public static void buildRelease( ScopeState parentState , ActionBase action , Release release , String SET , String[] PROJECTS ) throws Exception {
		if( release.isCumulative() )
			action.exit0( _Error.CannotBuildCumulative0 , "cannot build cumulative release" );
		
		CodebaseCommand.buildRelease( parentState , action , release , SET , PROJECTS );
	}

	public static void getAllRelease( ScopeState parentState , ActionBase action , Release release , String SET , String[] PROJECTS ) throws Exception {
		if( release.isCumulative() )
			action.exit0( _Error.CannotDownloadCumulative0 , "cannot download cumulative release" );
		
		action.context.CTX_DIST = true;
		CodebaseCommand.getAllRelease( parentState , action , release , SET , PROJECTS );
	}

	public static void getCumulativeRelease( ScopeState parentState , ActionBase action , Release release ) throws Exception {
		if( !release.isCumulative() )
			action.exit0( _Error.NotCumulativeRelease0 , "should be cumulative release" );
		
		Meta meta = release.getMeta();
		ActionGetCumulative ca = new ActionGetCumulative( action , null , release );
		ca.runSimpleProduct( parentState , meta , SecurityAction.ACTION_RELEASE , false );
	}

	private static void descope( ScopeState parentState , ActionBase action , Release release , ActionScope scope ) throws Exception {
		if( release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		ActionDescope ma = new ActionDescope( action , null , release );
		if( !ma.runAll( parentState , scope , null , SecurityAction.ACTION_RELEASE , false ) )
			action.exit0( _Error.ReleaseSetChangeErrors0 , "release set is not changed because of errors" );

		action.info( "scope (" + scope.getScopeInfo( action ) + ") - removed from release" );
	}
	
	public static void descopeAll( ScopeState parentState , ActionBase action , Release release ) throws Exception {
		if( release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );

		ActionReleaseScopeMaker maker = new ActionReleaseScopeMaker( action , release );
		maker.addScopeAll();
		ActionScope scope = maker.getScope();
		
		ActionDescope ma = new ActionDescope( action , null , release );
		if( !ma.runAll( parentState , scope , null , SecurityAction.ACTION_RELEASE , false ) )
			action.exit0( _Error.ReleaseSetChangeErrors0 , "release set is not changed because of errors" );
		
		action.info( "entire scope has been removed from release" );
	}
	
	public static void descopeConfComps( ScopeState parentState , ActionBase action , Release release , String[] COMPS ) throws Exception {
		if( release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		ActionReleaseScopeMaker maker = new ActionReleaseScopeMaker( action , release );
		maker.addScopeReleaseCategory( DBEnumScopeCategoryType.CONFIG , COMPS );
		ActionScope scope = maker.getScope();
		descope( parentState , action , release , scope );
	}
	
	public static void descopeManualItems( ScopeState parentState , ActionBase action , Release release , String[] ITEMS ) throws Exception {
		if( release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		ActionReleaseScopeMaker maker = new ActionReleaseScopeMaker( action , release );
		maker.addScopeReleaseCategory( DBEnumScopeCategoryType.MANUAL , ITEMS );
		ActionScope scope = maker.getScope();
		descope( parentState , action , release , scope );
	}
	
	public static void descopeBinary( ScopeState parentState , ActionBase action , Release release , String SET , String PROJECT , String[] ITEMS ) throws Exception {
		if( release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		ActionReleaseScopeMaker maker = new ActionReleaseScopeMaker( action , release );
		if( PROJECT.equals( "all" ) )
			maker.addScopeReleaseSet( SET , new String [] { "all" } );
		else
			maker.addScopeReleaseProjectItems( PROJECT , ITEMS );
		ActionScope scope = maker.getScope();
		descope( parentState , action , release , scope );
	}
	
	public static void descopeDatabase( ScopeState parentState , ActionBase action , Release release , String[] DELIVERIES ) throws Exception {
		if( release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		ActionReleaseScopeMaker maker = new ActionReleaseScopeMaker( action , release );
		maker.addScopeReleaseCategory( DBEnumScopeCategoryType.DB , DELIVERIES );
		ActionScope scope = maker.getScope();
		descope( parentState , action , release , scope );
	}

	public static void nextPhase( ScopeState parentState , ActionBase action , Release release ) throws Exception {
		Meta meta = release.getMeta();
		ActionSchedulePhase ma = new ActionSchedulePhase( action , null , release );
		ma.runSimpleProduct( parentState , meta , SecurityAction.ACTION_RELEASE , false );
	}
	
	public static void setPhaseDeadline( ScopeState parentState , ActionBase action , Release release , String PHASE , Date deadlineDate ) throws Exception {
		Meta meta = release.getMeta();
		ActionSchedulePhase ma = new ActionSchedulePhase( action , null , release , PHASE , deadlineDate );
		ma.runSimpleProduct( parentState , meta , SecurityAction.ACTION_RELEASE , false );
	}
	
	public static void setPhaseDuration( ScopeState parentState , ActionBase action , Release release , String PHASE , int duration ) throws Exception {
		Meta meta = release.getMeta();
		ActionSchedulePhase ma = new ActionSchedulePhase( action , null , release , PHASE , duration );
		ma.runSimpleProduct( parentState , meta , SecurityAction.ACTION_RELEASE , false );
	}
	
	public static void archiveRelease( ScopeState parentState , ActionBase action , Release release ) throws Exception {
		Meta meta = release.getMeta();
		ActionArchiveRelease ma = new ActionArchiveRelease( action , null , release );
		ma.runSimpleProduct( parentState , meta , SecurityAction.ACTION_RELEASE , false );
	}
	
	public static void touchRelease( ScopeState parentState , ActionBase action , Meta meta , String RELEASELABEL ) throws Exception {
		ActionTouchRelease ma = new ActionTouchRelease( action , null , meta , RELEASELABEL );
		ma.runSimpleProduct( parentState , meta , SecurityAction.ACTION_RELEASE , false );
	}
	
	public static void setSchedule( ScopeState parentState , ActionBase action , Release release , Date[] dates ) throws Exception {
		Meta meta = release.getMeta();
		ActionSchedulePhase ma = new ActionSchedulePhase( action , null , release , dates );
		ma.runSimpleProduct( parentState , meta , SecurityAction.ACTION_RELEASE , false );
	}
	
	public static void executeTickets( ScopeState parentState , ActionBase action , Release release , String method , String[] args ) throws Exception {
		Meta meta = release.getMeta();
		ActionTickets ma = new ActionTickets( action , null , release , method , args );
		ma.runSimpleProduct( parentState , meta , SecurityAction.ACTION_RELEASE , false );
	}
	
}
