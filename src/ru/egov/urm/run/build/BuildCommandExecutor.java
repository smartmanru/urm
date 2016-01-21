package ru.egov.urm.run.build;

import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionInit;
import ru.egov.urm.run.ActionScope;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.run.CommandAction;
import ru.egov.urm.run.CommandBuilder;
import ru.egov.urm.run.CommandExecutor;
import ru.egov.urm.storage.DistStorage;
import ru.egov.urm.storage.LocalFolder;

public class BuildCommandExecutor extends CommandExecutor {

	BuildCommand impl;

	public BuildCommandExecutor( CommandBuilder builder ) {
		super( builder );
		
		String cmdOpts;
		cmdOpts = "GETOPT_GET,GETOPT_CHECK,GETOPT_RELEASE,GETOPT_BRANCH,GETOPT_TAG,GETOPT_DATE,GETOPT_GROUP,GETOPT_VERSION";
		super.defineAction( CommandAction.newAction( new BuildAllTags() , "buildall-tags" , "build from tag" , cmdOpts , "./buildall-tags.sh [OPTIONS] <TAG> [set [projects]]" ) );
		
		cmdOpts = "GETOPT_ALL,GETOPT_GET,GETOPT_DIST,GETOPT_CHECK,GETOPT_RELEASE,GETOPT_BRANCH,GETOPT_TAG,GETOPT_DATE,GETOPT_GROUP,GETOPT_VERSION";
		super.defineAction( CommandAction.newAction( new BuildAllRelease() , "buildall-release" , "build release" , cmdOpts , "./buildall-release.sh [OPTIONS] [set [projects]]" ) );

		cmdOpts = "";
		super.defineAction( CommandAction.newAction( new CheckSet() , "checkset" , "check configuration variables" , "" , "./checkset.sh [OPTIONS]" ) );
		super.defineAction( CommandAction.newAction( new Custom() , "custom" , "run any custom operation in build scope" , cmdOpts , "./custom.sh [OPTIONS] [set [projects]]" ) );
		
		cmdOpts = "GETOPT_RELEASE,GETOPT_TAG,GETOPT_DATE,GETOPT_GROUP,GETOPT_VERSION";
		super.defineAction( CommandAction.newAction( new GetAll() , "getall" , "download build items" , cmdOpts , "./getall.sh [OPTIONS] [set [projects]]" ) );

		cmdOpts = "GETOPT_ALL,GETOPT_DIST,GETOPT_RELEASE,GETOPT_TAG,GETOPT_DATE,GETOPT_GROUP,GETOPT_VERSION";
		super.defineAction( CommandAction.newAction( new GetAllRelease() , "getall-release" , "download release build items" , cmdOpts , "./getall-release.sh [OPTIONS] [set [projects]]" ) );
		
		cmdOpts = "GETOPT_RELEASE,GETOPT_BRANCH,GETOPT_TAG,GETOPT_DATE,GETOPT_GROUP";
		super.defineAction( CommandAction.newAction( new Codebase—heckout() , "codebase-checkout" , "checkout sources to update" , cmdOpts , "./codebase-checkout.sh [OPTIONS] <CODIR> [set [projects]]" ) );
		super.defineAction( CommandAction.newAction( new Codebase—ommit() , "codebase-commit" , "commit sources after updates" , cmdOpts , "./codebase-commit.sh [OPTIONS] <CODIR> [set [projects]]" ) );
		super.defineAction( CommandAction.newAction( new CodebaseCopyBranches() , "codebase-copybranches" , "copy tag or branch to branch" , cmdOpts , "./codebase-copybranch.sh [OPTIONS] <SRCBRANCH> <DSTBRANCH> [set [projects]]" ) );
		super.defineAction( CommandAction.newAction( new CodebaseCopyBranchToTag() , "codebase-copybranchtotag" , "copy tag or branch to branch" , cmdOpts , "./codebase-copybranch.sh [OPTIONS] <SRCBRANCH> <DSTTAG> [set [projects]]" ) );
		super.defineAction( CommandAction.newAction( new CodebaseCopyNewTags() , "codebase-copynewtags" , "copy tag to tag, do not delete old tags" , cmdOpts , "./codebase-copynewtags.sh [OPTIONS] <SRCTAG> <DSTTAG> [set [projects]]" ) );
		super.defineAction( CommandAction.newAction( new CodebaseCopyTags() , "codebase-copytags" , "copy tag to tag, delete old tags" , cmdOpts , "./codebase-copytags.sh [OPTIONS] <SRCTAG> <DSTTAG> [set [projects]]" ) );
		super.defineAction( CommandAction.newAction( new CodebaseCopyTagToBranch() , "codebase-copytagtobranch" , "copy tag to new branch" , cmdOpts , "./codebase-copytagtobranch.sh [OPTIONS] <SRCTAG> <DSTBRANCH> [set [projects]]" ) );
		super.defineAction( CommandAction.newAction( new CodebaseDropBranch() , "codebase-dropbranch" , "drop branches" , cmdOpts , "./codebase-dropbranch.sh [OPTIONS] <BRANCH> [set [projects]]" ) );
		super.defineAction( CommandAction.newAction( new CodebaseDropTags() , "codebase-droptags" , "drop tags" , cmdOpts , "./codebase-droptags.sh [OPTIONS] <TAG> [set [projects]]" ) );
		super.defineAction( CommandAction.newAction( new CodebaseExport() , "codebase-export" , "codebase export" , cmdOpts , "./codebase-export.sh [OPTIONS] <CODIR> [set [projects]]" ) );
		super.defineAction( CommandAction.newAction( new CodebaseRenameBranch() , "codebase-renamebranch" , "rename branch" , cmdOpts , "./codebase-renamebranch.sh [OPTIONS] <SRCBRANCH> <DSTBRANCH> [set [projects]]" ) );
		super.defineAction( CommandAction.newAction( new CodebaseRenameTags() , "codebase-renametags" , "rename tag" , cmdOpts , "./codebase-renametags.sh [OPTIONS] <SRCTAG> <DSTTAG> [set [projects]]" ) );
		super.defineAction( CommandAction.newAction( new CodebaseSetVersion() , "codebase-setversion" , "change version in pom.xml using maven" , cmdOpts , "./codebase-setversion.sh [OPTIONS] <VERSION> [set [projects]]" ) );

		cmdOpts = "GETOPT_UPDATENEXUS, GETOPT_RELEASE, GETOPT_GROUP";
		super.defineAction( CommandAction.newAction( new ThirdpartyUpload() , "uploaddist" , "upload thirdparty binaries to nexus from release" , cmdOpts , "./uploaddist.sh [OPTIONS] <RELEASELABEL>" ) );
	}
	
