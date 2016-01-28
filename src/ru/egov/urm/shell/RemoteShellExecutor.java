package ru.egov.urm.shell;

import ru.egov.urm.run.ActionBase;
import ru.egov.urm.storage.Folder;

public class RemoteShellExecutor extends ShellExecutor {
	Account account;

	public RemoteShellExecutor( String name , ShellExecutorPool pool , Account account , String rootPath , Folder tmpFolder ) {
		super( name , pool , rootPath , tmpFolder );
		this.account = account;
	}

	public void start( ActionBase action ) throws Exception {
		ProcessBuilder builder;
		String keyFile = action.context.KEYNAME;
		if( !keyFile.isEmpty() )
			builder = new ProcessBuilder( "ssh" , "-T" , account.HOSTLOGIN , "-i " , keyFile );
		else
			builder = new ProcessBuilder( "ssh" , "-T" , account.HOSTLOGIN );
		super.createProcess( action , builder , rootPath );
	}
	
}
