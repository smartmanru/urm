package org.urm.engine.storage;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.dist.VersionInfo;
import org.urm.engine.shell.Account;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.loader.Types.*;
import org.urm.meta.product.Meta;

public class ServerStorage {

	static String S_REDIST_ARCHIVE_TYPE_DIRECT = "direct";
	static String S_REDIST_ARCHIVE_TYPE_CHILD = "child";
	static String S_REDIST_ARCHIVE_TYPE_SUBDIR = "subdir";

	static String S_CONFIGTARFILE = "config.tar";
	
	public Artefactory artefactory;
	public Meta meta;
	
	public MetaEnvServer server;
	public MetaEnvServerNode node;
	public Account account;

	public ServerStorage( Artefactory artefactory , Account account ) {
		this.artefactory = artefactory;
		this.meta = null;
		this.server = null;
		this.node = null;
		this.account = account;
	}
	
	public ServerStorage( Artefactory artefactory , Account account , MetaEnvServer server , MetaEnvServerNode node ) {
		this.artefactory = artefactory;
		this.meta = server.meta;
		this.account = account;
		this.server = server;
		this.node = node;		
	}
	
	public void checkRootDir( ActionBase action , String rootPath ) throws Exception {
		if( account.isLinux() ) {
			if( rootPath.isEmpty() || rootPath.equals( "/" ) || rootPath.startsWith( "/" ) == false || rootPath.endsWith( "/" ) )
				action.exit1( _Error.InvalidRootDir1 , "checkRootDir: invalid root dir=" + rootPath , rootPath );
		}
		else
		if( account.isWindows() ) {
			if( rootPath.isEmpty() || rootPath.equals( "/" ) )
				action.exit1( _Error.InvalidRootDir1 , "checkRootDir: invalid root dir=" + rootPath , rootPath );
			
			String noletter = rootPath.substring( 1 );
			if( noletter.startsWith( ":/" ) == false || noletter.equals( ":/" ) )
				action.exit1( _Error.InvalidRootDir1 , "checkRootDir: invalid root dir=" + rootPath , rootPath );
		}
	}

	public void checkRelativeDir( ActionBase action , String folder ) throws Exception {
		if( folder.isEmpty() || folder.startsWith( "/" ) || folder.endsWith( "/" ) )
			action.exit1( _Error.InvalidRelativeDir1 , "checkRootDir: invalid relative dir=" + folder , folder );
	}
	
	public RemoteFolder getRedistTmpFolder( ActionBase action ) throws Exception {
		String path = action.getContextRedistPath( account );
		if( path == null )
			return( null );
		
		path = Common.getPath( path , "tmp" , account.USER );
		RemoteFolder rf = new RemoteFolder( account , path );
		return( rf );
	}
	
	public RemoteFolder getRedistTmpFolder( ActionBase action , String folder ) throws Exception {
		RemoteFolder rf = getRedistTmpFolder( action );
		return( rf.getSubFolder( action , folder ) );
	}
	
	public RemoteFolder getRedistLocationFolder( ActionBase action , VersionInfo version , String LOCATION , EnumContentType CONTENTTYPE , boolean rollout ) throws Exception {
		String path = getPathRedistReleaseRoot( action , version , CONTENTTYPE , rollout );
		path = Common.getPath( path , LOCATION );
		RemoteFolder rf = new RemoteFolder( account , path );
		return( rf );
	}

	protected RemoteFolder getStateLocationFolder( ActionBase action , String LOCATION , EnumContentType CONTENTTYPE ) throws Exception {
		String path = getPathStateLocation( action , LOCATION , CONTENTTYPE );
		RemoteFolder rf = new RemoteFolder( account , path );
		return( rf );
	}
	
	public RemoteFolder getRedistHostRootFolder( ActionBase action ) throws Exception {
		Account rootAccount = account.getRootAccount();
		String path = action.getEnvRedistPath( server );
		RemoteFolder rf = new RemoteFolder( rootAccount , path );
		return( rf );
	}

	protected RemoteFolder getReleasesFolder( ActionBase action ) throws Exception {
		RemoteFolder folder = getRedistFolder( action );
		return( folder.getSubFolder( action , "releases" ) );
	}
	
	protected RemoteFolder getReleaseFolder( ActionBase action , VersionInfo version ) throws Exception {
		RemoteFolder folder = getReleasesFolder( action );
		return( folder.getSubFolder( action , version.getReleaseName() ) );
	}
	
	protected RemoteFolder getStateFolder( ActionBase action ) throws Exception {
		RemoteFolder folder = getRedistFolder( action );
		return( folder.getSubFolder( action , "state" ) );
	}

	public String getRedistFolderByContent( ActionBase action , EnumContentType CONTENTTYPE , boolean rollout ) throws Exception {
		String rolloutDir = Common.getEnumLower( CONTENTTYPE );
		if( rollout )
			return( rolloutDir );
		
		String rollbackDir = rolloutDir + ".backup";
		return( rollbackDir );
	}
	
	protected String getPathRedistReleaseRoot( ActionBase action , VersionInfo version , EnumContentType CONTENTTYPE , boolean rollout ) throws Exception {
		String C_COMMON_DIRPATH = getRedistFolderRootPath( action );
		C_COMMON_DIRPATH = Common.getPath( C_COMMON_DIRPATH , "releases" , version.getReleaseName() );
		
		String folder = getRedistFolderByContent( action , CONTENTTYPE , rollout );
		return( Common.getPath( C_COMMON_DIRPATH , folder ) );
	}
	
	protected String getPathRedistStateRoot( ActionBase action , EnumContentType CONTENTTYPE ) throws Exception {
		String C_COMMON_DIRPATH = getRedistFolderRootPath( action );
		C_COMMON_DIRPATH = Common.getPath( C_COMMON_DIRPATH , "state" );
		
		String folder = getRedistFolderByContent( action , CONTENTTYPE , true );
		return( Common.getPath( C_COMMON_DIRPATH , folder ) );
	}
	
	public String getPathRedistLocation( ActionBase action , VersionInfo version , String LOCATION , EnumContentType CONTENTTYPE , boolean rollout ) throws Exception {
		checkRelativeDir( action , LOCATION );
		String C_COMMON_DIRPATH = getPathRedistReleaseRoot( action , version , CONTENTTYPE , rollout );
		return( Common.getPath( C_COMMON_DIRPATH , LOCATION ) );
	}

	public String getPathStateLocation( ActionBase action , String LOCATION , EnumContentType CONTENTTYPE ) throws Exception {
		checkRelativeDir( action , LOCATION );
		String C_COMMON_DIRPATH = getPathRedistStateRoot( action , CONTENTTYPE );
		return( Common.getPath( C_COMMON_DIRPATH , LOCATION ) );
	}

	protected RemoteFolder getRuntimeLocationFolder( ActionBase action , String LOCATION ) throws Exception {
		checkRootDir( action , server.ROOTPATH );
		String path = Common.getPath( server.ROOTPATH , LOCATION );
		RemoteFolder rf = new RemoteFolder( account , path );
		return( rf );
	}
	
	protected RemoteFolder getRedistFolder( ActionBase action ) throws Exception {
		String path = getRedistFolderRootPath( action );
		RemoteFolder rf = new RemoteFolder( account , path );
		return( rf );
	}
	
	protected String getRedistFolderRootPath( ActionBase action ) throws Exception {
		String path = action.getContextRedistPath( account );
		if( path == null )
			return( null );

		if( node != null )
			path = Common.getPath( path , server.NAME + "-node" + node.POS );
		else
			path = Common.getPath( path , account.USER + "-account" );
		return( path );
	}

}