	public boolean run( ActionInit action ) {
		try {
			// create implementation
			impl = new BuildCommand();
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

	private DistStorage loadCommandRelease( ActionBase action ) throws Exception {
		String RELEASELABEL = options.OPT_RELEASELABEL;
		
		if( RELEASELABEL.isEmpty() )
			RELEASELABEL = meta.product.CONFIG_RELEASEVER;
		if( RELEASELABEL.isEmpty() )
			RELEASELABEL = "next";
		
		DistStorage release = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		return( release );
	}
	
	private ActionScope getCodebaseScope( ActionBase action , int argStart ) throws Exception {
		String SET = options.getArg( argStart );
		String[] PROJECTS = options.getArgList( argStart + 1 );
		action.logAction();

		ActionScope scope = action.getFullScope( SET , PROJECTS , action.options.OPT_RELEASELABEL );
		if( scope.isEmpty( action ) )
			action.exit( "nothing to do, scope is empty" );
		
		return( scope );
	}
	
	private String getCODIR( ActionBase action , int argpos ) throws Exception {
		String CODIR = options.getRequiredArg( action , argpos , "CODIR is empty" );
		if( CODIR.startsWith( "/" ) || CODIR.startsWith( "." ) || CODIR.startsWith( "$" ) || CODIR.startsWith( "~" ) )
			return( CODIR );
		
		return( meta.product.CONFIG_BUILDPATH + "/default/" + CODIR );
	}
	
	// script interface
	private class Custom extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String SET = options.getArg( 0 );
		String[] PROJECTS = options.getArgList( 1 );
		impl.buildCustom( action , SET , PROJECTS );
	}
	}
	
