package ru.egov.urm.meta;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.storage.MetadataStorage;

public class MetaDatabase {

	Metadata meta;

	public Map<String,MetaDatabaseSchema> mapSchema = new HashMap<String,MetaDatabaseSchema>();
	public Map<String,MetaDatabaseDatagroup> mapDatagroup = new HashMap<String,MetaDatabaseDatagroup>();
	public Map<String,String> mapAligned = new HashMap<String,String>();
	
	public String PUBLISHERS;
	public String ALIGNEDMAPPING;
	
	public MetaDatabase( Metadata meta ) {
		this.meta = meta;
	}

	public void load( ActionBase action , Node node ) throws Exception {
		if( !loadAdministration( action , node ) )
			return;
		
		loadSchemaSet( action , node );
	}

	public boolean loadAdministration( ActionBase action , Node node ) throws Exception {
		Node administration = ConfReader.xmlGetFirstChild( action , node , "administration" );
		if( administration == null ) {
			action.debug( "database administration is missing, ignore database information." );
			return( false );
		}
		
		Node pubs = ConfReader.xmlGetFirstChild( action , administration , "publishers" );
		PUBLISHERS = ConfReader.getAttrValue( action , pubs , "list" );

		Node aligned = ConfReader.xmlGetFirstChild( action , administration , "aligned" );
		if( aligned == null )
			return( true );
		
		ALIGNEDMAPPING = ConfReader.getAttrValue( action , aligned , "mapping" );
		String[] list = Common.splitSpaced( ALIGNEDMAPPING );
		for( String pair : list ) {
			String name = Common.getPartBeforeFirst( pair , "=" );
			String id = Common.getPartAfterFirst( pair , "=" );
			mapAligned.put( name , id );
		}
		
		return( true );
	}

	public void loadSchemaSet( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( action , node , "schema" );
		if( items == null )
			return;
		
		for( Node schemaNode : items ) {
			MetaDatabaseSchema item = new MetaDatabaseSchema( meta , this );
			item.load( action , schemaNode );
			mapSchema.put( item.SCHEMA , item );
		}
		
		items = ConfReader.xmlGetChildren( action , node , "datagroup" );
		if( items == null )
			return;
		
		for( Node dgNode : items ) {
			MetaDatabaseDatagroup item = new MetaDatabaseDatagroup( meta , this );
			item.load( action , dgNode );
			mapDatagroup.put( item.NAME , item );
		}
	}
	
	public MetaDatabaseSchema getSchema( ActionBase action , String name ) throws Exception {
		MetaDatabaseSchema schema = mapSchema.get( name );
		if( schema == null )
			action.exit( "unknown schema=" + name );
		return( schema );
	}

	public String alignedGetIDByBame( ActionBase action , String P_NAME ) throws Exception {
		if( mapAligned.isEmpty() ) {
			if( !P_NAME.equals( "common" ) )
				action.exit( "unable to use aligned items due to empty aligned mapping in distr specification" );
	
			return( "0" );
		}
		
		if( P_NAME.equals( "common" ) )
			return( "0" );
		
		if( P_NAME.equals( "regional" ) )
			return( "9" );

		String S_COMMON_ALIGNEDID = mapAligned.get( P_NAME );
		if( S_COMMON_ALIGNEDID.isEmpty() )
			action.exit( "unable to find aligned id for name=" + P_NAME );
		
		return( S_COMMON_ALIGNEDID );
	}

