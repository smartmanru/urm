package org.urm.meta.product;

public class MetaProductUnit {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	
	public Meta meta;
	public MetaUnits units;

	public int ID; 
	public String NAME;
	public String DESC;
	public int PV;

	public MetaProductUnit( Meta meta , MetaUnits units ) {
		this.meta = meta;
		this.units = units;
		ID = -1;
		PV = -1;
	}
	
	public void createUnit( String name , String desc ) {
		modifyUnit( name , desc );
	}

	public void modifyUnit( String name , String desc ) {
		this.NAME = name;
		this.DESC = desc;
	}
	
	public MetaProductUnit copy( Meta meta , MetaUnits units ) throws Exception {
		MetaProductUnit r = new MetaProductUnit( meta , units );
		
		r.ID = ID;
		r.PV = PV;
		r.NAME = NAME;
		r.DESC = DESC;
		return( r );
	}

}
