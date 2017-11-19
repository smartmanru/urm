package org.urm.db;

public class DBEnumInfo {

	public DBEnumInfo( Class<?> enumClass , int enumID ) {
		this.enumClass = enumClass;
		this.enumID = enumID;
	}
	
	Class<?> enumClass;
	int enumID;
	
}
