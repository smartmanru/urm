package ru.egov.urm.run.database;

import ru.egov.urm.run.ActionInit;
import ru.egov.urm.run.CommandBuilder;
import ru.egov.urm.run.CommandExecutor;

public class DatabaseCommandExecutor extends CommandExecutor {

	public DatabaseCommandExecutor( CommandBuilder builder ) {
		super( builder );
	}
	
	public boolean run( ActionInit action ) {
		return( false );
	}

}
