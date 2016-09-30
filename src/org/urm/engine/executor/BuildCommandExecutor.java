package org.urm.engine.executor;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.action.ActionScopeTarget;
import org.urm.action.build.BuildCommand;
import org.urm.common.action.CommandMeta;
import org.urm.engine.ServerEngine;
import org.urm.engine.action.ActionInit;
import org.urm.engine.action.CommandAction;
import org.urm.engine.action.CommandExecutor;
import org.urm.engine.dist.Dist;
import org.urm.engine.meta.Meta;
import org.urm.engine.meta.MetaProductBuildSettings;
import org.urm.engine.storage.LocalFolder;

public class BuildCommandExecutor extends CommandExecutor {

	BuildCommand impl;
	
	public BuildCommandExecutor( ServerEngine engine , CommandMeta commandInfo ) throws Exception {
		super( engine , commandInfo );
		
		super.defineAction( new BuildAllTags() , "buildall-tags" );
		super.defineAction( new BuildAllRelease() , "buildall-release" );
		super.defineAction( new CheckSet() , "checkset" );
		super.defineAction( new Custom() , "custom" );
		super.defineAction( new GetAll() , "getall" );
		super.defineAction( new GetAllRelease() , "getall-release" );
		super.defineAction( new CodebaseCheckout() , "codebase-checkout" );
		super.defineAction( new CodebaseCommit() , "codebase-commit" );
		super.defineAction( new CodebaseCopyBranches() , "codebase-copybranches" );
		super.defineAction( new CodebaseCopyBranchToTag() , "codebase-copybranchtotag" );
		super.defineAction( new CodebaseCopyNewTags() , "codebase-copynewtags" );
		super.defineAction( new CodebaseCopyTags() , "codebase-copytags" );
		super.defineAction( new CodebaseCopyTagToBranch() , "codebase-copytagtobranch" );
		super.defineAction( new CodebaseDropBranch() , "codebase-dropbranch" );
		super.defineAction( new CodebaseDropTags() , "codebase-droptags" );
		super.defineAction( new CodebaseExport() , "codebase-export" );
		super.defineAction( new CodebaseRenameBranch() , "codebase-renamebranch" );
		super.defineAction( new CodebaseRenameTags() , "codebase-renametags" );
		super.defineAction( new CodebaseSetVersion() , "codebase-setversion" );
		super.defineAction( new ThirdpartyUploadDist() , "uploaddist" );
		super.defineAction( new ThirdpartyUploadLib() , "uploadlib" );
	}
	
	public boolean run( ActionInit action ) {
		try {
			// create implementation
			impl = new BuildCommand();
		}
		catch( Throwable e ) {
			action.handle( e );
			return( false );
		}
		
		// log action and run 
		boolean res = super.runMethod( action , action.commandAction );
		return( res );
	}

	private Dist loadCommandRelease( ActionBase action ) throws Exception {
		String RELEASELABEL = action.context.CTX_RELEASELABEL;
		Meta meta = action.getContextMeta();
		
		if( RELEASELABEL.isEmpty() ) {
			MetaProductBuildSettings build = action.getBuildSettings( meta );
			RELEASELABEL = build.CONFIG_RELEASE_VERSION;
		}
		if( RELEASELABEL.isEmpty() )
			RELEASELABEL = "next";
		
		Dist release = action.artefactory.getDistStorageByLabel( action , meta , RELEASELABEL );
		return( release );
	}
	
	private ActionScope getCodebaseScope( ActionBase action , int argStart ) throws Exception {
		String SET = getArg( action , argStart );
		String[] PROJECTS = getArgList( action , argStart + 1 );
		action.logAction();

		ActionScope scope;
		Dist dist = null;
		String RELEASELABEL = action.context.CTX_RELEASELABEL;
		Meta meta = action.getContextMeta();
		if( !RELEASELABEL.isEmpty() ) {
			dist = action.artefactory.getDistStorageByLabel( action , meta , RELEASELABEL );
			scope = ActionScope.getReleaseSetScope( action , dist , SET , PROJECTS );
		}
		else
			scope = ActionScope.getProductSetScope( action , meta , SET , PROJECTS );
		
		if( scope.isEmpty( action ) )
			action.exit0( _Error.ScopeEmpty0 , "nothing to do, scope is empty" );
		
		return( scope );
	}
	
