package org.urm.meta;

public class MatchItem {

	public Integer FKID;
	public String FKNAME;
	public boolean MATCHED;

	public MatchItem( int id ) {
		FKID = id;
		FKNAME = "";
		MATCHED = true;
	}
	
	public MatchItem( String name ) {
		FKID = null;
		FKNAME = name;
		MATCHED = false;
	}
	
	public MatchItem( Integer id , String name ) {
		FKID = id;
		FKNAME = name;
		MATCHED = ( id == null )? false : true;
	}

	public MatchItem copy() {
		MatchItem r = new MatchItem( FKID , FKNAME );
		return( r );
	}

	public void match( Integer id ) {
		FKID = id;
		FKNAME = "";
		MATCHED = true;
	}
	
}
