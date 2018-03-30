package org.urm.db.release;

import org.urm.action.ActionBase;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.dist.ReleaseBuildScopeSet;
import org.urm.engine.dist.ReleaseDistScopeSet;
import org.urm.engine.run.EngineMethod;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaProductDoc;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.release.Release;

public class DBReleaseScope {

	public static boolean addAllSource( EngineMethod method , ActionBase action , Release release , MetaSourceProjectSet set ) throws Exception {
		return( false );
	}
	
	public static boolean addAllCategory( EngineMethod method , ActionBase action , Release release , DBEnumScopeCategoryType CATEGORY ) throws Exception {
		return( false );
	}
	
	public static boolean addProjectAllItems( EngineMethod method , ActionBase action , Release release , MetaSourceProject project ) throws Exception {
		return( false );
	}

	public static boolean addProjectItem( EngineMethod method , ActionBase action , Release release , MetaSourceProject project , MetaSourceProjectItem item ) throws Exception {
		return( false );
	}

	public static boolean addConfItem( EngineMethod method , ActionBase action , Release release , MetaDistrConfItem item ) throws Exception {
		return( false );
	}

	public static boolean addManualItem( EngineMethod method , ActionBase action , Release release , MetaDistrBinaryItem item ) throws Exception {
		return( false );
	}

	public static boolean addDerivedItem( EngineMethod method , ActionBase action , Release release , MetaDistrBinaryItem item ) throws Exception {
		return( false );
	}

	public static boolean addBinaryItem( EngineMethod method , ActionBase action , Release release , MetaDistrBinaryItem item ) throws Exception {
		return( false );
	}
	
	public static boolean addDeliveryAllDatabaseSchemes( EngineMethod method , ActionBase action , Release release , MetaDistrDelivery delivery ) throws Exception {
		return( false );
	}

	public static boolean addDeliveryAllDocs( EngineMethod method , ActionBase action , Release release , MetaDistrDelivery delivery ) throws Exception {
		return( false );
	}

	public static boolean addDeliveryDatabaseSchema( EngineMethod method , ActionBase action , Release release , MetaDistrDelivery delivery , MetaDatabaseSchema schema ) throws Exception {
		return( false );
	}
	
	public static boolean addDeliveryDoc( EngineMethod method , ActionBase action , Release release , MetaDistrDelivery delivery , MetaProductDoc doc ) throws Exception {
		return( false );
	}
	
	public static boolean addDatabaseAll( EngineMethod method , ActionBase action , Release release ) throws Exception {
		return( false );
	}

	public static boolean addDocAll( EngineMethod method , ActionBase action , Release release ) throws Exception {
		return( false );
	}

	public static boolean descopeAll( EngineMethod method , ActionBase action , Release release ) throws Exception {
		return( false );
	}

	public static boolean descopeSet( EngineMethod method , ActionBase action , Release release , ReleaseBuildScopeSet set ) throws Exception {
		return( false );
	}

	public static boolean descopeSet( EngineMethod method , ActionBase action , Release release , ReleaseDistScopeSet set ) throws Exception {
		return( false );
	}

	public static boolean descopeAllProjects( EngineMethod method , ActionBase action , Release release ) throws Exception {
		return( false );
	}

}
