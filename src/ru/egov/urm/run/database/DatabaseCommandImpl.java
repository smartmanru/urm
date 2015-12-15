package ru.egov.urm.run.database;

import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaReleaseDelivery;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScope;
import ru.egov.urm.run.CommandExecutor;
import ru.egov.urm.storage.DistStorage;

public class DatabaseCommandImpl {

	CommandExecutor executor;
	
	public DatabaseCommandImpl( CommandExecutor executor ) {
		this.executor = executor;
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
		action.log( "apply database changes release=" + dist.RELEASEDIR + ", delivery=" + deliveryInfo + ", scope=" + indexScope );
		ActionApplyAutomatic ma = new ActionApplyAutomatic( action , null , dist , delivery , indexScope );
		ActionScope scope = ActionScope.getEnvDatabaseScope( action , dist );
		ma.runAll( scope );
	}

}
