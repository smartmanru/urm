package org.urm.engine.storage;

import java.io.File;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.DBEnumOSType;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.shell.Account;
import org.urm.meta.ProductMeta;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.product.MetaProductBuildSettings;
import org.urm.meta.product.MetaProductCoreSettings;
import org.urm.meta.product.MetaSourceProject;

public class Artefactory {

	public LocalFolder workFolder;
	
	public Artefactory( LocalFolder workFolder ) {
		this.workFolder = workFolder;
	}

	public void createWorkFolder() throws Exception {
		if( workFolder != null ) {
			File file = new File( workFolder.folderPath );
			file.mkdirs();
		}
	}
	
	public LocalFolder getAnyFolder( ActionBase action , String dirname ) throws Exception {
		action.checkRequired( dirname , "dirname" );
		if( dirname.equals( "/" ) )
			action.exit0( _Error.RootNotPermitted0 , "/ is not permitted for operations" );
		
		return( new LocalFolder( dirname , action.isLocalWindows() ) );
	}

	public LocalFolder getTmpFolder( ActionBase action ) throws Exception {
		return( workFolder.getSubFolder( action , "urm.tmp" ) );
	}
	
	public LocalFolder getWorkFolder( ActionBase action ) throws Exception {
		return( workFolder );
	}

	public LocalFolder getWorkFolder( ActionBase action , String name ) throws Exception {
		return( workFolder.getSubFolder( action , name ) );
	}
	
	public String getWorkPath( ActionBase action , String name ) throws Exception {
		return( workFolder.getFilePath( action , name ) );
	}
	
	public LocalFolder getArtefactFolder( ActionBase action , Meta meta ) throws Exception {
		return( getArtefactFolder( action , action.shell.account.osType , meta , "" ) );
	}
	
	public LocalFolder getArtefactFolder( ActionBase action , DBEnumOSType osType , Meta meta , String FOLDER ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings( meta );
		if( build.CONFIG_ARTEFACTDIR.isEmpty() )
			action.exit0( _Error.MissingArtefactDir0 , "Missing artefact directory in product build configuration" );
		if( build.CONFIG_APPVERSION.isEmpty() )
			action.exit0( _Error.MissingAppVersion0 , "Missing application version in product build configuration" );
		
		MetaProductCoreSettings core = meta.getProductCoreSettings();
		String artefactDir = Common.getPath( build.CONFIG_ARTEFACTDIR , build.CONFIG_APPVERSION , FOLDER );
		String finalPath = core.getTargetPath( osType , artefactDir );
		
		LocalFolder folder = getAnyFolder( action , finalPath );
		folder.ensureExists( action );
		return( folder );
	}
	
	public Dist getDistStorageByLabel( ActionBase action , Meta meta , String RELEASELABEL ) throws Exception {
		action.checkRequired( RELEASELABEL , "RELEASELABEL" );

		DistRepository repo = getDistRepository( action , meta );
		Dist storage = repo.getDistByLabel( action , RELEASELABEL );
		return( storage );
	}
	
	public DistRepository getDistRepository( ActionBase action , Meta meta ) throws Exception {
		ProductMeta storage = meta.getStorage();
		DistRepository repo = storage.getDistRepository();
		return( repo );
	}
	
	public BaseRepository getBaseRepository( ActionBase action ) throws Exception {
		BaseRepository repo = BaseRepository.getBaseRepository( action , this );
		return( repo );
	}
	
	public MonitoringStorage getMonitoringStorage( ActionBase action ) throws Exception {
		return( new MonitoringStorage( this , workFolder ) );
	}

	public NexusStorage getNexusStorage( ActionBase action , Integer NEXUS_RESOURCE , Meta meta , String repository ) throws Exception {
		action.checkRequired( repository , "repository" );
		return( new NexusStorage( this , meta , workFolder , NEXUS_RESOURCE , repository ) );
	}

	public NexusStorage getNexusStorage( ActionBase action , Integer NEXUS_RESOURCE , Meta meta , String repository , LocalFolder folder ) throws Exception {
		action.checkRequired( repository , "repository" );
		return( new NexusStorage( this , meta , folder , NEXUS_RESOURCE , repository ) );
	}

	public NexusStorage getDefaultNexusStorage( ActionBase action , Integer NEXUS_RESOURCE , Meta meta ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings( meta );
		return( new NexusStorage( this , meta , workFolder , NEXUS_RESOURCE , build.CONFIG_NEXUS_REPO ) );
	}
	
	public NexusStorage getDefaultNexusStorage( ActionBase action , Integer NEXUS_RESOURCE , Meta meta , LocalFolder folder ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings( meta );
		return( new NexusStorage( this , meta , folder , NEXUS_RESOURCE , build.CONFIG_NEXUS_REPO ) );
	}
	
