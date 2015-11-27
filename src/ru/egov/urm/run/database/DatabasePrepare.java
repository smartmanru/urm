package ru.egov.urm.run.database;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaDatabase;
import ru.egov.urm.meta.MetaDistr;
import ru.egov.urm.meta.MetaDistrDelivery;
import ru.egov.urm.meta.Metadata;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.storage.DistStorage;
import ru.egov.urm.storage.FileSet;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.SourceStorage;

public class DatabasePrepare {

	private DistStorage distStorage;
	private MetaDistrDelivery dbDelivery;
	private DatabaseSpecific specific;
	private LocalFolder srcFolder;
	private LocalFolder dstFolder;
	private FileSet srcFileSet;

	String errorFolder;
	
	boolean S_CHECK_FAILED;
	boolean S_DIC_CONTENT;
	boolean S_SVC_CONTENT;
	boolean S_SMEVATTR_CONTENT;

	private String S_ERROR_MSG;
	private String ALL_SCHEMA_LIST; 

	Metadata meta;
	MetaDistr distr;
	MetaDatabase database;
	
	public void processDatabaseFiles( ActionBase action , DistStorage distStorage , MetaDistrDelivery dbDelivery , LocalFolder src , LocalFolder dst , DatabaseSpecific specific ) throws Exception {
		this.distStorage = distStorage;
		this.dbDelivery = dbDelivery;
		this.specific = specific;
		this.srcFolder = src;
		this.dstFolder = dst;
		
		meta = action.meta;
		distr = meta.distr;
		database = distr.database;
		errorFolder = "db-" + Common.getNameTimeStamp();
		
		S_CHECK_FAILED = false;
		S_DIC_CONTENT = false;
		S_SVC_CONTENT = false;
		S_SMEVATTR_CONTENT = false;
		ALL_SCHEMA_LIST = dbDelivery.SCHEMASET;
		
		action.debug( "prepare from " + src.folderPath + " to " + dst.folderPath + " (permitted schema list={" + ALL_SCHEMA_LIST + "}) ..." );
		
		if( !src.checkExists( action ) )
			action.exit( "sql folder does not exist" );
		
		srcFileSet = src.getFileSet( action );
		dst.recreateThis( action );
		
		// get aligned
		FileSet[] F_ALIGNEDDIRLIST = null;
		FileSet aligned = srcFileSet.getDirByPath( action , "aligned" );
		if( aligned != null )
			F_ALIGNEDDIRLIST = aligned.dirs.values().toArray( new FileSet[0] );

		// check scripts from SVN (exit on errors if no -s option)
		checkAll( action , F_ALIGNEDDIRLIST );

		// change script numbers and copy to ../patches.log (exit on errors if no -s option)
		copyAll( action , F_ALIGNEDDIRLIST );
	}

	private void checkAll( ActionBase action , FileSet[] P_ALIGNEDDIRLIST ) throws Exception {
		// common
		String S_COMMON_ALIGNEDID = database.alignedGetIDByBame( action , "common" );
		action.log( "prepare: =================================== check common ..." );
		check( action , srcFileSet , S_COMMON_ALIGNEDID );

		// aligned
		if( P_ALIGNEDDIRLIST == null )
			return;
		
		for( FileSet aligneddir : P_ALIGNEDDIRLIST ) {
			S_COMMON_ALIGNEDID = database.alignedGetIDByBame( action , aligneddir.dirName );

			action.log( "prepare: =================================== check aligned dir=" + aligneddir + " ..." );
			check( action , aligneddir , S_COMMON_ALIGNEDID );
		}
	}

	private void copyAll( ActionBase action , FileSet[] P_ALIGNEDDIRLIST ) throws Exception {
		// common
		String S_COMMON_ALIGNEDID = database.alignedGetIDByBame( action , "common" );
		action.log( "prepare: =================================== copy common id=" + S_COMMON_ALIGNEDID + " ..." );
		
		LocalFolder F_TARGETDIR = dstFolder;
		copyCore( action , srcFileSet , S_COMMON_ALIGNEDID , F_TARGETDIR );
		copyServices( action , srcFileSet , S_COMMON_ALIGNEDID , F_TARGETDIR );

		// aligned
		if( P_ALIGNEDDIRLIST == null )
			return;
		
		for( FileSet aligneddir : P_ALIGNEDDIRLIST ) {
			S_COMMON_ALIGNEDID = database.alignedGetIDByBame( action , aligneddir.dirName );

			action.log( "prepare: =================================== copy aligned dir=" + aligneddir + " id=" + S_COMMON_ALIGNEDID + " ..." );
			
			F_TARGETDIR = dstFolder.getSubFolder( action , "aligned/" + aligneddir );
			copyCore( action , aligneddir , S_COMMON_ALIGNEDID , F_TARGETDIR );
			copyServices( action , aligneddir , S_COMMON_ALIGNEDID , F_TARGETDIR );
		}
	}
	
