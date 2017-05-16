package org.urm.action.build;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.engine.dist.Dist;

public class BuildPlan {
	
	public List<BuildPlanSet> listSets;
	Map<String,BuildPlanSet> mapSets;
	public Dist dist;
	public BuildPlanSet selectSet;
	
	public BuildPlan( Dist dist ) {
		this.dist = dist;
		listSets = new LinkedList<BuildPlanSet>();
		mapSets = new HashMap<String,BuildPlanSet>();
	}
	
	public int getSetCount() {
		return( listSets.size() );
	}
	
	public void addSet( BuildPlanSet set ) {
		listSets.add( set );
		mapSets.put( set.name , set );
	}
	
	public BuildPlanSet findSet( String setName ) {
		return( mapSets.get( setName ) );
	}
	
	public void selectSet( String setName ) {
		if( setName.isEmpty() )
			selectSet = null;
		else
			selectSet = findSet( setName );
		
		for( BuildPlanSet set : listSets ) {
			for( BuildPlanItem item : set.listItems ) {
				if( selectSet != null && set != selectSet )
					item.setExecute( false );
			}
		}
	}

	public boolean hasExecute() {
		if( hasCompile() || hasConf() || hasDatabase() )
			return( true );
		return( false );
	}
	
	public boolean hasCompile() {
		if( isCompileAll() || isCompileSelected() )
			return( true );
		return( false );
	}
	
	public boolean isCompileAll() {
		if( selectSet != null )
			return( false );
			
		for( BuildPlanSet set : listSets ) {
			if( set.build && set.hasSelected() )
				return( true );
		}
		
		return( false );
	}
	
	public boolean isCompileSelected() {
		if( selectSet == null || selectSet.build == false )
			return( false );
		if( !selectSet.hasSelected() )
			return( false );
		return( true );
	}
	
	public boolean hasConf() {
		if( isConfAll() || isConfSelected() )
			return( true );
		return( false );
	}
	
	public boolean isConfAll() {
		if( selectSet != null )
			return( false );
			
		for( BuildPlanSet set : listSets ) {
			if( set.conf && set.hasSelected() )
				return( true );
		}
		
		return( false );
	}
	
	public boolean isConfSelected() {
		if( selectSet == null || selectSet.conf == false )
			return( false );
		if( !selectSet.hasSelected() )
			return( false );
		return( true );
	}
	
	public boolean hasDatabase() {
		if( isDatabaseAll() || isDatabaseSelected() )
			return( true );
		return( false );
	}
	
	public boolean isDatabaseAll() {
		if( selectSet != null )
			return( false );
			
		for( BuildPlanSet set : listSets ) {
			if( set.db && set.hasSelected() )
				return( true );
		}
		
		return( false );
	}
	
	public boolean isDatabaseSelected() {
		if( selectSet == null || selectSet.db == false )
			return( false );
		if( !selectSet.hasSelected() )
			return( false );
		return( true );
	}
	
}
