package org.urm.engine.dist;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.product.Meta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReleaseMasterHistory {

	public Meta meta;
	public ReleaseMaster master;

	public String RELEASE;
	public Date appendDate;
	
	public ReleaseMasterHistory( Meta meta , ReleaseMaster master ) {
		this.meta = meta;
		this.master = master;
	}

	public void load( ActionBase action , Node root ) throws Exception {
		RELEASE = ConfReader.getAttrValue( root , "release" );
		appendDate = Common.getDateValue( ConfReader.getAttrValue( root , "added" ) );
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "release" , RELEASE );
		Common.xmlSetElementAttr( doc , root , "added" , Common.getDateValue( appendDate ) );
	}

	public void create( ActionBase action , String RELEASEVER ) throws Exception {
		this.RELEASE = RELEASEVER;
		this.appendDate = new Date();
	}
	
}
