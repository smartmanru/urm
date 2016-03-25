package ru.egov.urm;

import ru.egov.urm.action.ActionInit;
import ru.egov.urm.action.CommandAction;
import ru.egov.urm.action.CommandBuilder;
import ru.egov.urm.action.CommandExecutor;

public class UrmConfigurator extends CommandExecutor {

	public UrmConfigurator( CommandBuilder builder ) {
		super( builder );
		
		String cmdOpts = "";
		super.defineAction( CommandAction.newAction( new ConfigureLinux() , "configure-linux" , "configure as linux" , cmdOpts , "./configure.sh [OPTIONS] {all|build|env} [envname [dcname]]" ) );
		super.defineAction( CommandAction.newAction( new ConfigureWindows() , "configure-windows" , "configure as windows" , cmdOpts , "./configure.cmd [OPTIONS] {all|build|env} [envname [dcname]]" ) );
	}

	public boolean run( ActionInit action ) {
		// log action and run 
		boolean res = super.runMethod( action , commandAction );
		return( res );
	}

	private class ConfigureLinux extends CommandAction {
	public void run( ActionInit action ) throws Exception {
	}
	}

	private class ConfigureWindows extends CommandAction {
	public void run( ActionInit action ) throws Exception {
	}
	}

}
