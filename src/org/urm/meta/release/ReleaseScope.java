package org.urm.meta.release;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.db.core.DBEnums.DBEnumBuildTargetType;
import org.urm.db.core.DBEnums.DBEnumDistTargetType;
import org.urm.meta.MatchItem;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaProductDoc;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectSet;

public class ReleaseScope {

	public Release release;
	
	private Map<Integer,ReleaseBuildTarget> scopeBuildMapById;
	private Map<Integer,ReleaseDistTarget> scopeDistMapById;

	public ReleaseScope( Release release ) {
		this.release = release;
		
		scopeBuildMapById = new HashMap<Integer,ReleaseBuildTarget>(); 
		scopeDistMapById = new HashMap<Integer,ReleaseDistTarget>();
	}

	public void copy( Release rrelease , ReleaseScope r ) throws Exception {
		for( ReleaseBuildTarget target : scopeBuildMapById.values() ) {
			ReleaseBuildTarget rtarget = target.copy( null , r );
			r.addBuildTarget( rtarget );
		}
		
		for( ReleaseDistTarget target : scopeDistMapById.values() ) {
			ReleaseDistTarget rtarget = target.copy( null , r );
			r.addDistTarget( rtarget );
		}
	}
	
	public void clear() {
		scopeBuildMapById.clear();
		scopeDistMapById.clear();
	}
	
	public ReleaseDistTarget getDistTarget( int id ) throws Exception {
		ReleaseDistTarget rt = scopeDistMapById.get( id );
		if( rt == null )
			Common.exitUnexpected();
		return( rt );
	}

	public ReleaseBuildTarget getBuildTarget( int id ) throws Exception {
		ReleaseBuildTarget rt = scopeBuildMapById.get( id );
		if( rt == null )
			Common.exitUnexpected();
		return( rt );
	}

	public void addDistTarget( ReleaseDistTarget target ) {
		scopeDistMapById.put( target.ID , target );
	}
	
	public void addBuildTarget( ReleaseBuildTarget target ) {
		scopeBuildMapById.put( target.ID , target );
	}
	
	public void removeDistTarget( ReleaseDistTarget target ) {
		scopeDistMapById.remove( target.ID );
	}
	
	public void removeBuildTarget( ReleaseBuildTarget target ) {
		scopeBuildMapById.remove( target.ID );
	}
	
	public boolean isEmpty() {
		if( scopeBuildMapById.isEmpty() && scopeDistMapById.isEmpty() )
			return( true );
		return( false );
	}

	public ReleaseDistTarget findDistTarget( MetaDistrDelivery distDelivery , DBEnumDistTargetType type ) {
		for( ReleaseDistTarget target : scopeDistMapById.values() ) {
			if( target.TYPE == type && target.DELIVERY.equals( distDelivery.ID ) )
				return( target );
		}
		return( null );
	}

	public ReleaseDistTarget findDistBinaryItemTarget( MetaDistrBinaryItem item ) {
		for( ReleaseDistTarget target : scopeDistMapById.values() ) {
			if( target.TYPE == DBEnumDistTargetType.BINARYITEM && target.BINARY.equals( item.ID ) )
				return( target );
		}
		return( null );
	}
	
	public ReleaseDistTarget findDistConfItemTarget( MetaDistrConfItem item ) {
		for( ReleaseDistTarget target : scopeDistMapById.values() ) {
			if( target.TYPE == DBEnumDistTargetType.CONFITEM && target.CONF.equals( item.ID ) )
				return( target );
		}
		return( null );
	}
	
	public ReleaseDistTarget findDistDeliverySchemaTarget( MetaDistrDelivery distDelivery , MetaDatabaseSchema schema ) {
		for( ReleaseDistTarget target : scopeDistMapById.values() ) {
			if( target.TYPE == DBEnumDistTargetType.SCHEMA && target.DELIVERY.equals( distDelivery.ID ) && target.SCHEMA.equals( schema.ID ) )
				return( target );
		}
		return( null );
	}
	
