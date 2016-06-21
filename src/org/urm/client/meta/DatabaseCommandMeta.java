package org.urm.client.meta;

import org.urm.common.action.CommandMethod;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;

public class DatabaseCommandMeta extends CommandMeta {

	public static String NAME = "database";
	
	public DatabaseCommandMeta( CommandBuilder builder ) {
		super( builder , NAME );
		
		String cmdOpts = "";
		cmdOpts = "OPT_DC, OPT_DBPASSWORD";
		super.defineAction( CommandMethod.newCritical( "initdb" , false , "prepare database for operation" , cmdOpts , "./initdb.sh [OPTIONS] <server>" ) );
		cmdOpts = "OPT_DBPASSWORD";
		super.defineAction( CommandMethod.newNormal( "getsql" , true , "get database release content" , cmdOpts , "./getsql.sh [OPTIONS] {all|<deliveries>}" ) );
		cmdOpts = "OPT_DC, OPT_DBPASSWORD";
		super.defineAction( CommandMethod.newCritical( "dbmanual" , false , "apply manual scripts under system account" , cmdOpts , "./dbmanual.sh [OPTIONS] <RELEASELABEL> <DBSERVER> {all|<indexes>}" ) );
		cmdOpts = "OPT_DBPASSWORD, OPT_DBMODE, OPT_DC, OPT_DB, OPT_DBTYPE, OPT_DBALIGNED";
		super.defineAction( CommandMethod.newCritical( "dbapply" , false , "apply application scripts and load data files" , cmdOpts , "./dbapply.sh [OPTIONS] <RELEASELABEL> {all|<delivery> {all|<mask>}} (mask is distributive file mask)" ) );
		cmdOpts = "OPT_DC, OPT_DBPASSWORD, OPT_DB";
		super.defineAction( CommandMethod.newNormal( "manage" , false , "manage accounting information" , cmdOpts , "./manage.sh [OPTIONS] <RELEASELABEL> <status|correct|rollback|drop> [{all|<indexes>}]" ) );
		cmdOpts = "OPT_DC, OPT_DBPASSWORD";
		super.defineAction( CommandMethod.newCritical( "import" , false , "import specified in etc/datapump/file dump to database" , cmdOpts , "./import.sh [OPTIONS] <server> {all|meta|data} [schema]" ) );
		cmdOpts = "OPT_DC, OPT_DBPASSWORD";
		super.defineAction( CommandMethod.newNormal( "export" , false , "export specified in etc/datapump/file dump from database" , cmdOpts , "./export.sh [OPTIONS] <server> {all|meta|data [schema]}" ) );
	}
	
}
