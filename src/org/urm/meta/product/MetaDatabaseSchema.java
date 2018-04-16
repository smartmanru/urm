package org.urm.meta.product;

import org.urm.db.core.DBEnums.*;

public class MetaDatabaseSchema {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_DBTYPE = "dbtype";
	public static String PROPERTY_DBNAME = "dbname";
	public static String PROPERTY_DBUSER = "dbuser";
	
	public Meta meta;
	public MetaDatabase database;

	public int ID;
	public String NAME;
	public String DESC;
	public DBEnumDbmsType DBMS_TYPE;
	public String DBNAMEDEF;
	public String DBUSERDEF;
	public int PV;
	public DBEnumChangeType CHANGETYPE;
	
	public MetaDatabaseSchema( Meta meta , MetaDatabase database ) {
		this.meta = meta;
		this.database = database;
		ID = -1;
		PV = -1;
	}
	
	public MetaDatabaseSchema copy( Meta meta , MetaDatabase database ) throws Exception {
		MetaDatabaseSchema r = new MetaDatabaseSchema( meta , database );

		r.ID = ID;
		r.NAME = NAME;
		r.DBMS_TYPE = DBMS_TYPE;
		r.DBNAMEDEF = DBNAMEDEF;
		r.DBUSERDEF = DBUSERDEF;
		r.DESC = DESC;
		r.PV = PV;
		r.CHANGETYPE = CHANGETYPE;
		return( r );
	}

	public void createSchema( String name , String desc , DBEnumDbmsType type , String dbname , String dbuser ) throws Exception {
		modifySchema( name , desc , type , dbname , dbuser );
	}
	
	public void modifySchema( String name , String desc , DBEnumDbmsType type , String dbname , String dbuser ) throws Exception {
		this.NAME = name;
		this.DESC = desc;
		this.DBMS_TYPE = type;
		this.DBNAMEDEF = dbname;
		this.DBUSERDEF = dbuser;
	}

}
