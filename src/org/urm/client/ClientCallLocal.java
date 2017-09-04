package org.urm.client;

import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandOptions;
import org.urm.engine.Engine;

public class ClientCallLocal {

	public boolean runClient( CommandBuilder builder , CommandMeta commandInfo , CommandOptions options ) throws Exception {
		Engine server = new Engine( builder.execrc );
		server.init();
		return( server.runClientMode( options , commandInfo ) );
	}
	
}
