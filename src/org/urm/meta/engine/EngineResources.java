package org.urm.meta.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.Engine;
import org.urm.engine.EngineTransaction;
import org.urm.meta.EngineObject;
import org.urm.meta.Types.VarRESOURCECATEGORY;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineResources extends EngineObject {

	public EngineRegistry registry;
	public Engine engine;

	Map<String,EngineAuthResource> resourceMap;

	public EngineResources( EngineRegistry registry ) {
		super( registry );
		this.registry = registry;
		this.engine = registry.loader.engine;
		
		resourceMap = new HashMap<String,EngineAuthResource>();
	}

	@Override
	public String getName() {
		return( "server-resources" );
	}
	
	public EngineResources copy() throws Exception {
		EngineResources r = new EngineResources( registry );
		
		for( EngineAuthResource res : resourceMap.values() ) {
			EngineAuthResource rc = res.copy( r );
			r.resourceMap.put( rc.NAME , rc );
		}
		return( r );
	}
	
	public void load( Node root ) throws Exception {
		if( root == null )
			return;
		
		Node[] list = ConfReader.xmlGetChildren( root , "resource" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			EngineAuthResource res = new EngineAuthResource( this );
			res.load( node );

			resourceMap.put( res.NAME , res );
		}
	}

	public void save( Document doc , Element root ) throws Exception {
		for( EngineAuthResource res : resourceMap.values() ) {
			Element resElement = Common.xmlCreateElement( doc , root , "resource" );
			res.save( doc , resElement );
		}
	}

	public EngineAuthResource findResource( String name ) {
		EngineAuthResource res = resourceMap.get( name );
		return( res );
	}

	public EngineAuthResource getResource( String name ) throws Exception {
		EngineAuthResource res = resourceMap.get( name );
		if( res == null )
			Common.exit1( _Error.UnknownResource1 , "unknown resource=" + name , name );
		return( res );
	}

	public String[] getList() {
		return( Common.getSortedKeys( resourceMap ) );
	}
	
	public String[] getList( VarRESOURCECATEGORY rcCategory ) {
		List<String> list = new LinkedList<String>();
		for( EngineAuthResource res : resourceMap.values() ) {
			if( rcCategory == VarRESOURCECATEGORY.ANY )
				list.add( res.NAME );
			else
			if( rcCategory == VarRESOURCECATEGORY.SSH && res.isSshKey() )
				list.add( res.NAME );
			else
			if( rcCategory == VarRESOURCECATEGORY.CREDENTIALS && res.isCredentials() )
				list.add( res.NAME );
			else
			if( ( rcCategory == VarRESOURCECATEGORY.SOURCE || rcCategory == VarRESOURCECATEGORY.NEXUS ) && res.isNexus() )
				list.add( res.NAME );
			else
			if( ( rcCategory == VarRESOURCECATEGORY.SOURCE || rcCategory == VarRESOURCECATEGORY.VCS ) && res.isVCS() )
				list.add( res.NAME );
		}
		return( Common.getSortedList( list ) );
	}
	
	public void createResource( EngineTransaction transaction , EngineAuthResource res ) throws Exception {
		if( resourceMap.get( res.NAME ) != null )
			transaction.exit( _Error.DuplicateResource1 , "resource already exists name=" + res.NAME , new String[] { res.NAME } );
			
		res.createResource();
		resourceMap.put( res.NAME , res );
	}
	
	public void updateResource( EngineTransaction transaction , EngineAuthResource res , EngineAuthResource resNew ) throws Exception {
		res.updateResource( transaction , resNew );
		dropResourceMirrors( transaction , res );
	}
	
	public void deleteResource( EngineTransaction transaction , EngineAuthResource res ) throws Exception {
		if( resourceMap.get( res.NAME ) == null )
			transaction.exit( _Error.UnknownResource1 , "unknown resource name=" + res.NAME , new String[] { res.NAME } );
			
		dropResourceMirrors( transaction , res );
		resourceMap.remove( res.NAME );
	}

	public void dropResourceMirrors( EngineTransaction transaction , EngineAuthResource res ) throws Exception {
		if( !res.isVCS() )
			return;
		
		ActionBase action = transaction.getAction();
		EngineMirrors mirrors = action.getServerMirrors();
		mirrors.dropResourceMirrors( transaction , res );
	}
	
}