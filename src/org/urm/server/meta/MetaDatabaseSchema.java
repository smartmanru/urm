package org.urm.server.meta;

import org.urm.common.ConfReader;
import org.urm.server.action.ActionBase;
import org.urm.server.action.database.DatabaseSpecific;
import org.urm.server.meta.Metadata.VarDBMSTYPE;
import org.urm.server.meta.Metadata.VarNAMETYPE;
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
		SCHEMA = action.getNameAttr( node , VarNAMETYPE.ALPHANUM );
		dbmsType = meta.getDbmsType( action , ConfReader.getAttrValue( node , "dbtype" ) );
		DBNAME = ConfReader.getAttrValue( node , "dbname" , SCHEMA );
		DBUSER = ConfReader.getAttrValue( node , "dbuser" , SCHEMA );
		
		specific = new DatabaseSpecific( dbmsType );
	}

}
