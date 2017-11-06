package org.urm.db;

public abstract class DBEnumTypes {

	public enum DBEnumResourceType implements DBEnumInterface {
		UNKNOWN(0) ,
		CREDENTIALS(1) ,
		SSH(2) ,
		SVN(3) ,
		GIT(4) ,
		NEXUS(5);
		
		private final int value;
		@Override public int getValue() { return( value ); };
		private DBEnumResourceType( int value ) { this.value = value; };
		public static DBEnumResourceType getValue( int value ) { return( DBEnumTypes.getValue( DBEnumResourceType.class , value ) ); };
		public static DBEnumResourceType getValue( String value ) { return( DBEnumTypes.getValue( DBEnumResourceType.class , value ) ); };
		
		public boolean isAuthResource() {
			if( this == GIT || this == NEXUS || this == SVN )
				return( true );
			return( false );
		}
	}

    public static <T extends Enum<T> & DBEnumInterface> T getValue( Class<T> type , int value ) {
    	for( T t : type.getEnumConstants() ) {
    		if( t.getValue() == value )
    			return( t );
    	}
    	return( null );
    }

    public static <T extends Enum<T> & DBEnumInterface> T getValue( Class<T> type , String value ) {
    	for( T t : type.getEnumConstants() ) {
    		if( value.equals( t.name() ) )
    			return( t );
    	}
    	return( null );
    }
	
}

