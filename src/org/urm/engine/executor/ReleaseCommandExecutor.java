package org.urm.engine.executor;

import java.util.Date;

import org.urm.action.release.ReleaseCommand;
import org.urm.common.Common;
import org.urm.common.action.CommandMeta;
import org.urm.common.meta.ReleaseCommandMeta;
import org.urm.engine.ServerEngine;
import org.urm.engine.action.ActionInit;
import org.urm.engine.action.CommandAction;
import org.urm.engine.action.CommandExecutor;
import org.urm.engine.dist.Dist;
import org.urm.meta.engine.ServerReleaseLifecycle;
import org.urm.meta.product.Meta;
import org.urm.meta.Types.*;

public class ReleaseCommandExecutor extends CommandExecutor {

	ReleaseCommand impl;
	
	public static ReleaseCommandExecutor createExecutor( ServerEngine engine ) throws Exception {
		ReleaseCommandMeta commandInfo = new ReleaseCommandMeta();
		return( new ReleaseCommandExecutor( engine , commandInfo ) );
	}
		
	private ReleaseCommandExecutor( ServerEngine engine , CommandMeta commandInfo ) throws Exception {
		super( engine , commandInfo );
		
		defineAction( new CreateRelease() , "create" );
		defineAction( new ModifyRelease() , "modify" );
		defineAction( new PhaseRelease() , "phase" );
		defineAction( new DeleteRelease() , "drop" );
		defineAction( new StatusRelease() , "status" );
		defineAction( new CloseRelease() , "close" );
		defineAction( new CopyRelease() , "copy" );
		defineAction( new FinishRelease() , "finish" );
		defineAction( new CompleteRelease() , "complete" );
		defineAction( new ReopenRelease() , "reopen" );
		defineAction( new MaintainProd() , "prod" );
		defineAction( new ArchiveRelease() , "archive" );
		defineAction( new TouchRelease() , "touch" );
		defineAction( new AddReleaseBuildProjects() , "scope" );
		defineAction( new AddReleaseBuildItems() , "scopeitems" );
		defineAction( new AddReleaseDatabaseItems() , "scopedb" );
		defineAction( new AddReleaseConfigItems() , "scopeconf" );
		defineAction( new BuildRelease() , "build" );
		defineAction( new GetRelease() , "getdist" );
		defineAction( new DescopeRelease() , "descope" );
	}	

	@Override
	public boolean run( ActionInit action ) {
		try {
			// create implementation
			impl = new ReleaseCommand();
		}
		catch( Throwable e ) {
			action.handle( e );
			return( false );
		}
		
		// log action and run 
		boolean res = super.runMethod( action , action.commandAction );
		return( res );
	}

