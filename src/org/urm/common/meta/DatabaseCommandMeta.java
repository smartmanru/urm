package org.urm.common.meta;

import org.urm.common.action.CommandMethodMeta;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.OptionsMeta;
import org.urm.common.action.CommandMethodMeta.ACTION_ACCESS;
import org.urm.common.action.CommandMethodMeta.SecurityAction;

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
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_INITDB , ACTION_ACCESS.ENV , false , SecurityAction.ACTION_DEPLOY , false , "prepare database for operation" , cmdOpts , "<server>" ) );
		cmdOpts = "OPT_DBPASSWORD";
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_GETSQL , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_CODEBASE , true , "get database release content" , cmdOpts , "{all|<deliveries>}" ) );
		cmdOpts = "OPT_SG, OPT_DBPASSWORD";
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_DBMANUAL , ACTION_ACCESS.ENV , false , SecurityAction.ACTION_DEPLOY , false , "apply manual scripts under system account" , cmdOpts , "<RELEASELABEL> <DBSERVER> {all|<indexes>}" ) );
		cmdOpts = "OPT_DBPASSWORD, OPT_DBMODE, OPT_SG, OPT_DB, OPT_DBTYPE, OPT_DBALIGNED";
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_DBAPPLY , ACTION_ACCESS.ENV , false , SecurityAction.ACTION_DEPLOY , false , "apply application scripts and load data files" , cmdOpts , "<RELEASELABEL> {all|<delivery> {all|<mask>}} (mask is distributive file mask)" ) );
		cmdOpts = "OPT_SG, OPT_DBPASSWORD, OPT_DB";
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_MANAGE , ACTION_ACCESS.ENV , false , SecurityAction.ACTION_DEPLOY , false , "manage accounting information" , cmdOpts , "<RELEASELABEL> <status|correct|rollback|drop> [{all|<indexes>}]" ) );
		cmdOpts = "OPT_SG, OPT_DBPASSWORD";
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_IMPORT , ACTION_ACCESS.ENV , false , SecurityAction.ACTION_DEPLOY , false , "import specified in etc/datapump/file dump to database" , cmdOpts , "<server> {all|meta|data} [schema]" ) );
		cmdOpts = "OPT_SG, OPT_DBPASSWORD";
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_EXPORT , ACTION_ACCESS.ENV , true , SecurityAction.ACTION_DEPLOY , false , "export specified in etc/datapump/file dump from database" , cmdOpts , "<server> {all|meta|data [schema]}" ) );
	}
	
}