	private void copyServices( ActionBase action , FileSet P_ALIGNEDNAME , String P_ALIGNEDID , LocalFolder P_TARGETDIR ) throws Exception {
		for( FileSet name : P_ALIGNEDNAME.dirs.values() ) {
			if( name.dirName.startsWith( "war." ) )
				copyOneWar( action , P_ALIGNEDNAME , P_ALIGNEDID , P_TARGETDIR , name );
			if( name.dirName.startsWith( "forms." ) )
				copyOneForms( action , P_ALIGNEDNAME , P_ALIGNEDID , P_TARGETDIR , name );
		}
			
		if( S_CHECK_FAILED ) {
			if( action.options.OPT_FORCE )
				action.log( "prepare: errors in script set. Ignored." );
			else
				action.exit( "prepare: errors in script set" );
		}
	}

	private void copyOneForms( ActionBase action , FileSet P_ALIGNEDNAME , String P_ALIGNEDID , LocalFolder P_TARGETDIR , FileSet P_ORGNAME ) throws Exception {
		action.log( "process forms regional folder: " + P_ORGNAME.dirName + " ..." );
		copyDir( action , P_ALIGNEDNAME , P_ALIGNEDID , P_ORGNAME.getDirByPath( action , "svcdic" ) , P_TARGETDIR.getSubFolder( action , "svcrun" ) );
		copyDir( action , P_ALIGNEDNAME , P_ALIGNEDID , P_ORGNAME.getDirByPath( action , "svcspec" ) , P_TARGETDIR.getSubFolder( action , "svcrun" ) );
		copyDir( action , P_ALIGNEDNAME , P_ALIGNEDID , P_ORGNAME.getDirByPath( action , "svcform" ) , P_TARGETDIR.getSubFolder( action , "svcrun" ) );
		copyUddi( action , P_ALIGNEDNAME , P_ALIGNEDID , P_TARGETDIR , P_ORGNAME );

		// copy publisher part
		FileSet svcpub = P_ORGNAME.getDirByPath( action , "svcpub" ); 
		if( svcpub != null )
			copyZip( action , P_ALIGNEDNAME , P_ALIGNEDID , svcpub , P_TARGETDIR.getSubFolder( action , "svcpub" ) );
	}

	private void copyOneWar( ActionBase action , FileSet P_ALIGNEDNAME , String P_ALIGNEDID , LocalFolder P_TARGETDIR , FileSet P_MPNAME ) throws Exception {
		action.log( "process war regional folder: " + P_MPNAME.dirName + " ..." );
		copyDir( action , P_ALIGNEDNAME , P_ALIGNEDID , P_MPNAME.getDirByPath( action , "svcdic" ) , P_TARGETDIR.getSubFolder( action , "svcrun" ) );
		copyDir( action , P_ALIGNEDNAME , P_ALIGNEDID , P_MPNAME.getDirByPath( action , "svcspec" ) , P_TARGETDIR.getSubFolder( action , "svcrun" ) );
		copyUddi( action , P_ALIGNEDNAME , P_ALIGNEDID , P_TARGETDIR , P_MPNAME );
	}

	private void copyUddi( ActionBase action , FileSet P_ALIGNEDNAME , String P_ALIGNEDID , LocalFolder P_TARGETDIR , FileSet P_UDDIDIR ) throws Exception {
		String F_UDDINUM = database.getSqlIndexPrefix( action , P_UDDIDIR.dirName + ".juddi" , P_ALIGNEDID );

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
			action.log( P_UDDIDIR + "/svcdic ..." );
			if( svcdic.files.containsKey( "extdicuddi.txt" ) )
				specific.grepComments( action , "UDDI" , srcFolder , Common.getPath( svcdic.dirPath , "extdicuddi.txt" ) , P_TARGETDIR , SRC_DICFILE_EP );
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
				specific.grepComments( action , "SMEVATTR" , srcFolder , Common.getPath( svcspec.dirPath , script ) , P_TARGETDIR , SRC_SMEVATTRFILE );
				specific.grepComments( action , "UDDI" , srcFolder , Common.getPath( svcspec.dirPath , script ) , P_TARGETDIR , SRC_SVCFILE_EP );
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

		specific.addComment( action , "UAT UDDI setup script" , P_TARGETDIR , DST_FNAME_UAT );
		specific.addComment( action , "PROD UDDI setup script" , P_TARGETDIR , DST_FNAME_PROD );

		// process endpoints
		if( S_DIC_CONTENT || S_SVC_CONTENT )
			processUddiEndpoints( action , F_UDDINUM , P_TARGETDIR , DST_FNAME_UAT , DST_FNAME_PROD , SRC_DICFILE_EP , SRC_SVCFILE_EP );

		// process smev attrs
		if( S_SMEVATTR_CONTENT )
			processUddiSmevAttrs( action , F_UDDINUM , P_TARGETDIR , DST_FNAME_UAT , DST_FNAME_PROD , SRC_SMEVATTRFILE );
	}
			
