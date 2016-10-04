package org.urm.meta.product;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.product.Meta.VarNAMETYPE;
import org.w3c.dom.Node;

public class MetaDatabaseDatagroup {

	protected Meta meta;
	public MetaDatabase database;

	String NAME;
	Map<String,MetaDatabaseSchema> schemaSet;
	
	public MetaDatabaseDatagroup( Meta meta , MetaDatabase database ) {
		this.meta = meta;
		this.database = database;
	}

	public void load( ActionBase action , Node node ) throws Exception {
		schemaSet = new HashMap<String,MetaDatabaseSchema>(); 

		NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		String schemalist = ConfReader.getAttrValue( node , "schemaset" ); 
		
		for( String schemaName : Common.splitSpaced( schemalist ) ) {
			MetaDatabaseSchema schema = database.getSchema( action , schemaName );
			schemaSet.put( schema.SCHEMA , schema );
		}
	}

	public Map<String,MetaDatabaseSchema> getSchemes( ActionBase action ) throws Exception {
		return( schemaSet );
	}
	
}
