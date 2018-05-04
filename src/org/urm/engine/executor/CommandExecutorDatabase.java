package org.urm.engine.executor;

import org.urm.action.ActionBase;
import org.urm.action.ActionReleaseScopeMaker;
import org.urm.action.ActionScope;
import org.urm.action.database.DatabaseCommand;
import org.urm.common.action.CommandMeta;
import org.urm.common.meta.DatabaseCommandMeta;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.Engine;
import org.urm.engine.action.CommandMethod;
import org.urm.engine.action.CommandExecutor;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.ReleaseDistScope;
import org.urm.engine.dist.ReleaseDistScopeDelivery;
import org.urm.engine.status.ScopeState;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.ProductMeta;

public class CommandExecutorDatabase extends CommandExecutor {

	DatabaseCommand impl;
	MetaEnv env;
	MetaEnvSegment sg;
	
	String propertyBasedMethods;
	
	public static CommandExecutorDatabase createExecutor( Engine engine ) throws Exception {
		DatabaseCommandMeta commandInfo = new DatabaseCommandMeta( engine.optionsMeta );
		return( new CommandExecutorDatabase( engine , commandInfo ) );
	}
		
	private CommandExecutorDatabase( Engine engine , CommandMeta commandInfo ) throws Exception {
		super( engine , commandInfo );
		
		super.defineAction( new InitDB() , "initdb" );
		super.defineAction( new GetReleaseScripts() , "getsql" );
		super.defineAction( new ApplyManual() , "dbmanual" );
		super.defineAction( new ApplyAutomatic() , "dbapply" );
		super.defineAction( new ManageRelease() , "manage" );
		super.defineAction( new ImportDB() , "import" );
		super.defineAction( new ExportDB() , "export" );
		
		propertyBasedMethods = "initdb dbmanual dbapply import";
		impl = new DatabaseCommand();
	}
	
	@Override
	public boolean runExecutorImpl( ScopeState parentState , ActionBase action , CommandMethod method ) {
		// log action and run 
		boolean res = super.runMethod( parentState , action , method );
		return( res );
	}

	private ActionScope getIndexScope( ActionBase action , Dist dist , int posFrom ) throws Exception {
		String[] INDEXES = getArgList( action , posFrom );
		ActionReleaseScopeMaker maker = new ActionReleaseScopeMaker( action , dist.release );
		maker.addScopeReleaseDatabaseManualItems( INDEXES );
		return( maker.getScope() );
	}
	
	private class GetReleaseScripts extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		AppProduct product = action.getContextProduct();
		Dist dist = action.getReleaseDist( product , RELEASELABEL );
		String[] DELIVERIES = getArgList( action , 1 );
		
		ActionReleaseScopeMaker maker = new ActionReleaseScopeMaker( action , dist.release );
		maker.addScopeReleaseCategory( DBEnumScopeCategoryType.DB , DELIVERIES );
		ActionScope scope = maker.getScope();
		impl.getReleaseScripts( parentState , action , scope , dist );
	}
	}
	
	private class InitDB extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String SERVER = getRequiredArg( action , 0 , "SERVER" );
		int node = getIntArg( action , 1 , -1 );
		impl.initDatabase( parentState , action , SERVER , node );
	}
	}

	private class ApplyManual extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		AppProduct product = action.getContextProduct();
		Dist dist = action.getReleaseDist( product , RELEASELABEL );
		String SERVER = getRequiredArg( action , 1 , "DBSERVER" );
		MetaEnvServer server = action.context.sg.getServer( SERVER );
		ActionScope scope = getIndexScope( action , dist , 2 );
		impl.applyManual( parentState , action , scope , dist , server );
	}
	}

	private class ApplyAutomatic extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		AppProduct product = action.getContextProduct();
		Dist dist = action.getReleaseDist( product , RELEASELABEL );
		String DELIVERY = getRequiredArg( action , 1 , "delivery" );
		
		ReleaseDistScopeDelivery delivery = null;
		String indexScope = null;
		if( DELIVERY.equals( "all" ) )
			checkNoArgs( action , 2 );
		else {
			delivery = ReleaseDistScope.createDeliveryScope( dist.release , DELIVERY , DBEnumScopeCategoryType.DB );
			indexScope = getRequiredArg( action , 2 , "mask" );
			checkNoArgs( action , 3 );
		}
		
		impl.applyAutomatic( parentState , action , dist , delivery , indexScope );
	}
	}

	private class ManageRelease extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		
		AppProduct product = action.getContextProduct();
		Dist dist = action.getReleaseDist( product , RELEASELABEL );
		Meta meta = dist.meta;
		
		String CMD = getRequiredArg( action , 1 , "CMD" );
		String DELIVERY = getRequiredArg( action , 2 , "delivery" );
		
		MetaDistrDelivery delivery = null;
		String indexScope = null;
		if( DELIVERY.equals( "all" ) )
			checkNoArgs( action , 3 );
		else {
			ProductMeta storage = meta.getStorage();
			MetaDistr distr = storage.getDistr();
			delivery = distr.getDelivery( DELIVERY );
			indexScope = getRequiredArg( action , 3 , "mask" );
			checkNoArgs( action , 4 );
		}
		
		impl.manageRelease( parentState , action , meta , dist.release.RELEASEVER , delivery , CMD , indexScope );
	}
	}
	
	private class ImportDB extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String TASK = getRequiredArg( action , 0 , "TASK" );
		String CMD = getRequiredArg( action , 1 , "CMD" );
		String SCHEMA = getArg( action , 2 );
		impl.importDatabase( parentState , action , TASK , CMD , SCHEMA );
	}
	}
	
	private class ExportDB extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String TASK = getRequiredArg( action , 0 , "TASK" );
		String CMD = getRequiredArg( action , 1 , "CMD" );
		String SCHEMA = getArg( action , 2 );
		impl.exportDatabase( parentState , action , TASK , CMD , SCHEMA );
	}
	}
	
}
