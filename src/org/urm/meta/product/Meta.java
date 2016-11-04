package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.engine.ServerSession;
import org.urm.engine.ServerTransaction;
import org.urm.meta.ServerLoader;
import org.urm.meta.ServerObject;
import org.urm.meta.ServerProductMeta;
import org.urm.meta.product.MetaDesign.VarELEMENTTYPE;
import org.urm.meta.product.MetaDesign.VarLINKTYPE;
import org.w3c.dom.Node;

public class Meta extends ServerObject {

	public String name;
	public ServerSession session;
	
	private ServerLoader loader;
	private ServerProductMeta storage;

	private MetaProductVersion version;
	private MetaProductSettings product;
	private MetaDatabase database;
	private MetaDistr distr;
	private MetaSource sources;
	private MetaMonitoring monitoring;
	
	static String[] configurableExtensions = {
		"cmd" , "sh" , "xml" , "txt" , "properties" , "conf" , "config" , "xconf" , "groovy" , "sql" , "yml" 
	};

	static String S_REDIST_ARCHIVE_TYPE_DIRECT = "direct";
	static String S_REDIST_ARCHIVE_TYPE_CHILD = "child";
	static String S_REDIST_ARCHIVE_TYPE_SUBDIR = "subdir";
	
	public enum VarCATEGORY {
		UNKNOWN ,
		BUILD ,
		MANUAL , 
		PREBUILT ,
		CONFIG ,
		DB ,
		ENV
	};

	public enum VarDEPLOYITEMTYPE {
		UNKNOWN ,
		BINARY ,
		CONF ,
		SCHEMA ,
		COMP
	};
	
	public enum VarDEPLOYITEMTYPENOCOMP {
		UNKNOWN ,
		BINARY ,
		CONF ,
		SCHEMA
	};
	
	public enum VarITEMVERSION {
		UNKNOWN ,
		NONE ,
		IGNORE ,
		MIDDASH ,
		MIDPOUND ,
		PREFIX
	};
	
	public enum VarITEMSRCTYPE {
		UNKNOWN ,
		NEXUS ,
		NUGET ,
		NUGET_PLATFORM ,
		STATICWAR ,
		SVN ,
		SVNOLD ,
		SVNNEW ,
		GENERATED
	};
	
	public enum VarBUILDMODE {
		UNKNOWN ,
		BRANCH ,
		MAJORBRANCH ,
		TRUNK ,
		DEVBRANCH ,
		DEVTRUNK
	};
	
	public enum VarSERVERACCESSTYPE {
		UNKNOWN ,
		SERVICE ,
		PACEMAKER ,
		GENERIC ,
		MANUAL
	};

	public enum VarSERVERRUNTYPE {
		UNKNOWN ,
		DATABASE ,
		APP ,
		WEBUI ,
		WEBAPP ,
		COMMAND
	};

	public enum VarNODETYPE {
		UNKNOWN ,
		SELF ,
		ADMIN ,
		SLAVE
	};
	
	public enum VarDEPLOYMODE {
		UNKNOWN ,
		MANUAL , 
		COLD , 
		HOT ,
		LINKS_SINGLEDIR ,
		LINKS_MULTIDIR ,
		COPYONLY
	};
	
	public enum VarDBMSTYPE {
		UNKNOWN ,
		ORACLE ,
		POSTGRESQL ,
		FIREBIRD
	};

	public enum VarSESSIONTYPE {
		UNKNOWN ,
		UNIXLOCAL ,
		UNIXREMOTE ,
		UNIXFROMWINDOWS ,
		WINDOWSLOCAL ,
		WINDOWSREMOTE ,
		WINDOWSFROMUNIX
	};

	public enum VarDISTITEMTYPE {
		UNKNOWN ,
		BINARY ,
		DOTNETPKG ,
		STATICWAR ,
		ARCHIVE_DIRECT ,	// deploydir = archive/content
		ARCHIVE_CHILD ,		// deploydir/archivename = archive/archivename/fullcontent
		ARCHIVE_SUBDIR		// deploydir/archivename = archive/content/fullcontent
	};
	
