package org.urm.engine.data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.db.core.DBEnums.DBEnumResourceType;
import org.urm.engine.Engine;
import org.urm.meta.EngineObject;
import org.urm.meta.MatchItem;
import org.urm.meta.Types.EnumResourceCategory;
import org.urm.meta.engine.AuthResource;
import org.urm.meta.engine._Error;

public class EngineResources extends EngineObject {

	public Engine engine;
	
	Map<String,AuthResource> resourceMap;
	Map<Integer,AuthResource> resourceMapById;

	public EngineResources( Engine engine ) {
		super( null );
		this.engine = engine;
		
		resourceMap = new HashMap<String,AuthResource>();
		resourceMapById = new HashMap<Integer,AuthResource>();
	}

	@Override
	public String getName() {
		return( "server-resources" );
	}
	
	public EngineResources copy() throws Exception {
		EngineResources r = new EngineResources( engine );
		
		for( AuthResource res : resourceMap.values() ) {
			AuthResource rc = res.copy( r );
			r.addResource( rc );
		}
		return( r );
	}
	
	public void addResource( AuthResource rc ) {
		resourceMap.put( rc.NAME , rc );
		resourceMapById.put( rc.ID , rc );
	}

	public void updateResource( AuthResource rc ) throws Exception {
		Common.changeMapKey( resourceMap , rc , rc.NAME );
	}
	
	public void removeResource( AuthResource rc ) throws Exception {
		resourceMap.remove( rc.NAME );
		resourceMapById.remove( rc.ID );
	}
	
	public AuthResource findResource( String name ) {
		AuthResource res = resourceMap.get( name );
		return( res );
	}

	public AuthResource findResource( Integer id ) {
		AuthResource res = resourceMapById.get( id );
		return( res );
	}

	public AuthResource getResource( String name ) throws Exception {
		AuthResource res = resourceMap.get( name );
		if( res == null )
			Common.exit1( _Error.UnknownResource1 , "unknown resource=" + name , name );
		return( res );
	}

	public AuthResource getResource( Integer id ) throws Exception {
		AuthResource res = resourceMapById.get( id );
		if( res == null )
			Common.exit1( _Error.UnknownResource1 , "unknown resource=" + id , "" + id );
		return( res );
	}

	public AuthResource getResource( MatchItem item ) throws Exception {
		if( item == null )
			return( null );
		if( item.MATCHED )
			return( getResource( item.FKID ) );
		return( getResource( item.FKNAME ) );
	}
	
	public String getResourceName( MatchItem item ) throws Exception {
		if( item == null )
			return( "" );
		AuthResource res = getResource( item );
		return( res.NAME );
	}
	
	public String[] getResourceNames() {
		return( Common.getSortedKeys( resourceMap ) );
	}
	
	public String[] getResourceNames( EnumResourceCategory rcCategory ) {
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
	
	public MatchItem matchResource( String name , DBEnumResourceType rcType ) throws Exception {
		if( name == null || name.isEmpty() )
			return( null );
		
		AuthResource res = findResource( name );
		if( res == null )
			return( new MatchItem( name ) );
		if( rcType != null && rcType != res.RESOURCE_TYPE )
			return( new MatchItem( name ) );
		return( new MatchItem( res.ID ) );
	}

	public MatchItem matchResource( Integer id , String name , DBEnumResourceType rcType ) throws Exception {
		if( id == null && name.isEmpty() )
			return( null );
		AuthResource res = ( id == null )? findResource( name ) : getResource( id );
		if( res != null ) {
			if( rcType != null && rcType != res.RESOURCE_TYPE )
				return( new MatchItem( name ) );
			return( new MatchItem( res.ID ) );
		}
		return( new MatchItem( name ) );
	}
	
	public boolean matchResource( MatchItem item , DBEnumResourceType rcType ) throws Exception {
		if( item == null )
			return( true );

		AuthResource res = null;
		if( item.MATCHED ) {
			res = getResource( item.FKID );
			if( rcType != null && rcType != res.RESOURCE_TYPE ) {
				item.unmatch( res.NAME );
				return( false );
			}
			return( true );
		}
		
		res = findResource( item.FKNAME );
		if( rcType == null || rcType == res.RESOURCE_TYPE ) {
			item.match( res.ID );
			return( true );
		}
		return( false );
	}
	
}