	private void copyCore( ActionBase action , FileSet P_ALIGNEDNAME , String P_ALIGNEDID , LocalFolder P_TARGETDIR ) throws Exception {
		action.log( "preparing core scripts aligned=" + P_ALIGNEDNAME.dirName + " ..." );
		copyDir( action , P_ALIGNEDNAME , P_ALIGNEDID , P_ALIGNEDNAME.getDirByPath( action , "coreddl" ) , P_TARGETDIR );
		copyDir( action , P_ALIGNEDNAME , P_ALIGNEDID , P_ALIGNEDNAME.getDirByPath( action , "coredml" ) , P_TARGETDIR );
		copyDir( action , P_ALIGNEDNAME , P_ALIGNEDID , P_ALIGNEDNAME.getDirByPath( action , "coreprodonly" ) , P_TARGETDIR.getSubFolder( action , "prodonly" ) );
		copyDir( action , P_ALIGNEDNAME , P_ALIGNEDID , P_ALIGNEDNAME.getDirByPath( action , "coreuatonly" ) , P_TARGETDIR.getSubFolder( action , "uatonly" ) );
		copyDir( action , P_ALIGNEDNAME , P_ALIGNEDID , P_ALIGNEDNAME.getDirByPath( action , "coresvc" ) , P_TARGETDIR.getSubFolder( action , "svcrun" ) );

		// copy dataload part
		FileSet dataload = P_ALIGNEDNAME.getDirByPath( action , "dataload" );
		if( dataload != null )
			copyCtl( action , P_ALIGNEDNAME , P_ALIGNEDID , dataload , P_TARGETDIR.getSubFolder( action , "dataload" ) );

		// copy manual part
		FileSet manual = P_ALIGNEDNAME.getDirByPath( action , "manual" );
		if( manual != null )
			srcFolder.copyFolder( action , manual.dirPath , P_TARGETDIR.getSubFolder( action , "manual" ) );
	}

	private void copyZip( ActionBase action , FileSet P_ALIGNEDNAME , String P_ALIGNEDID , FileSet P_DIRFROM , LocalFolder P_DIRTO ) throws Exception {
		action.log( "prepare/copy " + P_DIRFROM.dirPath + " ..." );

		P_DIRTO.ensureExists( action );

		// regional tail
		String F_REGIONALINDEX = "";
		if( P_ALIGNEDNAME.dirName.equals( "regional" ) )
			F_REGIONALINDEX = "RR";

		// add registration index
		for( String x : Common.getSortedKeys( P_DIRFROM.files ) ) {
			if( !x.endsWith( ".zip" ) )
				continue;
			
			String F_SCRIPTNUM = Common.getPartBeforeFirst( x , "-" );
			
			// get filename without index
			String F_FILEBASE = Common.getPartAfterFirst( x , "-" );
			
			// rename - by index
			srcFolder.copyFile( action , P_DIRFROM.dirPath , x , P_DIRTO , "18" + P_ALIGNEDID + F_SCRIPTNUM + F_REGIONALINDEX + "-" + F_FILEBASE );
		}
	}

	private void copyCtl( ActionBase action , FileSet P_ALIGNEDNAME , String P_ALIGNEDID , FileSet P_CTLFROM , LocalFolder P_CTLTO ) throws Exception {
		P_CTLTO.ensureExists( action );

		// regional tail
		String F_REGIONALINDEX = "";
		if( P_ALIGNEDNAME.equals( "regional" ) )
			F_REGIONALINDEX = "RR";

		// add registration index
		for( String x : Common.getSortedKeys( P_CTLFROM.files ) ) {
			if( !x.endsWith( ".ctl" ) )
				continue;
		
			action.debug( "process " + x + " ..." );
			String F_SCRIPTNUM = Common.getPartBeforeFirst( x , "-" );
			
			// get filename without extension
			String F_FILEBASE = Common.getPartBeforeLast( x , "." );
			String F_FILEBASENOINDEX = Common.getPartAfterFirst( F_FILEBASE , "-" );
			
			// rename - all by ctl index
			srcFolder.copyFiles( action , P_CTLFROM.dirPath , F_SCRIPTNUM + "-*" , P_CTLTO );

			// change index
			P_CTLTO.renameFile( action , F_FILEBASE + ".ctl" , P_CTLTO + "/20" + P_ALIGNEDID + F_SCRIPTNUM + F_REGIONALINDEX + "-" + F_FILEBASENOINDEX + ".ctl" );

			if( P_CTLTO.checkFileExists( action , F_FILEBASE + ".sql" ) )
				P_CTLTO.renameFile( action , F_FILEBASE + ".sql" , "21" + P_ALIGNEDID + F_SCRIPTNUM + F_REGIONALINDEX + "-" + F_FILEBASENOINDEX + ".sql" );
		}
	}