	public enum VarDISTITEMORIGIN {
		UNKNOWN ,
		MANUAL ,
		BUILD ,
		DISTITEM
	};
	
	public enum VarPROCESSMODE {
		UNKNOWN ,
		STARTED ,
		STARTING ,
		ERRORS ,
		STOPPED
	};

	public enum VarCONFITEMTYPE {
		UNKNOWN ,
		FILES ,
		DIR
	};
	
	public enum VarCONTENTTYPE {
		UNKNOWN ,
		BINARYCOLDDEPLOY ,
		BINARYHOTDEPLOY ,
		BINARYCOPYONLY ,
		CONFCOLDDEPLOY ,
		CONFHOTDEPLOY ,
		CONFCOPYONLY
	};

	public enum VarNAMETYPE {
		UNKNOWN ,
		ANY ,
		ALPHANUM ,
		ALPHANUMDOT ,
		ALPHANUMDOTDASH
	};
	
	public enum VarARCHIVETYPE {
		UNKNOWN ,
		TARGZ ,
		TAR ,
		ZIP
	};
	
	public enum VarBASESRCTYPE {
		UNKNOWN ,
		ARCHIVE_LINK ,
		ARCHIVE_DIRECT ,
		NODIST ,
		INSTALLER
	};
	
	public enum VarBASESRCFORMAT {
		UNKNOWN ,
		TARGZ_SINGLEDIR ,
		ZIP_SINGLEDIR ,
		SINGLEFILE
	};
	
	private static String configurableExtensionsFindOptions = createConfigurableExtensions();
	
	public Meta( ServerProductMeta storage , ServerSession session ) {
		super( null );
		this.storage = storage;
		this.loader = storage.loader;
		this.session = session;
		name = storage.name;
	}
	
	public void replaceStorage( ActionBase action , ServerProductMeta storage ) throws Exception {
		loader.releaseSessionProductMetadata( action , this , false );
		
		// clear old refs
		version = null;
		product = null;
		database = null;
		distr = null;
		sources = null;
		monitoring = null;
		
		this.storage = storage;
		storage.addSessionMeta( this );
		session.addProductMeta( this );
	}

	public boolean isCorrect() {
		if( storage.loadFailed )
			return( false );
		return( true );
	}
	
	private static String createConfigurableExtensions() {
		String configurableExtensionsFindOptions = "";
		for( int k = 0; k < configurableExtensions.length; k++ ) {
			if( k > 0 )
				configurableExtensionsFindOptions += " -o ";
			configurableExtensionsFindOptions += "-name \"*." + configurableExtensions[ k ] + "\"";
		}
		return( configurableExtensionsFindOptions );
	}
	
	public void setVersion( MetaProductVersion version ) {
		this.version = version;
	}
	
	public void setProduct( MetaProductSettings product ) {
		this.product = product;
	}
	
	public void setDistr( MetaDistr distr ) {
		this.distr = distr;
	}
	
	public void setDatabase( MetaDatabase database ) {
		this.database = database;
	}
	
	public void setSources( MetaSource sources ) {
		this.sources = sources;
	}

	public synchronized ServerProductMeta getStorage( ActionBase action ) throws Exception {
		return( storage );
	}

	public synchronized MetaProductVersion getVersion( ActionBase action ) throws Exception {
		if( version == null )
			version = loader.loadVersion( action.actionInit , storage );
		return( version );
	}
	
	public synchronized MetaProductSettings getProductSettings( ActionBase action ) throws Exception {
		if( product == null )
			product = loader.loadProduct( action.actionInit , storage );
		return( product );
	}
	
	public synchronized MetaDatabase getDatabase( ActionBase action ) throws Exception {
		if( database == null )
			database = loader.loadDatabase( action.actionInit , storage );
		return( database );
	}

	public synchronized MetaDistr getDistr( ActionBase action ) throws Exception {
		if( distr == null )
			distr = loader.loadDistr( action.actionInit , storage );
		return( distr );
	}

