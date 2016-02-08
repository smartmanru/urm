package ru.egov.urm.shell;

import ru.egov.urm.meta.Metadata.VarOSTYPE;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.storage.Folder;

public class RemoteShellExecutor extends ShellExecutor {
	Account account;

	public RemoteShellExecutor( String name , ShellExecutorPool pool , Account account , String rootPath , Folder tmpFolder ) {
		super( name , pool , rootPath , tmpFolder );
		this.account = account;
	}

	public void start( ActionBase action ) throws Exception {
		String terminalOption = ( account.OSTYPE == VarOSTYPE.WINDOWS )? "-T" : "-T";
		
		ProcessBuilder builder;
		String keyFile = action.context.CTX_KEYNAME;
		if( !keyFile.isEmpty() ) {
			if( action.context.CTX_TRACEINTERNAL )
				action.trace( "create process - ssh " + terminalOption + " " + account.HOSTLOGIN + " -i " + keyFile );
			if( terminalOption.isEmpty() )
				builder = new ProcessBuilder( "ssh" , account.HOSTLOGIN , "-i " , keyFile );
			else
				builder = new ProcessBuilder( "ssh" , terminalOption , account.HOSTLOGIN , "-i " , keyFile );
		}
		else {
			if( action.context.CTX_TRACEINTERNAL )
				action.trace( "create process - ssh " + terminalOption + " " + account.HOSTLOGIN );
			if( terminalOption.isEmpty() )
				builder = new ProcessBuilder( "ssh" , account.HOSTLOGIN );
			else
				builder = new ProcessBuilder( "ssh" , terminalOption , account.HOSTLOGIN );
		}
		super.createProcess( action , builder , rootPath );
	}
	
}
