package org.urm.common.meta;

import org.urm.common.action.CommandMeta;

public class MonitorCommandMeta extends CommandMeta {

	public static String NAME = "monitor";
	public static String DESC = "setup and run monitoring of environments";
	
	public MonitorCommandMeta() {
		super( NAME , DESC );
	}
	
}
