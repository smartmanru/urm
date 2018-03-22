package org.urm.meta.release;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.product.Meta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReleaseMasterHistory {

	public static String PROPERTY_RELEASE = "release";
	public static String PROPERTY_DATEADDED = "added";
	
	public Meta meta;
	public ReleaseMaster master;

	public String RELEASE;
	public Date appendDate;
	
	public ReleaseMasterHistory( Meta meta , ReleaseMaster master ) {
		this.meta = meta;
		this.master = master;
	}

	public ReleaseMasterHistory copy( ReleaseMaster rm ) throws Exception {
		ReleaseMasterHistory rrh = new ReleaseMasterHistory( rm.meta , rm );
		rrh.RELEASE = RELEASE;
		rrh.appendDate = appendDate;
		return( rrh );
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		RELEASE = ConfReader.getAttrValue( root , PROPERTY_RELEASE );
		appendDate = Common.getDateValue( ConfReader.getAttrValue( root , PROPERTY_DATEADDED ) );
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , PROPERTY_RELEASE , RELEASE );
		Common.xmlSetElementAttr( doc , root , PROPERTY_DATEADDED , Common.getDateValue( appendDate ) );
	}

	public void create( ActionBase action , String RELEASEVER ) throws Exception {
		this.RELEASE = RELEASEVER;
		this.appendDate = new Date();
	}
	
}
