package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.action.build.BuildCommand;
import org.urm.engine.dist.Dist;
import org.urm.meta.engine.ServerAuth.SecurityAction;
import org.urm.meta.product.Meta;
import org.urm.meta.Types.*;

public class ReleaseCommand {

	public ReleaseCommand() {
	}

	public void createProd( ActionBase action , Meta meta , String RELEASEVER ) throws Exception {
		ActionCreateProd ma = new ActionCreateProd( action , null , meta , RELEASEVER );
		ma.runSimpleProduct( meta.name , SecurityAction.ACTION_RELEASE , false );
	}
	
	public void createRelease( ActionBase action , Meta meta , String RELEASELABEL ) throws Exception {
		ActionCreateRelease ma = new ActionCreateRelease( action , null , meta , RELEASELABEL );
		ma.runSimpleProduct( meta.name , SecurityAction.ACTION_RELEASE , false );
	}

	public void modifyRelease( ActionBase action , Dist dist ) throws Exception {
		ActionModifyRelease ma = new ActionModifyRelease( action , null , dist );
		ma.runSimpleProduct( dist.meta.name , SecurityAction.ACTION_RELEASE , false );
	}

	public void deleteRelease( ActionBase action , Meta meta , String RELEASELABEL , boolean force ) throws Exception {
		Dist dist = action.artefactory.getDistStorageByLabel( action , meta , RELEASELABEL );
		ActionDeleteRelease ma = new ActionDeleteRelease( action , null , dist , force );
		ma.runSimpleProduct( meta.name , SecurityAction.ACTION_RELEASE , false );
	}
	
	public void closeRelease( ActionBase action , Meta meta , String RELEASELABEL ) throws Exception {
		Dist dist = action.artefactory.getDistStorageByLabel( action , meta , RELEASELABEL );
		ActionForceCloseRelease ma = new ActionForceCloseRelease( action , null , dist );
		ma.runSimpleProduct( meta.name , SecurityAction.ACTION_RELEASE , false );
	}
	
	public void copyRelease( ActionBase action , Meta meta , String RELEASESRC , String RELEASEDST ) throws Exception {
		Dist distSrc = action.artefactory.getDistStorageByLabel( action , meta , RELEASESRC );
		ActionCopyRelease ma = new ActionCopyRelease( action , null , distSrc , RELEASEDST );
		ma.runSimpleProduct( meta.name , SecurityAction.ACTION_RELEASE , false );
	}
	
	public void finishRelease( ActionBase action , Meta meta , String RELEASELABEL ) throws Exception {
		Dist dist = action.artefactory.getDistStorageByLabel( action , meta , RELEASELABEL );
		ActionFinishRelease ma = new ActionFinishRelease( action , null , dist );
		ma.runSimpleProduct( meta.name , SecurityAction.ACTION_RELEASE , false );
	}
	
	public void reopenRelease( ActionBase action , Meta meta , String RELEASELABEL ) throws Exception {
		Dist dist = action.artefactory.getDistStorageByLabel( action , meta , RELEASELABEL );
		ActionReopenRelease ma = new ActionReopenRelease( action , null , dist );
		ma.runSimpleProduct( meta.name , SecurityAction.ACTION_RELEASE , false );
	}
	
	public void statusRelease( ActionBase action , Meta meta , String RELEASELABEL ) throws Exception {
		Dist dist = action.artefactory.getDistStorageByLabel( action , meta , RELEASELABEL );
		ActionPrintReleaseStatus ma = new ActionPrintReleaseStatus( action , null , dist );
		ma.runSimpleProduct( meta.name , SecurityAction.ACTION_RELEASE , false );
	}

	private void addReleaseScope( ActionBase action , Dist dist , ActionScope scope ) throws Exception {
		ActionAddScope ma = new ActionAddScope( action , null , dist );
		
		dist.openForChange( action );
		if( !ma.runAll( scope , null , SecurityAction.ACTION_DEPLOY , false ) ) {
			dist.closeChange( action );
			action.exit0( _Error.ReleaseSetChangeErrors0 , "release set is not changed because of errors" );
		}

		dist.saveReleaseXml( action );
		dist.createDeliveryFolders( action );
		dist.closeChange( action );
		
		action.info( "scope (" + scope.getScopeInfo( action ) + ") - added to release" );
	}
	