	public synchronized MetaSource getSources( ActionBase action ) throws Exception {
		if( sources == null )
			sources = loader.loadSources( action.actionInit , storage );
		return( sources );
	}

	public synchronized MetaMonitoring getMonitoring( ActionBase action ) throws Exception {
		if( monitoring == null )
			monitoring = loader.loadMonitoring( action.actionInit , storage );
		return( monitoring );
	}
	
	public synchronized MetaDesign getDesignData( ActionBase action , String fileName ) throws Exception {
		return( loader.loadDesignData( action.actionInit , storage , fileName ) );
	}
	
	public String[] getEnvList() {
		return( storage.getEnvironmentNames() );
	}
	
	public synchronized MetaEnv getEnvData( ActionBase action , String envFile , boolean loadProps ) throws Exception {
		return( loader.loadEnvData( action.actionInit , storage , envFile , loadProps ) );
	}
	
	public MetaEnv findEnv( String envId ) {
		return( storage.findEnvironment( envId ) );
	}
	
	public synchronized MetaEnv getEnv( ActionBase action , String envId ) throws Exception {
		getStorage( action );
		return( storage.findEnvironment( envId ) );
	}
	
	public static String getConfigurableExtensionsFindOptions( ActionBase action ) throws Exception {
		return( configurableExtensionsFindOptions );
	}

	public static boolean isConfigurableFile( ActionBase action , String filePath ) throws Exception {
		String ext = Common.getFileExtension( filePath );
		for( int k = 0; k < configurableExtensions.length; k++ )
			if( ext.equals( configurableExtensions[ k ] ) )
				return( true );
		return( false );
	}

	public static String[] getConfigurableExtensions( ActionBase action ) throws Exception {
		return( configurableExtensions );
	}

