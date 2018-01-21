package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.core.DBEnums.DBEnumSourceItemType;
import org.urm.engine.EngineTransaction;
import org.urm.engine.TransactionBase;
import org.urm.meta.Types;
import org.urm.meta.Types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaSourceProjectItem {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_SRCTYPE = "type";
	public static String PROPERTY_BASENAME = "basename";
	public static String PROPERTY_EXT = "extension";
	public static String PROPERTY_STATICEXT = "staticextension";
	public static String PROPERTY_PATH = "path";
	public static String PROPERTY_VERSION = "version";
	public static String PROPERTY_NODIST = "internal";
	
	public int ID;
	public String NAME;
	public String DESC;
	public DBEnumSourceItemType SOURCEITEM_TYPE;
	public String BASENAME;
	public String EXT;
	public String STATICEXT;
	public String PATH;
	public String FIXED_VERSION;
	public boolean NODIST;
	public int PV;
	
	public MetaDistrBinaryItem distItem;

	public Meta meta;
	public MetaSourceProject project;
	
	public MetaSourceProjectItem( Meta meta , MetaSourceProject project ) {
		this.meta = meta;
		this.project = project;
	}
	
	public void createItem( EngineTransaction transaction , String name ) throws Exception {
		this.NAME = name;
		SOURCEITEM_TYPE = VarITEMSRCTYPE.UNKNOWN;
		BASENAME = "";
		EXT = "";
		FIXED_VERSION = "";
		STATICEXT = "";
		PATH = "";
		NODIST = true;
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		
		SOURCEITEM_TYPE = Types.getItemSrcType( ConfReader.getRequiredAttrValue( node , "type" ) , false );
		BASENAME = ConfReader.getAttrValue( node , "basename" );
		if( BASENAME.isEmpty() )
			BASENAME = NAME;

		EXT = ConfReader.getAttrValue( node , "extension" );
		FIXED_VERSION = ConfReader.getAttrValue( node , "version" );
		PATH = ConfReader.getAttrValue( node , "itempath" );
		
		STATICEXT = "";
		STATICEXT = "";
		if( SOURCEITEM_TYPE == VarITEMSRCTYPE.STATICWAR ) {
			STATICEXT = ConfReader.getAttrValue( node , "staticextension" );
			if( STATICEXT.isEmpty() )
				STATICEXT="-webstatic.tar.gz";
		}
		
		NODIST = ConfReader.getBooleanAttrValue( node , "internal" , false );
	}

	public void setDistItem( ActionBase action , MetaDistrBinaryItem distItem ) throws Exception {
		this.distItem = distItem;
		this.NODIST = ( distItem == null )? true : false;
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , NAME );
		
		Common.xmlSetElementAttr( doc , root , "type" , Common.getEnumLower( SOURCEITEM_TYPE ) );
		Common.xmlSetElementAttr( doc , root , "basename" , BASENAME );
		Common.xmlSetElementAttr( doc , root , "extension" , EXT );
		Common.xmlSetElementAttr( doc , root , "version" , FIXED_VERSION );
		Common.xmlSetElementAttr( doc , root , "itempath" , PATH );
		Common.xmlSetElementAttr( doc , root , "internal" , Common.getBooleanValue( NODIST ) );

		Common.xmlSetElementAttr( doc , root , "staticextension" , STATICEXT );
	}
	
	public MetaSourceProjectItem copy( ActionBase action , Meta meta , MetaSourceProject project ) throws Exception {
		MetaSourceProjectItem r = new MetaSourceProjectItem( meta , project );
		r.NAME = NAME;
		
		r.SOURCEITEM_TYPE = SOURCEITEM_TYPE;
		r.BASENAME = BASENAME;
		r.EXT = EXT;
		r.FIXED_VERSION = FIXED_VERSION;
		r.PATH = PATH;
		r.STATICEXT = STATICEXT;
		r.NODIST = NODIST;
		
		return( r );
	}
	
	public boolean isInternal() {
		if( NODIST )
			return( true );
		return( false );
	}

	public boolean isTargetLocal() {
		if( isSourceDirectory() )
			return( true );
		return( false );
	}
	
	public boolean isSourceDirectory() {
		if( SOURCEITEM_TYPE == VarITEMSRCTYPE.DIRECTORY )
			return( true );
		return( false );
	}
	
	public boolean isSourceBasic() {
		if( SOURCEITEM_TYPE == VarITEMSRCTYPE.BASIC )
			return( true );
		return( false );
	}
	
	public boolean isSourcePackage() {
		if( SOURCEITEM_TYPE == VarITEMSRCTYPE.PACKAGE )
			return( true );
		return( false );
	}
	
	public boolean isSourceStaticWar() {
		if( SOURCEITEM_TYPE == VarITEMSRCTYPE.STATICWAR )
			return( true );
		return( false );
	}
	
	public void setSourceData( TransactionBase transaction , VarITEMSRCTYPE srcType , String artefactName , String ext , String path , String version , boolean dist ) throws Exception {
		SOURCEITEM_TYPE = srcType;
		BASENAME = ( artefactName.isEmpty() )? NAME : artefactName;
		EXT = ( srcType == VarITEMSRCTYPE.STATICWAR )? ".war" : ext;
		FIXED_VERSION = version;
		PATH = path;
		STATICEXT = ( srcType == VarITEMSRCTYPE.STATICWAR )? ext : "";
		NODIST = ( dist )? false : true;
	}
	
	public String getArtefactSampleFile() {
		String value = BASENAME;
		if( SOURCEITEM_TYPE == VarITEMSRCTYPE.BASIC || SOURCEITEM_TYPE == VarITEMSRCTYPE.PACKAGE || SOURCEITEM_TYPE == VarITEMSRCTYPE.CUSTOM ) {
			if( !FIXED_VERSION.isEmpty() )
				value += "-" + FIXED_VERSION;
			value += EXT;
		}
		else
		if( SOURCEITEM_TYPE == VarITEMSRCTYPE.DIRECTORY )
			value = BASENAME;
		else
		if( SOURCEITEM_TYPE == VarITEMSRCTYPE.STATICWAR )
			value = BASENAME + EXT + "/" + BASENAME + STATICEXT;
		return( value );
	}
	
}
