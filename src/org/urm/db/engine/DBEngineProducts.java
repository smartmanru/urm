package org.urm.db.engine;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.product.DBProductData;
import org.urm.engine.Engine;
import org.urm.engine.EngineTransaction;
import org.urm.engine.action.ActionInit;
import org.urm.meta.EngineLoader;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.engine.EngineAuth;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.EngineMirrors;
import org.urm.meta.product.ProductMeta;

public class DBEngineProducts {

	public static AppProduct createProduct( EngineTransaction transaction , AppSystem system , String name , String desc , String path , boolean forceClearMeta , boolean forceClearDist ) throws Exception {
		Engine engine = transaction.engine;
		DBConnection c = transaction.getConnection();
		ActionInit action = transaction.getAction();
		EngineDirectory directory = system.directory;
		
		transaction.changeMirrors( action.getServerMirrors() );
		EngineMirrors mirrors = transaction.getTransactionMirrors();
		
		AppProduct product = directory.findProduct( name );
		boolean change = false;
		if( product != null ) {
			if( !forceClearMeta )
				Common.exitUnexpected();

			change = true;
			if( !transaction.recreateMetadata( product.storage.meta ) )
				Common.exitUnexpected();
			
			DBProductData.dropProductData( c , product.storage );
			DBEngineDirectory.deleteProduct( transaction , directory , product , true , false , false );
			DBEngineMirrors.deleteProductResources( transaction , mirrors , product , forceClearMeta , false , false );
		}
		
		product = DBEngineDirectory.createProduct( transaction , directory , system , name , desc , path );
		DBEngineMirrors.createProductMirrors( transaction , mirrors , product );
		
		EngineLoader loader = engine.createLoader( transaction );
		ProductMeta storage = loader.createProduct( product , forceClearMeta , forceClearDist );
		
		if( change )
			transaction.replaceProductMetadata( storage );
		else
			transaction.createProductMetadata( storage );
		
		return( product );
	}
	
	public static void deleteProduct( EngineTransaction transaction , AppProduct product , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
		DBConnection c = transaction.getConnection();
		ActionInit action = transaction.getAction();
		EngineAuth auth = action.getServerAuth();
		EngineMirrors mirrors = action.getActiveMirrors();
		ProductMeta storage = product.storage;

		if( !transaction.deleteMetadata( storage.meta ) )
			Common.exitUnexpected();
		if( !transaction.changeMirrors( mirrors ) )
			Common.exitUnexpected();

		DBEngineAuth.deleteProductAccess( c , auth , product );
		DBProductData.dropProductData( c , storage );
		DBEngineMirrors.deleteProductResources( transaction , mirrors , product , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
		DBEngineDirectory.deleteProduct( transaction , product.directory , product , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
	}
	
}
