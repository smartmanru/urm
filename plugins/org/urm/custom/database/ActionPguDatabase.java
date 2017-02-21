package org.urm.custom.database;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.RunErrorClass;
import org.urm.engine.custom.CommandCustom;
import org.urm.engine.custom.ICustomDatabase;
import org.urm.engine.storage.FileSet;
import org.urm.engine.storage.LocalFolder;

public class ActionPguDatabase implements ICustomDatabase {

	boolean S_DIC_CONTENT;
	boolean S_SVC_CONTENT;
	boolean S_SMEVATTR_CONTENT;
	boolean S_CHECK_FAILED = false;
	public String PUBLISHERS;

	public static int ErrorBase = RunErrorClass.BasePlugin;
	public static final int ScriptSetErrors0 = ErrorBase + 1;
	public static final int ReleaseDatabaseFileSetCheckFailed0 = ErrorBase + 2;
	public static final int MissingOrganizationalMappingFile1 = ErrorBase + 3;
	
	public ActionPguDatabase() {
		S_DIC_CONTENT = false;
		S_SVC_CONTENT = false;
		S_SMEVATTR_CONTENT = false;
	}
	
	public String getGroupName( ActionBase action , CommandCustom custom , String groupFolder ) throws Exception {
		return( null );
	}
	
	public void checkSourceFile( ActionBase action , CommandCustom custom , String groupName , LocalFolder folder , String filePath ) throws Exception {
		action.exitNotImplemented();
	}
	
	public void addSourceFile( ActionBase action , CommandCustom custom , String groupName , LocalFolder folder , String filePath ) throws Exception {
		action.exitNotImplemented();
	}
	
	public void startSourceGroup( ActionBase action , CommandCustom custom , String groupName ) throws Exception {
		action.exitNotImplemented();
	}
	
	public void finishSourceGroup( ActionBase action , CommandCustom custom , String groupName ) throws Exception {
		action.exitNotImplemented();
	}
	
	protected void copyServices( ActionBase action , FileSet P_ALIGNEDNAME , String P_ALIGNEDID , LocalFolder P_TARGETDIR ) throws Exception {
		LocalFolder scriptDir = P_TARGETDIR.getSubFolder( action , "scripts" );
		
		for( FileSet name : P_ALIGNEDNAME.dirs.values() ) {
//			if( name.dirName.equals( "coresvc" ) )
//				copyDir( action , P_ALIGNEDNAME , P_ALIGNEDID , P_ALIGNEDNAME.getDirByPath( action , "coresvc" ) , scriptDir );
			if( name.dirName.startsWith( "war." ) )
				copyOneWar( action , P_ALIGNEDNAME , P_ALIGNEDID , scriptDir , name );
			if( name.dirName.startsWith( "forms." ) )
				copyOneForms( action , P_ALIGNEDNAME , P_ALIGNEDID , scriptDir , name );
		}
			
		if( S_CHECK_FAILED ) {
			if( action.isForced() )
				action.error( "prepare: errors in script set. Ignored." );
			else
				action.exit0( ScriptSetErrors0 , "prepare: errors in script set" );
		}
	}

	private void copyOneForms( ActionBase action , FileSet P_ALIGNEDNAME , String P_ALIGNEDID , LocalFolder P_TARGETDIR , FileSet P_ORGNAME ) throws Exception {
		action.info( "process forms regional folder: " + P_ORGNAME.dirName + " ..." );
//		copyDir( action , P_ALIGNEDNAME , P_ALIGNEDID , P_ORGNAME.getDirByPath( action , "svcdic" ) , P_TARGETDIR.getSubFolder( action , "svcrun" ) );
//		copyDir( action , P_ALIGNEDNAME , P_ALIGNEDID , P_ORGNAME.getDirByPath( action , "svcspec" ) , P_TARGETDIR.getSubFolder( action , "svcrun" ) );
//		copyDir( action , P_ALIGNEDNAME , P_ALIGNEDID , P_ORGNAME.getDirByPath( action , "svcform" ) , P_TARGETDIR.getSubFolder( action , "svcrun" ) );
		copyUddi( action , P_ALIGNEDNAME , P_ALIGNEDID , P_TARGETDIR , P_ORGNAME );

		// copy publisher part
		FileSet svcpub = P_ORGNAME.getDirByPath( action , "svcpub" ); 
		if( svcpub != null )
			copyZip( action , P_ALIGNEDNAME , P_ALIGNEDID , svcpub , P_TARGETDIR.getSubFolder( action , "svcpub" ) );
	}

