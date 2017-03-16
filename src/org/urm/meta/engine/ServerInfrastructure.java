package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.urm.action.ActionCore;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.urm.engine.ServerTransaction;
import org.urm.meta.ServerLoader;
import org.urm.meta.ServerObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerInfrastructure extends ServerObject {

	public ServerLoader loader;
	
	private Map<String,ServerDatacenter> mapDatacenters;
	
	public ServerInfrastructure( ServerLoader loader ) {
		super( null );
		this.loader = loader;
		mapDatacenters = new HashMap<String,ServerDatacenter>(); 
	}
	
	@Override
	public String getName() {
		return( "server-infrastructure" );
	}
	
	public void load( String infrastructureFile , RunContext execrc ) throws Exception {
		Document doc = ConfReader.readXmlFile( execrc , infrastructureFile );
		Node root = doc.getDocumentElement();
		
		Node[] list = ConfReader.xmlGetChildren( root , "datacenter" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			ServerDatacenter datacenter = new ServerDatacenter( this );
			datacenter.load( node );
			addDatacenter( datacenter );
		}
	}
	
	public void save( ActionCore action , String path , RunContext execrc ) throws Exception {
		Document doc = Common.xmlCreateDoc( "infrastructure" );
		Element root = doc.getDocumentElement();
		
		for( String id : Common.getSortedKeys( mapDatacenters ) ) {
			ServerDatacenter datacenter = mapDatacenters.get( id );
			Element node = Common.xmlCreateElement( doc , root , "datacenter" );
			datacenter.save( doc , node );
		}
		
		Common.xmlSaveDoc( doc , path );
	}

	public void addDatacenter( ServerDatacenter datacenter ) {
		mapDatacenters.put( datacenter.ID , datacenter );
	}

	public ServerDatacenter findDatacenter( String id ) {
		return( mapDatacenters.get( id ) );
	}

	public String[] getDatacenters() {
		return( Common.getSortedKeys( mapDatacenters ) );
	}

	public void createDatacenter( ServerTransaction transaction , ServerDatacenter datacenter ) throws Exception {
		addDatacenter( datacenter );
	}
	
	public void modifyDatacenter( ServerTransaction transaction , ServerDatacenter datacenter ) throws Exception {
		for( Entry<String,ServerDatacenter> entry : mapDatacenters.entrySet() ) {
			if( entry.getValue() == datacenter ) {
				mapDatacenters.remove( entry.getKey() );
				break;
			}
		}
		
		addDatacenter( datacenter );
	}
	
	public void deleteDatacenter( ServerTransaction transaction , ServerDatacenter datacenter ) throws Exception {
		mapDatacenters.remove( datacenter.ID );
		datacenter.deleteDatacenter( transaction );
	}

}
