package org.urm.engine.dist;

import java.util.HashMap;
import java.util.Map;

import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseBuildTarget;

public class ReleaseBuildScopeProject {

	public Release release;
	public MetaSourceProject project;

	public boolean all;
	public ReleaseBuildTarget scopeProjectTarget;
	
	private Map<String,ReleaseBuildScopeProjectItem> mapItems;
	
	public ReleaseBuildScopeProject( Release release , MetaSourceProject project ) {
		this.project = project;
		mapItems = new HashMap<String,ReleaseBuildScopeProjectItem>();
		all = false;
	}

	public ReleaseBuildScopeProjectItem findItem( MetaSourceProjectItem item ) {
		return( mapItems.get( item.NAME ) );
	}
	
	public void addItem( ReleaseBuildScopeProjectItem scopeItem ) {
		mapItems.put( scopeItem.item.NAME , scopeItem );
	}

	public ReleaseBuildScopeProjectItem[] getItems() {
		return( mapItems.values().toArray( new ReleaseBuildScopeProjectItem[0] ) );
	}
	
	public void setAll( boolean all ) {
		this.all = all;
	}
	
	public boolean isEmpty() {
		if( mapItems.isEmpty() )
			return( true );
		return( false );
	}
	
}
