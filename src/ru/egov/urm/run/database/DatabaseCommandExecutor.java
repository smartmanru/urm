package ru.egov.urm.run.database;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaEnv;
import ru.egov.urm.meta.MetaEnvDC;
import ru.egov.urm.meta.MetaEnvServer;
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
		super.defineAction( CommandAction.newAction( new getReleaseScripts() , "getsql" , "get database release content" , cmdOpts , "./getsql.sh [OPTIONS] {all|<deliveries>}" ) );
		cmdOpts = "";
		super.defineAction( CommandAction.newAction( new applyManual() , "dbmanual" , "apply manual scipt" , cmdOpts , "./dbmanual.sh [OPTIONS] <RELEASELABEL> <DBSERVER> {all|<indexes>}" ) );
		
		envMethods = "sqlmanual";
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
	
	private class getReleaseScripts extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		ActionScope scope = getReleaseScope( action );
		impl.getReleaseScripts( action , scope , scope.release );
	}
	}

	private class applyManual extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String RELEASELABEL = options.getRequiredArg( action , 0 , "RELEASELABEL" );
		DistStorage dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		String SERVER = options.getRequiredArg( action , 1 , "DBSERVER" );
		MetaEnvServer server = action.meta.dc.getServer( action , SERVER );
		ActionScope scope = getIndexScope( action , dist , 2 );
		impl.applyManual( action , scope , scope.release , server );
	}
	}

}
