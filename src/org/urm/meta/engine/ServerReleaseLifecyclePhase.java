package org.urm.meta.engine;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.ServerObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerReleaseLifecyclePhase extends ServerObject {

	ServerReleaseLifecycle lc;
	
	public String ID;
	public String DESC;
	
	public ServerReleaseLifecyclePhase( ServerReleaseLifecycle lc ) {
		super( lc );
		this.lc = lc;
	}
	
	public void load( Node root ) throws Exception {
		ID = ConfReader.getAttrValue( root , "id" );
		DESC = ConfReader.getAttrValue( root , "desc" );
	}

	public void save( Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "id" , ID );
		Common.xmlSetElementAttr( doc , root , "desc" , DESC );
	}
	
	public ServerReleaseLifecyclePhase copy( ServerReleaseLifecycle rlc ) throws Exception {
		ServerReleaseLifecyclePhase r = new ServerReleaseLifecyclePhase( rlc );
		r.ID = ID;
		r.DESC = DESC;
		return( r );
	}
	
	
}
