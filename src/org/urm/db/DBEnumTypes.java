package org.urm.db;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.engine.Engine;
import org.urm.engine.EngineDB;

public abstract class DBEnumTypes {

	public enum DBEnumResourceType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		CREDENTIALS(1,null) ,
		SSH(2,null) ,
		SVN(3,null) ,
		GIT(4,null) ,
		NEXUS(5,null);
		
		public boolean isAuthResource() {
			if( this == GIT || this == NEXUS || this == SVN )
				return( true );
			return( false );
		}
		
		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumResourceType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumResourceType getValue( Integer value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumResourceType.class , value , required , UNKNOWN ) ); };
		public static DBEnumResourceType getValue( String value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumResourceType.class , value , required , UNKNOWN ) ); };
	}

	public enum DBEnumBaseCategoryType implements DBEnumInterface {
		HOST(1,null) ,
		ACCOUNT(2,null) ,
		APP(3,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumBaseCategoryType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumBaseCategoryType getValue( Integer value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumBaseCategoryType.class , value , required , null ) ); };
		public static DBEnumBaseCategoryType getValue( String value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumBaseCategoryType.class , value , required , null ) ); };
	};
	
	public enum DBEnumBaseSrcType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		PACKAGE(1,null) ,
		ARCHIVE_LINK(2,null) ,
		ARCHIVE_DIRECT(3,null) ,
		NODIST(4,null) ,
		INSTALLER(5,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumBaseSrcType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumBaseSrcType getValue( Integer value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumBaseSrcType.class , value , required , UNKNOWN ) ); };
		public static DBEnumBaseSrcType getValue( String value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumBaseSrcType.class , value , required , UNKNOWN ) ); };
	};
	
	public enum DBEnumBaseSrcFormatType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		TARGZ_SINGLEDIR(1,null) ,
		ZIP_SINGLEDIR(2,null) ,
		SINGLEFILE(3,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumBaseSrcFormatType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumBaseSrcFormatType getValue( Integer value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumBaseSrcFormatType.class , value , required , UNKNOWN ) ); };
		public static DBEnumBaseSrcFormatType getValue( String value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumBaseSrcFormatType.class , value , required , UNKNOWN ) ); };
	};
	
	public enum DBEnumServerAccessType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		SERVICE(1,null) ,
		PACEMAKER(2,null) ,
		DOCKER(3,null) ,
		GENERIC(4,null) ,
		MANUAL(5,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumServerAccessType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumServerAccessType getValue( Integer value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumServerAccessType.class , value , required , UNKNOWN ) ); };
		public static DBEnumServerAccessType getValue( String value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumServerAccessType.class , value , required , UNKNOWN ) ); };
	};

	public enum DBEnumParamValueType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		STRING(1,null) ,
		NUMBER(2,null) ,
		BOOL(3,null) ,
		PATH(4,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumParamValueType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumParamValueType getValue( Integer value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumParamValueType.class , value , required , UNKNOWN ) ); };
		public static DBEnumParamValueType getValue( String value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumParamValueType.class , value , required , UNKNOWN ) ); };
	};

	public enum DBEnumOSType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		LINUX(1,null) ,
		WINDOWS(2,null);

		public static VarOSTYPE getVarValue( DBEnumOSType ostype ) throws Exception {
			if( ostype == null )
				return( VarOSTYPE.UNKNOWN );
			return( VarOSTYPE.valueOf( ostype.name() ) ); 
		}
		public static DBEnumOSType getValue( VarOSTYPE ostype ) throws Exception {
			if( ostype == null )
				return( DBEnumOSType.UNKNOWN );
			return( getValue( ostype.name() , false ) ); 
		}
		
		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumOSType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumOSType getValue( Integer value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumOSType.class , value , required , UNKNOWN ) ); };
		public static DBEnumOSType getValue( String value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumOSType.class , value , required , UNKNOWN ) ); };
	};
	
	public enum DBEnumObjectType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		ENUM(1,null) ,
		PARAM(2,null) ,
		RESOURCE(3,null) ,
		DATACENTER(10,null) ,
		NETWORK(11,null) ,
		HOST(12,null) ,
		ACCOUNT(13,null) ,
		BASE_CATEGORY(20,null) ,
		BASE_GROUP(21,null) ,
		BASE_ITEM(22,null) ,
		SYSTEM(30,null) ,
		PRODUCT(31,null) ,
		SYSTEM_PARAM(32,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumObjectType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumObjectType getValue( Integer value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumObjectType.class , value , required , UNKNOWN ) ); };
		public static DBEnumObjectType getValue( String value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumObjectType.class , value , required , UNKNOWN ) ); };
	};

	//#################################################
	// implementation
	private static Class<?>[] enums = { 
			DBEnumBaseCategoryType.class , 
			DBEnumBaseSrcFormatType.class ,
			DBEnumBaseSrcType.class , 
			DBEnumParamValueType.class ,
			DBEnumResourceType.class ,
			DBEnumServerAccessType.class ,
			DBEnumOSType.class ,
			DBEnumObjectType.class
			}; 

	private static String prefix = "DBEnum";
	private static String suffix = "Type";
	
	private static <T extends Enum<T> & DBEnumInterface> T getValue( Class<T> type , Integer value , boolean required , T unknownValue ) throws Exception {
		if( value == 0 ) {
			if( required )
				Common.exit1( _Error.MissingEnumIntType1 , "missing enum integer type value, enum=" + type.getSimpleName() , type.getSimpleName() );
			return( unknownValue );
		}
		
    	for( T t : type.getEnumConstants() ) {
    		if( t.code() == value )
    			return( t );
    	}
    	
		Common.exit2( _Error.InvalidEnumIntType2 , "invalid enum item=" + value + ", enum=" + type.getSimpleName() , "" + value , type.getSimpleName() );
		return( null );
    }

    private static <T extends Enum<T> & DBEnumInterface> T getValue( Class<T> type , String value , boolean required , T unknownValue ) throws Exception {
		if( value == null || value.isEmpty() ) {
			if( required )
				Common.exit1( _Error.MissingEnumStringType1 , "missing enum string type value, enum=" + type.getSimpleName() , type.getSimpleName() );
			return( unknownValue );
		}
		
		@SuppressWarnings("unchecked")
		T t = ( T )getEnumValue( type , value );
		if( t != null )
			return( t );
    	
		Common.exit2( _Error.InvalidEnumStringType2 , "invalid enum item=" + value + ", enum=" + type.getSimpleName() , value , type.getSimpleName() );
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
    	connection.update( DBQueries.UPDATE_ENUMS_DROP0 );
    	
		int enumsId = DBNames.getEnumsId();
    	connection.update( DBQueries.UPDATE_NAMES_DROPPARENT1 , new String[] { "" + enumsId } );
    	
    	for( Class<?> c : enums ) {
    		String name = getEnumName( c );
    		int enumId = DBNames.getNameIndex( connection , enumsId , name , DBEnumObjectType.ENUM );
    		
    		if( !connection.update( DBQueries.UPDATE_ENUMS_ADD3 , new String[] { "0" , "" + enumId , EngineDB.getString( name ) } ) )
    			Common.exitUnexpected();
    		
    		for( Object object : c.getEnumConstants() ) {
    			DBEnumInterface oi = ( DBEnumInterface )object;
    			int elementValue = oi.code();
    			Enum<?> ev = ( Enum<?> )object;
    			String elementName = ev.name().toLowerCase();
    			
        		if( !connection.update( DBQueries.UPDATE_ENUMS_ADD3 , new String[] { "" + enumId , "" + elementValue , EngineDB.getString( elementName ) } ) )
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
    
    private static Enum<?> getEnumValue( Class<?> type , String value ) {
		String valueCheck = value.toUpperCase();
    	for( Object t : type.getEnumConstants() ) {
    		Enum<?> et = ( Enum<?> )t;
    		if( valueCheck.equals( et.name() ) )
    			return( et );
    		
    		DBEnumInterface oi = ( DBEnumInterface )t;
    		String[] synonyms = oi.synonyms();
    		if( synonyms != null ) {
    			for( String synonym : synonyms ) {
    				if( valueCheck.equals( synonym ) )
    					return( et );
    			}
    		}
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

