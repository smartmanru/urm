package ru.egov.urm.run.release;

import ru.egov.urm.Common;
import ru.egov.urm.meta.Metadata.VarBUILDMODE;
import ru.egov.urm.meta.Metadata.VarCATEGORY;
import ru.egov.urm.run.ActionInit;
import ru.egov.urm.run.CommandAction;
import ru.egov.urm.run.CommandBuilder;
import ru.egov.urm.run.CommandExecutor;
import ru.egov.urm.run.CommandOptions;
import ru.egov.urm.storage.DistStorage;

public class ReleaseCommandExecutor extends CommandExecutor {

	ReleaseCommand impl;
	String RELEASELABEL;
	String[] ARGS;
	
	public ReleaseCommandExecutor( CommandBuilder builder ) {
		super( builder );
		
		String releaseOpts = "";
		super.manualActions = true;
		defineAction( new CreateRelease() , "create" , "create release" , releaseOpts , "{branch|trunk|majorbranch|devbranch|devtrunk}" );
		defineAction( new DeleteRelease() , "drop" , "delete release" , releaseOpts , "" );
		defineAction( new StatusRelease() , "status" , "get release status" , releaseOpts , "" );
		defineAction( new CloseRelease() , "close" , "close release" , releaseOpts , "" );
		String addOpts = "GETOPT_BRANCH,GETOPT_TAG,GETOPT_VERSION,GETOPT_REPLACE";
		defineAction( new AddReleaseBuildProjects() , "add" , "add projects to build (except for prebuilt) and use all its binary items" , addOpts , "set [target1 target2 ...]" );
		defineAction( new AddReleaseBuildItems() , "additems" , "add specified binary items to built (if not prebuilt) and get" , addOpts , "item1 [item2 ...]" );
		String addDbOpts = "GETOPT_ALL";
		defineAction( new AddReleaseDatabaseItems() , "adddb" , "add database changes to release deliveries" , addDbOpts , "delivery1 [delivery2 ...]" );
		String addConfOpts = "GETOPT_REPLACE";
		defineAction( new AddReleaseConfigItems() , "addconf" , "add configuration items to release" , addConfOpts , "[component1 component2 ...]" );
		String buildReleaseOpts = "GETOPT_DIST,GETOPT_CHECK";
		defineAction( new BuildRelease() , "build" , "build release and (with -get) " , buildReleaseOpts , "[set [projects]]" );
		String getReleaseOpts = "GETOPT_DIST,GETOPT_MOVE_ERRORS";
		defineAction( new GetRelease() , "get" , "download ready and/or built release items" , getReleaseOpts , "[set [projects]]" );
		String getDescopeOpts = "";
		defineAction( new DescopeRelease() , "descope" , "descope release elements" , getDescopeOpts , "set [project [project items]|configuration components|database deliveries]" );
	}	

	private void defineAction( CommandAction method , String action , String function , String opts , String syntaxArgs ) {
		super.defineAction( CommandAction.newAction( method , action , function , opts , "./release.sh [OPTIONS] " + action + " <RELEASELABEL> " + syntaxArgs ) );
	}
	
	@Override public boolean setManualOptions( CommandOptions options ) {
		String action = options.getArg( 0 );
		RELEASELABEL = options.getArg( 1 );
		if( !super.setAction( action , RELEASELABEL ) )
			return( false );
		
		if( RELEASELABEL.isEmpty() ) {
			super.print( "Missing RELEASELABEL. Run release.sh to see help." );
			return( false );
		}
		
		return( true );
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
		VarBUILDMODE BUILDMODE = options.getRequiredBuildModeArg( action , 2 );
		options.checkNoArgs( action , 3 );
		impl.createRelease( action , RELEASELABEL , BUILDMODE );
	}
	}

	private class DeleteRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		options.checkNoArgs( action , 2 );
		impl.deleteRelease( action , RELEASELABEL , options.OPT_FORCE );
	}
	}

	private class CloseRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		options.checkNoArgs( action , 2 );
		impl.closeRelease( action , RELEASELABEL );
	}
	}

	private class StatusRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		options.checkNoArgs( action , 2 );
		impl.statusRelease( action , RELEASELABEL );
	}
	}

	private class AddReleaseBuildProjects extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String SET = options.getArg( 2 );
		String[] elements = options.getArgList( 3 );
		
		impl.addReleaseBuildProjects( action , RELEASELABEL , SET , elements );
	}
	}

	private class AddReleaseConfigItems extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String[] elements = options.getArgList( 2 );
		
		impl.addReleaseConfigItems( action , RELEASELABEL , elements );
	}
	}

	private class AddReleaseDatabaseItems extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String[] elements = options.getArgList( 2 );
		
		impl.addReleaseDatabaseItems( action , RELEASELABEL , elements );
	}
	}

	private class AddReleaseBuildItems extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String[] elements = options.getArgList( 2 );
		
		impl.addReleaseBuildItems( action , RELEASELABEL , elements );
	}
	}

	private class BuildRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String SET = options.getArg( 2 );
		String[] PROJECTS = options.getArgList( 3 );

		DistStorage release = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		
		impl.buildRelease( action , SET , PROJECTS , release );
	}
	}

	private class GetRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String SET = options.getArg( 2 );
		String[] PROJECTS = options.getArgList( 3 );

		DistStorage release = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		
		impl.getAllRelease( action , SET , PROJECTS , release );
	}
	}

	private class DescopeRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		DistStorage release = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		
		String SET = options.getRequiredArg( action , 2 , "SET" );
		if( SET.equals( "all" ) ) {
			impl.descopeAll( action , release );
		}
		else
		if( SET.equals( Common.getEnumLower( VarCATEGORY.CONFIG ) ) ) {
			String[] COMPS = options.getArgList( 3 );
			impl.descopeConfComps( action , release , COMPS );
		}
		else
		if( SET.equals( Common.getEnumLower( VarCATEGORY.DB ) ) ) {
			String[] ITEMS = options.getArgList( 3 );
			impl.descopeDatabase( action , release , ITEMS );
		}
		else
		if( SET.equals( Common.getEnumLower( VarCATEGORY.MANUAL ) ) ) {
			String[] ITEMS = options.getArgList( 3 );
			impl.descopeManualItems( action , release , ITEMS );
		}
		else {
			String PROJECT = options.getArg( 3 );
			String[] ITEMS = options.getArgList( 4 );
			impl.descopeBinary( action , release , SET , PROJECT , ITEMS );
		}
	}
	}
	
}
