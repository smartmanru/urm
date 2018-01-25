package org.urm.meta.product;

public class MetaProductDoc {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_EXT = "extension";
	public static String PROPERTY_UNITBOUND = "unitbound";
	
	public Meta meta;
	public MetaDocs docs;

	public int ID;
	public String NAME;
	public String DESC;
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
		r.EXT = EXT;
		r.DESC = DESC;
		r.UNITBOUND = UNITBOUND;
		r.PV = PV;
		return( r );
	}

	public void createDoc( String name , String desc , String ext , boolean unitbound ) throws Exception {
		modifyDoc( name , desc , ext , unitbound );
	}
	
	public void modifyDoc( String name , String desc , String ext , boolean unitbound ) throws Exception {
		this.NAME = name;
		this.EXT = ext;
		this.DESC = desc;
		this.UNITBOUND = unitbound;
	}

}