	public String getSqlIndexPrefix( ActionBase action , String P_FORLDERNAME , String P_ALIGNEDID ) throws Exception {
		String F_FOLDERNAME = Common.replace( P_FORLDERNAME , "/" , "." );
		String F_FOLDERBASE = Common.getPartBeforeFirst( F_FOLDERNAME , "." );

		String X_ALIGNED = "A" + P_ALIGNEDID;
		String X_TYPE = null;
		String X_INSTANCE = "main";
		
		String S_SQL_DIRID = "";
		if( F_FOLDERBASE.equals( "coreddl" ) )
			X_TYPE = "00";
		else if( F_FOLDERBASE.equals( "coredml" ) )
			X_TYPE = "01";
		else if( F_FOLDERBASE.equals( "coreprodonly" ) || F_FOLDERBASE.equals( "coreuatonly" ) )
			X_TYPE = "02";
		else if( F_FOLDERBASE.equals( "coresvc" ) )
			X_TYPE = "03";
		else if( F_FOLDERBASE.equals( "dataload" ) )
			X_TYPE = "04";
		else if( F_FOLDERBASE.equals( "war" ) ) {
			String[] items = Common.splitDotted( F_FOLDERNAME );
		
			String S_WAR_REGIONID = items[1];
			String S_WAR_NAME = items[2];
			String S_WAR_SUBDIRNAME = items[3];

			if( S_WAR_SUBDIRNAME.equals( "juddi" ) )
				X_TYPE = "14";
			else if( S_WAR_SUBDIRNAME.equals( "svcdic" ) )
				X_TYPE = "15";
			else if( S_WAR_SUBDIRNAME.equals( "svcspec" ) )
				X_TYPE = "16";
			else
				action.exit( "invalid folder=" + P_FORLDERNAME );

			String S_WAR_MRID = meta.distr.getWarMRId( action , S_WAR_NAME );
			X_INSTANCE = S_WAR_REGIONID + "M" + S_WAR_MRID;
		}
		else if( F_FOLDERBASE.equals( "forms" ) ) {
			String S_ORG_REGIONID = Common.getListItem( F_FOLDERNAME , "." , 1 );
			String S_ORG_EXTID = Common.getListItem( F_FOLDERNAME , "." , 2 );
			String S_ORG_SUBDIRNAME = Common.getListItem( F_FOLDERNAME , "." , 3 );

			// get ORGID info
			String S_ORG_FOLDERID = getOrgInfo( action , S_ORG_EXTID );
			if( S_ORG_FOLDERID.isEmpty() )
				action.exit( "unknown orgExtId=" + S_ORG_EXTID );
			
			if( S_ORG_SUBDIRNAME.equals( "juddi" ) )
				X_TYPE = "24";
			else if( S_ORG_SUBDIRNAME.equals( "svcdic" ) )
				X_TYPE = "25";
			else if( S_ORG_SUBDIRNAME.equals( "svcspec" ) )
				X_TYPE = "26";
			else if( S_ORG_SUBDIRNAME.equals( "svcform" ) )
				X_TYPE = "27";
			else if( S_ORG_SUBDIRNAME.equals( "svcpub" ) )
				X_TYPE = "28";
			else
				action.exit( "invalid database folder=" + P_FORLDERNAME );

			X_INSTANCE = S_ORG_REGIONID + "O" + S_ORG_EXTID;
		}
		else
			action.exit( "invalid database folder=" + P_FORLDERNAME );
		
		S_SQL_DIRID = X_ALIGNED + "-T" + X_TYPE + "-I" + X_INSTANCE;
		return( S_SQL_DIRID );
	}

	public boolean checkOrgInfo( ActionBase action , String S_ORG_EXTID ) throws Exception {
		String S_ORG_FOLDERID = getOrgInfo( action , S_ORG_EXTID );
		if( S_ORG_FOLDERID.isEmpty() )
			return( false );
		return( true );
	}

	public String getOrgInfo( ActionBase action , String S_ORG_EXTID ) throws Exception {
		// read org item mapping
		MetadataStorage storage = action.artefactory.getMetadataStorage( action );
		String path = storage.getOrgInfoFile( action );
		if( !action.session.checkFileExists( action , path ) )
			action.exit( "organizational mapping file " + path + " not found" );
			
		String S_ORG_FOLDERID = action.session.customGetValue( action , "grep " + Common.getQuoted( "^" + S_ORG_EXTID + "=" ) + " " + path + " | cut -d " + Common.getQuoted( "=" ) + " -f2" );
		return( S_ORG_FOLDERID );
	}
	
	public boolean checkPublisher( ActionBase action , String F_PUBLISHER )	throws Exception {
		return( Common.checkPartOfSpacedList( F_PUBLISHER , PUBLISHERS ) );
	}
	
	public MetaDatabaseDatagroup getDatagroup( ActionBase action , String name ) throws Exception {
		MetaDatabaseDatagroup datagroup = mapDatagroup.get( name );
		if( datagroup == null )
			action.exit( "unknown datagroup=" + name );
		return( datagroup );
	}
	
}
