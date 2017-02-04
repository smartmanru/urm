package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionCore;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.urm.meta.ServerLoader;
import org.urm.meta.ServerObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerReleaseLifecycles extends ServerObject {

	public ServerLoader loader;
	
	private Map<String,ServerReleaseLifecycle> mapLifecycles;
	
	public ServerReleaseLifecycles( ServerLoader loader ) {
		super( null );
		this.loader = loader;
		mapLifecycles = new HashMap<String,ServerReleaseLifecycle>(); 
	}
	
	public void load( String lcFile , RunContext execrc ) throws Exception {
		Document doc = ConfReader.readXmlFile( execrc , lcFile );
		Node root = doc.getDocumentElement();
		
		Node[] list = ConfReader.xmlGetChildren( root , "lifecycle" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			ServerReleaseLifecycle lc = new ServerReleaseLifecycle( this );
			lc.load( node );
			addLifecycle( lc );
		}
	}
	
	public void save( ActionCore action , String path , RunContext execrc ) throws Exception {
		Document doc = Common.xmlCreateDoc( "lifecycles" );
		Element root = doc.getDocumentElement();
		
		for( String id : Common.getSortedKeys( mapLifecycles ) ) {
			ServerReleaseLifecycle lc = mapLifecycles.get( id );
			Element node = Common.xmlCreateElement( doc , root , "lifecycle" );
			lc.save( doc , node );
		}
		
		Common.xmlSaveDoc( doc , path );
	}

	public void addLifecycle( ServerReleaseLifecycle lc ) {
		mapLifecycles.put( lc.ID , lc );
	}

	public ServerReleaseLifecycle findLifecycle( String id ) {
		return( mapLifecycles.get( id ) );
	}

	public String[] getLifecycles() {
		return( Common.getSortedKeys( mapLifecycles ) );
	}

}
