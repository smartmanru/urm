package org.urm.meta;

import org.urm.ConfReader;
import org.urm.action.ActionBase;
import org.urm.action.database.DatabaseSpecific;
import org.urm.meta.Metadata.VarDBMSTYPE;
import org.urm.meta.Metadata.VarNAMETYPE;
import org.w3c.dom.Node;

public class MetaDatabaseSchema {

	Metadata meta;
	public MetaDatabase database;

	public String SCHEMA;
	public VarDBMSTYPE dbmsType;
	public String DBNAME;
	public String DBUSER;
	
	public DatabaseSpecific specific;
	
	public MetaDatabaseSchema( Metadata meta , MetaDatabase database ) {
		this.meta = meta;
		this.database = database;
	}

	public void load( ActionBase action , Node node ) throws Exception {
		SCHEMA = ConfReader.getNameAttr( action , node , VarNAMETYPE.ALPHANUM );
		dbmsType = meta.getDbmsType( action , ConfReader.getAttrValue( action , node , "dbtype" ) );
		DBNAME = ConfReader.getAttrValue( action , node , "dbname" , SCHEMA );
		DBUSER = ConfReader.getAttrValue( action , node , "dbuser" , SCHEMA );
		
		specific = new DatabaseSpecific( dbmsType );
	}

}
