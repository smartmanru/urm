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

	public static MatchItem create( String value ) {
		if( value == null || value.isEmpty() )
			return( null );
		return( new MatchItem( value ) );
	}
	
	public static MatchItem create( Integer value ) {
		if( value == null )
			return( null );
		return( new MatchItem( value ) );
	}
	
	public static MatchItem copy( MatchItem item ) {
		if( item == null )
			return( null );
		MatchItem r = new MatchItem( item.FKID , item.FKNAME );
		return( r );
	}

	public static boolean isMatched( MatchItem item ) {
		if( item == null )
			return( true );
		return( item.MATCHED );
	}
		
	public static boolean equals( MatchItem item , int id ) {
		if( item == null )
			return( false );
		if( item.FKID == id )
			return( true );
		return( false );
	}
	
	public void match( Integer id ) {
		FKID = id;
		FKNAME = "";
		MATCHED = true;
	}

	public void unmatch( String name ) {
		FKID = null;
		FKNAME = name;
		MATCHED = false;
	}

}
