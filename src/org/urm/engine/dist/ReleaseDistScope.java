package org.urm.engine.dist;

import java.util.HashMap;
import java.util.Map;

import org.urm.db.core.DBEnums.DBEnumScopeCategoryType;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaProductDoc;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseBuildTarget;
import org.urm.meta.release.ReleaseDistTarget;
import org.urm.meta.release.ReleaseScope;

public class ReleaseDistScope {

	public Release release;

	private Map<DBEnumScopeCategoryType,ReleaseDistScopeSet> mapSet;
	
	public ReleaseDistScope( Release release ) {
		this.release = release;
		mapSet = new HashMap<DBEnumScopeCategoryType,ReleaseDistScopeSet>(); 
	}

	public static ReleaseDistScope createScope( Release release ) throws Exception {
		return( createScope( release , null ) );
	}
	
	public static ReleaseDistScope createScope( Release release , DBEnumScopeCategoryType CATEGORY ) throws Exception {
		ReleaseDistScope scope = new ReleaseDistScope( release );
		
		Meta meta = release.getMeta();
		MetaDistr distr = meta.getDistr();
		ReleaseDistScopeDelivery scopeDelivery;
		
		for( MetaDistrDelivery delivery : distr.getDeliveries() ) {
			if( ( CATEGORY == null || CATEGORY == DBEnumScopeCategoryType.BINARY ) && delivery.hasBinaryItems() ) {
				scopeDelivery = createDeliveryScope( release , delivery , DBEnumScopeCategoryType.BINARY );
				addDelivery( release , scope , scopeDelivery );
			}
			if( ( CATEGORY == null || CATEGORY == DBEnumScopeCategoryType.CONFIG ) && delivery.hasConfItems() ) {
				scopeDelivery = createDeliveryScope( release , delivery , DBEnumScopeCategoryType.CONFIG );
				addDelivery( release , scope , scopeDelivery );
			}
			if( ( CATEGORY == null || CATEGORY == DBEnumScopeCategoryType.DB ) && delivery.hasDatabaseItems() ) {
				scopeDelivery = createDeliveryScope( release , delivery , DBEnumScopeCategoryType.DB );
				addDelivery( release , scope , scopeDelivery );
			}
			if( ( CATEGORY == null || CATEGORY == DBEnumScopeCategoryType.DOC ) && delivery.hasDocItems() ) {
				scopeDelivery = createDeliveryScope( release , delivery , DBEnumScopeCategoryType.DOC );
				addDelivery( release , scope , scopeDelivery );
			}
		}
		
		return( scope );
	}
	
	private static void addDelivery( Release release , ReleaseDistScope scope , ReleaseDistScopeDelivery scopeDelivery ) {
		if( scopeDelivery.isEmpty() )
			return;
		
		ReleaseDistScopeSet scopeSet = scope.findCategorySet( scopeDelivery.CATEGORY );
		if( scopeSet == null ) {
			scopeSet = new ReleaseDistScopeSet( release , scopeDelivery.CATEGORY );
			scope.addScopeSet( scopeSet );
		}
		
		scopeSet.addDelivery( scopeDelivery );
	}
	
	public static ReleaseDistScopeDelivery createDeliveryScope( Release release , String deliveryName , DBEnumScopeCategoryType CATEGORY ) throws Exception {
		Meta meta = release.getMeta();
		MetaDistr distr = meta.getDistr();
		MetaDistrDelivery delivery = distr.getDelivery( deliveryName );
		return( createDeliveryScope( release , delivery , CATEGORY ) );
	}
	
	public static ReleaseDistScopeDelivery createDeliveryScope( Release release , MetaDistrDelivery distDelivery , DBEnumScopeCategoryType CATEGORY ) throws Exception {
		ReleaseDistScopeDelivery scopeDelivery = new ReleaseDistScopeDelivery( release , distDelivery , CATEGORY );
		if( CATEGORY == DBEnumScopeCategoryType.BINARY )
			createBinary( release , scopeDelivery );
		else
		if( CATEGORY == DBEnumScopeCategoryType.CONFIG )
			createConf( release , scopeDelivery );
		else
		if( CATEGORY == DBEnumScopeCategoryType.DB )
			createDatabase( release , scopeDelivery );
		else
		if( CATEGORY == DBEnumScopeCategoryType.DOC )
			createDoc( release , scopeDelivery );
		return( scopeDelivery );
	}

	private static void createBinary( Release release , ReleaseDistScopeDelivery scopeDelivery ) throws Exception {
		boolean all = false;
		ReleaseScope scope = release.getScope();
		for( MetaDistrBinaryItem item : scopeDelivery.distDelivery.getBinaryItems() ) {
			boolean matched = false;
			for( ReleaseDistTarget target : scope.getDistTargets() ) {
				if( target.isDistAll() ) {
					matched = true;
					all = true;
				}
				else
				if( target.isDeliveryBinaries() && target.DELIVERY.equals( item.delivery.ID ) && target.ALL ) {
					matched = true;
					all = true;
				}
				else
				if( target.isBinaryItem() && target.BINARY.equals( item.ID ) ) {
					matched = true;
				}
			}
			
			if( !matched ) {
				if( item.isProjectItem() ) {
					if( checkScopeBuildProjectItem( scopeDelivery.release , item.sourceProjectItem ) )
						matched = true;
				}
			}

			if( matched ) {
				if( scopeDelivery.findBinaryItem( item ) == null ) {
					ReleaseDistScopeDeliveryItem deliveryItem = new ReleaseDistScopeDeliveryItem( release , item );
					scopeDelivery.addBinaryItem( deliveryItem );
				}
			}
		}
		
		scopeDelivery.setAll( all );
	}
	
