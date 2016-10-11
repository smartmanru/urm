package org.urm.meta.product;

public class MetaBaseCategory {

	public enum CATEGORY_TYPE {
		CategoryHost ,
		CategoryAccount ,
		CategoryApp
	};
	
	public CATEGORY_TYPE type;
	public String NAME;
	
	public MetaBaseCategory( CATEGORY_TYPE type , String NAME ) {
		this.type = type;
		this.NAME = NAME;
	}
	
}
