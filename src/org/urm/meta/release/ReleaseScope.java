package org.urm.meta.release;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.db.core.DBEnums.DBEnumBuildTargetType;
import org.urm.db.core.DBEnums.DBEnumDistTargetType;
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

	public ReleaseScope copy( Release rrelease ) throws Exception {
		ReleaseScope r = new ReleaseScope( rrelease );
		
		for( ReleaseBuildTarget target : scopeBuildMapById.values() ) {
			ReleaseBuildTarget rtarget = target.copy( r );
			r.addScopeBuildTarget( rtarget );
		}
		
		for( ReleaseDistTarget target : scopeDistMapById.values() ) {
			ReleaseDistTarget rtarget = target.copy( r );
			r.addScopeDeliveryTarget( rtarget );
		}

		return( r );
	}
	
	public ReleaseDistTarget getScopeDeliveryTarget( int id ) throws Exception {
		ReleaseDistTarget rt = scopeDistMapById.get( id );
		if( rt == null )
			Common.exitUnexpected();
		return( rt );
	}

	public ReleaseBuildTarget getScopeBuildTarget( int id ) throws Exception {
		ReleaseBuildTarget rt = scopeBuildMapById.get( id );
		if( rt == null )
			Common.exitUnexpected();
		return( rt );
	}

	private void addScopeDeliveryTarget( ReleaseDistTarget target ) {
		scopeDistMapById.put( target.ID , target );
	}
	
	private void addScopeBuildTarget( ReleaseBuildTarget target ) {
		scopeBuildMapById.put( target.ID , target );
	}
	
	public boolean isEmpty() {
		if( scopeBuildMapById.isEmpty() && scopeDistMapById.isEmpty() )
			return( true );
		return( false );
	}

	public ReleaseDistTarget findScopeDistDeliveryTarget( MetaDistrDelivery distDelivery , DBEnumDistTargetType type ) {
		for( ReleaseDistTarget target : scopeDistMapById.values() ) {
			if( target.TYPE == type && target.DELIVERY.equals( distDelivery.ID ) )
				return( target );
		}
		return( null );
	}

	public ReleaseDistTarget findScopeDistBinaryItemTarget( MetaDistrBinaryItem item ) {
		for( ReleaseDistTarget target : scopeDistMapById.values() ) {
			if( target.TYPE == DBEnumDistTargetType.BINARYITEM && target.BINARY.equals( item.ID ) )
				return( target );
		}
		return( null );
	}
	
	public ReleaseDistTarget findScopeDistConfItemTarget( MetaDistrConfItem item ) {
		for( ReleaseDistTarget target : scopeDistMapById.values() ) {
			if( target.TYPE == DBEnumDistTargetType.CONFITEM && target.CONF.equals( item.ID ) )
				return( target );
		}
		return( null );
	}
	
	public ReleaseDistTarget findScopeDistDeliverySchemaTarget( MetaDistrDelivery distDelivery , MetaDatabaseSchema schema ) {
		for( ReleaseDistTarget target : scopeDistMapById.values() ) {
			if( target.TYPE == DBEnumDistTargetType.SCHEMA && target.DELIVERY.equals( distDelivery.ID ) && target.SCHEMA.equals( schema.ID ) )
				return( target );
		}
		return( null );
	}
	
	public ReleaseDistTarget findScopeDistDeliveryDocTarget( MetaDistrDelivery distDelivery , MetaProductDoc doc ) {
		for( ReleaseDistTarget target : scopeDistMapById.values() ) {
			if( target.TYPE == DBEnumDistTargetType.DOC && target.DELIVERY.equals( distDelivery.ID ) && target.DOC.equals( doc.ID ) )
				return( target );
		}
		return( null );
	}

	public ReleaseBuildTarget findScopeBuildProjectTarget( MetaSourceProject project ) {
		for( ReleaseBuildTarget target : scopeBuildMapById.values() ) {
			if( ( target.TYPE == DBEnumBuildTargetType.PROJECTALLITEMS || target.TYPE == DBEnumBuildTargetType.PROJECTNOITEMS ) && target.PROJECT.equals( project.ID ) )
				return( target );
		}
		return( null );
	}

	public ReleaseBuildTarget findScopeBuildProjectSetTarget( MetaSourceProjectSet set ) {
		for( ReleaseBuildTarget target : scopeBuildMapById.values() ) {
			if( target.TYPE == DBEnumBuildTargetType.PROJECTSET && target.SRCSET.equals( set.ID ) )
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

}