	private void copyOneWar( ActionBase action , FileSet P_ALIGNEDNAME , String P_ALIGNEDID , LocalFolder P_TARGETDIR , FileSet P_MPNAME ) throws Exception {
		action.info( "process war regional folder: " + P_MPNAME.dirName + " ..." );
//		copyDir( action , P_ALIGNEDNAME , P_ALIGNEDID , P_MPNAME.getDirByPath( action , "svcdic" ) , P_TARGETDIR.getSubFolder( action , "svcrun" ) );
//		copyDir( action , P_ALIGNEDNAME , P_ALIGNEDID , P_MPNAME.getDirByPath( action , "svcspec" ) , P_TARGETDIR.getSubFolder( action , "svcrun" ) );
		copyUddi( action , P_ALIGNEDNAME , P_ALIGNEDID , P_TARGETDIR , P_MPNAME );
	}

	private void copyUddi( ActionBase action , FileSet P_ALIGNEDNAME , String P_ALIGNEDID , LocalFolder P_TARGETDIR , FileSet P_UDDIDIR ) throws Exception {
//		MetaDatabaseSchema schema = database.getSchema( action , "juddi" ); 
//		String F_UDDINUM = database.getSqlIndexPrefix( action , P_UDDIDIR.dirName + ".juddi" , P_ALIGNEDID );
		String F_UDDINUM = "";

		// regional tail
		String F_REGIONALINDEX = "";
		if( P_ALIGNEDNAME.equals( "regional" ) )
			F_REGIONALINDEX = "RR";

		// process UDDI
		String SRC_DICFILE_EP = "svcrun/uddidic." + F_UDDINUM + F_REGIONALINDEX + ".ep.txt";
		String SRC_SVCFILE_EP = "svcrun/uddisvc." + F_UDDINUM + F_REGIONALINDEX + ".ep.txt";
		String SRC_SMEVATTRFILE = "svcrun/uddisvc." + F_UDDINUM + F_REGIONALINDEX + ".smevattr.txt";

		FileSet svcdic = P_UDDIDIR.getDirByPath( action , "svcdic" );
		if( svcdic != null ) {
			P_TARGETDIR.ensureFolderExists( action , "svcrun" );
			action.info( P_UDDIDIR + "/svcdic ..." );
//			if( svcdic.files.containsKey( "extdicuddi.txt" ) )
//				schema.specific.grepComments( action , "UDDI" , srcFolder , Common.getPath( svcdic.dirPath , "extdicuddi.txt" ) , P_TARGETDIR , SRC_DICFILE_EP );
		}

		FileSet svcspec = P_UDDIDIR.getDirByPath( action , "svcspec" );
		if( svcspec != null && svcspec.hasFilesEndingWith( ".sql" ) ) {
			P_TARGETDIR.ensureFolderExists( action , "svcrun" );
			action.debug( "process uddi " + svcspec.dirPath + " ..." );

			// empty resulting files
			P_TARGETDIR.removeFiles( action , SRC_SVCFILE_EP );
			P_TARGETDIR.removeFiles( action , SRC_SMEVATTRFILE );

			for( String script : Common.getSortedKeys( svcspec.files ) ) {
				if( !script.endsWith( ".sql" ) )
					continue;
					
				// extract required smev attributes
//				schema.specific.grepComments( action , "SMEVATTR" , srcFolder , Common.getPath( svcspec.dirPath , script ) , P_TARGETDIR , SRC_SMEVATTRFILE );
//				schema.specific.grepComments( action , "UDDI" , srcFolder , Common.getPath( svcspec.dirPath , script ) , P_TARGETDIR , SRC_SVCFILE_EP );
			}
		}

		if( !checkUddi( action , P_TARGETDIR , SRC_DICFILE_EP , SRC_SVCFILE_EP , SRC_SMEVATTRFILE ) ) {
			action.debug( "prepare: no UDDI content" );
			return;
		}

		String DST_FNAME_UAT = "svcrun/uatonly/" + F_UDDINUM + "000" + F_REGIONALINDEX + "-juddi-uat.sql";
		String DST_FNAME_PROD = "svcrun/prodonly/" + F_UDDINUM + "000" + F_REGIONALINDEX + "-juddi-prod.sql";

		// process content
		P_TARGETDIR.ensureFolderExists( action , Common.getDirName( DST_FNAME_UAT ) );
		P_TARGETDIR.ensureFolderExists( action , Common.getDirName( DST_FNAME_PROD ) );

//		schema.specific.addComment( action , "UAT UDDI setup script" , P_TARGETDIR , DST_FNAME_UAT );
//		schema.specific.addComment( action , "PROD UDDI setup script" , P_TARGETDIR , DST_FNAME_PROD );

		// process endpoints
		if( S_DIC_CONTENT || S_SVC_CONTENT )
			processUddiEndpoints( action , F_UDDINUM , P_TARGETDIR , DST_FNAME_UAT , DST_FNAME_PROD , SRC_DICFILE_EP , SRC_SVCFILE_EP );

		// process smev attrs
		if( S_SMEVATTR_CONTENT )
			processUddiSmevAttrs( action , F_UDDINUM , P_TARGETDIR , DST_FNAME_UAT , DST_FNAME_PROD , SRC_SMEVATTRFILE );
	}
			
