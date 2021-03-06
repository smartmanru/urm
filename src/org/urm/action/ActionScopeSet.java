package org.urm.action;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.dist.ReleaseBuildScopeSet;
import org.urm.engine.dist.ReleaseDistScopeDelivery;
import org.urm.engine.dist.ReleaseDistScopeDeliveryItem;
import org.urm.engine.dist.ReleaseDistScopeSet;
import org.urm.engine.shell.Account;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvStartGroup;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectSet;

public class ActionScopeSet {

	public ActionScope scope;
	public Meta meta;
	public String NAME;
	public DBEnumScopeCategoryType CATEGORY;
	public boolean setFull;
	
	public MetaSourceProjectSet pset;
	public ReleaseBuildScopeSet releaseBuildScopeSet;
	public ReleaseDistScopeSet releaseDistScopeSet;
	
	Map<String,ActionScopeTarget> targets = new HashMap<String,ActionScopeTarget>();

	public MetaEnvSegment sg;
	boolean specifiedExplicitly;
	
	public ActionScopeSet( ActionScope scope , boolean specifiedExplicitly ) {
		this.scope = scope;
		this.meta = scope.meta;
		this.specifiedExplicitly = specifiedExplicitly;
		this.setFull = false;
	}
	
	public void setFullContent( boolean full ) {
		this.setFull = full;
	}
	
	public boolean isFull() {
		return( setFull );
	}
	
	public boolean isEmpty() {
		// manual files are by default
		if( CATEGORY == DBEnumScopeCategoryType.MANUAL && setFull )
			return( false );
		
		return( targets.isEmpty() );
	}
	
	public ActionScopeTarget[] getTargets() throws Exception {
		List<ActionScopeTarget> list = new LinkedList<ActionScopeTarget>();
		for( String name : Common.getSortedKeys( targets ) ) {
			ActionScopeTarget target = targets.get( name );
			list.add( target );
		}
		return( list.toArray( new ActionScopeTarget[0] ) );
	}
	
	public void create( ActionBase action , MetaSourceProjectSet pset ) throws Exception {
		this.pset = pset;
		this.NAME = pset.NAME;
		this.CATEGORY = DBEnumScopeCategoryType.PROJECT;
		this.setFull = false;
	}

	public void create( ActionBase action , ReleaseDistScopeSet rset ) throws Exception {
		this.releaseDistScopeSet = rset;
		this.CATEGORY = rset.CATEGORY;
		this.NAME = Common.getEnumLower( CATEGORY );
		this.setFull = false;
	}

	public void create( ActionBase action , ReleaseBuildScopeSet rset ) throws Exception {
		this.releaseBuildScopeSet = rset;
		this.pset = rset.set;
		this.CATEGORY = DBEnumScopeCategoryType.PROJECT;
		this.NAME = pset.NAME;
		this.setFull = false;
	}

	public void create( ActionBase action , MetaEnvSegment sg ) throws Exception {
		this.pset = null;
		this.NAME = sg.NAME;
		this.CATEGORY = DBEnumScopeCategoryType.ENV;
		this.setFull = false;
		this.sg = sg;
	}

	public void create( ActionBase action , DBEnumScopeCategoryType CATEGORY ) throws Exception {
		this.pset = null;
		this.NAME = Common.getEnumLower( CATEGORY );
		this.CATEGORY = CATEGORY;
		this.setFull = false;
	}

	public void addTarget( ActionBase action , ActionScopeTarget target ) throws Exception {
		action.trace( "scope: add target=" + target.NAME );
		targets.put( target.NAME , target );
	}
	
	public ActionScopeTarget findSourceTarget( ActionBase action , MetaSourceProject project ) throws Exception {
		return( targets.get( project.NAME ) );
	}
	
	public String getScopeInfo( ActionBase action ) throws Exception {
		if( targets.isEmpty() ) {
			if( CATEGORY == DBEnumScopeCategoryType.MANUAL && setFull )
				return( "manual files" );
			return( "" );
		}
		
		String scope = NAME + "={";
		if( setFull ) {
			scope += "all}";
			return( scope );
		}

		String itemlist = "";
		if( CATEGORY == DBEnumScopeCategoryType.MANUAL )
			itemlist = "manual files";
		else
		if( CATEGORY == DBEnumScopeCategoryType.DERIVED )
			itemlist = "derived files";
		
		for( ActionScopeTarget scopeTarget : targets.values() )
			itemlist = Common.concat( itemlist , scopeTarget.getScopeInfo( action ) , ", " );
		
		scope += itemlist + "}";
		return( scope );
	}

