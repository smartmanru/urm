package org.urm.action.build;

import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.action.ActionScopeTarget;
import org.urm.action.conf.ActionGetConf;
import org.urm.action.conf.ConfBuilder;
import org.urm.action.database.ActionGetDB;
import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.ReleaseDelivery;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.LogStorage;
import org.urm.meta.engine.ServerAuth.SecurityAction;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.Meta.VarCATEGORY;

public class BuildCommand {
	
	public BuildCommand() {
	}

	public void buildTags( ActionBase action , String TAG , ActionScope scope , LocalFolder OUTDIR , String OUTFILE , Dist dist ) throws Exception {
		ActionBuild ca = new ActionBuild( action.actionInit , null , OUTDIR , OUTFILE , TAG );
		ca.runEachBuildableProject( scope , SecurityAction.ACTION_BUILD , false );
		
		if( ca.isFailed() ) {
			if( action.context.CTX_GET || action.context.CTX_DIST )
				action.error( "BUILD FAILED, do not download any artefacts" );
			else
				action.error( "BUILD FAILED" );
		}
		else {
			action.info( "BUILD SUCCESSFUL" );
			
			if( action.context.CTX_GET || action.context.CTX_DIST )
				getAll( action , scope , dist );
		}
	}
	
	public void getAll( ActionBase action , ActionScope scope , Dist dist ) throws Exception {
		boolean copyDist = action.context.CTX_DIST;
		
		// required for serviceCall and storageService processing, even without -dist option
		LocalFolder downloadFolder = action.artefactory.getDownloadFolder( action , scope.meta );
		downloadFolder.removeContent( action );
	
		// precreate delivery folders in release
		if( copyDist ) {
			dist.openForChange( action );
			dist.createDeliveryFolders( action );
		}
		
		action.info( "getAll: download scope={" + scope.getScopeInfo( action ) + "}" );

		boolean res = true;
		ActionGetBinary ca = new ActionGetBinary( action , null , copyDist , dist , downloadFolder );
		if( !ca.runEachSourceProject( scope , SecurityAction.ACTION_BUILD , false ) )
			res = false;

		if( dist != null && scope.hasConfig( action ) ) {
			ActionGetConf cacf = new ActionGetConf( action , null , dist );
			if( !cacf.runEachCategoryTarget( scope , VarCATEGORY.CONFIG , SecurityAction.ACTION_BUILD , false ) )
				res = false;
			
			// automatically create configuration difference after distributive update
			if( res && action.context.CTX_DIST )
				createConfigDiffFile( action , scope , dist );
		}
		
		if( dist != null && scope.hasDatabase( action ) ) {
			ActionGetDB cadb = new ActionGetDB( action , null , dist );
			if( !cadb.runEachCategoryTarget( scope , VarCATEGORY.DB , SecurityAction.ACTION_BUILD , false ) )
				res = false;
		}
		
		if( dist != null && scope.hasManual( action ) ) {
			ActionGetManual cam = new ActionGetManual( action , null , scope.meta , copyDist , dist , downloadFolder );
			if( !cam.runProductBuild( scope.meta.name , SecurityAction.ACTION_BUILD , action.context.buildMode , false ) )
				res = false;
		}
		
		if( copyDist )
			dist.closeChange( action );
		
		if( !res )
			action.exit0( _Error.BuildErrors0 , "there are errors, please check" );
			
		if( copyDist )
			action.info( "getAll: download has been finished, copied to distribution directory " + dist.RELEASEDIR );
		else
			action.info( "getAll: download has been finished, saved to artefacts directory " + downloadFolder.folderPath );
	}
	
	private void createConfigDiffFile( ActionBase action , ActionScope scope , Dist dist ) throws Exception {
		action.info( "update configuration difference information ..." );
		ConfBuilder builder = new ConfBuilder( action , scope.meta );
		
		for( ReleaseDelivery delivery : dist.release.getDeliveries( action ).values() ) {
			if( delivery.getConfItems( action ).size() > 0 ) {
				String file = builder.createConfDiffFile( dist , delivery );
				dist.replaceConfDiffFile( action , file , delivery );
			}
		}
	}
	
