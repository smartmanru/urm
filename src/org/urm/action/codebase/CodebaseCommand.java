package org.urm.action.codebase;

import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.ActionProductScopeMaker;
import org.urm.action.ActionReleaseScopeMaker;
import org.urm.action.ActionScope;
import org.urm.action.ActionScopeTarget;
import org.urm.action.conf.ActionGetConf;
import org.urm.action.conf.ConfBuilder;
import org.urm.action.database.ActionGetDB;
import org.urm.common.Common;
import org.urm.common.action.CommandMethodMeta.SecurityAction;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.ReleaseDelivery;
import org.urm.engine.status.ScopeState;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.LogStorage;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.Types.*;

public class CodebaseCommand {
	
	public CodebaseCommand() {
	}

	public void buildTags( ScopeState parentState , ActionBase action , String TAG , ActionScope scope , LocalFolder OUTDIR , String OUTFILE , Dist dist ) throws Exception {
		ActionBuild ca = new ActionBuild( action , null , OUTDIR , OUTFILE , TAG , dist );
		ca.runEachBuildableProject( parentState , scope , SecurityAction.ACTION_CODEBASE , false );
		
		if( ca.isFailed() ) {
			if( action.context.CTX_GET || action.context.CTX_DIST )
				action.error( "BUILD FAILED, skip downloading artefacts" );
			else
				action.error( "BUILD FAILED" );
		}
		else {
			action.info( "BUILD SUCCESSFUL" );
			
			if( action.context.CTX_GET || action.context.CTX_DIST )
				getAll( parentState , action , scope , dist );
		}
	}
	
	public void getAll( ScopeState parentState , ActionBase action , ActionScope scope , Dist dist ) throws Exception {
		boolean copyDist = action.context.CTX_DIST;
		
		// required for serviceCall and storageService processing, even without -dist option
		LocalFolder downloadFolder = action.artefactory.getWorkFolder( action , "download" );
		downloadFolder.recreateThis( action );
	
		// precreate delivery folders in release
		if( copyDist ) {
			dist.openForDataChange( action );
			dist.createDeliveryFolders( action );
		}
		
		action.info( "getAll: download scope={" + scope.getScopeInfo( action ) + "}" );

		boolean res = true;
		ActionGetBinary ca = new ActionGetBinary( action , null , copyDist , dist , downloadFolder );
		if( !ca.runEachSourceProject( parentState , scope , SecurityAction.ACTION_CODEBASE , false ) )
			res = false;

		if( dist != null && scope.hasConfig( action ) ) {
			ActionGetConf cacf = new ActionGetConf( action , null , dist , downloadFolder , action.context.CTX_DIST );
			if( !cacf.runEachCategoryTarget( parentState , scope , VarCATEGORY.CONFIG , SecurityAction.ACTION_CODEBASE , false ) )
				res = false;
			
			// automatically create configuration difference after distributive update
			if( res && action.context.CTX_DIST )
				createConfigDiffFile( action , scope , dist );
		}
		
		if( dist != null && scope.hasDatabase( action ) ) {
			ActionGetDB cadb = new ActionGetDB( action , null , dist , downloadFolder , action.context.CTX_DIST );
			if( !cadb.runEachCategoryTarget( parentState , scope , VarCATEGORY.DB , SecurityAction.ACTION_CODEBASE , false ) )
				res = false;
		}
		
		if( dist != null && scope.hasManual( action ) ) {
			ActionGetManual cam = new ActionGetManual( action , null , scope.meta , copyDist , dist , downloadFolder );
			if( !cam.runProductBuild( parentState , scope.meta.name , SecurityAction.ACTION_CODEBASE , action.context.buildMode , false ) )
				res = false;
		}
		
		if( copyDist ) {
			dist.closeDataChange( action );
			dist.finishStatus( action );
		}
		
		if( !res )
			action.exit0( _Error.BuildErrors0 , "there are errors, please check" );
			
		if( copyDist )
			action.info( "getAll: download has been finished, copied to distribution directory " + dist.RELEASEDIR );
		else
			action.debug( "getAll: download has been finished, saved to artefacts directory " + downloadFolder.folderPath );
	}
	
