package org.urm.engine.shell;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;

public class ActionShells {

	public ActionBase action;
	
	public Map<String,ShellExecutor> executors;
	public Map<String,ShellInteractive> interactive;
	
	public ActionShells( ActionBase action ) {
		this.action = action;
		
		executors = new HashMap<String,ShellExecutor>(); 
		interactive = new HashMap<String,ShellInteractive>(); 
	}

	public ShellExecutor getExecutor( String name ) {
		return( executors.get( name ) );
	}

	public ShellExecutor[] getExecutorList() {
		return( executors.values().toArray( new ShellExecutor[0] ) );
	}
	
	public ShellInteractive[] getInteractiveList() {
		return( interactive.values().toArray( new ShellInteractive[0] ) );
	}
	
	public void addExecutor( ShellExecutor executor ) {
		executors.put( executor.name , executor );
	}

	public void removeExecutor( String name ) {
		executors.remove( name );
	}

	public void removeInteractive( String name ) {
		interactive.remove( name );
	}

	public void addInteractive( ShellInteractive shell ) {
		interactive.put( shell.name , shell );
	}

}
