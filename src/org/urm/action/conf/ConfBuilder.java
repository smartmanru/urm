package org.urm.action.conf;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import org.urm.Common;
import org.urm.ConfReader;
import org.urm.PropertySet;
import org.urm.action.ActionBase;
import org.urm.dist.Dist;
import org.urm.dist.ReleaseDelivery;
import org.urm.dist.ReleaseTarget;
import org.urm.meta.MetaDistrConfItem;
import org.urm.meta.MetaEnvServer;
import org.urm.meta.MetaEnvServerNode;
import org.urm.meta.Metadata;
import org.urm.storage.Artefactory;
import org.urm.storage.FileSet;
import org.urm.storage.HiddenFiles;
import org.urm.storage.LocalFolder;
import org.urm.storage.SourceStorage;

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

	public String createConfDiffFile( Dist release , ReleaseDelivery delivery ) throws Exception {
		// copy conf from release
		LocalFolder releaseFolder = artefactory.getWorkFolder( action , "release.delivery.conf" );
		releaseFolder.recreateThis( action );
		release.copyDistConfToFolder( action , delivery , releaseFolder );
		
		// copy conf from product
		action.debug( "compare with product configuration ..." );
		LocalFolder prodFolder = artefactory.getWorkFolder( action , "prod.delivery.conf" );
		prodFolder.recreateThis( action );
		SourceStorage storage = artefactory.getSourceStorage( action , prodFolder );
		
		for( ReleaseTarget releaseComp : delivery.getConfItems( action ).values() ) {
			ConfSourceFolder sourceFolder = new ConfSourceFolder( meta );
			sourceFolder.createReleaseConfigurationFolder( action , releaseComp );
			storage.downloadProductConfigItem( action , sourceFolder , prodFolder );
		}
		
		// create diff
		String diffFile = Dist.CONFDIFF_FILENAME;
		releaseFolder.removeFiles( action , diffFile );

		// compare file sets
		FileSet releaseSet = releaseFolder.getFileSet( action );
		FileSet prodSet = prodFolder.getFileSet( action );
		
		ConfDiffSet diff = new ConfDiffSet( releaseSet , prodSet , null , true ); 
		diff.calculate( action , release.release );
		
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
			action.info( "run " + runScript );
			action.session.custom( action , live.folderPath , "chmod 744 " + runScript + "; ./" + runScript + " " + 
				server.dc.env.ID + " " + server.dc.NAME + " " + server.NAME + " " + node.POS );
		}
		
		// copy explicit environment directories
		String envFolder = "template-" + server.dc.env;
		if( live.checkFolderExists( action , envFolder ) )
			live.copyDirContent( action , live.getSubFolder( action , envFolder ) );

		live.removeFiles( action , "template-* " + CONFIGURE_SCRIPT );
		
		// copy hidden environment directories
		HiddenFiles hidden = artefactory.getHiddenFiles();
		hidden.copyHiddenConf( action , server , confItem , live );
		
		// process parameters
		String[] files = ( action.context.account.isLinux() )? getLinuxFiles( live , confItem ) : getWindowsFiles( live , confItem ); 
		for( String file : files ) {
			if( file.startsWith( "./" ) )
				file = file.substring( 2 );
			configureFile( live , file , node , null , StandardCharsets.UTF_8 );
		}
	}

	private String[] getLinuxFiles( LocalFolder live , MetaDistrConfItem confItem ) throws Exception {
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
		return( files );
	}
	
	private String[] getWindowsFiles( LocalFolder live , MetaDistrConfItem confItem ) throws Exception {
		String[] extOptions = meta.getConfigurableExtensions( action );
		String[] extConf = Common.splitSpaced( confItem.EXTCONF );
		FileSet files = live.getFileSet( action );
		
		List<String> filtered = new LinkedList<String>(); 
		for( String file : files.fileList ) {
			boolean confRun = false;
			for( String s : extOptions ) {
				if( file.endsWith( s ) ) {
					confRun = true;
					break;
				}
			}
			for( String s : extConf ) {
				if( file.endsWith( s ) ) {
					confRun = true;
					break;
				}
			}
			if( confRun )
				filtered.add( file );
		}
		
		return( filtered.toArray( new String[0] ) );
	}
	
	public void configureFolder( ActionBase action , LocalFolder folder , MetaEnvServer server , PropertySet props , Charset charset ) throws Exception {
		action.trace( "parse configuration files in folder=" + folder.folderPath + " ..." );
		FileSet files = folder.getFileSet( action );
		if( props == null )
			props = server.properties;
		
		for( String file : files.fileList )
			configureFile( folder , file , server , props , charset );
	}

	public void configureFolder( ActionBase action , LocalFolder folder , MetaEnvServerNode node , PropertySet props ) throws Exception {
		configureFolder( action , folder , node , props , StandardCharsets.UTF_8 );
	}
	
	public void configureFolder( ActionBase action , LocalFolder folder , MetaEnvServerNode node , PropertySet props , Charset charset ) throws Exception {
		action.trace( "parse configuration files in folder=" + folder.folderPath + " ..." );
		FileSet files = folder.getFileSet( action );
		
		if( props == null )
			props = node.properties;
		
		for( String file : files.fileList )
			configureFile( folder , file , node , props , charset );
	}
	
	public void configureFile( LocalFolder live , String file , MetaEnvServer server , PropertySet props , Charset charset ) throws Exception {
		action.trace( "parse file=" + file + " ..." );
		String filePath = live.getFilePath( action , file );
		List<String> fileLines = ConfReader.readFileLines( action , filePath , charset );
		
		if( props == null )
			props = server.properties;
		
		boolean changed = false;
		for( int k = 0; k < fileLines.size(); k++ ) {
			String s = fileLines.get( k );
			String res = props.processFinalValue( action , s , server.osType );
			if( res != null ) {
				fileLines.set( k , res );
				changed = true;
			}
		}

		if( changed )
			Common.createFileFromStringList( filePath , fileLines , charset );
	}
	
	public void configureFile( LocalFolder live , String file , MetaEnvServerNode node , PropertySet props , Charset charset ) throws Exception {
		action.trace( "parse file=" + file + " ..." );
		String filePath = live.getFilePath( action , file );
		List<String> fileLines = ConfReader.readFileLines( action , filePath , charset );
		
		if( props == null )
			props = node.properties;
		
		boolean changed = false;
		for( int k = 0; k < fileLines.size(); k++ ) {
			String s = fileLines.get( k );
			String res = props.processFinalValue( action , s , node.server.osType );
			if( res != null ) {
				fileLines.set( k , res );
				changed = true;
			}
		}

		if( changed )
			Common.createFileFromStringList( filePath , fileLines , charset );
	}
	
}
