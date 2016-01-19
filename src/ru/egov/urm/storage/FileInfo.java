package ru.egov.urm.storage;

import ru.egov.urm.Common;
import ru.egov.urm.run.ActionBase;

public class FileInfo {

	public String version;
	public String md5value;
	public String deployNameNoVersion;
	public String finalName;
	
	public FileInfo() {
	}

	public FileInfo( String version , String md5value , String deployNameNoVersion , String finalName ) {
		this.version = version;
		this.md5value = md5value;
		this.deployNameNoVersion = deployNameNoVersion;
		this.finalName = finalName;
	}
	
	public void split( ActionBase action , String value ) throws Exception {
		version = Common.getListItem( value , ":" , 0 );
		md5value = Common.getListItem( value , ":" , 1 );
		deployNameNoVersion = Common.getListItem( value , ":" , 2 );
		finalName = Common.getListItem( value , ":" , 3 );
	}

	public String value( ActionBase action ) throws Exception {
		String value = version + ":" + md5value + ":" + deployNameNoVersion + ":" + finalName;
		return( value );
	}

}
