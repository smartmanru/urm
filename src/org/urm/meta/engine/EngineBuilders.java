package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.engine.Engine;
import org.urm.meta.EngineObject;
import org.urm.meta.MatchItem;

public class EngineBuilders extends EngineObject {

	public Engine engine;

	private Map<String,ProjectBuilder> builderMap;
	private Map<Integer,ProjectBuilder> builderMapById;

	public EngineBuilders( Engine engine ) {
		super( null );
		this.engine = engine;
		
		builderMap = new HashMap<String,ProjectBuilder>();
		builderMapById = new HashMap<Integer,ProjectBuilder>();
	}

	@Override
	public String getName() {
		return( "server-builders" );
	}
	
	public EngineBuilders copy() throws Exception {
		EngineBuilders r = new EngineBuilders( engine );

		for( ProjectBuilder res : builderMap.values() ) {
			ProjectBuilder rc = res.copy( r );
			r.addBuilder( rc );
		}
		return( r );
	}

	public void addBuilder( ProjectBuilder builder ) {
		builderMap.put( builder.NAME , builder );
		builderMapById.put( builder.ID , builder );
	}
	
	public void removeBuilder( ProjectBuilder builder ) {
		builderMap.remove( builder.NAME );
		builderMapById.remove( builder.ID );
	}
	
	public void updateBuilder( ProjectBuilder builder ) throws Exception {
		Common.changeMapKey( builderMap , builder , builder.NAME );
	}
	
	public ProjectBuilder findBuilder( String name ) {
		ProjectBuilder builder = builderMap.get( name );
		return( builder );
	}

	public Integer findBuilderId( String name ) {
		if( name.isEmpty() )
			return( null );
		ProjectBuilder builder = builderMap.get( name );
		if( builder == null )
			return( null );
		return( builder.ID );
	}

	public ProjectBuilder getBuilder( String name ) throws Exception {
		ProjectBuilder builder = builderMap.get( name );
		if( builder == null )
			Common.exit1( _Error.UnknownBuilder1 , "unknown builder=" + name , name );
		return( builder );
	}

	public Integer getBuilderId( String name ) throws Exception {
		if( name.isEmpty() )
			return( null );
		ProjectBuilder builder = getBuilder( name );
		return( builder.ID );
	}

	public ProjectBuilder getBuilder( Integer id ) throws Exception {
		ProjectBuilder builder = builderMapById.get( id );
		if( builder == null )
			Common.exit1( _Error.UnknownBuilder1 , "unknown builder=" + id , "" + id );
		return( builder );
	}

	public String getBuilderName( Integer id ) throws Exception {
		if( id == null )
			return( null );
		ProjectBuilder builder = getBuilder( id );
		return( builder.NAME );
	}

	public String[] getBuilderNames() {
		return( Common.getSortedKeys( builderMap ) );
	}

	public MatchItem getBuilderMatchItem( Integer id , String name ) throws Exception {
		if( id == null && name.isEmpty() )
			return( null );
		ProjectBuilder builder = ( id == null )? findBuilder( name ) : getBuilder( id );
		MatchItem match = ( builder == null )? new MatchItem( name ) : new MatchItem( builder.ID );
		return( match );
	}

	public String getBuilderName( MatchItem item ) throws Exception {
		if( item == null )
			return( "" );
		if( item.MATCHED ) {
			ProjectBuilder builder = getBuilder( item.FKID );
			return( builder.NAME );
		}
		return( item.FKNAME );
	}
	
}
