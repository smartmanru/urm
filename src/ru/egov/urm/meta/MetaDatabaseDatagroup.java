package ru.egov.urm.meta;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.run.ActionBase;

public class MetaDatabaseDatagroup {

	Metadata meta;
	public MetaDatabase database;

	String NAME;
	Map<String,MetaDatabaseSchema> schemaSet;
	
	public MetaDatabaseDatagroup( Metadata meta , MetaDatabase database ) {
		this.meta = meta;
		this.database = database;
	}

	public void load( ActionBase action , Node node ) throws Exception {
		schemaSet = new HashMap<String,MetaDatabaseSchema>(); 

		NAME = ConfReader.getNameAttr( action , node );
		String schemalist = ConfReader.getAttrValue( action , node , "schemaset" ); 
		
		for( String schemaName : Common.splitSpaced( schemalist ) ) {
			MetaDatabaseSchema schema = database.getSchema( action , schemaName );
			schemaSet.put( schema.SCHEMA , schema );
		}
	}

	public Map<String,MetaDatabaseSchema> getSchemes( ActionBase action ) throws Exception {
		return( schemaSet );
	}
	
}
