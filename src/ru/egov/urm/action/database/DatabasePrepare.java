package ru.egov.urm.action.database;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.MetaDatabase;
import ru.egov.urm.meta.MetaDatabaseSchema;
import ru.egov.urm.meta.MetaDistr;
import ru.egov.urm.meta.MetaDistrDelivery;
import ru.egov.urm.meta.Metadata;
import ru.egov.urm.storage.DistStorage;
import ru.egov.urm.storage.FileSet;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.SourceStorage;

public class DatabasePrepare {

	private DistStorage distStorage;
	private MetaDistrDelivery dbDelivery;
	private LocalFolder srcFolder;
	private LocalFolder dstFolder;
	private FileSet srcFileSet;

	String errorFolder;
	
	boolean S_CHECK_FAILED;

	private String S_ERROR_MSG;
	private String ALL_SCHEMA_LIST; 

	Metadata meta;
	MetaDistr distr;
	MetaDatabase database;
	
	public static String ALIGNED_FOLDER = "aligned";
	public static String REGIONAL_FOLDER = "regional";
	public static String COREDDL_FOLDER = "coreddl";
	public static String COREDML_FOLDER = "coredml";
	public static String COREPRODONLY_FOLDER = "coreprodonly";
	public static String COREUATONLY_FOLDER = "coreuatonly";
	public static String DATALOAD_FOLDER = "dataload";
	public static String MANUAL_FOLDER = "manual";
	public static String ROLLBACK_FOLDER = "rollback";
	
	public static String ALIGNED_COMMON = "common";
	
	public boolean processDatabaseFiles( ActionBase action , DistStorage distStorage , MetaDistrDelivery dbDelivery , LocalFolder src , LocalFolder dst ) throws Exception {
		this.distStorage = distStorage;
		this.dbDelivery = dbDelivery;
		this.srcFolder = src;
		this.dstFolder = dst;
		
		meta = action.meta;
		distr = meta.distr;
		database = distr.database;
		errorFolder = "db-" + Common.getNameTimeStamp();
		
		S_CHECK_FAILED = false;
		ALL_SCHEMA_LIST = dbDelivery.SCHEMASET;
		
		action.debug( "prepare from " + src.folderPath + " to " + dst.folderPath + " (permitted schema list={" + ALL_SCHEMA_LIST + "}) ..." );
		
		if( !src.checkExists( action ) )
			action.exit( "sql folder does not exist" );
		
		srcFileSet = src.getFileSet( action );
		dst.recreateThis( action );
		
		// get aligned
		FileSet[] F_ALIGNEDDIRLIST = null;
		FileSet aligned = srcFileSet.getDirByPath( action , ALIGNED_FOLDER );
		if( aligned != null )
			F_ALIGNEDDIRLIST = aligned.dirs.values().toArray( new FileSet[0] );

		// check scripts from SVN (exit on errors if no -s option)
		checkAll( action , F_ALIGNEDDIRLIST );
		if( S_CHECK_FAILED ) {
			if( !action.context.CTX_FORCE ) {
				action.log( "script set check failed, cancelled" );
				return( false );
			}
				
			action.log( "script set check failed, ignored" );
		}

		// change script numbers and copy to ../patches.log (exit on errors if no -s option)
		copyAll( action , F_ALIGNEDDIRLIST );
		return( true );
	}

	private void checkAll( ActionBase action , FileSet[] P_ALIGNEDDIRLIST ) throws Exception {
		// common
		String S_COMMON_ALIGNEDID = ALIGNED_COMMON;
		action.log( "prepare: =================================== check common ..." );
		check( action , srcFileSet , S_COMMON_ALIGNEDID );

		// aligned
		if( P_ALIGNEDDIRLIST == null )
			return;
		
		for( FileSet aligneddir : P_ALIGNEDDIRLIST ) {
			S_COMMON_ALIGNEDID = aligneddir.dirName;

			action.log( "prepare: =================================== check aligned dir=" + aligneddir + " ..." );
			check( action , aligneddir , S_COMMON_ALIGNEDID );
		}
	}

