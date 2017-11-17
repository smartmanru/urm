package org.urm.meta.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.EngineTransaction;
import org.urm.meta.EngineObject;
import org.urm.meta.Types.EnumResourceCategory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineResources extends EngineObject {

	public EngineRegistry registry;
	
	Map<String,AuthResource> resourceMap;

	public EngineResources( EngineRegistry registry ) {
		super( registry );
		this.registry = registry;
		
		resourceMap = new HashMap<String,AuthResource>();
	}

	@Override
	public String getName() {
		return( "server-resources" );
	}
	
	public EngineResources copy() throws Exception {
		EngineResources r = new EngineResources( registry );
		
		for( AuthResource res : resourceMap.values() ) {
			AuthResource rc = res.copy( r );
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
			AuthResource res = new AuthResource( this );
			res.load( node );

			resourceMap.put( res.NAME , res );
		}
	}

	public void save( Document doc , Element root ) throws Exception {
		for( AuthResource res : resourceMap.values() ) {
			Element resElement = Common.xmlCreateElement( doc , root , "resource" );
			res.save( doc , resElement );
		}
	}

	public AuthResource findResource( String name ) {
		AuthResource res = resourceMap.get( name );
		return( res );
	}

	public AuthResource getResource( String name ) throws Exception {
		AuthResource res = resourceMap.get( name );
		if( res == null )
			Common.exit1( _Error.UnknownResource1 , "unknown resource=" + name , name );
		return( res );
	}

	public String[] getList() {
		return( Common.getSortedKeys( resourceMap ) );
	}
	
	public String[] getList( EnumResourceCategory rcCategory ) {
		List<String> list = new LinkedList<String>();
		for( AuthResource res : resourceMap.values() ) {
			if( rcCategory == EnumResourceCategory.ANY )
				list.add( res.NAME );
			else
			if( rcCategory == EnumResourceCategory.SSH && res.isSshKey() )
				list.add( res.NAME );
			else
			if( rcCategory == EnumResourceCategory.CREDENTIALS && res.isCredentials() )
				list.add( res.NAME );
			else
			if( ( rcCategory == EnumResourceCategory.SOURCE || rcCategory == EnumResourceCategory.NEXUS ) && res.isNexus() )
				list.add( res.NAME );
			else
			if( ( rcCategory == EnumResourceCategory.SOURCE || rcCategory == EnumResourceCategory.VCS ) && res.isVCS() )
				list.add( res.NAME );
		}
		return( Common.getSortedList( list ) );
	}
	
	public void createResource( EngineTransaction transaction , AuthResource res ) throws Exception {
		if( resourceMap.get( res.NAME ) != null )
			transaction.exit( _Error.DuplicateResource1 , "resource already exists name=" + res.NAME , new String[] { res.NAME } );
			
		res.createResource();
		resourceMap.put( res.NAME , res );
	}
	
	public void updateResource( EngineTransaction transaction , AuthResource res , AuthResource resNew ) throws Exception {
		res.updateResource( transaction , resNew );
		dropResourceMirrors( transaction , res );
	}
	
	public void deleteResource( EngineTransaction transaction , AuthResource res ) throws Exception {
		if( resourceMap.get( res.NAME ) == null )
			transaction.exit( _Error.UnknownResource1 , "unknown resource name=" + res.NAME , new String[] { res.NAME } );
			
		dropResourceMirrors( transaction , res );
		resourceMap.remove( res.NAME );
	}

	public void dropResourceMirrors( EngineTransaction transaction , AuthResource res ) throws Exception {
		if( !res.isVCS() )
			return;
		
		ActionBase action = transaction.getAction();
		EngineMirrors mirrors = action.getServerMirrors();
		mirrors.dropResourceMirrors( transaction , res );
	}
	
}
