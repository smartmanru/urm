package ru.egov.urm.action.build;

import java.util.Map;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.action.ActionScope;
import ru.egov.urm.action.ActionScopeTarget;
import ru.egov.urm.action.database.ActionGetDB;
import ru.egov.urm.conf.ActionGetConf;
import ru.egov.urm.conf.ConfBuilder;
import ru.egov.urm.meta.MetaReleaseDelivery;
import ru.egov.urm.meta.Metadata.VarCATEGORY;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.storage.DistStorage;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.LogStorage;

public class BuildCommand {
	
	public BuildCommand() {
	}

	public void buildTags( ActionBase action , String TAG , ActionScope scope , LocalFolder OUTDIR , String OUTFILE ) throws Exception {
		ActionBuild ca = new ActionBuild( action , null , OUTDIR , OUTFILE , TAG );
		ca.runEachBuildableProject( scope );
		
		if( ca.isFailed() ) {
			if( action.context.CTX_GET || action.context.CTX_DIST )
				action.log( "BUILD FAILED, do not download any artefacts" );
			else
				action.log( "BUILD FAILED" );
		}
		else {
			action.log( "BUILD SUCCESSFUL" );
			
			if( action.context.CTX_GET || action.context.CTX_DIST )
				getAll( action , scope );
		}
	}
	
	public void getAll( ActionBase action , ActionScope scope ) throws Exception {
		boolean copyDist = action.context.CTX_DIST;
		
		// required for serviceCall and storageService processing, even without -dist option
		LocalFolder downloadFolder = action.artefactory.getDownloadFolder( action );
		downloadFolder.removeAll( action );
	
		// precreate delivery folders in release
		if( copyDist ) {
			scope.release.openForChange( action );
			scope.release.createDeliveryFolders( action );
		}
		
		action.log( "getAll: download scope={" + scope.getScopeInfo( action ) + "}" );

		boolean res = true;
		ActionGetBinary ca = new ActionGetBinary( action , null , copyDist , scope.release , downloadFolder );
		if( !ca.runEachSourceProject( scope ) )
			res = false;

		if( scope.releaseBound && scope.hasConfig( action ) ) {
			ActionGetConf cacf = new ActionGetConf( action , null , scope.release );
			if( !cacf.runEachCategoryTarget( scope , VarCATEGORY.CONFIG ) )
				res = false;
			
			// automatically create configuration difference after distributive update
			if( action.context.CTX_DIST )
				createConfigDiffFile( action , scope );
		}
		
		if( scope.releaseBound && scope.hasDatabase( action ) ) {
			ActionGetDB cadb = new ActionGetDB( action , null , scope.release );
			if( !cadb.runEachCategoryTarget( scope , VarCATEGORY.DB ) )
				res = false;
		}
		
		if( scope.releaseBound && scope.hasManual( action ) ) {
			ActionGetManual cam = new ActionGetManual( action , null , copyDist , scope.release , downloadFolder );
			if( !cam.runSimple() )
				res = false;
		}
		
		if( copyDist )
			scope.release.closeChange( action );
		
		if( !res )
			action.exit( "there are errors in release, please check" );
			
		if( copyDist )
			action.log( "getAll: download has been finished, copied to distribution directory " + scope.release.RELEASEDIR );
		else
			action.log( "getAll: download has been finished, saved to artefacts directory " + downloadFolder.folderPath );
	}
	
	private void createConfigDiffFile( ActionBase action , ActionScope scope ) throws Exception {
		action.log( "update configuration difference information ..." );
		ConfBuilder builder = new ConfBuilder( action );
		
		for( MetaReleaseDelivery delivery : scope.release.info.getDeliveries( action ).values() ) {
			if( delivery.getConfItems( action ).size() > 0 ) {
				String file = builder.createConfDiffFile( scope.release , delivery );
				scope.release.replaceConfDiffFile( action , file , delivery );
			}
		}
	}
	
	public void setTag( ActionBase action , String TAG , ActionScope scope ) throws Exception {
		ActionSetTagOnBuildBranch ca = new ActionSetTagOnBuildBranch( action , null , TAG );
		ca.runEachBuildableProject( scope );
	}
	
