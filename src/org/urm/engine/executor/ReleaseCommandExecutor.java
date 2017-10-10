package org.urm.engine.executor;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.action.release.ReleaseCommand;
import org.urm.common.Common;
import org.urm.common.action.CommandMeta;
import org.urm.common.meta.ReleaseCommandMeta;
import org.urm.engine.Engine;
import org.urm.engine.action.CommandMethod;
import org.urm.engine.action.CommandExecutor;
import org.urm.engine.dist.Dist;
import org.urm.engine.status.ScopeState;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.product.Meta;
import org.urm.meta.Types.*;

public class ReleaseCommandExecutor extends CommandExecutor {

	ReleaseCommand impl;
	
	public static ReleaseCommandExecutor createExecutor( Engine engine ) throws Exception {
		ReleaseCommandMeta commandInfo = new ReleaseCommandMeta( engine.optionsMeta );
		return( new ReleaseCommandExecutor( engine , commandInfo ) );
	}
		
	private ReleaseCommandExecutor( Engine engine , CommandMeta commandInfo ) throws Exception {
		super( engine , commandInfo );
		
		defineAction( new CreateRelease() , ReleaseCommandMeta.METHOD_CREATE );
		defineAction( new ModifyRelease() , ReleaseCommandMeta.METHOD_MODIFY );
		defineAction( new PhaseRelease() , ReleaseCommandMeta.METHOD_PHASE );
		defineAction( new ScheduleRelease() , ReleaseCommandMeta.METHOD_SCHEDULE );
		defineAction( new DeleteRelease() , ReleaseCommandMeta.METHOD_DROP );
		defineAction( new StatusRelease() , ReleaseCommandMeta.METHOD_STATUS );
		defineAction( new CleanupRelease() , ReleaseCommandMeta.METHOD_CLEANUP );
		defineAction( new CopyRelease() , ReleaseCommandMeta.METHOD_COPY );
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
		
		impl = new ReleaseCommand();
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
		impl.createRelease( parentState , action , meta , RELEASELABEL , releaseDate , lc );
	}
	}

	private class ModifyRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		Date releaseDate = getDateArg( action , 1 );
		ReleaseLifecycle lc = getLifecycleArg( action , 2 );
		checkNoArgs( action , 3 );
		Meta meta = action.getContextMeta();
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		impl.modifyRelease( parentState , action , dist , releaseDate , lc );
	}
	}

	private class PhaseRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String CMD = getRequiredArg( action , 1 , "CMD" );
		
		Meta meta = action.getContextMeta();
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		
		if( CMD.equals( "next" ) ) {
			checkNoArgs( action , 2 );
			impl.nextPhase( parentState , action , dist );
		}
		else
		if( CMD.equals( "deadline" ) ) {
			String PHASE = getRequiredArg( action , 2 , "PHASE" );
			Date deadlineDate = getDateArg( action , 3 );
			checkNoArgs( action , 4 );
			impl.setPhaseDeadline( parentState , action , dist , PHASE , deadlineDate );
		}
		else
		if( CMD.equals( "days" ) ) {
			String PHASE = getRequiredArg( action , 2 , "PHASE" );
			int duration = getIntArg( action , 3 , 0 );
			if( duration < 0 )
				super.wrongArgs( action );
				
			checkNoArgs( action , 4 );
			impl.setPhaseDuration( parentState , action , dist , PHASE , duration );
		}
		else
			super.wrongArgs( action );
	}
	}

	private class ScheduleRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		Meta meta = action.getContextMeta();
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		
		int nPhases = dist.release.schedule.getPhaseCount();
		Date[] dates = new Date[ nPhases * 2 ];
		for( int k = 0; k < nPhases; k++ ) {
			dates[ 2 * k ] = getRequiredDateArg( action , 1 + 2 * k , "STARTDATE" + (k+1) );
			dates[ 2 * k + 1 ] = getRequiredDateArg( action , 1 + 2 * k + 1 , "FINISHDATE" + (k+1) );
		}
		
		checkNoArgs( action , 2 * nPhases + 2 );
		impl.setSchedule( parentState , action , dist , dates );
	}
	}
	
	private class DeleteRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		Meta meta = action.getContextMeta();
		impl.deleteRelease( parentState , action , meta , RELEASELABEL , action.isForced() );
	}
	}

	private class CleanupRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		Meta meta = action.getContextMeta();
		impl.cleanupRelease( parentState , action , meta , RELEASELABEL );
	}
	}

	private class CopyRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASESRC = getRequiredArg( action , 0 , "RELEASESRC" );
		String RELEASEDST = getRequiredArg( action , 1 , "RELEASEDST" );
		Date releaseDate = getRequiredDateArg( action , 2 , "RELEASEDATE" );
		ReleaseLifecycle lc = getLifecycleArg( action , 3 );
		checkNoArgs( action , 4 );
		Meta meta = action.getContextMeta();
		impl.copyRelease( parentState , action , meta , RELEASESRC , RELEASEDST , releaseDate , lc );
	}
	}

	private class FinishRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		Meta meta = action.getContextMeta();
		impl.finishRelease( parentState , action , meta , RELEASELABEL );
	}
	}

	private class CompleteRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		Meta meta = action.getContextMeta();
		impl.completeRelease( parentState , action , meta , RELEASELABEL );
	}
	}

	private class ReopenRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		Meta meta = action.getContextMeta();
		impl.reopenRelease( parentState , action , meta , RELEASELABEL );
	}
	}

	private class MasterOperations extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String CMD = getRequiredArg( action , 0 , "CMD" );
		Meta meta = action.getContextMeta();
		if( CMD.equals( "create" ) ) {
			String RELEASEVER = getRequiredArg( action , 1 , "RELEASEVER" );
			checkNoArgs( action , 2 );
			impl.createProdInitial( parentState , action , meta , RELEASEVER );
		}
		else
		if( CMD.equals( "copy" ) ) {
			String RELEASEDIR = getRequiredArg( action , 1 , "RELEASEDIR" );
			checkNoArgs( action , 2 );
			impl.createProdCopy( parentState , action , meta , RELEASEDIR );
		}
		else
		if( CMD.equals( "status" ) ) {
			checkNoArgs( action , 1 );
			impl.prodStatus( parentState , action , meta );
		}
		else
		if( CMD.equals( "add" ) ) {
			String RELEASELABEL = getRequiredArg( action , 1 , "RELEASELABEL" );
			checkNoArgs( action , 2 );
			Dist dist = action.getReleaseDist( meta , RELEASELABEL );
			impl.appendProd( parentState , action , dist );
		}
		else
		if( CMD.equals( "drop" ) ) {
			checkNoArgs( action , 1 );
			impl.deleteProd( parentState , action , meta );
		}
		else
			super.wrongArgs( action );
	}
	}

	private class ArchiveRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		Meta meta = action.getContextMeta();
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		impl.archiveRelease( parentState , action , dist );
	}
	}

	private class TouchRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		Meta meta = action.getContextMeta();
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		impl.touchRelease( parentState , action , meta , RELEASELABEL );
	}
	}

	private class StatusRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		Meta meta = action.getContextMeta();
		impl.statusRelease( parentState , action , meta , RELEASELABEL );
	}
	}

	private class AddReleaseBuildProjects extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String SET = getArg( action , 1 );
		String[] elements = getArgList( action , 2 );
		
		Meta meta = action.getContextMeta();
		impl.addReleaseBuildProjects( parentState , action , meta , RELEASELABEL , SET , elements );
	}
	}

	private class ScopeSpec extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String SET = getArg( action , 1 );
		String[] elements = getArgList( action , 2 );
		
		Meta meta = action.getContextMeta();
		impl.setScopeSpecifics( parentState , action , meta , RELEASELABEL , SET , elements );
	}
	}

	private class AddReleaseConfigItems extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String[] elements = getArgList( action , 1 );
		
		Meta meta = action.getContextMeta();
		impl.addReleaseConfigItems( parentState , action , meta , RELEASELABEL , elements );
	}
	}

	private class AddReleaseDatabaseItems extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String[] elements = getArgList( action , 1 );
		
		Meta meta = action.getContextMeta();
		impl.addReleaseDatabaseItems( parentState , action , meta , RELEASELABEL , elements );
	}
	}

	private class AddReleaseBuildItems extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String[] elements = getArgList( action , 1 );
		
		Meta meta = action.getContextMeta();
		impl.addReleaseBuildItems( parentState , action , meta , RELEASELABEL , elements );
	}
	}

	private class ScopeSet extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String TYPE = getRequiredArg( action , 1 , "SCOPETYPE" );
		String[] elements = getArgList( action , 2 );
		
		Meta meta = action.getContextMeta();
		if( TYPE.equals( "source" ) )
			impl.setScope( parentState , action , meta , RELEASELABEL , true , elements );
		else
		if( TYPE.equals( "delivery" ) )
			impl.setScope( parentState , action , meta , RELEASELABEL , false , elements );
	}
	}

	private class BuildRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String SET = getArg( action , 1 );
		String[] PROJECTS = getArgList( action , 2 );

		Meta meta = action.getContextMeta();
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		impl.buildRelease( parentState , action , SET , PROJECTS , dist );
	}
	}

	private class GetRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String SET = getArg( action , 1 );
		String[] PROJECTS = getArgList( action , 2 );

		Meta meta = action.getContextMeta();
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		
		if( dist.release.isCumulative() ) {
			if( SET.isEmpty() || SET.equals( "all" ) )
				impl.getCumulativeRelease( parentState , action , dist );
			else
				action.exit0( _Error.UnexpectedCumulativeParameters0 , "unexpected parameters to settle cumulative release" );
		}
		else
			impl.getAllRelease( parentState , action , SET , PROJECTS , dist );
	}
	}

	private class DescopeRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		Meta meta = action.getContextMeta();
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		
		String SET = getRequiredArg( action , 1 , "SET" );
		if( SET.equals( "all" ) ) {
			impl.descopeAll( parentState , action , dist );
		}
		else
		if( SET.equals( Common.getEnumLower( VarCATEGORY.CONFIG ) ) ) {
			String[] COMPS = getArgList( action , 2 );
			impl.descopeConfComps( parentState , action , dist , COMPS );
		}
		else
		if( SET.equals( Common.getEnumLower( VarCATEGORY.DB ) ) ) {
			String[] ITEMS = getArgList( action , 2 );
			impl.descopeDatabase( parentState , action , dist , ITEMS );
		}
		else
		if( SET.equals( Common.getEnumLower( VarCATEGORY.MANUAL ) ) ) {
			String[] ITEMS = getArgList( action , 2 );
			impl.descopeManualItems( parentState , action , dist , ITEMS );
		}
		else {
			String PROJECT = getArg( action , 2 );
			String[] ITEMS = getArgList( action , 3 );
			impl.descopeBinary( parentState , action , dist , SET , PROJECT , ITEMS );
		}
	}
	}

	private class ExecuteTickets extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		Meta meta = action.getContextMeta();
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		String METHOD = getRequiredArg( action , 1 , "METHOD" );
		String[] args = getArgList( action , 2 );
		
		impl.executeTickets( parentState , action , dist , METHOD , args );
	}
	}

	
}
