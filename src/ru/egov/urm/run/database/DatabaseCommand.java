package ru.egov.urm.run.database;

import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaReleaseDelivery;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScope;
import ru.egov.urm.run.CommandOptions.SQLMODE;
import ru.egov.urm.storage.DistStorage;

public class DatabaseCommand {

	public DatabaseCommand() {
	}

	public void initDatabase( ActionBase action , String SERVER ) throws Exception {
		MetaEnvServer server = action.meta.dc.getServer( action , SERVER );
		ActionInitDatabase ma = new ActionInitDatabase( action , null , server );
		ma.runSimple();
	}

	public void getReleaseScripts( ActionBase action , ActionScope scope , DistStorage dist ) throws Exception {
		ActionGetDB ma = new ActionGetDB( action , null , dist );
		ma.runAll( scope );
	}

	public void applyManual( ActionBase action , ActionScope scope , DistStorage dist , MetaEnvServer server ) throws Exception {
		dist.open( action );
		
		ActionApplyManual ma = new ActionApplyManual( action , null , dist , server );
		ma.runAll( scope );
	}

	public void applyAutomatic( ActionBase action , DistStorage dist , MetaReleaseDelivery delivery , String indexScope ) throws Exception {
		dist.open( action );
		
		String deliveryInfo = ( delivery != null )? delivery.distDelivery.NAME : "(all)";
		String itemsInfo = ( indexScope != null )? indexScope : "(all)";
		
		String op = null;
		if( action.context.CTX_DBMODE == SQLMODE.ANYWAY )
			op = "all";
		else if( action.context.CTX_DBMODE == SQLMODE.APPLY )
			op = "new";
		else if( action.context.CTX_DBMODE == SQLMODE.CORRECT )
			op = "failed";
		else 
			action.exit( "database mode is not set" );
		
		action.log( "apply database changes (" + op + ") release=" + dist.RELEASEDIR + ", delivery=" + deliveryInfo + ", items=" + itemsInfo );
		ActionApplyAutomatic ma = new ActionApplyAutomatic( action , null , dist , delivery , indexScope );
		ActionScope scope = ActionScope.getEnvDatabaseScope( action , dist );
		ma.runAll( scope );
	}

	public void manageRelease( ActionBase action , ActionScope scope , DistStorage dist ) throws Exception {
		action.exitNotImplemented();
	}

	public void importDatabase( ActionBase action , String SERVER , String CMD , String SCHEMA ) throws Exception {
		MetaEnvServer server = action.meta.dc.getServer( action , SERVER );
		ActionImportDatabase ma = new ActionImportDatabase( action , null , server , CMD , SCHEMA );
		ma.runSimple();
	}

	public void exportDatabase( ActionBase action , String SERVER , String CMD , String SCHEMA ) throws Exception {
		MetaEnvServer server = action.meta.dc.getServer( action , SERVER );
		ActionExportDatabase ma = new ActionExportDatabase( action , null , server , CMD , SCHEMA );
		ma.runSimple();
	}

}