	private static void createConf( Release release , ReleaseDistScopeDelivery scopeDelivery ) throws Exception {
		boolean all = false;
		ReleaseScope scope = release.getScope();
		for( MetaDistrConfItem item : scopeDelivery.distDelivery.getConfItems() ) {
			boolean matched = false;
			for( ReleaseDistTarget target : scope.getDistTargets() ) {
				if( target.isDistAll() ) {
					matched = true;
					all = true;
				}
				else
				if( target.isDeliveryConfs() && target.DELIVERY.equals( item.delivery.ID ) && target.ALL ) {
					matched = true;
					all = true;
				}
				else
				if( target.isConfItem() && target.CONF.equals( item.ID ) ) {
					matched = true;
				}
			}

			if( matched ) {
				if( scopeDelivery.findConfItem( item ) == null ) {
					ReleaseDistScopeDeliveryItem deliveryItem = new ReleaseDistScopeDeliveryItem( release , item );
					scopeDelivery.addConfItem( deliveryItem );
				}
			}
		}
		
		scopeDelivery.setAll( all );
	}
	
	private static void createDatabase( Release release , ReleaseDistScopeDelivery scopeDelivery ) throws Exception {
		boolean all = false;
		ReleaseScope scope = release.getScope();
		for( MetaDatabaseSchema schema : scopeDelivery.distDelivery.getDatabaseSchemes() ) {
			boolean matched = false;
			for( ReleaseDistTarget target : scope.getDistTargets() ) {
				if( target.isDistAll() ) {
					matched = true;
					all = true;
				}
				else
				if( target.isDeliveryDatabase() && target.DELIVERY.equals( scopeDelivery.distDelivery.ID ) && target.ALL ) {
					matched = true;
					all = true;
				}
				else
				if( target.isSchema() && target.SCHEMA.equals( schema.ID ) ) {
					matched = true;
					break;
				}
			}
			
			if( matched ) {
				if( scopeDelivery.findSchema( schema ) == null ) {
					ReleaseDistScopeDeliveryItem deliveryItem = new ReleaseDistScopeDeliveryItem( release , scopeDelivery.distDelivery , schema );
					scopeDelivery.addSchemaItem( deliveryItem );
				}
			}
		}
		scopeDelivery.setAll( all );
	}
	
	private static void createDoc( Release release , ReleaseDistScopeDelivery scopeDelivery ) throws Exception {
		boolean all = false;
		ReleaseScope scope = release.getScope();
		for( MetaProductDoc doc : scopeDelivery.distDelivery.getDocs() ) {
			boolean matched = false;
			for( ReleaseDistTarget target : scope.getDistTargets() ) {
				if( target.isDistAll() ) {
					matched = true;
					all = true;
				}
				else
				if( target.isDeliveryDocs() && target.DELIVERY.equals( scopeDelivery.distDelivery.ID ) && target.ALL ) {
					matched = true;
					all = true;
				}
				else
				if( target.isDoc() && target.DOC.equals( doc.ID ) ) {
					matched = true;
				}
			}
			
			if( matched ) {
				if( scopeDelivery.findDoc( doc ) == null ) {
					ReleaseDistScopeDeliveryItem deliveryItem = new ReleaseDistScopeDeliveryItem( release , scopeDelivery.distDelivery , doc );
					scopeDelivery.addDocItem( deliveryItem );
				}
			}
		}
		scopeDelivery.setAll( all );
	}

	public static boolean checkScopeBuildProjectItem( Release release , MetaSourceProjectItem item ) {
		ReleaseScope scope = release.getScope();
		for( ReleaseBuildTarget target : scope.getBuildTargets() ) {
			if( target.isBuildAll() )
				return( true );
			if( target.isBuildSet() && target.ALL && target.SRCSET.equals( item.project.set.ID ) )
				return( true );
			if( target.isBuildProject() && target.ALL && target.PROJECT.equals( item.project.ID ) )
				return( true );
		}
		return( false );
	}

	public void addScopeSet( ReleaseDistScopeSet scopeSet ) {
		mapSet.put( scopeSet.CATEGORY , scopeSet );
	}

	public ReleaseDistScopeSet findCategorySet( DBEnumScopeCategoryType CATEGORY ) {
		return( mapSet.get( CATEGORY ) );
	}

	public ReleaseDistScopeSet[] getSets() {
		return( mapSet.values().toArray( new ReleaseDistScopeSet[0] ) );
	}

	public ReleaseDistScopeDeliveryItem findCategoryDeliveryItem( DBEnumScopeCategoryType CATEGORY , String itemName ) {
		ReleaseDistScopeSet set = findCategorySet( CATEGORY );
		if( set == null )
			return( null );
		return( set.findDeliveryItem( itemName ) );
	}

	public ReleaseDistScopeDeliveryItem findDistScopeSourceItem( MetaSourceProjectItem item ) {
		if( item.isInternal() )
			return( null );
		ReleaseDistScopeSet set = findCategorySet( DBEnumScopeCategoryType.BINARY );
		if( set == null )
			return( null );
		ReleaseDistScopeDelivery delivery = set.findDelivery( item.distItem.delivery );
		if( delivery == null )
			return( null );
		return( delivery.findBinaryItem( item.distItem ) );
	}

	public ReleaseDistScopeDelivery findCategoryDelivery( DBEnumScopeCategoryType CATEGORY , MetaDistrDelivery delivery ) {
		ReleaseDistScopeSet set = findCategorySet( CATEGORY );
		if( set == null )
			return( null );
		return( set.findDelivery( delivery ) );
	}
	
}