	private void copyZip( ActionBase action , FileSet P_ALIGNEDNAME , String P_ALIGNEDID , FileSet P_DIRFROM , LocalFolder P_DIRTO ) throws Exception {
		action.info( "prepare/copy " + P_DIRFROM.dirPath + " ..." );

		P_DIRTO.ensureExists( action );

		// regional tail
//		String F_REGIONALINDEX = "";
//		if( P_ALIGNEDNAME.dirName.equals( "regional" ) )
//			F_REGIONALINDEX = "RR";

		// add registration index
		for( String x : Common.getSortedKeys( P_DIRFROM.files ) ) {
			if( !x.endsWith( ".zip" ) )
				continue;
			
//			String F_SCRIPTNUM = Common.getPartBeforeFirst( x , "-" );
//			
//			// get filename without index
//			String F_FILEBASE = Common.getPartAfterFirst( x , "-" );
			
			// rename - by index
//			srcFolder.copyFile( action , P_DIRFROM.dirPath , x , P_DIRTO , "18" + P_ALIGNEDID + F_SCRIPTNUM + F_REGIONALINDEX + "-" + F_FILEBASE );
		}
	}

	private boolean checkUddi( ActionBase action , LocalFolder P_TARGETDIR , String P_DICFILE_EP , String P_SVCFILE_EP , String P_SMEVATTRFILE ) throws Exception {
		// check files have content
		S_DIC_CONTENT = false;
		S_SVC_CONTENT = false;
		S_SMEVATTR_CONTENT = false;

		boolean CHECK_CONTENT = false;
		if( P_TARGETDIR.checkFileExists( action , P_DICFILE_EP ) ) {
			if( !P_TARGETDIR.isFileEmpty( action , P_DICFILE_EP ) ) {
				S_DIC_CONTENT = true;
				CHECK_CONTENT = true;
			}
		}

		if( P_TARGETDIR.checkFileExists( action , P_SVCFILE_EP ) ) {
			if( !P_TARGETDIR.isFileEmpty( action , P_SVCFILE_EP ) ) {
				S_SVC_CONTENT = true;
				CHECK_CONTENT = true;
			}
		}

		if( P_TARGETDIR.checkFileExists( action , P_SMEVATTRFILE ) ) {
			if( !P_TARGETDIR.isFileEmpty( action , P_SMEVATTRFILE ) ) {
				S_SMEVATTR_CONTENT = true;
				CHECK_CONTENT = true;
			}
		}

		if( !CHECK_CONTENT )
			return( false );

		return( true );
	}

	protected void check( ActionBase action , FileSet P_ALIGNEDNAME , String P_ALIGNEDID ) throws Exception {
		S_CHECK_FAILED = false;

		// check folders
		for( String dir : Common.getSortedKeys( P_ALIGNEDNAME.dirs ) ) {
			if( dir.startsWith( "war." ) )
				checkOneWar( action , P_ALIGNEDNAME , P_ALIGNEDID , dir );
			else
			if( dir.startsWith( "forms." ) )
				checkOneForms( action , P_ALIGNEDNAME , P_ALIGNEDID , dir );
			else
			if( dir.equals( "manual" ) )
				continue;
			else {
				action.error( "prepare: aligned=" + P_ALIGNEDNAME + " - invalid release folder: " + dir );
//				moveErrors( action , P_ALIGNEDNAME , P_ALIGNEDID , dir , "invalid release folder" );
				S_CHECK_FAILED = true;
			}
		}

		if( S_CHECK_FAILED )
			action.ifexit( ReleaseDatabaseFileSetCheckFailed0 , "release database file set check failed" , null );
	}

