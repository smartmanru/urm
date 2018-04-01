package org.urm.engine.executor;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.action.release.ReleaseCommand;
import org.urm.common.Common;
import org.urm.common.action.CommandMeta;
import org.urm.common.meta.ReleaseCommandMeta;
import org.urm.db.core.DBEnums.DBEnumScopeCategoryType;
import org.urm.engine.Engine;
import org.urm.engine.action.CommandMethod;
import org.urm.engine.action.CommandExecutor;
import org.urm.engine.dist.Dist;
import org.urm.engine.status.ScopeState;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.product.Meta;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseSchedule;

public class CommandExecutorRelease extends CommandExecutor {

	public static CommandExecutorRelease createExecutor( Engine engine ) throws Exception {
		ReleaseCommandMeta commandInfo = new ReleaseCommandMeta( engine.optionsMeta );
		return( new CommandExecutorRelease( engine , commandInfo ) );
	}
		
	private CommandExecutorRelease( Engine engine , CommandMeta commandInfo ) throws Exception {
		super( engine , commandInfo );
		
		defineAction( new CreateRelease() , ReleaseCommandMeta.METHOD_CREATE );
		defineAction( new ModifyRelease() , ReleaseCommandMeta.METHOD_MODIFY );
		defineAction( new PhaseRelease() , ReleaseCommandMeta.METHOD_PHASE );
		defineAction( new ScheduleRelease() , ReleaseCommandMeta.METHOD_SCHEDULE );
		defineAction( new DeleteRelease() , ReleaseCommandMeta.METHOD_DROP );
		defineAction( new StatusRelease() , ReleaseCommandMeta.METHOD_STATUS );
		defineAction( new CleanupRelease() , ReleaseCommandMeta.METHOD_CLEANUP );
		defineAction( new CopyRelease() , ReleaseCommandMeta.METHOD_COPY );
		defineAction( new ImportRelease() , ReleaseCommandMeta.METHOD_IMPORT );
		defineAction( new FinishRelease() , ReleaseCommandMeta.METHOD_FINISH );
		defineAction( new CompleteRelease() , ReleaseCommandMeta.METHOD_COMPLETE );
		defineAction( new ReopenRelease() , ReleaseCommandMeta.METHOD_REOPEN );
		defineAction( new MasterOperations() , ReleaseCommandMeta.METHOD_MASTER );
		defineAction( new ArchiveRelease() , ReleaseCommandMeta.METHOD_ARCHIVE );
		defineAction( new TouchRelease() , ReleaseCommandMeta.METHOD_TOUCH );
		defineAction( new AddReleaseBuildProjects() , ReleaseCommandMeta.METHOD_SCOPEADD );
		defineAction( new ScopeSpec() , ReleaseCommandMeta.METHOD_SCOPESPEC );
		defineAction( new AddReleaseBuildItems() , ReleaseCommandMeta.METHOD_SCOPEITEMS );
		defineAction( new AddReleaseDatabaseItems() , ReleaseCommandMeta.METHOD_SCOPEDB );
		defineAction( new AddReleaseConfigItems() , ReleaseCommandMeta.METHOD_SCOPECONF );
		defineAction( new ScopeSet() , ReleaseCommandMeta.METHOD_SCOPESET );
		defineAction( new BuildRelease() , ReleaseCommandMeta.METHOD_BUILD );
		defineAction( new GetRelease() , ReleaseCommandMeta.METHOD_GETDIST );
		defineAction( new DescopeRelease() , ReleaseCommandMeta.METHOD_DESCOPE );
		defineAction( new ExecuteTickets() , ReleaseCommandMeta.METHOD_TICKETS );
	}	

	@Override
	public boolean runExecutorImpl( ScopeState parentState , ActionBase action , CommandMethod method ) {
		boolean res = super.runMethod( parentState , action , method );
		return( res );
	}