	private String getCODIR( ActionBase action , int argpos ) throws Exception {
		String CODIR = getRequiredArg( action , argpos , "CODIR is empty" );
		if( CODIR.startsWith( "/" ) || CODIR.startsWith( "." ) || CODIR.startsWith( "$" ) || CODIR.startsWith( "~" ) )
			return( CODIR );
		
		return( action.artefactory.getWorkPath( action , "default/" + CODIR ) );
	}
	
	// script interface
	private class Custom extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String SET = getArg( action , 0 );
		String[] PROJECTS = getArgList( action , 1 );
		Meta meta = action.getContextMeta();
		impl.buildCustom( action , meta , SET , PROJECTS );
	}
	}
	
	private class BuildAllRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		action.checkRequired( action.context.buildMode , "BUILDMODE" );
		String SET = getArg( action , 0 );
		String[] PROJECTS = getArgList( action , 1 );
		Dist release = loadCommandRelease( action );
		Meta meta = action.getContextMeta();
		impl.buildRelease( action , meta , SET , PROJECTS , release );
	}
	}
	
	private class CheckSet extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		Meta meta = action.getContextMeta();
		impl.printActiveProperties( action , meta );
	}
	}
	
	private class GetAll extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String SET = getArg( action , 0 );
		String[] TARGETS = getArgList( action , 1 );
		action.logAction();
		
		ActionScope scope;
		Dist dist = null;
		String RELEASELABEL = action.context.CTX_RELEASELABEL;
		Meta meta = action.getContextMeta();
		if( !RELEASELABEL.isEmpty() ) {
			dist = action.artefactory.getDistStorageByLabel( action , meta , RELEASELABEL );
			scope = ActionScope.getReleaseSetScope( action , dist , SET , TARGETS );
		}
		else
			scope = ActionScope.getProductSetScope( action , meta , SET , TARGETS );
		
		if( scope.isEmpty( action ) ) {
			action.info( "nothing to get" );
			return;
		}
		
		impl.getAll( action , scope , dist );
	}
	}
	
	private class GetAllRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		action.checkRequired( action.context.buildMode , "BUILDMODE" );
		
		String SET = getArg( action , 0 );
		String[] TARGETS = getArgList( action , 1 );

		Dist dist = loadCommandRelease( action );
		impl.getAllRelease( action , SET , TARGETS , dist );
	}
	}
	
	// implementation
	private class BuildAllTags extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String TAG = getArg( action , 0 );
		String SET = getArg( action , 1 );
		String[] PROJECTS = getArgList( action , 2 );
		Meta meta = action.getContextMeta();
		impl.buildAllTags( action , meta , TAG , SET , PROJECTS , null );
	}
	}

	private class CodebaseCheckout extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String CODIRNAME = getCODIR( action , 0 );
		LocalFolder CODIR = action.artefactory.getAnyFolder( action , CODIRNAME );
		ActionScope scope = getCodebaseScope( action , 1 ); 
		impl.checkout( action , scope , CODIR );
	}
	}
	
	private class CodebaseCommit extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String CODIRNAME = getCODIR( action , 0 );
		String MESSAGE = getRequiredArg( action , 1 , "MESSAGE" );
		ActionScope scope = getCodebaseScope( action , 2 ); 
		LocalFolder CODIR = action.artefactory.getAnyFolder( action , CODIRNAME );
		impl.commit( action , scope , CODIR , MESSAGE );
	}
	}
	
	private class CodebaseCopyBranches extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String BRANCH1 = getRequiredArg( action , 0 , "BRANCH1" );
		String BRANCH2 = getRequiredArg( action , 1 , "BRANCH2" );
		ActionScope scope = getCodebaseScope( action , 2 ); 
		impl.copyBranches( action , scope , BRANCH1 , BRANCH2 );
	}
	}
	
	private class CodebaseCopyBranchToTag extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String BRANCH = getRequiredArg( action , 0 , "BRANCH" );
		String TAG = getRequiredArg( action , 1 , "TAG" );
		ActionScope scope = getCodebaseScope( action , 2 ); 
		impl.copyBranchToag( action , scope , BRANCH , TAG );
	}
	}
	
	private class CodebaseCopyNewTags extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String TAG1 = getRequiredArg( action , 0 , "TAG1" );
		String TAG2 = getRequiredArg( action , 1 , "TAG2" );
		ActionScope scope = getCodebaseScope( action , 2 ); 
		impl.copyNewTags( action , scope , TAG1 , TAG2 );
	}
	}
	
	private class CodebaseCopyTags extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String TAG1 = getRequiredArg( action , 0 , "TAG1" );
		String TAG2 = getRequiredArg( action , 1 , "TAG2" );
		ActionScope scope = getCodebaseScope( action , 2 );
		impl.copyTags( action , scope , TAG1 , TAG2 );
	}
	}
	
	private class CodebaseCopyTagToBranch extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String TAG1 = getRequiredArg( action , 0 , "TAG1" );
		String BRANCH2 = getRequiredArg( action , 1 , "BRANCH2" );
		ActionScope scope = getCodebaseScope( action , 2 ); 
		impl.copyTagToBranch( action , scope , TAG1 , BRANCH2 );
	}
	}
	
	private class CodebaseDropTags extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String TAG1 = getRequiredArg( action , 0 , "TAG1" );
		ActionScope scope = getCodebaseScope( action , 1 ); 
		impl.dropTags( action , scope , TAG1 );
	}
	}
	
	private class CodebaseDropBranch extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String BRANCH1 = getRequiredArg( action , 0 , "BRANCH1" );
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
		String BRANCH1 = getRequiredArg( action , 0 , "BRANCH1" );
		String BRANCH2 = getRequiredArg( action , 1 , "BRANCH2" );
		ActionScope scope = getCodebaseScope( action , 2 ); 
		impl.renameBranch( action , scope , BRANCH1 , BRANCH2 );
	}
	}
	
	private class CodebaseRenameTags extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String TAG1 = getRequiredArg( action , 0 , "TAG1" );
		String TAG2 = getRequiredArg( action , 1 , "TAG2" );
		ActionScope scope = getCodebaseScope( action , 2 ); 
		impl.renameTags( action , scope , TAG1 , TAG2 );
	}
	}
	
	private class CodebaseSetVersion extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String VERSION = getRequiredArg( action , 0 , "VERSION" );
		ActionScope scope = getCodebaseScope( action , 1 );
		impl.setVersion( action , scope , VERSION );
	}
	}

	private class ThirdpartyUploadDist extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		
		Meta meta = action.getContextMeta();
		Dist release = action.artefactory.getDistStorageByLabel( action , meta , RELEASELABEL );
		
		ActionScopeTarget scopeProject = ActionScope.getReleaseProjectItemsScopeTarget( action , release , "thirdparty" , null );
		impl.thirdpartyUploadDist( action , scopeProject , release );
	}
	}

	private class ThirdpartyUploadLib extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String GROUPID = getRequiredArg( action , 0 , "GROUPID" );
		String FILE = getRequiredArg( action , 1 , "FILE" );
		String ARTEFACTID = getArg( action , 2 );
		String VERSION = getArg( action , 3 );
		String CLASSIFIER = getArg( action , 4 );
		
		Meta meta = action.getContextMeta();
		impl.thirdpartyUploadLib( action , meta , GROUPID , FILE , ARTEFACTID , VERSION , CLASSIFIER );
	}
	}
	
}
