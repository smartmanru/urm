package org.urm.db.product;

import org.urm.meta.EngineLoader;

public class DBProductData {

	public static void dropProductData( EngineLoader loader ) throws Exception {
		dropProductReleasesData( loader );
		dropProductDesignData( loader );
		dropProductEnvData( loader );
		dropProductCoreData( loader );
	}

	public static void dropProductReleasesData( EngineLoader loader ) throws Exception {
	}
	
	public static void dropProductDesignData( EngineLoader loader ) throws Exception {
	}
	
	public static void dropProductEnvData( EngineLoader loader ) throws Exception {
	}
	
	public static void dropProductCoreData( EngineLoader loader ) throws Exception {
	}
	
}
