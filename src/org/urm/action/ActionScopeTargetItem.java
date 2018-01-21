package org.urm.action;

import org.urm.engine.dist.ReleaseTargetItem;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.product.MetaProductBuildSettings;
import org.urm.meta.product.MetaSourceProjectItem;

public class ActionScopeTargetItem {

	public Meta meta;
	public ActionScopeTarget target;
	
	public String NAME;
	public MetaDistrBinaryItem distItem;
	public MetaSourceProjectItem sourceItem;
	public MetaEnvServerNode envServerNode;
	public MetaDatabaseSchema schema;
	
	public ReleaseTargetItem releaseItem;
	public boolean scriptIndex = false;
	public boolean specifiedExplicitly;
	
	private ActionScopeTargetItem( ActionScopeTarget target ) {
		this.target = target;
		this.meta = target.meta;
	}
	
	public ActionScopeTargetItem copy( ActionScopeTarget targetNew ) {
		ActionScopeTargetItem item = new ActionScopeTargetItem( targetNew );
		item.NAME = NAME;
		item.distItem = distItem;
		item.sourceItem = sourceItem;
		item.envServerNode = envServerNode;
		item.schema = schema;
		
		item.releaseItem = releaseItem;
		item.scriptIndex = scriptIndex;
		item.specifiedExplicitly = specifiedExplicitly;
		return( item );
	}
	
	public static ActionScopeTargetItem createSourceProjectTargetItem( ActionScopeTarget target , MetaSourceProjectItem sourceItem , MetaDistrBinaryItem distItem , boolean specifiedExplicitly ) {
		ActionScopeTargetItem ti = new ActionScopeTargetItem( target ); 
		ti.distItem = distItem;
		ti.sourceItem = sourceItem;
		ti.specifiedExplicitly = specifiedExplicitly;
		ti.NAME = sourceItem.NAME;
		return( ti );
	}
	
	public static ActionScopeTargetItem createDeliverySchemaTargetItem( ActionScopeTarget target , MetaDatabaseSchema schema , boolean specifiedExplicitly ) {
		ActionScopeTargetItem ti = new ActionScopeTargetItem( target ); 
		ti.schema = schema;
		ti.specifiedExplicitly = specifiedExplicitly;
		ti.NAME = schema.NAME;
		return( ti );
	}
	
	public static ActionScopeTargetItem createEnvServerNodeTargetItem( ActionScopeTarget target , MetaEnvServerNode envServerNode , boolean specifiedExplicitly ) {
		ActionScopeTargetItem ti = new ActionScopeTargetItem( target ); 
		ti.envServerNode = envServerNode;
		ti.specifiedExplicitly = specifiedExplicitly;
		ti.NAME = "" + envServerNode.POS;
		return( ti );
	}
	
	public static ActionScopeTargetItem createReleaseTargetItem( ActionScopeTarget target , ReleaseTargetItem releaseItem , boolean specifiedExplicitly ) {
		ActionScopeTargetItem ti = new ActionScopeTargetItem( target ); 
		ti.distItem = releaseItem.distItem;
		ti.sourceItem = releaseItem.sourceItem;
		ti.releaseItem = releaseItem;
		ti.specifiedExplicitly = specifiedExplicitly;
		ti.NAME = releaseItem.NAME;
		return( ti );
	}

	public static ActionScopeTargetItem createScriptIndexTargetItem( ActionScopeTarget target , String index ) {
		ActionScopeTargetItem ti = new ActionScopeTargetItem( target );
		ti.scriptIndex = true;
		ti.specifiedExplicitly = true;
		ti.NAME = index;
		return( ti );
	}

	public String getProjectItemBuildVersion( ActionBase action ) throws Exception {
		String BUILDVERSION = "";
		if( !action.context.CTX_VERSION.isEmpty() )
			BUILDVERSION = action.context.CTX_VERSION;
		
		if( BUILDVERSION.isEmpty() && releaseItem != null ) {
			BUILDVERSION = releaseItem.BUILDVERSION;
			
			if( BUILDVERSION.isEmpty() )
				BUILDVERSION = releaseItem.target.BUILDVERSION;
		}
		
		if( BUILDVERSION.isEmpty() )
			BUILDVERSION = sourceItem.FIXED_VERSION;
		
		if( BUILDVERSION.isEmpty() ) {
			MetaProductBuildSettings build = action.getBuildSettings( meta );
			BUILDVERSION = build.CONFIG_APPVERSION;
		}
			
		if( BUILDVERSION.isEmpty() )
			action.exit0( _Error.BuildVersionNotSet0 , "buildByTag: BUILDVERSION not set" );
		
		return( BUILDVERSION );
	}

	public boolean isSimilarItem( ActionBase action , ActionScopeTargetItem sample ) throws Exception {
		if( distItem != sample.distItem ||
			sourceItem != sample.sourceItem ||
			envServerNode != sample.envServerNode ||
			schema != sample.schema ||
			scriptIndex != sample.scriptIndex )
			return( false );
		
		if( scriptIndex )
			return( NAME.equals( sample.NAME ) );
		
		return( true );
	}
	
}
