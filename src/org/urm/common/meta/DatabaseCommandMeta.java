package org.urm.common.meta;

import org.urm.common.action.CommandMethodMeta;
import org.urm.common.action.CommandMeta;

public class DatabaseCommandMeta extends CommandMeta {

	public static String NAME = "database";
	public static String DESC = "manage databases, export/import operations, apply changes";
	
	public DatabaseCommandMeta() {
		super( NAME , DESC );
		
		String cmdOpts = "";
		cmdOpts = "OPT_DC, OPT_DBPASSWORD";
		super.defineAction( CommandMethodMeta.newCritical( this , "initdb" , false , "prepare database for operation" , cmdOpts , "./initdb.sh [OPTIONS] <server>" ) );
		cmdOpts = "OPT_DBPASSWORD";
		super.defineAction( CommandMethodMeta.newNormal( this , "getsql" , true , "get database release content" , cmdOpts , "./getsql.sh [OPTIONS] {all|<deliveries>}" ) );
		cmdOpts = "OPT_DC, OPT_DBPASSWORD";
		super.defineAction( CommandMethodMeta.newCritical( this , "dbmanual" , false , "apply manual scripts under system account" , cmdOpts , "./dbmanual.sh [OPTIONS] <RELEASELABEL> <DBSERVER> {all|<indexes>}" ) );
		cmdOpts = "OPT_DBPASSWORD, OPT_DBMODE, OPT_DC, OPT_DB, OPT_DBTYPE, OPT_DBALIGNED";
		super.defineAction( CommandMethodMeta.newCritical( this , "dbapply" , false , "apply application scripts and load data files" , cmdOpts , "./dbapply.sh [OPTIONS] <RELEASELABEL> {all|<delivery> {all|<mask>}} (mask is distributive file mask)" ) );
		cmdOpts = "OPT_DC, OPT_DBPASSWORD, OPT_DB";
		super.defineAction( CommandMethodMeta.newNormal( this , "manage" , false , "manage accounting information" , cmdOpts , "./manage.sh [OPTIONS] <RELEASELABEL> <status|correct|rollback|drop> [{all|<indexes>}]" ) );
		cmdOpts = "OPT_DC, OPT_DBPASSWORD";
		super.defineAction( CommandMethodMeta.newCritical( this , "import" , false , "import specified in etc/datapump/file dump to database" , cmdOpts , "./import.sh [OPTIONS] <server> {all|meta|data} [schema]" ) );
		cmdOpts = "OPT_DC, OPT_DBPASSWORD";
		super.defineAction( CommandMethodMeta.newNormal( this , "export" , false , "export specified in etc/datapump/file dump from database" , cmdOpts , "./export.sh [OPTIONS] <server> {all|meta|data [schema]}" ) );
	}
	
}