	private void processUddiSmevAttrs( ActionBase action , String P_SVCNUM , LocalFolder P_TARGETDIR , String FNAME_UAT , String FNAME_PROD , String SMEVATTRFILE ) throws Exception {
		// process content for endpoints
		String LOCAL_UDDI_FNAME = "uddi.txt";
		P_TARGETDIR.removeFiles( action , LOCAL_UDDI_FNAME );

		P_TARGETDIR.appendFileWithFile( action , LOCAL_UDDI_FNAME , SMEVATTRFILE );

		specific.smevAttrBegin( action , P_TARGETDIR , FNAME_UAT );
		specific.smevAttrBegin( action , P_TARGETDIR , FNAME_PROD );

		for( String line : P_TARGETDIR.readFileLines( action , LOCAL_UDDI_FNAME ) ) {
			// format:
			// -- SMEVATTR NNN name="text" code="XXX" region="NN" accesspoint="NN"
			line = line.replace( '"' , '@' );
			String first = Common.getListItem( line , "@" , 0 );
			
			String UDDI_ATTR_ID = Common.getListItem( first , " " , 2 );
			String UDDI_ATTR_NAME = Common.getListItem( line , "@" , 1 );
			String UDDI_ATTR_CODE = Common.getListItem( line , "@" , 3 );
			String UDDI_ATTR_REGION = Common.getListItem( line , "@" , 5 );
			String UDDI_ATTR_ACCESSPOINT = Common.getListItem( line , "@" , 7 );

			if( UDDI_ATTR_ID.isEmpty() || UDDI_ATTR_NAME.isEmpty() || UDDI_ATTR_CODE.isEmpty() || UDDI_ATTR_REGION.isEmpty() || UDDI_ATTR_ACCESSPOINT.isEmpty() ) {
				S_CHECK_FAILED = true;
				action.log( "prepare: invalid string - line=" + line );
			}

			specific.smevAttrAddValue( action , UDDI_ATTR_ID , UDDI_ATTR_NAME , UDDI_ATTR_CODE , UDDI_ATTR_REGION , UDDI_ATTR_ACCESSPOINT , P_TARGETDIR , FNAME_UAT );
			specific.smevAttrAddValue( action , UDDI_ATTR_ID , UDDI_ATTR_NAME , UDDI_ATTR_CODE , UDDI_ATTR_REGION , UDDI_ATTR_ACCESSPOINT , P_TARGETDIR , FNAME_PROD );
		}

		specific.smevAttrEnd( action , P_TARGETDIR , FNAME_UAT );
		specific.smevAttrEnd( action , P_TARGETDIR , FNAME_PROD );

		action.debug( "prepare: SVCNUM=" + P_SVCNUM + " - UDDI content has been created for smev attributes." );
	}