	public void addReleaseBuildProjects( ActionBase action , Meta meta , String RELEASELABEL , String SET , String[] elements ) throws Exception {
		Dist dist = action.artefactory.getDistStorageByLabel( action , meta , RELEASELABEL );
		if( dist.release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		ActionScope scope = ActionScope.getProductSetScope( action , meta , SET , elements );
		addReleaseScope( action , dist , scope );
	}

	public void addReleaseConfigItems( ActionBase action , Meta meta , String RELEASELABEL , String[] elements ) throws Exception {
		Dist dist = action.artefactory.getDistStorageByLabel( action , meta , RELEASELABEL );
		if( dist.release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		ActionScope scope = ActionScope.getProductCategoryScope( action , meta , VarCATEGORY.CONFIG , elements );
		addReleaseScope( action , dist , scope );
	}

	public void addReleaseDatabaseItems( ActionBase action , Meta meta , String RELEASELABEL , String[] DELIVERIES ) throws Exception {
		Dist dist = action.artefactory.getDistStorageByLabel( action , meta , RELEASELABEL );
		if( dist.release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		ActionScope scope = ActionScope.getProductCategoryScope( action , meta , VarCATEGORY.DB , DELIVERIES );
		addReleaseScope( action , dist , scope );
	}

	public void addReleaseBuildItems( ActionBase action , Meta meta , String RELEASELABEL , String[] ITEMS ) throws Exception {
		Dist dist = action.artefactory.getDistStorageByLabel( action , meta , RELEASELABEL );
		if( dist.release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		ActionScope scope = ActionScope.getProductDistItemsScope( action , meta , ITEMS );
		addReleaseScope( action , dist , scope );
	}

	public void buildRelease( ActionBase action , String SET , String[] PROJECTS , Dist dist ) throws Exception {
		if( dist.release.isCumulative() )
			action.exit0( _Error.CannotBuildCumulative0 , "cannot build cumulative release" );
		
		BuildCommand buildImpl = new BuildCommand();
		buildImpl.buildRelease( action , dist.meta , SET , PROJECTS , dist );
	}

	public void getAllRelease( ActionBase action , String SET , String[] PROJECTS , Dist dist ) throws Exception {
		if( dist.release.isCumulative() )
			action.exit0( _Error.CannotDownloadCumulative0 , "cannot download cumulative release" );
		
		BuildCommand buildImpl = new BuildCommand();
		action.context.CTX_DIST = true;
		buildImpl.getAllRelease( action , SET , PROJECTS , dist );
	}

	public void getCumulativeRelease( ActionBase action , Dist dist ) throws Exception {
		if( !dist.release.isCumulative() )
			action.exit0( _Error.NotCumulativeRelease0 , "should be cumulative release" );
		
		ActionGetCumulative ca = new ActionGetCumulative( action , null , dist.meta , dist );
		ca.runSimpleProduct( dist.meta.name , SecurityAction.ACTION_RELEASE , false );
	}

	private void descope( ActionBase action , Dist dist , ActionScope scope ) throws Exception {
		if( dist.release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		ActionDescope ma = new ActionDescope( action , null , dist );
		
		dist.openForChange( action );
		if( !ma.runAll( scope , null , SecurityAction.ACTION_DEPLOY , false ) ) {
			dist.closeChange( action );
			action.exit0( _Error.ReleaseSetChangeErrors0 , "release set is not changed because of errors" );
		}

		dist.saveReleaseXml( action );
		dist.closeChange( action );
		
		action.info( "scope (" + scope.getScopeInfo( action ) + ") - removed from release" );
	}
	
	public void descopeAll( ActionBase action , Dist dist ) throws Exception {
		if( dist.release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		dist.descopeAll( action );
	}
	
	public void descopeConfComps( ActionBase action , Dist dist , String[] COMPS ) throws Exception {
		if( dist.release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		ActionScope scope = ActionScope.getReleaseCategoryScope( action , dist , VarCATEGORY.CONFIG , COMPS );
		descope( action , dist , scope );
	}
	
	public void descopeManualItems( ActionBase action , Dist dist , String[] ITEMS ) throws Exception {
		if( dist.release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		ActionScope scope = ActionScope.getReleaseCategoryScope( action , dist , VarCATEGORY.MANUAL , ITEMS );
		descope( action , dist , scope );
	}
	
	public void descopeBinary( ActionBase action , Dist dist , String SET , String PROJECT , String[] ITEMS ) throws Exception {
		if( dist.release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		ActionScope scope;
		if( PROJECT.equals( "all" ) )
			scope = ActionScope.getReleaseSetScope( action , dist , SET , new String [] { "all" } );
		else
			scope = ActionScope.getReleaseProjectItemsScope( action , dist , PROJECT , ITEMS );
		descope( action , dist , scope );
	}
	
	public void descopeDatabase( ActionBase action , Dist dist , String[] DELIVERIES ) throws Exception {
		if( dist.release.isCumulative() )
			action.exit0( _Error.CannotChangeCumulative0 , "cannot change scope of cumulative release" );
		
		ActionScope scope = ActionScope.getReleaseCategoryScope( action , dist , VarCATEGORY.DB , DELIVERIES );
		descope( action , dist , scope );
	}
	
}
