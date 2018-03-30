package org.urm.action.release;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.db.release.DBReleaseScope;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaProductDoc;
import org.urm.meta.product.MetaSources;
import org.urm.meta.release.Release;
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
		if( sourcePath ) {
			if( !executeBySource() )
				return( SCOPESTATE.RunFail );
		}
		else {
			if( !executeByDelivery() ) 
				return( SCOPESTATE.RunFail );
		}
		return( SCOPESTATE.RunSuccess );
	}

	private boolean executeBySource() throws Exception {
		Meta meta = release.getMeta();
		MetaSources source = meta.getSources();
		
		// add new
		if( pathItems.length == 1 ) {
			if( pathItems[0].equals( "all" ) ) {
				for( MetaSourceProjectSet set : source.getSets() ) {
					if( !DBReleaseScope.addAllSource( super.method , this , release , set ) )
						return( false );
				}
				return( true );
			}
			if( pathItems[0].equals( "none" ) ) {
				DBReleaseScope.descopeAllProjects( super.method , this , release );
				return( true );
			}
		}
		
		Map<String,String> check = new HashMap<String,String>();
		for( String path : pathItems ) {
			check.put( path , "all" );
			String[] els = Common.split( path , "/" );
			if( els.length == 1 ) {
				MetaSourceProjectSet set = source.getProjectSet( els[0] );
				if( !DBReleaseScope.addAllSource( super.method , this , release , set ) )
					return( false );
				continue;
			}
			if( els.length == 2 ) {
				MetaSourceProjectSet set = source.getProjectSet( els[0] );
				check.put( els[0] , "set" );
				MetaSourceProject project = set.getProject( els[1] );
				if( !DBReleaseScope.addProjectAllItems( super.method , this , release , project ) )
					return( false );
				continue;
			}
			if( els.length == 3 ) {
				MetaSourceProjectSet set = source.getProjectSet( els[0] );
				check.put( els[0] , "set" );
				MetaSourceProject project = set.getProject( els[1] );
				check.put( Common.concat( els[0] , els[1] , "/" ) , "project" );
				MetaSourceProjectItem item = project.getItem( els[2] );
				if( !DBReleaseScope.addProjectItem( super.method , this , release , project , item ) )
					return( false );
				continue;
			}
		}
		
		// descope missing
		Common.exitUnexpected();
		return( true );
	}

	private boolean executeByDelivery() throws Exception {
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
						DBEnumScopeCategoryType.DOC } ) {
					if( !DBReleaseScope.addAllCategory( super.method , this , release , category ) )
						return( false );
				}
				
				for( MetaSourceProjectSet set : source.getSets() ) {
					if( !DBReleaseScope.addAllSource( super.method , this , release , set ) )
						return( false );
				}
				return( true );
			}
			if( pathItems[0].equals( "none" ) ) {
				DBReleaseScope.descopeAll( super.method , this , release );
				return( true );
			}
		}
			
		Map<String,String> check = new HashMap<String,String>();
		for( String path : pathItems ) {
			check.put( path , "all" );
			String[] els = Common.split( path , "/" );
			if( els.length == 1 ) {
				MetaDistrDelivery delivery = distr.getDelivery( els[0] );
				if( !addAllDelivery( check , delivery ) )
					return( false );
				continue;
			}
			if( els.length == 2 ) {
				MetaDistrDelivery delivery = distr.getDelivery( els[0] );
				check.put( els[0] , SCOPETYPE_DELIVERY );
				
				String type = els[1];
				if( type.equals( SCOPEITEM_SCHEMA ) ) {
					if( !addDeliveryAllSchemes( check , delivery ) )
						return( false );
				}
				else
				if( type.equals( SCOPEITEM_DOC ) ) {
					if( !addDeliveryAllDocs( check , delivery ) )
						return( false );
				}
			}
			if( els.length == 3 ) {
				MetaDistrDelivery delivery = distr.getDelivery( els[0] );
				check.put( els[0] , SCOPETYPE_DELIVERY );
				String type = els[1];
				
				if( type.equals( SCOPEITEM_BINARY ) ) {
					MetaDistrBinaryItem item = delivery.getBinaryItem( els[2] );
					if( !DBReleaseScope.addBinaryItem( super.method , this , release , item ) )
						return( false );
				}
				else
					if( type.equals( SCOPEITEM_CONF ) ) {
					MetaDistrConfItem item = delivery.getConfItem( els[2] );
					if( !DBReleaseScope.addConfItem( super.method , this , release , item ) )
						return( false );
				}
				else
					if( type.equals( SCOPEITEM_SCHEMA ) ) {
					MetaDatabaseSchema schema = delivery.getSchema( els[2] );
					if( !DBReleaseScope.addDeliveryDatabaseSchema( super.method , this , release , delivery , schema ) )
						return( false );
				}
				else
					if( type.equals( SCOPEITEM_DOC ) ) {
					MetaProductDoc doc = delivery.getDoc( els[2] );
					if( !DBReleaseScope.addDeliveryDoc( super.method , this , release , delivery , doc ) )
						return( false );
				}
			}
		}

		// descope missing
		Common.exitUnexpected();
		return( true );
	}

	private boolean addAllDelivery( Map<String,String> check , MetaDistrDelivery delivery ) throws Exception {
		for( MetaDistrBinaryItem binaryItem : delivery.getBinaryItems() ) {
			if( !DBReleaseScope.addBinaryItem( super.method , this , release , binaryItem ) )
				return( false );
			check.put( Common.getListPath( new String[] { delivery.NAME , SCOPEITEM_BINARY , binaryItem.NAME } ) , "binary" );
		}
		for( MetaDistrConfItem confItem : delivery.getConfItems() ) {
			if( !DBReleaseScope.addConfItem( super.method , this , release , confItem ) )
				return( false );
			check.put( Common.getListPath( new String[] { delivery.NAME , SCOPEITEM_CONF , confItem.NAME } ) , "conf" );
		}
		if( delivery.hasDatabaseItems() ) {
			if( !addDeliveryAllSchemes( check , delivery ) )
				return( false );
		}
		if( delivery.hasDocItems() ) {
			if( !addDeliveryAllDocs( check , delivery ) )
				return( false );
		}
		return( true );
	}
	
	private boolean addDeliveryAllSchemes( Map<String,String> check , MetaDistrDelivery delivery ) throws Exception {
		if( !DBReleaseScope.addDeliveryAllDatabaseSchemes( super.method , this , release , delivery ) )
			return( false );
		
		for( MetaDatabaseSchema schema : delivery.getDatabaseSchemes() )
			check.put( Common.getListPath( new String[] { delivery.NAME , SCOPEITEM_SCHEMA , schema.NAME } ) , "database" );
		return( true );
	}
	
	private boolean addDeliveryAllDocs( Map<String,String> check , MetaDistrDelivery delivery ) throws Exception {
		if( !DBReleaseScope.addDeliveryAllDocs( super.method , this , release , delivery ) )
			return( false );
		
		for( MetaProductDoc doc : delivery.getDocs() )
			check.put( Common.getListPath( new String[] { delivery.NAME , SCOPEITEM_DOC , doc.NAME } ) , "doc" );
		return( true );
	}
	
}
