package org.urm.common.meta;

import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandMethodMeta;

public class MainCommandMeta extends CommandMeta {

	public static String NAME = "bin";
	public static String DESC = "URM instance administration";
	
	public static String MASTERFILE = "master.files.info";
	public static String RELEASEPREFIX = "release:";
	public static String PROXYPREFIX = "proxy:";
	public static String CONTEXT_FILENAME_LIXUX = "_context.sh";
	public static String CONTEXT_FILENAME_WIN = "_context.cmd";
	
	public MainCommandMeta() {
		super( NAME , DESC );
		
		String cmdOpts = "OPT_HOST,OPT_PORT";
		super.defineAction( CommandMethodMeta.newCritical( this , "configure" , true , "configure proxy files" , cmdOpts , "{default|server|standalone} [envname [sgname]]" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , "svnsave" , true , "save master file set in svn" , cmdOpts , "" ) );
		cmdOpts = "OPT_PORT";
		super.defineAction( CommandMethodMeta.newNormal( this , "server" , true , "server control" , cmdOpts , "{start|stop|status}" ) );
		cmdOpts = "";
		super.defineAction( CommandMethodMeta.newNormal( this , "auth" , true , "console client authorization" , cmdOpts , "{-user U -key K|-user U -password P}" ) );
		cmdOpts = "";
		super.defineAction( CommandMethodMeta.newNormal( this , "interactive" , true , "interactive session" , cmdOpts , "(internal action)" ) );
		super.defineAction( CommandMethodMeta.newNormal( this , "temporary" , true , "temporary" , cmdOpts , "(internal action)" ) );
	}
	
}
