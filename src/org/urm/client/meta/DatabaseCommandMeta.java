package org.urm.client.meta;

import org.urm.common.action.CommandMethod;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;

public class DatabaseCommandMeta extends CommandMeta {

	public static String NAME = "database";
	
	public DatabaseCommandMeta( CommandBuilder builder ) {
		super( builder , NAME );
		
		String cmdOpts = "";
		cmdOpts = "GETOPT_DC, GETOPT_DBPASSWORD";
		super.defineAction( CommandMethod.newAction( "initdb" , false , "prepare database for operation" , cmdOpts , "./initdb.sh [OPTIONS] <server>" ) );
		cmdOpts = "GETOPT_DBPASSWORD";
		super.defineAction( CommandMethod.newAction( "getsql" , true , "get database release content" , cmdOpts , "./getsql.sh [OPTIONS] {all|<deliveries>}" ) );
		cmdOpts = "GETOPT_DC, GETOPT_DBPASSWORD";
		super.defineAction( CommandMethod.newAction( "dbmanual" , false , "apply manual scripts under system account" , cmdOpts , "./dbmanual.sh [OPTIONS] <RELEASELABEL> <DBSERVER> {all|<indexes>}" ) );
		cmdOpts = "GETOPT_DBPASSWORD, GETOPT_DBMODE, GETOPT_DC, GETOPT_DB, GETOPT_DBTYPE, GETOPT_DBALIGNED";
		super.defineAction( CommandMethod.newAction( "dbapply" , false , "apply application scripts and load data files" , cmdOpts , "./dbapply.sh [OPTIONS] <RELEASELABEL> {all|<delivery> {all|<mask>}} (mask is distributive file mask)" ) );
		cmdOpts = "GETOPT_DC, GETOPT_DBPASSWORD, GETOPT_DB";
		super.defineAction( CommandMethod.newAction( "manage" , false , "manage accounting information" , cmdOpts , "./manage.sh [OPTIONS] <RELEASELABEL> <status|correct|rollback|drop> [{all|<indexes>}]" ) );
		cmdOpts = "GETOPT_DC, GETOPT_DBPASSWORD";
		super.defineAction( CommandMethod.newAction( "import" , false , "import specified in etc/datapump/file dump to database" , cmdOpts , "./import.sh [OPTIONS] <server> {all|meta|data} [schema]" ) );
		cmdOpts = "GETOPT_DC, GETOPT_DBPASSWORD";
		super.defineAction( CommandMethod.newAction( "export" , false , "export specified in etc/datapump/file dump from database" , cmdOpts , "./export.sh [OPTIONS] <server> {all|meta|data [schema]}" ) );
	}
	
}
