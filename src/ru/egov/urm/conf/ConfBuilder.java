package ru.egov.urm.conf;

import java.util.List;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.meta.MetaDistrConfItem;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.MetaReleaseDelivery;
import ru.egov.urm.meta.MetaReleaseTarget;
import ru.egov.urm.meta.MetaSourceFolder;
import ru.egov.urm.meta.Metadata;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.storage.Artefactory;
import ru.egov.urm.storage.DistStorage;
import ru.egov.urm.storage.FileSet;
import ru.egov.urm.storage.HiddenFiles;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.SourceStorage;

public class ConfBuilder {

	Metadata meta;
	ActionBase action;
	Artefactory artefactory;

	static String CONFIGURE_SCRIPT = "configure.sh";
	
	public ConfBuilder( ActionBase action ) {
		this.action = action;
		this.artefactory = action.artefactory;
		this.meta = action.meta;
	}

	public String createConfDiffFile( DistStorage release , MetaReleaseDelivery delivery ) throws Exception {
		// copy conf from release
		LocalFolder releaseFolder = artefactory.getWorkFolder( action , "release.delivery.conf" );
		releaseFolder.recreateThis( action );
		release.copyDistConfToFolder( action , delivery , releaseFolder );
		
		// copy conf from product
		action.debug( "compare with product configuration ..." );
		LocalFolder prodFolder = artefactory.getWorkFolder( action , "prod.delivery.conf" );
		prodFolder.recreateThis( action );
		SourceStorage storage = artefactory.getSourceStorage( action , prodFolder );
		for( MetaReleaseTarget releaseComp : delivery.getConfItems( action ).values() ) {
			MetaSourceFolder sourceFolder = new MetaSourceFolder( meta );
			sourceFolder.createReleaseConfigurationFolder( action , releaseComp );
			storage.downloadProductConfigItem( action , sourceFolder , prodFolder );
		}
		
		// create diff
		String diffFile = DistStorage.confDiffFileName;
		releaseFolder.removeFiles( action , diffFile );

		// compare file sets
		FileSet releaseSet = releaseFolder.getFileSet( action );
		FileSet prodSet = prodFolder.getFileSet( action );
		
		ConfDiffSet diff = new ConfDiffSet( releaseSet , prodSet ); 
		diff.calculate( action , release.info );
		
		String filePath = releaseFolder.getFilePath( action , diffFile ); 
		diff.save( action , filePath );
		
		return( filePath );
	}

	public void configureLiveComponent( LocalFolder live , MetaDistrConfItem confItem , MetaEnvServer server , MetaEnvServerNode node ) throws Exception {
		// copy hidden environment directories
		HiddenFiles hidden = artefactory.getHiddenFiles();
		hidden.copyHiddenConf( action , server , confItem , live );
	}
	
	public void configureComponent( LocalFolder template , LocalFolder live , MetaDistrConfItem confItem , MetaEnvServer server , MetaEnvServerNode node ) throws Exception {
		// copy source templates
		live.copyDirContent( action , template );

		// run preconfigure
		String runScript = CONFIGURE_SCRIPT;
		if( live.checkFileExists( action , runScript ) ) {
			action.log( "run " + runScript );
			action.session.custom( action , live.folderPath , "chmod 744 " + runScript + "; ./" + runScript + " " + 
				meta.env.ID + " " + meta.dc.NAME + " " + server.NAME + " " + node.POS );
		}
		
		// copy explicit environment directories
		String envFolder = "template-" + meta.env;
		if( live.checkFolderExists( action , envFolder ) )
			live.copyDirContent( action , live.getSubFolder( action , envFolder ) );

		live.removeFiles( action , "template-* " + CONFIGURE_SCRIPT );
		
		// copy hidden environment directories
		HiddenFiles hidden = artefactory.getHiddenFiles();
		hidden.copyHiddenConf( action , server , confItem , live );
		
		// process parameters
		String extOptions = meta.getConfigurableExtensionsFindOptions( action );
		String extCompOptions = extOptions;
		for( String mask : Common.splitSpaced( confItem.EXTCONF ) ) {
			if( !extCompOptions.isEmpty() )
				extCompOptions += " -o -name ";
			extCompOptions += Common.getQuoted( mask );
		}
		String list = action.session.customGetValue( action , live.folderPath , "F_FILES=`find . -type f -a \\( " + 
				extCompOptions + " \\) | tr \"\\n\" \" \"`; if [ \"$F_FILES\" != \"\" ]; then grep -l \"@.*@\" $F_FILES; fi" );
		String[] files = Common.splitLines( list ); 
		for( String file : files ) {
			if( file.startsWith( "./" ) )
				file = file.substring( 2 );
			parseConfigParameters( live , file , node );
		}
	}

	public void parseConfigParameters( ActionBase action , LocalFolder folder , MetaEnvServer server ) throws Exception {
		action.trace( "parse configuration files in folder=" + folder.folderPath + " ..." );
		FileSet files = folder.getFileSet( action );
		for( String file : files.fileList )
			parseConfigParameters( folder , file , server );
	}
	
	public void parseConfigParameters( LocalFolder live , String file , MetaEnvServer server ) throws Exception {
		action.trace( "parse file=" + file + " ..." );
		String filePath = live.getFilePath( action , file );
		List<String> fileLines = ConfReader.readFileLines( action , filePath );
		
		boolean changed = false;
		for( int k = 0; k < fileLines.size(); k++ ) {
			String s = fileLines.get( k );
			String res = server.properties.processValue( action , s );
			if( res != null ) {
				fileLines.set( k , res );
				changed = true;
			}
		}

		if( changed )
			Common.createFileFromStringList( filePath , fileLines );
	}
	
	private void parseConfigParameters( LocalFolder live , String file , MetaEnvServerNode node ) throws Exception {
		action.trace( "parse file=" + file + " ..." );
		String filePath = live.getFilePath( action , file );
		List<String> fileLines = ConfReader.readFileLines( action , filePath );
		
		boolean changed = false;
		for( int k = 0; k < fileLines.size(); k++ ) {
			String s = fileLines.get( k );
			String res = node.properties.processValue( action , s );
			if( res != null ) {
				fileLines.set( k , res );
				changed = true;
			}
		}

		if( changed )
			Common.createFileFromStringList( filePath , fileLines );
	}
	
}
