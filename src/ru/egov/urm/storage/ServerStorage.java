package ru.egov.urm.storage;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaDistrBinaryItem;
import ru.egov.urm.meta.MetaDistrConfItem;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.Metadata.VarCONTENTTYPE;
import ru.egov.urm.meta.Metadata.VarDISTITEMTYPE;
import ru.egov.urm.run.ActionBase;

public class ServerStorage {

	public enum RedistFileType {
		CONFCOMP ,
		BINARY ,
		ARCHIVE
	};

	static String S_REDIST_ARCHIVE_TYPE_DIRECT = "direct";
	static String S_REDIST_ARCHIVE_TYPE_CHILD = "child";
	static String S_REDIST_ARCHIVE_TYPE_SUBDIR = "subdir";

	static String S_CONFIGTARFILE = "config.tgz";
	
	public Artefactory artefactory;
	public MetaEnvServer server;
	public MetaEnvServerNode node;
	public String hostLogin;
	public String type;
	
	public ServerStorage( Artefactory artefactory , MetaEnvServer server , MetaEnvServerNode node ) {
		this.artefactory = artefactory;
		this.server = server;
		this.node = node;
		this.hostLogin = node.HOSTLOGIN;
	}
	
	public ServerStorage( Artefactory artefactory , String type , String hostLogin ) {
		this.artefactory = artefactory;
		this.server = null;
		this.node = null;
		this.hostLogin = hostLogin;
		this.type = type;
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
		String path = getRedistFolderRootPath( action );
		path = Common.getPath( path , "tmp" );
		RemoteFolder rf = new RemoteFolder( artefactory , hostLogin , path );
		return( rf );
	}
	
	public RemoteFolder getRedistLocationFolder( ActionBase action , String RELEASEDIR , String LOCATION , VarCONTENTTYPE CONTENTTYPE , boolean rollout ) throws Exception {
		String path = getPathRedistReleaseRoot( action , RELEASEDIR , CONTENTTYPE , rollout );
		path = Common.getPath( path , LOCATION );
		RemoteFolder rf = new RemoteFolder( artefactory , hostLogin , path );
		return( rf );
	}

	protected RemoteFolder getStateLocationFolder( ActionBase action , String LOCATION , VarCONTENTTYPE CONTENTTYPE ) throws Exception {
		String path = getPathStateLocation( action , LOCATION , CONTENTTYPE );
		RemoteFolder rf = new RemoteFolder( artefactory , hostLogin , path );
		return( rf );
	}
	
	public RemoteFolder getRedistHostRootFolder( ActionBase action ) throws Exception {
		String path = action.meta.env.REDISTPATH;
		String rootLogin = Common.getRootAccount( Common.getAccountHost( hostLogin ) );
		RemoteFolder rf = new RemoteFolder( artefactory , rootLogin , path );
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
		C_COMMON_DIRPATH = Common.getPath( Common.getPath( C_COMMON_DIRPATH , "releases" ) , RELEASEDIR );
		
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
	
	protected String getPathRedistLocation( ActionBase action , String RELEASEDIR , String LOCATION , VarCONTENTTYPE CONTENTTYPE , boolean rollout ) throws Exception {
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
		RemoteFolder rf = new RemoteFolder( artefactory , hostLogin , path );
		return( rf );
	}
	
	protected RemoteFolder getRedistFolder( ActionBase action ) throws Exception {
		String path = getRedistFolderRootPath( action );
		RemoteFolder rf = new RemoteFolder( artefactory , hostLogin , path );
		return( rf );
	}
	
	protected String getRedistFolderRootPath( ActionBase action ) throws Exception {
		String path = action.meta.env.REDISTPATH;
		if( server != null )
			path = Common.getPath( path , server.NAME + "-node" + node.POS );
		else
			path = Common.getPath( path , type );
		return( path );
	}

	public String getRedistFileKey( ActionBase action , String redistFile ) throws Exception {
		return( Common.getListItem( redistFile , "-" , 1 ) );
	}
	
	public RedistFileType getRedistFileType( ActionBase action , String redistFile ) throws Exception {
		if( redistFile.startsWith( "config-" ) )
			return( RedistFileType.CONFCOMP );
		
		if( redistFile.startsWith( "archive-" ) )
			return( RedistFileType.ARCHIVE );
		
		if( redistFile.startsWith( "binary-" ) )
			return( RedistFileType.BINARY );
		
		action.exitUnexpectedState();
		return( null );
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
	
	public String getRedistBinaryName( ActionBase action , MetaDistrBinaryItem item , String deployName ) throws Exception {
		String name = "binary-" + item.KEY + "-" + deployName;
		return( name );
	}

	public String getRedistBinaryFileDeployName( ActionBase action , String redistFile ) throws Exception {
		String fileNoType = Common.getPartAfterFirst( redistFile , "-" );
		return( Common.getPartAfterFirst( fileNoType , "-" ) );
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
		return( "config-" + item.KEY + "-" + mode + ".tgz" );
	}

	public String getRedistArchiveName( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		if( item.DISTTYPE == VarDISTITEMTYPE.ARCHIVE_DIRECT )
			return( "archive-" + item.KEY + "-" + S_REDIST_ARCHIVE_TYPE_DIRECT + "-files" + item.EXT );
	
		if( item.DISTTYPE == VarDISTITEMTYPE.ARCHIVE_SUBDIR )
			return( "archive-" + item.KEY + "-" + S_REDIST_ARCHIVE_TYPE_SUBDIR + "-" + item.DEPLOYBASENAME + item.EXT );
	
		if( item.DISTTYPE == VarDISTITEMTYPE.ARCHIVE_CHILD )
			return( "archive-" + item.KEY + "-" + S_REDIST_ARCHIVE_TYPE_CHILD + "-" + item.DEPLOYBASENAME + item.EXT );
		
		action.exitUnexpectedState();
		return( null );
	}

	public String getStateBaseName( ActionBase action , VarCONTENTTYPE CONTENTTYPE , String redistFile ) throws Exception {
		String fileNoType = Common.getPartAfterFirst( redistFile , "-" );
		String fileStateName = Common.getPartBeforeFirst( redistFile , "-" ) + "-" + Common.getPartBeforeFirst( fileNoType , "-" );
		return( fileStateName );
	}

	public String getStateFileName( ActionBase action , String stateBaseName ) throws Exception {
		return( stateBaseName + ".file" );
	}

	public String getStateInfoName( ActionBase action , String stateBaseName ) throws Exception {
		return( stateBaseName + ".ver" );
	}

}
