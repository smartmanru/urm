package org.urm.action.deploy;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.action.CommandOptions;
import org.urm.common.meta.DeployCommandMeta;
import org.urm.engine.ServerEventsApp;
import org.urm.engine.ServerEventsListener;
import org.urm.engine.dist.Dist;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvSegment;

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

	public boolean executeRedist( ActionBase action , ServerEventsApp app , ServerEventsListener listener , CommandOptions options ) {
		String[] args = null;
		
		// redist
		if( selectSet == null )
			args = new String[] { dist.RELEASEDIR , "all" };
		else {
			DeployPlanSet set = selectSet;
			String[] selected = set.getSelected();
			if( selected.length > 0 ) {
				args = new String[ 1 + selected.length ];
				args[ 0 ] = dist.RELEASEDIR;
				for( int k = 0; k < selected.length; k++ )
					args[ 1 + k ] = Common.getPartAfterFirst( selected[ k ] , "::" );
			}
		}
		
		MetaEnvSegment sg = ( selectSg == null )? null : selectSg.sg;
		return( action.runNotifyMethod( app , listener , env.meta , env , sg , DeployCommandMeta.NAME , DeployCommandMeta.METHOD_REDIST , args , options ) );
	}

	public boolean executeDeploy( ActionBase action , ServerEventsApp app , ServerEventsListener listener , CommandOptions options ) {
		String[] args = null;
		
		// redist
		if( selectSet == null )
			args = new String[] { dist.RELEASEDIR , "all" };
		else {
			DeployPlanSet set = selectSet;
			String[] selected = set.getSelected();
			if( selected.length > 0 ) {
				args = new String[ 1 + selected.length ];
				args[ 0 ] = dist.RELEASEDIR;
				for( int k = 0; k < selected.length; k++ )
					args[ 1 + k ] = Common.getPartAfterFirst( selected[ k ] , "::" );
			}
		}
		
		MetaEnvSegment sg = ( selectSg == null )? null : selectSg.sg;
		return( action.runNotifyMethod( app , listener , env.meta , env , sg , DeployCommandMeta.NAME , DeployCommandMeta.METHOD_DEPLOYREDIST , args , options ) );
	}
	
}
