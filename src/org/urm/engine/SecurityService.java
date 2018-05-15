package org.urm.engine;

import org.urm.meta.loader.EngineObject;

public class SecurityService extends EngineObject {

	Engine engine;
	
	public SecurityService( Engine engine ) {
		super( null );
		this.engine = engine;
	}
	
	@Override
	public String getName() {
		return( "server-security" );
	}

	public void createContainer( String name , String password ) throws Exception {
	}
	
}
