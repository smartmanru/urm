package org.urm.client.meta;

import org.urm.common.action.CommandMethod;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;

public class MonitorCommandMeta extends CommandMeta {

	public static String NAME = "monitor";
	
	public MonitorCommandMeta( CommandBuilder builder ) {
		super( builder , NAME );
		
		String cmdOpts = "";
		super.defineAction( CommandMethod.newAction( "start" , true , "start monitor server" , cmdOpts , "./start.sh [OPTIONS]" ) );
	}
	
}
