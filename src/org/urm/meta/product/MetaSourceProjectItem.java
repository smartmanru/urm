package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.EngineTransaction;
import org.urm.engine.TransactionBase;
import org.urm.meta.Types;
import org.urm.meta.Types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaSourceProjectItem {

	public String ITEMNAME;
	public VarITEMSRCTYPE itemSrcType;
	public String ITEMBASENAME;
	public String ITEMEXTENSION;
	public String ITEMVERSION;
	public String ITEMSTATICEXTENSION;
	public String ITEMPATH;
	boolean internal;
	
	public MetaDistrBinaryItem distItem;

	public Meta meta;
	public MetaSourceProject project;
	
	public MetaSourceProjectItem( Meta meta , MetaSourceProject project ) {
		this.meta = meta;
		this.project = project;
	}
	
	public void createItem( EngineTransaction transaction , String name ) throws Exception {
		this.ITEMNAME = name;
		itemSrcType = VarITEMSRCTYPE.UNKNOWN;
		ITEMBASENAME = "";
		ITEMEXTENSION = "";
		ITEMVERSION = "";
		ITEMSTATICEXTENSION = "";
		ITEMPATH = "";
		internal = true;
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		ITEMNAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		
		itemSrcType = Types.getItemSrcType( ConfReader.getRequiredAttrValue( node , "type" ) , false );
		ITEMBASENAME = ConfReader.getAttrValue( node , "basename" );
		if( ITEMBASENAME.isEmpty() )
			ITEMBASENAME = ITEMNAME;

		ITEMEXTENSION = ConfReader.getAttrValue( node , "extension" );
		ITEMVERSION = ConfReader.getAttrValue( node , "version" );
		ITEMPATH = ConfReader.getAttrValue( node , "itempath" );
		
		ITEMSTATICEXTENSION = "";
		ITEMSTATICEXTENSION = "";
		if( itemSrcType == VarITEMSRCTYPE.STATICWAR ) {
			ITEMSTATICEXTENSION = ConfReader.getAttrValue( node , "staticextension" );
			if( ITEMSTATICEXTENSION.isEmpty() )
				ITEMSTATICEXTENSION="-webstatic.tar.gz";
		}
		
		internal = ConfReader.getBooleanAttrValue( node , "internal" , false );
	}

	public void setDistItem( ActionBase action , MetaDistrBinaryItem distItem ) throws Exception {
		this.distItem = distItem;
		this.internal = ( distItem == null )? true : false;
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , ITEMNAME );
		
		Common.xmlSetElementAttr( doc , root , "type" , Common.getEnumLower( itemSrcType ) );
		Common.xmlSetElementAttr( doc , root , "basename" , ITEMBASENAME );
		Common.xmlSetElementAttr( doc , root , "extension" , ITEMEXTENSION );
		Common.xmlSetElementAttr( doc , root , "version" , ITEMVERSION );
		Common.xmlSetElementAttr( doc , root , "itempath" , ITEMPATH );
		Common.xmlSetElementAttr( doc , root , "internal" , Common.getBooleanValue( internal ) );

		Common.xmlSetElementAttr( doc , root , "staticextension" , ITEMSTATICEXTENSION );
	}
	
	public MetaSourceProjectItem copy( ActionBase action , Meta meta , MetaSourceProject project ) throws Exception {
		MetaSourceProjectItem r = new MetaSourceProjectItem( meta , project );
		r.ITEMNAME = ITEMNAME;
		
		r.itemSrcType = itemSrcType;
		r.ITEMBASENAME = ITEMBASENAME;
		r.ITEMEXTENSION = ITEMEXTENSION;
		r.ITEMVERSION = ITEMVERSION;
		r.ITEMPATH = ITEMPATH;
		r.ITEMSTATICEXTENSION = ITEMSTATICEXTENSION;
		r.internal = internal;
		
		return( r );
	}
	
	public boolean isInternal() {
		if( internal )
			return( true );
		return( false );
	}

	public boolean isTargetLocal() {
		if( isSourceDirectory() )
			return( true );
		return( false );
	}
	
	public boolean isSourceDirectory() {
		if( itemSrcType == VarITEMSRCTYPE.DIRECTORY )
			return( true );
		return( false );
	}
	
	public boolean isSourceBasic() {
		if( itemSrcType == VarITEMSRCTYPE.BASIC )
			return( true );
		return( false );
	}
	
	public boolean isSourcePackage() {
		if( itemSrcType == VarITEMSRCTYPE.PACKAGE )
			return( true );
		return( false );
	}
	
	public boolean isSourceStaticWar() {
		if( itemSrcType == VarITEMSRCTYPE.STATICWAR )
			return( true );
		return( false );
	}
	
	public void setSourceData( TransactionBase transaction , VarITEMSRCTYPE srcType , String artefactName , String ext , String path , String version , boolean dist ) throws Exception {
		itemSrcType = srcType;
		ITEMBASENAME = ( artefactName.isEmpty() )? ITEMNAME : artefactName;
		ITEMEXTENSION = ( srcType == VarITEMSRCTYPE.STATICWAR )? ".war" : ext;
		ITEMVERSION = version;
		ITEMPATH = path;
		ITEMSTATICEXTENSION = ( srcType == VarITEMSRCTYPE.STATICWAR )? ext : "";
		internal = ( dist )? false : true;
	}
	
	public String getArtefactSampleFile() {
		String value = ITEMBASENAME;
		if( itemSrcType == VarITEMSRCTYPE.BASIC || itemSrcType == VarITEMSRCTYPE.PACKAGE || itemSrcType == VarITEMSRCTYPE.CUSTOM ) {
			if( !ITEMVERSION.isEmpty() )
				value += "-" + ITEMVERSION;
			value += ITEMEXTENSION;
		}
		else
		if( itemSrcType == VarITEMSRCTYPE.DIRECTORY )
			value = ITEMBASENAME;
		else
		if( itemSrcType == VarITEMSRCTYPE.STATICWAR )
			value = ITEMBASENAME + ITEMEXTENSION + "/" + ITEMBASENAME + ITEMSTATICEXTENSION;
		return( value );
	}
	
}
