package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
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

	protected Meta meta;
	public MetaSourceProject project;
	
	public MetaSourceProjectItem( Meta meta , MetaSourceProject project ) {
		this.meta = meta;
		this.project = project;
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
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , ITEMNAME );
		
		Common.xmlSetElementAttr( doc , root , "type" , Common.getEnumLower( itemSrcType ) );
		Common.xmlSetElementAttr( doc , root , "basename" , ITEMBASENAME );

		Common.xmlSetElementAttr( doc , root , "extension" , ITEMEXTENSION );
		Common.xmlSetElementAttr( doc , root , "version" , ITEMVERSION );
		Common.xmlSetElementAttr( doc , root , "itempath" , ITEMPATH );

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
		return( r );
	}
	
	public boolean isInternal() {
		if( itemSrcType == VarITEMSRCTYPE.INTERNAL )
			return( true );
		return( false );
	}
	
}