	public ReleaseDistTarget findDistDeliveryDocTarget( MetaDistrDelivery distDelivery , MetaProductDoc doc ) {
		for( ReleaseDistTarget target : scopeDistMapById.values() ) {
			if( target.TYPE == DBEnumDistTargetType.DOC && target.DELIVERY.equals( distDelivery.ID ) && target.DOC.equals( doc.ID ) )
				return( target );
		}
		return( null );
	}

	public ReleaseBuildTarget findBuildProjectTarget( MetaSourceProject project ) {
		for( ReleaseBuildTarget target : scopeBuildMapById.values() ) {
			if( ( target.TYPE == DBEnumBuildTargetType.PROJECTALLITEMS || target.TYPE == DBEnumBuildTargetType.PROJECTNOITEMS ) && target.PROJECT.equals( project.ID ) )
				return( target );
		}
		return( null );
	}

	public ReleaseBuildTarget findBuildProjectSetTarget( MetaSourceProjectSet set ) {
		for( ReleaseBuildTarget target : scopeBuildMapById.values() ) {
			if( target.TYPE == DBEnumBuildTargetType.PROJECTSET && target.SRCSET.equals( set.ID ) )
				return( target );
		}
		return( null );
	}

	public ReleaseBuildTarget findBuildAllTarget() {
		for( ReleaseBuildTarget target : scopeBuildMapById.values() ) {
			if( target.TYPE == DBEnumBuildTargetType.BUILDALL )
				return( target );
		}
		return( null );
	}
	
	public ReleaseDistTarget findDistAllTarget() {
		for( ReleaseDistTarget target : scopeDistMapById.values() ) {
			if( target.TYPE == DBEnumDistTargetType.DISTALL )
				return( target );
		}
		return( null );
	}
	
	public ReleaseDistTarget[] getDistTargets() {
		return( scopeDistMapById.values().toArray( new ReleaseDistTarget[0] ) );
	}
	
	public ReleaseBuildTarget[] getBuildTargets() {
		return( scopeBuildMapById.values().toArray( new ReleaseBuildTarget[0] ) );
	}

	public ReleaseBuildTarget[] getChildBuildTargets( ReleaseBuildTarget target ) {
		List<ReleaseBuildTarget> list = new LinkedList<ReleaseBuildTarget>();
		for( ReleaseBuildTarget targetCheck : scopeBuildMapById.values() ) {
			if( target.isParentOf( targetCheck ) )
				list.add( target );
		}
		return( list.toArray( new ReleaseBuildTarget[0] ) );
	}

	public ReleaseDistTarget[] getChildDistTargets( ReleaseDistTarget target ) {
		List<ReleaseDistTarget> list = new LinkedList<ReleaseDistTarget>();
		for( ReleaseDistTarget targetCheck : scopeDistMapById.values() ) {
			if( target.isParentOf( targetCheck ) )
				list.add( target );
		}
		return( list.toArray( new ReleaseDistTarget[0] ) );
	}

	public ReleaseDistTarget findDistDeliveryConfTarget( MetaDistrDelivery delivery ) {
		for( ReleaseDistTarget target : scopeDistMapById.values() ) {
			if( target.isDeliveryConfs() && MatchItem.equals( target.DELIVERY , delivery.ID ) )
				return( target );
		}
		return( null );
	}

	public ReleaseDistTarget findDistDeliveryBinaryTarget( MetaDistrDelivery delivery ) {
		for( ReleaseDistTarget target : scopeDistMapById.values() ) {
			if( target.isDeliveryBinaries() && MatchItem.equals( target.DELIVERY , delivery.ID ) )
				return( target );
		}
		return( null );
	}
	
	public ReleaseDistTarget findDistDeliveryDatabaseTarget( MetaDistrDelivery delivery ) {
		for( ReleaseDistTarget target : scopeDistMapById.values() ) {
			if( target.isDeliveryDatabase() && MatchItem.equals( target.DELIVERY , delivery.ID ) )
				return( target );
		}
		return( null );
	}
	
	public ReleaseDistTarget findDistDeliveryDocTarget( MetaDistrDelivery delivery ) {
		for( ReleaseDistTarget target : scopeDistMapById.values() ) {
			if( target.isDeliveryDocs() && MatchItem.equals( target.DELIVERY , delivery.ID ) )
				return( target );
		}
		return( null );
	}
	
}
