package org.urm.client;

import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandOptions;
import org.urm.engine.ServerEngine;

public class ClientCallLocal {

	public boolean runClient( CommandBuilder builder , CommandMeta commandInfo , CommandOptions options ) throws Exception {
		ServerEngine server = new ServerEngine( builder.execrc );
		server.init();
		return( server.runClientMode( options , commandInfo ) );
	}
	
}
