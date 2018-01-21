package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.engine.Engine;
import org.urm.meta.EngineObject;

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

	public ProjectBuilder getBuilder( String name ) throws Exception {
		ProjectBuilder builder = builderMap.get( name );
		if( builder == null )
			Common.exit1( _Error.UnknownBuilder1 , "unknown builder=" + name , name );
		return( builder );
	}

	public ProjectBuilder getBuilder( Integer id ) throws Exception {
		ProjectBuilder builder = builderMapById.get( id );
		if( builder == null )
			Common.exit1( _Error.UnknownBuilder1 , "unknown builder=" + id , "" + id );
		return( builder );
	}

	public String[] getBuilderNames() {
		return( Common.getSortedKeys( builderMap ) );
	}
	
}
