package org.urm.server.executor;

import org.urm.common.Common;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandOptions;
import org.urm.server.CommandExecutor;
import org.urm.server.ServerEngine;
import org.urm.server.action.ActionInit;
import org.urm.server.action.CommandAction;
import org.urm.server.action.release.ReleaseCommand;
import org.urm.server.dist.Dist;
import org.urm.server.meta.Metadata.VarCATEGORY;

public class ReleaseCommandExecutor extends CommandExecutor {

	ReleaseCommand impl;
	
	public ReleaseCommandExecutor( ServerEngine engine , CommandMeta commandInfo , CommandOptions options ) throws Exception {
		super( engine , commandInfo , options );
		
		defineAction( new CreateRelease() , "create" );
		defineAction( new ModifyRelease() , "modify" );
		defineAction( new DeleteRelease() , "drop" );
		defineAction( new StatusRelease() , "status" );
		defineAction( new CloseRelease() , "close" );
		defineAction( new CopyRelease() , "copy" );
		defineAction( new FinishRelease() , "finish" );
		defineAction( new ReopenRelease() , "reopen" );
		defineAction( new MaintainProd() , "prod" );
		defineAction( new AddReleaseBuildProjects() , "scope" );
		defineAction( new AddReleaseBuildItems() , "scopeitems" );
		defineAction( new AddReleaseDatabaseItems() , "scopedb" );
		defineAction( new AddReleaseConfigItems() , "scopeconf" );
		defineAction( new BuildRelease() , "build" );
		defineAction( new GetRelease() , "getdist" );
		defineAction( new DescopeRelease() , "descope" );
	}	

	public boolean run( ActionInit action ) {
		try {
			// create implementation
			impl = new ReleaseCommand();
			action.meta.loadDistr( action );
			action.meta.loadSources( action );
		}
		catch( Throwable e ) {
			action.log( e );
			return( false );
		}
		
		// log action and run 
		boolean res = super.runMethod( action , action.commandAction );
		return( res );
	}

	private class CreateRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		impl.createRelease( action , RELEASELABEL );
	}
	}

	private class ModifyRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		Dist dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		impl.modifyRelease( action , dist );
	}
	}

	private class DeleteRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		impl.deleteRelease( action , RELEASELABEL , action.context.CTX_FORCE );
	}
	}

	private class CloseRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		impl.closeRelease( action , RELEASELABEL );
	}
	}

	private class CopyRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASESRC = getRequiredArg( action , 0 , "RELEASESRC" );
		String RELEASEDST = getRequiredArg( action , 1 , "RELEASEDST" );
		checkNoArgs( action , 2 );
		impl.copyRelease( action , RELEASESRC , RELEASEDST );
	}
	}

	private class FinishRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		impl.finishRelease( action , RELEASELABEL );
	}
	}

	private class ReopenRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		impl.reopenRelease( action , RELEASELABEL );
	}
	}

	private class MaintainProd extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String CMD = getRequiredArg( action , 0 , "CMD" );
		if( CMD.equals( "create" ) ) {
			String RELEASEVER = getRequiredArg( action , 1 , "RELEASEVER" );
			checkNoArgs( action , 2 );
			impl.createProd( action , RELEASEVER );
		}
		else
			action.exit( "wrong args" );
	}
	}

	private class StatusRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		checkNoArgs( action , 1 );
		impl.statusRelease( action , RELEASELABEL );
	}
	}

	private class AddReleaseBuildProjects extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String SET = getArg( action , 1 );
		String[] elements = getArgList( action , 2 );
		
		impl.addReleaseBuildProjects( action , RELEASELABEL , SET , elements );
	}
	}

	private class AddReleaseConfigItems extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String[] elements = getArgList( action , 1 );
		
		impl.addReleaseConfigItems( action , RELEASELABEL , elements );
	}
	}

	private class AddReleaseDatabaseItems extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String[] elements = getArgList( action , 1 );
		
		impl.addReleaseDatabaseItems( action , RELEASELABEL , elements );
	}
	}

	private class AddReleaseBuildItems extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String[] elements = getArgList( action , 1 );
		
		impl.addReleaseBuildItems( action , RELEASELABEL , elements );
	}
	}

	private class BuildRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String SET = getArg( action , 1 );
		String[] PROJECTS = getArgList( action , 2 );

		Dist dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		
		impl.buildRelease( action , SET , PROJECTS , dist );
	}
	}

	private class GetRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String SET = getArg( action , 1 );
		String[] PROJECTS = getArgList( action , 2 );

		Dist dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		
		if( dist.release.isCumulative() ) {
			if( SET.isEmpty() || SET.equals( "all" ) )
				impl.getCumulativeRelease( action , dist );
			else
				action.exit( "unexpected parameters to settle cumulative release" );
		}
		else
			impl.getAllRelease( action , SET , PROJECTS , dist );
	}
	}

	private class DescopeRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		Dist dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		
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
