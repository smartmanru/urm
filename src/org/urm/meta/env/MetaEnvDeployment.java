package org.urm.meta.env;

import org.urm.action.ActionBase;
import org.urm.meta.product.Meta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaEnvDeployment {

	protected Meta meta;
	public MetaEnvSegment sg;
	
	public MetaEnvDeployment( Meta meta , MetaEnvSegment sg ) {
		this.meta = meta;
		this.sg = sg;
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
	}

	public MetaEnvDeployment copy( ActionBase action , Meta meta , MetaEnvSegment sg ) throws Exception {
		MetaEnvDeployment r = new MetaEnvDeployment( meta , sg );
		return( r );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
	}
	
}
