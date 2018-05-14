package org.urm.meta.system;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.engine.products.EngineProductEnvs;
import org.urm.engine.schedule.ScheduleProperties;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.loader.MatchItem;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDatabaseSchema;

public class ProductDump {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_ENV = "env";
	public static String PROPERTY_SEGMENT = "segment";
	public static String PROPERTY_SERVER = "server";
	public static String PROPERTY_EXPORT = "export";
	public static String PROPERTY_DATASET = "dataset";
	public static String PROPERTY_OWNTABLESET = "owntableset";
	public static String PROPERTY_DUMPDIR = "dumpdir";
	public static String PROPERTY_SETDBENV = "setdbenv";
	public static String PROPERTY_DATAPUMPDIR = "datapumpdir";
	public static String PROPERTY_POSTREFRESH = "postrefresh";
	public static String PROPERTY_SCHEDULE = "schedule";
	public static String PROPERTY_STANDBY = "standby";
	public static String PROPERTY_NFS = "nfs";
	public static String PROPERTY_OFFLINE = "offline";
	
	public AppProductDumps dumps; 
	
	public int ID;
	public boolean MODEEXPORT;
	public String NAME;
	public String DESC;
	public MatchItem DB;
	public String DB_FKENV;
	public String DB_FKSG;
	public String DB_FKSERVER;
	public String DATASET;
	public boolean OWNTABLESET;
	public String DUMPDIR;
	public String REMOTE_SETDBENV;
	public String DATABASE_DATAPUMPDIR;
	public String POSTREFRESH;
	public boolean USESTANDBY;
	public boolean USENFS;
	public boolean OFFLINE;
	public int EV;

	public ScheduleProperties schedule;
	Map<Integer,ProductDumpMask> tables;

	public ProductDump( AppProductDumps dumps ) {
		this.dumps = dumps;
		
		schedule = new ScheduleProperties();
		tables = new HashMap<Integer,ProductDumpMask>();
	}
	
	public ProductDump copy( AppProductDumps rdumps ) {
		ProductDump r = new ProductDump( rdumps );
		
		r.ID = ID;
		r.MODEEXPORT = MODEEXPORT;
		r.NAME = NAME;
		r.DESC = DESC;
		r.DB = MatchItem.copy( DB );
		r.DB_FKENV = DB_FKENV;
		r.DB_FKSG = DB_FKSG;
		r.DB_FKSERVER = DB_FKSERVER;
		r.DATASET = DATASET;
		r.OWNTABLESET = OWNTABLESET;
		r.DUMPDIR = DUMPDIR;
		r.REMOTE_SETDBENV = REMOTE_SETDBENV;
		r.DATABASE_DATAPUMPDIR = DATABASE_DATAPUMPDIR;
		r.POSTREFRESH = POSTREFRESH;
		r.USESTANDBY = USESTANDBY;
		r.USENFS = USENFS;
		r.OFFLINE = OFFLINE;
		r.EV = EV;

		r.schedule = schedule.copy();
		
		for( ProductDumpMask mask : tables.values() ) {
			ProductDumpMask rmask = mask.copy( r );
			addTableMask( rmask );
		}
		
		return( r );
	}

	public void addTableMask( ProductDumpMask mask ) {
		tables.put( mask.ID , mask );
	}
	
	public void removeTableMask( ProductDumpMask mask ) {
		tables.remove( mask.ID );
	}
	
	public boolean isOnline() {
		return( !OFFLINE );
	}

	public boolean isExport() {
		return( MODEEXPORT );
	}
	
	public boolean isImport() {
		if( MODEEXPORT )
			return( false );
		return( true );
	}
	
	public void create( String name , String desc , boolean export ) {
		this.MODEEXPORT = export;
		this.NAME = name;
		this.DESC = desc;
	}
	
	public void modify( String name , String desc ) {
		this.NAME = name;
		this.DESC = desc;
	}

	public void setTarget( MetaEnvServer server , boolean standby , String setdbenv ) {
		setTargetServer( server );
		this.USESTANDBY = standby;
		this.REMOTE_SETDBENV = setdbenv;
	}

