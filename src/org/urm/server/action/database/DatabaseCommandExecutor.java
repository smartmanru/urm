package org.urm.server.action.database;

import org.urm.common.Common;
import org.urm.dist.Dist;
import org.urm.dist.DistRepository;
import org.urm.dist.ReleaseDelivery;
import org.urm.meta.MetaDistrDelivery;
import org.urm.meta.MetaEnv;
import org.urm.meta.MetaEnvDC;
import org.urm.meta.MetaEnvServer;
import org.urm.meta.Metadata.VarCATEGORY;
import org.urm.server.action.ActionInit;
import org.urm.server.action.ActionScope;
import org.urm.server.action.CommandAction;
import org.urm.server.action.CommandBuilder;
import org.urm.server.action.CommandExecutor;

public class DatabaseCommandExecutor extends CommandExecutor {

	public static String NAME = "database";
	DatabaseCommand impl;
	MetaEnv env;
	MetaEnvDC dc;
	
	String propertyBasedMethods;
	
	public DatabaseCommandExecutor( CommandBuilder builder ) {
		super( builder , NAME );
		
		String cmdOpts = "";
		cmdOpts = "GETOPT_DC, GETOPT_DBPASSWORD";
		super.defineAction( CommandAction.newAction( new InitDB() , "initdb" , false , "prepare database for operation" , cmdOpts , "./initdb.sh [OPTIONS] <server>" ) );
		cmdOpts = "GETOPT_DBPASSWORD";
		super.defineAction( CommandAction.newAction( new GetReleaseScripts() , "getsql" , true , "get database release content" , cmdOpts , "./getsql.sh [OPTIONS] {all|<deliveries>}" ) );
		cmdOpts = "GETOPT_DC, GETOPT_DBPASSWORD";
		super.defineAction( CommandAction.newAction( new ApplyManual() , "dbmanual" , false , "apply manual scripts under system account" , cmdOpts , "./dbmanual.sh [OPTIONS] <RELEASELABEL> <DBSERVER> {all|<indexes>}" ) );
		cmdOpts = "GETOPT_DBPASSWORD, GETOPT_DBMODE, GETOPT_DC, GETOPT_DB, GETOPT_DBTYPE, GETOPT_DBALIGNED";
		super.defineAction( CommandAction.newAction( new ApplyAutomatic() , "dbapply" , false , "apply application scripts and load data files" , cmdOpts , "./dbapply.sh [OPTIONS] <RELEASELABEL> {all|<delivery> {all|<mask>}} (mask is distributive file mask)" ) );
		cmdOpts = "GETOPT_DC, GETOPT_DBPASSWORD, GETOPT_DB";
		super.defineAction( CommandAction.newAction( new ManageRelease() , "manage" , false , "manage accounting information" , cmdOpts , "./manage.sh [OPTIONS] <RELEASELABEL> <status|correct|rollback|drop> [{all|<indexes>}]" ) );
		cmdOpts = "GETOPT_DC, GETOPT_DBPASSWORD";
		super.defineAction( CommandAction.newAction( new ImportDB() , "import" , false , "import specified in etc/datapump/file dump to database" , cmdOpts , "./import.sh [OPTIONS] <server> {all|meta|data} [schema]" ) );
		cmdOpts = "GETOPT_DC, GETOPT_DBPASSWORD";
		super.defineAction( CommandAction.newAction( new ExportDB() , "export" , false , "export specified in etc/datapump/file dump from database" , cmdOpts , "./export.sh [OPTIONS] <server> {all|meta|data [schema]}" ) );
		
		propertyBasedMethods = "initdb dbmanual dbapply import";
	}
	
	public boolean run( ActionInit action ) {
		try {
			// create implementation
			impl = new DatabaseCommand();
			meta.loadDistr( action );
			meta.loadSources( action );
			
			boolean loadProps = Common.checkPartOfSpacedList( commandAction.name , propertyBasedMethods ); 
			action.context.loadEnv( action , loadProps );
		}
		catch( Throwable e ) {
			action.log( e );
			return( false );
		}
		
		// log action and run 
		boolean res = super.runMethod( action , commandAction );
		return( res );
	}

	private ActionScope getIndexScope( ActionInit action , Dist dist , int posFrom ) throws Exception {
		String[] INDEXES = options.getArgList( posFrom );
		return( ActionScope.getDatabaseManualItemsScope( action , dist , INDEXES ) );
	}
	
	private class GetReleaseScripts extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = options.getRequiredArg( action , 0 , "RELEASELABEL" );
		Dist dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		String[] DELIVERIES = options.getArgList( 1 );
		ActionScope scope = ActionScope.getReleaseCategoryScope( action , dist , VarCATEGORY.DB , DELIVERIES );
		impl.getReleaseScripts( action , scope , dist );
	}
	}
	
	private class InitDB extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String SERVER = options.getRequiredArg( action , 0 , "SERVER" );
		int node = options.getIntArg( 1 , -1 );
		impl.initDatabase( action , SERVER , node );
	}
	}

	private class ApplyManual extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = options.getRequiredArg( action , 0 , "RELEASELABEL" );
		Dist dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		String SERVER = options.getRequiredArg( action , 1 , "DBSERVER" );
		MetaEnvServer server = action.context.dc.getServer( action , SERVER );
		ActionScope scope = getIndexScope( action , dist , 2 );
		impl.applyManual( action , scope , dist , server );
	}
	}

	private class ApplyAutomatic extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = options.getRequiredArg( action , 0 , "RELEASELABEL" );
		Dist dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		String DELIVERY = options.getRequiredArg( action , 1 , "delivery" );
		
		ReleaseDelivery delivery = null;
		String indexScope = null;
		if( DELIVERY.equals( "all" ) )
			options.checkNoArgs( action , 2 );
		else {
			delivery = dist.release.getDelivery( action , DELIVERY );
			indexScope = options.getRequiredArg( action , 2 , "mask" );
			options.checkNoArgs( action , 3 );
		}
		
		impl.applyAutomatic( action , dist , delivery , indexScope );
	}
	}

	private class ManageRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = options.getRequiredArg( action , 0 , "RELEASELABEL" );
		DistRepository repo = action.artefactory.getDistRepository( action );
		String RELEASEVER = repo.getReleaseVerByLabel( action , RELEASELABEL );
		
		String CMD = options.getRequiredArg( action , 1 , "CMD" );
		String DELIVERY = options.getRequiredArg( action , 2 , "delivery" );
		
		MetaDistrDelivery delivery = null;
		String indexScope = null;
		if( DELIVERY.equals( "all" ) )
			options.checkNoArgs( action , 3 );
		else {
			delivery = action.meta.distr.getDelivery( action , DELIVERY );
			indexScope = options.getRequiredArg( action , 3 , "mask" );
			options.checkNoArgs( action , 4 );
		}
		
		impl.manageRelease( action , RELEASEVER , delivery , CMD , indexScope );
	}
	}
	
	private class ImportDB extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String SERVER = options.getRequiredArg( action , 0 , "SERVER" );
		String CMD = options.getRequiredArg( action , 1 , "CMD" );
		String SCHEMA = options.getArg( 2 );
		impl.importDatabase( action , SERVER , CMD , SCHEMA );
	}
	}
	
	private class ExportDB extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String SERVER = options.getRequiredArg( action , 0 , "SERVER" );
		String CMD = options.getRequiredArg( action , 1 , "CMD" );
		String SCHEMA = options.getArg( 2 );
		impl.exportDatabase( action , SERVER , CMD , SCHEMA );
	}
	}
	
}
