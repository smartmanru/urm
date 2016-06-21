package org.urm.server.action.main;

import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandMethod;

public class MainMeta extends CommandMeta {

	public static String NAME = "bin";
	
	public static String MASTERFILE = "master.files.info";
	public static String RELEASEPREFIX = "release:";
	public static String PROXYPREFIX = "proxy:";
	public static String CONTEXT_FILENAME_LIXUX = "_context.sh";
	public static String CONTEXT_FILENAME_WIN = "_context.cmd";
	
	public MainMeta( CommandBuilder builder ) {
		super( builder , NAME );
		
		String cmdOpts = "OPT_HOST,OPT_PORT";
		super.defineAction( CommandMethod.newCritical( "configure-linux" , true , "configure proxy files" , cmdOpts , "./configure.sh [OPTIONS] {default|server|standalone} [envname [dcname]]" ) );
		super.defineAction( CommandMethod.newCritical( "configure-windows" , true , "configure proxy files" , cmdOpts , "configure.cmd [OPTIONS] {default|server|standalone} [envname [dcname]]" ) );
		super.defineAction( CommandMethod.newCritical( "svnsave" , true , "save master file set in svn" , cmdOpts , "svnsave [OPTIONS]" ) );
		cmdOpts = "OPT_PORT";
		super.defineAction( CommandMethod.newNormal( "server" , true , "server control" , cmdOpts , "srver [OPTIONS] {start|stop|status}" ) );
	}
	
}