	public void setTargetServer( Integer serverId , String fkEnv , String fkSegment , String fkServer ) {
		if( serverId != null ) {
			this.DB = MatchItem.create( serverId );
			this.DB_FKENV = "";
			this.DB_FKSG = "";
			this.DB_FKSERVER = "";
		}
		else {
			this.DB = MatchItem.create( fkServer );
			this.DB_FKENV = fkEnv;
			this.DB_FKSG = fkSegment;
			this.DB_FKSERVER = fkServer;
		}
	}
	
	public void setTargetServer( MetaEnvServer server ) {
		this.DB = MatchItem.create( server.ID );
		this.DB_FKENV = "";
		this.DB_FKSG = "";
		this.DB_FKSERVER = "";
	}

	public void setTargetDetails( boolean standby , String setdbenv ) {
		this.USESTANDBY = standby;
		this.REMOTE_SETDBENV = setdbenv;
	}
	
	public void setFiles( String dataset , boolean ownTables , String dumpdir , String datapumpdir , boolean nfs , String postRefresh ) {
		this.DATASET = dataset;
		this.OWNTABLESET = ownTables;
		this.DUMPDIR = dumpdir;
		this.DATABASE_DATAPUMPDIR = datapumpdir;
		this.USENFS = nfs;
		this.POSTREFRESH = postRefresh;
	}

	public void setFilesDataset( String dataset ) {
		this.DATASET = dataset;
	}

	public void setFilesDetails( boolean ownTables , String dumpdir , String datapumpdir , boolean nfs , String postRefresh ) {
		this.OWNTABLESET = ownTables;
		this.DUMPDIR = dumpdir;
		this.DATABASE_DATAPUMPDIR = datapumpdir;
		this.USENFS = nfs;
		this.POSTREFRESH = postRefresh;
	}
	
	public void setSchedule( ScheduleProperties schedule ) {
		this.schedule = schedule;
	}

	public ProductDumpMask[] getTables() {
		return( tables.values().toArray( new ProductDumpMask[0] ) );
	}

	public void setOffline( boolean offline ) {
		this.OFFLINE = offline;
	}

	public Map<String,List<ProductDumpMask>> getTableSets( String schemaFilter ) throws Exception {
		MetaEnvServer server = getServer();
		MetaDatabase db = server.meta.getDatabase();
		
		Map<String,List<ProductDumpMask>> tableSet = new HashMap<String,List<ProductDumpMask>>();
		for( ProductDumpMask data : tables.values() ) {
			MetaDatabaseSchema schema = db.getSchema( data.SCHEMA );
			if( !schemaFilter.isEmpty() ) {
				if( !schema.NAME.equals( schemaFilter ) )
					continue;
			}
			
			List<ProductDumpMask> schemaSet = tableSet.get( schema.NAME );
			if( schemaSet == null ) {
				schemaSet = new LinkedList<ProductDumpMask>();
				tableSet.put( schema.NAME , schemaSet );
			}
			
			schemaSet.add( data );
		}
		
		return( tableSet );
	}

	public MetaEnvServer getServer() throws Exception {
		MetaEnvServer server = findServer();
		if( server == null )
			Common.exitUnexpected();
		return( server );
	}
	
	public MetaEnvServer findServer() {
		EngineProductEnvs envs = dumps.product.findEnvs();
		if( DB.MATCHED )
			return( envs.findServer( DB.FKID ) );
		
		MetaEnv env = envs.findEnv( DB_FKENV );
		if( env == null )
			return( null );
		
		MetaEnvSegment sg = env.findSegment( DB_FKSG );
		if( sg == null )
			return( null );
		
		return( sg.findServer( DB_FKSERVER ) );
	}

	public ProductDumpMask getDumpMask( int id ) throws Exception {
		ProductDumpMask mask = tables.get( id );
		if( mask == null )
			Common.exitUnexpected();
		return( mask );
	}
	
}
