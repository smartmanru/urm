package ru.egov.urm.shell;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.Metadata.VarOSTYPE;
import ru.egov.urm.storage.Folder;

public class RemoteShellExecutor extends ShellExecutor {
	public RemoteShellExecutor( String name , ShellExecutorPool pool , Account account , String rootPath , Folder tmpFolder ) {
		super( name , pool , account , rootPath , tmpFolder );
	}

	public void start( ActionBase action ) throws Exception {
		ProcessBuilder builder;
		
		if( account.OSTYPE == VarOSTYPE.WINDOWS ) {
			if( action.context.CTX_TRACEINTERNAL )
				action.trace( "create local sh process on behalf of " + account.HOSTLOGIN );
			builder = new ProcessBuilder( "sh" );
		}
		else {
			String keyFile = action.context.CTX_KEYNAME;
			if( !keyFile.isEmpty() ) {
				if( action.context.CTX_TRACEINTERNAL )
					action.trace( "create process - ssh -T " + account.HOSTLOGIN + " -i " + keyFile );
				builder = new ProcessBuilder( "ssh" , "-T" , account.HOSTLOGIN , "-i " , keyFile );
			}
			else {
				if( action.context.CTX_TRACEINTERNAL )
					action.trace( "create process - ssh -T " + account.HOSTLOGIN );
				builder = new ProcessBuilder( "ssh" , "-T" , account.HOSTLOGIN );
			}
		}
			
		super.createProcess( action , builder , rootPath );
	}
	
}
