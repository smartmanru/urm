package org.urm.common.action;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;

public class CommandMeta {

	public String name;
	public String desc;

	public Map<String,CommandMethodMeta> actionsMap = new HashMap<String,CommandMethodMeta>();
	public List<CommandMethodMeta> actionsList = new LinkedList<CommandMethodMeta>();
	
	public CommandMeta( String name , String desc ) {
		this.name = name;
		this.desc = desc;
	}
	
	public void defineAction( CommandMethodMeta action ) {
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
		for( CommandMethodMeta action : actionsMap.values() )
			if( action.isOptionApplicable( var ) )
				return( true );
		return( false );
	}

	public CommandMethodMeta getAction( String actionName ) throws Exception {
		CommandMethodMeta method = actionsMap.get( actionName );
		if( method == null )
			Common.exit2( _Error.MetaNoAction2 , "executor action is not present in meta, name=" + actionName , name , actionName );
		return( method );
	}
	
}
