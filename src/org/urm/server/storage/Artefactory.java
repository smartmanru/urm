package org.urm.server.storage;

import org.urm.common.Common;
import org.urm.server.ServerTransaction;
import org.urm.server.action.ActionBase;
import org.urm.server.dist.Dist;
import org.urm.server.dist.DistRepository;
import org.urm.server.meta.FinalMetaSystem;
import org.urm.server.meta.MetaEnvServer;
import org.urm.server.meta.MetaEnvServerNode;
import org.urm.server.meta.MetaMonitoring;
import org.urm.server.meta.MetaSourceProject;
import org.urm.server.meta.Metadata;
import org.urm.server.shell.Account;
import org.urm.server.vcs.GenericVCS;
import org.urm.server.vcs.GitVCS;
import org.urm.server.vcs.SubversionVCS;

public class Artefactory {

	public Metadata meta;
	public LocalFolder workFolder;
	
	public Artefactory( Metadata meta , LocalFolder workFolder ) {
		this.meta = meta;
		this.workFolder = workFolder;
	}

	public LocalFolder getAnyFolder( ActionBase action , String dirname ) throws Exception {
		action.checkRequired( dirname , "dirname" );
		if( dirname.equals( "/" ) )
			action.exit( "/ is not permitted for operations" );
		
		return( new LocalFolder( dirname , action.isLocalWindows() ) );
	}

