package org.urm.meta.product;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.schedule.ScheduleProperties;
import org.urm.meta.Types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaDump {

	public Meta meta;
	public MetaDatabase database;

	public boolean EXPORT;
	public String NAME;
	public String DESC;
	public String ENV;
	public String SG;
	public String SERVER;
	public String DATASET;
	public String TABLESETFILE;
	public String DUMPDIR;
	public String REMOTE_SETDBENV;
	public String DATABASE_DATAPUMPDIR;
	public String POSTREFRESH;
	public String SCHEDULE;
	public boolean STANDBY;
	public boolean NFS;
	public boolean ONLINE;
	
	List<String> tables;

	public MetaDump( Meta meta , MetaDatabase database ) {
		this.meta = meta;
		this.database = database;
		
		tables = new LinkedList<String>(); 
	}
	
	public MetaDump copy( ActionBase action , Meta rmeta , MetaDatabase rdatabase ) throws Exception {
		MetaDump r = new MetaDump( rmeta , rdatabase );
		r.EXPORT = EXPORT;
		r.NAME = NAME;
		r.DESC = DESC;
		r.ENV = ENV;
		r.SG = SG;
		r.SERVER = SERVER;
		r.DATASET = DATASET;
		r.TABLESETFILE = TABLESETFILE;
		r.DUMPDIR = DUMPDIR;
		r.REMOTE_SETDBENV = REMOTE_SETDBENV;
		r.DATABASE_DATAPUMPDIR = DATABASE_DATAPUMPDIR;
		r.POSTREFRESH = POSTREFRESH;
		r.SCHEDULE = SCHEDULE; 
		r.STANDBY = STANDBY;
		r.NFS = NFS;
		r.ONLINE = ONLINE;
		r.tables.addAll( tables );
		return( r );
	}

	public void load( ActionBase action , Node node ) throws Exception {
		EXPORT = ConfReader.getBooleanAttrValue( node , "export" , true );
		NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		DESC = ConfReader.getAttrValue( node , "desc" );
		ENV = ConfReader.getAttrValue( node , "env" );
		SG = ConfReader.getAttrValue( node , "sg" );
		SERVER = ConfReader.getAttrValue( node , "server" );
		DATASET = ConfReader.getAttrValue( node , "dataset" );
		TABLESETFILE = ConfReader.getAttrValue( node , "tableset" );
		DUMPDIR = ConfReader.getAttrValue( node , "dumpdir" );
		REMOTE_SETDBENV = ConfReader.getAttrValue( node , "setdbenv" );
		DATABASE_DATAPUMPDIR = ConfReader.getAttrValue( node , "datapumpdir" );
		POSTREFRESH = ConfReader.getAttrValue( node , "postrefresh" );
		SCHEDULE = ConfReader.getAttrValue( node , "schedule" );
		STANDBY = ConfReader.getBooleanAttrValue( node , "standby" , false );
		NFS = ConfReader.getBooleanAttrValue( node , "nfs" , false );
		ONLINE = ConfReader.getBooleanAttrValue( node , "online" , false );
		
		Node tablesNode = ConfReader.xmlGetFirstChild( node , "tables" );
		if( tablesNode != null ) {
			Node[] items = ConfReader.xmlGetChildren( node , "group" );
			if( items == null )
				return;
			
			for( Node groupNode : items ) {
				String set = ConfReader.getAttrValue( groupNode , "set" );
				tables.add( set );
			}
		}
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementBooleanAttr( doc , root , "export" , EXPORT );
		Common.xmlSetElementAttr( doc , root , "name" , NAME );
		Common.xmlSetElementAttr( doc , root , "desc" , DESC );
		Common.xmlSetElementAttr( doc , root , "env" , ENV );
		Common.xmlSetElementAttr( doc , root , "sg" , SG );
		Common.xmlSetElementAttr( doc , root , "server" , SERVER );
		Common.xmlSetElementAttr( doc , root , "dataset" , DATASET );
		Common.xmlSetElementAttr( doc , root , "tableset" , TABLESETFILE );
		Common.xmlSetElementAttr( doc , root , "dumpdir" , DUMPDIR );
		Common.xmlSetElementAttr( doc , root , "setdbenv" , REMOTE_SETDBENV );
		Common.xmlSetElementAttr( doc , root , "datapumpdir" , DATABASE_DATAPUMPDIR );
		Common.xmlSetElementAttr( doc , root , "postrefresh" , POSTREFRESH );
		Common.xmlSetElementAttr( doc , root , "schedule" , SCHEDULE );
		Common.xmlSetElementBooleanAttr( doc , root , "standby" , STANDBY );
		Common.xmlSetElementBooleanAttr( doc , root , "nfs" , NFS );
		Common.xmlSetElementBooleanAttr( doc , root , "online" , ONLINE );
		
		Element tablesElement = Common.xmlCreateElement( doc , root , "tables" );
		for( String set : tables ) {
			Element groupElement = Common.xmlCreateElement( doc , tablesElement , "set" );
			Common.xmlSetElementAttr( doc , groupElement , "set" , set );
		}
	}

	public boolean isOnline() {
		return( ONLINE );
	}

	public void create( String name , String desc , boolean export ) {
		this.EXPORT = export;
		this.NAME = name;
		this.DESC = desc;
	}
	
	public void modify( String name , String desc ) {
		this.NAME = name;
		this.DESC = desc;
	}
	
	public void setTarget( MetaEnvServer server , boolean standby , String setdbenv ) {
		this.ENV = server.sg.env.NAME;
		this.SG = server.sg.NAME;
		this.SERVER = server.NAME;
		this.STANDBY = standby;
		this.REMOTE_SETDBENV = setdbenv;
	}
	
	public void setFiles( String dataset , String dumpdir , String datapumpdir , boolean nfs , String postRefresh ) {
		this.DATASET = dataset;
		this.DUMPDIR = dumpdir;
		this.DATABASE_DATAPUMPDIR = datapumpdir;
		this.NFS = nfs;
		this.POSTREFRESH = postRefresh;
	}

	public void setSchedule( ScheduleProperties schedule ) {
		if( schedule == null )
			this.SCHEDULE = "";
		else
			this.SCHEDULE = schedule.getScheduleData();
	}

	public String[] getTables() {
		return( tables.toArray( new String[0] ) );
	}

	public void addTables( String schema , String set ) {
		tables.add( schema + ":" + set );
	}
	
	public void deleteTables( int index ) {
		tables.remove( index );
	}

	public void setOnline( boolean online ) {
		this.ONLINE = online;
	}

	public Map<String,Map<String,String>> getTableSets( String schemaFilter ) {
		Map<String,Map<String,String>> tableSet = new HashMap<String,Map<String,String>>();
		for( String data : tables ) {
			String[] parts = Common.split( data , ":" );
			if( parts.length < 2 )
				continue;
			
			String schema = ( parts.length > 0 )? parts[0] : "";
			String set = ( parts.length > 1 )? parts[1] : "";
			if( !schemaFilter.isEmpty() ) {
				if( !schema.equals( schemaFilter ) )
					continue;
			}
			
			Map<String,String> schemaSet = tableSet.get( schema );
			if( schemaSet == null ) {
				schemaSet = new HashMap<String,String>();
				tableSet.put( schema , schemaSet );
			}
			
			schemaSet.put( set , set );
		}
		
		return( tableSet );
	}
	
}
