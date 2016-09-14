package org.urm.engine.meta;

import org.urm.action.database.DatabaseSpecific;
import org.urm.common.ConfReader;
import org.urm.engine.action.ActionBase;
import org.urm.engine.meta.Meta.VarDBMSTYPE;
import org.urm.engine.meta.Meta.VarNAMETYPE;
import org.w3c.dom.Node;

public class MetaDatabaseSchema {

	protected Meta meta;
	public MetaDatabase database;

	public String SCHEMA;
	public VarDBMSTYPE dbmsType;
	public String DBNAME;
	public String DBUSER;
	
	public DatabaseSpecific specific;
	
	public MetaDatabaseSchema( Meta meta , MetaDatabase database ) {
		this.meta = meta;
		this.database = database;
	}

	public void load( ActionBase action , Node node ) throws Exception {
		SCHEMA = action.getNameAttr( node , VarNAMETYPE.ALPHANUM );
		dbmsType = meta.getDbmsType( ConfReader.getAttrValue( node , "dbtype" ) );
		DBNAME = ConfReader.getAttrValue( node , "dbname" , SCHEMA );
		DBUSER = ConfReader.getAttrValue( node , "dbuser" , SCHEMA );
		
		specific = new DatabaseSpecific( dbmsType );
	}

}
