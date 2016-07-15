package org.urm.server.executor;

import org.urm.common.Common;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandOptions;
import org.urm.server.CommandExecutor;
import org.urm.server.ServerEngine;
import org.urm.server.action.ActionInit;
import org.urm.server.action.ActionScope;
import org.urm.server.action.CommandAction;
import org.urm.server.action.database.DatabaseCommand;
import org.urm.server.dist.Dist;
import org.urm.server.dist.DistRepository;
import org.urm.server.dist.ReleaseDelivery;
import org.urm.server.meta.MetaDistrDelivery;
import org.urm.server.meta.MetaEnv;
import org.urm.server.meta.MetaEnvDC;
import org.urm.server.meta.MetaEnvServer;
import org.urm.server.meta.Metadata.VarCATEGORY;

public class DatabaseCommandExecutor extends CommandExecutor {

	DatabaseCommand impl;
	MetaEnv env;
	MetaEnvDC dc;
	
	String propertyBasedMethods;
	
	public DatabaseCommandExecutor( ServerEngine engine , CommandMeta commandInfo  , CommandOptions options) throws Exception {
		super( engine , commandInfo , options );
		
		super.defineAction( new InitDB() , "initdb" );
		super.defineAction( new GetReleaseScripts() , "getsql" );
		super.defineAction( new ApplyManual() , "dbmanual" );
		super.defineAction( new ApplyAutomatic() , "dbapply" );
		super.defineAction( new ManageRelease() , "manage" );
		super.defineAction( new ImportDB() , "import" );
		super.defineAction( new ExportDB() , "export" );
		
		propertyBasedMethods = "initdb dbmanual dbapply import";
	}
	
	public boolean run( ActionInit action ) {
		try {
			// create implementation
			impl = new DatabaseCommand();
			action.meta.loadDistr( action );
			action.meta.loadSources( action );
			
			boolean loadProps = Common.checkPartOfSpacedList( action.actionName , propertyBasedMethods ); 
			action.context.loadEnv( action , loadProps );
		}
		catch( Throwable e ) {
			action.log( e );
			return( false );
		}
		
		// log action and run 
		boolean res = super.runMethod( action , action.commandAction );
		return( res );
	}

	private ActionScope getIndexScope( ActionInit action , Dist dist , int posFrom ) throws Exception {
		String[] INDEXES = getArgList( action , posFrom );
		return( ActionScope.getDatabaseManualItemsScope( action , dist , INDEXES ) );
	}
	
	private class GetReleaseScripts extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		Dist dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		String[] DELIVERIES = getArgList( action , 1 );
		ActionScope scope = ActionScope.getReleaseCategoryScope( action , dist , VarCATEGORY.DB , DELIVERIES );
		impl.getReleaseScripts( action , scope , dist );
	}
	}
	
	private class InitDB extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String SERVER = getRequiredArg( action , 0 , "SERVER" );
		int node = getIntArg( action , 1 , -1 );
		impl.initDatabase( action , SERVER , node );
	}
	}

	private class ApplyManual extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		Dist dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		String SERVER = getRequiredArg( action , 1 , "DBSERVER" );
		MetaEnvServer server = action.context.dc.getServer( action , SERVER );
		ActionScope scope = getIndexScope( action , dist , 2 );
		impl.applyManual( action , scope , dist , server );
	}
	}

	private class ApplyAutomatic extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		Dist dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		String DELIVERY = getRequiredArg( action , 1 , "delivery" );
		
		ReleaseDelivery delivery = null;
		String indexScope = null;
		if( DELIVERY.equals( "all" ) )
			checkNoArgs( action , 2 );
		else {
			delivery = dist.release.getDelivery( action , DELIVERY );
			indexScope = getRequiredArg( action , 2 , "mask" );
			checkNoArgs( action , 3 );
		}
		
		impl.applyAutomatic( action , dist , delivery , indexScope );
	}
	}

	private class ManageRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		DistRepository repo = action.artefactory.getDistRepository( action );
		String RELEASEVER = repo.getReleaseVerByLabel( action , RELEASELABEL );
		
		String CMD = getRequiredArg( action , 1 , "CMD" );
		String DELIVERY = getRequiredArg( action , 2 , "delivery" );
		
		MetaDistrDelivery delivery = null;
		String indexScope = null;
		if( DELIVERY.equals( "all" ) )
			checkNoArgs( action , 3 );
		else {
			delivery = action.meta.distr.getDelivery( action , DELIVERY );
			indexScope = getRequiredArg( action , 3 , "mask" );
			checkNoArgs( action , 4 );
		}
		
		impl.manageRelease( action , RELEASEVER , delivery , CMD , indexScope );
	}
	}
	
	private class ImportDB extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String SERVER = getRequiredArg( action , 0 , "SERVER" );
		String CMD = getRequiredArg( action , 1 , "CMD" );
		String SCHEMA = getArg( action , 2 );
		impl.importDatabase( action , SERVER , CMD , SCHEMA );
	}
	}
	
	private class ExportDB extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String SERVER = getRequiredArg( action , 0 , "SERVER" );
		String CMD = getRequiredArg( action , 1 , "CMD" );
		String SCHEMA = getArg( action , 2 );
		impl.exportDatabase( action , SERVER , CMD , SCHEMA );
	}
	}
	
}
