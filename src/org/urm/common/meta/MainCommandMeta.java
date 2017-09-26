package org.urm.common.meta;

import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandMethodMeta;
import org.urm.common.action.OptionsMeta;
import org.urm.common.action.CommandMethodMeta.ACTION_ACCESS;
import org.urm.meta.engine.EngineAuth.SecurityAction;

public class MainCommandMeta extends CommandMeta {

	public static String NAME = "bin";
	public static String DESC = "URM instance administration";
	
	public static String MASTERFILE = "master.files.info";
	public static String RELEASEPREFIX = "release:";
	public static String PROXYPREFIX = "proxy:";
	public static String CONTEXT_FILENAME_LIXUX = "_context.sh";
	public static String CONTEXT_FILENAME_WIN = "_context.cmd";
	
	public MainCommandMeta( OptionsMeta options ) {
		super( options , NAME , DESC );
		
		String cmdOpts = "OPT_HOST,OPT_PORT";
		super.defineAction( CommandMethodMeta.newCritical( this , "configure" , ACTION_ACCESS.SERVER , false , SecurityAction.ACTION_ADMIN , true , "configure proxy files" , cmdOpts , "{default|server|standalone} [envname [sgname]]" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , "svnsave" , ACTION_ACCESS.SERVER , false , SecurityAction.ACTION_ADMIN , true , "save master file set in svn" , cmdOpts , "" ) );
		cmdOpts = "OPT_PORT";
		super.defineAction( CommandMethodMeta.newNormal( this , "server" , ACTION_ACCESS.SERVER , false , SecurityAction.ACTION_ADMIN , true , "server control" , cmdOpts , "{start|stop|status}" ) );
		cmdOpts = "";
		super.defineAction( CommandMethodMeta.newNormal( this , "auth" , ACTION_ACCESS.SERVER , true , SecurityAction.ACTION_EXECUTE , true , "console client authorization" , cmdOpts , "{-user U -key K|-user U -password P}" ) );
		cmdOpts = "";
		super.defineAction( CommandMethodMeta.newNormal( this , "interactive" , ACTION_ACCESS.SERVER , true , SecurityAction.ACTION_EXECUTE , true , "interactive session" , cmdOpts , "(internal action)" ) );
		super.defineAction( CommandMethodMeta.newNormal( this , "temporary" , ACTION_ACCESS.SERVER , true , SecurityAction.ACTION_EXECUTE , true , "temporary" , cmdOpts , "(internal action)" ) );
	}
	
}
