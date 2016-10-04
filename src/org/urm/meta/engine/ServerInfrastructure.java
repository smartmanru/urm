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

public class ServerInfrastructure extends ServerObject {

	public ServerLoader loader;
	
	private Map<String,ServerNetwork> mapNetworks;
	
	public ServerInfrastructure( ServerLoader loader ) {
		super( null );
		this.loader = loader;
		mapNetworks = new HashMap<String,ServerNetwork>(); 
	}
	
	public void load( String infrastructureFile , RunContext execrc ) throws Exception {
		Document doc = ConfReader.readXmlFile( execrc , infrastructureFile );
		Node root = doc.getDocumentElement();
		
		Node[] list = ConfReader.xmlGetChildren( root , "network" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			ServerNetwork network = new ServerNetwork( this );
			network.load( node );
			addNetwork( network );
		}
	}
	
	public void save( ActionCore action , String path , RunContext execrc ) throws Exception {
		Document doc = Common.xmlCreateDoc( "infrastructure" );
		Element root = doc.getDocumentElement();
		
		for( String id : Common.getSortedKeys( mapNetworks ) ) {
			ServerNetwork network = mapNetworks.get( id );
			Element node = Common.xmlCreateElement( doc , root , "network" );
			network.save( doc , node );
		}
		
		Common.xmlSaveDoc( doc , path );
	}

	public void addNetwork( ServerNetwork network ) {
		mapNetworks.put( network.ID , network );
	}
	
}