	public static VarOSTYPE getOSType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingOSType0 , "missing operating system type" );
			return( VarOSTYPE.UNKNOWN );
		}
		
		VarOSTYPE value = null;
		try {
			value = VarOSTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidOSType1 , "invalid OS type=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarCATEGORY getCategory( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingCategory0 , "missing category" );
			return( VarCATEGORY.UNKNOWN );
		}

		VarCATEGORY value = null;
		try {
			value = VarCATEGORY.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidCategory1 , "invalid category=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarSERVERACCESSTYPE getServerAccessType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingServerAccessType0 , "missing server access type" );
			return( VarSERVERACCESSTYPE.UNKNOWN );
		}
		
		VarSERVERACCESSTYPE value = null;
		try {
			value = VarSERVERACCESSTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidServerAccessType1 , "invalid server access type=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarSERVERRUNTYPE getServerRunType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingServerRunType0 , "missing server run type" );
			return( VarSERVERRUNTYPE.UNKNOWN );
		}
		
		VarSERVERRUNTYPE value = null;
		try {
			value = VarSERVERRUNTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidServerRunType1 , "invalid server run type=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarNODETYPE getNodeType( String ID , VarNODETYPE defValue ) throws Exception {
		if( ID.isEmpty() )
			return( defValue );
		
		VarNODETYPE value = null;
		try {
			value = VarNODETYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidNodeType1 , "invalid node type=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarDEPLOYMODE getDeployMode( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingDeployType0 , "missing deploy type" );
			return( VarDEPLOYMODE.UNKNOWN );
		}
		
		VarDEPLOYMODE value = null;
		try {
			value = VarDEPLOYMODE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidDeployType1 , "invalid deploytype=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarITEMSRCTYPE getItemSrcType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingSourceItemType0 , "missing source item type" );
			return( VarITEMSRCTYPE.UNKNOWN );
		}
		
		VarITEMSRCTYPE value = null;
		try {
			value = VarITEMSRCTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidSourceItemType1 , "invalid source item type=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarDISTITEMTYPE getItemDistType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingDistributiveItemType0 , "missing distributive item type" );
			return( VarDISTITEMTYPE.UNKNOWN );
		}
		
		VarDISTITEMTYPE value = null;
		try {
			value = VarDISTITEMTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidDistributiveItemType1 , "invalid distributive item type=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarDISTITEMORIGIN getItemDistOrigin( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingDistItemSource0 , "missing distributive item source" );
			return( VarDISTITEMORIGIN.UNKNOWN );
		}
		
		VarDISTITEMORIGIN value = null;
		try {
			value = VarDISTITEMORIGIN.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidDistributiveItemSource1 , "invalid distributive item source=" + ID , ID );
		}
		
		if( value == null )
			Common.exit0( _Error.MissingDistributiveItemSource0 , "missing distributive item source" );
		
		return( value );
	}
	
	public static VarDEPLOYITEMTYPE getDeployItemType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingDeployItemType0 , "missing deploy item type" );
			return( VarDEPLOYITEMTYPE.UNKNOWN );
		}
		
		VarDEPLOYITEMTYPE value = null;
		try {
			value = VarDEPLOYITEMTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidDeployItemType1 , "invalid deploy item type=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarBUILDMODE getBuildMode( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingBuildMode0 , "missing build mode" );
			return( VarBUILDMODE.UNKNOWN );
		}
		
		VarBUILDMODE value = null;
		try {
			value = VarBUILDMODE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidBuildMode1 , "invalid build mode=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarDBMSTYPE getDbmsType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingDbmsType0 , "missing DBMS type" );
			return( VarDBMSTYPE.UNKNOWN );
		}
		
		VarDBMSTYPE value = null;		
		try {
			value = VarDBMSTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidDbmsType1 , "invalid dbmstype=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarCONFITEMTYPE getConfItemType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingConfItemType0 , "missing configuration item type" );
			return( VarCONFITEMTYPE.UNKNOWN );
		}
		
		VarCONFITEMTYPE value = null;		
		try {
			value = VarCONFITEMTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidConfItemType1 , "invalid confitemtype=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarBASESRCTYPE getBaseSrcType( String TYPE , boolean required ) throws Exception {
		if( TYPE.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingBaseSrcType0 , "missing base srctype" );
			return( VarBASESRCTYPE.UNKNOWN );
		}
		
		VarBASESRCTYPE value = null;		
		try {
			value = VarBASESRCTYPE.valueOf( Common.xmlToEnumValue( TYPE ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidBaseSrcType1 , "invalid base srctype=" + TYPE , TYPE );
		}
		
		return( value );
	}

	public static VarBASESRCFORMAT getBaseSrcFormat( String TYPE , boolean required ) throws Exception {
		if( TYPE.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingBaseSrcType0 , "missing base srcformat" );
			return( VarBASESRCFORMAT.UNKNOWN );
		}
		
		VarBASESRCFORMAT value = null;		
		try {
			value = VarBASESRCFORMAT.valueOf( Common.xmlToEnumValue( TYPE ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidBaseSrcFormat1 , "invalid base srcformat=" + TYPE , TYPE );
		}
		
		return( value );
	}

	public static VarITEMVERSION readItemVersionAttr( Node node , String attrName ) throws Exception {
		String ID = ConfReader.getAttrValue( node , attrName , "default" );
		return( getItemVersionType( ID ) );
	}

	public static VarITEMVERSION getItemVersionType( String ID ) throws Exception {
		if( ID.isEmpty() || ID.equals( "default" ) )
			return( VarITEMVERSION.PREFIX );
		
		VarITEMVERSION value = null;
		try {
			value = VarITEMVERSION.valueOf( ID.toUpperCase() );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidItemVersionType1 , "invalid item version type=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarELEMENTTYPE getDesignElementType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingDesignElementType0 , "missing design element type" );
			return( VarELEMENTTYPE.UNKNOWN );
		}
		
		VarELEMENTTYPE value = null;		
		try {
			value = VarELEMENTTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidDesignElementType1 , "invalid design element type=" + ID , ID );
		}
		
		return( value );
	}

	public static VarLINKTYPE getDesignLinkType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingDesignLinkType0 , "missing design link type" );
			return( VarLINKTYPE.UNKNOWN );
		}
		
		VarLINKTYPE value = null;		
		try {
			value = VarLINKTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidDesignLinkType1 , "invalid design link type=" + ID , ID );
		}
		
		return( value );
	}

	public static VarCATEGORY readCategoryAttr( Node node ) throws Exception {
		String value = ConfReader.getAttrValue( node , "category" );
		return( getCategory( value , true ) );
	}
	
	public static boolean isSourceCategory( VarCATEGORY value ) {
		if( value == VarCATEGORY.BUILD || value == VarCATEGORY.PREBUILT )
			return( true );
		return( false );
	}
	
	public static boolean isBuildableCategory( VarCATEGORY value ) {
		if( value == VarCATEGORY.BUILD )
			return( true );
		return( false );
	}
	
	public static VarCATEGORY[] getAllCategories() {
		VarCATEGORY[] categories = { VarCATEGORY.BUILD , VarCATEGORY.MANUAL , VarCATEGORY.PREBUILT , VarCATEGORY.CONFIG , VarCATEGORY.DB };
		return( categories );
	}

	public static VarCATEGORY[] getAllReleaseCategories() {
		VarCATEGORY[] categories = { VarCATEGORY.BUILD , VarCATEGORY.MANUAL , VarCATEGORY.PREBUILT , VarCATEGORY.CONFIG , VarCATEGORY.DB };
		return( categories );
	}

	public static VarCATEGORY[] getAllSourceCategories() {
		VarCATEGORY[] categories = { VarCATEGORY.BUILD , VarCATEGORY.PREBUILT };
		return( categories );
	}

	public static VarCATEGORY[] getAllBuildableCategories() {
		VarCATEGORY[] categories = { VarCATEGORY.BUILD };
		return( categories );
	}
	
	public static String getVersionPattern( ActionBase action , VarITEMVERSION version , String basename , String ext ) throws Exception {
		String value = "";
		if( version == VarITEMVERSION.NONE || version == VarITEMVERSION.IGNORE )
			value = basename + ext;
		else if( version == VarITEMVERSION.MIDPOUND )
			value = Common.getLiteral( basename ) + "##[0-9.]+" + Common.getLiteral( ext );
		else if( version == VarITEMVERSION.MIDDASH )
			value = basename + "-[0-9.]+" + ext;
		else if( version == VarITEMVERSION.PREFIX )
			value = "[0-9.]+-" + Common.getLiteral( basename + ext );
		else
			action.exitUnexpectedState();
		
		return( value );
	}
	
	public static String[] getVersionPatterns( ActionBase action , MetaDistrBinaryItem distItem ) throws Exception {
		String basename = distItem.DISTBASENAME;
		String ext = distItem.EXT;
		if( distItem.deployVersion == VarITEMVERSION.IGNORE ) {
			String[] values = new String[1];
			values[0] = getVersionPattern( action , distItem.deployVersion , basename , ext );
			return( values );
		}

		String[] values = new String[4];
		values[0] = getVersionPattern( action , VarITEMVERSION.NONE , basename , ext );
		values[1] = getVersionPattern( action , VarITEMVERSION.MIDPOUND , basename , ext );
		values[2] = getVersionPattern( action , VarITEMVERSION.MIDDASH , basename , ext );
		values[3] = getVersionPattern( action , VarITEMVERSION.PREFIX , basename , ext );
		return( values );
	}
	
	public static boolean isBinaryContent( ActionBase action , VarCONTENTTYPE c ) throws Exception {
		if( c == VarCONTENTTYPE.BINARYCOLDDEPLOY || c == VarCONTENTTYPE.BINARYCOPYONLY || c == VarCONTENTTYPE.BINARYHOTDEPLOY )
			return( true );
		return( false );
	}
	
	public static boolean isConfContent( ActionBase action , VarCONTENTTYPE c ) throws Exception {
		if( c == VarCONTENTTYPE.CONFCOLDDEPLOY || c == VarCONTENTTYPE.CONFCOPYONLY || c == VarCONTENTTYPE.CONFHOTDEPLOY )
			return( true );
		return( false );
	}
	
    public static String getNameAttr( ActionBase action , Node node , VarNAMETYPE nameType ) throws Exception {
    	String name = ConfReader.getRequiredAttrValue( node , "name" );
    	if( nameType == VarNAMETYPE.ANY )
    		return( name );
    	
    	String mask = null;
    	if( nameType == VarNAMETYPE.ALPHANUM )
    		mask = "[0-9a-zA-Z_]+";
    	else
    	if( nameType == VarNAMETYPE.ALPHANUMDOT )
    		mask = "[0-9a-zA-Z_.]+";
    	else
    	if( nameType == VarNAMETYPE.ALPHANUMDOTDASH )
    		mask = "[0-9a-zA-Z_.-]+";
    	else
    		action.exitUnexpectedState();
    		
    	if( !name.matches( mask ) )
    		action.exit1( _Error.WrongNameAttribute1 , "name attribute should contain only alphanumeric or dot characters, value=" + name , name );
    	return( name );	
    }

    public static boolean isArchive( VarDISTITEMTYPE distItemType ) {
		if( distItemType == VarDISTITEMTYPE.ARCHIVE_CHILD || 
			distItemType == VarDISTITEMTYPE.ARCHIVE_DIRECT || 
			distItemType == VarDISTITEMTYPE.ARCHIVE_SUBDIR )
			return( true );
		return( false );
    }

    public MetaEnv findMetaEnv( MetaEnv env ) {
    	if( env == null )
    		return( null );
    	return( findEnv( env.ID ) );
    }
    
    public MetaEnvDC findMetaEnvDC( MetaEnvDC dc ) {
    	if( dc == null )
    		return( null );
    	MetaEnv env = findMetaEnv( dc.env );
    	if( env == null )
    		return( null );
    	return( env.findDC( dc.NAME ) );
    }
    
    public MetaEnvServer findMetaEnvServer( MetaEnvServer server ) {
    	if( server == null )
    		return( null );
    	MetaEnvDC dc = findMetaEnvDC( server.dc );
    	if( dc == null )
    		return( null );
    	return( dc.findServer( server.NAME ) );
    }

    public MetaEnvServerNode getMetaEnvServerNode( MetaEnvServerNode node ) {
    	if( node == null )
    		return( null );
    	MetaEnvServer server = findMetaEnvServer( node.server );
    	if( server == null )
    		return( null );
    	return( server.findNode( node.POS ) );
    }

	public void deleteBinaryItemFromEnvironments( ServerTransaction transaction , MetaDistrBinaryItem item ) throws Exception {
		for( MetaEnv env : storage.getEnvironments() )
			for( MetaEnvDC dc : env.getDatacenters() )
				for( MetaEnvServer server : dc.getServers() )
					server.reflectDeleteBinaryItem( transaction , item );
	}

	public void deleteConfItemFromEnvironments( ServerTransaction transaction , MetaDistrConfItem item ) throws Exception {
		for( MetaEnv env : storage.getEnvironments() )
			for( MetaEnvDC dc : env.getDatacenters() )
				for( MetaEnvServer server : dc.getServers() )
					server.reflectDeleteConfItem( transaction , item );
	}

	public void deleteComponentFromEnvironments( ServerTransaction transaction , MetaDistrComponent item ) throws Exception {
		for( MetaEnv env : storage.getEnvironments() )
			for( MetaEnvDC dc : env.getDatacenters() )
				for( MetaEnvServer server : dc.getServers() )
					server.reflectDeleteComponent( transaction , item );
	}

	public void deleteDatabaseSchemaFromEnvironments( ServerTransaction transaction , MetaDatabaseSchema schema ) throws Exception {
		for( MetaEnv env : storage.getEnvironments() )
			for( MetaEnvDC dc : env.getDatacenters() )
				for( MetaEnvServer server : dc.getServers() )
					server.reflectDeleteSchema( transaction , schema );
	}

}
