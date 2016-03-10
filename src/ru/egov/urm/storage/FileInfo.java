package ru.egov.urm.storage;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaDistrBinaryItem;
import ru.egov.urm.meta.MetaDistrConfItem;
import ru.egov.urm.run.ActionBase;

public class FileInfo {

	MetaDistrBinaryItem binaryItem;
	MetaDistrConfItem confItem;
	public String itemName;
	public String version;
	public String md5value;
	public String deployNameNoVersion;
	public String finalName;
	boolean partial;
	
	public FileInfo() {
	}

	public FileInfo( MetaDistrBinaryItem item , String version , String md5value , String deployNameNoVersion , String finalName ) {
		this.binaryItem = item;
		this.itemName = item.KEY;
		this.version = version;
		this.md5value = md5value;
		this.deployNameNoVersion = deployNameNoVersion;
		this.finalName = finalName;
	}
	
	public FileInfo( MetaDistrConfItem item , String version , String md5value , boolean partial) {
		this.confItem = item;
		this.itemName = item.KEY;
		this.version = version;
		this.md5value = md5value;
	}
	
	public void set( ActionBase action , MetaDistrBinaryItem item , String value ) throws Exception {
		this.binaryItem = item; 
		this.confItem = null; 
		this.itemName = item.KEY;
		this.version = Common.getListItem( value , ":" , 0 );
		this.md5value = Common.getListItem( value , ":" , 1 );
		this.deployNameNoVersion = Common.getListItem( value , ":" , 2 );
		this.finalName = Common.getListItem( value , ":" , 3 );
	}

	public void set( ActionBase action , MetaDistrConfItem item , String value ) throws Exception {
		this.binaryItem = null; 
		this.confItem = item; 
		this.itemName = item.KEY;
		this.version = Common.getListItem( value , ":" , 0 );
		this.md5value = Common.getListItem( value , ":" , 1 );
		this.partial = Common.getBooleanValue( Common.getListItem( value , ":" , 2 ) );
	}

	public String value( ActionBase action ) throws Exception {
		if( confItem != null )
			return( version + ":" + md5value + ":" + Common.getBooleanValue( partial ) );
		return( version + ":" + md5value + ":" + deployNameNoVersion + ":" + finalName );
	}

	public String getInfoName( ActionBase action ) throws Exception {
		return( itemName + ".ver" );
	}
	
	public String getFileName( ActionBase action ) throws Exception {
		return( itemName + ".file" );
	}
	
	public static String getInfoName( ActionBase action , MetaDistrConfItem confItem ) throws Exception {
		return( confItem.KEY + ".ver" );
	}
	
	public static String getInfoName( ActionBase action , MetaDistrBinaryItem binaryItem ) throws Exception {
		return( binaryItem.KEY + ".ver" );
	}
	
	public static String getFileName( ActionBase action , MetaDistrConfItem confItem ) throws Exception {
		return( confItem.KEY + ".file" );
	}
	
	public static String getFileName( ActionBase action , MetaDistrBinaryItem binaryItem ) throws Exception {
		return( binaryItem.KEY + ".file" );
	}
	
}
