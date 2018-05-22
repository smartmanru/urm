package org.urm.action.conf;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.SecurityService;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.ReleaseDistScopeDelivery;
import org.urm.engine.dist.ReleaseDistScopeDeliveryItem;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.ObjectMeta;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.properties.PropertyValue;
import org.urm.engine.shell.Shell;
import org.urm.engine.storage.Artefactory;
import org.urm.engine.storage.FileSet;
import org.urm.engine.storage.HiddenFiles;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.SourceStorage;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaProductSettings;

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

	public String createConfDiffFile( Dist dist , ReleaseDistScopeDelivery delivery ) throws Exception {
		// copy conf from release
		LocalFolder releaseFolder = artefactory.getWorkFolder( action , "release.delivery.conf" );
		releaseFolder.recreateThis( action );
		dist.copyDistConfToFolder( action , delivery , releaseFolder );
		
		// copy conf from product
		action.debug( "compare with product configuration ..." );
		LocalFolder prodFolder = artefactory.getWorkFolder( action , "prod.delivery.conf" );
		prodFolder.recreateThis( action );
		SourceStorage storage = artefactory.getSourceStorage( action , dist.meta , prodFolder );
		
		for( ReleaseDistScopeDeliveryItem releaseComp : delivery.getItems() ) {
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
		
		ConfDiffSet diff = new ConfDiffSet( dist.meta , releaseSet , prodSet , null , true ); 
		diff.calculate( action , dist.release );
		
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
				server.sg.env.NAME + " " + server.sg.NAME + " " + server.NAME + " " + node.POS , Shell.WAIT_DEFAULT );
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
		ObjectProperties ops = node.getProperties();
		ops = getSecuredOps( node , ops );
		
		String[] files = ( action.context.account.isLinux() )? getLinuxFiles( live , confItem ) : getWindowsFiles( live , confItem ); 
		for( String file : files ) {
			if( file.startsWith( "./" ) )
				file = file.substring( 2 );
			configureFileInternal( live , file , ops , StandardCharsets.UTF_8 , node.server.isWindows() );
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
				extCompOptions + " \\) | tr \"\\n\" \" \"`; if [ \"$F_FILES\" != \"\" ]; then grep -l \"@.*@\" $F_FILES; fi" , Shell.WAIT_DEFAULT );
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
	
	public void configureFolder( ActionBase action , LocalFolder folder , MetaEnvServer server , ObjectProperties ops , Charset charset ) throws Exception {
		action.trace( "parse configuration files in folder=" + folder.folderPath + " ..." );
		FileSet files = folder.getFileSet( action );
		if( ops == null )
			ops = server.getProperties();
		ops = getSecuredOps( server , ops );
		
		for( String file : files.fileList )
			configureFileInternal( folder , file , ops , charset , server.isWindows() );
	}

	public void configureFolder( ActionBase action , LocalFolder folder , MetaEnvServerNode node , ObjectProperties ops ) throws Exception {
		configureFolder( action , folder , node , ops , StandardCharsets.UTF_8 );
	}
	
	public void configureFolder( ActionBase action , LocalFolder folder , MetaEnvServerNode node , ObjectProperties ops , Charset charset ) throws Exception {
		action.trace( "parse configuration files in folder=" + folder.folderPath + " ..." );
		FileSet files = folder.getFileSet( action );
		
		if( ops == null )
			ops = node.getProperties();
		ops = getSecuredOps( node , ops );
		
		for( String file : files.fileList )
			configureFileInternal( folder , file , ops , charset , node.server.isWindows() );
	}
	
	public void configureFile( LocalFolder live , String file , MetaEnvServer server , ObjectProperties ops , Charset charset ) throws Exception {
		if( ops == null )
			Common.exitUnexpected();
		configureFileInternal( live , file , ops , charset , server.isWindows() );
	}
	
	private void configureFileInternal( LocalFolder live , String file , ObjectProperties ops , Charset charset , boolean isWindows ) throws Exception {
		action.trace( "parse file=" + file + " ..." );
		String filePath = live.getFilePath( action , file );
		List<String> fileLines = action.readFileLines( filePath , charset );
		
		boolean changed = false;
		for( int k = 0; k < fileLines.size(); k++ ) {
			String s = fileLines.get( k );
			PropertyValue res = ops.getFinalValue( s , isWindows , true , false );
			if( res != null ) {
				fileLines.set( k , res.getFinalValue() );
				changed = true;
			}
		}

		if( changed )
			Common.createFileFromStringList( action.execrc , filePath , fileLines , charset );
	}

	public ObjectProperties getSecuredOps( MetaEnvServer server , ObjectProperties ops ) throws Exception {
		ObjectProperties tops = ops.copy( ops.getParent() );
		tops.recalculateProperties();
		
		MetaProductSettings settings = server.meta.getProductSettings();
		ObjectProperties pops = settings.getParameters();
		ObjectMeta om = pops.getMeta();
		PropertyEntity entity = om.getCustomEntity();
		
		SecurityService ss = action.engine.getSecurity();
		for( EntityVar var : entity.getVars() ) {
			if( var.isSecured() ) {
				String value = ss.getEnvServerVarEffective( action , server , var );
				tops.setManualStringProperty( var.NAME , value );
			}
		}
		
		return( tops );
	}
	
	public ObjectProperties getSecuredOps( MetaEnvServerNode node , ObjectProperties ops ) throws Exception {
		ObjectProperties tops = ops.copy( ops.getParent() );
		tops.recalculateProperties();
		
		MetaProductSettings settings = node.meta.getProductSettings();
		ObjectProperties pops = settings.getParameters();
		ObjectMeta om = pops.getMeta();
		PropertyEntity entity = om.getCustomEntity();
		
		SecurityService ss = action.engine.getSecurity();
		for( EntityVar var : entity.getVars() ) {
			if( var.isSecured() ) {
				String value = ss.getEnvServerNodeVarEffective( action , node , var );
				tops.setManualStringProperty( var.NAME , value );
			}
		}
		
		return( tops );
	}
	
}