	private void checkOneForms( ActionBase action , FileSet P_ALIGNEDNAME , String P_ALIGNEDID , String P_ORGNAME ) throws Exception {
		String[] items = Common.splitDotted( P_ORGNAME );
		String F_REGION = items[1];
		String F_ORGID = items[2];

		boolean F_DIR_FAILED = false;
		if( F_REGION.isEmpty() || F_ORGID.isEmpty() ) {
			action.error( "prepare: invalid regional forms folder name=" + P_ORGNAME + ", expected format is forms.regnum.orgcode" );
			F_DIR_FAILED = true;
		}

		// check region is NN
		if( !F_REGION.matches( "[0-9][0-9]" ) ) {
			action.error( "prepare: invalid regional folder name=" + P_ORGNAME + ", region=" + F_REGION + ", expected NN" );
			F_DIR_FAILED = true;
		}

		if( F_DIR_FAILED ) {
			S_CHECK_FAILED = true;
//			moveErrors( action , P_ALIGNEDNAME , P_ALIGNEDID , P_ORGNAME , "invalid regional forms folder name=" + P_ORGNAME + ", expected format is forms.regnum.orgcode" );
		}

		// check ORGID
//		if( !database.checkOrgInfo( action , F_ORGID ) ) {
//			S_CHECK_FAILED = true;
//			moveErrors( action , P_ALIGNEDNAME , P_ALIGNEDID , P_ORGNAME , "invalid orgId=" + F_ORGID );
//		}

		action.debug( "check forms region=" + F_REGION + ", orgname=" + P_ORGNAME + " ..." );
//		checkDir( action , P_ALIGNEDNAME , P_ALIGNEDID , P_ORGNAME + "/svcdic" , "sql" , "nsi" );
//		checkDir( action , P_ALIGNEDNAME , P_ALIGNEDID , P_ORGNAME + "/svcspec" , "sql" , "pgu pguapi" );
//		checkDir( action , P_ALIGNEDNAME , P_ALIGNEDID , P_ORGNAME + "/svcform" , "form" , ALL_SCHEMA_LIST );
//		checkDir( action , P_ALIGNEDNAME , P_ALIGNEDID , P_ORGNAME + "/svcpub" , "pub" , ALL_SCHEMA_LIST );
	}

	private void checkOneWar( ActionBase action , FileSet P_ALIGNEDNAME , String P_ALIGNEDID , String P_MPNAME ) throws Exception {
		String[] items = Common.splitDotted( P_MPNAME );
		String F_REGION = items[1];
		String F_WAR = items[2];

		boolean F_DIR_FAILED = false;
		if( F_REGION.isEmpty() || F_WAR.isEmpty() ) {
			action.error( "prepare: invalid regional war folder name=" + P_MPNAME + ", expected format is war.regnum.warname" );
			F_DIR_FAILED = true;
		}

		// check region is NN
		if( !F_REGION.matches( "[0-9][0-9]" ) ) {
			action.error( "prepare: invalid regional folder name=" + P_MPNAME + ", region=" + F_REGION + ", expected NN" );
			F_DIR_FAILED = true;
		}

		if( F_DIR_FAILED ) {
			S_CHECK_FAILED = true;
//			moveErrors( action , P_ALIGNEDNAME , P_ALIGNEDID , P_MPNAME , "invalid regional war folder name=" + P_MPNAME + ", expected format is war.regnum.warname" );
			return;
		}

//		if( !distr.checkWarMRId( action , F_WAR ) ) {
//			S_CHECK_FAILED = true;
//			moveErrors( action , P_ALIGNEDNAME , P_ALIGNEDID , P_MPNAME , "unknown war=" + F_WAR );
//			return;
//		}

		action.debug( "check war region=" + F_REGION + ", mpname=" + P_MPNAME + " ..." );
//		checkDir( action , P_ALIGNEDNAME , P_ALIGNEDID , P_MPNAME + "/svcdic" , "sql" , "nsi" );
//		checkDir( action , P_ALIGNEDNAME , P_ALIGNEDID , P_MPNAME + "/svcspec" , "sql" , "pguapi" );
	}

