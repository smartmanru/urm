package org.urm.meta.env;

import org.urm.meta.loader.MatchItem;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDatabaseSchema;

public class MetaDumpMask {

	public static String PROPERTY_INCLUDE = "inlcude";
	public static String PROPERTY_SCHEMA = "schema";
	public static String PROPERTY_MASK = "mask";
	
	public Meta meta;
	public MetaDump dump;

	public int ID;
	public MatchItem SCHEMA;
	public boolean INCLUDE;
	public String TABLEMASK;
	public int EV;
	
	public MetaDumpMask( Meta meta , MetaDump dump ) {
		this.meta = meta;
		this.dump = dump;
		
		ID = -1;
		EV = -1;
	}

	public MetaDumpMask copy( Meta rmeta , MetaDump rdump ) {
		MetaDumpMask r = new MetaDumpMask( rmeta , rdump );
		
		 r.ID = ID;
		 r.SCHEMA = MatchItem.copy( SCHEMA );
		 r.INCLUDE = INCLUDE;
		 r.TABLEMASK = TABLEMASK;
		 r.EV = EV;
		 
		 return( r );
	}

	public void create( MetaDatabaseSchema schema , boolean include , String tablemask ) {
		modify( schema , include , tablemask );
	}
	
	public void modify( MetaDatabaseSchema schema , boolean include , String tablemask ) {
		this.SCHEMA = MatchItem.create( schema.ID );
		this.INCLUDE = include;
		this.TABLEMASK = tablemask;
	}

	public MetaDatabaseSchema findSchema() {
		MetaDatabase db = meta.getDatabase();
		return( db.findSchema( SCHEMA ) );
	}
	
}
