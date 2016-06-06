package org.urm.storage;

import java.util.HashMap;
import java.util.Map;

import org.urm.Common;
import org.urm.action.ActionBase;
import org.urm.meta.MetaDistrBinaryItem;
import org.urm.meta.MetaDistrConfItem;

public class FileInfo {

	public MetaDistrBinaryItem binaryItem;
	public MetaDistrConfItem confItem;
	public String itemName;
	public String version;
	public String md5value;
	public String deployBaseName;
	public String deployFinalName;
	boolean partial;
	
	public FileInfo() {
	}

	public FileInfo( MetaDistrBinaryItem item , String version , String md5value , String deployNameNoVersion , String finalName ) {
		this.binaryItem = item;
		this.itemName = item.KEY;
		this.version = Common.nonull( version );
		this.md5value = Common.nonull( md5value );
		this.deployBaseName = Common.nonull( deployNameNoVersion );
		this.deployFinalName = Common.nonull( finalName );
	}
	
	public FileInfo( MetaDistrConfItem item , String version , String md5value , boolean partial ) {
		this.confItem = item;
		this.itemName = item.KEY;
		this.version = Common.nonull( version );
		this.md5value = Common.nonull( md5value );
	}
	
	private Map<String,String> getParams( ActionBase action , String value ) throws Exception {
		Map<String,String> params = new HashMap<String,String>();
		for( String pair : Common.split( value , "," ) ) {
			String[] values = Common.split( pair , "=" );
			if( values.length == 2 )
				params.put( values[0] , values[1] );
		}
		
		return( params );
	}
	
	private void scatterParams( ActionBase action , String value ) throws Exception {
		Map<String,String> params = getParams( action , value );
		version = Common.nonull( params.get( "version" ) );
		md5value = Common.nonull( params.get( "md5" ) );
		deployBaseName = Common.nonull( params.get( "base" ) );
		deployFinalName = Common.nonull( params.get( "final" ) );
		if( confItem != null );
			partial = Common.getBooleanValue( params.get( "partial" ) );
	}
	
	private String gatherParams( ActionBase action ) throws Exception {
		Map<String,String> params = new HashMap<String,String>();
		params.put( "version" , version );
		params.put( "md5" , md5value );
		params.put( "base" , deployBaseName );
		params.put( "final" , deployFinalName );
		if( confItem != null )
			params.put( "partial" , Common.getBooleanValue( partial ) );
		
		String value = "";
		for( String key : params.keySet() ) {
			String data = params.get( key );
			if( data != null && data.isEmpty() == false ) {
				if( !value.isEmpty() )
					value += ",";
				value += key + "=" + data;
			}
		}
		return( value );
	}
	
	public void set( ActionBase action , MetaDistrBinaryItem item , String value ) throws Exception {
		this.binaryItem = item; 
		this.confItem = null; 
		this.itemName = item.KEY;
		scatterParams( action , value );
	}

	public void set( ActionBase action , MetaDistrConfItem item , String value ) throws Exception {
		this.binaryItem = null; 
		this.confItem = item; 
		this.itemName = item.KEY;
		scatterParams( action , value );
	}

	public String value( ActionBase action ) throws Exception {
		return( gatherParams( action ) );
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
