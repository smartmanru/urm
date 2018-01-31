package org.urm.engine.dist;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReleaseMasterItem {

	public Meta meta;
	public ReleaseMaster master;

	public String KEY;
	public String RELEASE;
	public String DELIVERY;
	public String FOLDER;
	public String FILE;
	public String MD5;
	
	public ReleaseMasterItem( Meta meta , ReleaseMaster master ) {
		this.meta = meta;
		this.master = master;
	}

	public ReleaseMasterItem copy( ActionBase action , ReleaseMaster rm ) throws Exception {
		ReleaseMasterItem ritem = new ReleaseMasterItem( rm.meta , rm );
		ritem.KEY = KEY;
		ritem.RELEASE = RELEASE;
		ritem.DELIVERY = DELIVERY;
		ritem.FOLDER = FOLDER;
		ritem.FILE = FILE;
		ritem.MD5 = MD5;
		return( ritem );
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		KEY = ConfReader.getAttrValue( root , Release.PROPERTY_KEY );
		RELEASE = ConfReader.getAttrValue( root , Release.PROPERTY_RELEASE );
		DELIVERY = ConfReader.getAttrValue( root , Release.PROPERTY_DELIVERY );
		FOLDER = ConfReader.getAttrValue( root , Release.PROPERTY_FOLDER );
		FILE = ConfReader.getAttrValue( root , Release.PROPERTY_FILE );
		MD5 = ConfReader.getAttrValue( root , Release.PROPERTY_MD5 );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , Release.PROPERTY_KEY , KEY );
		Common.xmlSetElementAttr( doc , root , Release.PROPERTY_RELEASE , RELEASE );
		Common.xmlSetElementAttr( doc , root , Release.PROPERTY_DELIVERY , DELIVERY );
		Common.xmlSetElementAttr( doc , root , Release.PROPERTY_FOLDER , FOLDER );
		Common.xmlSetElementAttr( doc , root , Release.PROPERTY_FILE , FILE );
		Common.xmlSetElementAttr( doc , root , Release.PROPERTY_MD5 , MD5 );
	}
	
	public void setRelease( ActionBase action , Release src , MetaDistrBinaryItem distItem , DistItemInfo info ) throws Exception {
		KEY = distItem.NAME;
		RELEASE = src.RELEASEVER;
		DELIVERY = distItem.delivery.NAME;
		FOLDER = distItem.delivery.FOLDER;
		FILE = info.fileName;
		MD5 = info.md5value;
	}

	public void setManual( ActionBase action , MetaDistrBinaryItem distItem , DistItemInfo info ) throws Exception {
		KEY = distItem.NAME;
		RELEASE = master.release.RELEASEVER + "-manual";
		DELIVERY = distItem.delivery.NAME;
		FOLDER = distItem.delivery.FOLDER;
		FILE = info.fileName;
		MD5 = info.md5value;
	}
	
	public void update( ActionBase action , MetaDistrBinaryItem distItem , DistItemInfo info ) throws Exception {
		DELIVERY = distItem.delivery.NAME;
		FOLDER = distItem.delivery.FOLDER;
		FILE = info.fileName;
		
		if( !MD5.equals( info.md5value ) ) {
			RELEASE = "manual-" + master.release.RELEASEVER;
			MD5 = info.md5value;
		}
	}

}
