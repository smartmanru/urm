package org.urm.action.deploy;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.engine.dist.Dist;
import org.urm.meta.product.MetaEnv;

public class DeployPlan {
	
	public List<DeployPlanSegment> listSg;
	Map<String,DeployPlanSegment> mapSg;
	
	public Dist dist;
	public MetaEnv env;
	public DeployPlanSegment selectSg;
	public DeployPlanSet selectSet;

	boolean redist;
	boolean deploy;
	
	public DeployPlan( Dist dist , MetaEnv env , boolean redist , boolean deploy ) {
		this.dist = dist;
		this.env = env;
		this.redist = redist;
		this.deploy = deploy;
		
		listSg = new LinkedList<DeployPlanSegment>();
		mapSg = new HashMap<String,DeployPlanSegment>();
	}
	
	public int getSegmentCount() {
		return( listSg.size() );
	}
	
	public void addSegment( DeployPlanSegment sg ) {
		listSg.add( sg );
		mapSg.put( sg.sg.NAME , sg );
	}
	
	public DeployPlanSegment findSet( String sgName ) {
		return( mapSg.get( sgName ) );
	}
	
	public void selectSegment( DeployPlanSegment sg ) {
		this.selectSg = sg;
	}
			
	public void selectSet( String setName ) {
		if( setName.isEmpty() )
			selectSet = null;
		else
			selectSet = selectSg.findSet( setName );
		
		for( DeployPlanSegment sg : listSg ) {
			for( DeployPlanSet set : sg.listSets ) {
				for( DeployPlanItem item : set.listItems ) {
					if( selectSet != null && set != selectSet )
						item.setExecute( false );
				}
			}
		}
	}

	public void setDeploy( boolean deploy ) {
		this.deploy = deploy;
	}
	
	public boolean hasExecute() {
		for( DeployPlanSegment sg : listSg ) {
			for( DeployPlanSet set : sg.listSets ) {
				for( DeployPlanItem item : set.listItems ) {
					if( item.execute )
						return( true );
				}
			}
		}
		return( false );
	}
	
}
