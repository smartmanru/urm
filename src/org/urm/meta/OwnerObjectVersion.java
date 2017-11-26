package org.urm.meta;

import org.urm.db.core.DBEnums.DBEnumObjectVersionType;
import org.urm.db.core.DBEnums.DBEnumOwnerStatusType;

public class OwnerObjectVersion {

	public int OWNER_OBJECT_ID;
	public DBEnumObjectVersionType OBJECT_VERSION_TYPE; 
	public int VERSION;
	public Integer LAST_IMPORT_ID;
	public String LAST_NAME;
	public DBEnumOwnerStatusType OWNER_STATUS_TYPE;
	
	public int nextVersion;
	
	public OwnerObjectVersion( int objectId , DBEnumObjectVersionType type ) {
		this.OWNER_OBJECT_ID = objectId;
		this.OBJECT_VERSION_TYPE = type;
		this.VERSION = -1;
		this.LAST_IMPORT_ID = null;
		this.LAST_NAME = null;
		this.OWNER_STATUS_TYPE = DBEnumOwnerStatusType.UNKNOWN;
		
		nextVersion = -1;
	}
	
}
