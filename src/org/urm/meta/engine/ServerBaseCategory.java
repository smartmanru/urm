package org.urm.meta.engine;

public class ServerBaseCategory {

	public enum CATEGORY_TYPE {
		CategoryHost ,
		CategoryAccount ,
		CategoryApp
	};
	
	public CATEGORY_TYPE type;
	public String NAME;
	
	public ServerBaseCategory( CATEGORY_TYPE type , String NAME ) {
		this.type = type;
		this.NAME = NAME;
	}
	
}
