package org.urm.db.env;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.core.DBEnums.DBEnumOSType;
import org.urm.db.core.DBEnums.DBEnumServerAccessType;
import org.urm.db.core.DBEnums.DBEnumServerRunType;
import org.urm.engine.EngineTransaction;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerDeployment;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMetaEnvServer {

	public void load( ActionBase action , Node node ) throws Exception {
		if( !super.initCreateStarted( sg.getProperties() ) )
			return;

		loadDeployments( action , node );
		
		super.loadFromNodeAttributes( action , node , false );
		scatterProperties( action );
		
		super.loadFromNodeElements( action , node , true );
		super.resolveRawProperties();

		loadNodes( action , node );
		loadBase( action , node );
		
		super.initFinished();
	}

	private void loadBase( ActionBase action , Node node ) throws Exception {
		Node item = ConfReader.xmlGetFirstChild( node , ELEMENT_PLATFORM );
		if( item == null )
			return;
		
		basesw = new MetaEnvServerBase( meta , this );
		basesw.load( action , item );
	}
		
	private void loadDeployments( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , ELEMENT_DEPLOY );
		if( items == null )
			return;
		
		for( Node dpnode : items ) {
			MetaEnvServerDeployment dp = new MetaEnvServerDeployment( meta , this );
			dp.load( action , dpnode );
			addDeployment( dp );
		}
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		super.saveSplit( doc , root );
		
		if( basesw != null ) {
			Element baseElement = Common.xmlCreateElement( doc , root , ELEMENT_PLATFORM );
			basesw.save( action , doc , baseElement );
		}

		for( MetaEnvServerDeployment deploy : deployments ) {
			Element deployElement = Common.xmlCreateElement( doc , root , ELEMENT_DEPLOY );
			deploy.save( action , doc , deployElement );
		}
		
		for( MetaEnvServerNode node : nodes ) {
			Element nodeElement = Common.xmlCreateElement( doc , root , ELEMENT_NODE );
			node.save( action , doc , nodeElement );
		}
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		if( !super.initCreateStarted( server.getProperties() ) )
			return;

		super.loadFromNodeAttributes( action , node , false );
		scatterProperties( action );
		
		super.loadFromNodeElements( action , node , true );
		super.resolveRawProperties();
		
		loadPrepare( action , node );
		super.initFinished();
	}
		
	private void loadPrepare( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , ELEMENT_PREPARE );
		if( items == null )
			return;
		
		for( Node snnode : items ) {
			MetaEnvServerPrepareApp sn = new MetaEnvServerPrepareApp( meta , this );
			sn.load( action , snnode );
			addPrepare( sn );
		}
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		super.saveSplit( doc , root );
		
		for( MetaEnvServerPrepareApp prepare : prepareMap.values() ) {
			Element prepareElement = Common.xmlCreateElement( doc , root , ELEMENT_PREPARE );
			prepare.save( action , doc , prepareElement );
		}
	}

	private void loadNodes( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , ELEMENT_NODE );
		if( items == null )
			return;
		
		int pos = 1;
		for( Node snnode : items ) {
			MetaEnvServerNode sn = new MetaEnvServerNode( meta , this , pos );
			sn.load( action , snnode );
			addNode( sn );
			pos++;
		}
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
