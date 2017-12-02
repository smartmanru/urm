package org.urm.meta.engine;

import org.urm.db.core.DBEnums.*;
import org.urm.meta.EngineObject;

public class MirrorRepository extends EngineObject {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_MIRROR_TYPE = "type";
	public static String PROPERTY_RESOURCE = "resource";
	public static String PROPERTY_RESOURCE_REPO = "repository";
	public static String PROPERTY_RESOURCE_ROOT = "rootpath";
	public static String PROPERTY_RESOURCE_DATA = "datapath";
	
	public EngineMirrors mirrors;
	
	public int ID;
	public String NAME;
	public String DESC;
	public DBEnumMirrorType MIRROR_TYPE;
	public Integer RESOURCE_ID;
	public String RESOURCE_REPO;
	public String RESOURCE_ROOT;
	public String RESOURCE_DATA;
	public int CV;

	public Integer productId;
	
	public MirrorRepository( EngineMirrors mirrors ) {
		super( mirrors );
		this.mirrors = mirrors;
	}

	@Override
	public String getName() {
		return( NAME );
	}
	
	public String getFolderName() {
		return( NAME );
	}
	
	public boolean isActive() {
		if( RESOURCE_ID == null )
			return( false );
		return( true );
	}
	
	public boolean isServer() {
		return( MIRROR_TYPE == DBEnumMirrorType.SERVER );
	}
	
	public boolean isProject() {
		return( MIRROR_TYPE == DBEnumMirrorType.PROJECT );
	}
	
	public boolean isProductMeta() {
		return( MIRROR_TYPE == DBEnumMirrorType.PRODUCT_META );
	}
	
	public boolean isProductData() {
		return( MIRROR_TYPE == DBEnumMirrorType.PRODUCT_DATA );
	}
	
	public MirrorRepository copy( EngineMirrors mirror ) throws Exception {
		MirrorRepository r = new MirrorRepository( mirror );
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.MIRROR_TYPE = MIRROR_TYPE;
		r.RESOURCE_ID = RESOURCE_ID;
		r.RESOURCE_REPO = RESOURCE_REPO;
		r.RESOURCE_ROOT = RESOURCE_ROOT;
		r.RESOURCE_DATA = RESOURCE_DATA;
		r.CV = CV;
		return( r );
	}
	
	public void createRepository( String name , String desc , DBEnumMirrorType type ) throws Exception {
		modifyRepository( name , desc , type );
	}
	
	public void modifyRepository( String name , String desc , DBEnumMirrorType type ) throws Exception {
		this.NAME = name;
		this.DESC = desc;
		this.MIRROR_TYPE = type;
	}
	
	public void setMirror( Integer resourceId , String reponame , String reporoot , String dataroot ) throws Exception {
		this.RESOURCE_ID = resourceId;
		this.RESOURCE_REPO = reponame;
		this.RESOURCE_ROOT = ( reporoot.isEmpty() )? "/" : reporoot;
		this.RESOURCE_DATA = ( dataroot.isEmpty() )? "/" : dataroot;
	}
	
	public void clearMirror() throws Exception {
		RESOURCE_ID = null;
		RESOURCE_REPO = "";
		RESOURCE_ROOT = "";
		RESOURCE_DATA = "";
	}
	
}
