package org.urm.engine.executor;

import org.urm.action.ActionBase;
import org.urm.action.ActionReleaseScopeMaker;
import org.urm.action.ActionScope;
import org.urm.action.database.DatabaseCommand;
import org.urm.common.action.CommandMeta;
import org.urm.common.meta.DatabaseCommandMeta;
import org.urm.engine.ServerEngine;
import org.urm.engine.action.CommandMethod;
import org.urm.engine.action.CommandExecutor;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.ReleaseDelivery;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.Types.*;

public class DatabaseCommandExecutor extends CommandExecutor {

	DatabaseCommand impl;
	MetaEnv env;
	MetaEnvSegment sg;
	
	String propertyBasedMethods;
	
	public static DatabaseCommandExecutor createExecutor( ServerEngine engine ) throws Exception {
		DatabaseCommandMeta commandInfo = new DatabaseCommandMeta( engine.optionsMeta );
		return( new DatabaseCommandExecutor( engine , commandInfo ) );
	}
		
	private DatabaseCommandExecutor( ServerEngine engine , CommandMeta commandInfo ) throws Exception {
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
	public boolean runExecutorImpl( ActionBase action , CommandMethod method ) {
		// log action and run 
		boolean res = super.runMethod( action , method );
		return( res );
	}

	private ActionScope getIndexScope( ActionBase action , Dist dist , int posFrom ) throws Exception {
		String[] INDEXES = getArgList( action , posFrom );
		ActionReleaseScopeMaker maker = new ActionReleaseScopeMaker( action , dist );
		maker.addScopeReleaseDatabaseManualItems( INDEXES );
		return( maker.getScope() );
	}
	
	private class GetReleaseScripts extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		Meta meta = action.getContextMeta();
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		String[] DELIVERIES = getArgList( action , 1 );
		
		ActionReleaseScopeMaker maker = new ActionReleaseScopeMaker( action , dist );
		maker.addScopeReleaseCategory( VarCATEGORY.DB , DELIVERIES );
		ActionScope scope = maker.getScope();
		impl.getReleaseScripts( action , scope , dist );
	}
	}
	
	private class InitDB extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		String SERVER = getRequiredArg( action , 0 , "SERVER" );
		int node = getIntArg( action , 1 , -1 );
		impl.initDatabase( action , SERVER , node );
	}
	}

	private class ApplyManual extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		Meta meta = action.getContextMeta();
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		String SERVER = getRequiredArg( action , 1 , "DBSERVER" );
		MetaEnvServer server = action.context.sg.getServer( action , SERVER );
		ActionScope scope = getIndexScope( action , dist , 2 );
		impl.applyManual( action , scope , dist , server );
	}
	}

	private class ApplyAutomatic extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		Meta meta = action.getContextMeta();
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
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

	private class ManageRelease extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		Meta meta = action.getContextMeta();
		DistRepository repo = action.artefactory.getDistRepository( action , meta );
		String RELEASEVER = repo.getReleaseVerByLabel( action , RELEASELABEL );
		
		String CMD = getRequiredArg( action , 1 , "CMD" );
		String DELIVERY = getRequiredArg( action , 2 , "delivery" );
		
		MetaDistrDelivery delivery = null;
		String indexScope = null;
		if( DELIVERY.equals( "all" ) )
			checkNoArgs( action , 3 );
		else {
			MetaDistr distr = meta.getDistr( action );
			delivery = distr.getDelivery( action , DELIVERY );
			indexScope = getRequiredArg( action , 3 , "mask" );
			checkNoArgs( action , 4 );
		}
		
		impl.manageRelease( action , meta , RELEASEVER , delivery , CMD , indexScope );
	}
	}
	
	private class ImportDB extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		String SERVER = getRequiredArg( action , 0 , "SERVER" );
		String CMD = getRequiredArg( action , 1 , "CMD" );
		String SCHEMA = getArg( action , 2 );
		impl.importDatabase( action , SERVER , CMD , SCHEMA );
	}
	}
	
	private class ExportDB extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		String SERVER = getRequiredArg( action , 0 , "SERVER" );
		String CMD = getRequiredArg( action , 1 , "CMD" );
		String SCHEMA = getArg( action , 2 );
		impl.exportDatabase( action , SERVER , CMD , SCHEMA );
	}
	}
	
}
