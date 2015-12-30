package ru.egov.urm.run.release;

import ru.egov.urm.meta.Metadata.VarBUILDMODE;
import ru.egov.urm.meta.Metadata.VarCATEGORY;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScope;
import ru.egov.urm.run.build.BuildCommand;
import ru.egov.urm.storage.DistStorage;

public class ReleaseCommand {

	public ReleaseCommand() {
	}

	public void createRelease( ActionBase action , String RELEASELABEL , VarBUILDMODE BUILDMODE ) throws Exception {
		ActionCreateRelease ma = new ActionCreateRelease( action , null , RELEASELABEL , BUILDMODE );
		ma.runSimple();
	}

	public void deleteRelease( ActionBase action , String RELEASELABEL , boolean force ) throws Exception {
		DistStorage dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		ActionDeleteRelease ma = new ActionDeleteRelease( action , null , dist , force );
		ma.runSimple();
	}
	
	public void closeRelease( ActionBase action , String RELEASELABEL ) throws Exception {
		DistStorage dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		ActionForceCloseRelease ma = new ActionForceCloseRelease( action , null , dist );
		ma.runSimple();
	}
	
	public void statusRelease( ActionBase action , String RELEASELABEL ) throws Exception {
		DistStorage dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		ActionPrintReleaseStatus ma = new ActionPrintReleaseStatus( action , null , dist );
		ma.runSimple();
	}

	private void addReleaseScope( ActionBase action , DistStorage dist , ActionScope scope ) throws Exception {
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
		DistStorage dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		ActionScope scope = ActionScope.getProductSetScope( action , SET , elements );
		addReleaseScope( action , dist , scope );
	}

	public void addReleaseConfigItems( ActionBase action , String RELEASELABEL , String[] elements ) throws Exception {
		DistStorage dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		ActionScope scope = ActionScope.getProductCategoryScope( action , VarCATEGORY.CONFIG , elements );
		addReleaseScope( action , dist , scope );
	}

	public void addReleaseDatabaseItems( ActionBase action , String RELEASELABEL , String[] DELIVERIES ) throws Exception {
		DistStorage dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		ActionScope scope = ActionScope.getProductCategoryScope( action , VarCATEGORY.DB , DELIVERIES );
		addReleaseScope( action , dist , scope );
	}

	public void addReleaseBuildItems( ActionBase action , String RELEASELABEL , String[] ITEMS ) throws Exception {
		DistStorage dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		ActionScope scope = ActionScope.getProductDistItemsScope( action , ITEMS );
		addReleaseScope( action , dist , scope );
	}

	public void buildRelease( ActionBase action , String SET , String[] PROJECTS , DistStorage release ) throws Exception {
		BuildCommand buildImpl = new BuildCommand();
		buildImpl.buildRelease( action , SET , PROJECTS , release );
	}

	public void getAllRelease( ActionBase action , String SET , String[] PROJECTS , DistStorage release ) throws Exception {
		BuildCommand buildImpl = new BuildCommand();
		buildImpl.getAllRelease( action , SET , PROJECTS , release );
	}

	private void descope( ActionBase action , DistStorage dist , ActionScope scope ) throws Exception {
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
	
	public void descopeAll( ActionBase action , DistStorage dist ) throws Exception {
		ActionScope scope = ActionScope.getReleaseSetScope( action , dist , "all" , null );
		descope( action , dist , scope );
	}
	
	public void descopeConfComps( ActionBase action , DistStorage dist , String[] COMPS ) throws Exception {
		ActionScope scope = ActionScope.getReleaseCategoryScope( action , dist , VarCATEGORY.CONFIG , COMPS );
		descope( action , dist , scope );
	}
	
	public void descopeManualItems( ActionBase action , DistStorage dist , String[] ITEMS ) throws Exception {
		ActionScope scope = ActionScope.getReleaseCategoryScope( action , dist , VarCATEGORY.MANUAL , ITEMS );
		descope( action , dist , scope );
	}
	
	public void descopeBinary( ActionBase action , DistStorage dist , String SET , String PROJECT , String[] ITEMS ) throws Exception {
		ActionScope scope;
		if( PROJECT.equals( "all" ) )
			scope = ActionScope.getReleaseSetScope( action , dist , SET , new String [] { "all" } );
		else
			scope = ActionScope.getReleaseProjectItemsScope( action , dist , PROJECT , ITEMS );
		descope( action , dist , scope );
	}
	
	public void descopeDatabase( ActionBase action , DistStorage dist , String[] DELIVERIES ) throws Exception {
		ActionScope scope = ActionScope.getReleaseCategoryScope( action , dist , VarCATEGORY.DB , DELIVERIES );
		descope( action , dist , scope );
	}
	
}
