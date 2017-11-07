package org.urm.db;

import org.urm.common.Common;

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
		@Override public int getValue() { return( value ); };
		private DBEnumResourceType( int value ) { this.value = value; };
		public static DBEnumResourceType getValue( Integer value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumResourceType.class , value , required , UNKNOWN ) ); };
		public static DBEnumResourceType getValue( String value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumResourceType.class , value , required , UNKNOWN ) ); };
	}

	public enum DBEnumBaseCategoryType implements DBEnumInterface {
		HOST(1) ,
		ACCOUNT(2) ,
		APP(3);

		private final int value;
		@Override public int getValue() { return( value ); };
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
		@Override public int getValue() { return( value ); };
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
		@Override public int getValue() { return( value ); };
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
		@Override public int getValue() { return( value ); };
		private DBEnumServerAccessType( int value ) { this.value = value; };
		public static DBEnumServerAccessType getValue( Integer value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumServerAccessType.class , value , required , null ) ); };
		public static DBEnumServerAccessType getValue( String value , boolean required ) throws Exception { return( DBEnumTypes.getValue( DBEnumServerAccessType.class , value , required , null ) ); };
	};

	public enum PropertyValueType implements DBEnumInterface {
		UNKNOWN(0) ,
		PROPERTY_STRING(1) ,
		PROPERTY_NUMBER(2) ,
		PROPERTY_BOOL(3) ,
		PROPERTY_PATH(4);

		private final int value;
		@Override public int getValue() { return( value ); };
		private PropertyValueType( int value ) { this.value = value; };
		public static PropertyValueType getValue( Integer value , boolean required ) throws Exception { return( DBEnumTypes.getValue( PropertyValueType.class , value , required , null ) ); };
		public static PropertyValueType getValue( String value , boolean required ) throws Exception { return( DBEnumTypes.getValue( PropertyValueType.class , value , required , null ) ); };
	};

	//#################################################
	// implementation
    public static <T extends Enum<T> & DBEnumInterface> T getValue( Class<T> type , Integer value , boolean required , T unknownValue ) throws Exception {
		if( value == 0 ) {
			if( required )
				Common.exit1( _Error.MissingEnumIntType1 , "missing enum integer type value, enum=" + type.getName() , type.getName() );
			return( unknownValue );
		}
		
    	for( T t : type.getEnumConstants() ) {
    		if( t.getValue() == value )
    			return( t );
    	}
    	
		Common.exit2( _Error.InvalidEnumIntType2 , "invalid enum item=" + value + ", enum=" + type.getName() , "" + value , type.getName() );
		return( null );
    }

    public static <T extends Enum<T> & DBEnumInterface> T getValue( Class<T> type , String value , boolean required , T unknownValue ) throws Exception {
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
	
}

