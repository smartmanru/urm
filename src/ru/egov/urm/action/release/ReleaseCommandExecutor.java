package ru.egov.urm.action.release;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionInit;
import ru.egov.urm.action.CommandAction;
import ru.egov.urm.action.CommandBuilder;
import ru.egov.urm.action.CommandExecutor;
import ru.egov.urm.dist.Dist;
import ru.egov.urm.meta.Metadata.VarCATEGORY;

public class ReleaseCommandExecutor extends CommandExecutor {

	public static String NAME = "release";
	ReleaseCommand impl;
	
	public ReleaseCommandExecutor( CommandBuilder builder ) {
		super( builder , NAME );
		
		String releaseOpts = "GETOPT_BUILDMODE,GETOPT_OBSOLETE,GETOPT_COMPATIBILITY,GETOPT_CUMULATIVE";
		defineAction( CommandAction.newAction( new CreateRelease() , "create" , true , "create release" , releaseOpts , "./create.sh [OPTIONS] <RELEASELABEL>" ) );
		releaseOpts = "GETOPT_ALL,GETOPT_BUILDMODE,GETOPT_OBSOLETE,GETOPT_COMPATIBILITY";
		defineAction( CommandAction.newAction( new ModifyRelease() , "modify" , true , "set release properties" , releaseOpts , "./modify.sh [OPTIONS] <RELEASELABEL>" ) );
		releaseOpts = "";
		defineAction( CommandAction.newAction( new DeleteRelease() , "drop" , true , "delete release" , releaseOpts , "./drop.sh [OPTIONS] <RELEASELABEL>" ) );
		defineAction( CommandAction.newAction( new StatusRelease() , "status" , true , "get release status" , releaseOpts , "./status.sh [OPTIONS] <RELEASELABEL>" ) );
		defineAction( CommandAction.newAction( new CloseRelease() , "close" , true , "close release" , releaseOpts , "./close.sh [OPTIONS] <RELEASELABEL>" ) );
		defineAction( CommandAction.newAction( new CopyRelease() , "copy" , true , "copy release" , releaseOpts , "./close.sh [OPTIONS] <RELEASESRC> <RELEASEDST>" ) );
		defineAction( CommandAction.newAction( new FinishRelease() , "finish" , true , "finish release" , releaseOpts , "./finish.sh [OPTIONS] <RELEASELABEL>" ) );
		defineAction( CommandAction.newAction( new ReopenRelease() , "reopen" , true , "reopen release" , releaseOpts , "./reopen.sh [OPTIONS] <RELEASELABEL>" ) );
		defineAction( CommandAction.newAction( new MaintainProd() , "prod" , true , "create master distributive from predefined set" , releaseOpts , "./prod.sh [OPTIONS] create <initial version>" ) );
		String addOpts = "GETOPT_BRANCH,GETOPT_TAG,GETOPT_VERSION,GETOPT_REPLACE";
		defineAction( CommandAction.newAction( new AddReleaseBuildProjects() , "scope" , true , "add projects to build (except for prebuilt) and use all its binary items" , addOpts , "./scope.sh [OPTIONS] <RELEASELABEL> <set> [target1 target2 ...]" ) );
		defineAction( CommandAction.newAction( new AddReleaseBuildItems() , "scopeitems" , true , "add specified binary items to built (if not prebuilt) and get" , addOpts , "./scopeitems.sh [OPTIONS] <RELEASELABEL> item1 [item2 ...]" ) );
		String addDbOpts = "GETOPT_ALL";
		defineAction( CommandAction.newAction( new AddReleaseDatabaseItems() , "scopedb" , true , "add database changes to release deliveries" , addDbOpts , "./scopedb.sh [OPTIONS] <RELEASELABEL> delivery1 [delivery2 ...]" ) );
		String addConfOpts = "GETOPT_REPLACE";
		defineAction( CommandAction.newAction( new AddReleaseConfigItems() , "scopeconf" , true , "add configuration items to release" , addConfOpts , "./scopeconf.sh [OPTIONS] <RELEASELABEL> [component1 component2 ...]" ) );
		String buildReleaseOpts = "GETOPT_DIST,GETOPT_CHECK";
		defineAction( CommandAction.newAction( new BuildRelease() , "build" , true , "build release and (with -get) " , buildReleaseOpts , "./build.sh [OPTIONS] <RELEASELABEL> [set [projects]]" ) );
		String getReleaseOpts = "GETOPT_DIST,GETOPT_MOVE_ERRORS";
		defineAction( CommandAction.newAction( new GetRelease() , "getdist" , true , "download ready and/or built release items" , getReleaseOpts , "./getdist.sh [OPTIONS] <RELEASELABEL> [set [projects]]" ) );
		String getDescopeOpts = "";
		defineAction( CommandAction.newAction( new DescopeRelease() , "descope" , true , "descope release elements" , getDescopeOpts , "./descope.sh [OPTIONS] <RELEASELABEL> set [project [project items]|configuration components|database deliveries]" ) );
	}	

	public boolean run( ActionInit action ) {
		try {
			// create implementation
			impl = new ReleaseCommand();
			meta.loadDistr( action );
			meta.loadSources( action );
		}
		catch( Throwable e ) {
			action.log( e );
			return( false );
		}
		
		// log action and run 
		boolean res = super.runMethod( action , commandAction );
		return( res );
	}