	private void processUddiEndpoints( ActionBase action , String P_SVCNUM , LocalFolder P_TARGETDIR , String FNAME_UAT , String FNAME_PROD , String DICFILE , String SVCFILE ) throws Exception {
		// process content for endpoints
		String LOCAL_UDDI_FNAME = "uddi.txt";
		P_TARGETDIR.removeFiles( action , LOCAL_UDDI_FNAME );

		if( S_DIC_CONTENT )
			P_TARGETDIR.appendFileWithFile( action , LOCAL_UDDI_FNAME , DICFILE );

		if( S_SVC_CONTENT )
			P_TARGETDIR.appendFileWithFile( action , LOCAL_UDDI_FNAME , SVCFILE );

		specific.uddiBegin( action , P_TARGETDIR , FNAME_UAT );
		specific.uddiBegin( action , P_TARGETDIR , FNAME_PROD );

		for( String line : P_TARGETDIR.readFileLines( action , LOCAL_UDDI_FNAME ) ) {
			if( !line.startsWith( "-- UDDI" ) )
				continue;
			
			// format:
			// -- UDDI 10000034549 testurl produrl
			String[] lineParts = Common.splitSpaced( line );
			if( lineParts.length != 5 ) {
				action.log( "prepare: invalid UDDI line: " + line );
				S_CHECK_FAILED = true;
			}
			else {
				String UDDI_KEY = lineParts[2];
				String UDDI_UAT = lineParts[3];
				String UDDI_PROD = lineParts[4];
				if( UDDI_KEY.isEmpty() || UDDI_UAT.isEmpty() || UDDI_PROD.isEmpty() ) {
					action.log( "prepare: invalid UDDI data: key=" + UDDI_KEY + ", UDDI_UAT=" + UDDI_UAT + ", UDDI_PROD=" + UDDI_PROD );
					S_CHECK_FAILED = true;
				}
				else {
					specific.uddiAddEndpoint( action , UDDI_KEY , UDDI_UAT , P_TARGETDIR , FNAME_UAT );
					specific.uddiAddEndpoint( action , UDDI_KEY , UDDI_PROD , P_TARGETDIR , FNAME_PROD );
				}
			}
		}

		specific.uddiEnd( action , P_TARGETDIR , FNAME_UAT );
		specific.uddiEnd( action , P_TARGETDIR , FNAME_PROD );

		action.debug( "prepare: SVCNUM=" + P_SVCNUM + " - UDDI content has been created for endpoints." );
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

	private void check( ActionBase action , FileSet P_ALIGNEDNAME , String P_ALIGNEDID ) throws Exception {
		S_CHECK_FAILED = false;

		// check folders
		for( String dir : Common.getSortedKeys( P_ALIGNEDNAME.dirs ) ) {
			if( dir.equals( "coreddl" ) || dir.equals( "coredml" ) || dir.equals( "coresvc" ) || dir.equals( "coreprodonly" ) || dir.equals( "coreuatonly" ) )
				checkDir( action , P_ALIGNEDNAME , P_ALIGNEDID , dir , "sql" , ALL_SCHEMA_LIST );
			else
			if( dir.equals( "dataload" ) ) 
				checkDir( action , P_ALIGNEDNAME , P_ALIGNEDID , dir , "ctl" , ALL_SCHEMA_LIST );
			else
			if( dir.startsWith( "war." ) )
				checkOneWar( action , P_ALIGNEDNAME , P_ALIGNEDID , dir );
			else
			if( dir.startsWith( "forms." ) )
				checkOneForms( action , P_ALIGNEDNAME , P_ALIGNEDID , dir );
			else
			if( dir.equals( "aligned" ) && P_ALIGNEDNAME == srcFileSet )
				continue;
			else
			if( dir.equals( "manual" ) )
				continue;
			else {
				action.log( "prepare: aligned=" + P_ALIGNEDNAME + " - invalid release folder: " + dir );
				moveErrors( action , P_ALIGNEDNAME , P_ALIGNEDID , dir , "invalid release folder" );
				S_CHECK_FAILED = true;
			}
		}

		if( S_CHECK_FAILED && !action.options.OPT_FORCE )
			action.exit( "release database file set check failed" );
	}

	private void checkOneForms( ActionBase action , FileSet P_ALIGNEDNAME , String P_ALIGNEDID , String P_ORGNAME ) throws Exception {
		String[] items = Common.split( P_ORGNAME , "\\." );
		String F_REGION = items[1];
		String F_ORGID = items[2];

		boolean F_DIR_FAILED = false;
		if( F_REGION.isEmpty() || F_ORGID.isEmpty() ) {
			action.log( "prepare: invalid regional forms folder name=" + P_ORGNAME + ", expected format is forms.regnum.orgcode" );
			F_DIR_FAILED = true;
		}

		// check region is NN
		if( !F_REGION.matches( "[0-9][0-9]" ) ) {
			action.log( "prepare: invalid regional folder name=" + P_ORGNAME + ", region=" + F_REGION + ", expected NN" );
			F_DIR_FAILED = true;
		}

		if( F_DIR_FAILED ) {
			S_CHECK_FAILED = true;
			moveErrors( action , P_ALIGNEDNAME , P_ALIGNEDID , P_ORGNAME , "invalid regional forms folder name=" + P_ORGNAME + ", expected format is forms.regnum.orgcode" );
		}

		// check ORGID
		if( !database.checkOrgInfo( action , F_ORGID ) ) {
			S_CHECK_FAILED = true;
			moveErrors( action , P_ALIGNEDNAME , P_ALIGNEDID , P_ORGNAME , "invalid orgId=" + F_ORGID );
		}

		action.debug( "check forms region=" + F_REGION + ", orgname=" + P_ORGNAME + " ..." );
		checkDir( action , P_ALIGNEDNAME , P_ALIGNEDID , P_ORGNAME + "/svcdic" , "sql" , "nsi" );
		checkDir( action , P_ALIGNEDNAME , P_ALIGNEDID , P_ORGNAME + "/svcspec" , "sql" , "pgu pguapi" );
		checkDir( action , P_ALIGNEDNAME , P_ALIGNEDID , P_ORGNAME + "/svcform" , "form" , ALL_SCHEMA_LIST );
		checkDir( action , P_ALIGNEDNAME , P_ALIGNEDID , P_ORGNAME + "/svcpub" , "pub" , ALL_SCHEMA_LIST );
	}

	private void checkOneWar( ActionBase action , FileSet P_ALIGNEDNAME , String P_ALIGNEDID , String P_MPNAME ) throws Exception {
		String[] items = Common.split( P_MPNAME , "\\." );
		String F_REGION = items[1];
		String F_WAR = items[2];

		boolean F_DIR_FAILED = false;
		if( F_REGION.isEmpty() || F_WAR.isEmpty() ) {
			action.log( "prepare: invalid regional war folder name=" + P_MPNAME + ", expected format is war.regnum.warname" );
			F_DIR_FAILED = true;
		}

		// check region is NN
		if( !F_REGION.matches( "[0-9][0-9]" ) ) {
			action.log( "prepare: invalid regional folder name=" + P_MPNAME + ", region=" + F_REGION + ", expected NN" );
			F_DIR_FAILED = true;
		}

		if( F_DIR_FAILED ) {
			S_CHECK_FAILED = true;
			moveErrors( action , P_ALIGNEDNAME , P_ALIGNEDID , P_MPNAME , "invalid regional war folder name=" + P_MPNAME + ", expected format is war.regnum.warname" );
			return;
		}

		if( !distr.checkWarMRId( action , F_WAR ) ) {
			S_CHECK_FAILED = true;
			moveErrors( action , P_ALIGNEDNAME , P_ALIGNEDID , P_MPNAME , "unknown war=" + F_WAR );
			return;
		}

		action.debug( "check war region=" + F_REGION + ", mpname=" + P_MPNAME + " ..." );
		checkDir( action , P_ALIGNEDNAME , P_ALIGNEDID , P_MPNAME + "/svcdic" , "sql" , "nsi" );
		checkDir( action , P_ALIGNEDNAME , P_ALIGNEDID , P_MPNAME + "/svcspec" , "sql" , "pguapi" );
	}

	private boolean checkDuplicateIndex( ActionBase action , FileSet dir , String index , String ext ) throws Exception {
		int count = 0;
		for( String s : dir.files.keySet() ) {
			if( s.startsWith( index + "-" ) && s.endsWith( "." + ext ) ) {
				count++;
				if( count > 1 )
					return( true );
			}
		}
		return( false );
	}
	
	private void checkDir( ActionBase action , FileSet P_ALIGNEDNAME , String P_ALIGNEDID , String P_DIR , String P_TYPE , String P_SCHEMALIST ) throws Exception {
		FileSet dir = P_ALIGNEDNAME.getDirByPath( action , P_DIR );
		if( dir == null )
			return;

		for( String xbase : Common.getSortedKeys( dir.files ) ) {
			boolean F_ONEFAILED = false;
			String F_ONEFAILED_MSG = "";

			// check well-formed name
			if( !xbase.matches( "[0-9a-zA-Z_.-]*" ) ) {
				F_ONEFAILED_MSG = Common.concat( F_ONEFAILED_MSG , "invalid filename characters - '" + xbase + "'" , "; " );
				F_ONEFAILED = true;
			}

			// get extension
			String F_EXT = Common.getPartAfterLast( xbase , "." );

			// for sql type it should be the ONLY extension
			if( F_ONEFAILED == false && P_TYPE.equals( "sql" ) && !F_EXT.equals( "sql" ) ) {
				F_ONEFAILED_MSG = Common.concat( F_ONEFAILED_MSG , "invalid filename extension - " + xbase , "; " );
				F_ONEFAILED = true;
			}

			if( P_TYPE.equals( "sql" ) ) {
				// for sql type files should have NNN-SCHEMA-zzz.sql format
				String F_SCRIPTNUM = Common.getListItem( xbase , "-" , 0 );
				String F_SCRIPTSCHEMA = Common.getListItem( xbase , "-" , 1 );

				if( F_ONEFAILED == false && ( F_SCRIPTNUM.isEmpty() || !F_SCRIPTNUM.matches( "[0-9][0-9][0-9]" ) ) ) {
					F_ONEFAILED_MSG = Common.concat( F_ONEFAILED_MSG , "invalid script number - " + xbase , "; " );
					F_ONEFAILED = true;
				}

				// check scriptnum is unique
				if( F_ONEFAILED == false && F_SCRIPTNUM.isEmpty() == false && checkDuplicateIndex( action , dir , F_SCRIPTNUM , F_EXT ) ) {
					F_ONEFAILED_MSG = Common.concat( F_ONEFAILED_MSG , "not unique script number - " + xbase , "; " );
					F_ONEFAILED = true;
				}

				if( !F_ONEFAILED ) {
					if( !checkSchema( action , P_ALIGNEDNAME , F_SCRIPTSCHEMA , P_SCHEMALIST ) ) {
						F_ONEFAILED_MSG = Common.concat( F_ONEFAILED_MSG , "invalid schema for " + xbase + ", permitted schema list - {" + P_SCHEMALIST + "}" , "; " );
						F_ONEFAILED = true;
					}
				}
			}
			else 
			if( P_TYPE.equals( "ctl" ) ) {
				// for ctl type .sql and .ctl files should have NNN-SCHEMA-zzz.ctl format, other files should be NNN-xxx format
				String F_SCRIPTNUM = Common.getListItem( xbase , "-" , 0 );
				if( F_ONEFAILED == false && ( F_SCRIPTNUM.isEmpty() || !F_SCRIPTNUM.matches( "[0-9][0-9][0-9]" ) ) ) {
					F_ONEFAILED_MSG = Common.concat( F_ONEFAILED_MSG , "invalid dataload file number - " + xbase , "; " );
					F_ONEFAILED = true;
				}

				if( F_EXT.equals( "ctl" ) || F_EXT.equals( "sql" ) ) {
					// check scriptnum is unique across the same extension
					if( F_ONEFAILED == false && F_SCRIPTNUM.isEmpty() == false && checkDuplicateIndex( action , dir , F_SCRIPTNUM , F_EXT ) ) {
						F_ONEFAILED_MSG = Common.concat( F_ONEFAILED_MSG , "not unique dataload file number - " + xbase , "; " );
						F_ONEFAILED = true;
					}

					String F_SCRIPTSCHEMA = Common.getListItem( xbase , "-" , 1 );

					if( !F_ONEFAILED ) {
						if( !checkSchema( action , P_ALIGNEDNAME , F_SCRIPTSCHEMA , P_SCHEMALIST ) ) {
							F_ONEFAILED_MSG = Common.concat( F_ONEFAILED_MSG , "invalid schema for " + xbase + ", permitted schema list - {" + P_SCHEMALIST + "}" , "; " );
							F_ONEFAILED = true;
						}
					}
				}
			}
			else
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
				
				if( !database.checkPublisher( action , F_PUBLISHER ) ) {
					F_ONEFAILED_MSG = Common.concat( F_ONEFAILED_MSG , "unknown publisher=" + F_PUBLISHER + " for " + xbase , "; " );
					F_ONEFAILED = true;
				}
			}

			// check sql file content
			if( F_ONEFAILED == false && F_EXT.equals( "sql" ) ) {
				if( !checkSql( action , dir , xbase ) ) {
					F_ONEFAILED_MSG = Common.concat( F_ONEFAILED_MSG , S_ERROR_MSG , "; " );
					F_ONEFAILED = true;
				}
			}

			if( F_ONEFAILED ) {
				moveErrors( action , dir , P_ALIGNEDID , xbase , F_ONEFAILED_MSG );
				S_CHECK_FAILED = true;
			}
		}
	}

