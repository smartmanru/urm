package ru.egov.urm.shell;

import ru.egov.urm.run.ActionBase;

public class RemoteShellExecutor extends ShellExecutor {
	String hostLogin;

	public RemoteShellExecutor( String name , ShellExecutorPool pool , String hostLogin , String rootPath ) {
		super( name , pool , rootPath );
		this.hostLogin = hostLogin;
	}
	
	public void start( ActionBase action ) throws Exception {
		ProcessBuilder builder;
		String keyFile = action.context.KEYNAME;
		if( !keyFile.isEmpty() )
			builder = new ProcessBuilder( "ssh" , "-n -T" , hostLogin , "-i " , keyFile );
		else
			builder = new ProcessBuilder( "ssh" , "-n -T" , hostLogin );
		super.createProcess( action , builder , rootPath );
	}
	
}