	private void createConfigDiffFile( ActionBase action , ActionScope scope , Dist dist ) throws Exception {
		action.info( "update configuration difference information ..." );
		ConfBuilder builder = new ConfBuilder( action , scope.meta );
		
		for( ReleaseDelivery delivery : dist.release.getDeliveries() ) {
			if( delivery.getConfItems().length > 0 ) {
				String file = builder.createConfDiffFile( dist , delivery );
				dist.replaceConfDiffFile( action , file , delivery );
			}
		}
	}
	
	public void setTag( ScopeState parentState , ActionBase action , String TAG , ActionScope scope ) throws Exception {
		ActionSetTagOnBuildBranch ca = new ActionSetTagOnBuildBranch( action , null , TAG );
		if( !ca.runEachBuildableProject( parentState , scope , SecurityAction.ACTION_CODEBASE , false ) )
			action.exit1( _Error.ProjectTagError1 , "Error tagging projects, tag=" + TAG , TAG );
	}
	
	public void printActiveProperties( ScopeState parentState , ActionBase action , Meta meta ) throws Exception {
		MetaProductSettings product = meta.getProductSettings();
		Map<String,String> exports = product.getExportProperties( action );
		if( !exports.isEmpty() ) {
			action.info( "----------------");
			action.info( "product exports:");
			action.info( "----------------");
			for( String key : Common.getSortedKeys( exports ) )
				action.info( "export " + key + "=" + exports.get( key ) );
		}
		
		action.info( "-------------------");
		action.info( "product properties:");
		action.info( "-------------------");
		action.printValues( product.getProperties() );
	}

	public void checkout( ScopeState parentState , ActionBase action , ActionScope scope , LocalFolder CODIR ) throws Exception {
		ActionGetCodebase ca = new ActionGetCodebase( action , null , CODIR , true , true , "" );
		ca.runEachBuildableProject( parentState , scope , SecurityAction.ACTION_CODEBASE , false );
	}
	
	public void commit( ScopeState parentState , ActionBase action , ActionScope scope , LocalFolder CODIR , String MESSAGE ) throws Exception {
		ActionCommitCodebase ca = new ActionCommitCodebase( action , null , CODIR , MESSAGE );
		ca.runEachBuildableProject( parentState , scope , SecurityAction.ACTION_CODEBASE , false );
	}
	
	public void copyBranches( ScopeState parentState , ActionBase action , ActionScope scope , String BRANCH1 , String BRANCH2 ) throws Exception {
		ActionCopyCodebase ca = new ActionCopyCodebase( action , null , true , BRANCH1 , true , BRANCH2 , false );
		ca.runEachBuildableProject( parentState , scope , SecurityAction.ACTION_CODEBASE , false );
	}
	
	public void copyBranchTag( ScopeState parentState , ActionBase action , ActionScope scope , String BRANCH , String TAG ) throws Exception {
		ActionCopyCodebase ca = new ActionCopyCodebase( action , null , true , BRANCH , false , TAG , true );
		ca.runEachBuildableProject( parentState , scope , SecurityAction.ACTION_CODEBASE , false );
	}
	
	public void copyNewTags( ScopeState parentState , ActionBase action , ActionScope scope , String TAG1 , String TAG2 ) throws Exception {
		ActionCopyCodebase ca = new ActionCopyCodebase( action , null , false , TAG1 , false , TAG2 , false );
		ca.runEachBuildableProject( parentState , scope , SecurityAction.ACTION_CODEBASE , false );
	}
	
