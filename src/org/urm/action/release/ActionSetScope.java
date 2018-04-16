package org.urm.action.release;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.db.release.DBReleaseScope;
import org.urm.engine.dist.ReleaseBuildScope;
import org.urm.engine.dist.ReleaseBuildScopeProject;
import org.urm.engine.dist.ReleaseBuildScopeSet;
import org.urm.engine.dist.ReleaseDistScope;
import org.urm.engine.dist.ReleaseDistScopeDelivery;
import org.urm.engine.dist.ReleaseDistScopeDeliveryItem;
import org.urm.engine.dist.ReleaseDistScopeSet;
import org.urm.engine.run.EngineMethod;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.engine.ProductReleases;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaProductDoc;
import org.urm.meta.product.MetaSources;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseRepository;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.product.MetaSourceProjectSet;

public class ActionSetScope extends ActionBase {

	public static String SCOPETYPE_SOURCE = "source";
	public static String SCOPETYPE_DELIVERY = "delivery";
	public static String SCOPEITEM_BINARY = "binary";
	public static String SCOPEITEM_CONF = "conf";
	public static String SCOPEITEM_SCHEMA = "schema";
	public static String SCOPEITEM_DOC = "doc";
	
	public Release release;
	boolean sourcePath;
	String[] pathItems;
	
	public ActionSetScope( ActionBase action , String stream , Release release , boolean sourcePath , String[] pathItems ) {
		super( action , stream , "Set scope, release=" + release.RELEASEVER );
		this.release = release;
		this.sourcePath = sourcePath;
		this.pathItems = pathItems;
	}

	@Override 
	protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		EngineMethod method = super.method;

		Meta meta = release.getMeta();
		ProductReleases releases = meta.getReleases();
		ReleaseRepository repoUpdated = method.changeReleaseRepository( releases );
		Release releaseUpdated = method.changeRelease( repoUpdated , release );
		
		synchronized( releases ) {
			if( sourcePath ) {
				if( !executeBySource( method , releaseUpdated ) )
					return( SCOPESTATE.RunFail );
			}
			else {
				if( !executeByDelivery( method , releaseUpdated ) ) 
					return( SCOPESTATE.RunFail );
			}
		}
		
