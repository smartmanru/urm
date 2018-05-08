package org.urm.engine.executor;

import org.urm.action.ActionBase;
import org.urm.action.ActionProductScopeMaker;
import org.urm.action.ActionReleaseScopeMaker;
import org.urm.action.ActionScope;
import org.urm.action.ActionScopeTarget;
import org.urm.action.codebase.CodebaseCommand;
import org.urm.common.action.CommandMeta;
import org.urm.common.meta.CodebaseCommandMeta;
import org.urm.engine.Engine;
import org.urm.engine.action.CommandMethod;
import org.urm.engine.action.CommandExecutor;
import org.urm.engine.dist.Dist;
import org.urm.engine.products.EngineProductReleases;
import org.urm.engine.status.ScopeState;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.product.Meta;
import org.urm.meta.release.Release;

public class CommandExecutorCodebase extends CommandExecutor {

	public static CommandExecutorCodebase createExecutor( Engine engine ) throws Exception {
		CodebaseCommandMeta commandInfo = new CodebaseCommandMeta( engine.optionsMeta );
		return( new CommandExecutorCodebase( engine , commandInfo ) );
	}
		
	private CommandExecutorCodebase( Engine engine , CommandMeta commandInfo ) throws Exception {
		super( engine , commandInfo );
		
		super.defineAction( new BuildAllTags() , CodebaseCommandMeta.METHOD_BUILDTAGS );
		super.defineAction( new BuildAllRelease() , CodebaseCommandMeta.METHOD_BUILDRELEASE );
		super.defineAction( new CheckSet() , CodebaseCommandMeta.METHOD_CHECKSET );
		super.defineAction( new Custom() , CodebaseCommandMeta.METHOD_CUSTOM );
		super.defineAction( new GetAll() , CodebaseCommandMeta.METHOD_GETBUILD );
		super.defineAction( new GetAllRelease() , CodebaseCommandMeta.METHOD_GETRELEASE );
		super.defineAction( new CodebaseCheckout() , CodebaseCommandMeta.METHOD_CHECKOUT );
		super.defineAction( new CodebaseCommit() , CodebaseCommandMeta.METHOD_COMMIT );
		super.defineAction( new CodebaseCopyBranches() , CodebaseCommandMeta.METHOD_COPYBRANCHES );
		super.defineAction( new CodebaseCopyBranchToTag() , CodebaseCommandMeta.METHOD_COPYBRANCHTOTAG );
		super.defineAction( new CodebaseCopyNewTags() , CodebaseCommandMeta.METHOD_COPYNEWTAGS );
		super.defineAction( new CodebaseCopyTags() , CodebaseCommandMeta.METHOD_COPYTAGS );
		super.defineAction( new CodebaseCopyTagToBranch() , CodebaseCommandMeta.METHOD_COPYTAGTOBRANCH );
		super.defineAction( new CodebaseDropBranch() , CodebaseCommandMeta.METHOD_DROPBRANCH );
		super.defineAction( new CodebaseDropTags() , CodebaseCommandMeta.METHOD_DROPTAGS );
		super.defineAction( new CodebaseExport() , CodebaseCommandMeta.METHOD_EXPORT );
		super.defineAction( new CodebaseList() , CodebaseCommandMeta.METHOD_LIST );
		super.defineAction( new CodebaseRenameBranch() , CodebaseCommandMeta.METHOD_RENAMEBRANCH );
		super.defineAction( new CodebaseRenameTags() , CodebaseCommandMeta.METHOD_RENAMETAGS );
		super.defineAction( new CodebaseSetVersion() , CodebaseCommandMeta.METHOD_SETVERSION );
		super.defineAction( new ThirdpartyUploadDist() , CodebaseCommandMeta.METHOD_UPLOADDIST );
		super.defineAction( new ThirdpartyUploadLib() , CodebaseCommandMeta.METHOD_UPLOADLIB );
	}

	@Override
	public boolean runExecutorImpl( ScopeState parentState , ActionBase action , CommandMethod method ) {
		boolean res = super.runMethod( parentState , action , method );
		return( res );
	}

	private ActionScope getCodebaseScope( ActionBase action , int argStart ) throws Exception {
		String SET = getArg( action , argStart );
		String[] PROJECTS = getArgList( action , argStart + 1 );
		action.logAction();

		ActionScope scope;
		String RELEASELABEL = action.context.CTX_RELEASELABEL;
		
		if( !RELEASELABEL.isEmpty() ) {
			Release release = super.getReleaseByLabel( action , RELEASELABEL );
			ActionReleaseScopeMaker maker = new ActionReleaseScopeMaker( action , release );
			maker.addScopeReleaseSet( SET , PROJECTS );
			scope = maker.getScope();
		}
		else {
			Meta meta = action.getContextMeta();
			ActionProductScopeMaker maker = new ActionProductScopeMaker( action , meta );
			maker.addScopeProductSet( SET , PROJECTS );
			scope = maker.getScope();
		}
		
		if( scope.isEmpty() )
			action.exit0( _Error.ScopeEmpty0 , "nothing to do, scope is empty" );
		
		return( scope );
	}

