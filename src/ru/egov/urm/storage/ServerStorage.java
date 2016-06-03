package ru.egov.urm.storage;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.Metadata.VarCONTENTTYPE;
import ru.egov.urm.shell.Account;

public class ServerStorage {

	static String S_REDIST_ARCHIVE_TYPE_DIRECT = "direct";
	static String S_REDIST_ARCHIVE_TYPE_CHILD = "child";
	static String S_REDIST_ARCHIVE_TYPE_SUBDIR = "subdir";

	static String S_CONFIGTARFILE = "config.tar";
	
	public Artefactory artefactory;
	public MetaEnvServer server;
	public MetaEnvServerNode node;
	public Account account;
	
	public ServerStorage( Artefactory artefactory , Account account , MetaEnvServer server , MetaEnvServerNode node ) {
		this.artefactory = artefactory;
		this.server = null;
		this.node = null;
		this.account = account;
		this.server = server;
		this.node = node;		
	}
	
	public void checkRootDir( ActionBase action , String rootPath ) throws Exception {
		if( account.isLinux() ) {
			if( rootPath.isEmpty() || rootPath.equals( "/" ) || rootPath.startsWith( "/" ) == false || rootPath.endsWith( "/" ) )
				action.exit( "checkRootDir: invalid root dir=" + rootPath );
		}
		else
		if( account.isWindows() ) {
			if( rootPath.isEmpty() || rootPath.equals( "/" ) )
				action.exit( "checkRootDir: invalid root dir=" + rootPath );
			
			String noletter = rootPath.substring( 1 );
			if( noletter.startsWith( ":/" ) == false || noletter.equals( ":/" ) )
				action.exit( "checkRootDir: invalid root dir=" + rootPath );
		}
	}

	public void checkRelativeDir( ActionBase action , String folder ) throws Exception {
		if( folder.isEmpty() || folder.startsWith( "/" ) || folder.endsWith( "/" ) )
			action.exit( "checkRootDir: invalid relative dir=" + folder );
	}
	
	public RemoteFolder getRedistTmpFolder( ActionBase action ) throws Exception {
		String path = Common.getPath( action.context.CTX_REDISTPATH , "tmp" );
		RemoteFolder rf = new RemoteFolder( artefactory , account , path );
		return( rf );
	}
	
	public RemoteFolder getRedistTmpFolder( ActionBase action , String folder ) throws Exception {
		RemoteFolder rf = getRedistTmpFolder( action );
		return( rf.getSubFolder( action , folder ) );
	}
	
	public RemoteFolder getRedistLocationFolder( ActionBase action , String RELEASEDIR , String LOCATION , VarCONTENTTYPE CONTENTTYPE , boolean rollout ) throws Exception {
		String path = getPathRedistReleaseRoot( action , RELEASEDIR , CONTENTTYPE , rollout );
		path = Common.getPath( path , LOCATION );
		RemoteFolder rf = new RemoteFolder( artefactory , account , path );
		return( rf );
	}

	protected RemoteFolder getStateLocationFolder( ActionBase action , String LOCATION , VarCONTENTTYPE CONTENTTYPE ) throws Exception {
		String path = getPathStateLocation( action , LOCATION , CONTENTTYPE );
		RemoteFolder rf = new RemoteFolder( artefactory , account , path );
		return( rf );
	}
	
	public RemoteFolder getRedistHostRootFolder( ActionBase action ) throws Exception {
		String path = ( action.context.env == null )? action.meta.product.CONFIG_REDISTPATH : action.context.env.REDISTPATH;
		Account rootAccount = account.getRootAccount( action );
		RemoteFolder rf = new RemoteFolder( artefactory , rootAccount , path );
		return( rf );
	}

	protected RemoteFolder getReleasesFolder( ActionBase action ) throws Exception {
		RemoteFolder folder = getRedistFolder( action );
		return( folder.getSubFolder( action , "releases" ) );
	}
	
	protected RemoteFolder getReleaseFolder( ActionBase action , String RELEASEDIR ) throws Exception {
		RemoteFolder folder = getReleasesFolder( action );
		return( folder.getSubFolder( action , RELEASEDIR ) );
	}
	
	protected RemoteFolder getStateFolder( ActionBase action ) throws Exception {
		RemoteFolder folder = getRedistFolder( action );
		return( folder.getSubFolder( action , "state" ) );
	}

	public String getRedistFolderByContent( ActionBase action , VarCONTENTTYPE CONTENTTYPE , boolean rollout ) throws Exception {
		String rolloutDir = Common.getEnumLower( CONTENTTYPE );
		if( rollout )
			return( rolloutDir );
		
		String rollbackDir = rolloutDir + ".backup";
		return( rollbackDir );
	}
	
	protected String getPathRedistReleaseRoot( ActionBase action , String RELEASEDIR , VarCONTENTTYPE CONTENTTYPE , boolean rollout ) throws Exception {
		if( CONTENTTYPE == null )
			action.exit( "getPathRedistReleaseRoot: invalid params" );

		String C_COMMON_DIRPATH = getRedistFolderRootPath( action );
		C_COMMON_DIRPATH = Common.getPath( C_COMMON_DIRPATH , "releases" , RELEASEDIR );
		
		String folder = getRedistFolderByContent( action , CONTENTTYPE , rollout );
		return( Common.getPath( C_COMMON_DIRPATH , folder ) );
	}
	
	protected String getPathRedistStateRoot( ActionBase action , VarCONTENTTYPE CONTENTTYPE ) throws Exception {
		if( CONTENTTYPE == null )
			action.exit( "getPathRedistStateRoot: invalid params" );

		String C_COMMON_DIRPATH = getRedistFolderRootPath( action );
		C_COMMON_DIRPATH = Common.getPath( C_COMMON_DIRPATH , "state" );
		
		String folder = getRedistFolderByContent( action , CONTENTTYPE , true );
		return( Common.getPath( C_COMMON_DIRPATH , folder ) );
	}
	
	public String getPathRedistLocation( ActionBase action , String RELEASEDIR , String LOCATION , VarCONTENTTYPE CONTENTTYPE , boolean rollout ) throws Exception {
		checkRelativeDir( action , LOCATION );
		String C_COMMON_DIRPATH = getPathRedistReleaseRoot( action , RELEASEDIR , CONTENTTYPE , rollout );
		return( Common.getPath( C_COMMON_DIRPATH , LOCATION ) );
	}

	public String getPathStateLocation( ActionBase action , String LOCATION , VarCONTENTTYPE CONTENTTYPE ) throws Exception {
		checkRelativeDir( action , LOCATION );
		String C_COMMON_DIRPATH = getPathRedistStateRoot( action , CONTENTTYPE );
		return( Common.getPath( C_COMMON_DIRPATH , LOCATION ) );
	}

	protected RemoteFolder getRuntimeLocationFolder( ActionBase action , String LOCATION ) throws Exception {
		checkRootDir( action , server.ROOTPATH );
		String path = Common.getPath( server.ROOTPATH , LOCATION );
		RemoteFolder rf = new RemoteFolder( artefactory , account , path );
		return( rf );
	}
	
	protected RemoteFolder getRedistFolder( ActionBase action ) throws Exception {
		String path = getRedistFolderRootPath( action );
		RemoteFolder rf = new RemoteFolder( artefactory , account , path );
		return( rf );
	}
	
	protected String getRedistFolderRootPath( ActionBase action ) throws Exception {
		String path = action.context.CTX_REDISTPATH;
		if( server != null )
			path = Common.getPath( path , server.NAME + "-node" + node.POS );
		return( path );
	}

}
