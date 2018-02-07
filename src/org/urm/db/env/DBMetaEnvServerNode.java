package org.urm.db.env;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.DBEnumNodeType;
import org.urm.db.core.DBEnums.DBEnumOSType;
import org.urm.db.core.DBEnums.DBEnumServerAccessType;
import org.urm.db.core.DBEnums.DBEnumServerRunType;
import org.urm.engine.EngineTransaction;
import org.urm.meta.engine.HostAccount;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMetaEnvServerNode {

	public void load( ActionBase action , Node node ) throws Exception {
		if( !super.initCreateStarted( server.getProperties() ) )
			return;

		super.loadFromNodeAttributes( action , node , false );
		scatterProperties( action );
		super.loadFromNodeElements( action , node , true );
		super.resolveRawProperties();
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		super.saveSplit( doc , root );
	}
	
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
