package org.urm.action.database;

import org.urm.action.ActionBase;
import org.urm.action.ActionEnvScopeMaker;
import org.urm.action.ActionScope;
import org.urm.common.action.CommandOptions.SQLMODE;
import org.urm.common.action.CommandMethodMeta.SecurityAction;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.ReleaseDistScopeDelivery;
import org.urm.engine.status.ScopeState;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.system.AppProduct;

public class DatabaseCommand {

	public DatabaseCommand() {
	}

	public void initDatabase( ScopeState parentState , ActionBase action , String SERVER , int nodePos ) throws Exception {
		MetaEnvServer server = action.context.sg.getServer( SERVER );
		MetaEnvServerNode node;
		if( nodePos < 0 )
			node = server.getMasterNode();
		else
			node = server.getNodeByPos( nodePos );
			
		ActionInitDatabase ma = new ActionInitDatabase( action , null , server , node );
		ma.runSimpleEnv( parentState , server.sg.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void getReleaseScripts( ScopeState parentState , ActionBase action , ActionScope scope , Dist dist ) throws Exception {
		LocalFolder downloadFolder = action.artefactory.getWorkFolder( action , "download" );
		downloadFolder.recreateThis( action );
		ActionGetDB ma = new ActionGetDB( action , null , dist , downloadFolder , action.context.CTX_DIST );
		ma.runAll( parentState , scope , null , SecurityAction.ACTION_CODEBASE , false );
	}

	public void applyManual( ScopeState parentState , ActionBase action , ActionScope scope , Dist dist , MetaEnvServer server ) throws Exception {
		dist.openForUse( action );
		
		ActionApplyManual ma = new ActionApplyManual( action , null , dist , server );
		ma.runAll( parentState , scope , server.sg.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void applyAutomatic( ScopeState parentState , ActionBase action , Dist dist , ReleaseDistScopeDelivery delivery , String indexScope ) throws Exception {
		dist.openForUse( action );
		
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
			action.exit0( _Error.DatabaseModeNotSet0 , "database mode is not set" );
		
		action.info( "apply database changes (" + op + ") release=" + dist.RELEASEDIR + ", delivery=" + deliveryInfo + ", items=" + itemsInfo );
		
		ActionEnvScopeMaker maker = new ActionEnvScopeMaker( action , action.context.env );
		maker.addScopeEnvDatabase( dist.release );
		
		ActionApplyAutomatic ma = new ActionApplyAutomatic( action , null , dist , delivery , indexScope );
		ma.runAll( parentState , maker.getScope() , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void manageRelease( ScopeState parentState , ActionBase action , Meta meta , String RELEASEVER , MetaDistrDelivery delivery , String CMD , String indexScope ) throws Exception {
		ActionEnvScopeMaker maker = new ActionEnvScopeMaker( action , action.context.env );
		maker.addScopeEnvDatabase( null );
		
		ActionManageRegistry ma = new ActionManageRegistry( action , null , RELEASEVER , CMD , delivery , indexScope );
		ma.runAll( parentState , maker.getScope() , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void importDatabase( ScopeState parentState , ActionBase action , String TASK , String CMD , String SCHEMA ) throws Exception {
		AppProduct product = action.getContextProduct();
		ActionImportDatabase ma = new ActionImportDatabase( action , null , product , TASK , CMD , SCHEMA );
		ma.runSimpleEnv( parentState , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void exportDatabase( ScopeState parentState , ActionBase action , String TASK , String CMD , String SCHEMA ) throws Exception {
		AppProduct product = action.getContextProduct();
		ActionExportDatabase ma = new ActionExportDatabase( action , null , product , TASK , CMD , SCHEMA );
		ma.runSimpleEnv( parentState , action.context.env , SecurityAction.ACTION_SECURED , true );
	}

}
