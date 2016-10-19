package org.urm.engine;

import org.urm.meta.ServerObject;

public class ServerEvents extends ServerObject {

	ServerEngine engine;

	public ServerEvents( ServerEngine engine ) {
		super( null );
		this.engine = engine;
	}

	public void init() throws Exception {
	}

	public ServerEventsApp createApp( String appId , ServerEventsListener app ) {
		return( new ServerEventsApp( this , appId ) );
	}

	public void deleteApp( ServerEventsApp app ) {
		app.deleteSubscriptions();
	}

}
