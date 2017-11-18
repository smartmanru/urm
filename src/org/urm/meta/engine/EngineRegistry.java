package org.urm.meta.engine;

import org.urm.action.ActionCore;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.urm.db.DBConnection;
import org.urm.engine.Engine;
import org.urm.engine.TransactionBase;
import org.urm.meta.EngineData;
import org.urm.meta.EngineMatcher;
import org.urm.meta.EngineObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineRegistry extends EngineObject {

	public EngineData data;
	public Engine engine;
	public RunContext execrc;
	
	public EngineMirrors mirrors;
	public EngineResources resources;
	public EngineBuilders builders;

	public EngineRegistry( EngineData data ) {
		super( null );
		this.data = data;
		this.engine = data.engine;
		this.execrc = engine.execrc;
		mirrors = new EngineMirrors( this ); 
		resources = new EngineResources( this );
		builders = new EngineBuilders( this ); 
	}
	
	@Override
	public String getName() {
		return( "server-registry" );
	}
	
	public void loadxml( EngineMatcher matcher , Node root , DBConnection c ) throws Exception {
		Node node;
		node = ConfReader.xmlGetFirstChild( root , "resources" );
		resources.load( node );
		node = ConfReader.xmlGetFirstChild( root , "mirror" );
		mirrors.load( node );
		node = ConfReader.xmlGetFirstChild( root , "build" );
		builders.load( node );
	}
	
	public void savexml( ActionCore action , Document doc , Element root , RunContext execrc ) throws Exception {
		Element node;
		node = Common.xmlCreateElement( doc , root , "resources" );
		resources.save( doc , node );
		node = Common.xmlCreateElement( doc , root , "mirror" );
		mirrors.save( doc , node );
		node = Common.xmlCreateElement( doc , root , "build" );
		builders.save( doc , node );
	}

	public void setResources( TransactionBase transaction , EngineResources resourcesNew ) throws Exception {
		resources = resourcesNew;
	}
	
	public void setMirrors( TransactionBase transaction , EngineMirrors mirrorsNew ) throws Exception {
		mirrors = mirrorsNew;
	}
	
	public void setBuilders( TransactionBase transaction , EngineBuilders buildersNew ) throws Exception {
		builders = buildersNew;
	}
	
}
