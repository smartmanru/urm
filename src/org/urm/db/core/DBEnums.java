package org.urm.db.core;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
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
		STRING(1,new String[] {"PROPERTY_STRING"}) ,
		NUMBER(2,new String[] {"PROPERTY_NUMBER"}) ,
		BOOL(3,new String[] {"PROPERTY_BOOL"}) ,
		PATH(4,new String[] {"PROPERTY_PATH"});

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumParamValueType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumParamValueType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumParamValueType.class , value , required , UNKNOWN ) ); };
		public static DBEnumParamValueType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumParamValueType.class , value , required , UNKNOWN ) ); };
	};

	public enum DBEnumParamValueSubType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		DEFAULT(1,null) ,
		PATHABSOLUTE(20,null) ,
		PATHABSOLUTEWINDOWS(2,null) ,
		PATHABSOLUTELINUX(3,null) ,
		PATHRELATIVE(4,null) ,
		PATHRELATIVEWINDOWS(5,null) ,
		PATHRELATIVELINUX(6,null) ,
		PATHABSOLUTEENGINE(10,null) ,
		PATHRELATIVEENGINE(11,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumParamValueSubType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumParamValueSubType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumParamValueSubType.class , value , required , UNKNOWN ) ); };
		public static DBEnumParamValueSubType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumParamValueSubType.class , value , required , UNKNOWN ) ); };
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
		
		public boolean isLinux() { return( value == LINUX.value ); };
		public boolean isWindows() { return( value == WINDOWS.value ); };
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
		MIRROR(7,null) ,
		BUILDER(8,null) ,
		BUILDREG(9,null) ,
		DATACENTER(10,null) ,
		NETWORK(11,null) ,
		HOST(12,null) ,
		HOSTACCOUNT(13,null) ,
		AUTH_GROUP(15,null) ,
		AUTH_USER(16,null) ,
		BASE_CATEGORY(20,null) ,
		BASE_GROUP(21,null) ,
		BASE_ITEM(22,null) ,
		APPSYSTEM(30,null) ,
		APPPRODUCT(31,null) ,
		DBSCHEMA(50,null) ,
		META(101,null) ,
		META_UNIT(102,null) ,
		META_SCHEMA(103,null) ,
		META_SOURCESET(104,null) ,
		META_SOURCEPROJECT(105,null) ,
		META_SOURCEITEM(106,null) ,
		META_DOC(107,null) ,
		META_POLICY(108,null) ,
		META_DIST_DELIVERY(151,null) ,
		META_DIST_BINARYITEM(152,null) ,
		META_DIST_CONFITEM(153,null) ,
		META_DIST_COMPONENT(154,null) ,
		META_DIST_COMPITEM(155,null) ,
		ENVIRONMENT(156,null) ,
		ENVIRONMENT_SEGMENT(157,null) ,
		ENVIRONMENT_SERVER(158,null) ,
		ENVIRONMENT_NODE(159,null) ,
		ENVIRONMENT_STARTGROUP(160,null) ,
		ENVIRONMENT_DEPLOYMENT(161,null) ,
		ENVIRONMENT_MONTARGET(162,null) ,
		ENVIRONMENT_MONITEM(163,null);

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
	
	public enum DBEnumDbmsType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		ORACLE(1,null) ,
		POSTGRESQL(2,null) ,
		FIREBIRD(3,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumDbmsType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumDbmsType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumDbmsType.class , value , required , UNKNOWN ) ); };
		public static DBEnumDbmsType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumDbmsType.class , value , required , UNKNOWN ) ); };
	};
	
	public enum DBEnumProjectType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		BUILDABLE(1,null) ,
		PREBUILT_NEXUS(2,null) ,
		PREBUILT_VCS(3,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumProjectType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumProjectType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumProjectType.class , value , required , UNKNOWN ) ); };
		public static DBEnumProjectType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumProjectType.class , value , required , UNKNOWN ) ); };
	};
	
	public enum DBEnumSourceItemType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		BASIC(1,null) ,
		DIRECTORY(2,null) ,
		STATICWAR(3,null) ,
		PACKAGE(4,null) ,
		CUSTOM(5,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumSourceItemType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumSourceItemType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumSourceItemType.class , value , required , UNKNOWN ) ); };
		public static DBEnumSourceItemType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumSourceItemType.class , value , required , UNKNOWN ) ); };
	};
	
	public enum DBEnumBinaryItemType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		BINARY(1,null) ,
		PACKAGE(2,null) ,
		STATICWAR(3,null) ,
		ARCHIVE_DIRECT(4,null) ,	// deploydir = archive/content
		ARCHIVE_CHILD(5,null) ,		// deploydir/archivename = archive/archivename/fullcontent
		ARCHIVE_SUBDIR(6,null);		// deploydir/archivename = archive/fullcontent

	    public boolean isArchive() {
			if( this == ARCHIVE_CHILD || 
				this == ARCHIVE_DIRECT || 
				this == ARCHIVE_SUBDIR )
				return( true );
			return( false );
	    }
		
		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumBinaryItemType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumBinaryItemType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumBinaryItemType.class , value , required , UNKNOWN ) ); };
		public static DBEnumBinaryItemType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumBinaryItemType.class , value , required , UNKNOWN ) ); };
	};
	
	public enum DBEnumDeployVersionType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		NONE(1,null) ,
		IGNORE(2,null) ,
		MIDDASH(3,null) ,
		MIDPOUND(4,null) ,
		PREFIX(5,null);

		public String getVersionPattern( String basename , String ext ) throws Exception {
			String value = "";
			if( this == NONE || this == IGNORE )
				value = basename + ext;
			else if( this == MIDPOUND )
				value = Common.getLiteral( basename ) + "##[0-9.]+.*" + Common.getLiteral( ext );
			else if( this == MIDDASH )
				value = basename + "-[0-9.]+.*" + ext;
			else if( this == PREFIX )
				value = "[0-9.]+-" + Common.getLiteral( basename + ext );
			else
				Common.exitUnexpected();
			
			return( value );
		}
		
		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumDeployVersionType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumDeployVersionType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumDeployVersionType.class , value , required , UNKNOWN ) ); };
		public static DBEnumDeployVersionType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumDeployVersionType.class , value , required , UNKNOWN ) ); };
	};
	
	public enum DBEnumItemOriginType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		MANUAL(1,null) ,
		DERIVED(2,null) ,
		BUILD(3,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumItemOriginType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumItemOriginType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumItemOriginType.class , value , required , UNKNOWN ) ); };
		public static DBEnumItemOriginType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumItemOriginType.class , value , required , UNKNOWN ) ); };
	};
	
	public enum DBEnumConfItemType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		FILES(1,null) ,
		DIR(2,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumConfItemType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumConfItemType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumConfItemType.class , value , required , UNKNOWN ) ); };
		public static DBEnumConfItemType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumConfItemType.class , value , required , UNKNOWN ) ); };
	};
	
	public enum DBEnumCompItemType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		BINARY(1,null) ,
		CONF(2,null) ,
		SCHEMA(3,null) ,
		WSDL(4,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumCompItemType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumCompItemType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumCompItemType.class , value , required , UNKNOWN ) ); };
		public static DBEnumCompItemType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumCompItemType.class , value , required , UNKNOWN ) ); };
	};
	
	public enum DBEnumMonItemType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		CHECKURL(1,null) ,
		CHECKWS(2,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumMonItemType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumMonItemType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumMonItemType.class , value , required , UNKNOWN ) ); };
		public static DBEnumMonItemType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumMonItemType.class , value , required , UNKNOWN ) ); };
	};
	
	public enum DBEnumDocCategoryType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		DESIGNTIME(1,null) ,
		USETIME(2,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumDocCategoryType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumDocCategoryType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumDocCategoryType.class , value , required , UNKNOWN ) ); };
		public static DBEnumDocCategoryType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumDocCategoryType.class , value , required , UNKNOWN ) ); };
	};
	
	public enum DBEnumEnvType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		PRODUCTION(1,null) ,
		UAT(2,null) ,
		DEVELOPMENT(3,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumEnvType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumEnvType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumEnvType.class , value , required , UNKNOWN ) ); };
		public static DBEnumEnvType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumEnvType.class , value , required , UNKNOWN ) ); };
	};
	
	public enum DBEnumServerRunType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		DATABASE(1,null) ,
		APP(2,null) ,
		WEBUI(3,null) ,
		WEBAPP(4,null) ,
		COMMAND(5,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumServerRunType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumServerRunType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumServerRunType.class , value , required , UNKNOWN ) ); };
		public static DBEnumServerRunType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumServerRunType.class , value , required , UNKNOWN ) ); };
	};
	
	public enum DBEnumServerDependencyType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		NLB(1,null) ,
		PROXY(2,null) ,
		STATIC(3,null) ,
		SUBORDINATE(4,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumServerDependencyType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumServerDependencyType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumServerDependencyType.class , value , required , UNKNOWN ) ); };
		public static DBEnumServerDependencyType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumServerDependencyType.class , value , required , UNKNOWN ) ); };
	};
	
	public enum DBEnumServerDeploymentType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		BINARY(1,null) ,
		CONF(2,null) ,
		SCHEMA(3,null) ,
		COMP(4,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumServerDeploymentType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumServerDeploymentType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumServerDeploymentType.class , value , required , UNKNOWN ) ); };
		public static DBEnumServerDeploymentType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumServerDeploymentType.class , value , required , UNKNOWN ) ); };
	};
	
	public enum DBEnumDeployModeType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		MANUAL(1,null) ,
		COLD(2,null) ,
		HOT(3,null) ,
		LINKS_SINGLEDIR(4,null) ,
		LINKS_MULTIDIR(5,null) ,
		COPYONLY(6,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumDeployModeType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumDeployModeType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumDeployModeType.class , value , required , UNKNOWN ) ); };
		public static DBEnumDeployModeType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumDeployModeType.class , value , required , UNKNOWN ) ); };
	};
	
	public enum DBEnumNodeType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		SELF(1,null) ,
		ADMIN(2,null) ,
		SLAVE(3,null);

		private final int value;
		private String[] synonyms;
		@Override public int code() { return( value ); };
		@Override public String[] synonyms() { return( synonyms ); };
		private DBEnumNodeType( int value , String[] synonyms ) { this.value = value; this.synonyms = synonyms; };
		public static DBEnumNodeType getValue( Integer value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumNodeType.class , value , required , UNKNOWN ) ); };
		public static DBEnumNodeType getValue( String value , boolean required ) throws Exception { return( DBEnums.getValue( DBEnumNodeType.class , value , required , UNKNOWN ) ); };
	};
	
	public enum DBEnumObjectVersionType implements DBEnumInterface {
		UNKNOWN(0,null) ,
		APP(1,null) ,
		CORE(2,null) ,
		LOCAL(3,null) ,
		SYSTEM(4,null) ,
		PRODUCT(5,null) ,
		ENVIRONMENT(6,null);

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
		RC(11,null) ,
		ENGINE(12,null) ,
		PRODUCTCTX(61,null) ,
		PRODUCTDEFS(13,null) ,
		PRODUCTBUILD(14,null) ,
		BASEGROUP(15,null) ,
		BASEITEM(16,null) ,
		MONITORING(17,null) ,
		LIFECYCLE(18,null) ,
		LIFECYCLEPHASE(19,null) ,
		RESOURCE(20,null) ,
		MIRROR(21,null) ,
		BUILDER(22,null) ,
		BUILDREG(23,null) ,
		LDAPSETTINGS(24,null) ,
		DATACENTER(31,null) ,
		NETWORK(32,null) ,
		HOST(33,null) ,
		ACCOUNT(34,null) ,
		AUTHGROUP(35,null) ,
		AUTHUSER(36,null) ,
		APPSYSTEM(41,null) ,
		APPPRODUCT(42,null) ,
		PRODUCT(50,null) ,
		RC_CUSTOM(111,null) ,
		ENGINE_CUSTOM(112,null) ,
		BASEITEM_CUSTOM(116,null) ,
		SYSTEM_CUSTOM(141,null) ,
		PRODUCT_CUSTOM(150,null) ,
		PRODUCT_VERSION(151,null) ,
		PRODUCT_MONITORING(152,null) ,
		PRODUCT_UNIT(153,null) ,
		PRODUCT_SCHEMA(154,null) ,
		PRODUCT_SOURCESET(155,null) ,
		PRODUCT_SOURCEPROJECT(156,null) ,
		PRODUCT_SOURCEITEM(157,null) ,
		PRODUCT_DOC(158,null) ,
		PRODUCT_POLICY(159,null) ,
		PRODUCT_DIST_DELIVERY(201,null) ,
		PRODUCT_DIST_BINARYITEM(202,null) ,
		PRODUCT_DIST_CONFITEM(203,null) ,
		PRODUCT_DIST_COMPONENT(204,null) ,
		PRODUCT_DIST_COMPITEM(205,null) ,
		ENV_PRIMARY(301,null) ,
		ENV_EXTRA(302,null) ,
		ENV_CUSTOM(303,null) ,
		ENV_SEGMENT_PRIMARY(311,null) ,
		ENV_SEGMENT_STARTGROUP(312,null) ,
		ENV_SEGMENT_CUSTOM(313,null) ,
		ENV_SEGMENT_MONTARGET(314,null) ,
		ENV_SEGMENT_MONITEM(315,null) ,
		ENV_SERVER_PRIMARY(321,null) ,
		ENV_SERVER_EXTRA(322,null) ,
		ENV_SERVER_CUSTOM(323,null) ,
		ENV_DEPLOYMENT(325,null) ,
		ENV_NODE_PRIMARY(331,null) ,
		ENV_NODE_CUSTOM(333,null);

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
		PRODUCTCTX(4,null),
		PRODUCTDEFS(3,null),
		BUILDMODE_COMMON(10,null),
		BUILDMODE_BRANCH(11,null) ,
		BUILDMODE_MAJORBRANCH(12,null) ,
		BUILDMODE_TRUNK(13,null) ,
		BUILDMODE_DEVBRANCH(14,null) ,
		BUILDMODE_DEVTRUNK(15,null) ,
		MONITORING(20,null) ,
		LDAP(50,null) ,
		METACORE(101,null),
		METAMON(102,null),
		SERVERBASE(122,null),
		DEFAULT(200,null);

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
		new DBEnumInfo( DBEnumOwnerStatusType.class , 510 ) ,
		new DBEnumInfo( DBEnumObjectType.class , 511 ) ,
		new DBEnumInfo( DBEnumObjectVersionType.class , 512 ) ,
		new DBEnumInfo( DBEnumBaseCategoryType.class , 513 ) , 
		new DBEnumInfo( DBEnumBaseSrcFormatType.class , 514 ) ,
		new DBEnumInfo( DBEnumBaseSrcType.class , 515 ) , 
		new DBEnumInfo( DBEnumParamValueType.class , 516 ) ,
		new DBEnumInfo( DBEnumResourceType.class , 517 ) ,
		new DBEnumInfo( DBEnumServerAccessType.class , 518 ) ,
		new DBEnumInfo( DBEnumOSType.class , 519 ) ,
		new DBEnumInfo( DBEnumBuilderMethodType.class , 520 ) ,
		new DBEnumInfo( DBEnumBuilderTargetType.class , 521 ) ,
		new DBEnumInfo( DBEnumMirrorType.class , 522 ) ,
		new DBEnumInfo( DBEnumLifecycleType.class , 523 ) ,
		new DBEnumInfo( DBEnumLifecycleStageType.class , 524 ) ,
		new DBEnumInfo( DBEnumBuildModeType.class , 525 ) ,
		new DBEnumInfo( DBEnumParamEntityType.class , 526 ) ,
		new DBEnumInfo( DBEnumParamRoleType.class , 527 ) ,
		new DBEnumInfo( DBEnumChatType.class , 528 ) ,
		new DBEnumInfo( DBEnumParamValueSubType.class , 529 ) ,
		new DBEnumInfo( DBEnumDbmsType.class , 530 ) ,
		new DBEnumInfo( DBEnumProjectType.class , 531 ) ,
		new DBEnumInfo( DBEnumSourceItemType.class , 532 ) ,
		new DBEnumInfo( DBEnumBinaryItemType.class , 533 ) ,
		new DBEnumInfo( DBEnumDeployVersionType.class , 534 ) ,
		new DBEnumInfo( DBEnumItemOriginType.class , 535 ) ,
		new DBEnumInfo( DBEnumConfItemType.class , 536 ) ,
		new DBEnumInfo( DBEnumCompItemType.class , 537 ) ,
		new DBEnumInfo( DBEnumMonItemType.class , 538 ) ,
		new DBEnumInfo( DBEnumDocCategoryType.class , 539 ) ,
		new DBEnumInfo( DBEnumEnvType.class , 540 ) ,
		new DBEnumInfo( DBEnumServerRunType.class , 541 ) ,
		new DBEnumInfo( DBEnumServerDependencyType.class , 542 ) ,
		new DBEnumInfo( DBEnumServerDeploymentType.class , 543 ) ,
		new DBEnumInfo( DBEnumDeployModeType.class , 544 ) ,
		new DBEnumInfo( DBEnumNodeType.class , 545 )
	}; 

	private static String prefix = "DBEnum";
	private static String suffix = "Type";
	
	private static <T extends Enum<T> & DBEnumInterface> T getValue( Class<T> type , Integer value , boolean required , T unknownValue ) throws Exception {
		if( value == 0 ) {
			if( required )
				Common.exit1( _Error.MissingEnumIntType1 , "missing required enum integer type value, enum=" + type.getSimpleName() , type.getSimpleName() );
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
				Common.exit1( _Error.MissingEnumStringType1 , "missing required enum string type value, enum=" + type.getSimpleName() , type.getSimpleName() );
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
    	connection.modify( DBQueries.MODIFY_ENUMS_DROP0 );
    	
    	for( DBEnumInfo e : enums )
    		addEnum( connection , e.enumClass );
    }

    public static void addEnum( DBConnection connection , Class<?> ec ) throws Exception {
    	DBEnumInfo ef = null;
    	for( DBEnumInfo e : enums ) {
    		if( e.enumClass == ec ) {
    			ef = e;
    			break;
    		}
    	}
    	
    	if( ef == null )
    		Common.exitUnexpected();
    	
		String name = getEnumName( ec );
		int enumId = ef.enumID;
		if( !connection.modify( DBQueries.MODIFY_ENUMS_ADD4 , new String[] { "0" , "" + enumId , EngineDB.getString( name ) , "" + EngineDB.APP_VERSION } ) )
			Common.exitUnexpected();
		
		for( Object object : ec.getEnumConstants() ) {
			DBEnumInterface oi = ( DBEnumInterface )object;
			int elementValue = oi.code();
			Enum<?> ev = ( Enum<?> )object;
			String elementName = ev.name().toLowerCase();
			
    		if( !connection.modify( DBQueries.MODIFY_ENUMS_ADD4 , new String[] { "" + enumId , "" + elementValue , EngineDB.getString( elementName ) , "" + EngineDB.APP_VERSION } ) )
    			Common.exitUnexpected();
		}
    }
    
    public static void addEnumItem( DBConnection connection , DBEnumInterface oi ) throws Exception {
    	Enum<?> ei = ( Enum<?> )oi;
    	Class<?> ec = ei.getClass();
    	
    	DBEnumInfo ef = null;
    	for( DBEnumInfo e : enums ) {
    		if( e.enumClass == ec ) {
    			ef = e;
    			break;
    		}
    	}
    	
    	if( ef == null )
    		Common.exitUnexpected();
    	
    	String elementName = ei.name().toLowerCase();
    	if( !connection.modify( DBQueries.MODIFY_ENUMS_ADD4 , new String[] { "" + ef.enumID , "" + oi.code() , EngineDB.getString( elementName ) , "" + EngineDB.APP_VERSION } ) )
			Common.exitUnexpected();
    }
    
    public static Class<?> getEnum( String name ) throws Exception {
    	for( DBEnumInfo e : enums ) {
    		String ename = getEnumName( e.enumClass );
    		if( ename.equals( name ) )
    			return( e.enumClass );
    	}
    	
    	return( null );
    }
    
    public static int getEnumCode( Class<?> type , String value ) throws Exception {
		int code = DBEnums.VALUE_UNKNOWN;
		if( value != null && !value.isEmpty() ) {
			Enum<?> e = DBEnums.getEnumValue( type , value );
			if( e == null )
				Common.exit2( _Error.InvalidEnumStringType2 , "invalid enum item=" + value + ", enum=" + type.getSimpleName() , value , type.getSimpleName() );
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
		String valueCheck = Common.xmlToEnumValue( value );
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

