package org.urm.action.deploy;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.meta.product.MetaEnvSegment;

public class DeployPlanSegment {
	
	public DeployPlan plan;
	public MetaEnvSegment sg;
	public int segmentPos;
	
	public List<DeployPlanSet> listSets;
	Map<String,DeployPlanSet> mapSets;
	
	public DeployPlanSegment( DeployPlan plan , MetaEnvSegment sg , int segmentPos ) {
		this.plan = plan;
		this.sg = sg;
		this.segmentPos = segmentPos;
		
		listSets = new LinkedList<DeployPlanSet>();
		mapSets = new HashMap<String,DeployPlanSet>();
	}

	public void addSet( DeployPlanSet set ) {
		listSets.add( set );
		mapSets.put( set.name , set );
	}
	
	public DeployPlanSet findSet( String setName ) {
		return( mapSets.get( setName ) );
	}
	
}
