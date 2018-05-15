package org.urm.db.engine;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.env.DBEnvData;
import org.urm.db.product.DBProductData;
import org.urm.db.release.DBReleaseData;
import org.urm.engine.Engine;
import org.urm.engine.AuthService;
import org.urm.engine.action.ActionInit;
import org.urm.engine.data.EngineDirectory;
import org.urm.engine.data.EngineMirrors;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.products.EngineProductRevisions;
import org.urm.engine.transaction.EngineTransaction;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.loader.EngineLoader;
import org.urm.meta.product.ProductMeta;

public class DBEngineProducts {

	public static AppProduct createProduct( EngineTransaction transaction , AppSystem system , String name , String desc , String path ) throws Exception {
		Engine engine = transaction.engine;
		DBConnection c = transaction.getConnection();
		ActionInit action = transaction.getAction();
		EngineDirectory directory = system.directory;
		
		transaction.changeMirrors( action.getEngineMirrors() );
		EngineMirrors mirrors = transaction.getTransactionMirrors();
		
		AppProduct product = directory.findProduct( name );
		boolean forceClearMeta = false; 
		ProductMeta storageOld = null;
		if( product != null ) {
			EngineProduct ep = product.findEngineProduct();
			storageOld = ep.findDraftRevision();
			if( storageOld != null ) {
				forceClearMeta = true;
				if( storageOld.isExists() ) {
					DBEnvData.dropEnvData( c , storageOld );
					DBProductData.dropProductData( c , storageOld );
				}
			}
			
			DBEngineDirectory.deleteProduct( transaction , directory , product , true , false , false );
			DBEngineMirrors.deleteProductResources( transaction , mirrors , product , forceClearMeta , false , false );
		}
		
		product = DBEngineDirectory.createProduct( transaction , directory , system , name , desc , path );
		DBEngineMirrors.createProductMirrors( transaction , mirrors , product );
		
		EngineLoader loader = engine.createLoader( transaction );
		ProductMeta storage = loader.createProduct( product , forceClearMeta , false );
		if( storage == null )
			Common.exit0( _Error.UnableCreateProduct0 , "Unable to create product" );
		
		if( storageOld != null )
			transaction.requestReplaceProductMetadata( storage , storageOld );
		else {
			transaction.createProduct( product );
			transaction.createProductMetadata( product , storage );
		}
		
		return( product );
	}
	
	public static void deleteProduct( EngineTransaction transaction , AppProduct product , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
		DBConnection c = transaction.getConnection();
		ActionInit action = transaction.getAction();
		AuthService auth = action.getServerAuth();
		EngineMirrors mirrors = action.getActiveMirrors();
		
		EngineProduct ep = product.getEngineProduct();
		EngineProductRevisions revisions = ep.getRevisions();
		
		if( !transaction.requestDeleteProduct( product ) )
			Common.exitUnexpected();
		
		for( ProductMeta storage : revisions.getRevisions() ) {
			DBEnvData.dropEnvData( c , storage );
			DBReleaseData.dropAllMeta( c , storage );
			DBProductData.dropProductData( c , storage );
		}
		
		DBEngineAuth.deleteProductAccess( c , auth , product );
		DBEngineMirrors.deleteProductResources( transaction , mirrors , product , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
		DBEngineDirectory.deleteProduct( transaction , product.directory , product , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
	}
	
}
