package org.urm.meta.env;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.engine.schedule.ScheduleProperties;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDatabaseSchema;

public class MetaDump {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
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
	
	public Meta meta;
	public MetaEnv env;

	public int ID;
	public boolean MODEEXPORT;
	public String NAME;
	public String DESC;
	public Integer SERVER;
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
	Map<Integer,MetaDumpMask> tables;

	public MetaDump( Meta meta , MetaEnv env ) {
		this.meta = meta;
		this.env = env;
		
		schedule = new ScheduleProperties();
		tables = new HashMap<Integer,MetaDumpMask>();
	}
	
	public MetaDump copy( Meta rmeta , MetaEnv renv ) throws Exception {
		MetaDump r = new MetaDump( rmeta , renv );
		
		r.ID = ID;
		r.MODEEXPORT = MODEEXPORT;
		r.NAME = NAME;
		r.DESC = DESC;
		r.SERVER = SERVER;
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
		
		for( MetaDumpMask mask : tables.values() ) {
			MetaDumpMask rmask = mask.copy( rmeta , r );
			addTableMask( rmask );
		}
		
		return( r );
	}

	public void addTableMask( MetaDumpMask mask ) {
		tables.put( mask.ID , mask );
	}
	
	public void removeTableMask( MetaDumpMask mask ) {
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
	
	public void setTargetServer( MetaEnvServer server ) {
		this.SERVER = server.ID;
		this.meta = server.meta;
		this.env = server.sg.env;
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

	public MetaDumpMask[] getTables() {
		return( tables.values().toArray( new MetaDumpMask[0] ) );
	}

	public void setOffline( boolean offline ) {
		this.OFFLINE = offline;
	}

	public Map<String,List<MetaDumpMask>> getTableSets( String schemaFilter ) throws Exception {
		MetaDatabase db = meta.getDatabase();
		
		Map<String,List<MetaDumpMask>> tableSet = new HashMap<String,List<MetaDumpMask>>();
		for( MetaDumpMask data : tables.values() ) {
			MetaDatabaseSchema schema = db.getSchema( data.SCHEMA );
			if( !schemaFilter.isEmpty() ) {
				if( !schema.NAME.equals( schemaFilter ) )
					continue;
			}
			
			List<MetaDumpMask> schemaSet = tableSet.get( schema.NAME );
			if( schemaSet == null ) {
				schemaSet = new LinkedList<MetaDumpMask>();
				tableSet.put( schema.NAME , schemaSet );
			}
			
			schemaSet.add( data );
		}
		
		return( tableSet );
	}

	public MetaEnvServer findServer() {
		return( env.findServer( SERVER ) );
	}

	public MetaDumpMask getDumpMask( int id ) throws Exception {
		MetaDumpMask mask = tables.get( id );
		if( mask == null )
			Common.exitUnexpected();
		return( mask );
	}
	
}