	public void copyTags( ScopeState parentState , ActionBase action , ActionScope scope , String TAG1 , String TAG2 ) throws Exception {
		ActionCopyCodebase ca = new ActionCopyCodebase( action , null , false , TAG1 , false , TAG2 , true );
		ca.runEachBuildableProject( parentState , scope , SecurityAction.ACTION_CODEBASE , false );
	}
	
	public void copyTagToBranch( ScopeState parentState , ActionBase action , ActionScope scope , String TAG1 , String BRANCH2 ) throws Exception {
		ActionCopyCodebase ca = new ActionCopyCodebase( action , null , false , TAG1 , true , BRANCH2 , false );
		ca.runEachBuildableProject( parentState , scope , SecurityAction.ACTION_CODEBASE , false );
	}
	
	public void dropTags( ScopeState parentState , ActionBase action , ActionScope scope , String TAG1 ) throws Exception {
		ActionDropCodebase ca = new ActionDropCodebase( action , null , false , TAG1 );
		ca.runEachBuildableProject( parentState , scope , SecurityAction.ACTION_CODEBASE , false );
	}
	
	public void dropBranch( ScopeState parentState , ActionBase action , ActionScope scope , String BRANCH1 ) throws Exception {
		ActionDropCodebase ca = new ActionDropCodebase( action , null , true , BRANCH1 );
		ca.runEachBuildableProject( parentState , scope , SecurityAction.ACTION_CODEBASE , false );
	}
	
	public void export( ScopeState parentState , ActionBase action , ActionScope scope , LocalFolder CODIR , String SINGLEFILE ) throws Exception {
		ActionGetCodebase ca = new ActionGetCodebase( action , null , CODIR , false , true , SINGLEFILE );
		ca.runEachBuildableProject( parentState , scope , SecurityAction.ACTION_CODEBASE , false );
	}
	
	public void renameBranch( ScopeState parentState , ActionBase action , ActionScope scope , String BRANCH1 , String BRANCH2 ) throws Exception {
		ActionRenameCodebase ca = new ActionRenameCodebase( action , null , true , BRANCH1 , true , BRANCH2 , false );
		ca.runEachBuildableProject( parentState , scope , SecurityAction.ACTION_CODEBASE , false );
	}
	
	public void renameTags( ScopeState parentState , ActionBase action , ActionScope scope , String TAG1 , String TAG2 ) throws Exception {
		ActionRenameCodebase ca = new ActionRenameCodebase( action , null , false , TAG1 , false , TAG2 , true );
		ca.runEachBuildableProject( parentState , scope , SecurityAction.ACTION_CODEBASE , false );
	}
	
	public void setVersion( ScopeState parentState , ActionBase action , ActionScope scope , String VERSION ) throws Exception {
		ActionSetVersion ca = new ActionSetVersion( action , null , VERSION );
		ca.runEachBuildableProject( parentState , scope , SecurityAction.ACTION_CODEBASE , false );
	}
	
	public void buildAllTags( ScopeState parentState , ActionBase action , Meta meta , String TAG , String SET , String[] PROJECTS , Dist dist ) throws Exception {
		action.checkRequired( action.context.buildMode , "BUILDMODE" );
		
		// execute
		LogStorage storage = action.artefactory.getTagBuildLogStorage( action , meta , TAG );
		LocalFolder OUTDIR = storage.logFolder;
		String OUTFILE = OUTDIR.folderPath + "/build.final.out"; 
		action.redirectTS( "buildAllTags:" , OUTDIR.folderPath , "buildall" , "out" );
		action.logAction();
		
		ActionScope scope;
		if( dist == null ) {
			ActionProductScopeMaker maker = new ActionProductScopeMaker( action , meta );
			maker.addScopeProductSet( SET , PROJECTS );
			scope = maker.getScope();
		}
		else {
			ActionReleaseScopeMaker maker = new ActionReleaseScopeMaker( action , dist );
			maker.addScopeReleaseSet( SET , PROJECTS );
			scope = maker.getScope();
		}
			
		if( scope.isEmpty() ) {
			action.info( "nothing to build" );
			return;
		}
		
		action.info( "buildAllTags: TAG=" + TAG + ", " + scope.getScopeInfo( action ) );
		action.info( "BUILD TARGETS ..." );
		action.shell.createFileFromString( action , OUTFILE , "FINAL STATUS:" );

		buildTags( parentState , action , TAG , scope , OUTDIR , OUTFILE , dist );
	}

