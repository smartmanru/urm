package org.urm.action;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.action.CommandContext;
import org.urm.engine.dist.ReleaseBuildScope;
import org.urm.engine.dist.ReleaseBuildScopeSet;
import org.urm.engine.dist.ReleaseDistScope;
import org.urm.engine.dist.ReleaseDistScopeSet;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.release.Release;

public class ActionScope {

	public Meta meta;
	public CommandContext context;

	private Map<DBEnumScopeCategoryType,ActionScopeSet> categoryMap = new HashMap<DBEnumScopeCategoryType,ActionScopeSet>();
	private Map<String,ActionScopeSet> sourceMap = new HashMap<String,ActionScopeSet>();
	private Map<String,ActionScopeSet> envMap = new HashMap<String,ActionScopeSet>();

	public MetaEnv env;
	public boolean scopeFullProduct;
	public boolean scopeFullEnv;
	public boolean scopeFullRelease;
	
	public Release release;
	public ReleaseBuildScope releaseBuildScope;
	public ReleaseDistScope releaseDistScope;

	public ActionScope( ActionBase action ) {
		this.context = action.context;
		this.scopeFullProduct = false;
		this.scopeFullEnv = false;
		this.scopeFullRelease = false;
	}
	
	public ActionScope( ActionBase action , Meta meta ) {
		this.meta = meta;
		this.context = action.context;
		this.scopeFullProduct = false;
		this.scopeFullEnv = false;
		this.scopeFullRelease = false;
	}
	
	public ActionScope( ActionBase action , MetaEnv env ) {
		this.meta = env.meta;
		this.env = env;
		this.context = action.context;
		this.scopeFullProduct = false;
		this.scopeFullEnv = false;
		this.scopeFullRelease = false;
	}
	
	public void setReleaseBuildScope( Release release ) throws Exception {
		if( releaseBuildScope != null )
			return;

		this.release = release;
		this.releaseBuildScope = ReleaseBuildScope.createScope( release );
	}
	
	public void setReleaseDistScope( Release release ) throws Exception {
		if( releaseDistScope != null )
			return;

		this.release = release;
		this.releaseDistScope = ReleaseDistScope.createScope( release );
	}
	
	public void setIncomplete() {
		this.scopeFullProduct = false;
		this.scopeFullEnv = false;
		this.scopeFullRelease = false;
	}
	
	public void setFullProduct( ActionBase action , boolean full ) throws Exception {
		this.scopeFullProduct = full;
	}
	
	public void setFullEnv( ActionBase action , boolean full ) throws Exception {
		this.scopeFullEnv = full;
	}
	
	public void setFullRelease( ActionBase action , boolean full ) throws Exception {
		this.scopeFullRelease = full;
	}
	
	public boolean isPartialProduct() {
		return( !scopeFullProduct );
	}

	public boolean isPartialEnv() {
		return( !scopeFullEnv );
	}

	public boolean isPartialRelease() {
		return( !scopeFullRelease );
	}

	public boolean isFull() {
		if( scopeFullProduct || scopeFullEnv || scopeFullRelease )
			return( true );
		return( false );
	}
	
	public ActionScopeSet makeProjectScopeSet( ActionBase action , MetaSourceProjectSet pset ) throws Exception {
		ActionScopeSet sset = sourceMap.get( pset.NAME );
		if( sset != null )
			return( sset );
		
		sset = new ActionScopeSet( this , true );
		sset.create( action , pset );
		addScopeSet( action , sset );
		return( sset );
	}
	
	public ActionScopeSet makeProductCategoryScopeSet( ActionBase action , DBEnumScopeCategoryType CATEGORY ) throws Exception {
		ActionScopeSet sset = getCategorySet( action , CATEGORY );
		if( sset != null )
			return( sset );
		
		sset = new ActionScopeSet( this , true );
		sset.create( action , CATEGORY );
		addScopeSet( action , sset );
		return( sset );
	}
	
	public ActionScopeSet makeEnvScopeSet( ActionBase action , MetaEnv env , MetaEnvSegment sg , boolean specifiedExplicitly ) throws Exception {
		ActionScopeSet sset = getCategorySet( action , DBEnumScopeCategoryType.ENV );
		if( sset != null )
			return( sset );
		
		sset = new ActionScopeSet( this , specifiedExplicitly );
		sset.create( action , sg );
		addScopeSet( action , sset );
		return( sset );
	}
	
	public ActionScopeSet makeReleaseCategoryScopeSet( ActionBase action , DBEnumScopeCategoryType CATEGORY ) throws Exception {
		ActionScopeSet sset = getCategorySet( action , CATEGORY );
		if( sset != null )
			return( sset );
		
		ReleaseDistScopeSet rset = releaseDistScope.findCategorySet( CATEGORY );
		if( rset == null ) {
			action.debug( "ignore non-release set=" + Common.getEnumLower( CATEGORY ) );
			return( null );
		}
		
		sset = new ActionScopeSet( this , true );
		sset.create( action , rset );
		action.trace( "add scope set category=" + Common.getEnumLower( CATEGORY ) );
		addScopeSet( action , sset );
		return( sset );
	}
	
