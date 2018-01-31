package org.urm.meta.product;

import org.urm.db.core.DBEnums.DBEnumDocCategoryType;

public class MetaProductDoc {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_CATEGORY = "type";
	public static String PROPERTY_EXT = "extension";
	public static String PROPERTY_UNITBOUND = "unitbound";
	
	public Meta meta;
	public MetaDocs docs;

	public int ID;
	public String NAME;
	public String DESC;
	public DBEnumDocCategoryType DOC_CATEGORY;
	public String EXT;
	public boolean UNITBOUND;
	public int PV;

	public MetaProductDoc( Meta meta , MetaDocs docs ) {
		this.meta = meta;
		this.docs = docs;
		ID = -1;
		PV = -1;
	}
	
	public MetaProductDoc copy( Meta rmeta , MetaDocs rdocs ) throws Exception {
		MetaProductDoc r = new MetaProductDoc( rmeta , rdocs );
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.DOC_CATEGORY = DOC_CATEGORY;
		r.EXT = EXT;
		r.UNITBOUND = UNITBOUND;
		r.PV = PV;
		return( r );
	}

	public void createDoc( String name , String desc , DBEnumDocCategoryType category , String ext , boolean unitbound ) throws Exception {
		modifyDoc( name , desc , category , ext , unitbound );
	}
	
	public void modifyDoc( String name , String desc , DBEnumDocCategoryType category , String ext , boolean unitbound ) throws Exception {
		this.NAME = name;
		this.DESC = desc;
		this.DOC_CATEGORY = category;
		this.EXT = ext;
		this.UNITBOUND = unitbound;
	}

}
