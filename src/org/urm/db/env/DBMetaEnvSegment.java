package org.urm.db.env;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.EngineTransaction;
import org.urm.meta.EngineLoader;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvStartInfo;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Node;

public class DBMetaEnvSegment {

	public static String ELEMENT_DEPLOYMENT = "deployment";
	public static String ELEMENT_STARTORDER = "startorder";
	public static String ELEMENT_SERVER = "server";
	
	public static MetaEnvSegment importxml( EngineLoader loader , ProductMeta storage , MetaEnv env , Node root ) throws Exception {
		MetaEnvSegment sg = new MetaEnvSegment( storage.meta , env );
		
		loader.trace( "import meta env segment object, name=" + env.NAME );

		importxmlMain( loader , storage , env , sg , root );
		importxmlServers( loader , storage , env , sg , root );
		importxmlStartOrder( loader , storage , env , sg , root );
		importxmlDeployment( loader , storage , env , sg , root );
		
 		return( sg );
	}

	public static void importxmlMain( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , Node root ) throws Exception {
	}
	
	public static void importxmlServers( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , Node root ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_SERVER );
		if( items == null )
			return;
		
		for( Node node : items ) {
			MetaEnvServer server = DBMetaEnvServer.importxml( loader , storage , env , sg , node );
			sg.addServer( server );
		}
	}
	
	public static void importxmlStartOrder( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , Node root ) throws Exception {
	}
	
	public static void importxmlDeployment( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , Node root ) throws Exception {
	}
	
	public static MetaEnvSegment createSegment( EngineTransaction transaction , ProductMeta storage , MetaEnv env , String name , String desc , Integer dcId ) throws Exception {
		Common.exitUnexpected();
		return( null );
	}

	public static void modifySegment( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , String name , String desc , Integer dcId ) throws Exception {
		Common.exitUnexpected();
	}

	public static void setSegmentBaseline( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , Integer sgId ) throws Exception {
		Common.exitUnexpected();
	}

	public static void setSegmentOffline( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , boolean offline ) throws Exception {
		Common.exitUnexpected();
	}

	public static void deleteSegment( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvSegment sg ) throws Exception {
		Common.exitUnexpected();
	}

	public static void setStartInfo( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , MetaEnvStartInfo startInfo ) throws Exception {
		Common.exitUnexpected();
	}

	public static void updateCustomProperties( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvSegment sg ) throws Exception {
		Common.exitUnexpected();
	}
	
}