	private class CreateRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		Date releaseDate = getDateArg( action , 1 );
		ReleaseLifecycle lc = getLifecycleArg( action , 2 );
		checkNoArgs( action , 3 );
		Meta meta = action.getContextMeta();
		ReleaseCommand.createRelease( parentState , action , meta , RELEASELABEL , releaseDate , lc );
	}
	}

	private class ImportRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		Meta meta = action.getContextMeta();
		ReleaseCommand.importRelease( parentState , action , meta , RELEASELABEL );
	}
	}

	private class ModifyRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		Date releaseDate = getDateArg( action , 1 );
		ReleaseLifecycle lc = getLifecycleArg( action , 2 );
		checkNoArgs( action , 3 );
		Release release = super.getRelease( action , RELEASELABEL );
		ReleaseCommand.modifyRelease( parentState , action , release , releaseDate , lc );
	}
	}

	private class PhaseRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String CMD = getRequiredArg( action , 1 , "CMD" );
		
		Release release = super.getRelease( action , RELEASELABEL );
		
		if( CMD.equals( "next" ) ) {
			checkNoArgs( action , 2 );
			ReleaseCommand.nextPhase( parentState , action , release );
		}
		else
		if( CMD.equals( "deadline" ) ) {
			String PHASE = getRequiredArg( action , 2 , "PHASE" );
			Date deadlineDate = getDateArg( action , 3 );
			checkNoArgs( action , 4 );
			ReleaseCommand.setPhaseDeadline( parentState , action , release , PHASE , deadlineDate );
		}
		else
		if( CMD.equals( "days" ) ) {
			String PHASE = getRequiredArg( action , 2 , "PHASE" );
			int duration = getIntArg( action , 3 , 0 );
			if( duration < 0 )
				super.wrongArgs( action );
				
			checkNoArgs( action , 4 );
			ReleaseCommand.setPhaseDuration( parentState , action , release , PHASE , duration );
		}
		else
			super.wrongArgs( action );
	}
	}

	private class ScheduleRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		Release release = super.getRelease( action , RELEASELABEL );
		
		ReleaseSchedule schedule = release.getSchedule();
		int nPhases = schedule.getPhaseCount();
		Date[] dates = new Date[ nPhases * 2 ];
		for( int k = 0; k < nPhases; k++ ) {
			dates[ 2 * k ] = getRequiredDateArg( action , 1 + 2 * k , "STARTDATE" + (k+1) );
			dates[ 2 * k + 1 ] = getRequiredDateArg( action , 1 + 2 * k + 1 , "FINISHDATE" + (k+1) );
		}
		
		checkNoArgs( action , 2 * nPhases + 2 );
		ReleaseCommand.setSchedule( parentState , action , release , dates );
	}
	}
	
	private class DeleteRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		Release release = super.getRelease( action , RELEASELABEL );
		ReleaseCommand.deleteRelease( parentState , action , release , action.isForced() );
	}
	}

	private class CleanupRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		Dist dist = super.getDist( action , RELEASELABEL );
		ReleaseCommand.cleanupDist( parentState , action , dist );
	}
	}

	private class CopyRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASESRC = getRequiredArg( action , 0 , "RELEASESRC" );
		String RELEASEDST = getRequiredArg( action , 1 , "RELEASEDST" );
		Date releaseDate = getRequiredDateArg( action , 2 , "RELEASEDATE" );
		ReleaseLifecycle lc = getLifecycleArg( action , 3 );
		checkNoArgs( action , 4 );
		Release release = super.getRelease( action , RELEASESRC );
		ReleaseCommand.copyRelease( parentState , action , release , RELEASEDST , releaseDate , lc );
	}
	}

	private class FinishRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		Release release = super.getRelease( action , RELEASELABEL );
		ReleaseCommand.finishRelease( parentState , action , release );
	}
	}

	private class CompleteRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		Release release = super.getRelease( action , RELEASELABEL );
		ReleaseCommand.completeRelease( parentState , action , release );
	}
	}

	private class ReopenRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		Release release = super.getRelease( action , RELEASELABEL );
		ReleaseCommand.reopenRelease( parentState , action , release );
	}
	}

	private class MasterOperations extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String CMD = getRequiredArg( action , 0 , "CMD" );
		Meta meta = action.getContextMeta();
		if( CMD.equals( "create" ) ) {
			String RELEASEVER = getRequiredArg( action , 1 , "RELEASEVER" );
			checkNoArgs( action , 2 );
			ReleaseCommand.createMasterInitial( parentState , action , meta , RELEASEVER );
		}
		else
		if( CMD.equals( "copy" ) ) {
			String RELEASEDIR = getRequiredArg( action , 1 , "RELEASEDIR" );
			checkNoArgs( action , 2 );
			ReleaseCommand.createMasterCopy( parentState , action , meta , RELEASEDIR );
		}
		else
		if( CMD.equals( "status" ) ) {
			checkNoArgs( action , 1 );
			ReleaseCommand.masterStatus( parentState , action , meta );
		}
		else
		if( CMD.equals( "add" ) ) {
			String RELEASELABEL = getRequiredArg( action , 1 , "RELEASELABEL" );
			checkNoArgs( action , 2 );
			Release release = super.getRelease( action , RELEASELABEL );
			ReleaseCommand.appendMaster( parentState , action , release );
		}
		else
		if( CMD.equals( "drop" ) ) {
			checkNoArgs( action , 1 );
			ReleaseCommand.deleteMaster( parentState , action , meta );
		}
		else
			super.wrongArgs( action );
	}
	}

	private class ArchiveRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		Release release = super.getRelease( action , RELEASELABEL );
		ReleaseCommand.archiveRelease( parentState , action , release );
	}
	}

	private class TouchRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		Release release = super.getRelease( action , RELEASELABEL );
		ReleaseCommand.touchRelease( parentState , action , release );
	}
	}

	private class StatusRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		Release release = super.getRelease( action , RELEASELABEL );
		ReleaseCommand.statusRelease( parentState , action , release );
	}
	}

	private class AddReleaseBuildProjects extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String SET = getArg( action , 1 );
		String[] elements = getArgList( action , 2 );
		
		Release release = super.getRelease( action , RELEASELABEL );
		ReleaseCommand.addReleaseBuildProjects( parentState , action , release , SET , elements );
	}
	}

	private class ScopeSpec extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String SET = getArg( action , 1 );
		String[] elements = getArgList( action , 2 );
		
		Release release = super.getRelease( action , RELEASELABEL );
		ReleaseCommand.setScopeSpecifics( parentState , action , release , SET , elements );
	}
	}

	private class AddReleaseConfigItems extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String[] elements = getArgList( action , 1 );
		
		Release release = super.getRelease( action , RELEASELABEL );
		ReleaseCommand.addReleaseConfigItems( parentState , action , release , elements );
	}
	}

	private class AddReleaseDatabaseItems extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String[] elements = getArgList( action , 1 );
		
		Release release = super.getRelease( action , RELEASELABEL );
		ReleaseCommand.addReleaseDatabaseItems( parentState , action , release , elements );
	}
	}

	private class AddReleaseBuildItems extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String[] elements = getArgList( action , 1 );
		
		Release release = super.getRelease( action , RELEASELABEL );
		ReleaseCommand.addReleaseBuildItems( parentState , action , release , elements );
	}
	}

	private class ScopeSet extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String TYPE = getRequiredArg( action , 1 , "SCOPETYPE" );
		String[] elements = getArgList( action , 2 );
		
		Release release = super.getRelease( action , RELEASELABEL );
		if( TYPE.equals( "source" ) )
			ReleaseCommand.setScope( parentState , action , release , true , elements );
		else
		if( TYPE.equals( "delivery" ) )
			ReleaseCommand.setScope( parentState , action , release , false , elements );
	}
	}

	private class BuildRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String SET = getArg( action , 1 );
		String[] PROJECTS = getArgList( action , 2 );

		Release release = super.getRelease( action , RELEASELABEL );
		ReleaseCommand.buildRelease( parentState , action , release , SET , PROJECTS );
	}
	}

	private class GetRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String SET = getArg( action , 1 );
		String[] PROJECTS = getArgList( action , 2 );

		Release release = super.getRelease( action , RELEASELABEL );
		
		if( release.isCumulative() ) {
			if( SET.isEmpty() || SET.equals( "all" ) )
				ReleaseCommand.getCumulativeRelease( parentState , action , release );
			else
				action.exit0( _Error.UnexpectedCumulativeParameters0 , "unexpected parameters to settle cumulative release" );
		}
		else
			ReleaseCommand.getAllRelease( parentState , action , release , SET , PROJECTS );
	}
	}

	private class DescopeRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		Release release = super.getRelease( action , RELEASELABEL );
		
		String SET = getRequiredArg( action , 1 , "SET" );
		if( SET.equals( "all" ) ) {
			ReleaseCommand.descopeAll( parentState , action , release );
		}
		else
		if( SET.equals( Common.getEnumLower( DBEnumScopeCategoryType.CONFIG ) ) ) {
			String[] COMPS = getArgList( action , 2 );
			ReleaseCommand.descopeConfComps( parentState , action , release , COMPS );
		}
		else
		if( SET.equals( Common.getEnumLower( DBEnumScopeCategoryType.DB ) ) ) {
			String[] ITEMS = getArgList( action , 2 );
			ReleaseCommand.descopeDatabase( parentState , action , release , ITEMS );
		}
		else
		if( SET.equals( Common.getEnumLower( DBEnumScopeCategoryType.MANUAL ) ) ) {
			String[] ITEMS = getArgList( action , 2 );
			ReleaseCommand.descopeManualItems( parentState , action , release , ITEMS );
		}
		else {
			String PROJECT = getArg( action , 2 );
			String[] ITEMS = getArgList( action , 3 );
			ReleaseCommand.descopeBinary( parentState , action , release , SET , PROJECT , ITEMS );
		}
	}
	}

	private class ExecuteTickets extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String METHOD = getRequiredArg( action , 1 , "METHOD" );
		String[] args = getArgList( action , 2 );
		
		Release release = super.getRelease( action , RELEASELABEL );
		ReleaseCommand.executeTickets( parentState , action , release , METHOD , args );
	}
	}
	
}
