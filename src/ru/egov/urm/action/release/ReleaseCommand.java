package ru.egov.urm.action.release;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.action.ActionScope;
import ru.egov.urm.action.build.BuildCommand;
import ru.egov.urm.dist.Dist;
import ru.egov.urm.meta.Metadata.VarCATEGORY;

public class ReleaseCommand {

	public ReleaseCommand() {
	}

	public void createProd( ActionBase action , String RELEASEVER ) throws Exception {
		ActionCreateProd ma = new ActionCreateProd( action , null , RELEASEVER );
		ma.runSimple();
	}
	
	public void createRelease( ActionBase action , String RELEASELABEL ) throws Exception {
		ActionCreateRelease ma = new ActionCreateRelease( action , null , RELEASELABEL );
		ma.runSimple();
	}

	public void modifyRelease( ActionBase action , Dist dist ) throws Exception {
		ActionModifyRelease ma = new ActionModifyRelease( action , null , dist );
		ma.runSimple();
	}

	public void deleteRelease( ActionBase action , String RELEASELABEL , boolean force ) throws Exception {
		Dist dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		ActionDeleteRelease ma = new ActionDeleteRelease( action , null , dist , force );
		ma.runSimple();
	}
	
	public void closeRelease( ActionBase action , String RELEASELABEL ) throws Exception {
		Dist dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		ActionForceCloseRelease ma = new ActionForceCloseRelease( action , null , dist );
		ma.runSimple();
	}
	
	public void copyRelease( ActionBase action , String RELEASESRC , String RELEASEDST ) throws Exception {
		Dist distSrc = action.artefactory.getDistStorageByLabel( action , RELEASESRC );
		ActionCopyRelease ma = new ActionCopyRelease( action , null , distSrc , RELEASEDST );
		ma.runSimple();
	}
	
	public void finishRelease( ActionBase action , String RELEASELABEL ) throws Exception {
		Dist dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		ActionFinishRelease ma = new ActionFinishRelease( action , null , dist );
		ma.runSimple();
	}
	
	public void reopenRelease( ActionBase action , String RELEASELABEL ) throws Exception {
		Dist dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		ActionReopenRelease ma = new ActionReopenRelease( action , null , dist );
		ma.runSimple();
	}
	
	public void statusRelease( ActionBase action , String RELEASELABEL ) throws Exception {
		Dist dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		ActionPrintReleaseStatus ma = new ActionPrintReleaseStatus( action , null , dist );
		ma.runSimple();
	}

	private void addReleaseScope( ActionBase action , Dist dist , ActionScope scope ) throws Exception {
		ActionAddScope ma = new ActionAddScope( action , null , dist );
		
		dist.openForChange( action );
		if( !ma.runAll( scope ) ) {
			dist.closeChange( action );
			action.exit( "release set is not changed because of errors" );
		}

		dist.saveReleaseXml( action );
		dist.createDeliveryFolders( action );
		dist.closeChange( action );
		
		action.log( "scope (" + scope.getScopeInfo( action ) + ") - added to release" );
	}
	
	public void addReleaseBuildProjects( ActionBase action , String RELEASELABEL , String SET , String[] elements ) throws Exception {
		Dist dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		ActionScope scope = ActionScope.getProductSetScope( action , SET , elements );
		addReleaseScope( action , dist , scope );
	}

	public void addReleaseConfigItems( ActionBase action , String RELEASELABEL , String[] elements ) throws Exception {
		Dist dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		ActionScope scope = ActionScope.getProductCategoryScope( action , VarCATEGORY.CONFIG , elements );
		addReleaseScope( action , dist , scope );
	}

	public void addReleaseDatabaseItems( ActionBase action , String RELEASELABEL , String[] DELIVERIES ) throws Exception {
		Dist dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		ActionScope scope = ActionScope.getProductCategoryScope( action , VarCATEGORY.DB , DELIVERIES );
		addReleaseScope( action , dist , scope );
	}

	public void addReleaseBuildItems( ActionBase action , String RELEASELABEL , String[] ITEMS ) throws Exception {
		Dist dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		ActionScope scope = ActionScope.getProductDistItemsScope( action , ITEMS );
		addReleaseScope( action , dist , scope );
	}

	public void buildRelease( ActionBase action , String SET , String[] PROJECTS , Dist release ) throws Exception {
		BuildCommand buildImpl = new BuildCommand();
		buildImpl.buildRelease( action , SET , PROJECTS , release );
	}

	public void getAllRelease( ActionBase action , String SET , String[] PROJECTS , Dist release ) throws Exception {
		BuildCommand buildImpl = new BuildCommand();
		action.context.CTX_DIST = true;
		buildImpl.getAllRelease( action , SET , PROJECTS , release );
	}

	private void descope( ActionBase action , Dist dist , ActionScope scope ) throws Exception {
		ActionDescope ma = new ActionDescope( action , null , dist );
		
		dist.openForChange( action );
		if( !ma.runAll( scope ) ) {
			dist.closeChange( action );
			action.exit( "release set is not changed because of errors" );
		}

		dist.saveReleaseXml( action );
		dist.closeChange( action );
		
		action.log( "scope (" + scope.getScopeInfo( action ) + ") - removed from release" );
	}
	
	public void descopeAll( ActionBase action , Dist dist ) throws Exception {
		ActionScope scope = ActionScope.getReleaseSetScope( action , dist , "all" , null );
		descope( action , dist , scope );
	}
	
	public void descopeConfComps( ActionBase action , Dist dist , String[] COMPS ) throws Exception {
		ActionScope scope = ActionScope.getReleaseCategoryScope( action , dist , VarCATEGORY.CONFIG , COMPS );
		descope( action , dist , scope );
	}
	
	public void descopeManualItems( ActionBase action , Dist dist , String[] ITEMS ) throws Exception {
		ActionScope scope = ActionScope.getReleaseCategoryScope( action , dist , VarCATEGORY.MANUAL , ITEMS );
		descope( action , dist , scope );
	}
	
	public void descopeBinary( ActionBase action , Dist dist , String SET , String PROJECT , String[] ITEMS ) throws Exception {
		ActionScope scope;
		if( PROJECT.equals( "all" ) )
			scope = ActionScope.getReleaseSetScope( action , dist , SET , new String [] { "all" } );
		else
			scope = ActionScope.getReleaseProjectItemsScope( action , dist , PROJECT , ITEMS );
		descope( action , dist , scope );
	}
	
	public void descopeDatabase( ActionBase action , Dist dist , String[] DELIVERIES ) throws Exception {
		ActionScope scope = ActionScope.getReleaseCategoryScope( action , dist , VarCATEGORY.DB , DELIVERIES );
		descope( action , dist , scope );
	}
	
}
