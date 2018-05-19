package org.urm.engine.security;

import org.urm.common.Common;
import org.urm.meta.system.AppProduct;

public class SecureData {

	public static String GROUP_USERS = "users"; 
	public static String GROUP_RESOURCES = "resources"; 
	public static String GROUP_PRODUCTS = "products";
	public static String GROUP_CONTAINERS = "containers";
	
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
		return( Common.getPath( GROUP_RESOURCES , product.NAME , ATTR_CONTAINERNAME ) );
	}
	
}
