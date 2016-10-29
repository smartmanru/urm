package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.product.Meta.VarNAMETYPE;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaDistrComponentItem {

	protected Meta meta;
	MetaDistrComponent comp;
	
	public MetaDistrBinaryItem binaryItem;
	public MetaDistrConfItem confItem;
	public MetaDatabaseSchema schema; 
	public boolean OBSOLETE;
	public String DEPLOYNAME;

	public MetaDistrComponentItem( Meta meta , MetaDistrComponent comp ) {
		this.meta = meta;
		this.comp = comp;
	}

	public MetaDistrComponentItem copy( ActionBase action , Meta meta , MetaDistrComponent comp ) throws Exception {
		MetaDistrComponentItem r = new MetaDistrComponentItem( meta , comp );
		if( binaryItem != null )
			r.binaryItem = comp.dist.findBinaryItem( action , binaryItem.KEY );
		if( confItem != null )
			r.confItem = comp.dist.findConfItem( action , confItem.KEY );
		if( schema != null ) {
			MetaDatabase database = r.meta.getDatabase( action );
			r.schema = database.getSchema( action , schema.SCHEMA );
		}
		r.OBSOLETE = OBSOLETE;
		r.DEPLOYNAME = DEPLOYNAME;
		return( r );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		if( binaryItem != null )
			Common.xmlSetElementAttr( doc , root , "name" , binaryItem.KEY );
		if( confItem != null )
			Common.xmlSetElementAttr( doc , root , "name" , confItem.KEY );
		if( schema != null )
			Common.xmlSetElementAttr( doc , root , "name" , schema.SCHEMA );
		Common.xmlSetElementAttr( doc , root , "obsolete" , Common.getBooleanValue( OBSOLETE ) );
		Common.xmlSetElementAttr( doc , root , "deployname" , DEPLOYNAME );
	}
	
	public void loadBinary( ActionBase action , Node node ) throws Exception {
		String NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOTDASH );
		binaryItem = comp.dist.getBinaryItem( action , NAME );
		OBSOLETE = ConfReader.getBooleanAttrValue( node , "obsolete" , false );
		DEPLOYNAME = ConfReader.getAttrValue( node , "deployname" );
	}

	public void loadConf( ActionBase action , Node node ) throws Exception {
		String NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOTDASH );
		confItem = comp.dist.getConfItem( action , NAME );
		OBSOLETE = ConfReader.getBooleanAttrValue( node , "obsolete" , false );
	}

	public void loadSchema( ActionBase action , Node node ) throws Exception {
		String NAME = ConfReader.getRequiredAttrValue( node , "schema" );
		
		MetaDatabase database = meta.getDatabase( action );
		schema = database.getSchema( action , NAME );
		DEPLOYNAME = ConfReader.getAttrValue( node , "deployname" );
	}

}
