package org.urm.meta.engine;

import org.urm.action.ActionCore;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.urm.db.DBConnection;
import org.urm.engine.EngineTransaction;
import org.urm.engine.TransactionBase;
import org.urm.meta.EngineLoader;
import org.urm.meta.EngineObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineRegistry extends EngineObject {

	public EngineLoader loader;
	
	public EngineMirrors mirrors;
	public EngineResources resources;
	public EngineDirectory directory;
	public EngineBuilders builders;

	public EngineRegistry( EngineLoader loader ) {
		super( null );
		this.loader = loader;
		mirrors = new EngineMirrors( this ); 
		resources = new EngineResources( this );
		directory = new EngineDirectory( this );
		builders = new EngineBuilders( this ); 
	}
	
	@Override
	public String getName() {
		return( "server-registry" );
	}
	
	public void load( String propertyFile , RunContext execrc , DBConnection c , boolean savedb , EngineTransaction transaction  ) throws Exception {
		Document doc = ConfReader.readXmlFile( execrc , propertyFile );
		Node root = doc.getDocumentElement();
		
		Node node;
		node = ConfReader.xmlGetFirstChild( root , "resources" );
		resources.load( node );
		node = ConfReader.xmlGetFirstChild( root , "directory" );
		directory.load( node , c , savedb , transaction );
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

	public void setResources( TransactionBase transaction , EngineResources resourcesNew ) throws Exception {
		resources = resourcesNew;
	}
	
	public void setDirectory( TransactionBase transaction , EngineDirectory directoryNew ) throws Exception {
		directory = directoryNew;
	}
	
	public void setMirrors( TransactionBase transaction , EngineMirrors mirrorsNew ) throws Exception {
		mirrors = mirrorsNew;
	}
	
	public void setBuilders( TransactionBase transaction , EngineBuilders buildersNew ) throws Exception {
		builders = buildersNew;
	}
	
}