	private class CreateRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		Date releaseDate = getDateArg( action , 1 );
		ServerReleaseLifecycle lc = getLifecycleArg( action , 2 );
		checkNoArgs( action , 3 );
		Meta meta = action.getContextMeta();
		impl.createRelease( action , meta , RELEASELABEL , releaseDate , lc );
	}
	}

	private class ModifyRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		Date releaseDate = getDateArg( action , 1 );
		ServerReleaseLifecycle lc = getLifecycleArg( action , 2 );
		checkNoArgs( action , 3 );
		Meta meta = action.getContextMeta();
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		impl.modifyRelease( action , dist , releaseDate , lc );
	}
	}

	private class PhaseRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String CMD = getRequiredArg( action , 1 , "CMD" );
		
		Meta meta = action.getContextMeta();
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		
		if( CMD.equals( "next" ) ) {
			checkNoArgs( action , 2 );
			impl.nextPhase( action , dist );
		}
		else
		if( CMD.equals( "deadline" ) ) {
			String PHASE = getRequiredArg( action , 2 , "PHASE" );
			Date deadlineDate = getDateArg( action , 3 );
			checkNoArgs( action , 4 );
			impl.setPhaseDeadline( action , dist , PHASE , deadlineDate );
		}
		else
		if( CMD.equals( "days" ) ) {
			String PHASE = getRequiredArg( action , 2 , "PHASE" );
			int duration = getIntArg( action , 3 , 0 );
			if( duration < 0 )
				super.wrongArgs( action );
				
			checkNoArgs( action , 4 );
			impl.setPhaseDuration( action , dist , PHASE , duration );
		}
		else
			super.wrongArgs( action );
	}
	}

	private class DeleteRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		Meta meta = action.getContextMeta();
		impl.deleteRelease( action , meta , RELEASELABEL , action.isForced() );
	}
	}

	private class CloseRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		Meta meta = action.getContextMeta();
		impl.closeRelease( action , meta , RELEASELABEL );
	}
	}

	private class CopyRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASESRC = getRequiredArg( action , 0 , "RELEASESRC" );
		String RELEASEDST = getRequiredArg( action , 1 , "RELEASEDST" );
		Date releaseDate = getRequiredDateArg( action , 2 , "RELEASEDATE" );
		ServerReleaseLifecycle lc = getLifecycleArg( action , 3 );
		checkNoArgs( action , 4 );
		Meta meta = action.getContextMeta();
		impl.copyRelease( action , meta , RELEASESRC , RELEASEDST , releaseDate , lc );
	}
	}

	private class FinishRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		Meta meta = action.getContextMeta();
		impl.finishRelease( action , meta , RELEASELABEL );
	}
	}

	private class CompleteRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		Meta meta = action.getContextMeta();
		impl.completeRelease( action , meta , RELEASELABEL );
	}
	}

	private class ReopenRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		Meta meta = action.getContextMeta();
		impl.reopenRelease( action , meta , RELEASELABEL );
	}
	}

	private class MaintainProd extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String CMD = getRequiredArg( action , 0 , "CMD" );
		Meta meta = action.getContextMeta();
		if( CMD.equals( "create" ) ) {
			String RELEASEVER = getRequiredArg( action , 1 , "RELEASEVER" );
			checkNoArgs( action , 2 );
			impl.createProdInitial( action , meta , RELEASEVER );
		}
		else
		if( CMD.equals( "copy" ) ) {
			String RELEASEDIR = getRequiredArg( action , 1 , "RELEASEDIR" );
			checkNoArgs( action , 2 );
			impl.createProdCopy( action , meta , RELEASEDIR );
		}
		else
		if( CMD.equals( "status" ) ) {
			checkNoArgs( action , 1 );
			impl.prodStatus( action , meta );
		}
		else
		if( CMD.equals( "drop" ) ) {
			checkNoArgs( action , 1 );
			impl.deleteProd( action , meta );
		}
		else
			super.wrongArgs( action );
	}
	}

	private class ArchiveRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		Meta meta = action.getContextMeta();
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		impl.archiveRelease( action , dist );
	}
	}

	private class TouchRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		Meta meta = action.getContextMeta();
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		impl.touchRelease( action , meta , RELEASELABEL );
	}
	}

	private class StatusRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		Meta meta = action.getContextMeta();
		impl.statusRelease( action , meta , RELEASELABEL );
	}
	}

	private class AddReleaseBuildProjects extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String SET = getArg( action , 1 );
		String[] elements = getArgList( action , 2 );
		
		Meta meta = action.getContextMeta();
		impl.addReleaseBuildProjects( action , meta , RELEASELABEL , SET , elements );
	}
	}

	private class AddReleaseConfigItems extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String[] elements = getArgList( action , 1 );
		
		Meta meta = action.getContextMeta();
		impl.addReleaseConfigItems( action , meta , RELEASELABEL , elements );
	}
	}

	private class AddReleaseDatabaseItems extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String[] elements = getArgList( action , 1 );
		
		Meta meta = action.getContextMeta();
		impl.addReleaseDatabaseItems( action , meta , RELEASELABEL , elements );
	}
	}

	private class AddReleaseBuildItems extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String[] elements = getArgList( action , 1 );
		
		Meta meta = action.getContextMeta();
		impl.addReleaseBuildItems( action , meta , RELEASELABEL , elements );
	}
	}

	private class BuildRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String SET = getArg( action , 1 );
		String[] PROJECTS = getArgList( action , 2 );

		Meta meta = action.getContextMeta();
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		impl.buildRelease( action , SET , PROJECTS , dist );
	}
	}

	private class GetRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String SET = getArg( action , 1 );
		String[] PROJECTS = getArgList( action , 2 );

		Meta meta = action.getContextMeta();
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		
		if( dist.release.isCumulative() ) {
			if( SET.isEmpty() || SET.equals( "all" ) )
				impl.getCumulativeRelease( action , dist );
			else
				action.exit0( _Error.UnexpectedCumulativeParameters0 , "unexpected parameters to settle cumulative release" );
		}
		else
			impl.getAllRelease( action , SET , PROJECTS , dist );
	}
	}

	private class DescopeRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		Meta meta = action.getContextMeta();
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		
		String SET = getRequiredArg( action , 1 , "SET" );
		if( SET.equals( "all" ) ) {
			impl.descopeAll( action , dist );
		}
		else
		if( SET.equals( Common.getEnumLower( VarCATEGORY.CONFIG ) ) ) {
			String[] COMPS = getArgList( action , 2 );
			impl.descopeConfComps( action , dist , COMPS );
		}
		else
		if( SET.equals( Common.getEnumLower( VarCATEGORY.DB ) ) ) {
			String[] ITEMS = getArgList( action , 2 );
			impl.descopeDatabase( action , dist , ITEMS );
		}
		else
		if( SET.equals( Common.getEnumLower( VarCATEGORY.MANUAL ) ) ) {
			String[] ITEMS = getArgList( action , 2 );
			impl.descopeManualItems( action , dist , ITEMS );
		}
		else {
			String PROJECT = getArg( action , 2 );
			String[] ITEMS = getArgList( action , 3 );
			impl.descopeBinary( action , dist , SET , PROJECT , ITEMS );
		}
	}
	}
	
}