	public void setTag( ActionBase action , String TAG , ActionScope scope ) throws Exception {
		ActionSetTagOnBuildBranch ca = new ActionSetTagOnBuildBranch( action , null , TAG );
		ca.runEachBuildableProject( scope , SecurityAction.ACTION_BUILD , false );
	}
	
	public void printActiveProperties( ActionBase action , Meta meta ) throws Exception {
		MetaProductSettings product = meta.getProductSettings( action );
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

	public void checkout( ActionBase action , ActionScope scope , LocalFolder CODIR ) throws Exception {
		ActionGetCodebase ca = new ActionGetCodebase( action , null , CODIR , true , true , "" );
		ca.runEachBuildableProject( scope , SecurityAction.ACTION_BUILD , false );
	}
	
	public void commit( ActionBase action , ActionScope scope , LocalFolder CODIR , String MESSAGE ) throws Exception {
		ActionCommitCodebase ca = new ActionCommitCodebase( action , null , CODIR , MESSAGE );
		ca.runEachBuildableProject( scope , SecurityAction.ACTION_BUILD , false );
	}
	
	public void copyBranches( ActionBase action , ActionScope scope , String BRANCH1 , String BRANCH2 ) throws Exception {
		ActionCopyCodebase ca = new ActionCopyCodebase( action , null , true , BRANCH1 , true , BRANCH2 , false );
		ca.runEachBuildableProject( scope , SecurityAction.ACTION_BUILD , false );
	}
	
	public void copyBranchTag( ActionBase action , ActionScope scope , String BRANCH , String TAG ) throws Exception {
		ActionCopyCodebase ca = new ActionCopyCodebase( action , null , true , BRANCH , false , TAG , true );
		ca.runEachBuildableProject( scope , SecurityAction.ACTION_BUILD , false );
	}
	
	public void copyNewTags( ActionBase action , ActionScope scope , String TAG1 , String TAG2 ) throws Exception {
		ActionCopyCodebase ca = new ActionCopyCodebase( action , null , false , TAG1 , false , TAG2 , false );
		ca.runEachBuildableProject( scope , SecurityAction.ACTION_BUILD , false );
	}
	
	public void copyTags( ActionBase action , ActionScope scope , String TAG1 , String TAG2 ) throws Exception {
		ActionCopyCodebase ca = new ActionCopyCodebase( action , null , false , TAG1 , false , TAG2 , true );
		ca.runEachBuildableProject( scope , SecurityAction.ACTION_BUILD , false );
	}
	
	public void copyTagToBranch( ActionBase action , ActionScope scope , String TAG1 , String BRANCH2 ) throws Exception {
		ActionCopyCodebase ca = new ActionCopyCodebase( action , null , false , TAG1 , true , BRANCH2 , false );
		ca.runEachBuildableProject( scope , SecurityAction.ACTION_BUILD , false );
	}
	
	public void dropTags( ActionBase action , ActionScope scope , String TAG1 ) throws Exception {
		ActionDropCodebase ca = new ActionDropCodebase( action , null , false , TAG1 );
		ca.runEachBuildableProject( scope , SecurityAction.ACTION_BUILD , false );
	}
	
	public void dropBranch( ActionBase action , ActionScope scope , String BRANCH1 ) throws Exception {
		ActionDropCodebase ca = new ActionDropCodebase( action , null , true , BRANCH1 );
		ca.runEachBuildableProject( scope , SecurityAction.ACTION_BUILD , false );
	}
	
	public void export( ActionBase action , ActionScope scope , LocalFolder CODIR , String SINGLEFILE ) throws Exception {
		ActionGetCodebase ca = new ActionGetCodebase( action , null , CODIR , false , true , SINGLEFILE );
		ca.runEachBuildableProject( scope , SecurityAction.ACTION_BUILD , false );
	}
	
	public void renameBranch( ActionBase action , ActionScope scope , String BRANCH1 , String BRANCH2 ) throws Exception {
		ActionRenameCodebase ca = new ActionRenameCodebase( action , null , true , BRANCH1 , true , BRANCH2 , false );
		ca.runEachBuildableProject( scope , SecurityAction.ACTION_BUILD , false );
	}
	
	public void renameTags( ActionBase action , ActionScope scope , String TAG1 , String TAG2 ) throws Exception {
		ActionRenameCodebase ca = new ActionRenameCodebase( action , null , false , TAG1 , false , TAG2 , true );
		ca.runEachBuildableProject( scope , SecurityAction.ACTION_BUILD , false );
	}
	
	public void setVersion( ActionBase action , ActionScope scope , String VERSION ) throws Exception {
		ActionSetVersion ca = new ActionSetVersion( action , null , VERSION );
		ca.runEachBuildableProject( scope , SecurityAction.ACTION_BUILD , false );
	}
	
	public void buildAllTags( ActionBase action , Meta meta , String TAG , String SET , String[] PROJECTS , Dist dist ) throws Exception {
		action.checkRequired( action.context.buildMode , "BUILDMODE" );
		
		// execute
		LogStorage storage = action.artefactory.getTagBuildLogStorage( action , meta , TAG );
		LocalFolder OUTDIR = storage.logFolder;
		String OUTFILE = OUTDIR.folderPath + "/build.final.out"; 
		action.redirectTS( "buildAllTags:" , OUTDIR.folderPath , "buildall" , "out" );
		action.logAction();
		
		ActionScope scope;
		if( dist == null )
			scope = ActionScope.getProductSetScope( action , meta , SET , PROJECTS );
		else
			scope = ActionScope.getReleaseSetScope( action , dist , SET , PROJECTS );
			
		if( scope.isEmpty( action ) ) {
			action.info( "nothing to build" );
			return;
		}
		
		action.info( "buildAllTags: TAG=" + TAG + ", " + scope.getScopeInfo( action ) );
		action.info( "BUILD TARGETS ..." );
		action.shell.createFileFromString( action , OUTFILE , "FINAL STATUS:" );

		buildTags( action , TAG , scope , OUTDIR , OUTFILE , dist );
	}

	public void buildCustom( ActionBase action , Meta meta , String SET , String[] PROJECTS ) throws Exception {
		action.exitNotImplemented();
	}
	
	public void buildRelease( ActionBase action , Meta meta , String SET , String[] PROJECTS , Dist dist ) throws Exception {
		action.setBuildMode( dist.release.PROPERTY_BUILDMODE );
		
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
	
		ActionScope scope = ActionScope.getReleaseSetScope( action , dist , SET , PROJECTS );
		if( scope.isEmpty( action ) ) {
			action.info( "nothing to build" );
			return;
		}
		
		action.info( "buildRelease: set TAG=" + TAG + ", scope={" + scope.getScopeInfo( action , Meta.getAllBuildableCategories() ) + "}" );
		setTag( action , TAG , scope );
		
		action.info( "buildRelease: build TAG=" + TAG + ", scope={" + scope.getScopeInfo( action , Meta.getAllBuildableCategories() ) + "}" );
		String OUTFILE = OUTDIR.folderPath + "/build.final.out"; 
		action.shell.createFileFromString( action , OUTFILE , "FINAL STATUS:" );
		buildTags( action , TAG , scope , OUTDIR , OUTFILE , dist );
	}

	public void getAllRelease( ActionBase action , String SET , String[] PROJECTS , Dist dist ) throws Exception {
		action.setBuildMode( dist.release.PROPERTY_BUILDMODE );
		
		ActionScope scope = ActionScope.getReleaseSetScope( action , dist , SET , PROJECTS );
		if( scope.isEmpty( action ) ) {
			action.info( "nothing to get" );
			return;
		}
	
		action.logAction();
		
		// execute
		getAll( action , scope , dist );
	}

	public void thirdpartyUploadDist( ActionBase action , ActionScopeTarget scopeProject , Dist dist ) throws Exception {
		ActionUploadReleaseItem ca = new ActionUploadReleaseItem( action , null , dist );
		ca.runSingleTarget( scopeProject , null , SecurityAction.ACTION_BUILD , false );
	}

	public void thirdpartyUploadLib( ActionBase action , Meta meta , String GROUPID , String FILE , String ARTEFACTID , String VERSION , String CLASSIFIER ) throws Exception {
		ActionUploadLibItem ca = new ActionUploadLibItem( action , meta , null , GROUPID , FILE , ARTEFACTID , VERSION , CLASSIFIER );
		ca.runSimpleServer( SecurityAction.ACTION_BUILD , false );
	}

}
