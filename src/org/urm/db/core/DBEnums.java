package org.urm.db.core;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db._Error;
import org.urm.engine.EngineDB;
import org.urm.meta.EngineLoader;

public abstract class DBEnums {

	public static int VALUE_UNKNOWN = 0; 
	
	public enum DBEnumOwnerStatusType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		ACTIVE(1,null) ,
		DELETED(2,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumOwnerStatusType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumOwnerStatusType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumOwnerStatusType.class , value , required , null ) ); };
		public static DBEnumOwnerStatusType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumOwnerStatusType.class , value , required , null ) ); };
	}
	
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
		public static DBEnumResourceType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumResourceType.class , value , required , UNKNOWN ) ); };
		public static DBEnumResourceType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumResourceType.class , value , required , UNKNOWN ) ); };
	}

	public enum DBEnumBaseCategoryType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		HOST(1,null) ,
		ACCOUNT(2,null) ,
		APP(3,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumBaseCategoryType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumBaseCategoryType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumBaseCategoryType.class , value , required , null ) ); };
		public static DBEnumBaseCategoryType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumBaseCategoryType.class , value , required , null ) ); };
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
		public static DBEnumBaseSrcType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumBaseSrcType.class , value , required , UNKNOWN ) ); };
		public static DBEnumBaseSrcType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumBaseSrcType.class , value , required , UNKNOWN ) ); };
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
		public static DBEnumBaseSrcFormatType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumBaseSrcFormatType.class , value , required , UNKNOWN ) ); };
		public static DBEnumBaseSrcFormatType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumBaseSrcFormatType.class , value , required , UNKNOWN ) ); };
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
		public static DBEnumServerAccessType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumServerAccessType.class , value , required , UNKNOWN ) ); };
		public static DBEnumServerAccessType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumServerAccessType.class , value , required , UNKNOWN ) ); };
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
		public static DBEnumParamValueType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumParamValueType.class , value , required , UNKNOWN ) ); };
		public static DBEnumParamValueType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumParamValueType.class , value , required , UNKNOWN ) ); };
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
		public static DBEnumOSType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumOSType.class , value , required , UNKNOWN ) ); };
		public static DBEnumOSType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumOSType.class , value , required , UNKNOWN ) ); };
	};
	
	public enum DBEnumObjectType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		ROOT(1,null) ,
		ENUM(2,null) ,
		PARAM(3,null) ,
		RESOURCE(4,null) ,
		LIFECYCLE(5,null) ,
		LIFECYCLEPHASE(6,null) ,
		DATACENTER(10,null) ,
		NETWORK(11,null) ,
		HOST(12,null) ,
		ACCOUNT(13,null) ,
		BASE_CATEGORY(20,null) ,
		BASE_GROUP(21,null) ,
		BASE_ITEM(22,null) ,
		SYSTEM(30,null) ,
		PRODUCT(31,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumObjectType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumObjectType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumObjectType.class , value , required , UNKNOWN ) ); };
		public static DBEnumObjectType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumObjectType.class , value , required , UNKNOWN ) ); };
	};

	public enum DBEnumBuilderMethodType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		GENERIC(1,null) ,
		ANT(2,null) ,
		MAVEN(3,null) ,
		GRADLE(4,null) ,
		MSBUILD(5,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumBuilderMethodType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumBuilderMethodType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumBuilderMethodType.class , value , required , UNKNOWN ) ); };
		public static DBEnumBuilderMethodType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumBuilderMethodType.class , value , required , UNKNOWN ) ); };
	};
	
	public enum DBEnumBuilderTargetType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		LOCALPATH(1,null) ,
		NEXUS(2,null) ,
		NUGET(3,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumBuilderTargetType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumBuilderTargetType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumBuilderTargetType.class , value , required , UNKNOWN ) ); };
		public static DBEnumBuilderTargetType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumBuilderTargetType.class , value , required , UNKNOWN ) ); };
	};
	
	public enum DBEnumMirrorType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		SERVER(1,null) ,
		PROJECT(2,null) ,
		PRODUCT_META(3,null) ,
		PRODUCT_DATA(4,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumMirrorType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumMirrorType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumMirrorType.class , value , required , null ) ); };
		public static DBEnumMirrorType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumMirrorType.class , value , required , null ) ); };
	};

	public enum DBEnumLifecycleType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		MAJOR(1,null) ,
		MINOR(2,null) ,
		URGENT(3,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumLifecycleType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumLifecycleType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumLifecycleType.class , value , required , UNKNOWN ) ); };
		public static DBEnumLifecycleType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumLifecycleType.class , value , required , UNKNOWN ) ); };
	};
	
	public enum DBEnumLifecycleStageType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		RELEASE(1,null) ,
		DEPLOYMENT(2,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumLifecycleStageType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumLifecycleStageType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumLifecycleStageType.class , value , required , UNKNOWN ) ); };
		public static DBEnumLifecycleStageType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumLifecycleStageType.class , value , required , UNKNOWN ) ); };
	};
	
	public enum DBEnumBuildModeType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		BRANCH(1,null) ,
		MAJORBRANCH(2,null) ,
		TRUNK(3,null) ,
		DEVBRANCH(4,null) ,
		DEVTRUNK(5,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumBuildModeType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumBuildModeType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumBuildModeType.class , value , required , UNKNOWN ) ); };
		public static DBEnumBuildModeType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumBuildModeType.class , value , required , UNKNOWN ) ); };
	};
	
	public enum DBEnumObjectVersionType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		APP(1,null) ,
		CORE(2,null) ,
		SYSTEM(3,null) ,
		PRODUCT(4,null) ,
		ENVIRONMENT(5,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumObjectVersionType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumObjectVersionType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumObjectVersionType.class , value , required , null ) ); };
		public static DBEnumObjectVersionType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumObjectVersionType.class , value , required , null ) ); };
	};
	
	public enum DBEnumParamEntityType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		RC(1,null) ,
		ENGINE(2,null) ,
		PRODUCTDEFS(3,null) ,
		PRODUCTBUILD(4,null) ,
		BASEGROUP(5,null) ,
		BASEITEM(6,null) ,
		MONITORING(7,null) ,
		LIFECYCLE(8,null) ,
		LIFECYCLEPHASE(9,null) ,
		RESOURCE(10,null) ,
		DATACENTER(21,null) ,
		NETWORK(22,null) ,
		HOST(23,null) ,
		ACCOUNT(24,null) ,
		SYSTEM(101,null) ,
		PRODUCT(102,null) ,
		ENVIRONMENT(103,null) ,
		SEGMENT(104,null) ,
		SERVER(105,null) ,
		NODE(106,null) ,
		RC_CUSTOM(201,null) ,
		ENGINE_CUSTOM(202,null) ,
		BASEITEM_CUSTOM(203,null) ,
		SYSTEM_CUSTOM(301,null) ,
		PRODUCT_CUSTOM(302,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumParamEntityType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumParamEntityType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumParamEntityType.class , value , required , null ) ); };
		public static DBEnumParamEntityType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumParamEntityType.class , value , required , null ) ); };
	};
	
	public enum DBEnumParamRoleType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		RC(1,null) ,
		ENGINE(2,null),
		PRODUCTDEFS(3,null),
		BUILDMODE_COMMON(10,null),
		BUILDMODE_BRANCH(11,null) ,
		BUILDMODE_MAJORBRANCH(12,null) ,
		BUILDMODE_TRUNK(13,null) ,
		BUILDMODE_DEVBRANCH(14,null) ,
		BUILDMODE_DEVTRUNK(15,null) ,
		MONITORING(20,null) ,
		BASEITEM(30,null) ,
		SYSTEM(100,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumParamRoleType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumParamRoleType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumParamRoleType.class , value , required , null ) ); };
		public static DBEnumParamRoleType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumParamRoleType.class , value , required , null ) ); };
	};
	
	public enum DBEnumChatType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		JABBER(1,null) ,
		ROCKET(2,null) ,
		SKYPE(3,null) ,
		TELEGRAM(4,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumChatType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumChatType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumChatType.class , value , required , null ) ); };
		public static DBEnumChatType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumChatType.class , value , required , null ) ); };
	};
	
	//#################################################
	// implementation
	private static DBEnumInfo[] enums = { 
		new DBEnumInfo( DBEnumOwnerStatusType.class , 10 ) ,
		new DBEnumInfo( DBEnumObjectType.class , 11 ) ,
		new DBEnumInfo( DBEnumObjectVersionType.class , 12 ) ,
		new DBEnumInfo( DBEnumBaseCategoryType.class , 13 ) , 
		new DBEnumInfo( DBEnumBaseSrcFormatType.class , 14 ) ,
		new DBEnumInfo( DBEnumBaseSrcType.class , 15 ) , 
		new DBEnumInfo( DBEnumParamValueType.class , 16 ) ,
		new DBEnumInfo( DBEnumResourceType.class , 17 ) ,
		new DBEnumInfo( DBEnumServerAccessType.class , 18 ) ,
		new DBEnumInfo( DBEnumOSType.class , 19 ) ,
		new DBEnumInfo( DBEnumBuilderMethodType.class , 20 ) ,
		new DBEnumInfo( DBEnumBuilderTargetType.class , 21 ) ,
		new DBEnumInfo( DBEnumMirrorType.class , 22 ) ,
		new DBEnumInfo( DBEnumLifecycleType.class , 23 ) ,
		new DBEnumInfo( DBEnumLifecycleStageType.class , 24 ) ,
		new DBEnumInfo( DBEnumBuildModeType.class , 25 ) ,
		new DBEnumInfo( DBEnumParamEntityType.class , 26 ) ,
		new DBEnumInfo( DBEnumParamRoleType.class , 27 ) ,
		new DBEnumInfo( DBEnumChatType.class , 28 )
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

	public static String getEnumName( Class<?> c ) throws Exception {
		String name = c.getSimpleName();
		if( !name.startsWith( prefix ) )
			Common.exitUnexpected();
		if( !name.endsWith( suffix ) )
			Common.exitUnexpected();
		String nameDB = name.substring( prefix.length() , name.length() - suffix.length() );
		nameDB = nameDB.toLowerCase();
		return( nameDB );
	}
    
    public static void updateDatabase( DBConnection connection ) throws Exception {
    	connection.update( DBQueries.MODIFY_ENUMS_DROP0 );
    	
		int enumsId = DBNames.getEnumsId();
    	connection.update( DBQueries.MODIFY_NAMES_DROPPARENT1 , new String[] { "" + enumsId } );
    	
    	for( DBEnumInfo e : enums ) {
    		String name = getEnumName( e.enumClass );
    		int enumId = e.enumID;
    		
    		if( !connection.update( DBQueries.MODIFY_ENUMS_ADD4 , new String[] { "0" , "" + enumId , EngineDB.getString( name ) , "" + EngineDB.APP_VERSION } ) )
    			Common.exitUnexpected();
    		
    		for( Object object : e.enumClass.getEnumConstants() ) {
    			DBEnumInterface oi = ( DBEnumInterface )object;
    			int elementValue = oi.code();
    			Enum<?> ev = ( Enum<?> )object;
    			String elementName = ev.name().toLowerCase();
    			
        		if( !connection.update( DBQueries.MODIFY_ENUMS_ADD4 , new String[] { "" + enumId , "" + elementValue , EngineDB.getString( elementName ) , "" + EngineDB.APP_VERSION } ) )
        			Common.exitUnexpected();
    		}
    	}
    }
    
    public static Class<?> getEnum( String name ) throws Exception {
    	for( DBEnumInfo e : enums ) {
    		String ename = getEnumName( e.enumClass );
    		if( ename.equals( name ) )
    			return( e.enumClass );
    	}
    	
    	return( null );
    }
    
    public static int getEnumCode( Class<?> type , String value ) {
		int code = DBEnums.VALUE_UNKNOWN;
		if( !value.isEmpty() ) {
			Enum<?> e = DBEnums.getEnumValue( type , value );
			DBEnumInterface ei = ( DBEnumInterface )e;
			code = ei.code();
		}
		return( code );
    }
    
    public static String getEnumValue( Class<?> type , int code ) throws Exception {
    	for( Object t : type.getEnumConstants() ) {
    		DBEnumInterface oi = ( DBEnumInterface )t;
    		int elementValue = oi.code();
    		if( elementValue == code ) {
        		Enum<?> ev = ( Enum<?> )t;
        		return( ev.name().toLowerCase() );
    		}
    	}
    	
    	Common.exitUnexpected();
		return( null );
    }
    
    public static Enum<?> getEnumValue( Class<?> type , String value ) {
		String valueCheck = value.toUpperCase();
		valueCheck = Common.replace( valueCheck , "." , "_" );
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
    
    public static void verifyDatabase( EngineLoader loader ) throws Exception {
    	DBConnection c = loader.getConnection();
    	ResultSet rs = c.query( DBQueries.QUERY_ENUMS_GETALL0 );

    	Map<Integer,String> elistId = new HashMap<Integer,String>();
    	Map<String,Integer> elistName = new HashMap<String,Integer>();
    	Map<Integer,Map<Integer,String>> data = new HashMap<Integer,Map<Integer,String>>();

    	try {
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
		}
		finally {
			c.closeQuery();
		}
    	
    	// check enums are in database
    	for( DBEnumInfo e : enums ) {
    		String ename = getEnumName( e.enumClass );
    		Integer category = elistName.get( ename );
    		if( category == null )
		    	Common.exit1( _Error.MissingEnum1 , "Missing enum=" + ename , ename );
    		if( e.enumID != category )
    			Common.exit3( _Error.InvalidEnum3 , "Mismatched enum=" + ename + ", id=" + e.enumID + ", dbid=" + category.intValue() , ename , "" + e.enumID , "" + category.intValue() );
    		
    		Map<Integer,String> items = data.get( category );
    		for( Object ei : e.enumClass.getEnumConstants() ) {
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

