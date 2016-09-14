package org.urm.engine.meta;

import org.urm.action.ActionBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaDatabaseAdministration {

	protected Meta meta;
	public MetaDatabase database;

	public MetaDatabaseAdministration( Meta meta , MetaDatabase database ) {
		this.meta = meta;
		this.database = database;
	}

	public void load( ActionBase action , Node node ) throws Exception {
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
	}
	
}
