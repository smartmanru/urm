package org.urm.db.env;

import org.urm.common.Common;
import org.urm.db.core.DBEnums.DBEnumOSType;
import org.urm.db.core.DBEnums.DBEnumServerAccessType;
import org.urm.db.core.DBEnums.DBEnumServerRunType;
import org.urm.engine.EngineTransaction;
import org.urm.meta.EngineLoader;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerDeployment;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Node;

public class DBMetaEnvServer {

	public static String ELEMENT_NODE = "node";
	public static String ELEMENT_PLATFORM = "platform";
	public static String ELEMENT_DEPLOY = "deploy";

	public static MetaEnvServer importxml( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , Node root ) throws Exception {
		MetaEnvServer server = new MetaEnvServer( storage.meta , sg );
		
		loader.trace( "import meta env segment object, name=" + env.NAME );

		importxmlMain( loader , storage , env , server , root );
		importxmlNodes( loader , storage , env , server , root );
		importxmlBase( loader , storage , env , server , root );
		
 		return( server );
	}

	public static void importxmlMain( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvServer server , Node root ) throws Exception {
	}
	
	public static void importxmlNodes( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvServer server , Node root ) throws Exception {
	}
	
	public static void importxmlBase( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvServer server , Node root ) throws Exception {
	}
	
	public static MetaEnvServer createServer( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , String name , String desc , DBEnumOSType osType , DBEnumServerRunType runType , DBEnumServerAccessType accessType , String sysname ) throws Exception {
		Common.exitUnexpected();
		return( null );
	}
	
	public static void modifyServer( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServer server , String name , String desc , DBEnumOSType osType , DBEnumServerRunType runType , DBEnumServerAccessType accessType , String sysname ) throws Exception {
		Common.exitUnexpected();
	}
	
	public static void deleteServer( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServer server ) throws Exception {
		Common.exitUnexpected();
	}
	
	public static void setServerBaseline( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServer server , Integer baselineId ) throws Exception {
		Common.exitUnexpected();
	}
	
	public static void setServerBaseItem( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServer server , Integer baseItemId ) throws Exception {
		Common.exitUnexpected();
	}
	
	public static void setServerOffline( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServer server , boolean offline ) throws Exception {
		Common.exitUnexpected();
	}
	
	public static void setDeployments( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServer server , MetaEnvServerDeployment[] deployments ) throws Exception {
		Common.exitUnexpected();
	}
	
	public static void updateCustomProperties( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServer server ) throws Exception {
		Common.exitUnexpected();
	}
	
	public static void updateExtraProperties( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServer server ) throws Exception {
		Common.exitUnexpected();
	}
	
}
