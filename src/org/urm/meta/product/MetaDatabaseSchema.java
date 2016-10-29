package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.action.database.DatabaseSpecific;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.product.Meta.VarDBMSTYPE;
import org.urm.meta.product.Meta.VarNAMETYPE;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaDatabaseSchema {

	protected Meta meta;
	public MetaDatabase database;

	public String SCHEMA;
	public VarDBMSTYPE dbmsType;
	public String DBNAME;
	public String DBUSER;
	public String DESC;
	
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
		DESC = ConfReader.getAttrValue( node , "desc" );
		
		specific = new DatabaseSpecific( meta , dbmsType );
	}

	public MetaDatabaseSchema copy( ActionBase action , Meta meta , MetaDatabase database ) throws Exception {
		MetaDatabaseSchema r = new MetaDatabaseSchema( meta , database );
		
		r.SCHEMA = SCHEMA;
		r.dbmsType = dbmsType;
		r.DBNAME = DBNAME;
		r.DBUSER = DBUSER;
		r.DESC = DESC;
		r.specific = new DatabaseSpecific( meta , dbmsType );
		return( r );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , SCHEMA );
		Common.xmlSetElementAttr( doc , root , "dbtype" , Common.getEnumLower( dbmsType ) );
		Common.xmlSetElementAttr( doc , root , "dbname" , DBNAME );
		Common.xmlSetElementAttr( doc , root , "dbuser" , DBUSER );
		Common.xmlSetElementAttr( doc , root , "desc" , DESC );
	}
	
}