	private boolean checkServerDelivery( ActionBase action , MetaEnvServer server , ReleaseDistScopeDelivery delivery ) throws Exception {
		for( ReleaseDistScopeDeliveryItem item : delivery.getItems() ) {
			if( delivery.CATEGORY == DBEnumScopeCategoryType.CONFIG ) {
				if( action.context.CTX_CONFDEPLOY ) {
					if( server.hasConfItemDeployment( item.conf ) )
						return( true );
				}				
			}
			else
			if( delivery.CATEGORY == DBEnumScopeCategoryType.BINARY ) {
				if( action.context.CTX_DEPLOYBINARY ) {
					if( server.hasBinaryItemDeployment( item.binary ) )
						return( true );
				}
			}
			else
			if( delivery.CATEGORY == DBEnumScopeCategoryType.DB ) {
				if( server.hasDatabaseItemDeployment( item.schema ) )
					return( true );
			}
		}
		
		return( false );
	}
	
	public Map<String,MetaEnvServer> getReleaseServers( ActionBase action ) throws Exception {
		Map<String,MetaEnvServer> mapServers = new HashMap<String,MetaEnvServer>();

		for( ReleaseDistScopeSet set : scope.releaseDistScope.getSets() ) {
			for( ReleaseDistScopeDelivery delivery : set.getDeliveries() ) {
				for( MetaEnvServer server : sg.getServers() ) {
					if( checkServerDelivery( action , server , delivery ) )
						mapServers.put( server.NAME , server );
				}
			}
		}
		return( mapServers );
	}
	
	public Map<String,MetaEnvServer> getEnvDatabaseServers( ActionBase action ) throws Exception {
		Map<String,MetaEnvServer> mapServers = new HashMap<String,MetaEnvServer>();
		for( MetaEnvServer server : sg.getServers() ) {
			if( server.isRunDatabase() )
				mapServers.put( server.NAME , server );
		}
		return( mapServers );
	}
	
	public Map<String,MetaEnvServer> getEnvDatabaseReleaseServers( ActionBase action ) throws Exception {
		Map<String,MetaEnvServer> mapServers = new HashMap<String,MetaEnvServer>();
		for( ReleaseDistScopeSet set : scope.releaseDistScope.getSets() ) {
			if( set.CATEGORY != DBEnumScopeCategoryType.DB )
				continue;
			
			for( ReleaseDistScopeDelivery delivery : set.getDeliveries() ) {
				for( MetaEnvServer server : sg.getServers() ) {
					if( server.isRunDatabase() && server.hasDatabaseItemDeployment( delivery.distDelivery ) )
						mapServers.put( server.NAME , server );
				}
			}
		}
		return( mapServers );
	}
	
	public ActionScopeTarget findTarget( ActionBase action , String NAME ) throws Exception {
		ActionScopeTarget target = targets.get( NAME );
		return( target );
	}
	
	public Account[] getUniqueHosts( ActionBase action , ActionScopeTarget[] targets , Map<Account,ActionScopeTargetItem[]> data ) throws Exception {
		Map<String,Account> map = new HashMap<String,Account>();
		Map<String,List<ActionScopeTargetItem>> items = new HashMap<String,List<ActionScopeTargetItem>>();
		for( ActionScopeTarget target : targets ) {
			for( ActionScopeTargetItem item : target.getItems( action ) ) {
				Account account = action.getNodeAccount( item.envServerNode );
				map.put( account.HOST , account );
				
				List<ActionScopeTargetItem> list = items.get( account.HOST );
				if( list == null ) {
					list = new LinkedList<ActionScopeTargetItem>();
					items.put( account.HOST , list );
				}
				
				list.add( item );
			}
		}
		
		String[] keys = Common.getSortedKeys( map );
		Account[] accounts = new Account[ keys.length ];
		
		for( int k = 0; k < keys.length; k++ ) {
			Account account = map.get( keys[ k ] );
			accounts[ k ] = account;
			
			List<ActionScopeTargetItem> list = items.get( account.HOST );
			data.put( account , list.toArray( new ActionScopeTargetItem[0] ) );
		}
		
		return( accounts );
	}
	
