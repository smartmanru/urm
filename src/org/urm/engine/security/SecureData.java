package org.urm.engine.security;

import org.urm.common.Common;
import org.urm.engine.properties.EntityVar;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.system.AppProduct;
import org.urm.meta.system.AppSystem;

public class SecureData {

	public static String GROUP_USERS = "users"; 
	public static String GROUP_RESOURCES = "resources"; 
	public static String GROUP_PRODUCTS = "products";
	public static String GROUP_REVISIONS = "revisions";
	public static String GROUP_VARS = "vars";
	public static String GROUP_CONFFILES = "conf";
	public static String GROUP_SYSTEMS = "systems";
	public static String GROUP_ENVS = "envs";
	public static String GROUP_SEGMENTS = "segments";
	public static String GROUP_SERVERS = "servers";
	public static String GROUP_NODES = "nodes";
	
	public static String ITEM_MASTER = "master";
	
	public static String ATTR_PASSWORDMD5 = "password.md5";
	public static String ATTR_METHOD = "method";
	public static String ATTR_USER = "user";
	public static String ATTR_PASSWORD = "password";
	public static String ATTR_KEYPUBLIC = "key.public";
	public static String ATTR_KEYPRIVATE = "key.private";
	public static String ATTR_CONTAINERNAME = "container.name";
	
	public static String getMasterPasswordKey() {
		return( Common.getPath( ITEM_MASTER , ATTR_PASSWORDMD5 ) );
	}
	
	public static String getUserPasswordKey( AuthUser user ) {
		return( Common.getPath( GROUP_USERS , user.NAME , ATTR_PASSWORDMD5 ) );
	}
	
	public static String getResourceMethodKey( AuthResource res ) {
		return( Common.getPath( GROUP_RESOURCES , res.NAME , ATTR_METHOD ) );
	}
	
	public static String getResourceUserKey( AuthResource res ) {
		return( Common.getPath( GROUP_RESOURCES , res.NAME , ATTR_USER ) );
	}
	
	public static String getResourcePasswordKey( AuthResource res ) {
		return( Common.getPath( GROUP_RESOURCES , res.NAME , ATTR_PASSWORD ) );
	}
	
	public static String getResourceSshPublicKey( AuthResource res ) {
		return( Common.getPath( GROUP_RESOURCES , res.NAME , ATTR_KEYPUBLIC ) );
	}
	
	public static String getResourceSshPrivateKey( AuthResource res ) {
		return( Common.getPath( GROUP_RESOURCES , res.NAME , ATTR_KEYPRIVATE ) );
	}

	public static String getProductContainerName( AppProduct product ) {
		return( Common.getPath( GROUP_PRODUCTS , product.NAME , ATTR_CONTAINERNAME ) );
	}

	public static String getProductFolder( AppProduct product ) {
		return( Common.getPath( GROUP_PRODUCTS , product.NAME ) );
	}

	public static String getEngineVar( EntityVar var ) {
		return( Common.getPath( GROUP_VARS , var.NAME ) );
	}
	
	public static String getSystemVar( AppSystem system , EntityVar var ) {
		return( Common.getPath( GROUP_SYSTEMS , GROUP_VARS , var.NAME ) );
	}

	public static String getMetaFolder( Meta meta ) {
		String productFolder = getProductFolder( meta.findProduct() );
		return( Common.getPath( productFolder , GROUP_REVISIONS , meta.getRevision() ) );
	}
	
	public static String getEnvFolder( MetaEnv env ) {
		String metaFolder = getMetaFolder( env.meta );
		return( Common.getPath( metaFolder , GROUP_ENVS , env.NAME ) );
	}

	public static String getEnvSegmentFolder( MetaEnvSegment sg ) {
		String envFolder = getEnvFolder( sg.env );
		return( Common.getPath( envFolder , GROUP_SEGMENTS , sg.NAME ) );
	}

	public static String getEnvServerFolder( MetaEnvServer server ) {
		String sgFolder = getEnvSegmentFolder( server.sg );
		return( Common.getPath( sgFolder , GROUP_SERVERS , server.NAME ) );
	}
	
	public static String getEnvServerNodeFolder( MetaEnvServerNode node ) {
		String sgFolder = getEnvServerFolder( node.server );
		return( Common.getPath( sgFolder , GROUP_NODES , "node" + node.POS ) );
	}
	
	public static String getMetaVar( Meta meta , EntityVar var ) {
		String metaFolder = getMetaFolder( meta );
		return( Common.getPath( metaFolder , GROUP_VARS , var.NAME ) );
	}

	public static String getEnvVar( MetaEnv env , EntityVar var ) {
		String envFolder = getEnvFolder( env );
		return( Common.getPath( envFolder , GROUP_VARS , var.NAME ) );
	}
	
	public static String getEnvSegmentVar( MetaEnvSegment sg , EntityVar var ) {
		String sgFolder = getEnvSegmentFolder( sg );
		return( Common.getPath( sgFolder , GROUP_VARS , var.NAME ) );
	}
	
	public static String getEnvServerVar( MetaEnvServer server , EntityVar var ) {
		String serverFolder = getEnvServerFolder( server );
		return( Common.getPath( serverFolder , GROUP_VARS , var.NAME ) );
	}
	
	public static String getEnvServerNodeVar( MetaEnvServerNode node , EntityVar var ) {
		String nodeFolder = getEnvServerNodeFolder( node );
		return( Common.getPath( nodeFolder , GROUP_VARS , var.NAME ) );
	}

	public static String getEnvConfFolder( MetaEnv env , MetaDistrConfItem conf ) {
		String envFolder = getEnvFolder( env );
		return( Common.getPath( envFolder , GROUP_CONFFILES , conf.NAME ) );
	}
	
	public static String getEnvSegmentConfFolder( MetaEnvSegment sg , MetaDistrConfItem conf ) {
		String envFolder = getEnvSegmentFolder( sg );
		return( Common.getPath( envFolder , GROUP_CONFFILES , conf.NAME ) );
	}
	
	public static String getEnvServerConfFolder( MetaEnvServer server , MetaDistrConfItem conf ) {
		String envFolder = getEnvServerFolder( server );
		return( Common.getPath( envFolder , GROUP_CONFFILES , conf.NAME ) );
	}
	
	public static String getEnvServerNodeConfFolder( MetaEnvServerNode node , MetaDistrConfItem conf ) {
		String envFolder = getEnvServerNodeFolder( node );
		return( Common.getPath( envFolder , GROUP_CONFFILES , conf.NAME ) );
	}
	
}
