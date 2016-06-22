package org.urm.common.action;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.ExitException;

public class CommandMeta {

	public String name;
	public String desc;

	public Map<String,CommandMethod> actionsMap = new HashMap<String,CommandMethod>();
	public List<CommandMethod> actionsList = new LinkedList<CommandMethod>();
	
	public CommandMeta( CommandBuilder builder , String name , String desc ) {
		this.name = name;
		this.desc = desc;
	}
	
	public void defineAction( CommandMethod action ) {
		actionsMap.put( action.name , action );
		actionsList.add( action );
	}
	
	public void print( String s ) {
		System.out.println( s );
	}
	
	public void printhelp( String s ) {
		print( "# " + s );
	}
	
	public boolean isOptionApplicaple( CommandVar var ) {
		for( CommandMethod action : actionsMap.values() )
			if( action.isOptionApplicable( var ) )
				return( true );
		return( false );
	}

	public CommandMethod getAction( String name ) throws Exception {
		CommandMethod method = actionsMap.get( name );
		if( method == null )
			throw new ExitException( "executor action is not present in meta, name=" + name );
		return( method );
	}
	
}
