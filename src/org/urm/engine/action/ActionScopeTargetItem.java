package org.urm.engine.action;

import org.urm.engine.dist.ReleaseTargetItem;
import org.urm.engine.meta.MetaDistrBinaryItem;
import org.urm.engine.meta.MetaEnvServerNode;
import org.urm.engine.meta.MetaProductBuildSettings;
import org.urm.engine.meta.MetaSourceProjectItem;

public class ActionScopeTargetItem {

	public String NAME;
	public MetaDistrBinaryItem distItem;
	public MetaSourceProjectItem sourceItem;
	public MetaEnvServerNode envServerNode; 
	public ReleaseTargetItem releaseItem;
	public boolean scriptIndex = false;
	public boolean specifiedExplicitly;
	
	private ActionScopeTargetItem() {
	}
	
	public static ActionScopeTargetItem createSourceProjectTargetItem( MetaSourceProjectItem sourceItem , boolean specifiedExplicitly ) {
		ActionScopeTargetItem ti = new ActionScopeTargetItem(); 
		ti.distItem = sourceItem.distItem;
		ti.sourceItem = sourceItem;
		ti.specifiedExplicitly = specifiedExplicitly;
		ti.NAME = sourceItem.ITEMNAME;
		return( ti );
	}
	
	public static ActionScopeTargetItem createEnvServerNodeTargetItem( MetaEnvServerNode envServerNode , boolean specifiedExplicitly ) {
		ActionScopeTargetItem ti = new ActionScopeTargetItem(); 
		ti.envServerNode = envServerNode;
		ti.specifiedExplicitly = specifiedExplicitly;
		ti.NAME = "" + envServerNode.POS;
		return( ti );
	}
	
	public static ActionScopeTargetItem createReleaseTargetItem( ReleaseTargetItem releaseItem , boolean specifiedExplicitly ) {
		ActionScopeTargetItem ti = new ActionScopeTargetItem(); 
		ti.distItem = releaseItem.distItem;
		ti.sourceItem = releaseItem.sourceItem;
		ti.releaseItem = releaseItem;
		ti.specifiedExplicitly = specifiedExplicitly;
		ti.NAME = releaseItem.NAME;
		return( ti );
	}

	public static ActionScopeTargetItem createScriptIndexTargetItem( String index ) {
		ActionScopeTargetItem ti = new ActionScopeTargetItem();
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
			BUILDVERSION = sourceItem.ITEMVERSION;
		
		if( BUILDVERSION.isEmpty() ) {
			MetaProductBuildSettings build = action.getBuildSettings();
			BUILDVERSION = build.CONFIG_APPVERSION;
		}
			
		if( BUILDVERSION.isEmpty() )
			action.exit0( _Error.BuildVersionNotSet0 , "buildByTag: BUILDVERSION not set" );
		
		return( BUILDVERSION );
	}
	
}