	private boolean checkSchema( ActionBase action , FileSet P_ALIGNEDNAME , String P_SCHEMA , String P_SCHEMALIST ) throws Exception {
		if( P_SCHEMA.isEmpty() )
			return( false );

		// exact if regional or common
		if( P_ALIGNEDNAME == srcFileSet || P_ALIGNEDNAME.dirName.equals( "regional" ) ) {
			if( Common.checkPartOfSpacedList( P_SCHEMA , P_SCHEMALIST ) )
				return( true );
			return( false );
		}

		// in aligned can be both masked and direct
		String[] schemaList = Common.splitSpaced( P_SCHEMALIST ); 
		for( String schema : schemaList ) {
			String F_MASK = Common.replace( schema , "RR" , "[0-9][0-9]" );
			if( P_SCHEMA.equals( schema ) || P_SCHEMA.matches( F_MASK ) )
				return( true );
		}

		return( false );
	}

	private void copyDir( ActionBase action , FileSet P_ALIGNEDNAME , String P_ALIGNEDID , FileSet SQL_SRC_DIR , LocalFolder SQL_DST_DIR ) throws Exception {
		if( SQL_SRC_DIR == null )
			return;
		
		String SQL_PREFIX = database.getSqlIndexPrefix( action , SQL_SRC_DIR.dirPath , P_ALIGNEDID );

		// regional tail
		String F_REGIONALINDEX;

		if( !srcFolder.checkFolderExists( action , SQL_SRC_DIR.dirPath ) ) {
			action.debug( SQL_SRC_DIR.dirPath + " is not found. Skipped." );
			return;
		}

		SQL_DST_DIR.ensureExists( action );
		action.debug( "prepare/copy " + SQL_SRC_DIR.dirPath + " ..." );

		// process apply scripts
		for( String x : Common.getSortedKeys( SQL_SRC_DIR.files ) ) {
			if( !x.endsWith( ".sql" ) )
				continue;
			
			String[] items = Common.split( x , "-" );
			String xrindex = items[0];
			String xrschema = items[1];
			String xrtail = x.substring( xrindex.length() + 1 + xrschema.length() + 1 );
			
			if( P_ALIGNEDNAME.dirName.equals( "regional" ) || xrschema.indexOf( "RR" ) >= 0 )
				F_REGIONALINDEX = "RR";
			else
				F_REGIONALINDEX = "";

			copySql( action , SQL_SRC_DIR , x , SQL_DST_DIR , SQL_PREFIX + xrindex + F_REGIONALINDEX + "-" + xrschema + "-" + xrtail );
		}

		// process rollback scripts
		FileSet srcRollback = SQL_SRC_DIR.getDirByPath( action , "rollback" );
		if( srcRollback != null ) {
			LocalFolder dstRollback = SQL_DST_DIR.getSubFolder( action , "rollback" );
			dstRollback.ensureExists( action );

			for( String x : Common.getSortedKeys( srcRollback.files ) ) {
				if( !x.endsWith( ".sql" ) )
					continue;
			
				String xrindex = Common.getListItem( x , "-" , 0 );
				String xrschema = Common.getListItem( x , "-" , 1 );
				String xrtail = x.substring( xrindex.length() + 1 + xrschema.length() + 1 );
			
				if( P_ALIGNEDNAME.dirName.equals( "regional" ) || xrschema.indexOf( "RR" ) >= 0 )
					F_REGIONALINDEX = "RR";
				else
					F_REGIONALINDEX = "";

				copySql( action , srcRollback , x , dstRollback , SQL_PREFIX + xrindex + F_REGIONALINDEX + "-" + xrschema + "-" + xrtail );
			}
		}
	}

