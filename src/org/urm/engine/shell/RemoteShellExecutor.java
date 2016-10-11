package org.urm.engine.shell;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.storage.Folder;
import org.urm.meta.product.Meta.VarSESSIONTYPE;

public class RemoteShellExecutor extends ShellExecutor {
	
	public RemoteShellExecutor( String name , ShellPool pool , Account account , String rootPath , Folder tmpFolder ) {
		super( name , pool , account , rootPath , tmpFolder );
	}

	@Override
	public boolean start( ActionBase action ) throws Exception {
		ProcessBuilder builder = null;
		
		if( core.sessionType == VarSESSIONTYPE.WINDOWSFROMUNIX ) {
			if( action.context.CTX_TRACEINTERNAL )
				action.trace( "create local sh process on behalf of " + account.getPrintName() );
			builder = new ProcessBuilder( "sh" );
		}
		else if( core.sessionType == VarSESSIONTYPE.UNIXFROMWINDOWS ) {
			if( action.context.CTX_TRACEINTERNAL )
				action.trace( "create process - plink " + account.getPrintName() );
			
			String keyFile = action.context.CTX_KEYNAME;
			String cmd = "plink -P " + account.PORT;
			if( !keyFile.isEmpty() )
				cmd += " -i " + keyFile;
			
			cmd += " " + account.getHostLogin();
			builder = new ProcessBuilder( Common.createList( Common.splitSpaced( cmd ) ) );
		}
		else if( core.sessionType == VarSESSIONTYPE.UNIXREMOTE ) {
			String keyFile = action.context.CTX_KEYNAME;
			if( !keyFile.isEmpty() ) {
				if( action.context.CTX_TRACEINTERNAL )
					action.trace( "create process - ssh -T " + account.getSshAddr() + " -i " + keyFile );
				if( account.PORT == 22 )
					builder = new ProcessBuilder( "ssh" , "-T" , account.getHostLogin() , "-i" , keyFile );
				else
					builder = new ProcessBuilder( "ssh" , "-T" , "-p" , "" + account.PORT , account.getHostLogin() , "-i" , keyFile );
			}
			else {
				if( action.context.CTX_TRACEINTERNAL )
					action.trace( "create process - ssh -T " + account.getSshAddr() );
				if( account.PORT == 22 )
					builder = new ProcessBuilder( "ssh" , "-T" , account.getSshAddr() );
				else
					builder = new ProcessBuilder( "ssh" , "-T" , "-p" , "" + account.PORT , account.getSshAddr() );
			}
		}
		else
			action.exitUnexpectedState();
			
		return( super.createProcess( action , builder , rootPath ) );
	}
	
}
