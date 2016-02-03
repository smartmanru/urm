package ru.egov.urm.meta;

import org.w3c.dom.Node;

import ru.egov.urm.ConfReader;
import ru.egov.urm.meta.Metadata.VarDBMSTYPE;
import ru.egov.urm.meta.Metadata.VarNAMETYPE;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.database.DatabaseSpecific;

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
		
		specific = DatabaseSpecific.getSpecificHandler( action , dbmsType , null , null );
	}

}
