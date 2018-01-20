package org.urm.meta.product;

public class MetaDatabaseAdministration {

	protected Meta meta;
	public MetaDatabase database;

	public MetaDatabaseAdministration( Meta meta , MetaDatabase database ) {
		this.meta = meta;
		this.database = database;
	}

	public MetaDatabaseAdministration copy( Meta rmeta , MetaDatabase rdatabase ) throws Exception {
		MetaDatabaseAdministration r = new MetaDatabaseAdministration( rmeta , rdatabase );
		return( r );
	}
	
}
