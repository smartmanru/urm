package org.urm.action.release;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.dist.Dist;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaProductDoc;
import org.urm.meta.product.MetaSources;
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
	
	public Dist dist;
	boolean sourcePath;
	String[] pathItems;
	
	public ActionSetScope( ActionBase action , String stream , Dist dist , boolean sourcePath , String[] pathItems ) {
		super( action , stream , "Set scope, release=" + dist.RELEASEDIR );
		this.dist = dist;
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
		MetaSources source = dist.meta.getSources();
		
		// add new
		dist.reloadCheckOpenedForDataChange( this );
		if( pathItems.length == 1 ) {
			if( pathItems[0].equals( "all" ) ) {
				for( MetaSourceProjectSet set : source.getSets() ) {
					if( !dist.addAllSource( this , set ) )
						return( false );
				}
				return( true );
			}
			if( pathItems[0].equals( "none" ) ) {
				dist.descopeAllProjects( this );
				return( true );
			}
		}
		
		Map<String,String> check = new HashMap<String,String>();
		for( String path : pathItems ) {
			check.put( path , "all" );
			String[] els = Common.split( path , "/" );
			if( els.length == 1 ) {
				MetaSourceProjectSet set = source.getProjectSet( els[0] );
				if( !dist.addAllSource( this , set ) )
					return( false );
				continue;
			}
			if( els.length == 2 ) {
				MetaSourceProjectSet set = source.getProjectSet( els[0] );
				check.put( els[0] , "set" );
				MetaSourceProject project = set.getProject( els[1] );
				if( !dist.addProjectAllItems( this , project ) )
					return( false );
				continue;
			}
			if( els.length == 3 ) {
				MetaSourceProjectSet set = source.getProjectSet( els[0] );
				check.put( els[0] , "set" );
				MetaSourceProject project = set.getProject( els[1] );
				check.put( Common.concat( els[0] , els[1] , "/" ) , "project" );
				MetaSourceProjectItem item = project.getItem( els[2] );
				if( !dist.addProjectItem( this , project , item ) )
					return( false );
				continue;
			}
		}
		
		// descope missing
		Common.exitUnexpected();
		return( true );
	}

	private boolean executeByDelivery() throws Exception {
		MetaDistr distr = dist.meta.getDistr();
		MetaSources source = dist.meta.getSources();
		
		// add new 
		dist.reloadCheckOpenedForDataChange( this );
		if( pathItems.length == 1 ) {
			if( pathItems[0].equals( "all" ) ) {
				for( DBEnumScopeCategoryType category : new DBEnumScopeCategoryType[] { 
						DBEnumScopeCategoryType.MANUAL , 
						DBEnumScopeCategoryType.DERIVED , 
						DBEnumScopeCategoryType.CONFIG , 
						DBEnumScopeCategoryType.DB , 
						DBEnumScopeCategoryType.DOC } ) {
					if( !dist.addAllCategory( this , category ) )
						return( false );
				}
				
				for( MetaSourceProjectSet set : source.getSets() ) {
					if( !dist.addAllSource( this , set ) )
						return( false );
				}
				return( true );
			}
			if( pathItems[0].equals( "none" ) ) {
				dist.descopeAll( this );
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
					if( !dist.addBinaryItem( this , item ) )
						return( false );
				}
				else
					if( type.equals( SCOPEITEM_CONF ) ) {
					MetaDistrConfItem item = delivery.getConfItem( els[2] );
					if( !dist.addConfItem( this , item ) )
						return( false );
				}
				else
					if( type.equals( SCOPEITEM_SCHEMA ) ) {
					MetaDatabaseSchema schema = delivery.getSchema( els[2] );
					if( !dist.addDeliveryDatabaseSchema( this , delivery , schema ) )
						return( false );
				}
				else
					if( type.equals( SCOPEITEM_DOC ) ) {
					MetaProductDoc doc = delivery.getDoc( els[2] );
					if( !dist.addDeliveryDoc( this , delivery , doc ) )
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
			if( !dist.addBinaryItem( this , binaryItem ) )
				return( false );
			check.put( Common.getListPath( new String[] { delivery.NAME , SCOPEITEM_BINARY , binaryItem.NAME } ) , "binary" );
		}
		for( MetaDistrConfItem confItem : delivery.getConfItems() ) {
			if( !dist.addConfItem( this , confItem ) )
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
		if( !dist.addDeliveryAllDatabaseSchemes( this , delivery ) )
			return( false );
		
		for( MetaDatabaseSchema schema : delivery.getDatabaseSchemes() )
			check.put( Common.getListPath( new String[] { delivery.NAME , SCOPEITEM_SCHEMA , schema.NAME } ) , "database" );
		return( true );
	}
	
	private boolean addDeliveryAllDocs( Map<String,String> check , MetaDistrDelivery delivery ) throws Exception {
		if( !dist.addDeliveryAllDocs( this , delivery ) )
			return( false );
		
		for( MetaProductDoc doc : delivery.getDocs() )
			check.put( Common.getListPath( new String[] { delivery.NAME , SCOPEITEM_DOC , doc.NAME } ) , "doc" );
		return( true );
	}
	
}