	public void printActiveProperties( ActionBase action ) throws Exception {
		Map<String,String> exports = action.meta.product.getExportProperties( action );
		if( !exports.isEmpty() ) {
			action.log( "----------------");
			action.log( "product exports:");
			action.log( "----------------");
			for( String key : Common.getSortedKeys( exports ) )
				action.log( "export " + key + "=" + exports.get( key ) );
		}
		
		Map<String,String> props = action.meta.product.getProductProperties();
		action.log( "-------------------");
		action.log( "product properties:");
		action.log( "-------------------");
		for( String key : Common.getSortedKeys( props ) )
			action.log( "property " + key + "=" + props.get( key ) );
	}

	public void checkout( ActionBase action , ActionScope scope , LocalFolder CODIR ) throws Exception {
		ActionGetCodebase ca = new ActionGetCodebase( action , null , CODIR , true , true , "" );
		ca.runEachBuildableProject( scope );
	}
	
	public void commit( ActionBase action , ActionScope scope , LocalFolder CODIR , String MESSAGE ) throws Exception {
		ActionCommitCodebase ca = new ActionCommitCodebase( action , null , CODIR , MESSAGE );
		ca.runEachBuildableProject( scope );
	}
	
	public void ñopyBranches( ActionBase action , ActionScope scope , String BRANCH1 , String BRANCH2 ) throws Exception {
		ActionCopyCodebase ca = new ActionCopyCodebase( action , null , true , BRANCH1 , true , BRANCH2 , false );
		ca.runEachBuildableProject( scope );
	}
	
	public void ñopyBranchToTag( ActionBase action , ActionScope scope , String BRANCH , String TAG ) throws Exception {
		ActionCopyCodebase ca = new ActionCopyCodebase( action , null , true , BRANCH , false , TAG , true );
		ca.runEachBuildableProject( scope );
	}
	
	public void copyNewTags( ActionBase action , ActionScope scope , String TAG1 , String TAG2 ) throws Exception {
		ActionCopyCodebase ca = new ActionCopyCodebase( action , null , false , TAG1 , false , TAG2 , false );
		ca.runEachBuildableProject( scope );
	}
	
	public void copyTags( ActionBase action , ActionScope scope , String TAG1 , String TAG2 ) throws Exception {
		ActionCopyCodebase ca = new ActionCopyCodebase( action , null , false , TAG1 , false , TAG2 , true );
		ca.runEachBuildableProject( scope );
	}
	
	public void copyTagToBranch( ActionBase action , ActionScope scope , String TAG1 , String BRANCH2 ) throws Exception {
		ActionCopyCodebase ca = new ActionCopyCodebase( action , null , false , TAG1 , true , BRANCH2 , false );
		ca.runEachBuildableProject( scope );
	}
	
	public void dropTags( ActionBase action , ActionScope scope , String TAG1 ) throws Exception {
		ActionDropCodebase ca = new ActionDropCodebase( action , null , false , TAG1 );
		ca.runEachBuildableProject( scope );
	}
	
	public void dropBranch( ActionBase action , ActionScope scope , String BRANCH1 ) throws Exception {
		ActionDropCodebase ca = new ActionDropCodebase( action , null , true , BRANCH1 );
		ca.runEachBuildableProject( scope );
	}
	
	public void export( ActionBase action , ActionScope scope , LocalFolder CODIR , String SINGLEFILE ) throws Exception {
		ActionGetCodebase ca = new ActionGetCodebase( action , null , CODIR , false , true , SINGLEFILE );
		ca.runEachBuildableProject( scope );
	}
	
	public void renameBranch( ActionBase action , ActionScope scope , String BRANCH1 , String BRANCH2 ) throws Exception {
		ActionRenameCodebase ca = new ActionRenameCodebase( action , null , true , BRANCH1 , true , BRANCH2 , false );
		ca.runEachBuildableProject( scope );
	}
	
	public void renameTags( ActionBase action , ActionScope scope , String TAG1 , String TAG2 ) throws Exception {
		ActionRenameCodebase ca = new ActionRenameCodebase( action , null , false , TAG1 , false , TAG2 , true );
		ca.runEachBuildableProject( scope );
	}
	
	public void setVersion( ActionBase action , ActionScope scope , String VERSION ) throws Exception {
		ActionSetVersion ca = new ActionSetVersion( action , null , VERSION );
		ca.runEachBuildableProject( scope );
	}
	
