package org.urm.common.meta;

import org.urm.common.action.CommandMethodMeta;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.OptionsMeta;

public class DatabaseCommandMeta extends CommandMeta {

	public static String METHOD_INITDB = "initdb";
	public static String METHOD_GETSQL = "getsql";
	public static String METHOD_DBMANUAL = "dbmanual";
	public static String METHOD_DBAPPLY = "dbapply";
	public static String METHOD_MANAGE = "manage";
	public static String METHOD_IMPORT = "import";
	public static String METHOD_EXPORT = "export";
	
	public static String NAME = "database";
	public static String DESC = "manage databases, export/import operations, apply changes";
	
	public DatabaseCommandMeta( OptionsMeta options ) {
		super( options , NAME , DESC );
		
		String cmdOpts = "";
		cmdOpts = "OPT_SG, OPT_DBPASSWORD";
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_INITDB , false , "prepare database for operation" , cmdOpts , "<server>" ) );
		cmdOpts = "OPT_DBPASSWORD";
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_GETSQL , true , "get database release content" , cmdOpts , "{all|<deliveries>}" ) );
		cmdOpts = "OPT_SG, OPT_DBPASSWORD";
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_DBMANUAL , false , "apply manual scripts under system account" , cmdOpts , "<RELEASELABEL> <DBSERVER> {all|<indexes>}" ) );
		cmdOpts = "OPT_DBPASSWORD, OPT_DBMODE, OPT_SG, OPT_DB, OPT_DBTYPE, OPT_DBALIGNED";
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_DBAPPLY , false , "apply application scripts and load data files" , cmdOpts , "<RELEASELABEL> {all|<delivery> {all|<mask>}} (mask is distributive file mask)" ) );
		cmdOpts = "OPT_SG, OPT_DBPASSWORD, OPT_DB";
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_MANAGE , false , "manage accounting information" , cmdOpts , "<RELEASELABEL> <status|correct|rollback|drop> [{all|<indexes>}]" ) );
		cmdOpts = "OPT_SG, OPT_DBPASSWORD";
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_IMPORT , false , "import specified in etc/datapump/file dump to database" , cmdOpts , "<server> {all|meta|data} [schema]" ) );
		cmdOpts = "OPT_SG, OPT_DBPASSWORD";
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_EXPORT , false , "export specified in etc/datapump/file dump from database" , cmdOpts , "<server> {all|meta|data [schema]}" ) );
	}
	
}
