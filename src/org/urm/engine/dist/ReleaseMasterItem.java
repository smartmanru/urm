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

	public void load( ActionBase action , Node root ) throws Exception {
		KEY = ConfReader.getAttrValue( root , "key" );
		RELEASE = ConfReader.getAttrValue( root , "release" );
		DELIVERY = ConfReader.getAttrValue( root , "delivery" );
		FOLDER = ConfReader.getAttrValue( root , "folder" );
		FILE = ConfReader.getAttrValue( root , "file" );
		MD5 = ConfReader.getAttrValue( root , "md5" );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "key" , KEY );
		Common.xmlSetElementAttr( doc , root , "release" , RELEASE );
		Common.xmlSetElementAttr( doc , root , "delivery" , DELIVERY );
		Common.xmlSetElementAttr( doc , root , "folder" , FOLDER );
		Common.xmlSetElementAttr( doc , root , "file" , FILE );
		Common.xmlSetElementAttr( doc , root , "md5" , MD5 );
	}
	
	public void setItem( ActionBase action , Release release , MetaDistrBinaryItem distItem , DistItemInfo info ) throws Exception {
		KEY = distItem.KEY;
		RELEASE = release.RELEASEVER;
		DELIVERY = distItem.delivery.NAME;
		FOLDER = distItem.delivery.FOLDER;
		FILE = info.fileName;
		MD5 = info.md5value;
	}
	
}