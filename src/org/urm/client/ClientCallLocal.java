package org.urm.client;

import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;
import org.urm.server.ServerEngine;

public class ClientCallLocal {

	public boolean runClient( CommandBuilder builder , CommandMeta commandInfo ) throws Exception {
		ServerEngine server = new ServerEngine();
		return( server.runClientMode( builder.options , builder.rc , commandInfo ) );
	}
	
}