	private class BuildAllRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		action.checkRequired( action.context.buildMode , "BUILDMODE" );
		String SET = options.getArg( 0 );
		String[] PROJECTS = options.getArgList( 1 );
		DistStorage release = loadCommandRelease( action );
		impl.buildRelease( action , SET , PROJECTS , release );
	}
	}
	
	private class CheckSet extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		impl.printActiveProperties( action );
	}
	}
	
	private class GetAll extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String SET = options.getArg( 0 );
		String[] TARGETS = options.getArgList( 1 );
		action.logAction();
		
		ActionScope scope = action.getFullScope( SET , TARGETS , action.options.OPT_RELEASELABEL );
		if( scope.isEmpty( action ) ) {
			action.log( "nothing to get" );
			return;
		}
		
		impl.getAll( action , scope );
	}
	}
	
	private class GetAllRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		action.checkRequired( action.context.buildMode , "BUILDMODE" );
		
		String SET = options.getArg( 0 );
		String[] TARGETS = options.getArgList( 1 );

		DistStorage release = loadCommandRelease( action );
		impl.getAllRelease( action , SET , TARGETS , release );
	}
	}
	
	// implementation
	private class BuildAllTags extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String TAG = options.getArg( 0 );
		String SET = options.getArg( 1 );
		String[] PROJECTS = options.getArgList( 2 );
		impl.buildAllTags( action , TAG , SET , PROJECTS );
	}
	}

	private class Codebase—heckout extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String CODIRNAME = getCODIR( action , 0 );
		LocalFolder CODIR = action.artefactory.getAnyFolder( action , CODIRNAME );
		ActionScope scope = getCodebaseScope( action , 1 ); 
		impl.checkout( action , scope , CODIR );
	}
	}
	
	private class Codebase—ommit extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String CODIRNAME = getCODIR( action , 0 );
		String MESSAGE = options.getRequiredArg( action , 1 , "MESSAGE" );
		ActionScope scope = getCodebaseScope( action , 2 ); 
		LocalFolder CODIR = action.artefactory.getAnyFolder( action , CODIRNAME );
		impl.commit( action , scope , CODIR , MESSAGE );
	}
	}
	
	private class CodebaseCopyBranches extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String BRANCH1 = options.getRequiredArg( action , 0 , "BRANCH1" );
		String BRANCH2 = options.getRequiredArg( action , 1 , "BRANCH2" );
		ActionScope scope = getCodebaseScope( action , 2 ); 
		impl.ÒopyBranches( action , scope , BRANCH1 , BRANCH2 );
	}
	}
	
	private class CodebaseCopyBranchToTag extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String BRANCH = options.getRequiredArg( action , 0 , "BRANCH" );
		String TAG = options.getRequiredArg( action , 1 , "TAG" );
		ActionScope scope = getCodebaseScope( action , 2 ); 
		impl.ÒopyBranchToTag( action , scope , BRANCH , TAG );
	}
	}
	
	private class CodebaseCopyNewTags extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String TAG1 = options.getRequiredArg( action , 0 , "TAG1" );
		String TAG2 = options.getRequiredArg( action , 1 , "TAG2" );
		ActionScope scope = getCodebaseScope( action , 2 ); 
		impl.copyNewTags( action , scope , TAG1 , TAG2 );
	}
	}
	
	private class CodebaseCopyTags extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String TAG1 = options.getRequiredArg( action , 0 , "TAG1" );
		String TAG2 = options.getRequiredArg( action , 1 , "TAG2" );
		ActionScope scope = getCodebaseScope( action , 2 );
		impl.copyTags( action , scope , TAG1 , TAG2 );
	}
	}
	
	private class CodebaseCopyTagToBranch extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String TAG1 = options.getRequiredArg( action , 0 , "TAG1" );
		String BRANCH2 = options.getRequiredArg( action , 1 , "BRANCH2" );
		ActionScope scope = getCodebaseScope( action , 2 ); 
		impl.copyTagToBranch( action , scope , TAG1 , BRANCH2 );
	}
	}
	
	private class CodebaseDropTags extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String TAG1 = options.getRequiredArg( action , 0 , "TAG1" );
		ActionScope scope = getCodebaseScope( action , 1 ); 
		impl.dropTags( action , scope , TAG1 );
	}
	}
	
	private class CodebaseDropBranch extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String BRANCH1 = options.getRequiredArg( action , 0 , "BRANCH1" );
		ActionScope scope = getCodebaseScope( action , 1 ); 
		impl.dropBranch( action , scope , BRANCH1 );
	}
	}
	
	private class CodebaseExport extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String CODIRNAME = getCODIR( action , 0 );
		ActionScope scope = getCodebaseScope( action , 1 ); 
		LocalFolder CODIR = action.artefactory.getAnyFolder( action , CODIRNAME );
		impl.export( action , scope , CODIR , "" );
	}
	}
	
	private class CodebaseRenameBranch extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String BRANCH1 = options.getRequiredArg( action , 0 , "BRANCH1" );
		String BRANCH2 = options.getRequiredArg( action , 1 , "BRANCH2" );
		ActionScope scope = getCodebaseScope( action , 2 ); 
		impl.renameBranch( action , scope , BRANCH1 , BRANCH2 );
	}
	}
	
	private class CodebaseRenameTags extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String TAG1 = options.getRequiredArg( action , 0 , "TAG1" );
		String TAG2 = options.getRequiredArg( action , 1 , "TAG2" );
		ActionScope scope = getCodebaseScope( action , 2 ); 
		impl.renameTags( action , scope , TAG1 , TAG2 );
	}
	}
	
	private class CodebaseSetVersion extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String VERSION = options.getRequiredArg( action , 0 , "VERSION" );
		ActionScope scope = getCodebaseScope( action , 1 );
		impl.setVersion( action , scope , VERSION );
	}
	}

	private class ThirdpartyUpload extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = options.getRequiredArg( action , 0 , "RELEASELABEL" );
		
		DistStorage release = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		
		ActionScopeTarget scopeProject = ActionScope.getReleaseProjectItemsScopeTarget( action , release , "thirdparty" , null );
		impl.thirdpartyUploadDist( action , scopeProject , release );
	}
	}
	
}
