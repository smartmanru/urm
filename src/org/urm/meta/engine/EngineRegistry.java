package org.urm.meta.engine;

import org.urm.action.ActionCore;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.urm.db.DBConnection;
import org.urm.db.engine.DBEngineDirectory;
import org.urm.engine.Engine;
import org.urm.engine.TransactionBase;
import org.urm.meta.EngineLoader;
import org.urm.meta.EngineObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineRegistry extends EngineObject {

	public EngineLoader loader;
	public Engine engine;
	public RunContext execrc;
	
	public EngineMirrors mirrors;
	public EngineResources resources;
	public EngineDirectory directory;
	public EngineBuilders builders;

	public EngineRegistry( EngineLoader loader ) {
		super( null );
		this.loader = loader;
		this.engine = loader.engine;
		this.execrc = engine.execrc;
		mirrors = new EngineMirrors( this ); 
		resources = new EngineResources( this );
		directory = new EngineDirectory( this );
		builders = new EngineBuilders( this ); 
	}
	
	@Override
	public String getName() {
		return( "server-registry" );
	}
	
	public void loadmixed( String propertyFile , DBConnection c , boolean importxml , boolean withSystems ) throws Exception {
		Document doc = ConfReader.readXmlFile( execrc , propertyFile );
		Node root = doc.getDocumentElement();
		Node node;
		
		if( importxml == false || withSystems == false )
			directory.loaddb( c );
		else {
			node = ConfReader.xmlGetFirstChild( root , "directory" );
			directory.loadxml( node , c );
		}
		
		node = ConfReader.xmlGetFirstChild( root , "resources" );
		resources.load( node );
		node = ConfReader.xmlGetFirstChild( root , "mirror" );
		mirrors.load( node );
		node = ConfReader.xmlGetFirstChild( root , "build" );
		builders.load( node );
	}
	
	public void savexml( ActionCore action , String path , RunContext execrc ) throws Exception {
		Document doc = Common.xmlCreateDoc( "registry" );
		Element root = doc.getDocumentElement();
		
		Element node;
		node = Common.xmlCreateElement( doc , root , "resources" );
		resources.save( doc , node );
		node = Common.xmlCreateElement( doc , root , "directory" );
		DBEngineDirectory.savexml( directory , doc , node );
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
