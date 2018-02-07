package org.urm.db.env;

import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.EngineTransaction;
import org.urm.meta.engine.HostAccount;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.product.ProductMeta;

public class DBMetaEnvServerNode {

	public static MetaEnvServerNode createNode( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServer server , int pos , DBEnumNodeType nodeType , HostAccount account ) throws Exception {
		Common.exitUnexpected();
		return( null );
	}
	
	public static void modifyNode( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServerNode node , int pos , DBEnumNodeType nodeType , HostAccount account ) throws Exception {
		Common.exitUnexpected();
	}
	
	public static void deleteNode( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServerNode node ) throws Exception {
		Common.exitUnexpected();
	}
	
	public static void setOffline( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServerNode node , boolean offline ) throws Exception {
		Common.exitUnexpected();
	}
	
	public static void updateCustomProperties( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServerNode node ) throws Exception {
		Common.exitUnexpected();
	}
	
}
