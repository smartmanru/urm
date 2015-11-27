package ru.egov.urm.meta;

import org.w3c.dom.Node;

import ru.egov.urm.ConfReader;
import ru.egov.urm.run.ActionBase;

public class MetaDatabaseSchema {

	Metadata meta;
	public MetaDatabase database;

	public String SCHEMA;
	public String DBNAME;
	public String DBUSER;
	
	public MetaDatabaseSchema( Metadata meta , MetaDatabase database ) {
		this.meta = meta;
		this.database = database;
	}

	public void load( ActionBase action , Node node ) throws Exception {
		SCHEMA = ConfReader.getNameAttr( action , node );
		DBNAME = ConfReader.getAttrValue( action , node , "dbname" , SCHEMA );
		DBUSER = ConfReader.getAttrValue( action , node , "dbuser" , SCHEMA );
	}

}
