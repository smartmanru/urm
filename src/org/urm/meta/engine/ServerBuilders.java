package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.ServerEngine;
import org.urm.engine.ServerObject;
import org.urm.engine.ServerTransaction;
import org.urm.engine._Error;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerBuilders extends ServerObject {

	public ServerRegistry registry;
	public ServerEngine engine;

	Map<String,ServerProjectBuilder> builderMap;

	public ServerBuilders( ServerRegistry registry ) {
		super( registry );
		this.registry = registry;
		this.engine = registry.loader.engine;
		
		builderMap = new HashMap<String,ServerProjectBuilder>();
	}

	public ServerBuilders copy() throws Exception {
		ServerBuilders r = new ServerBuilders( registry );
		
		for( ServerProjectBuilder res : builderMap.values() ) {
			ServerProjectBuilder rc = res.copy( r );
			r.builderMap.put( rc.NAME , rc );
		}
		return( r );
	}
	
	public void load( Node root ) throws Exception {
		if( root == null )
			return;
		
		Node[] list = ConfReader.xmlGetChildren( root , "builder" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			ServerProjectBuilder builder = new ServerProjectBuilder( this );
			builder.load( node );

			builderMap.put( builder.NAME , builder );
		}
	}

	public void save( Document doc , Element root ) throws Exception {
		for( ServerProjectBuilder res : builderMap.values() ) {
			Element resElement = Common.xmlCreateElement( doc , root , "resource" );
			res.save( doc , resElement );
		}
	}

	public ServerProjectBuilder findBuilder( String name ) {
		ServerProjectBuilder builder = builderMap.get( name );
		return( builder );
	}

	public ServerProjectBuilder getBuilder( String name ) throws Exception {
		ServerProjectBuilder builder = builderMap.get( name );
		if( builder == null )
			Common.exit1( _Error.UnknownBuilder1 , "unknown builder=" + name , name );
		return( builder );
	}

	public String[] getList() {
		return( Common.getSortedKeys( builderMap ) );
	}
	
	public void createBuilder( ServerTransaction transaction , ServerProjectBuilder builder ) throws Exception {
		if( builderMap.get( builder.NAME ) != null )
			transaction.exit( _Error.BuilderAlreadyExists1 , "builder already exists name=" + builder.NAME , new String[] { builder.NAME } );
			
		builder.createBuilder();
		builderMap.put( builder.NAME , builder );
	}
	
	public void deleteBuilder( ServerTransaction transaction , ServerProjectBuilder builder ) throws Exception {
		if( builderMap.get( builder.NAME ) == null )
			transaction.exit( _Error.UnknownBuilder1 , "unknown builder name=" + builder.NAME , new String[] { builder.NAME } );
			
		builderMap.remove( builder.NAME );
	}

}
