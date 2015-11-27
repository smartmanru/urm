package ru.egov.urm.run;

import ru.egov.urm.meta.MetaDistrBinaryItem;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.MetaSourceProjectItem;
import ru.egov.urm.meta.MetaReleaseTargetItem;

public class ActionScopeTargetItem {

	public String NAME;
	public MetaDistrBinaryItem distItem;
	public MetaSourceProjectItem sourceItem;
	public MetaEnvServerNode envServerNode; 
	public MetaReleaseTargetItem releaseItem;
	public boolean specifiedExplicitly;
	
	public ActionScopeTargetItem( MetaSourceProjectItem sourceItem , boolean specifiedExplicitly ) {
		this.distItem = sourceItem.distItem;
		this.sourceItem = sourceItem;
		this.specifiedExplicitly = specifiedExplicitly;
		this.NAME = sourceItem.ITEMNAME;
	}
	
	public ActionScopeTargetItem( MetaEnvServerNode envServerNode , boolean specifiedExplicitly ) {
		this.envServerNode = envServerNode;
		this.specifiedExplicitly = specifiedExplicitly;
		this.NAME = "" + envServerNode.POS;
	}
	
	public ActionScopeTargetItem( MetaReleaseTargetItem releaseItem , boolean specifiedExplicitly ) {
		this.distItem = releaseItem.distItem;
		this.sourceItem = releaseItem.sourceItem;
		this.releaseItem = releaseItem;
		this.specifiedExplicitly = specifiedExplicitly;
		this.NAME = releaseItem.NAME;
	}

	public String getProjectItemBuildVersion( ActionBase action ) throws Exception {
		String BUILDVERSION = "";
		if( !action.options.OPT_VERSION.isEmpty() )
			BUILDVERSION = action.options.OPT_VERSION;
		
		if( BUILDVERSION.isEmpty() && releaseItem != null ) {
			BUILDVERSION = releaseItem.BUILDVERSION;
			
			if( BUILDVERSION.isEmpty() )
				BUILDVERSION = releaseItem.releaseProject.BUILDVERSION;
		}
		
		if( BUILDVERSION.isEmpty() )
			BUILDVERSION = sourceItem.ITEMVERSION;
		
		if( BUILDVERSION.isEmpty() )
			BUILDVERSION = action.meta.product.CONFIG_APPVERSION;
			
		if( BUILDVERSION.isEmpty() )
			action.exit( "buildByTag: BUILDVERSION not set" );
		
		return( BUILDVERSION );
	}
	
}