	public NexusStorage getDefaultNugetStorage( ActionBase action , Integer NEXUS_RESOURCE , Meta meta , LocalFolder folder ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings( meta );
		return( new NexusStorage( this , meta , folder , NEXUS_RESOURCE , build.CONFIG_NEXUS_REPO + "-nuget" ) );
	}
	
	public NexusStorage getThirdpartyNexusStorage( ActionBase action , Integer NEXUS_RESOURCE , Meta meta ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings( meta );
		return( new NexusStorage( this , meta , workFolder , NEXUS_RESOURCE , build.CONFIG_NEXUS_REPO_THIRDPARTY ) );
	}
	
	public NexusStorage getThirdpartyNexusStorage( ActionBase action , Integer NEXUS_RESOURCE , Meta meta , LocalFolder folder ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings( meta );
		return( new NexusStorage( this , meta , folder , NEXUS_RESOURCE , build.CONFIG_NEXUS_REPO_THIRDPARTY ) );
	}
	
	public SourceStorage getSourceStorage( ActionBase action , Meta meta ) throws Exception {
		return( getSourceStorage( action , meta , workFolder ) );
	}
	
	public SourceStorage getSourceStorage( ActionBase action , Meta meta , LocalFolder downloadFolder ) throws Exception {
		return( new SourceStorage( this , meta , downloadFolder ) );
	}
	
	public MetadataStorage getMetadataStorage( ActionBase action , Meta meta ) throws Exception {
		return( new MetadataStorage( this , meta ) );
	}

	public LogStorage getReleaseBuildLogStorage( ActionBase action , Meta meta , String release ) throws Exception {
		action.checkRequired( release , "release" );
		LogStorage storage = new LogStorage( this , meta );
		storage.prepareReleaseBuildLogFolder( action , release );
		return( storage );
	}

	public LogStorage getTagBuildLogStorage( ActionBase action , Meta meta , String TAG ) throws Exception {
		action.checkRequired( TAG , "TAG" );
		LogStorage storage = new LogStorage( this , meta );
		storage.prepareTagBuildLogFolder( action , TAG );
		return( storage );
	}

	public LogStorage getDatabaseLogStorage( ActionBase action , Meta meta , String release ) throws Exception {
		LogStorage storage = new LogStorage( this , meta );
		storage.prepareDatabaseLogFolder( action , release );
		return( storage );
	}

	public BuildStorage getEmptyBuildStorage( ActionBase action , MetaSourceProject sourceProject ) throws Exception {
		String MODE = action.context.getBuildModeName();
		if( MODE.isEmpty() ) 
			MODE = "default";

		String PATCHDIR = getWorkPath( action , MODE );
		action.shell.ensureDirExists( action , PATCHDIR );
		String PATCHPATH = PATCHDIR + "/" + sourceProject.NAME;
		
		LocalFolder folder = getAnyFolder( action , PATCHPATH );
		folder.removeThis( action );
		
		return( new BuildStorage( this , sourceProject.meta , folder ) );
	}

	public RedistStorage getRedistStorage( ActionBase action , MetaEnvServer server , MetaEnvServerNode node ) throws Exception {
		Account account = action.getNodeAccount( node );
		RedistStorage redist = new RedistStorage( this , account , server , node );
		return( redist );
	}

	public RedistStorage getRedistStorage( ActionBase action , Account account ) throws Exception {
		RedistStorage redist = new RedistStorage( this , account );
		return( redist );
	}

	public RuntimeStorage getRootRuntimeStorage( ActionBase action , MetaEnvServer server , MetaEnvServerNode node , boolean adm ) throws Exception {
		Account account = action.getNodeAccount( node );
		if( adm )
			account = account.getRootAccount( action );
		return( new RuntimeStorage( this , account , server , node ) );
	}

	public RuntimeStorage getRuntimeStorage( ActionBase action , MetaEnvServer server , MetaEnvServerNode node ) throws Exception {
		Account account = action.getNodeAccount( node );
		return( new RuntimeStorage( this , account , server , node ) );
	}

	public HiddenFiles getHiddenFiles( Meta meta ) throws Exception {
		return( new HiddenFiles( this , meta ) );
	}

	public UrmStorage getUrmStorage() throws Exception {
		return( new UrmStorage( this ) );
	}

	public VersionInfoStorage getVersionInfoStorage( ActionBase action , Account account ) throws Exception {
		RedistStorage redist = getRedistStorage( action , account ); 
		return( new VersionInfoStorage( redist ) );
	}

}