	private void check( ActionBase action , FileSet P_ALIGNEDNAME , String P_ALIGNEDID ) throws Exception {
		S_CHECK_FAILED = false;

		// check folders
		for( String dir : Common.getSortedKeys( P_ALIGNEDNAME.dirs ) ) {
			if( dir.equals( COREDDL_FOLDER ) || 
				dir.equals( COREDML_FOLDER ) || 
				dir.equals( COREPRODONLY_FOLDER ) || 
				dir.equals( COREUATONLY_FOLDER ) )
				checkDir( action , P_ALIGNEDNAME , P_ALIGNEDID , dir , "sql" , ALL_SCHEMA_LIST );
			else
			if( dir.equals( DATALOAD_FOLDER ) ) 
				checkDir( action , P_ALIGNEDNAME , P_ALIGNEDID , dir , "ctl" , ALL_SCHEMA_LIST );
			else
			if( dir.equals( ALIGNED_FOLDER ) && P_ALIGNEDNAME == srcFileSet )
				continue;
			else
			if( dir.equals( MANUAL_FOLDER ) )
				continue;
			else {
				boolean failed = true;
				if( action.custom.isCustomDatabase() ) {
					failed = false;
					
					String folderName = action.custom.getGroupName( action , dir );
					if( folderName != null ) {
						if( !action.custom.checkDatabaseDir( action , P_ALIGNEDNAME , P_ALIGNEDID , dir , ALL_SCHEMA_LIST ) )
							failed = true;
					}
				}
				
				if( failed ) {
					action.log( "prepare: aligned=" + P_ALIGNEDNAME + " - invalid release folder: " + dir );
					moveErrors( action , P_ALIGNEDNAME , P_ALIGNEDID , dir , "invalid release folder" );
					S_CHECK_FAILED = true;
				}
			}
		}

		if( S_CHECK_FAILED && !action.context.CTX_FORCE )
			action.exit( "release database file set check failed" );
	}

	private void copyAll( ActionBase action , FileSet[] P_ALIGNEDDIRLIST ) throws Exception {
		// common
		String S_COMMON_ALIGNEDID = ALIGNED_COMMON;
		action.log( "prepare: =================================== copy common id=" + S_COMMON_ALIGNEDID + " ..." );
		
		LocalFolder F_TARGETDIR = dstFolder;
		copyCore( action , srcFileSet , S_COMMON_ALIGNEDID , F_TARGETDIR );
		if( action.custom.isCustomDatabase() )
			action.custom.copyCustom( action , srcFileSet , S_COMMON_ALIGNEDID , F_TARGETDIR );

		// aligned
		if( P_ALIGNEDDIRLIST == null )
			return;
		
		for( FileSet aligneddir : P_ALIGNEDDIRLIST ) {
			S_COMMON_ALIGNEDID = aligneddir.dirName;

			action.log( "prepare: =================================== copy aligned dir=" + aligneddir + " id=" + S_COMMON_ALIGNEDID + " ..." );
			
			copyCore( action , aligneddir , S_COMMON_ALIGNEDID , F_TARGETDIR );
			if( action.custom.isCustomDatabase() )
				action.custom.copyCustom( action , aligneddir , S_COMMON_ALIGNEDID , F_TARGETDIR );
		}
	}
	
	private void copyCore( ActionBase action , FileSet P_ALIGNEDSET , String P_ALIGNEDID , LocalFolder P_TARGETDIR ) throws Exception {
		action.log( "preparing core scripts aligned=" + P_ALIGNEDSET.dirName + " ..." );
		LocalFolder scriptDir = P_TARGETDIR.getSubFolder( action , "sql" );
		copyDir( action , P_ALIGNEDSET , P_ALIGNEDID , P_ALIGNEDSET.getDirByPath( action , COREDDL_FOLDER ) , scriptDir );
		copyDir( action , P_ALIGNEDSET , P_ALIGNEDID , P_ALIGNEDSET.getDirByPath( action , COREDML_FOLDER ) , scriptDir );
		copyDir( action , P_ALIGNEDSET , P_ALIGNEDID , P_ALIGNEDSET.getDirByPath( action , COREPRODONLY_FOLDER ) , scriptDir );
		copyDir( action , P_ALIGNEDSET , P_ALIGNEDID , P_ALIGNEDSET.getDirByPath( action , COREUATONLY_FOLDER ) , scriptDir );

		// copy dataload part
		FileSet dataload = P_ALIGNEDSET.getDirByPath( action , DATALOAD_FOLDER );
		LocalFolder dataloadDir = P_TARGETDIR.getSubFolder( action , DATALOAD_FOLDER );
		if( dataload != null )
			copyCtl( action , P_ALIGNEDSET , P_ALIGNEDID , dataload , dataloadDir );
	}

