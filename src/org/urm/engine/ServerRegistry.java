package org.urm.engine;

import org.urm.action.ActionCore;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerRegistry {

	public ServerLoader loader;
	
	public ServerMirrors mirrors;
	public ServerResources resources;
	public ServerDirectory directory;
	public ServerBuilders builders;

	public ServerRegistry( ServerLoader loader ) {
		this.loader = loader;
		mirrors = new ServerMirrors( this ); 
		resources = new ServerResources( this );
		directory = new ServerDirectory( this );
		builders = new ServerBuilders( this ); 
	}
	
	public void load( String propertyFile , RunContext execrc ) throws Exception {
		Document doc = ConfReader.readXmlFile( execrc , propertyFile );
		Node root = doc.getDocumentElement();
		
		Node node;
		node = ConfReader.xmlGetFirstChild( root , "resources" );
		resources.load( node );
		node = ConfReader.xmlGetFirstChild( root , "directory" );
		directory.load( node );
		node = ConfReader.xmlGetFirstChild( root , "mirror" );
		mirrors.load( node );
		node = ConfReader.xmlGetFirstChild( root , "build" );
		builders.load( node );
	}
	
	public void save( ActionCore action , String path , RunContext execrc ) throws Exception {
		Document doc = Common.xmlCreateDoc( "registry" );
		Element root = doc.getDocumentElement();
		
		Element node;
		node = Common.xmlCreateElement( doc , root , "resources" );
		resources.save( doc , node );
		node = Common.xmlCreateElement( doc , root , "directory" );
		directory.save( doc , node );
		node = Common.xmlCreateElement( doc , root , "mirror" );
		mirrors.save( doc , node );
		node = Common.xmlCreateElement( doc , root , "build" );
		builders.save( doc , node );
		
		Common.xmlSaveDoc( doc , path );
	}

	public void setResources( ServerTransaction transaction , ServerResources resourcesNew ) throws Exception {
		resources = resourcesNew;
	}
	
	public void setDirectory( ServerTransaction transaction , ServerDirectory directoryNew ) throws Exception {
		directory = directoryNew;
	}
	
	public void setBuilders( ServerTransaction transaction , ServerBuilders buildersNew ) throws Exception {
		builders = buildersNew;
	}
	
}
