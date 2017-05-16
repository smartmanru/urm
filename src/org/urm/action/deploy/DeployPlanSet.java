package org.urm.action.deploy;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DeployPlanSet {

	public DeployPlanSegment sg;
	public int sectionPos;
	public String name;
	
	List<DeployPlanItem> listItems;
	Map<String,DeployPlanItem> mapItems;
	
	public DeployPlanSet( DeployPlanSegment sg , int sectionPos , String name ) {
		this.sg = sg;
		this.sectionPos = sectionPos;
		this.name = name;
		
		mapItems = new HashMap<String,DeployPlanItem>();
		listItems = new LinkedList<DeployPlanItem>();
	}
	
	public void addItem( DeployPlanItem item ) {
		mapItems.put( item.server.NAME , item );
		listItems.add( item );
	}
	
	public String getFullPos() {
		return( ( sg.segmentPos + 1 ) + "." + ( sectionPos + 1 ) );
	}

	public boolean hasSelected() {
		for( DeployPlanItem item : listItems ) {
			if( item.execute )
				return( true );
		}
		return( false );
	}

	public String[] getSelected() {
		List<String> selected = new LinkedList<String>();
		for( DeployPlanItem item : listItems ) {
			if( item.execute )
				selected.add( item.key );
		}
		return( selected.toArray( new String[0] ) );
	}
	
}
