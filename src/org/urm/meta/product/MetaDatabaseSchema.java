package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.action.database.DatabaseSpecific;
import org.urm.common.ConfReader;
import org.urm.meta.product.Meta.VarDBMSTYPE;
import org.urm.meta.product.Meta.VarNAMETYPE;
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
		dbmsType = Meta.getDbmsType( ConfReader.getAttrValue( node , "dbtype" ) , false );
		DBNAME = ConfReader.getAttrValue( node , "dbname" , SCHEMA );
		DBUSER = ConfReader.getAttrValue( node , "dbuser" , SCHEMA );
		
		specific = new DatabaseSpecific( meta , dbmsType );
	}

}
