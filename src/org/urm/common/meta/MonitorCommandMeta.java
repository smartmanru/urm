package org.urm.common.meta;

import org.urm.common.action.CommandMeta;
import org.urm.common.action.OptionsMeta;

public class MonitorCommandMeta extends CommandMeta {

	public static String NAME = "monitor";
	public static String DESC = "setup and run monitoring of environments";
	
	public MonitorCommandMeta( OptionsMeta options ) {
		super( options , NAME , DESC );
	}
	
}
