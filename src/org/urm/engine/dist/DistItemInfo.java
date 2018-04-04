package org.urm.engine.dist;

import java.util.Date;

import org.urm.common.Common;
import org.urm.engine.storage.FileInfo;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaProductDoc;

public class DistItemInfo {

	public MetaDistrDelivery delivery;
	public MetaDistrBinaryItem distBinaryItem;
	public MetaDistrConfItem distConfItem;
	public MetaProductDoc distDocItem;
	
	private String distDeliveryFolder;
	private String deliveryItemFolder;

	private boolean found;
	private String finalName;
	private String md5value;
	private Date timestamp; 
	
	public DistItemInfo( MetaDistrBinaryItem distBinaryItem ) {
		this.found = false;
		this.delivery = distBinaryItem.delivery;
		this.distBinaryItem = distBinaryItem;
		this.distDeliveryFolder = delivery.FOLDER;
		this.deliveryItemFolder = Dist.BINARY_FOLDER;
	}

	public DistItemInfo( MetaDistrConfItem distConfItem ) {
		this.found = false;
		this.delivery = distConfItem.delivery;
		this.distConfItem = distConfItem;
		this.distDeliveryFolder = delivery.FOLDER;
		this.deliveryItemFolder = Dist.CONFIG_FOLDER;
	}

	public DistItemInfo( MetaDistrDelivery delivery , MetaProductDoc distDocItem ) {
		this.found = false;
		this.delivery = delivery;
		this.distDocItem = distDocItem;
		this.distDeliveryFolder = delivery.FOLDER;
		this.deliveryItemFolder = Dist.DOC_FOLDER;
	}

	public void setFinalName( DistItemInfo src ) {
		this.found = src.found;
		this.finalName = src.finalName;
	}
	
	public void setMD5( DistItemInfo src ) {
		this.md5value = src.md5value;
	}
	
	public void setTimestamp( DistItemInfo src ) {
		this.timestamp = src.timestamp; 
	}
	
	public void setFinalName( String fileName ) {
		this.finalName = fileName;
	}
	
	public void setMD5( String md5value ) {
		this.md5value = md5value;
	}

	public void setTimestamp( Date timestamp ) {
		this.timestamp = timestamp;
	}

	public String getDistItemFolder() {
		return( Common.getPath( distDeliveryFolder , deliveryItemFolder ) );
	}

	public boolean isFound() {
		return( found );
	}

	public String getFinalName() {
		return( finalName );
	}

	public void clearFinal() {
		this.found = false;
		this.finalName = "";
		this.md5value = "";
		this.timestamp = null; 
	}

	public boolean checkMD5( FileInfo runInfo ) throws Exception {
		if( md5value.isEmpty() || runInfo.md5value == null || runInfo.md5value.isEmpty() )
			Common.exitUnexpected();
		
		if( md5value.equals( runInfo.md5value ) )
			return( true );
		return( false );
	}

	public String getMD5() {
		return( md5value );
	}

	public Date getTimestamp() {
		return( timestamp );
	}
	
	public String getDeliveryItemPath() {
		return( Common.getPath( deliveryItemFolder , finalName ) );
	}
	
}
