package org.urm.engine.dist;

import java.util.Map;

import org.urm.common.Common;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.product.MetaSources;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseBuildTarget;
import org.urm.meta.release.ReleaseDistTarget;

public class ReleaseBuildScope {

	public Release release;
	public ReleaseBuildTarget scopeTarget;
	
	private Map<String,ReleaseBuildScopeSet> mapSet;
	
	private ReleaseBuildScope( Release release ) {
		this.release = release;
	}
	
	public static ReleaseBuildScope createScope( Release release ) throws Exception {
		ReleaseBuildScope scope = new ReleaseBuildScope( release );
		Meta meta = release.getMeta();
		MetaSources sources = meta.getSources();
		for( MetaSourceProjectSet set : sources.getSets() ) {
			ReleaseBuildScopeSet scopeSet = createSetScope( release , set );
			if( !scopeSet.isEmpty() )
				scope.addSet( scopeSet );
		}
		
		for( ReleaseBuildTarget target : release.getScopeBuildTargets() ) {
			if( target.isBuildAll() ) {
				scope.scopeTarget = target;
				break;
			}
		}
		
		return( scope );
	}

	public static ReleaseBuildScopeSet createSetScope( Release release , MetaSourceProjectSet set ) throws Exception {
		ReleaseBuildScopeSet scopeSet = new ReleaseBuildScopeSet( release , set );
		createBuildableScopeSet( release , scopeSet );
		return( scopeSet );
	}

	private static void createBuildableScopeSet( Release release , ReleaseBuildScopeSet scopeSet ) throws Exception {
		for( ReleaseBuildTarget target : release.getScopeBuildTargets() ) {
			if( target.isBuildAll() )
				scopeSet.scopeTarget = target;
			if( target.isBuildSet() && target.SRCSET.equals( scopeSet.set.ID ) && target.ALL )
				scopeSet.scopeSetTarget = target;
		}
		
		for( MetaSourceProject project : scopeSet.set.getProjects() ) {
			boolean matched = false;
			for( ReleaseBuildTarget target : release.getScopeBuildTargets() ) {
				if( target.isBuildAll() ) {
					matched = true;
					break;
				}
				if( target.isBuildSet() && target.SRCSET.equals( project.set.ID ) && target.ALL ) {
					matched = true;
					break;
				}
				if( target.isBuildProject() && target.PROJECT.equals( project.ID ) ) {
					matched = true;
					break;
				}
			}

			if( !matched ) {
				if( checkScopeDistProjectItems( release , project ) )
					matched = true;
			}

			if( matched ) {
				if( scopeSet.findProject( project ) == null ) {
					ReleaseBuildScopeProject scopeProject = new ReleaseBuildScopeProject( release , project );
					scopeSet.addProject( scopeProject );
					createBuildableScopeProject( release , scopeProject );
				}
			}
		}
	}

	private static boolean checkScopeDistProjectItems( Release release , MetaSourceProject project ) throws Exception {
		Meta meta = release.getMeta();
		MetaDistr distr = meta.getDistr();
		
		for( ReleaseDistTarget target : release.getScopeDistTargets() ) {
			if( target.isDistAll() ) {
				if( project.hasDistItems() )
					return( true );
			}
			else
			if( target.isDeliveryBinaries() ) {
				MetaDistrDelivery delivery = distr.getDelivery( target.DELIVERY );
				for( MetaDistrBinaryItem item : delivery.getBinaryItems() ) {
					if( item.isProjectItem() && item.sourceProjectItem.project.ID == project.ID )
						return( true );
				}
			}
			else
			if( target.isBinaryItem() ) {
				MetaDistrBinaryItem item = distr.getBinaryItem( target.BINARY );
				if( item.isProjectItem() && item.sourceProjectItem.project.ID == project.ID )
					return( true );
			}
		}
		
		return( false );
	}

	private static void createBuildableScopeProject( Release release , ReleaseBuildScopeProject scopeProject ) throws Exception {
		boolean allItems = false;
		for( ReleaseBuildTarget target : release.getScopeBuildTargets() ) {
			if( target.isBuildProject() && target.PROJECT.equals( scopeProject.project.ID ) ) {
				scopeProject.scopeProjectTarget = target;
				if( target.ALL )
					allItems = true;
				break;
			}
		}
		
		if( allItems ) {
			createBuildableScopeProjectAllItems( release , scopeProject );
			return;
		}
		
		Meta meta = release.getMeta();
		MetaDistr distr = meta.getDistr();
		for( ReleaseDistTarget target : release.getScopeDistTargets() ) {
			if( target.isDistAll() ) {
				createBuildableScopeProjectAllItems( release , scopeProject );
				return;
			}
			if( target.isDeliveryBinaries() ) {
				MetaDistrDelivery delivery = distr.getDelivery( target.DELIVERY );
				for( MetaDistrBinaryItem item : delivery.getBinaryItems() ) {
					if( item.isProjectItem() && item.sourceProjectItem.project.ID == scopeProject.project.ID )
						createItem( release , scopeProject , item.sourceProjectItem );
				}
			}
			if( target.isBinaryItem() ) {
				MetaDistrBinaryItem item = distr.getBinaryItem( target.BINARY );
				if( item.isProjectItem() && item.sourceProjectItem.project.ID == scopeProject.project.ID )
					createItem( release , scopeProject , item.sourceProjectItem );
			}
		}
	}
	
	private static void createBuildableScopeProjectAllItems( Release release , ReleaseBuildScopeProject scopeProject ) throws Exception {
		scopeProject.setAll( true );
		for( MetaSourceProjectItem item : scopeProject.project.getItems() )
			createItem( release , scopeProject , item );
	}

	private static void createItem( Release release , ReleaseBuildScopeProject scopeProject , MetaSourceProjectItem item ) {
		if( scopeProject.findItem( item ) == null ) {
			ReleaseBuildScopeProjectItem scopeItem = new ReleaseBuildScopeProjectItem( release , item );
			scopeProject.addItem( scopeItem );
		}
	}
	
	private void addSet( ReleaseBuildScopeSet scopeSet ) {
		mapSet.put( scopeSet.set.NAME , scopeSet );
	}

	public ReleaseBuildScopeSet findSet( String setName ) {
		return( mapSet.get( setName ) );
	}

	public ReleaseBuildScopeSet[] getSets() {
		return( mapSet.values().toArray( new ReleaseBuildScopeSet[0] ) );
	}

	public ReleaseBuildScopeProject findProject( String projectName ) {
		Meta meta = release.getMeta();
		MetaSources sources = meta.getSources();
		MetaSourceProject project = sources.findProject( projectName );
		if( project == null )
			return( null );
		
		ReleaseBuildScopeSet set = findSet( project.set.NAME );
		if( set == null )
			return( null );
		
		return( set.findProject( project ) );
	}

	public String[] getSetNames() {
		return( Common.getSortedKeys( mapSet ) ); 
	}
	
}