	public void buildCustom( ScopeState parentState , ActionBase action , Meta meta , String SET , String[] PROJECTS ) throws Exception {
		action.exitNotImplemented();
	}
	
	public void buildRelease( ScopeState parentState , ActionBase action , Meta meta , String SET , String[] PROJECTS , Dist dist ) throws Exception {
		action.setBuildMode( dist.release.BUILDMODE );
		
		String TAG;
		if( !action.context.CTX_TAG.isEmpty() )
			TAG = action.context.CTX_TAG;
		else
			TAG = dist.release.getReleaseCandidateTag( action );
		String RELEASEDIR = dist.RELEASEDIR;
		
		LogStorage storage = action.artefactory.getReleaseBuildLogStorage( action , meta , RELEASEDIR );
		LocalFolder OUTDIR = storage.logFolder;
		action.redirectTS( "buildRelease:" , OUTDIR.folderPath , "buildall" , "out" );
	
		action.logAction();
	
		ActionReleaseScopeMaker maker = new ActionReleaseScopeMaker( action , dist );
		maker.addScopeReleaseSet( SET , PROJECTS );
		ActionScope scope = maker.getScope();
		if( scope.isEmpty() ) {
			action.info( "nothing to build" );
			return;
		}
		
		action.info( "buildRelease: set TAG=" + TAG + ", scope={" + scope.getScopeInfo( action , new VarCATEGORY[] { VarCATEGORY.BUILDABLE } ) + "}" );
		setTag( parentState , action , TAG , scope );
		
		action.info( "buildRelease: build TAG=" + TAG + ", scope={" + scope.getScopeInfo( action , new VarCATEGORY[] { VarCATEGORY.BUILDABLE } ) + "}" );
		String OUTFILE = OUTDIR.folderPath + "/build.final.out"; 
		action.shell.createFileFromString( action , OUTFILE , "FINAL STATUS:" );
		buildTags( parentState , action , TAG , scope , OUTDIR , OUTFILE , dist );
	}

	public void getAllRelease( ScopeState parentState , ActionBase action , String SET , String[] PROJECTS , Dist dist ) throws Exception {
		action.setBuildMode( dist.release.BUILDMODE );
		
		ActionReleaseScopeMaker maker = new ActionReleaseScopeMaker( action , dist );
		maker.addScopeReleaseSet( SET , PROJECTS );
		ActionScope scope = maker.getScope();
		if( scope.isEmpty() ) {
			action.info( "nothing to get" );
			return;
		}
	
		action.logAction();
		
		// execute
		getAll( parentState , action , scope , dist );
	}

	public void thirdpartyUploadDist( ScopeState parentState , ActionBase action , ActionScopeTarget scopeProject , Dist dist ) throws Exception {
		ActionUploadReleaseItem ca = new ActionUploadReleaseItem( action , null , dist );
		ca.runSingleTarget( parentState , scopeProject , null , SecurityAction.ACTION_CODEBASE , false );
	}

	public void thirdpartyUploadLib( ScopeState parentState , ActionBase action , Meta meta , String GROUPID , String FILE , String ARTEFACTID , String VERSION , String CLASSIFIER ) throws Exception {
		ActionUploadLibItem ca = new ActionUploadLibItem( action , null , meta , GROUPID , FILE , ARTEFACTID , VERSION , CLASSIFIER );
		ca.runSimpleServer( parentState , SecurityAction.ACTION_CODEBASE , false );
	}

}