	private ActionScope getCodebaseTargetsScope( ActionBase action , String SET , String[] TARGETS ) throws Exception {
		ActionScope scope;
		String RELEASELABEL = action.context.CTX_RELEASELABEL;
		if( !RELEASELABEL.isEmpty() ) {
			Release release = super.getReleaseByLabel( action , RELEASELABEL );
			ActionReleaseScopeMaker maker = new ActionReleaseScopeMaker( action , release );
			maker.addScopeReleaseSet( SET , TARGETS );
			scope = maker.getScope();
		}
		else {
			Meta meta = action.getContextMeta();
			ActionProductScopeMaker maker = new ActionProductScopeMaker( action , meta );
			maker.addScopeProductSet( SET , TARGETS );
			scope = maker.getScope();
		}
		
		if( scope.isEmpty() )
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
	private class Custom extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String SET = getArg( action , 0 );
		String[] PROJECTS = getArgList( action , 1 );
		Meta meta = action.getContextMeta();
		CodebaseCommand.buildCustom( parentState , action , meta , SET , PROJECTS );
	}
	}
	
	private class BuildAllRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		action.checkRequired( action.context.buildMode , "BUILDMODE" );
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String SET = getArg( action , 1 );
		String[] PROJECTS = getArgList( action , 2 );
		
		Release release = super.getRelease( action, RELEASELABEL);
		CodebaseCommand.buildRelease( parentState , action , release , SET , PROJECTS );
	}
	}
	
	private class CheckSet extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		Meta meta = action.getContextMeta();
		CodebaseCommand.printActiveProperties( parentState , action , meta );
	}
	}
	
	private class GetAll extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String SET = getArg( action , 0 );
		String[] TARGETS = getArgList( action , 1 );
		action.logAction();
		
		ActionScope scope = getCodebaseTargetsScope( action , SET , TARGETS );
		Meta meta = scope.release.getMeta();
		AppProduct product = meta.getProduct();
		EngineProductReleases releases = product.findReleases();
		Dist dist = releases.findDefaultReleaseDist( scope.release );
		CodebaseCommand.getAll( parentState , action , scope , dist );
	}
	}
	
	private class GetAllRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		action.checkRequired( action.context.buildMode , "BUILDMODE" );
		
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		String SET = getArg( action , 1 );
		String[] TARGETS = getArgList( action , 2 );

		Release release = super.getRelease( action , RELEASELABEL );
		CodebaseCommand.getAllRelease( parentState , action , release , SET , TARGETS );
	}
	}
	
	// implementation
	private class BuildAllTags extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String TAG = getArg( action , 0 );
		String SET = getArg( action , 1 );
		String[] PROJECTS = getArgList( action , 2 );
		Meta meta = action.getContextMeta();
		CodebaseCommand.buildAllTags( parentState , action , meta , TAG , SET , PROJECTS , null );
	}
	}

	private class CodebaseCheckout extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String CODIRNAME = getCODIR( action , 0 );
		LocalFolder CODIR = action.artefactory.getAnyFolder( action , CODIRNAME );
		ActionScope scope = getCodebaseScope( action , 1 ); 
		CodebaseCommand.checkout( parentState , action , scope , CODIR );
	}
	}
	
	private class CodebaseCommit extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String CODIRNAME = getCODIR( action , 0 );
		String MESSAGE = getRequiredArg( action , 1 , "MESSAGE" );
		ActionScope scope = getCodebaseScope( action , 2 ); 
		LocalFolder CODIR = action.artefactory.getAnyFolder( action , CODIRNAME );
		CodebaseCommand.commit( parentState , action , scope , CODIR , MESSAGE );
	}
	}
	
	private class CodebaseCopyBranches extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String BRANCH1 = getRequiredArg( action , 0 , "BRANCH1" );
		String BRANCH2 = getRequiredArg( action , 1 , "BRANCH2" );
		ActionScope scope = getCodebaseScope( action , 2 ); 
		CodebaseCommand.copyBranches( parentState , action , scope , BRANCH1 , BRANCH2 );
	}
	}
	
	private class CodebaseCopyBranchToTag extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String BRANCH = getRequiredArg( action , 0 , "BRANCH" );
		String TAG = getRequiredArg( action , 1 , "TAG" );
		ActionScope scope = getCodebaseScope( action , 2 ); 
		CodebaseCommand.copyBranchTag( parentState , action , scope , BRANCH , TAG );
	}
	}
	
	private class CodebaseCopyNewTags extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String TAG1 = getRequiredArg( action , 0 , "TAG1" );
		String TAG2 = getRequiredArg( action , 1 , "TAG2" );
		ActionScope scope = getCodebaseScope( action , 2 ); 
		CodebaseCommand.copyNewTags( parentState , action , scope , TAG1 , TAG2 );
	}
	}
	
	private class CodebaseCopyTags extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String TAG1 = getRequiredArg( action , 0 , "TAG1" );
		String TAG2 = getRequiredArg( action , 1 , "TAG2" );
		ActionScope scope = getCodebaseScope( action , 2 );
		CodebaseCommand.copyTags( parentState , action , scope , TAG1 , TAG2 );
	}
	}
	
	private class CodebaseCopyTagToBranch extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String TAG1 = getRequiredArg( action , 0 , "TAG1" );
		String BRANCH2 = getRequiredArg( action , 1 , "BRANCH2" );
		ActionScope scope = getCodebaseScope( action , 2 ); 
		CodebaseCommand.copyTagToBranch( parentState , action , scope , TAG1 , BRANCH2 );
	}
	}
	
	private class CodebaseDropTags extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String TAG1 = getRequiredArg( action , 0 , "TAG1" );
		ActionScope scope = getCodebaseScope( action , 1 ); 
		CodebaseCommand.dropTags( parentState , action , scope , TAG1 );
	}
	}
	
	private class CodebaseDropBranch extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String BRANCH1 = getRequiredArg( action , 0 , "BRANCH1" );
		ActionScope scope = getCodebaseScope( action , 1 ); 
		CodebaseCommand.dropBranch( parentState , action , scope , BRANCH1 );
	}
	}
	
	private class CodebaseExport extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String CODIRNAME = getCODIR( action , 0 );
		ActionScope scope = getCodebaseScope( action , 1 ); 
		LocalFolder CODIR = action.artefactory.getAnyFolder( action , CODIRNAME );
		CodebaseCommand.export( parentState , action , scope , CODIR , "" );
	}
	}
	
	private class CodebaseList extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String CMD = getArg( action , 0 );
		ActionScope scope = getCodebaseScope( action , 1 ); 
		CodebaseCommand.list( parentState , action , scope , CMD );
	}
	}
	
	private class CodebaseRenameBranch extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String BRANCH1 = getRequiredArg( action , 0 , "BRANCH1" );
		String BRANCH2 = getRequiredArg( action , 1 , "BRANCH2" );
		ActionScope scope = getCodebaseScope( action , 2 ); 
		CodebaseCommand.renameBranch( parentState , action , scope , BRANCH1 , BRANCH2 );
	}
	}
	
	private class CodebaseRenameTags extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String TAG1 = getRequiredArg( action , 0 , "TAG1" );
		String TAG2 = getRequiredArg( action , 1 , "TAG2" );
		ActionScope scope = getCodebaseScope( action , 2 ); 
		CodebaseCommand.renameTags( parentState , action , scope , TAG1 , TAG2 );
	}
	}
	
	private class CodebaseSetVersion extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String VERSION = getRequiredArg( action , 0 , "VERSION" );
		ActionScope scope = getCodebaseScope( action , 1 );
		CodebaseCommand.setVersion( parentState , action , scope , VERSION );
	}
	}

	private class ThirdpartyUploadDist extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		
		AppProduct product = action.getContextProduct();
		Dist dist = action.getReleaseDist( product , RELEASELABEL );
		
		ActionReleaseScopeMaker maker = new ActionReleaseScopeMaker( action , dist.release );
		ActionScopeTarget scopeProject = maker.addScopeReleaseProjectItemsTarget( "thirdparty" , null );
		CodebaseCommand.thirdpartyUploadDist( parentState , action , scopeProject , dist );
	}
	}

	private class ThirdpartyUploadLib extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String GROUPID = getRequiredArg( action , 0 , "GROUPID" );
		String FILE = getRequiredArg( action , 1 , "FILE" );
		String ARTEFACTID = getArg( action , 2 );
		String VERSION = getArg( action , 3 );
		String CLASSIFIER = getArg( action , 4 );
		
		Meta meta = action.getContextMeta();
		CodebaseCommand.thirdpartyUploadLib( parentState , action , meta , GROUPID , FILE , ARTEFACTID , VERSION , CLASSIFIER );
	}
	}
	
}