	public Account[] getUniqueAccounts( ActionBase action , ActionScopeTarget[] targets , Map<Account,ActionScopeTargetItem[]> data ) throws Exception {
		Map<String,Account> map = new HashMap<String,Account>();
		Map<String,List<ActionScopeTargetItem>> items = new HashMap<String,List<ActionScopeTargetItem>>();
		for( ActionScopeTarget target : targets ) {
			for( ActionScopeTargetItem item : target.getItems( action ) ) {
				Account account = action.getNodeAccount( item.envServerNode );
				String hostLogin = account.getHostLogin();
				map.put( hostLogin , account );
				
				List<ActionScopeTargetItem> list = items.get( hostLogin );
				if( list == null ) {
					list = new LinkedList<ActionScopeTargetItem>();
					items.put( hostLogin , list );
				}
				
				list.add( item );
			}
		}
		
		String[] keys = Common.getSortedKeys( map );
		Account[] accounts = new Account[ keys.length ];
		
		for( int k = 0; k < keys.length; k++ ) {
			Account account = map.get( keys[ k ] );
			accounts[ k ] = account;
			
			String hostLogin = account.getHostLogin();
			List<ActionScopeTargetItem> list = items.get( hostLogin );
			data.put( account , list.toArray( new ActionScopeTargetItem[0] ) );
		}
		
		return( accounts );
	}

	public List<ActionScopeTarget> getGroupServers( ActionBase action , MetaEnvStartGroup group ) throws Exception {
		List<ActionScopeTarget> groupTargets = new LinkedList<ActionScopeTarget>();
		for( MetaEnvServer server : group.getServers() ) {
			ActionScopeTarget target = targets.get( server.NAME );
			if( target != null )
				groupTargets.add( target );
		}
		return( groupTargets );
	}

	public void createMinusSet( ActionBase action , ActionScopeSet setAdd , ActionScopeSet setRemove ) throws Exception {
		this.setFull = setAdd.setFull;
		this.NAME = setAdd.NAME;
		this.CATEGORY = setAdd.CATEGORY;
		
		this.pset = setAdd.pset;
		this.releaseBuildScopeSet = setAdd.releaseBuildScopeSet;
		this.releaseDistScopeSet = setAdd.releaseDistScopeSet;
		this.sg = setAdd.sg;
		
		for( ActionScopeTarget target : setAdd.targets.values() )
			createMinusTarget( action , target , setRemove );
	}	

	public void createMinusTarget( ActionBase action , ActionScopeTarget targetAdd , ActionScopeSet setRemove ) throws Exception {
		ActionScopeTarget targetRemove = null;
		if( setRemove != null )
			targetRemove = setRemove.findSimilarTarget( action , targetAdd );
		
		if( targetRemove != null ) {
			if( targetRemove.isLeafTarget() )
				return;
		}

		ActionScopeTarget targetNew = targetAdd.copy( this );
		if( !targetNew.isLeafTarget() ) {
			targetNew.createMinusTarget( action , targetAdd , targetRemove );
			if( targetNew.isEmpty() )
				return;
		}
		
		addTarget( action , targetNew );
	}

	public ActionScopeTarget findSimilarTarget( ActionBase action , ActionScopeTarget sample ) throws Exception {
		return( findTarget( action , sample.NAME ) );
	}

	public String getSetBuildVersion() {
		String BUILDVERSION = "";
		if( releaseBuildScopeSet != null && releaseBuildScopeSet.scopeSetTarget != null )
			BUILDVERSION = releaseBuildScopeSet.scopeSetTarget.BUILD_VERSION;
			
		if( BUILDVERSION.isEmpty() && releaseBuildScopeSet.scopeTarget != null )
			BUILDVERSION = releaseBuildScopeSet.scopeTarget.BUILD_VERSION;
		
		return( BUILDVERSION );
	}
	
	public String getSetBuildBranch() {
		String BUILDBRANCH = "";
		if( releaseBuildScopeSet != null && releaseBuildScopeSet.scopeSetTarget != null )
			BUILDBRANCH = releaseBuildScopeSet.scopeSetTarget.BUILD_BRANCH;
			
		if( BUILDBRANCH.isEmpty() && releaseBuildScopeSet.scopeTarget != null )
			BUILDBRANCH = releaseBuildScopeSet.scopeTarget.BUILD_BRANCH;
		
		return( BUILDBRANCH );
	}
	
	public String getSetBuildTag() {
		String BUILDTAG = "";
		if( releaseBuildScopeSet != null && releaseBuildScopeSet.scopeSetTarget != null )
			BUILDTAG = releaseBuildScopeSet.scopeSetTarget.BUILD_TAG;
			
		if( BUILDTAG.isEmpty() && releaseBuildScopeSet.scopeTarget != null )
			BUILDTAG = releaseBuildScopeSet.scopeTarget.BUILD_TAG;
		
		return( BUILDTAG );
	}
	
}
