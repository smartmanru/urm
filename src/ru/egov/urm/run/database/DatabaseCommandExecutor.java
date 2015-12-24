package ru.egov.urm.run.database;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaEnv;
import ru.egov.urm.meta.MetaEnvDC;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaReleaseDelivery;
import ru.egov.urm.meta.Metadata.VarCATEGORY;
import ru.egov.urm.run.ActionInit;
import ru.egov.urm.run.ActionScope;
import ru.egov.urm.run.CommandAction;
import ru.egov.urm.run.CommandBuilder;
import ru.egov.urm.run.CommandExecutor;
import ru.egov.urm.storage.DistStorage;

public class DatabaseCommandExecutor extends CommandExecutor {

	DatabaseCommandImpl impl;
	MetaEnv env;
	MetaEnvDC dc;
	
	String envMethods;
	
	public DatabaseCommandExecutor( CommandBuilder builder ) {
		super( builder );
		
		String cmdOpts = "";
		cmdOpts = "";
		super.defineAction( CommandAction.newAction( new GetReleaseScripts() , "getsql" , "get database release content" , cmdOpts , "./getsql.sh [OPTIONS] {all|<deliveries>}" ) );
		cmdOpts = "";
		super.defineAction( CommandAction.newAction( new ApplyManual() , "dbmanual" , "apply manual scripts under system account" , cmdOpts , "./dbmanual.sh [OPTIONS] <RELEASELABEL> <DBSERVER> {all|<indexes>}" ) );
		cmdOpts = "GETOPT_DBMODE, GETOPT_DB, GETOPT_DBTYPE, GETOPT_DBALIGNED";
		super.defineAction( CommandAction.newAction( new ApplyAutomatic() , "dbapply" , "apply application scripts and load data files" , cmdOpts , "./dbapply.sh [OPTIONS] <RELEASELABEL> {all|<delivery> {all|<mask>}} (mask is distributive file mask)" ) );
		cmdOpts = "";
		super.defineAction( CommandAction.newAction( new ManageRelease() , "manage" , "manage accounting information" , cmdOpts , "./manage.sh [OPTIONS] <RELEASELABEL> <print|correct|rollback> [{all|<indexes>}]" ) );
		cmdOpts = "";
		super.defineAction( CommandAction.newAction( new ImportDB() , "import" , "import specified in etc/datapump/file dump to database" , cmdOpts , "./export.sh [OPTIONS] <datapump-file> {all|meta|data} [schema]" ) );
		cmdOpts = "";
		super.defineAction( CommandAction.newAction( new ExportDB() , "export" , "export specified in etc/datapump/file dump from database" , cmdOpts , "./export.sh [OPTIONS] <datapump-file> {all|meta|data} [schema]" ) );
		
		envMethods = "dbmanual dbapply";
	}
	
	public boolean run( ActionInit action ) {
		try {
			// create implementation
			impl = new DatabaseCommandImpl( this );
			meta.loadDistr( action );
			meta.loadSources( action );
			
			if( Common.checkPartOfSpacedList( commandAction.name , envMethods ) ) 
				meta.loadEnv( action , action.context.env , action.context.dc , true );
		}
		catch( Throwable e ) {
			action.log( e );
			return( false );
		}
		
		// log action and run 
		boolean res = super.runMethod( action , commandAction );
		return( res );
	}

	private ActionScope getReleaseScope( ActionInit action ) throws Exception {
		String RELEASELABEL = options.getRequiredArg( action , 0 , "RELEASELABEL" );
		DistStorage dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		String[] DELIVERIES = options.getArgList( 1 );
		return( ActionScope.getReleaseCategoryScope( action , dist , VarCATEGORY.DB , DELIVERIES ) );
	}
	
	private ActionScope getIndexScope( ActionInit action , DistStorage dist , int posFrom ) throws Exception {
		String[] INDEXES = options.getArgList( posFrom );
		return( ActionScope.getDatabaseManualItemsScope( action , dist , INDEXES ) );
	}
	
	private class GetReleaseScripts extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		ActionScope scope = getReleaseScope( action );
		impl.getReleaseScripts( action , scope , scope.release );
	}
	}

	private class ApplyManual extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = options.getRequiredArg( action , 0 , "RELEASELABEL" );
		DistStorage dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		String SERVER = options.getRequiredArg( action , 1 , "DBSERVER" );
		MetaEnvServer server = action.meta.dc.getServer( action , SERVER );
		ActionScope scope = getIndexScope( action , dist , 2 );
		impl.applyManual( action , scope , dist , server );
	}
	}

	private class ApplyAutomatic extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = options.getRequiredArg( action , 0 , "RELEASELABEL" );
		DistStorage dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		String DELIVERY = options.getRequiredArg( action , 1 , "delivery" );
		
		MetaReleaseDelivery delivery = null;
		String indexScope = null;
		if( DELIVERY.equals( "all" ) )
			options.checkNoArgs( action , 2 );
		else {
			delivery = dist.info.getDelivery( action , DELIVERY );
			indexScope = options.getRequiredArg( action , 2 , "mask" );
			options.checkNoArgs( action , 3 );
		}
		
		impl.applyAutomatic( action , dist , delivery , indexScope );
	}
	}

	private class ManageRelease extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = options.getRequiredArg( action , 0 , "RELEASELABEL" );
		DistStorage dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		ActionScope scope = getIndexScope( action , dist , 1 );
		impl.manageRelease( action , scope , scope.release );
	}
	}
	
	private class ImportDB extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String SPECFILE = options.getRequiredArg( action , 0 , "SPECFILE" );
		String CMD = options.getRequiredArg( action , 1 , "CMD" );
		String SCHEMA = options.getArg( 2 );
		impl.importDatabase( action , SPECFILE , CMD , SCHEMA );
	}
	}
	
	private class ExportDB extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String SPECFILE = options.getRequiredArg( action , 0 , "SPECFILE" );
		String CMD = options.getRequiredArg( action , 1 , "CMD" );
		String SCHEMA = options.getArg( 2 );
		impl.exportDatabase( action , SPECFILE , CMD , SCHEMA );
	}
	}
	
}
