package org.urm.action.build;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.RunError;
import org.urm.common.action.CommandOptions;
import org.urm.common.meta.ReleaseCommandMeta;
import org.urm.engine.ServerEventsApp;
import org.urm.engine.ServerEventsListener;
import org.urm.engine.dist.Dist;

public class BuildPlan {
	
	public List<BuildPlanSet> listSets;
	Map<String,BuildPlanSet> mapSets;
	public Dist dist;
	public BuildPlanSet selectSet;
	public RunError error;
	
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
	
	public boolean executeBuild( ActionBase action , ServerEventsApp app , ServerEventsListener listener , CommandOptions options ) {
		if( !executeCompile( action , app , listener , options ) )
			return( false );
		if( !executeConf( action , app , listener , options ) )
			return( false );
		if( !executeDatabase( action , app , listener , options ) )
			return( false );
		return( true );
	}
	
	public boolean executeCompile( ActionBase action , ServerEventsApp app , ServerEventsListener listener , CommandOptions options ) {
		String[] args = null;
		boolean run = true;
		
		// compile and get build results
		if( hasCompile() ) {
			run = false;
			if( isCompileAll() ) {
				args = new String[] { dist.RELEASEDIR , "all" };
				run = true;
			}
			else
			if( isCompileSelected() ) {
				BuildPlanSet set = selectSet;
				String[] selected = set.getSelected();
				
				args = new String[ 2 + selected.length ];
				args[ 0 ] = dist.RELEASEDIR;
				args[ 1 ] = set.set.set.NAME;
				for( int k = 0; k < selected.length; k++ )
					args[ 2 + k ] = Common.getPartAfterFirst( selected[ k ] , "::" );
				run = true;
			}
			
			if( run ) {
				error = action.runNotifyMethod( app , listener , dist.meta , null , null , ReleaseCommandMeta.NAME , ReleaseCommandMeta.METHOD_BUILD , args , options );
				if( error != null )
					return( false );
			}
		}
		return( true );
	}
	
	public boolean executeConf( ActionBase action , ServerEventsApp app , ServerEventsListener listener , CommandOptions options ) {
		String[] args = null;
		boolean run = true;
			
		if( hasConf() ) {
			run = false;
			if( isConfAll() ) {
				args = new String[] { dist.RELEASEDIR , "config" , "all" };
				run = true;
			}
			if( isDatabaseSelected() ) {
				BuildPlanSet set = selectSet;
				String[] selected = set.getSelected();
				
				args = new String[ 2 + selected.length ];
				args[ 0 ] = dist.RELEASEDIR;
				args[ 1 ] = "config";
				run = true;
			}
			
			if( run ) {
				error = action.runNotifyMethod( app , listener , dist.meta , null , null , ReleaseCommandMeta.NAME , ReleaseCommandMeta.METHOD_GETDIST , args , options );
				if( error != null )
					return( false );
			}
		}
		return( true );
	}
	
	public boolean executeDatabase( ActionBase action , ServerEventsApp app , ServerEventsListener listener , CommandOptions options ) {
		String[] args = null;
		boolean run = true;
		
		if( hasDatabase() ) {
			run = false;
			if( isDatabaseAll() ) {
				args = new String[] { dist.RELEASEDIR , "db" , "all" };
				run = true;
			}
			if( isDatabaseSelected() ) {
				BuildPlanSet set = selectSet;
				String[] selected = set.getSelected();
				
				args = new String[ 2 + selected.length ];
				args[ 0 ] = dist.RELEASEDIR;
				args[ 1 ] = "db";
				run = true;
			}
			
			if( run ) {
				error = action.runNotifyMethod( app , listener , dist.meta , null , null , ReleaseCommandMeta.NAME , ReleaseCommandMeta.METHOD_GETDIST , args , options );
				if( error != null )
					return( false );
			}
		}
		
		return( true );
	}
	
}
