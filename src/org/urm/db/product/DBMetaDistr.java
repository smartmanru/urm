package org.urm.db.product;

import org.urm.engine.EngineTransaction;
import org.urm.meta.ProductMeta;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaProductDoc;
import org.urm.meta.product.MetaProductUnit;

public class DBMetaDistr {

	public static void deleteUnit( EngineTransaction transaction , ProductMeta storage , MetaDistr distr , MetaProductUnit unit ) throws Exception {
	}

	public static void deleteDocument( EngineTransaction transaction , ProductMeta storage , MetaDistr distr , MetaProductDoc doc ) throws Exception {
	}

	public static void deleteDatabaseSchema( EngineTransaction transaction , ProductMeta storage , MetaDistr distr , MetaDatabaseSchema schema ) throws Exception {
	}
	
}
