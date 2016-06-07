package org.urm.server;

import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandMethod;

public class MainMeta extends CommandMeta {

	public static String NAME = "bin";
	
	public MainMeta( CommandBuilder builder ) {
		super( builder , NAME );
		
		String cmdOpts = "";
		super.defineAction( CommandMethod.newAction( "configure-linux" , true , "configure proxy files" , cmdOpts , "./configure.sh [OPTIONS] {linux|windows} {all|build|env} [envname [dcname]]" ) );
		super.defineAction( CommandMethod.newAction( "configure-windows" , true , "configure proxy files" , cmdOpts , "configure.cmd [OPTIONS] {linux|windows} {all|build|env} [envname [dcname]]" ) );
		super.defineAction( CommandMethod.newAction( "svnsave" , true , "save master file set in svn" , cmdOpts , "svnsave [OPTIONS]" ) );
	}
	
}