	public ActionScopeSet makeReleaseScopeSet( ActionBase action , ReleaseBuildScopeSet rset ) throws Exception {
		ActionScopeSet sset = getScopeSet( action , DBEnumScopeCategoryType.PROJECT , rset.set.NAME );
		if( sset != null )
			return( sset );
		
		sset = new ActionScopeSet( this , false );
		sset.create( action , rset );
		addScopeSet( action , sset );
		return( sset );
	}

	private ActionScopeSet getCategorySet( ActionBase action , DBEnumScopeCategoryType CATEGORY ) throws Exception {
		return( categoryMap.get( CATEGORY ) );
	}
	
	private ActionScopeSet getScopeSet( ActionBase action , DBEnumScopeCategoryType CATEGORY , String name ) throws Exception {
		if( CATEGORY.isSource() )
			return( sourceMap.get( name ) );
		if( CATEGORY == DBEnumScopeCategoryType.ENV )
			return( envMap.get( name ) );
		return( categoryMap.get( CATEGORY ) );
	}

	public boolean hasCategorySet( ActionBase action , DBEnumScopeCategoryType CATEGORY ) throws Exception {
		ActionScopeSet sset = getCategorySet( action , CATEGORY );
		if( sset == null || sset.isEmpty() )
			return( false );
		
		return( true );
	}
	
	public boolean hasConfig( ActionBase action ) throws Exception {
		return( hasCategorySet( action , DBEnumScopeCategoryType.CONFIG ) );
	}
	
	public boolean hasManual( ActionBase action ) throws Exception {
		return( hasCategorySet( action , DBEnumScopeCategoryType.MANUAL ) );
	}

	public boolean hasDerived( ActionBase action ) throws Exception {
		return( hasCategorySet( action , DBEnumScopeCategoryType.DERIVED ) );
	}

	public boolean hasDatabase( ActionBase action ) throws Exception {
		return( hasCategorySet( action , DBEnumScopeCategoryType.DB ) );
	}

	public boolean hasDoc( ActionBase action ) throws Exception {
		return( hasCategorySet( action , DBEnumScopeCategoryType.DOC ) );
	}

	public List<ActionScopeSet> getSetList() {
		List<ActionScopeSet> list = new LinkedList<ActionScopeSet>();
		list.addAll( sourceMap.values() );
		list.addAll( categoryMap.values() );
		list.addAll( envMap.values() );
		return( list );
	}
	
	public String getScopeInfo( ActionBase action , DBEnumScopeCategoryType[] categories ) throws Exception {
		String scope = "";
		
		boolean all = true;
		for( ActionScopeSet set : getSetList() ) {
			boolean add = true;
			if( categories != null ) {
				add = false;
				for( DBEnumScopeCategoryType CATEGORY : categories ) {
					if( CATEGORY.checkCategoryProperty( set.CATEGORY ) )
						add = true;
				}
			}
			
			if( add )
				scope = Common.concat( scope , set.getScopeInfo( action ) , "; " );
			else
				all = false;
		}

		if( all && isFull() )
			return( "all" );
		
		if( scope.isEmpty() )
			return( "nothing" );
		return( scope );
	}
	
	public String getScopeInfo( ActionBase action ) throws Exception {
		return( getScopeInfo( action , null ) );
	}
	
	public String getBuildScopeInfo( ActionBase action ) throws Exception {
		return( getScopeInfo( action , new DBEnumScopeCategoryType[] { DBEnumScopeCategoryType.SEARCH_SOURCEBUILDABLE } ) );
	}
	
	public String getSourceScopeInfo( ActionBase action ) throws Exception {
		return( getScopeInfo( action , DBEnumScopeCategoryType.getAllSourceCategories() ) );
	}
	
	public boolean isEmpty( ActionBase action , DBEnumScopeCategoryType[] categories ) throws Exception {
		for( ActionScopeSet set : getSetList() ) {
			if( categories == null ) {
				if( !set.isEmpty() )
					return( false );
				continue;
			}
			
			for( DBEnumScopeCategoryType CATEGORY : categories ) {
				if( CATEGORY.checkCategoryProperty( set.CATEGORY ) && !set.isEmpty() )
					return( false );
			}
		}
		
		return( true );
	}
	
	public boolean isEmpty() {
		for( ActionScopeSet set : getSetList() ) {
			if( !set.isEmpty() )
				return( false );
		}
		
		return( true );
	}
	
