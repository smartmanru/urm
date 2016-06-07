package org.urm.client.meta;

import org.urm.common.action.CommandMethod;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;

public class XDocCommandMeta extends CommandMeta {

	public static String NAME = "xdoc";
	
	public XDocCommandMeta( CommandBuilder builder ) {
		super( builder , NAME );
		
		String releaseOpts = "";
		defineAction( CommandMethod.newAction( "design" , true , "create design docs" , releaseOpts , "./design.sh [OPTIONS] {dot|png} <outdir>" ) );
	}	

}