	public LocalFolder getTmpFolder( ActionBase action ) throws Exception {
		String folder = Common.getPath( action.session.execrc.userHome , "urm.tmp" );
		return( getAnyFolder( action , folder ) );
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
	
	public LocalFolder getDownloadFolder( ActionBase action ) throws Exception {
		LocalFolder folder = getAnyFolder( action , meta.product.CONFIG_ARTEFACTDIR );
		folder.ensureExists( action );
		return( folder );
	}
	
	public LocalFolder getArtefactFolder( ActionBase action , String FOLDER ) throws Exception {
		action.checkRequired( FOLDER , "FOLDER" );
		
		String finalDir = meta.product.CONFIG_ARTEFACTDIR + "/" + FOLDER;
		LocalFolder folder = getAnyFolder( action , finalDir );
		folder.ensureExists( action );
		return( folder );
	}
	
	public Dist getDistProdStorage( ActionBase action ) throws Exception {
		return( getDistStorageByLabel( action , "prod" ) );
	}
	
	public Dist getDistStorageByLabel( ActionBase action , String RELEASELABEL ) throws Exception {
		action.checkRequired( RELEASELABEL , "RELEASELABEL" );

		DistRepository repo = getDistRepository( action );
		Dist storage = repo.getDistByLabel( action , RELEASELABEL );
		return( storage );
	}
	
	public Dist createDist( ActionBase action , String RELEASELABEL ) throws Exception {
		action.checkRequired( RELEASELABEL , "RELEASELABEL" );
		DistRepository repo = getDistRepository( action );
		Dist storage = repo.createDist( action , RELEASELABEL );
		return( storage );
	}
	
	public DistRepository getDistRepository( ActionBase action ) throws Exception {
		DistRepository repo = DistRepository.getDistRepository( action , this );
		return( repo );
	}
	
	public BaseRepository getBaseRepository( ActionBase action ) throws Exception {
		BaseRepository repo = BaseRepository.getBaseRepository( action , this );
		return( repo );
	}
	
	public MonitoringStorage getMonitoringStorage( ActionBase action , MetaMonitoring mon ) throws Exception {
		return( new MonitoringStorage( this , workFolder , mon ) );
	}

	public NexusStorage getNexusStorage( ActionBase action , String repository ) throws Exception {
		action.checkRequired( repository , "repository" );
		return( new NexusStorage( this , workFolder , repository ) );
	}

	public NexusStorage getNexusStorage( ActionBase action , String repository , LocalFolder folder ) throws Exception {
		action.checkRequired( repository , "repository" );
		return( new NexusStorage( this , folder , repository ) );
	}

	public NexusStorage getDefaultNexusStorage( ActionBase action ) throws Exception {
		return( new NexusStorage( this , workFolder , meta.product.CONFIG_NEXUS_REPO ) );
	}
	
	public NexusStorage getDefaultNexusStorage( ActionBase action , LocalFolder folder ) throws Exception {
		return( new NexusStorage( this , folder , meta.product.CONFIG_NEXUS_REPO ) );
	}
	
	public NexusStorage getDefaultNugetStorage( ActionBase action , LocalFolder folder ) throws Exception {
		return( new NexusStorage( this , folder , meta.product.CONFIG_NEXUS_REPO + "-nuget" ) );
	}
	
	public NexusStorage getThirdpartyNexusStorage( ActionBase action ) throws Exception {
		return( new NexusStorage( this , workFolder , meta.product.CONFIG_NEXUS_REPO_THIRDPARTY ) );
	}
	
	public NexusStorage getThirdpartyNexusStorage( ActionBase action , LocalFolder folder ) throws Exception {
		return( new NexusStorage( this , folder , meta.product.CONFIG_NEXUS_REPO_THIRDPARTY ) );
	}
	
	public SourceStorage getSourceStorage( ActionBase action ) throws Exception {
		return( new SourceStorage( this , workFolder ) );
	}
	
	public SourceStorage getSourceStorage( ActionBase action , LocalFolder downloadFolder ) throws Exception {
		return( new SourceStorage( this , downloadFolder ) );
	}
	
	public MetadataStorage getMetadataStorage( ActionBase action ) throws Exception {
		return( new MetadataStorage( this ) );
	}

	public LogStorage getReleaseBuildLogStorage( ActionBase action , String release ) throws Exception {
		action.checkRequired( release , "release" );
		LogStorage storage = new LogStorage( this );
		storage.prepareReleaseBuildLogFolder( action , release );
		return( storage );
	}

	public LogStorage getTagBuildLogStorage( ActionBase action , String TAG ) throws Exception {
		action.checkRequired( TAG , "TAG" );
		LogStorage storage = new LogStorage( this );
		storage.prepareTagBuildLogFolder( action , TAG );
		return( storage );
	}

	public LogStorage getDatabaseLogStorage( ActionBase action , String release ) throws Exception {
		LogStorage storage = new LogStorage( this );
		storage.prepareDatabaseLogFolder( action , release );
		return( storage );
	}

	public GitMirrorStorage getGitMirrorStorage( ActionBase action , String REPOSITORY ) throws Exception {
		return( getGitMirrorStorage( action , REPOSITORY + ".git" , false ) );
	}

	private GitMirrorStorage getGitMirrorStorage( ActionBase action , String NAME , boolean winBuild ) throws Exception {
		RedistStorage storage;
		Account account = ( winBuild )? action.getWinBuildAccount() : action.shell.account; 
		storage = getRedistStorage( action , account ); 
		
		Folder mirrorFolder = storage.getMirrorFolder( action , winBuild );
		Folder projectFolder = mirrorFolder.getSubFolder( action , NAME );
		if( !projectFolder.checkExists( action ) )
			action.exit( "getGitMirrorStorage: mirror path " + projectFolder.folderPath + " should be created using " + projectFolder.folderPath + "/mirror.sh" );
	
		return( new GitMirrorStorage( this , account , projectFolder , winBuild ) );
	}
	
	public GitMirrorStorage getGitMirrorStorage( ActionBase action , MetaSourceProject sourceProject , boolean build ) throws Exception {
		String REPONAME;
		String path = sourceProject.PATH.replaceAll( "/" , "" );
		REPONAME = path + "-" + sourceProject.PROJECT + ".git";
		boolean winBuild = ( build && sourceProject.getBuilder( action ).equals( "dotnet" ) )? true : false;
		
		return( getGitMirrorStorage( action , REPONAME , winBuild ) );
	}
	
	public BuildStorage getEmptyBuildStorage( ActionBase action , MetaSourceProject sourceProject ) throws Exception {
		String MODE = action.context.getBuildModeName();
		if( MODE.isEmpty() ) 
			MODE = "default";

		String PATCHDIR = getWorkPath( action , MODE );
		action.shell.ensureDirExists( action , PATCHDIR );
		String PATCHPATH = PATCHDIR + "/" + sourceProject.PROJECT;
		
		LocalFolder folder = getAnyFolder( action , PATCHPATH );
		folder.removeThis( action );
		
		return( new BuildStorage( this , folder ) );
	}

	public AuthStorage getAuthStorage( ActionBase action ) throws Exception {
		return( new AuthStorage( this ) );
	}
	
	public GenericVCS getVCS( ActionBase action , String vcsType , boolean build ) throws Exception {
		if( vcsType.equals( "svnold" ) || vcsType.equals( "svn" ) ) {
			AuthStorage auth = getAuthStorage( action );
			String SVNAUTH = auth.getOldSvnAuthParams( action );
			return( new SubversionVCS( action , action.meta.product.CONFIG_SVNOLD_PATH , SVNAUTH ) );
		}
		
		if( vcsType.equals( "svnnew" ) ) {
			AuthStorage auth = getAuthStorage( action );
			String SVNAUTH = auth.getNewSvnAuthParams( action );
			return( new SubversionVCS( action , action.meta.product.CONFIG_SVNOLD_PATH , SVNAUTH ) );
		}
		
		if( vcsType.equals( "git" ) )
			return( new GitVCS( action , build ) );
		
		action.exit( "unknown vcsType=" + vcsType );
		return( null );
	}

	public SubversionVCS getSvnDirect( ActionBase action ) throws Exception {
		String svnUrl = "";
		if( action.meta != null && action.meta.product != null )
			svnUrl = action.meta.product.CONFIG_SVNOLD_PATH;
		
		return( new SubversionVCS( action , svnUrl , "" ) );
	}

	public RedistStorage getRedistStorage( ActionBase action , MetaEnvServer server , MetaEnvServerNode node ) throws Exception {
		Account account = action.getNodeAccount( node );
		RedistStorage redist = new RedistStorage( this , account , server , node );
		return( redist );
	}

	public RedistStorage getRedistStorage( ActionBase action , Account account ) throws Exception {
		RedistStorage redist = new RedistStorage( this , account , null , null );
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

	public HiddenFiles getHiddenFiles() throws Exception {
		return( new HiddenFiles( this ) );
	}

	public UrmStorage getUrmStorage() throws Exception {
		return( new UrmStorage( this ) );
	}

	public VersionInfoStorage getVersionInfoStorage( ActionBase action , Account account ) throws Exception {
		RedistStorage redist = getRedistStorage( action , account ); 
		return( new VersionInfoStorage( redist ) );
	}

	public void deleteSystemResources( ServerTransaction transaction , FinalMetaSystem system , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
	}
	
}