	public ActionScopeSet[] getSets( ActionBase action ) throws Exception {
		List<ActionScopeSet> x = new LinkedList<ActionScopeSet>();
		for( ActionScopeSet set : getSetList() ) {
			if( !set.isEmpty() )
				x.add( set );
		}
		return( x.toArray( new ActionScopeSet[0] ) );
	}

	public ActionScopeSet[] getSourceSets( ActionBase action ) throws Exception {
		List<ActionScopeSet> x = new LinkedList<ActionScopeSet>();
		for( ActionScopeSet set : sourceMap.values() ) {
			if( !set.isEmpty() )
				x.add( set );
		}
		return( x.toArray( new ActionScopeSet[0] ) );
	}

	public ActionScopeSet[] getCategorySets( ActionBase action ) throws Exception {
		List<ActionScopeSet> x = new LinkedList<ActionScopeSet>();
		for( ActionScopeSet set : categoryMap.values() ) {
			if( !set.isEmpty() )
				x.add( set );
		}
		return( x.toArray( new ActionScopeSet[0] ) );
	}

	public ActionScopeSet[] getEnvSets( ActionBase action ) throws Exception {
		List<ActionScopeSet> x = new LinkedList<ActionScopeSet>();
		for( ActionScopeSet set : envMap.values() ) {
			if( !set.isEmpty() )
				x.add( set );
		}
		return( x.toArray( new ActionScopeSet[0] ) );
	}

	public ActionScopeSet[] getBuildableSets( ActionBase action ) throws Exception {
		List<ActionScopeSet> x = new LinkedList<ActionScopeSet>();
		for( ActionScopeSet set : sourceMap.values() ) {
			if( set.CATEGORY == DBEnumScopeCategoryType.PROJECT && !set.isEmpty() )
				x.add( set );
		}
		return( x.toArray( new ActionScopeSet[0] ) );
	}
	
	public Map<String,ActionScopeTarget> getCategorySetTargets( ActionBase action , DBEnumScopeCategoryType CATEGORY ) throws Exception {
		ActionScopeSet set = getCategorySet( action , CATEGORY );
		if( set == null )
			return( new HashMap<String,ActionScopeTarget>() );
		return( set.targets );
	}
	
	// implementation
	private void addScopeSet( ActionBase action , ActionScopeSet sset ) throws Exception {
		action.trace( "scope: scope add set category=" + Common.getEnumLower( sset.CATEGORY ) + ", name=" + sset.NAME );
		
		if( sset.CATEGORY.isSource() )
			sourceMap.put( sset.NAME , sset );
		else
		if( sset.CATEGORY == DBEnumScopeCategoryType.ENV )
			envMap.put( sset.NAME , sset );
		else
			categoryMap.put( sset.CATEGORY , sset );
	}
	
	public ActionScopeSet findSet( ActionBase action , DBEnumScopeCategoryType CATEGORY , String NAME ) throws Exception {
		if( CATEGORY.isSource() )
			return( sourceMap.get( NAME ) );
		if( CATEGORY == DBEnumScopeCategoryType.ENV )
			return( envMap.get( NAME ) );
		return( categoryMap.get( CATEGORY ) );
	}

	public static String getList( List<ActionScopeTarget> list ) {
		String s = "";
		for( ActionScopeTarget target : list )
			s = Common.addToList( s , target.NAME , "," );
		return( s );
	}
	
	public void createMinus( ActionBase action , ActionScope add , ActionScope remove ) throws Exception {
		this.scopeFullProduct = add.scopeFullProduct;
		this.scopeFullEnv = add.scopeFullEnv;
		this.scopeFullRelease = add.scopeFullRelease;

		for( ActionScopeSet setAdd : add.categoryMap.values() )
			createMinusSet( action , setAdd , remove );
		for( ActionScopeSet setAdd : add.sourceMap.values() )
			createMinusSet( action , setAdd , remove );
		for( ActionScopeSet setAdd : add.envMap.values() )
			createMinusSet( action , setAdd , remove );
	}
	
	private void createMinusSet( ActionBase action , ActionScopeSet setAdd , ActionScope scopeRemove ) throws Exception {
		ActionScopeSet setNew = new ActionScopeSet( this , true );
		if( setAdd.CATEGORY.isSource() )
			setNew.create( action , setAdd.pset );
		else
		if( setAdd.CATEGORY == DBEnumScopeCategoryType.ENV )
			setNew.create( action , setAdd.sg );
		else
			setNew.create( action , setAdd.CATEGORY );
		
		ActionScopeSet setRemove = scopeRemove.findSimilarSet( action , setAdd );
		setNew.createMinusSet( action , setAdd , setRemove );
		
		if( !setNew.isEmpty() )
			addScopeSet( action , setNew );
	}

	public ActionScopeSet findSimilarSet( ActionBase action , ActionScopeSet sample ) throws Exception {
		return( findSet( action , sample.CATEGORY , sample.NAME ) );
	}

}