	public void buildAllTags( ActionBase action , String TAG , String SET , String[] PROJECTS ) throws Exception {
		action.checkRequired( action.context.buildMode , "BUILDMODE" );
		
		// execute
		LogStorage storage = action.artefactory.getTagBuildLogStorage( action , TAG );
		LocalFolder OUTDIR = storage.logFolder;
		String OUTFILE = OUTDIR.folderPath + "/build.final.out"; 
		action.redirectTS( "buildAllTags:" , OUTDIR.folderPath , "buildall" , "out" );
		action.logAction();
		
		ActionScope scope = action.getFullScope( SET , PROJECTS , action.context.CTX_RELEASELABEL );
		if( scope.isEmpty( action ) ) {
			action.log( "nothing to build" );
			return;
		}
		
		action.log( "buildAllTags: TAG=" + TAG + ", " + scope.getScopeInfo( action ) );
		action.log( "BUILD TARGETS ..." );
		action.session.createFileFromString( action , OUTFILE , "FINAL STATUS:" );

		buildTags( action , TAG , scope , OUTDIR , OUTFILE );
	}

	public void buildCustom( ActionBase action , String SET , String[] PROJECTS ) throws Exception {
		action.exitNotImplemented();
	}
	
	public void buildRelease( ActionBase action , String SET , String[] PROJECTS , DistStorage release ) throws Exception {
		action.setBuildMode( release.info.PROPERTY_BUILDMODE );
		
		String TAG;
		if( !action.context.CTX_TAG.isEmpty() )
			TAG = action.context.CTX_TAG;
		else
			TAG = release.info.getReleaseCandidateTag( action );
		String RELEASEDIR = release.RELEASEDIR;
		
		LogStorage storage = action.artefactory.getReleaseBuildLogStorage( action , RELEASEDIR );
		LocalFolder OUTDIR = storage.logFolder;
		action.redirectTS( "buildRelease:" , OUTDIR.folderPath , "buildall" , "out" );
	
		action.logAction();
	
		ActionScope scope = ActionScope.getReleaseSetScope( action , release , SET , PROJECTS );
		if( scope.isEmpty( action ) ) {
			action.log( "nothing to build" );
			return;
		}
		
		action.log( "buildRelease: set TAG=" + TAG + ", scope={" + scope.getScopeInfo( action , action.meta.getAllBuildableCategories( action ) ) + "}" );
		setTag( action , TAG , scope );
		
		action.log( "buildRelease: build TAG=" + TAG + ", scope={" + scope.getScopeInfo( action , action.meta.getAllBuildableCategories( action ) ) + "}" );
		String OUTFILE = OUTDIR.folderPath + "/build.final.out"; 
		action.session.createFileFromString( action , OUTFILE , "FINAL STATUS:" );
		buildTags( action , TAG , scope , OUTDIR , OUTFILE );
	}

	public void getAllRelease( ActionBase action , String SET , String[] PROJECTS , DistStorage release ) throws Exception {
		action.setBuildMode( release.info.PROPERTY_BUILDMODE );
		
		ActionScope scope = ActionScope.getReleaseSetScope( action , release , SET , PROJECTS );
		if( scope.isEmpty( action ) ) {
			action.log( "nothing to get" );
			return;
		}
	
		String RELEASEDIR = release.RELEASEDIR;
		LogStorage storage = action.artefactory.getReleaseBuildLogStorage( action , RELEASEDIR );
		LocalFolder OUTDIR = storage.logFolder;
		action.teeTS( "getAllRelease:" , OUTDIR.folderPath , "buildall" , "out" );
	
		action.logAction();
		
		// execute
		getAll( action , scope );
	}

	public void thirdpartyUploadDist( ActionBase action , ActionScopeTarget scopeProject , DistStorage release ) throws Exception {
		ActionUploadReleaseItem ca = new ActionUploadReleaseItem( action , null , release );
		ShellExecutor bs = action.context.pool.createDedicatedLocalShell( ca , "build"  );
		
		try {
			ca.runSingleTarget( scopeProject );
		}
		finally {
			bs.kill( ca );
		}
	}

	public void thirdpartyUploadLib( ActionBase action , String GROUPID , String FILE , String ARTEFACTID , String VERSION , String CLASSIFIER ) throws Exception {
		ActionUploadLibItem ca = new ActionUploadLibItem( action , null , GROUPID , FILE , ARTEFACTID , VERSION , CLASSIFIER );
		ShellExecutor bs = action.context.pool.createDedicatedLocalShell( ca , "build"  );
		
		try {
			ca.runSimple();
		}
		finally {
			bs.kill( ca );
		}
	}

}
