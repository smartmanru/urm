package org.urm.meta.engine;

import java.util.LinkedList;
import java.util.List;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.ServerObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerReleaseLifecycle extends ServerObject {

	ServerReleaseLifecycles lifecycles;
	
	public String ID;
	public String DESC;
	
	List<ServerReleaseLifecyclePhase> phases;
	
	public ServerReleaseLifecycle( ServerReleaseLifecycles lifecycles ) {
		super( lifecycles );
		this.lifecycles = lifecycles;
		phases = new LinkedList<ServerReleaseLifecyclePhase>();
	}
	
	public ServerReleaseLifecycle copy() throws Exception {
		ServerReleaseLifecycle r = new ServerReleaseLifecycle( lifecycles );
		r.ID = ID;
		r.DESC = DESC;
		
		for( ServerReleaseLifecyclePhase phase : phases ) {
			ServerReleaseLifecyclePhase rphase = phase.copy( r );
			r.addPhase( rphase );
		}
		return( r );
	}
	
	private void addPhase( ServerReleaseLifecyclePhase phase ) {
		phases.add( phase );
	}
	
	public void load( Node root ) throws Exception {
		if( root == null )
			return;
		
		ID = ConfReader.getAttrValue( root , "id" );
		DESC = ConfReader.getAttrValue( root , "desc" );
		
		Node[] list = ConfReader.xmlGetChildren( root , "phase" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			ServerReleaseLifecyclePhase phase = new ServerReleaseLifecyclePhase( this );
			phase.load( node );
			addPhase( phase );
		}
	}

	public void save( Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "id" , ID );
		Common.xmlSetElementAttr( doc , root , "desc" , DESC );
		
		for( ServerReleaseLifecyclePhase phase : phases ) {
			Element element = Common.xmlCreateElement( doc , root , "phase" );
			phase.save( doc , element );
		}
	}

	
}