		return( SCOPESTATE.RunSuccess );
	}

	private boolean executeBySource( EngineMethod method , Release release ) throws Exception {
		Meta meta = release.getMeta();
		MetaSources source = meta.getSources();
		
		// add new
		if( pathItems.length == 1 ) {
			if( pathItems[0].equals( "all" ) ) {
				DBReleaseScope.addAllSource( method , this , release );
				return( true );
			}
			if( pathItems[0].equals( "none" ) ) {
				DBReleaseScope.descopeAllSource( method , this , release );
				return( true );
			}
		}
		
		ReleaseBuildScope buildScope = ReleaseBuildScope.createScope( release );
		DBReleaseScope.descopeBuildAll( method , this , release );
		
		Map<String,String> check = new HashMap<String,String>();
		for( String path : pathItems ) {
			check.put( path , "all" );
			String[] els = Common.split( path , "/" );
			if( els.length == 1 ) {
				MetaSourceProjectSet set = source.getProjectSet( els[0] );
				DBReleaseScope.addAllSourceSet( method , this , release , set );
				continue;
			}
			if( els.length == 2 ) {
				MetaSourceProjectSet set = source.getProjectSet( els[0] );
				check.put( els[0] , "set" );
				MetaSourceProject project = set.getProject( els[1] );
				DBReleaseScope.addAllProjectItems( method , this , release , project );
				continue;
			}
			if( els.length == 3 ) {
				MetaSourceProjectSet set = source.getProjectSet( els[0] );
				check.put( els[0] , "set" );
				MetaSourceProject project = set.getProject( els[1] );
				check.put( Common.concat( els[0] , els[1] , "/" ) , "project" );
				MetaSourceProjectItem item = project.getItem( els[2] );
				DBReleaseScope.addProjectItem( method , this , release , project , item );
				continue;
			}
		}

		// descope missing
		for( ReleaseBuildScopeSet set : buildScope.getSets() ) {
			String checkSet = check.get( set.set.NAME );
			if( checkSet == null ) {
				DBReleaseScope.descopeSet( method , this , release , set );
				continue;
			}

			if( checkSet.equals( "all" ) )
				continue;
			
			for( ReleaseBuildScopeProject target : set.getProjects() ) {
				String checkProject = check.get( Common.concat( set.set.NAME , target.project.NAME , "/" ) );
				if( checkProject == null ) {
					DBReleaseScope.descopeProject( method , this , release , set.set , target.project );
					continue;
				}
				
				if( checkProject.equals( "all" ) )
					continue;
				
				for( MetaSourceProjectItem item : target.project.getItems() ) {
					if( item.isInternal() )
						continue;
					
					String checkItem = check.get( Common.concat( Common.concat( set.set.NAME , target.project.NAME , "/" ) , item.NAME , "/" ) );
					if( checkItem == null )
						DBReleaseScope.descopeBinaryItem( method , this , release , item.distItem );
				}
			}
		}
		
		return( true );
	}

	private boolean executeByDelivery( EngineMethod method , Release releaseUpdated ) throws Exception {
		Meta meta = release.getMeta();
		MetaDistr distr = meta.getDistr();
		MetaSources source = meta.getSources();
		
		// add new 
		if( pathItems.length == 1 ) {
			if( pathItems[0].equals( "all" ) ) {
				for( DBEnumScopeCategoryType category : new DBEnumScopeCategoryType[] { 
						DBEnumScopeCategoryType.MANUAL , 
						DBEnumScopeCategoryType.DERIVED , 
						DBEnumScopeCategoryType.CONFIG , 
						DBEnumScopeCategoryType.DB , 
						DBEnumScopeCategoryType.DOC } )
					DBReleaseScope.addAllCategory( method , this , releaseUpdated , category );
				
				for( MetaSourceProjectSet set : source.getSets() )
					DBReleaseScope.addAllSourceSet( method , this , releaseUpdated , set );
				return( true );
			}
			if( pathItems[0].equals( "none" ) ) {
				DBReleaseScope.descopeAll( method , this , releaseUpdated );
				return( true );
			}
		}
			
		ReleaseDistScope distScope = ReleaseDistScope.createScope( release );
		DBReleaseScope.descopeDistAll( method , this , release );
		
		Map<String,String> check = new HashMap<String,String>();
		for( String path : pathItems ) {
			check.put( path , "all" );
			String[] els = Common.split( path , "/" );
			if( els.length == 1 ) {
				MetaDistrDelivery delivery = distr.getDelivery( els[0] );
				if( !addAllDelivery( method , releaseUpdated , check , delivery ) )
					return( false );
				continue;
			}
			if( els.length == 2 ) {
				MetaDistrDelivery delivery = distr.getDelivery( els[0] );
				check.put( els[0] , SCOPETYPE_DELIVERY );
				
				String type = els[1];
				if( type.equals( SCOPEITEM_SCHEMA ) ) {
					if( !addDeliveryAllSchemes( method , releaseUpdated , check , delivery ) )
						return( false );
				}
				else
				if( type.equals( SCOPEITEM_DOC ) ) {
					if( !addDeliveryAllDocs( method , releaseUpdated , check , delivery ) )
						return( false );
				}
			}
			if( els.length == 3 ) {
				MetaDistrDelivery delivery = distr.getDelivery( els[0] );
				check.put( els[0] , SCOPETYPE_DELIVERY );
				String type = els[1];
				
				if( type.equals( SCOPEITEM_BINARY ) ) {
					MetaDistrBinaryItem item = delivery.getBinaryItem( els[2] );
					DBReleaseScope.addBinaryItem( method , this , releaseUpdated , item );
				}
				else
				if( type.equals( SCOPEITEM_CONF ) ) {
					MetaDistrConfItem item = delivery.getConfItem( els[2] );
					DBReleaseScope.addConfItem( method , this , releaseUpdated , item );
				}
				else
				if( type.equals( SCOPEITEM_SCHEMA ) ) {
					MetaDatabaseSchema schema = delivery.getSchema( els[2] );
					DBReleaseScope.addDeliveryDatabaseSchema( method , this , releaseUpdated , delivery , schema );
				}
				else
				if( type.equals( SCOPEITEM_DOC ) ) {
					MetaProductDoc doc = delivery.getDoc( els[2] );
					DBReleaseScope.addDeliveryDoc( method , this , releaseUpdated , delivery , doc );
				}
			}
		}

		// descope missing
		for( ReleaseDistScopeSet set : distScope.getSets() ) {
			for( ReleaseDistScopeDelivery delivery : set.getDeliveries() ) {
				for( ReleaseDistScopeDeliveryItem item : delivery.getItems() ) {
					if( item.isBinary() ) {
						String checkItem = check.get( Common.getListPath( new String[] { delivery.distDelivery.NAME , SCOPEITEM_BINARY , item.binary.NAME } ) );
						if( checkItem == null )
							DBReleaseScope.descopeBinaryItem( method , this , releaseUpdated , item.binary );
					}
					else
					if( item.isConf() ) {
						String checkItem = check.get( Common.getListPath( new String[] { delivery.distDelivery.NAME , SCOPEITEM_CONF , item.conf.NAME } ) );
						if( checkItem == null )
							DBReleaseScope.descopeConfItem( method , this , releaseUpdated , item.conf );
					}
					else
					if( item.isSchema() ) {
						String checkItem = check.get( Common.getListPath( new String[] { delivery.distDelivery.NAME , SCOPEITEM_SCHEMA , item.schema.NAME } ) );
						if( checkItem == null )
							DBReleaseScope.descopeDeliverySchema( method , this , releaseUpdated , delivery.distDelivery , item.schema );
					}
					else
					if( item.isSchema() ) {
						String checkItem = check.get( Common.getListPath( new String[] { delivery.distDelivery.NAME , SCOPEITEM_SCHEMA , item.schema.NAME } ) );
						if( checkItem == null )
							DBReleaseScope.descopeDeliveryDoc( method , this , releaseUpdated , delivery.distDelivery , item.doc );
					}
				}
			}
		}
		
		return( true );
	}

	private boolean addAllDelivery( EngineMethod method , Release releaseUpdated , Map<String,String> check , MetaDistrDelivery delivery ) throws Exception {
		for( MetaDistrBinaryItem binaryItem : delivery.getBinaryItems() ) {
			DBReleaseScope.addBinaryItem( method , this , releaseUpdated , binaryItem );
			check.put( Common.getListPath( new String[] { delivery.NAME , SCOPEITEM_BINARY , binaryItem.NAME } ) , "binary" );
		}
		for( MetaDistrConfItem confItem : delivery.getConfItems() ) {
			DBReleaseScope.addConfItem( method , this , releaseUpdated , confItem );
			check.put( Common.getListPath( new String[] { delivery.NAME , SCOPEITEM_CONF , confItem.NAME } ) , "conf" );
		}
		if( delivery.hasDatabaseItems() ) {
			if( !addDeliveryAllSchemes( method , releaseUpdated , check , delivery ) )
				return( false );
		}
		if( delivery.hasDocItems() ) {
			if( !addDeliveryAllDocs( method , releaseUpdated , check , delivery ) )
				return( false );
		}
		return( true );
	}
	
	private boolean addDeliveryAllSchemes( EngineMethod method , Release releaseUpdated , Map<String,String> check , MetaDistrDelivery delivery ) throws Exception {
		DBReleaseScope.addDeliveryAllDatabaseSchemes( method , this , releaseUpdated , delivery );
		
		for( MetaDatabaseSchema schema : delivery.getDatabaseSchemes() )
			check.put( Common.getListPath( new String[] { delivery.NAME , SCOPEITEM_SCHEMA , schema.NAME } ) , "database" );
		return( true );
	}
	
	private boolean addDeliveryAllDocs( EngineMethod method , Release releaseUpdated , Map<String,String> check , MetaDistrDelivery delivery ) throws Exception {
		DBReleaseScope.addDeliveryAllDocs( method , this , releaseUpdated , delivery );
		
		for( MetaProductDoc doc : delivery.getDocs() )
			check.put( Common.getListPath( new String[] { delivery.NAME , SCOPEITEM_DOC , doc.NAME } ) , "doc" );
		return( true );
	}
	
}
