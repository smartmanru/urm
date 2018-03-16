package org.urm.meta;

import org.urm.common.Common;
import org.urm.common.ConfReader;
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
	
	public enum EnumAuthType {
		UNKNOWN ,
		PASSWORD ,
		CREDENTIALS ,
		KEYS ,
		ANONYMOUS , 
		CURRENTUSER
	};
	
	public enum EnumElementType {
		UNKNOWN ,
		EXTERNAL ,
		GENERIC ,
		SERVER ,
		DATABASE ,
		LIBRARY ,
		GROUP
	};
	
	public enum EnumLinkType {
		UNKNOWN ,
		GENERIC ,
		MSG
	};
	
	public enum EnumScopeCategory {
		UNKNOWN ,
		// source
		PROJECT ,
		// distributive
		CONFIG ,
		MANUAL ,
		DERIVED ,
		// delivery
		DB ,
		DOC ,
		// env hierarchy
		ENV ,
		// search only
		SEARCH_SOURCEBUILDABLE ,
		SEARCH_SOURCEPREBUILT ,
	};

	public enum EnumSessionType {
		UNKNOWN ,
		UNIXLOCAL ,
		UNIXREMOTE ,
		UNIXFROMWINDOWS ,
		WINDOWSLOCAL ,
		WINDOWSREMOTE ,
		WINDOWSFROMUNIX
	};

	public enum EnumProcessMode {
		UNKNOWN ,
		STARTED ,
		STARTING ,
		ERRORS ,
		STOPPED ,
		UNREACHABLE
	};

	public enum EnumContentType {
		UNKNOWN ,
		BINARYCOLDDEPLOY ,
		BINARYHOTDEPLOY ,
		BINARYCOPYONLY ,
		CONFCOLDDEPLOY ,
		CONFHOTDEPLOY ,
		CONFCOPYONLY
	};

	public enum EnumNameType {
		UNKNOWN ,
		ANY ,
		ALPHANUM ,
		ALPHANUMDOT ,
		ALPHANUMDOTDASH
	};
	
	public enum EnumArchiveType {
		UNKNOWN ,
		TARGZ ,
		TAR ,
		ZIP
	};
	
	public enum EnumTicketSetStatus {
		UNKNOWN ,
		NEW ,
		ACTIVE ,
		DESCOPED
	};

	public enum EnumTicketType {
		UNKNOWN ,
		FEATURE ,
		CHANGE ,
		DOCUMENT ,
		BUGFIX
	};

	public enum EnumTicketStatus {
		UNKNOWN ,
		NEW ,
		DEVDONE ,
		QADONE
	};

	public enum EnumTicketSetTargetType {
		UNKNOWN ,
		PROJECTSET ,
		PROJECTALLITEMS ,
		PROJECTNOITEMS ,
		DISTITEM ,
		CONFITEM ,
		SCHEMA ,
		DOC ,
		DELIVERYBINARIES ,
		DELIVERYCONFS ,
		DELIVERYDATABASE ,
		DELIVERYDOC
	};

	public enum EnumPackageExtension {
		UNKNOWN ,
		NUPKG ,
		RPM ,
		DEB
	}
	
	public enum EnumDistItemType {
		UNKNOWN ,
		BINARIES ,
		CONFIGURATION ,
		DATABASE ,
		DOCUMENTATION
	}
	
	public static EnumScopeCategory getCategory( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingCategory0 , "missing category" );
			return( EnumScopeCategory.UNKNOWN );
		}

		EnumScopeCategory value = null;
		try {
			value = EnumScopeCategory.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidCategory1 , "invalid category=" + ID , ID );
		}
		
		return( value );
	}
	
	public static EnumElementType getDesignElementType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingDesignElementType0 , "missing design element type" );
			return( EnumElementType.UNKNOWN );
		}
		
		EnumElementType value = null;		
		try {
			value = EnumElementType.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidDesignElementType1 , "invalid design element type=" + ID , ID );
		}
		
		return( value );
	}

	public static EnumLinkType getDesignLinkType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingDesignLinkType0 , "missing design link type" );
			return( EnumLinkType.UNKNOWN );
		}
		
		EnumLinkType value = null;		
		try {
			value = EnumLinkType.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidDesignLinkType1 , "invalid design link type=" + ID , ID );
		}
		
		return( value );
	}

	public static EnumTicketSetStatus getTicketSetStatus( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingTicketSetStatus0 , "missing ticket set status" );
			return( EnumTicketSetStatus.UNKNOWN );
		}
		
		EnumTicketSetStatus value = null;
		try {
			value = EnumTicketSetStatus.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidTicketSetStatus1 , "invalid ticket set status=" + ID , ID );
		}
		
		return( value );
	}

	public static EnumTicketType getTicketType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingTicketType0 , "missing ticket type" );
			return( EnumTicketType.UNKNOWN );
		}
		
		EnumTicketType value = null;
		try {
			value = EnumTicketType.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidTicketType1 , "invalid ticket type=" + ID , ID );
		}
		
		return( value );
	}

	public static EnumTicketStatus getTicketStatus( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingTicketStatus0 , "missing ticket status" );
			return( EnumTicketStatus.UNKNOWN );
		}
		
		EnumTicketStatus value = null;
		try {
			value = EnumTicketStatus.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidTicketStatus1 , "invalid ticket status=" + ID , ID );
		}
		
		return( value );
	}

	public static EnumTicketSetTargetType getTicketSetTargetType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingTicketSetTargetType0 , "missing ticket set target type" );
			return( EnumTicketSetTargetType.UNKNOWN );
		}
		
		EnumTicketSetTargetType value = null;
		try {
			value = EnumTicketSetTargetType.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidTicketSetTargetType1 , "invalid ticket set target type=" + ID , ID );
		}
		
		return( value );
	}

	public static EnumPackageExtension getPackageExtension( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingPackageExtension0 , "missing package extension" );
			return( EnumPackageExtension.UNKNOWN );
		}
		
		EnumPackageExtension value = null;
		try {
			value = EnumPackageExtension.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidPackageExtension1 , "invalid package extension=" + ID , ID );
		}
		
		return( value );
	}

	public static EnumDistItemType getDistItemType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingDistItemType0 , "missing dist item type" );
			return( EnumDistItemType.UNKNOWN );
		}
		
		EnumDistItemType value = null;
		try {
			value = EnumDistItemType.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidDistItemType1 , "invalid dist item type=" + ID , ID );
		}
		
		return( value );
	}

	public static boolean isBinaryContent( EnumContentType c ) throws Exception {
		if( c == EnumContentType.BINARYCOLDDEPLOY || c == EnumContentType.BINARYCOPYONLY || c == EnumContentType.BINARYHOTDEPLOY )
			return( true );
		return( false );
	}
	
	public static boolean isConfContent( EnumContentType c ) throws Exception {
		if( c == EnumContentType.CONFCOLDDEPLOY || c == EnumContentType.CONFCOPYONLY || c == EnumContentType.CONFHOTDEPLOY )
			return( true );
		return( false );
	}

	public static EnumScopeCategory readCategoryAttr( Node node ) throws Exception {
		String value = ConfReader.getAttrValue( node , "category" );
		return( Types.getCategory( value , true ) );
	}
	
	public static boolean isSourceCategory( EnumScopeCategory value ) {
		if( value == EnumScopeCategory.PROJECT )
			return( true );
		return( false );
	}
	
	public static EnumScopeCategory[] getAllReleaseCategories() {
		EnumScopeCategory[] categories = { 
				EnumScopeCategory.PROJECT , 
				EnumScopeCategory.CONFIG , 
				EnumScopeCategory.MANUAL , 
				EnumScopeCategory.DERIVED , 
				EnumScopeCategory.DB ,
				EnumScopeCategory.DOC
				};
		return( categories );
	}

	public static EnumScopeCategory[] getAllSourceCategories() {
		EnumScopeCategory[] categories = { EnumScopeCategory.PROJECT };
		return( categories );
	}

	public static boolean checkCategoryProperty( EnumScopeCategory part , EnumScopeCategory property ) {
		if( part == property )
			return( true );
		if( property == EnumScopeCategory.SEARCH_SOURCEBUILDABLE ) {
			if( part == EnumScopeCategory.PROJECT )
				return( true );
		}
		return( false );
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