	private class CreateRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = options.getRequiredArg( action , 0 , "RELEASELABEL" );
		options.checkNoArgs( action , 1 );
		impl.createRelease( action , RELEASELABEL );
	}
	}

	private class ModifyRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = options.getRequiredArg( action , 0 , "RELEASELABEL" );
		options.checkNoArgs( action , 1 );
		Dist release = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		impl.modifyRelease( action , release );
	}
	}

	private class DeleteRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = options.getRequiredArg( action , 0 , "RELEASELABEL" );
		options.checkNoArgs( action , 1 );
		impl.deleteRelease( action , RELEASELABEL , action.context.CTX_FORCE );
	}
	}

	private class CloseRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = options.getRequiredArg( action , 0 , "RELEASELABEL" );
		options.checkNoArgs( action , 1 );
		impl.closeRelease( action , RELEASELABEL );
	}
	}

	private class CopyRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASESRC = options.getRequiredArg( action , 0 , "RELEASESRC" );
		String RELEASEDST = options.getRequiredArg( action , 1 , "RELEASEDST" );
		options.checkNoArgs( action , 2 );
		impl.copyRelease( action , RELEASESRC , RELEASEDST );
	}
	}

	private class FinishRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = options.getRequiredArg( action , 0 , "RELEASELABEL" );
		options.checkNoArgs( action , 1 );
		impl.finishRelease( action , RELEASELABEL );
	}
	}

	private class ReopenRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = options.getRequiredArg( action , 0 , "RELEASELABEL" );
		options.checkNoArgs( action , 1 );
		impl.reopenRelease( action , RELEASELABEL );
	}
	}

	private class MaintainProd extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String CMD = options.getRequiredArg( action , 0 , "CMD" );
		if( CMD.equals( "create" ) ) {
			String RELEASEVER = options.getRequiredArg( action , 1 , "RELEASEVER" );
			options.checkNoArgs( action , 2 );
			impl.createProd( action , RELEASEVER );
		}
		else
			action.exit( "wrong args" );
	}
	}

	private class StatusRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = options.getRequiredArg( action , 0 , "RELEASELABEL" );
		options.checkNoArgs( action , 1 );
		impl.statusRelease( action , RELEASELABEL );
	}
	}

	private class AddReleaseBuildProjects extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = options.getRequiredArg( action , 0 , "RELEASELABEL" );
		String SET = options.getArg( 1 );
		String[] elements = options.getArgList( 2 );
		
		impl.addReleaseBuildProjects( action , RELEASELABEL , SET , elements );
	}
	}

	private class AddReleaseConfigItems extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = options.getRequiredArg( action , 0 , "RELEASELABEL" );
		String[] elements = options.getArgList( 1 );
		
		impl.addReleaseConfigItems( action , RELEASELABEL , elements );
	}
	}

	private class AddReleaseDatabaseItems extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = options.getRequiredArg( action , 0 , "RELEASELABEL" );
		String[] elements = options.getArgList( 1 );
		
		impl.addReleaseDatabaseItems( action , RELEASELABEL , elements );
	}
	}

	private class AddReleaseBuildItems extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = options.getRequiredArg( action , 0 , "RELEASELABEL" );
		String[] elements = options.getArgList( 1 );
		
		impl.addReleaseBuildItems( action , RELEASELABEL , elements );
	}
	}

	private class BuildRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = options.getRequiredArg( action , 0 , "RELEASELABEL" );
		String SET = options.getArg( 1 );
		String[] PROJECTS = options.getArgList( 2 );

		Dist release = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		
		impl.buildRelease( action , SET , PROJECTS , release );
	}
	}

	private class GetRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = options.getRequiredArg( action , 0 , "RELEASELABEL" );
		String SET = options.getArg( 1 );
		String[] PROJECTS = options.getArgList( 2 );

		Dist release = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		
		if( action.context.CTX_CUMULATIVE ) {
			if( SET.isEmpty() || SET.equals( "all" ) )
				impl.getCumulativeRelease( action , release );
			else
				action.exit( "unexpected parameters to settle cumulative release" );
		}
		else
			impl.getAllRelease( action , SET , PROJECTS , release );
	}
	}

	private class DescopeRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = options.getRequiredArg( action , 0 , "RELEASELABEL" );
		Dist release = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		
		String SET = options.getRequiredArg( action , 1 , "SET" );
		if( SET.equals( "all" ) ) {
			impl.descopeAll( action , release );
		}
		else
		if( SET.equals( Common.getEnumLower( VarCATEGORY.CONFIG ) ) ) {
			String[] COMPS = options.getArgList( 2 );
			impl.descopeConfComps( action , release , COMPS );
		}
		else
		if( SET.equals( Common.getEnumLower( VarCATEGORY.DB ) ) ) {
			String[] ITEMS = options.getArgList( 2 );
			impl.descopeDatabase( action , release , ITEMS );
		}
		else
		if( SET.equals( Common.getEnumLower( VarCATEGORY.MANUAL ) ) ) {
			String[] ITEMS = options.getArgList( 2 );
			impl.descopeManualItems( action , release , ITEMS );
		}
		else {
			String PROJECT = options.getArg( 2 );
			String[] ITEMS = options.getArgList( 3 );
			impl.descopeBinary( action , release , SET , PROJECT , ITEMS );
		}
	}
	}
	
}
