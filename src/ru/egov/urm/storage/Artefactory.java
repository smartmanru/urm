package ru.egov.urm.storage;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.action.CommandContext;
import ru.egov.urm.action.CommandExecutor;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.MetaMonitoring;
import ru.egov.urm.meta.MetaSourceProject;
import ru.egov.urm.meta.Metadata;
import ru.egov.urm.meta.Metadata.VarBUILDMODE;
import ru.egov.urm.meta.Metadata.VarOSTYPE;
import ru.egov.urm.shell.Account;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.vcs.GenericVCS;
import ru.egov.urm.vcs.GitVCS;
import ru.egov.urm.vcs.SubversionVCS;

public class Artefactory {

	Metadata meta;
	CommandContext context;
	public Artefactory parentArtefactory;
	public LocalFolder workFolder;
	String workFolderProcessId;
	boolean ownFolder;
	
	public Artefactory( Metadata meta , CommandContext context ) {
		this.meta = meta;
		this.context = context;
		this.parentArtefactory = null;
		ownFolder = false;
	}

	public Artefactory( Artefactory base ) {
		this.meta = base.meta;
		this.workFolder = base.workFolder;
		ownFolder = false;
		this.parentArtefactory = base;
		this.workFolderProcessId = base.workFolderProcessId;
	}

	public void setShell( ShellExecutor session ) {
		this.workFolderProcessId = session.getProcessId();
	}
	
	public LocalFolder getAnyFolder( ActionBase action , String dirname ) throws Exception {
		action.checkRequired( dirname , "dirname" );
		if( dirname.equals( "/" ) )
			action.exit( "/ is not permitted for operations" );
		if( dirname.startsWith( "~/" ) )
			dirname = action.session.rootPath + dirname.substring( 1 );
		
		return( new LocalFolder( this , dirname ) );
	}

	public void createWorkFolder( ActionBase action ) throws Exception {
		workFolder = getWorkFolder( action , action.context.streamName );
		workFolder.recreateThis( action );
		ownFolder = true;
	}

	public void deleteWorkFolder( ActionBase action ) throws Exception {
		if( workFolder == null || ownFolder == false )
			return;
		
		CommandExecutor executor = action.executor; 
		if( executor.isFailed() || action.context.CTX_SHOWALL )
			action.log( "saved work directory: " + workFolder.folderPath );
		else
			workFolder.removeThis( action );
	}

	public LocalFolder getWorkFolder( ActionBase action ) throws Exception {
		return( workFolder );
	}
	
	public LocalFolder getWorkFolder( ActionBase action , String name ) throws Exception {
		action.checkRequired( name , "name" );
		String dirname;
		
		workFolderProcessId = action.session.getProcessId();
		if( parentArtefactory != null ) {
			dirname = parentArtefactory.workFolder.folderPath;
		
			if( workFolderProcessId.equals( parentArtefactory.workFolderProcessId ) )
				dirname += "/" + name;
			else
				dirname += "/" + name + "/session-" + workFolderProcessId;
		}
		else {
			dirname = meta.product.CONFIG_BUILDPATH + "/" + name + "/session-" + workFolderProcessId;
		}
				 
		LocalFolder folder = getAnyFolder( action , dirname );
		return( folder );	
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
	
	public DistStorage getDistProdStorage( ActionBase action ) throws Exception {
		return( getDistStorageByLabel( action , "prod" ) );
	}
	
	public DistStorage getDistStorageByLabel( ActionBase action , String RELEASELABEL ) throws Exception {
		action.checkRequired( RELEASELABEL , "RELEASELABEL" );

		DistRepository repo = getDistRepository( action );
		DistStorage storage = repo.getDistByLabel( action , RELEASELABEL );
		return( storage );
	}
	
	public DistStorage createDistStorage( ActionBase action , String RELEASELABEL , VarBUILDMODE BUILDMODE ) throws Exception {
		action.checkRequired( RELEASELABEL , "RELEASELABEL" );
		DistRepository repo = getDistRepository( action );
		DistStorage storage = repo.createDist( action , RELEASELABEL , BUILDMODE );
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
		Account account = ( winBuild )? action.getWinBuildAccount() : action.session.account; 
		storage = getRedistStorage( "build" , account ); 
		
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

		String PATCHDIR = meta.product.CONFIG_BUILDPATH + "/" + MODE;
		action.session.ensureDirExists( action , PATCHDIR );
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

	public RedistStorage getRedistStorage( ActionBase action , MetaEnvServer server , MetaEnvServerNode node ) throws Exception {
		Account account = action.getAccount( node );
		return( new RedistStorage( this , "default" , account , server , node ) );
	}

	public RedistStorage getRedistStorage( String type , Account account ) throws Exception {
		return( new RedistStorage( this , type , account , null , null ) );
	}

	public RuntimeStorage getRootRuntimeStorage( ActionBase action , MetaEnvServer server , MetaEnvServerNode node , boolean adm ) throws Exception {
		Account account = action.getAccount( node );
		if( adm )
			account = account.getRootAccount( action );
		return( new RuntimeStorage( this , "default" , account , server , node ) );
	}

	public RuntimeStorage getRuntimeStorage( ActionBase action , MetaEnvServer server , MetaEnvServerNode node ) throws Exception {
		Account account = action.getAccount( node );
		return( new RuntimeStorage( this , "default" , account , server , node ) );
	}

	public HiddenFiles getHiddenFiles() throws Exception {
		return( new HiddenFiles( this ) );
	}

	public UrmStorage getUrmStorage() throws Exception {
		return( new UrmStorage( this ) );
	}

	public RemoteFolder getReleaseHostRedistFolder( ActionBase action ) throws Exception {
		Account account = Account.getAccount( action , context.env.DISTR_HOSTLOGIN , VarOSTYPE.UNIX );
		return( new RemoteFolder( this , account , context.env.REDISTPATH ) );
	}
	
}
