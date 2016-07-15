package org.urm.common.meta;

import org.urm.common.action.CommandMethod;
import org.urm.common.action.CommandMeta;

public class XDocCommandMeta extends CommandMeta {

	public static String NAME = "xdoc";
	public static String DESC = "create and update documentation";
	
	public XDocCommandMeta() {
		super( NAME , DESC );
		
		String releaseOpts = "";
		defineAction( CommandMethod.newNormal( "design" , true , "create design docs" , releaseOpts , "./design.sh [OPTIONS] {dot|png} <outdir>" ) );
	}	

}
