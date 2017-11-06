package org.urm.meta;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.w3c.dom.Node;

public class Types {

	public enum EnumResourceCategory {
		ANY ,
		VCS ,
		SSH ,
		CREDENTIALS ,
		NEXUS ,
		SOURCE;
	};
	
	public enum VarELEMENTTYPE {
		UNKNOWN ,
		EXTERNAL ,
		GENERIC ,
		SERVER ,
		DATABASE ,
		LIBRARY ,
		GROUP
	};
	
	public enum VarLINKTYPE {
		UNKNOWN ,
		GENERIC ,
		MSG
	};
	
	public enum VarPROJECTTYPE {
		UNKNOWN ,
		BUILDABLE ,
		PREBUILT_NEXUS ,
		PREBUILT_VCS
	};
	
	public enum VarBUILDERTYPE {
		UNKNOWN ,
		GENERIC ,
		ANT ,
		MAVEN ,
		GRADLE ,
		MSBUILD
	};
	
	public enum VarBUILDERTARGET {
		UNKNOWN ,
		LOCALPATH ,
		NEXUS
	};
	
	public enum VarCATEGORY {
		UNKNOWN ,
		PROJECT ,
		CONFIG ,
		MANUAL ,
		DERIVED ,
		DB ,
		ENV ,
		BUILDABLE ,
		PREBUILT
	};

	public enum VarENVTYPE {
		UNKNOWN ,
		PRODUCTION ,
		UAT ,
		DEVELOPMENT
	};
	
	public enum VarDEPLOYITEMTYPE {
		UNKNOWN ,
		BINARY ,
		CONF ,
		SCHEMA ,
		COMP
	};
	
	public enum VarCOMPITEMTYPE {
		UNKNOWN ,
		BINARY ,
		CONF ,
		SCHEMA ,
		WSDL
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
		BASIC ,
		DIRECTORY ,
		STATICWAR ,
		PACKAGE ,
		CUSTOM
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
		DOCKER ,
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
		PACKAGE ,
		STATICWAR ,
		ARCHIVE_DIRECT ,	// deploydir = archive/content
		ARCHIVE_CHILD ,		// deploydir/archivename = archive/archivename/fullcontent
		ARCHIVE_SUBDIR		// deploydir/archivename = archive/content/fullcontent
	};
	
	public enum VarDISTITEMORIGIN {
		UNKNOWN ,
		MANUAL ,
		DERIVED ,
		BUILD
	};
	
	public enum VarPROCESSMODE {
		UNKNOWN ,
		STARTED ,
		STARTING ,
		ERRORS ,
		STOPPED ,
		UNREACHABLE
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
	
	public enum VarBASESRCFORMAT {
		UNKNOWN ,
		TARGZ_SINGLEDIR ,
		ZIP_SINGLEDIR ,
		SINGLEFILE
	};
	
	public enum VarLCTYPE {
		UNKNOWN ,
		MAJOR ,
		MINOR ,
		URGENT
	};

	public enum VarLCSTAGE {
		UNKNOWN ,
		RELEASE ,
		DEPLOYMENT
	};

	public enum VarTICKETSETSTATUS {
		UNKNOWN ,
		NEW ,
		ACTIVE ,
		DESCOPED
	};

	public enum VarTICKETTYPE {
		UNKNOWN ,
		FEATURE ,
		CHANGE ,
		BUGFIX
	};

	public enum VarTICKETSTATUS {
		UNKNOWN ,
		NEW ,
		DEVDONE ,
		QADONE
	};

	public enum VarTICKETSETTARGETTYPE {
		UNKNOWN ,
		PROJECTSET ,
		PROJECTALLITEMS ,
		PROJECTNOITEMS ,
		DISTITEM ,
		CONFITEM ,
		SCHEMA ,
		DELIVERYBINARIES ,
		DELIVERYCONFS ,
		DELIVERYDATABASE
	};

	public enum VarPACKAGEEXTENSION {
		UNKNOWN ,
		NUPKG ,
		RPM ,
		DEB
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
	
	public static VarPROJECTTYPE getProjectType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingProjectType0 , "missing project type" );
			return( VarPROJECTTYPE.UNKNOWN );
		}
		
		VarPROJECTTYPE value = null;
		try {
			value = VarPROJECTTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidProjectType1 , "invalid project type=" + ID , ID );
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
	