	private void copyCtl( ActionBase action , FileSet P_ALIGNEDSET , String P_ALIGNEDID , FileSet P_CTLFROM , LocalFolder P_CTLTO ) throws Exception {
		P_CTLTO.ensureExists( action );

		// regional tail
		String F_REGIONALINDEX = "";
		if( P_ALIGNEDSET.dirName.equals( REGIONAL_FOLDER ) )
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
	
	private void checkDir( ActionBase action , FileSet P_ALIGNEDSET , String P_ALIGNEDID , String P_DIR , String P_TYPE , String P_SCHEMALIST ) throws Exception {
		FileSet dir = P_ALIGNEDSET.getDirByPath( action , P_DIR );
		if( dir == null )
			return;

		for( String xbase : Common.getSortedKeys( dir.files ) ) {
			action.trace( "check file=" + xbase );
			
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
					if( !checkSchema( action , P_ALIGNEDSET , F_SCRIPTSCHEMA , P_SCHEMALIST ) ) {
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
						if( !checkSchema( action , P_ALIGNEDSET , F_SCRIPTSCHEMA , P_SCHEMALIST ) ) {
							F_ONEFAILED_MSG = Common.concat( F_ONEFAILED_MSG , "invalid schema for " + xbase + ", permitted schema list - {" + P_SCHEMALIST + "}" , "; " );
							F_ONEFAILED = true;
						}
					}
				}
			}
			else
				action.exitUnexpectedState();

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

	private boolean checkSchema( ActionBase action , FileSet P_ALIGNEDSET , String P_SCHEMA , String P_SCHEMALIST ) throws Exception {
		if( P_SCHEMA.isEmpty() )
			return( false );

		// exact if regional or common
		if( P_ALIGNEDSET == srcFileSet || P_ALIGNEDSET.dirName.equals( REGIONAL_FOLDER ) ) {
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

	private void copyDir( ActionBase action , FileSet P_ALIGNEDSET , String P_ALIGNEDID , FileSet SQL_SRC_DIR , LocalFolder SQL_DST_DIR ) throws Exception {
		if( SQL_SRC_DIR == null )
			return;
		
		if( !srcFolder.checkFolderExists( action , SQL_SRC_DIR.dirPath ) ) {
			action.debug( SQL_SRC_DIR.dirPath + " is not found. Skipped." );
			return;
		}

		String SQL_PREFIX = getSqlIndexPrefix( action , SQL_SRC_DIR.dirPath , P_ALIGNEDID );

		SQL_DST_DIR.ensureExists( action );
		action.debug( "prepare/copy " + SQL_SRC_DIR.dirPath + " ..." );

		// process apply scripts
		for( String x : Common.getSortedKeys( SQL_SRC_DIR.files ) ) {
			if( !x.endsWith( ".sql" ) )
				continue;
			
			String[] items = Common.splitDashed( x );
			String xrindex = items[0];
			String xrschema = items[1];
			String xrtail = x.substring( xrindex.length() + 1 + xrschema.length() + 1 );
			
			// regional tail
			String F_REGIONALINDEX;
			if( P_ALIGNEDSET.dirName.equals( REGIONAL_FOLDER ) || xrschema.indexOf( "RR" ) >= 0 )
				F_REGIONALINDEX = "RR";
			else
				F_REGIONALINDEX = "ZZ";

			String newName = SQL_PREFIX + "-" + F_REGIONALINDEX + "-" + xrindex + "-" + xrschema + "-" + xrtail;
			copySql( action , SQL_SRC_DIR , x , SQL_DST_DIR , newName );
		}

		// process rollback scripts
		FileSet srcRollback = SQL_SRC_DIR.getDirByPath( action , ROLLBACK_FOLDER );
		if( srcRollback != null ) {
			LocalFolder dstRollback = SQL_DST_DIR.getSubFolder( action , DistStorage.ROLLBACK_FOLDER );
			dstRollback.ensureExists( action );

			for( String x : Common.getSortedKeys( srcRollback.files ) ) {
				if( !x.endsWith( ".sql" ) )
					continue;
			
				String xrindex = Common.getListItem( x , "-" , 0 );
				String xrschema = Common.getListItem( x , "-" , 1 );
				String xrtail = x.substring( xrindex.length() + 1 + xrschema.length() + 1 );
			
				// regional tail
				String F_REGIONALINDEX;
				if( P_ALIGNEDSET.dirName.equals( REGIONAL_FOLDER ) || xrschema.indexOf( "RR" ) >= 0 )
					F_REGIONALINDEX = "RR";
				else
					F_REGIONALINDEX = "ZZ";

				String newName = SQL_PREFIX + "-" + F_REGIONALINDEX + "-" + xrindex + "-" + xrschema + "-" + xrtail;
				copySql( action , srcRollback , x , dstRollback , newName );
			}
		}
	}

	private void moveErrors( ActionBase action , FileSet P_ALIGNEDSET , String P_ALIGNEDID , String P_PATH , String P_COMMENT ) throws Exception {
		if( !action.context.CTX_MOVE_ERRORS ) {
			action.log( "errors in " + P_PATH + ": " + P_COMMENT );
			return;
		}

		action.log( "moving " + P_PATH + " to errors folder ..." );

		SourceStorage sourceStorage = action.artefactory.getSourceStorage( action );
		String movePath = Common.getPath( P_ALIGNEDSET.dirPath , P_PATH );
		sourceStorage.moveReleaseDatabaseFilesToErrors( action , errorFolder , distStorage , dbDelivery , movePath , P_COMMENT );
	}

	private boolean checkSql( ActionBase action , FileSet P_ALIGNEDSET , String P_SCRIPT ) throws Exception {
		LocalFolder scriptFolder = srcFolder.getSubFolder( action , P_ALIGNEDSET.dirPath );
		String schemaName = Common.getListItem( P_SCRIPT , "-" , 1 );
		MetaDatabaseSchema schema = database.getSchema( action , schemaName );
		
		if( !schema.specific.validateScriptContent( action , scriptFolder , P_SCRIPT ) ) {
			S_ERROR_MSG = "invalid script content";
			return( false );
		}
		
		// check if regional
		if( P_ALIGNEDSET.dirName.equals( REGIONAL_FOLDER ) ) {
			String S_SPECIFIC_COMMENT = schema.specific.getComments( action , "REGIONS " , scriptFolder , P_SCRIPT ); 
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

	public String getSqlIndexPrefix( ActionBase action , String P_FORLDERNAME , String P_ALIGNEDID ) throws Exception {
		String F_FOLDERNAME = Common.replace( P_FORLDERNAME , "/" , "." );
		String F_FOLDERBASE = Common.getPartBeforeFirst( F_FOLDERNAME , "." );

		String X_ALIGNED = "a" + P_ALIGNEDID;
		String X_TYPE = null;
		String X_INSTANCE = "main";
		
		String S_SQL_DIRID = "";
		if( F_FOLDERBASE.equals( COREDDL_FOLDER ) )
			X_TYPE = "00";
		else if( F_FOLDERBASE.equals( COREDML_FOLDER ) )
			X_TYPE = "01";
		else if( F_FOLDERBASE.equals( COREPRODONLY_FOLDER ) || F_FOLDERBASE.equals( COREUATONLY_FOLDER ) )
			X_TYPE = "02";
		else if( F_FOLDERBASE.equals( DATALOAD_FOLDER ) )
			X_TYPE = "04";
		else
			action.exit( "invalid database folder=" + P_FORLDERNAME );
		
		S_SQL_DIRID = X_ALIGNED + "-t" + X_TYPE + "-i" + X_INSTANCE;
		return( S_SQL_DIRID );
	}

}
