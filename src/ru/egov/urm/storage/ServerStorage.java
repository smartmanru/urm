package ru.egov.urm.storage;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaDistrBinaryItem;
import ru.egov.urm.meta.MetaDistrConfItem;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.Metadata.VarCONTENTTYPE;
import ru.egov.urm.meta.Metadata.VarDISTITEMTYPE;
import ru.egov.urm.run.ActionBase;
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
	public String type;
	
	public ServerStorage( Artefactory artefactory , String type , Account account , MetaEnvServer server , MetaEnvServerNode node ) {
		this.artefactory = artefactory;
		this.server = null;
		this.node = null;
		this.account = account;
		this.type = type;
		this.server = server;
		this.node = node;		
	}
	
	public void checkRootDir( ActionBase action , String rootPath ) throws Exception {
		if( rootPath.isEmpty() || rootPath.equals( "/" ) || rootPath.startsWith( "/" ) == false || rootPath.endsWith( "/" ) )
			action.exit( "checkRootDir: invalid root dir=" + rootPath );
	}

	public void checkRelativeDir( ActionBase action , String folder ) throws Exception {
		if( folder.isEmpty() || folder.startsWith( "/" ) || folder.endsWith( "/" ) )
			action.exit( "checkRootDir: invalid relative dir=" + folder );
	}
	
	public RemoteFolder getRedistTmpFolder( ActionBase action ) throws Exception {
		String path;
		if( action.meta == null || action.context.env == null )
			path = action.meta.product.CONFIG_REDISTPATH;
		else
			path = action.context.env.REDISTPATH;
		
		path = Common.getPath( path , "tmp-" + account.USER );
		RemoteFolder rf = new RemoteFolder( artefactory , account , path );
		return( rf );
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

	protected String getPathStateLocation( ActionBase action , String LOCATION , VarCONTENTTYPE CONTENTTYPE ) throws Exception {
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
		if( action.meta == null || action.context.env == null )
			return( action.meta.product.CONFIG_REDISTPATH );
		
		String path = action.context.env.REDISTPATH;
		if( server != null )
			path = Common.getPath( path , server.NAME + "-node" + node.POS );
		else
			path = Common.getPath( path , type );
		return( path );
	}

	public String getRedistFileKey( ActionBase action , String redistFile ) throws Exception {
		return( Common.getListItem( redistFile , "-" , 1 ) );
	}
	
	public MetaDistrConfItem getRedistFileConfComp( ActionBase action , String redistFile ) throws Exception {
		String key = Common.getListItem( redistFile , "-" , 1 );
		MetaDistrConfItem item = action.meta.distr.findConfItem( action , key );
		if( item == null )
			action.exit( "unable to find configuration item=" + key + ", file=" + redistFile );
		return( item );
	}

	public MetaDistrBinaryItem getRedistFileBinaryItem( ActionBase action , String redistFile ) throws Exception {
		String key = Common.getListItem( redistFile , "-" , 1 );
		MetaDistrBinaryItem item = action.meta.distr.findBinaryItem( action , key );
		if( item == null )
			action.exit( "unable to find binary item=" + key + ", file=" + redistFile );
		return( item );
	}
	
	public MetaDistrBinaryItem getRedistFileArchiveItem( ActionBase action , String redistFile ) throws Exception {
		String key = Common.getListItem( redistFile , "-" , 1 );
		MetaDistrBinaryItem item = action.meta.distr.findBinaryItem( action , key );
		if( item == null )
			action.exit( "unable to find archive item=" + key + ", file=" + redistFile );
		return( item );
	}
	
	public String getDeployBinaryName( ActionBase action , MetaDistrBinaryItem item , String deployName ) throws Exception {
		String name = "binary-" + item.KEY + "-" + deployName;
		return( name );
	}

	public String getRedistBinaryFileDeployName( ActionBase action , String redistFile ) throws Exception {
		String fileNoType = Common.getPartAfterFirst( redistFile , "-" );
		String fileWithExt = Common.getPartAfterFirst( fileNoType , "-" );
		return( fileWithExt );
	}
	
	public boolean getRedistFileConfFull( ActionBase action , String redistFile ) throws Exception {
		String fileNoType = Common.getPartAfterFirst( redistFile , "-" );
		String fileAfterKey = Common.getPartAfterFirst( fileNoType , "-" );
		if( fileAfterKey.startsWith( "full." ) )
			return( true );
		return( false );
	}
	
	public String getConfigArchiveName( ActionBase action , MetaDistrConfItem item , boolean full ) throws Exception {
		String mode = ( full )? "full" : "partial";
		return( "config-" + item.KEY + "-" + mode + ".tar" );
	}

	public String getDeployNupkgName( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		return( "nupkg-" + item.KEY + "-files" + item.EXT );
	}

}
