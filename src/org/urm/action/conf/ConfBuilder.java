package org.urm.action.conf;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.PropertySet;
import org.urm.common.PropertyValue;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.ReleaseDelivery;
import org.urm.engine.dist.ReleaseTarget;
import org.urm.engine.storage.Artefactory;
import org.urm.engine.storage.FileSet;
import org.urm.engine.storage.HiddenFiles;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.SourceStorage;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;

public class ConfBuilder {

	Meta meta;
	ActionBase action;
	Artefactory artefactory;

	static String CONFIGURE_SCRIPT = "configure.sh";
	
	public ConfBuilder( ActionBase action , Meta meta ) {
		this.action = action;
		this.artefactory = action.artefactory;
		this.meta = meta;
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
		SourceStorage storage = artefactory.getSourceStorage( action , delivery.meta , prodFolder );
		
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
		
		ConfDiffSet diff = new ConfDiffSet( delivery.meta , releaseSet , prodSet , null , true ); 
		diff.calculate( action , release.release );
		
		String filePath = releaseFolder.getFilePath( action , diffFile ); 
		diff.save( action , filePath );
		
		return( filePath );
	}

	public void configureLiveComponent( LocalFolder live , MetaDistrConfItem confItem , MetaEnvServer server , MetaEnvServerNode node ) throws Exception {
		// copy hidden environment directories
		HiddenFiles hidden = artefactory.getHiddenFiles( confItem.meta );
		hidden.copyHiddenConf( action , server , confItem , live );
	}
	
	public void configureComponent( LocalFolder template , LocalFolder live , MetaDistrConfItem confItem , MetaEnvServer server , MetaEnvServerNode node ) throws Exception {
		// copy source templates
		live.copyDirContent( action , template );

		// run preconfigure
		String runScript = CONFIGURE_SCRIPT;
		if( live.checkFileExists( action , runScript ) ) {
			action.info( "run " + runScript );
			action.shell.custom( action , live.folderPath , "chmod 744 " + runScript + "; ./" + runScript + " " + 
				server.sg.env.ID + " " + server.sg.NAME + " " + server.NAME + " " + node.POS );
		}
		
		// copy explicit environment directories
		String envFolder = "template-" + server.sg.env;
		if( live.checkFolderExists( action , envFolder ) )
			live.copyDirContent( action , live.getSubFolder( action , envFolder ) );

		live.removeFiles( action , "template-* " + CONFIGURE_SCRIPT );
		
		// copy hidden environment directories
		HiddenFiles hidden = artefactory.getHiddenFiles( confItem.meta );
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
		String extOptions = Meta.getConfigurableExtensionsFindOptions( action );
		String extCompOptions = extOptions;
		for( String mask : Common.splitSpaced( confItem.EXTCONF ) ) {
			if( !extCompOptions.isEmpty() )
				extCompOptions += " -o -name ";
			extCompOptions += Common.getQuoted( mask );
		}
		String list = action.shell.customGetValue( action , live.folderPath , "F_FILES=`find . -type f -a \\( " + 
				extCompOptions + " \\) | tr \"\\n\" \" \"`; if [ \"$F_FILES\" != \"\" ]; then grep -l \"@.*@\" $F_FILES; fi" );
		String[] files = Common.splitLines( list );
		return( files );
	}
	
	private String[] getWindowsFiles( LocalFolder live , MetaDistrConfItem confItem ) throws Exception {
		String[] extOptions = Meta.getConfigurableExtensions( action );
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
			props = server.getProperties();
		
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
			props = node.getProperties();
		
		for( String file : files.fileList )
			configureFile( folder , file , node , props , charset );
	}
	
	public void configureFile( LocalFolder live , String file , MetaEnvServer server , PropertySet props , Charset charset ) throws Exception {
		action.trace( "parse file=" + file + " ..." );
		String filePath = live.getFilePath( action , file );
		List<String> fileLines = action.readFileLines( filePath , charset );
		
		if( props == null )
			props = server.getProperties();
		
		boolean changed = false;
		for( int k = 0; k < fileLines.size(); k++ ) {
			String s = fileLines.get( k );
			PropertyValue res = props.getFinalPropertyValue( s , server.isWindows() , true , false );
			if( res != null ) {
				fileLines.set( k , res.getFinalValue() );
				changed = true;
			}
		}

		if( changed )
			Common.createFileFromStringList( action.execrc , filePath , fileLines , charset );
	}
	
	public void configureFile( LocalFolder live , String file , MetaEnvServerNode node , PropertySet props , Charset charset ) throws Exception {
		action.trace( "parse file=" + file + " ..." );
		String filePath = live.getFilePath( action , file );
		List<String> fileLines = action.readFileLines( filePath , charset );
		
		if( props == null )
			props = node.getProperties();
		
		boolean changed = false;
		for( int k = 0; k < fileLines.size(); k++ ) {
			String s = fileLines.get( k );
			PropertyValue res = props.getFinalPropertyValue( s , node.server.isWindows() , true , false );
			if( res != null ) {
				fileLines.set( k , res.getFinalValue() );
				changed = true;
			}
		}

		if( changed )
			Common.createFileFromStringList( action.execrc , filePath , fileLines , charset );
	}
	
}
