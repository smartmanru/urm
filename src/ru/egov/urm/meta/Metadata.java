package ru.egov.urm.meta;

import org.w3c.dom.Node;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.storage.MetadataStorage;

public class Metadata {
	public MetaProduct product;
	public MetaDistr distr;
	public MetaSource sources;
	
	private String configurableExtensionsFindOptions = "";
	
	static String[] configurableExtensions = {
		"sh" , "xml" , "txt" , "properties" , "conf" , "config" , "xconf" , "groovy" , "sql" , "yml" 
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
	
	public enum VarSERVERTYPE {
		UNKNOWN ,
		SERVICE ,
		GENERIC_WEB ,
		GENERIC_SERVER ,
		GENERIC_COMMAND ,
		GENERIC_NOSSH ,
		DATABASE
	};

	public enum VarDEPLOYTYPE {
		UNKNOWN ,
		NONE , 
		MANUAL , 
		DEFAULT , 
		HOTDEPLOY ,
		LINKS_SINGLEDIR ,
		LINKS_MULTIDIR ,
		STATIC
	};
	
	public enum VarDBMSTYPE {
		UNKNOWN ,
		ORACLE ,
		POSTGRESQL
	};

	public enum VarOSTYPE {
		UNKNOWN ,
		UNIX ,
		WINDOWS
	};

	public enum VarDISTITEMTYPE {
		UNKNOWN ,
		BINARY ,
		DOTNETPKG ,
		WAR ,
		ARCHIVE_DIRECT ,	// deploydir = archive/content
		ARCHIVE_CHILD ,		// deploydir/archivename = archive/archivename/fullcontent
		ARCHIVE_SUBDIR		// deploydir/archivename = archive/content/fullcontent
	};
	
	public enum VarDISTITEMSOURCE {
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
		BINARYCOLDDEPLOY ,
		BINARYHOTDEPLOY ,
		CONFCOLDDEPLOY ,
		CONFHOTDEPLOY
	};

	public enum VarNAMETYPE {
		ANY ,
		ALPHANUM ,
		ALPHANUMDOT ,
		ALPHANUMDOTDASH
	};
	
	public Metadata() {
		configurableExtensionsFindOptions = "";
		for( int k = 0; k < configurableExtensions.length; k++ ) {
			if( k > 0 )
				configurableExtensionsFindOptions += " -o ";
			configurableExtensionsFindOptions += "-name \"*." + configurableExtensions[ k ] + "\"";
		}
	}
	
	public String getConfigurableExtensionsFindOptions( ActionBase action ) throws Exception {
		return( configurableExtensionsFindOptions );
	}
	
	public void loadProduct( ActionBase action ) throws Exception {
		product = new MetaProduct( this );
		MetadataStorage storage = action.artefactory.getMetadataStorage( action ); 
		product.load( action , storage );
	}
	
	public void loadDistr( ActionBase action ) throws Exception {
		distr = new MetaDistr( this );
		MetadataStorage storage = action.artefactory.getMetadataStorage( action ); 
		distr.load( action , storage );
	}

	public MetaEnv loadEnvData( ActionBase action , String envFile , boolean loadProps ) throws Exception {
		if( envFile.isEmpty() )
			action.exit( "environment file name is empty" );
		
		MetaEnv envData = new MetaEnv( this );
		MetadataStorage storage = action.artefactory.getMetadataStorage( action ); 
		envData.load( action , storage , envFile , loadProps );
		return( envData );
	}
	
	public MetaDesign loadDesignData( ActionBase action , String fileName ) throws Exception {
		MetaDesign design = new MetaDesign( this );
		MetadataStorage storage = action.artefactory.getMetadataStorage( action ); 
		design.load( action , storage , fileName );
		return( design );
	}
	
	public void loadSources( ActionBase action ) throws Exception {
		sources = new MetaSource( this );
		MetadataStorage storage = action.artefactory.getMetadataStorage( action ); 
		sources.load( action , storage );
	}

	public MetaMonitoring loadMonitoring( ActionBase action ) throws Exception {
		MetaMonitoring mon = new MetaMonitoring( this );
		MetadataStorage storage = action.artefactory.getMetadataStorage( action ); 
		mon.load( action , storage );
		return( mon );
	}
	
	public boolean isConfigurableFile( ActionBase action , String filePath ) throws Exception {
		String ext = Common.getFileExtension( filePath );
		for( int k = 0; k < configurableExtensions.length; k++ )
			if( ext.equals( configurableExtensions[ k ] ) )
				return( true );
		return( false );
	}

	public String[] getConfigurableExtensions( ActionBase action ) throws Exception {
		return( configurableExtensions );
	}

	public VarCATEGORY getCategory( ActionBase action , String ID ) throws Exception {
		VarCATEGORY value = null;
		try {
			value = VarCATEGORY.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			action.exit( "invalid category=" + ID );
		}
		if( value == null )
			action.exit( "missing category" );
		
		return( value );
	}
	
	public VarSERVERTYPE getServerType( ActionBase action , String ID ) throws Exception {
		VarSERVERTYPE value = null;
		try {
			value = VarSERVERTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			action.exit( "invalid server type=" + ID );
		}
		
		if( value == null )
			action.exit( "missing server type" );
		
		return( value );
	}
	
	public VarOSTYPE getOSType( ActionBase action , String ID ) throws Exception {
		VarOSTYPE value = null;
		try {
			value = VarOSTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			action.exit( "invalid operation system type=" + ID );
		}
		
		if( value == null )
			action.exit( "missing operating system type" );
		
		return( value );
	}
	
	public VarDEPLOYTYPE getDeployType( ActionBase action , String ID ) throws Exception {
		VarDEPLOYTYPE value = null;
		try {
			value = VarDEPLOYTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			action.exit( "invalid deploytype=" + ID );
		}
		
		if( value == null )
			action.exit( "unknown deploy type=" + ID );
		
		return( value );
	}
	
	public VarITEMSRCTYPE getItemSrcType( ActionBase action , String ID ) throws Exception {
		VarITEMSRCTYPE value = null;
		try {
			value = VarITEMSRCTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			action.exit( "invalid source item type=" + ID );
		}
		
		if( value == null )
			action.exit( "unknown source item type=" + ID );
		
		return( value );
	}
	
	public VarDISTITEMTYPE getItemDistType( ActionBase action , String ID ) throws Exception {
		VarDISTITEMTYPE value = null;
		try {
			value = VarDISTITEMTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			action.exit( "invalid distributive item type=" + ID );
		}
		
		if( value == null )
			action.exit( "unknown distributive item type=" + ID );
		
		return( value );
	}
	
	public VarDISTITEMSOURCE getItemDistSource( ActionBase action , String ID ) throws Exception {
		VarDISTITEMSOURCE value = null;
		try {
			value = VarDISTITEMSOURCE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			action.exit( "invalid distributive item source=" + ID );
		}
		
		if( value == null )
			action.exit( "unknown distributive item source=" + ID );
		
		return( value );
	}
	
	public VarDBMSTYPE getDbmsType( ActionBase action , String ID ) throws Exception {
		VarDBMSTYPE value = null;		
		
		try {
			value = VarDBMSTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			action.exit( "invalid dbmstype=" + ID );
		}
		
		if( value == null )
			action.exit( "unknown DBMS type=" + ID );
		
		return( value );
	}
	
	public VarCONFITEMTYPE getConfItemType( ActionBase action , String ID ) throws Exception {
		VarCONFITEMTYPE value = null;		
		
		try {
			value = VarCONFITEMTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			action.exit( "invalid confitemtype=" + ID );
		}
		
		if( value == null )
			action.exit( "unknown configuration item type=" + ID );
		
		return( value );
	}
	
	public VarCATEGORY readCategoryAttr( ActionBase action , Node node ) throws Exception {
		String value = ConfReader.getRequiredAttrValue( action , node , "category" );
		return( getCategory( action , value ) );
	}
	
	public VarITEMVERSION readItemVersionAttr( ActionBase action , Node node , String attrName ) throws Exception {
		String ID = ConfReader.getAttrValue( action , node , attrName , "default" );
		if( ID.equals( "default" ) )
			return( VarITEMVERSION.PREFIX );
		
		VarITEMVERSION value = VarITEMVERSION.valueOf( ID.toUpperCase() );
		if( value == null )
			action.exit( "unknown version type=" + ID );
		
		return( value );
	}
	
	public boolean isSourceCategory( ActionBase action , VarCATEGORY value ) throws Exception {
		if( value == VarCATEGORY.BUILD || value == VarCATEGORY.PREBUILT )
			return( true );
		return( false );
	}
	
	public boolean isBuildableCategory( ActionBase action , VarCATEGORY value ) throws Exception {
		if( value == VarCATEGORY.BUILD )
			return( true );
		return( false );
	}
	
	public VarCATEGORY[] getAllCategories( ActionBase action ) throws Exception {
		VarCATEGORY[] categories = { VarCATEGORY.BUILD , VarCATEGORY.MANUAL , VarCATEGORY.PREBUILT , VarCATEGORY.CONFIG , VarCATEGORY.DB };
		return( categories );
	}

	public VarCATEGORY[] getAllReleaseCategories( ActionBase action ) throws Exception {
		VarCATEGORY[] categories = { VarCATEGORY.BUILD , VarCATEGORY.MANUAL , VarCATEGORY.PREBUILT , VarCATEGORY.CONFIG , VarCATEGORY.DB };
		return( categories );
	}

	public VarCATEGORY[] getAllSourceCategories( ActionBase action ) throws Exception {
		VarCATEGORY[] categories = { VarCATEGORY.BUILD , VarCATEGORY.PREBUILT };
		return( categories );
	}

	public VarCATEGORY[] getAllBuildableCategories( ActionBase action ) throws Exception {
		VarCATEGORY[] categories = { VarCATEGORY.BUILD };
		return( categories );
	}
	
	public String getVersionPattern( ActionBase action , VarITEMVERSION version , String basename , String ext ) throws Exception {
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
			action.exit( "unknown version type=" + Common.getEnumLower( version ) );
		
		return( value );
	}
	
	public String[] getVersionPatterns( ActionBase action , MetaDistrBinaryItem distItem ) throws Exception {
		String basename = distItem.DISTBASENAME;
		String ext = distItem.EXT;
		if( distItem.DEPLOYVERSION == VarITEMVERSION.IGNORE ) {
			String[] values = new String[1];
			values[0] = getVersionPattern( action , distItem.DEPLOYVERSION , basename , ext );
			return( values );
		}

		String[] values = new String[4];
		values[0] = getVersionPattern( action , VarITEMVERSION.NONE , basename , ext );
		values[1] = getVersionPattern( action , VarITEMVERSION.MIDPOUND , basename , ext );
		values[2] = getVersionPattern( action , VarITEMVERSION.MIDDASH , basename , ext );
		values[3] = getVersionPattern( action , VarITEMVERSION.PREFIX , basename , ext );
		return( values );
	}
	
	public void updateProduct( ActionBase action ) throws Exception {
		product.updateProperties( action );
	}

}
