package org.urm.common.meta;

import org.urm.common.action.CommandMethod;
import org.urm.common.action.CommandMeta;

public class MonitorCommandMeta extends CommandMeta {

	public static String NAME = "monitor";
	public static String DESC = "setup and run monitoring of environments";
	
	public MonitorCommandMeta() {
		super( NAME , DESC );
		
		String cmdOpts = "";
		super.defineAction( CommandMethod.newNormal( "start" , true , "start monitor server" , cmdOpts , "./start.sh [OPTIONS]" ) );
	}
	
}
