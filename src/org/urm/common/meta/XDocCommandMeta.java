package org.urm.common.meta;

import org.urm.common.action.CommandMethodMeta;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.OptionsMeta;
import org.urm.common.action.CommandMethodMeta.ACTION_ACCESS;
import org.urm.meta.engine.EngineAuth.SecurityAction;

public class XDocCommandMeta extends CommandMeta {

	public static String NAME = "xdoc";
	public static String DESC = "create and update documentation";
	
	public XDocCommandMeta( OptionsMeta options ) {
		super( options , NAME , DESC );
		
		String releaseOpts = "";
		defineAction( CommandMethodMeta.newNormal( this , "design" , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_XDOC , true , "create design docs" , releaseOpts , "{dot|png} <outdir>" ) );
	}	

}
