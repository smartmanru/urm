package org.urm.meta.system;

import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.loader.MatchItem;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDatabaseSchema;

public class ProductDumpMask {

	public static String PROPERTY_INCLUDE = "inlcude";
	public static String PROPERTY_SCHEMA = "schema";
	public static String PROPERTY_MASK = "mask";
	
	public ProductDump dump;

	public int ID;
	public MatchItem SCHEMA;
	public boolean INCLUDE;
	public String TABLEMASK;
	public int EV;
	
	public ProductDumpMask( ProductDump dump ) {
		this.dump = dump;
		
		ID = -1;
		EV = -1;
	}

	public ProductDumpMask copy( ProductDump rdump ) {
		ProductDumpMask r = new ProductDumpMask( rdump );
		
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
		MetaEnvServer server = dump.findServer(); 
		MetaDatabase db = server.meta.getDatabase();
		return( db.findSchema( SCHEMA ) );
	}
	
}
