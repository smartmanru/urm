package org.urm.db;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.engine.Engine;

public abstract class DBEnumTypes {

	public enum DBEnumResourceType implements DBEnumInterface {
		UNKNOWN(0) ,
		CREDENTIALS(1) ,
		SSH(2) ,
		SVN(3) ,
		GIT(4) ,
		NEXUS(5);
		
		public boolean isAuthResource() {
			if( this == GIT || this == NEXUS || this == SVN )
				return( true );
			return( false );
		}
		
		private final int value;
		@Override public int code() { return( value ); };
		private DBEnumResourceType( int value ) { this.value = value; };
		public static DBEnumResourceType getValue( Integer value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumResourceType.class , value , required , UNKNOWN ) ); };
		public static DBEnumResourceType getValue( String value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumResourceType.class , value , required , UNKNOWN ) ); };
	}

	public enum DBEnumBaseCategoryType implements DBEnumInterface {
		HOST(1) ,
		ACCOUNT(2) ,
		APP(3);

		private final int value;
		@Override public int code() { return( value ); };
		private DBEnumBaseCategoryType( int value ) { this.value = value; };
		public static DBEnumBaseCategoryType getValue( Integer value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumBaseCategoryType.class , value , required , null ) ); };
		public static DBEnumBaseCategoryType getValue( String value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumBaseCategoryType.class , value , required , null ) ); };
	};
	
	public enum DBEnumBaseSrcType implements DBEnumInterface {
		UNKNOWN(0) ,
		PACKAGE(1) ,
		ARCHIVE_LINK(2) ,
		ARCHIVE_DIRECT(3) ,
		NODIST(4) ,
		INSTALLER(5);

		private final int value;
		@Override public int code() { return( value ); };
		private DBEnumBaseSrcType( int value ) { this.value = value; };
		public static DBEnumBaseSrcType getValue( Integer value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumBaseSrcType.class , value , required , null ) ); };
		public static DBEnumBaseSrcType getValue( String value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumBaseSrcType.class , value , required , null ) ); };
	};
	
	public enum DBEnumBaseSrcFormatType implements DBEnumInterface {
		UNKNOWN(0) ,
		TARGZ_SINGLEDIR(1) ,
		ZIP_SINGLEDIR(2) ,
		SINGLEFILE(3);

		private final int value;
		@Override public int code() { return( value ); };
		private DBEnumBaseSrcFormatType( int value ) { this.value = value; };
		public static DBEnumBaseSrcFormatType getValue( Integer value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumBaseSrcFormatType.class , value , required , null ) ); };
		public static DBEnumBaseSrcFormatType getValue( String value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumBaseSrcFormatType.class , value , required , null ) ); };
	};
	
	public enum DBEnumServerAccessType implements DBEnumInterface {
		UNKNOWN(0) ,
		SERVICE(1) ,
		PACEMAKER(2) ,
		DOCKER(3) ,
		GENERIC(4) ,
		MANUAL(5);

		private final int value;
		@Override public int code() { return( value ); };
		private DBEnumServerAccessType( int value ) { this.value = value; };
		public static DBEnumServerAccessType getValue( Integer value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumServerAccessType.class , value , required , null ) ); };
		public static DBEnumServerAccessType getValue( String value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumServerAccessType.class , value , required , null ) ); };
	};

	public enum DBEnumParamValueType implements DBEnumInterface {
		UNKNOWN(0) ,
		PROPERTY_STRING(1) ,
		PROPERTY_NUMBER(2) ,
		PROPERTY_BOOL(3) ,
		PROPERTY_PATH(4);

		private final int value;
		@Override public int code() { return( value ); };
		private DBEnumParamValueType( int value ) { this.value = value; };
		public static DBEnumParamValueType getValue( Integer value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumParamValueType.class , value , required , null ) ); };
		public static DBEnumParamValueType getValue( String value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumParamValueType.class , value , required , null ) ); };
	};

	//#################################################
	// implementation
	private static Class<?>[] enums = { 
			DBEnumBaseCategoryType.class , 
			DBEnumBaseSrcFormatType.class ,
			DBEnumBaseSrcType.class , 
			DBEnumParamValueType.class ,
			DBEnumResourceType.class ,
			DBEnumServerAccessType.class
			}; 

	private static String prefix = "DBEnum";
	private static String suffix = "Type";
	
	private static <T extends Enum<T> & DBEnumInterface> T getValue( Class<T> type , Integer value , boolean required , T unknownValue ) throws Exception {
		if( value == 0 ) {
			if( required )
				Common.exit1( _Error.MissingEnumIntType1 , "missing enum integer type value, enum=" + type.getName() , type.getName() );
			return( unknownValue );
		}
		
    	for( T t : type.getEnumConstants() ) {
    		if( t.code() == value )
    			return( t );
    	}
    	
		Common.exit2( _Error.InvalidEnumIntType2 , "invalid enum item=" + value + ", enum=" + type.getName() , "" + value , type.getName() );
		return( null );
    }

    private static <T extends Enum<T> & DBEnumInterface> T getValue( Class<T> type , String value , boolean required , T unknownValue ) throws Exception {
		if( value == null || value.isEmpty() ) {
			if( required )
				Common.exit1( _Error.MissingEnumStringType1 , "missing enum string type value, enum=" + type.getName() , type.getName() );
			return( unknownValue );
		}
		
    	for( T t : type.getEnumConstants() ) {
    		if( value.equals( t.name() ) )
    			return( t );
    	}
    	
		Common.exit2( _Error.InvalidEnumStringType2 , "invalid enum item=" + value + ", enum=" + type.getName() , value , type.getName() );
		return( null );
    }

	private static String getEnumName( Class<?> c ) throws Exception {
		String name = c.getSimpleName();
		if( !name.startsWith( prefix ) )
			Common.exitUnexpected();
		if( !name.endsWith( suffix ) )
			Common.exitUnexpected();
		String nameDB = name.substring( prefix.length() , name.length() - suffix.length() );
		nameDB = nameDB.toLowerCase();
		return( nameDB );
	}
    
    public static void updateDatabase( Engine engine , DBConnection connection ) throws Exception {
    	connection.update( DBQueries.QUERY_ENUMS_DROP0 );
    	
		int enumsId = DBNames.getEnumsId();
    	connection.update( DBQueries.QUERY_NAMES_DROPPARENT1 , new String[] { "" + enumsId } );
    	
    	for( Class<?> c : enums ) {
    		String name = getEnumName( c );
    		int enumId = DBNames.getNameIndex( connection , enumsId , name );
    		
    		if( !connection.update( DBQueries.QUERY_ENUMS_ADD3 , new String[] { "0" , "" + enumId , name } ) )
    			Common.exitUnexpected();
    		
    		for( Object object : c.getEnumConstants() ) {
    			DBEnumInterface oi = ( DBEnumInterface )object;
    			int elementValue = oi.code();
    			Enum<?> ev = ( Enum<?> )object;
    			String elementName = ev.name().toLowerCase();
    			
        		if( !connection.update( DBQueries.QUERY_ENUMS_ADD3 , new String[] { "" + enumId , "" + elementValue , elementName } ) )
        			Common.exitUnexpected();
    		}
    	}
    }
    
    private static Class<?> getEnum( String name ) throws Exception {
    	for( Class<?> c : enums ) {
    		String ename = getEnumName( c );
    		if( ename.equals( name ) )
    			return( c );
    	}
    	
    	return( null );
    }
    
    private static Enum<?> getEnumValue( Class<?> ec , String name ) {
		for( Object ei : ec.getEnumConstants() ) {
			Enum<?> item = ( Enum<?> )ei;
			String ename = item.name().toLowerCase();
			if( ename.equals( name ) )
				return( item );
		}
		return( null );
    }
    
    public static void verifyDatabase( Engine engine , DBConnection connection ) throws Exception {
    	ResultSet rs = connection.query( DBQueries.QUERY_ENUMS_GETALL0 );
    	if( rs == null )
    		Common.exitUnexpected();
    	
    	Map<Integer,String> elistId = new HashMap<Integer,String>();
    	Map<String,Integer> elistName = new HashMap<String,Integer>();
    	Map<Integer,Map<Integer,String>> data = new HashMap<Integer,Map<Integer,String>>();

    	// read and check db has in enums
    	while( rs.next() ) {
    		int category = rs.getInt( 1 );
    		int item = rs.getInt( 2 );
    		String name = rs.getString( 3 );
    		if( category == 0 ) {
    			Class<?> ec = getEnum( name );
    			if( ec == null )
    		    	Common.exit1( _Error.UnexpectedEnum1 , "Unexpected enum=" + name , name );

    	    	elistId.put( item , name );
    	    	elistName.put( name , item );
    		}
    		else {
    			Map<Integer,String> items = data.get( category );
    			if( items == null ) {
    				items = new HashMap<Integer,String>();
    				data.put( category , items );
    			}
    			
    			items.put( item , name );
    			
    			// elist should be first
    			String ename = elistId.get( category );
    			if( ename == null )
    		    	Common.exit1( _Error.UnexpectedEnum1 , "Unexpected enum=" + category , "" + category );
    				
    			Class<?> ec = getEnum( ename );
    			if( ec == null )
    		    	Common.exit1( _Error.UnexpectedEnum1 , "Unexpected enum=" + name , name );

    			Enum<?> eitem = getEnumValue( ec , name );
    			if( eitem == null )
    				Common.exit2( _Error.UnexpectedEnumItem2 , "Unexpected enum=" + ename + ", item=" + name , ename , name );
    		}
    	}
    	
    	// check enums are in database
    	for( Class<?> c : enums ) {
    		String ename = getEnumName( c );
    		Integer category = elistName.get( ename );
    		if( category == null )
		    	Common.exit1( _Error.MissingEnum1 , "Missing enum=" + ename , ename );
    		
    		Map<Integer,String> items = data.get( category );
    		for( Object ei : c.getEnumConstants() ) {
    			DBEnumInterface oi = ( DBEnumInterface )ei;
    			int key = oi.code();
    			if( !items.containsKey( key ) ) {
        			Enum<?> item = ( Enum<?> )ei;
        			String name = item.name().toLowerCase();
        			Common.exit2( _Error.MissingEnumItem2 , "Missing enum=" + ename + ", item=" + name , ename , name );
    			}
    		}
    	}    	
    }
    
}

