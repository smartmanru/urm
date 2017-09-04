package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.urm.action.ActionCore;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.urm.engine.EngineTransaction;
import org.urm.meta.EngineLoader;
import org.urm.meta.EngineObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineInfrastructure extends EngineObject {

	public EngineLoader loader;
	
	private Map<String,Datacenter> mapDatacenters;
	
	public EngineInfrastructure( EngineLoader loader ) {
		super( null );
		this.loader = loader;
		mapDatacenters = new HashMap<String,Datacenter>(); 
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
			Datacenter datacenter = new Datacenter( this );
			datacenter.load( node );
			addDatacenter( datacenter );
		}
	}
	
	public void save( ActionCore action , String path , RunContext execrc ) throws Exception {
		Document doc = Common.xmlCreateDoc( "infrastructure" );
		Element root = doc.getDocumentElement();
		
		for( String id : Common.getSortedKeys( mapDatacenters ) ) {
			Datacenter datacenter = mapDatacenters.get( id );
			Element node = Common.xmlCreateElement( doc , root , "datacenter" );
			datacenter.save( doc , node );
		}
		
		Common.xmlSaveDoc( doc , path );
	}

	public void addDatacenter( Datacenter datacenter ) {
		mapDatacenters.put( datacenter.ID , datacenter );
	}

	public Datacenter findDatacenter( String id ) {
		return( mapDatacenters.get( id ) );
	}

	public String[] getDatacenters() {
		return( Common.getSortedKeys( mapDatacenters ) );
	}

	public void createDatacenter( EngineTransaction transaction , Datacenter datacenter ) throws Exception {
		addDatacenter( datacenter );
	}
	
	public void modifyDatacenter( EngineTransaction transaction , Datacenter datacenter ) throws Exception {
		for( Entry<String,Datacenter> entry : mapDatacenters.entrySet() ) {
			if( entry.getValue() == datacenter ) {
				mapDatacenters.remove( entry.getKey() );
				break;
			}
		}
		
		addDatacenter( datacenter );
	}
	
	public void deleteDatacenter( EngineTransaction transaction , Datacenter datacenter ) throws Exception {
		mapDatacenters.remove( datacenter.ID );
		datacenter.deleteDatacenter( transaction );
	}

}