	private void processUddiSmevAttrs( ActionBase action , String P_SVCNUM , LocalFolder P_TARGETDIR , String FNAME_UAT , String FNAME_PROD , String SMEVATTRFILE ) throws Exception {
//		MetaDatabaseSchema schema = database.getSchema( action , "juddi" );
		
		// process content for endpoints
		String LOCAL_UDDI_FNAME = "uddi.txt";
		P_TARGETDIR.removeFiles( action , LOCAL_UDDI_FNAME );

		P_TARGETDIR.appendFileWithFile( action , LOCAL_UDDI_FNAME , SMEVATTRFILE );

//		schema.specific.smevAttrBegin( action , P_TARGETDIR , FNAME_UAT );
//		schema.specific.smevAttrBegin( action , P_TARGETDIR , FNAME_PROD );

		for( String line : P_TARGETDIR.readFileLines( action , LOCAL_UDDI_FNAME ) ) {
			line = line.replace( '"' , '@' );
			String first = Common.getListItem( line , "@" , 0 );
			
			String UDDI_ATTR_ID = Common.getListItem( first , " " , 2 );
			String UDDI_ATTR_NAME = Common.getListItem( line , "@" , 1 );
			String UDDI_ATTR_CODE = Common.getListItem( line , "@" , 3 );
			String UDDI_ATTR_REGION = Common.getListItem( line , "@" , 5 );
			String UDDI_ATTR_ACCESSPOINT = Common.getListItem( line , "@" , 7 );

			if( UDDI_ATTR_ID.isEmpty() || UDDI_ATTR_NAME.isEmpty() || UDDI_ATTR_CODE.isEmpty() || UDDI_ATTR_REGION.isEmpty() || UDDI_ATTR_ACCESSPOINT.isEmpty() ) {
				S_CHECK_FAILED = true;
				action.error( "prepare: invalid string - line=" + line );
			}

//			schema.specific.smevAttrAddValue( action , UDDI_ATTR_ID , UDDI_ATTR_NAME , UDDI_ATTR_CODE , UDDI_ATTR_REGION , UDDI_ATTR_ACCESSPOINT , P_TARGETDIR , FNAME_UAT );
//			schema.specific.smevAttrAddValue( action , UDDI_ATTR_ID , UDDI_ATTR_NAME , UDDI_ATTR_CODE , UDDI_ATTR_REGION , UDDI_ATTR_ACCESSPOINT , P_TARGETDIR , FNAME_PROD );
		}

//		schema.specific.smevAttrEnd( action , P_TARGETDIR , FNAME_UAT );
//		schema.specific.smevAttrEnd( action , P_TARGETDIR , FNAME_PROD );

		action.debug( "prepare: SVCNUM=" + P_SVCNUM + " - UDDI content has been created for smev attributes." );
	}

	private void processUddiEndpoints( ActionBase action , String P_SVCNUM , LocalFolder P_TARGETDIR , String FNAME_UAT , String FNAME_PROD , String DICFILE , String SVCFILE ) throws Exception {
//		MetaDatabaseSchema schema = database.getSchema( action , "juddi" );
		
		// process content for endpoints
		String LOCAL_UDDI_FNAME = "uddi.txt";
		P_TARGETDIR.removeFiles( action , LOCAL_UDDI_FNAME );

		if( S_DIC_CONTENT )
			P_TARGETDIR.appendFileWithFile( action , LOCAL_UDDI_FNAME , DICFILE );

		if( S_SVC_CONTENT )
			P_TARGETDIR.appendFileWithFile( action , LOCAL_UDDI_FNAME , SVCFILE );

//		schema.specific.uddiBegin( action , P_TARGETDIR , FNAME_UAT );
//		schema.specific.uddiBegin( action , P_TARGETDIR , FNAME_PROD );

		for( String line : P_TARGETDIR.readFileLines( action , LOCAL_UDDI_FNAME ) ) {
			if( !line.startsWith( "-- UDDI" ) )
				continue;
			
			// format:
			// -- UDDI 10000034549 testurl produrl
			String[] lineParts = Common.splitSpaced( line );
			if( lineParts.length != 5 ) {
				action.error( "prepare: invalid UDDI line: " + line );
				S_CHECK_FAILED = true;
			}
			else {
				String UDDI_KEY = lineParts[2];
				String UDDI_UAT = lineParts[3];
				String UDDI_PROD = lineParts[4];
				if( UDDI_KEY.isEmpty() || UDDI_UAT.isEmpty() || UDDI_PROD.isEmpty() ) {
					action.error( "prepare: invalid UDDI data: key=" + UDDI_KEY + ", UDDI_UAT=" + UDDI_UAT + ", UDDI_PROD=" + UDDI_PROD );
					S_CHECK_FAILED = true;
				}
				else {
//					schema.specific.uddiAddEndpoint( action , UDDI_KEY , UDDI_UAT , P_TARGETDIR , FNAME_UAT );
//					schema.specific.uddiAddEndpoint( action , UDDI_KEY , UDDI_PROD , P_TARGETDIR , FNAME_PROD );
				}
			}
		}

//		schema.specific.uddiEnd( action , P_TARGETDIR , FNAME_UAT );
//		schema.specific.uddiEnd( action , P_TARGETDIR , FNAME_PROD );

		action.debug( "prepare: SVCNUM=" + P_SVCNUM + " - UDDI content has been created for endpoints." );
	}

