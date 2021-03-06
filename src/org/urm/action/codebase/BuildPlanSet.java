package org.urm.action.codebase;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.engine.dist.ReleaseBuildScopeSet;
import org.urm.engine.dist.ReleaseDistScopeSet;

public class BuildPlanSet {
	
	public BuildPlan plan;
	public ReleaseBuildScopeSet buildSet;
	public ReleaseDistScopeSet distSet;
	public int pos;
	public String name;
	public Map<String,BuildPlanItem> mapItems;
	public List<BuildPlanItem> listItems;
	public boolean build;
	public boolean conf;
	public boolean db;
	
	public BuildPlanSet( BuildPlan plan , int pos , String name ) {
		this.plan = plan;
		this.pos = pos;
		this.name = name;
		mapItems = new HashMap<String,BuildPlanItem>();
		listItems = new LinkedList<BuildPlanItem>();
		build = false;
		conf = false;
		db = false;
	}

	public void createBuildSet( ReleaseBuildScopeSet buildSet ) {
		this.buildSet = buildSet;
	}
	
	public void createDistSet( ReleaseDistScopeSet distSet ) {
		this.distSet = distSet;
	}
	
	public void addItem( BuildPlanItem item ) {
		mapItems.put( item.key , item );
		listItems.add( item );
	}
	
	public String getFullPos() {
		return( "" + ( pos + 1 ) );
	}

	public void createBuild() {
		build = true;
	}
	
	public void createConf() {
		conf = true;
	}
	
	public void createDatabase() {
		db = true;
	}

	public boolean hasSelected() {
		for( BuildPlanItem item : listItems ) {
			if( item.execute )
				return( true );
		}
		return( false );
	}

	public String[] getSelected() {
		List<String> selected = new LinkedList<String>();
		for( BuildPlanItem item : listItems ) {
			if( item.execute )
				selected.add( item.key );
		}
		return( selected.toArray( new String[0] ) );
	}

	public void clearRun() {
		for( BuildPlanItem item : listItems )
			item.clearRun();
	}
	
}