	private void moveErrors( ActionBase action , FileSet P_ALIGNEDNAME , String P_ALIGNEDID , String P_PATH , String P_COMMENT ) throws Exception {
		if( !action.options.OPT_MOVE_ERRORS ) {
			action.log( "errors in " + P_PATH + ": " + P_COMMENT );
			return;
		}

		action.log( "moving " + P_PATH + " to errors folder ..." );

		SourceStorage sourceStorage = action.artefactory.getSourceStorage( action );
		String movePath = Common.getPath( P_ALIGNEDNAME.dirPath , P_PATH );
		sourceStorage.moveReleaseDatabaseFilesToErrors( action , errorFolder , distStorage , dbDelivery , movePath , P_COMMENT );
	}

	private boolean checkSql( ActionBase action , FileSet P_ALIGNEDNAME , String P_SCRIPT ) throws Exception {
		LocalFolder scriptFolder = srcFolder.getSubFolder( action , P_ALIGNEDNAME.dirPath );
		if( !specific.validateScriptContent( action , scriptFolder , P_SCRIPT ) ) {
			S_ERROR_MSG = "invalid script content";
			return( false );
		}
		
		// check if regional
		if( P_ALIGNEDNAME.dirName.equals( "regional" ) ) {
			String S_SPECIFIC_COMMENT = specific.getComments( action , "REGIONS " , scriptFolder , P_SCRIPT ); 
			if( S_SPECIFIC_COMMENT.isEmpty() ) {
				S_ERROR_MSG = "script should have REGIONS header property - " + P_SCRIPT;
				return( false );
			}
		}	

		return( true );
	}

	private void copySql( ActionBase action , FileSet srcDir , String srcName , LocalFolder dstDir , String dstName ) throws Exception {
		srcFolder.copyFile( action , srcDir.dirPath , srcName , dstDir , dstName );
	}

}
