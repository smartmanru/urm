package org.urm.action;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.Release;
import org.urm.engine.dist.ReleaseDelivery;
import org.urm.engine.dist.ReleaseDistSet;
import org.urm.engine.dist.ReleaseTarget;
import org.urm.engine.dist.ReleaseTargetItem;
import org.urm.engine.shell.Account;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.product.MetaEnvStartGroup;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.Types.*;

public class ActionScopeSet {

	ActionScope scope;
	public Meta meta;
	public String NAME;
	public VarCATEGORY CATEGORY;
	public boolean setFull;
	
	public MetaSourceProjectSet pset;
	public ReleaseDistSet rset;
	
	Map<String,ActionScopeTarget> targets = new HashMap<String,ActionScopeTarget>();

	public MetaEnv env;
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
	
	public boolean isEmpty() {
		// manual files are by default
		if( CATEGORY == VarCATEGORY.MANUAL && setFull )
			return( false );
		
		return( targets.isEmpty() );
	}
	
	public Map<String,ActionScopeTarget> getTargets( ActionBase action ) throws Exception {
		return( targets );
	}
	
	public void create( ActionBase action , MetaSourceProjectSet pset ) throws Exception {
		this.pset = pset;
		this.NAME = pset.NAME;
		this.CATEGORY = VarCATEGORY.PROJECT;
		this.setFull = false;
	}

	public void create( ActionBase action , ReleaseDistSet rset ) throws Exception {
		this.rset = rset;
		this.pset = rset.set;
		this.NAME = rset.NAME;
		this.CATEGORY = rset.CATEGORY;
		this.setFull = false;
	}

	public void create( ActionBase action , VarCATEGORY CATEGORY ) throws Exception {
		this.pset = null;
		this.NAME = Common.getEnumLower( CATEGORY );
		this.CATEGORY = CATEGORY;
		this.setFull = false;
	}

	public void create( ActionBase action , MetaEnv env , MetaEnvSegment sg ) throws Exception {
		this.pset = null;
		this.NAME = sg.NAME;
		this.CATEGORY = VarCATEGORY.ENV;
		this.setFull = false;
		this.env = env;
		this.sg = sg;
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
			if( CATEGORY == VarCATEGORY.MANUAL && setFull )
				return( "manual files" );
			return( "" );
		}
		
		String scope = NAME + "={";
		if( setFull ) {
			scope += "all}";
			return( scope );
		}

		String itemlist = "";
		if( CATEGORY == VarCATEGORY.MANUAL )
			itemlist = "manual files";
		else
		if( CATEGORY == VarCATEGORY.DERIVED )
			itemlist = "derived files";
		for( ActionScopeTarget scopeTarget : targets.values() )
			itemlist = Common.concat( itemlist , scopeTarget.getScopeInfo( action ) , ", " );
		
		scope += itemlist + "}";
		return( scope );
	}

	private boolean checkServerDatabaseDelivery( ActionBase action , MetaEnvServer server , ReleaseDelivery delivery ) throws Exception {
		return( server.hasDatabaseItemDeployment( delivery.distDelivery ) );
	}
	
	private boolean checkServerDelivery( ActionBase action , MetaEnvServer server , ReleaseDelivery delivery ) throws Exception {
		if( action.context.CTX_CONFDEPLOY ) {
			for( ReleaseTarget target : delivery.getConfItems() ) {
				if( server.hasConfItemDeployment( target.distConfItem ) )
					return( true );
			}
		}
		
		for( ReleaseTargetItem item : delivery.getDatabaseItems() ) {
			if( server.hasDatabaseItemDeployment( item.schema ) )
				return( true );
		}

		if( action.context.CTX_DEPLOYBINARY ) {
			for( ReleaseTarget target : delivery.getManualItems() ) {
				if( server.hasBinaryItemDeployment( target.distManualItem ) )
					return( true );
			}
			for( ReleaseTargetItem item : delivery.getProjectItems() ) {
				if( server.hasBinaryItemDeployment( item.distItem ) )
					return( true );
			}
		}
		
		return( false );
	}
	
	public Map<String,MetaEnvServer> getReleaseServers( ActionBase action , Dist release ) throws Exception {
		Map<String,MetaEnvServer> mapServers = new HashMap<String,MetaEnvServer>();
		Release info = release.release;

		for( ReleaseDelivery delivery : info.getDeliveries() ) {
			for( MetaEnvServer server : sg.getServers() ) {
				if( checkServerDelivery( action , server , delivery ) )
					mapServers.put( server.NAME , server );
			}
		}
		return( mapServers );
	}
	
	public Map<String,MetaEnvServer> getEnvDatabaseServers( ActionBase action , Dist dist ) throws Exception {
		Map<String,MetaEnvServer> mapServers = new HashMap<String,MetaEnvServer>();
		if( dist == null ) {
			for( MetaEnvServer server : sg.getServers() )
				mapServers.put( server.NAME , server );
			return( mapServers );
		}
		
		Release info = dist.release;

		for( ReleaseDelivery delivery : info.getDeliveries() ) {
			for( MetaEnvServer server : sg.getServers() ) {
				if( checkServerDatabaseDelivery( action , server , delivery ) )
					mapServers.put( server.NAME , server );
			}
		}
		return( mapServers );
	}
	
	public ActionScopeTarget findTarget( ActionBase action , String NAME ) throws Exception {
		ActionScopeTarget target = targets.get( NAME );
		return( target );
	}
	
	public Account[] getUniqueHosts( ActionBase action , ActionScopeTarget[] targets ) throws Exception {
		Map<String,Account> map = new HashMap<String,Account>(); 
		for( ActionScopeTarget target : targets ) {
			for( ActionScopeTargetItem item : target.getItems( action ) ) {
				Account account = action.getNodeAccount( item.envServerNode );
				map.put( account.HOST , action.getNodeAccount( item.envServerNode ) );
			}
		}
		
		String[] keys = Common.getSortedKeys( map );
		Account[] accounts = new Account[ keys.length ];
		
		for( int k = 0; k < keys.length; k++ )
			accounts[ k ] = map.get( keys[ k ] );
		
		return( accounts );
	}
	
	public Account[] getUniqueAccounts( ActionBase action , ActionScopeTarget[] targets ) throws Exception {
		Map<String,MetaEnvServerNode> map = new HashMap<String,MetaEnvServerNode>(); 
		for( ActionScopeTarget target : targets ) {
			for( ActionScopeTargetItem item : target.getItems( action ) )
				map.put( item.envServerNode.HOSTLOGIN , item.envServerNode );
		}
		
		String[] hostLogins = Common.getSortedKeys( map );
		Account[] accounts = new Account[ hostLogins.length ];
		
		for( int k = 0; k < hostLogins.length; k++ )
			accounts[ k ] = action.getNodeAccount( map.get( hostLogins[ k ] ) );
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
		this.rset = setAdd.rset;

		this.env = setAdd.env;
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
	
}
