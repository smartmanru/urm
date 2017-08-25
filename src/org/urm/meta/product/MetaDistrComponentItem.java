package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.ServerTransaction;
import org.urm.meta.Types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaDistrComponentItem {

	public Meta meta;
	public MetaDistrComponent comp;
	
	public VarCOMPITEMTYPE type;
	public String NAME;
	public MetaDistrBinaryItem binaryItem;
	public MetaDistrConfItem confItem;
	public MetaDatabaseSchema schema; 
	public boolean OBSOLETE;
	public String DEPLOYNAME;

	public MetaDistrComponentItem( Meta meta , MetaDistrComponent comp ) {
		this.meta = meta;
		this.comp = comp;
	}

	public void createComponentItem( ServerTransaction transaction ) throws Exception {
		this.type = VarCOMPITEMTYPE.UNKNOWN;
		this.NAME = "";
		this.OBSOLETE = false;
		this.DEPLOYNAME = "";
	}

	public void setBinaryItem( ServerTransaction transaction , MetaDistrBinaryItem binaryItem , String DEPLOYNAME ) throws Exception {
		this.type = VarCOMPITEMTYPE.BINARY;
		this.binaryItem = binaryItem;
		this.NAME = binaryItem.KEY;
		this.DEPLOYNAME = DEPLOYNAME;
	}
	
	public void setConfItem( ServerTransaction transaction , MetaDistrConfItem confItem ) throws Exception {
		this.type = VarCOMPITEMTYPE.CONF;
		this.confItem = confItem;
		this.NAME = confItem.KEY;
		this.DEPLOYNAME = "";
	}
	
	public void setSchema( ServerTransaction transaction , MetaDatabaseSchema schema , String DEPLOYNAME ) throws Exception {
		this.type = VarCOMPITEMTYPE.SCHEMA;
		this.schema = schema;
		this.NAME = schema.SCHEMA;
		this.DEPLOYNAME = DEPLOYNAME;
	}
	
	public MetaDistrComponentItem copy( ActionBase action , Meta meta , MetaDistrComponent comp ) throws Exception {
		MetaDistrComponentItem r = new MetaDistrComponentItem( meta , comp );
		r.type = type;
		r.NAME = NAME;
		if( binaryItem != null )
			r.binaryItem = comp.dist.findBinaryItem( binaryItem.KEY );
		else
		if( confItem != null )
			r.confItem = comp.dist.findConfItem( confItem.KEY );
		else
		if( schema != null ) {
			MetaDatabase database = r.meta.getDatabase( action );
			r.schema = database.getSchema( action , schema.SCHEMA );
		}
		r.OBSOLETE = OBSOLETE;
		r.DEPLOYNAME = DEPLOYNAME;
		return( r );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , NAME );
		Common.xmlSetElementAttr( doc , root , "obsolete" , Common.getBooleanValue( OBSOLETE ) );
		Common.xmlSetElementAttr( doc , root , "deployname" , DEPLOYNAME );
	}
	
	public void loadBinary( ActionBase action , Node node ) throws Exception {
		NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOTDASH );
		type = VarCOMPITEMTYPE.BINARY;
		binaryItem = comp.dist.getBinaryItem( action , NAME );
		OBSOLETE = ConfReader.getBooleanAttrValue( node , "obsolete" , false );
		DEPLOYNAME = ConfReader.getAttrValue( node , "deployname" );
	}

	public void loadConf( ActionBase action , Node node ) throws Exception {
		NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOTDASH );
		type = VarCOMPITEMTYPE.CONF;
		confItem = comp.dist.getConfItem( action , NAME );
		OBSOLETE = ConfReader.getBooleanAttrValue( node , "obsolete" , false );
	}

	public void loadSchema( ActionBase action , Node node ) throws Exception {
		NAME = ConfReader.getRequiredAttrValue( node , "name" );
		
		MetaDatabase database = meta.getDatabase( action );
		type = VarCOMPITEMTYPE.SCHEMA;
		schema = database.getSchema( action , NAME );
		DEPLOYNAME = ConfReader.getAttrValue( node , "deployname" );
	}

}