	public static VarENVTYPE getEnvType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingEnvType0 , "missing deploy item type" );
			return( VarENVTYPE.UNKNOWN );
		}
		
		VarENVTYPE value = null;
		try {
			value = VarENVTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidEnvType1 , "invalid environment type=" + ID , ID );
		}
		
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
	
	public static VarBASESRCFORMAT getBaseSrcFormat( String TYPE , boolean required ) throws Exception {
		if( TYPE.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingBuilderTarget0 , "missing base srcformat" );
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

	public static VarBUILDERTYPE getBuilderType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingBuilderType0 , "missing builder type" );
			return( VarBUILDERTYPE.UNKNOWN );
		}
		
		VarBUILDERTYPE value = null;		
		try {
			value = VarBUILDERTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidBuilderType1 , "invalid builder type=" + ID , ID );
		}
		
		return( value );
	}

	public static VarBUILDERTARGET getBuilderTarget( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingBuilderTarget0 , "missing builder target" );
			return( VarBUILDERTARGET.UNKNOWN );
		}
		
		VarBUILDERTARGET value = null;		
		try {
			value = VarBUILDERTARGET.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidBuilderTarget1 , "invalid builder target=" + ID , ID );
		}
		
		return( value );
	}

	public static VarLCTYPE getLCType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingLCType0 , "missing lifecycle type" );
			return( VarLCTYPE.UNKNOWN );
		}
		
		VarLCTYPE value = null;
		try {
			value = VarLCTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidLCType1 , "invalid lifecycle type=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarLCSTAGE getLCStage( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingLCStage0 , "missing lifecycle stage" );
			return( VarLCSTAGE.UNKNOWN );
		}
		
		VarLCSTAGE value = null;
		try {
			value = VarLCSTAGE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidLCStage1 , "invalid lifecycle stage=" + ID , ID );
		}
		
		return( value );
	}

	public static VarTICKETSETSTATUS getTicketSetStatus( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingTicketSetStatus0 , "missing ticket set status" );
			return( VarTICKETSETSTATUS.UNKNOWN );
		}
		
		VarTICKETSETSTATUS value = null;
		try {
			value = VarTICKETSETSTATUS.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidTicketSetStatus1 , "invalid ticket set status=" + ID , ID );
		}
		
		return( value );
	}

	public static VarTICKETTYPE getTicketType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingTicketType0 , "missing ticket type" );
			return( VarTICKETTYPE.UNKNOWN );
		}
		
		VarTICKETTYPE value = null;
		try {
			value = VarTICKETTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidTicketType1 , "invalid ticket type=" + ID , ID );
		}
		
		return( value );
	}

	public static VarTICKETSTATUS getTicketStatus( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingTicketStatus0 , "missing ticket status" );
			return( VarTICKETSTATUS.UNKNOWN );
		}
		
		VarTICKETSTATUS value = null;
		try {
			value = VarTICKETSTATUS.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidTicketStatus1 , "invalid ticket status=" + ID , ID );
		}
		
		return( value );
	}

	public static VarTICKETSETTARGETTYPE getTicketSetTargetType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingTicketSetTargetType0 , "missing ticket set target type" );
			return( VarTICKETSETTARGETTYPE.UNKNOWN );
		}
		
		VarTICKETSETTARGETTYPE value = null;
		try {
			value = VarTICKETSETTARGETTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidTicketSetTargetType1 , "invalid ticket set target type=" + ID , ID );
		}
		
		return( value );
	}

	public static VarPACKAGEEXTENSION getPackageExtension( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingPackageExtension0 , "missing package extension" );
			return( VarPACKAGEEXTENSION.UNKNOWN );
		}
		
		VarPACKAGEEXTENSION value = null;
		try {
			value = VarPACKAGEEXTENSION.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidPackageExtension1 , "invalid package extension=" + ID , ID );
		}
		
		return( value );
	}

    public static boolean isArchive( VarDISTITEMTYPE distItemType ) {
		if( distItemType == VarDISTITEMTYPE.ARCHIVE_CHILD || 
			distItemType == VarDISTITEMTYPE.ARCHIVE_DIRECT || 
			distItemType == VarDISTITEMTYPE.ARCHIVE_SUBDIR )
			return( true );
		return( false );
    }

	public static boolean isBinaryContent( VarCONTENTTYPE c ) throws Exception {
		if( c == VarCONTENTTYPE.BINARYCOLDDEPLOY || c == VarCONTENTTYPE.BINARYCOPYONLY || c == VarCONTENTTYPE.BINARYHOTDEPLOY )
			return( true );
		return( false );
	}
	
	public static boolean isConfContent( VarCONTENTTYPE c ) throws Exception {
		if( c == VarCONTENTTYPE.CONFCOLDDEPLOY || c == VarCONTENTTYPE.CONFCOPYONLY || c == VarCONTENTTYPE.CONFHOTDEPLOY )
			return( true );
		return( false );
	}

	public static VarCATEGORY readCategoryAttr( Node node ) throws Exception {
		String value = ConfReader.getAttrValue( node , "category" );
		return( Types.getCategory( value , true ) );
	}
	
	public static boolean isSourceCategory( VarCATEGORY value ) {
		if( value == VarCATEGORY.PROJECT )
			return( true );
		return( false );
	}
	
	public static VarCATEGORY[] getAllReleaseCategories() {
		VarCATEGORY[] categories = { VarCATEGORY.PROJECT , VarCATEGORY.CONFIG , VarCATEGORY.DB , VarCATEGORY.MANUAL , VarCATEGORY.DERIVED };
		return( categories );
	}

	public static VarCATEGORY[] getAllSourceCategories() {
		VarCATEGORY[] categories = { VarCATEGORY.PROJECT };
		return( categories );
	}

	public static boolean checkCategoryProperty( VarCATEGORY part , VarCATEGORY property ) {
		if( part == property )
			return( true );
		if( property == VarCATEGORY.BUILDABLE ) {
			if( part == VarCATEGORY.PROJECT )
				return( true );
		}
		return( false );
	}
	
	public static String getVersionPattern( ActionBase action , VarITEMVERSION version , String basename , String ext ) throws Exception {
		String value = "";
		if( version == VarITEMVERSION.NONE || version == VarITEMVERSION.IGNORE )
			value = basename + ext;
		else if( version == VarITEMVERSION.MIDPOUND )
			value = Common.getLiteral( basename ) + "##[0-9.]+.*" + Common.getLiteral( ext );
		else if( version == VarITEMVERSION.MIDDASH )
			value = basename + "-[0-9.]+.*" + ext;
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

	public static boolean isPackageExtension( String ext ) {
		try {
			if( !ext.startsWith( "." ) )
				return( false );
			
			getPackageExtension( ext.substring( 1 ) , true );
		}
		catch( Throwable e ) {
			return( false );
		}
		return( true );
	}
	
	
}