	public void copyCustom( ActionBase action , CommandCustom custom , FileSet ALIGNEDNAME , String ALIGNEDID , LocalFolder TARGETDIR ) throws Exception {
//		copyServices( action , srcFileSet , S_COMMON_ALIGNEDID , TARGETDIR );
	}

	public boolean checkDatabaseDir( ActionBase action , CommandCustom custom , FileSet P_ALIGNEDNAME , String P_ALIGNEDID , String P_DIR , String P_SCHEMALIST ) throws Exception {
		String F_ONEFAILED_MSG = "";
		boolean F_ONEFAILED = false;
		String P_TYPE = "";
		
		String xbase = "";
		if( P_DIR.startsWith( "war." ) )
			checkOneWar( action , P_ALIGNEDNAME , P_ALIGNEDID , P_DIR );
		else
		if( P_DIR.startsWith( "forms." ) )
			checkOneForms( action , P_ALIGNEDNAME , P_ALIGNEDID , P_DIR );
		
		if( P_TYPE.equals( "form" ) ) {
			// for form script file should have ESERVICEID-pguforms-orderform.sql format
			String F_SERVICEID = Common.getListItem( xbase , "-" , 0 );

			if( !xbase.equals( F_SERVICEID + "-pguforms-orderform.sql" ) ) {
				F_ONEFAILED_MSG = Common.concat( F_ONEFAILED_MSG , "invalid script name=" + xbase + ", expected - ESERVICEID-pguforms-orderform.sql" , "; " );
				F_ONEFAILED = true;
			}
		}
		else
		if( P_TYPE.equals( "pub" ) ) {
			// publisher file should have ESERVICEID-publisher-orderform.zip format
			String F_SERVICEID = Common.getListItem( xbase , "-" , 0 );
			String F_PUBLISHER = Common.getListItem( xbase , "-" , 1 );

			if( !xbase.equals( F_SERVICEID + "-" + F_PUBLISHER + "-orderform.zip" ) ) {
				F_ONEFAILED_MSG = Common.concat( F_ONEFAILED_MSG , "invalid script name=" + xbase + ", expected - ESERVICEID-PUBLISHER-orderform.zip" , "; " );
				F_ONEFAILED = true;
			}
			
//			if( !database.checkPublisher( action , F_PUBLISHER ) ) {
//				F_ONEFAILED_MSG = Common.concat( F_ONEFAILED_MSG , "unknown publisher=" + F_PUBLISHER + " for " + xbase , "; " );
//				F_ONEFAILED = true;
//			}
		}
		
		return( F_ONEFAILED );
	}
	
	public boolean checkOrgInfo( ActionBase action , String S_ORG_EXTID ) throws Exception {
		String S_ORG_FOLDERID = getOrgInfo( action , S_ORG_EXTID );
		if( S_ORG_FOLDERID.isEmpty() )
			return( false );
		return( true );
	}

	public String getOrgInfo( ActionBase action , String S_ORG_EXTID ) throws Exception {
		// read org item mapping
		String path = "orginfo.txt";
		if( !action.shell.checkFileExists( action , path ) )
			action.exit1( MissingOrganizationalMappingFile1 , "organizational mapping file " + path + " not found" , path );
			
		String S_ORG_FOLDERID = action.shell.customGetValue( action , "grep " + Common.getQuoted( "^" + S_ORG_EXTID + "=" ) + " " + path + " | cut -d " + Common.getQuoted( "=" ) + " -f2" );
		return( S_ORG_FOLDERID );
	}
	
	public boolean checkPublisher( ActionBase action , String F_PUBLISHER )	throws Exception {
		return( Common.checkPartOfSpacedList( F_PUBLISHER , PUBLISHERS ) );
	}
	
}
