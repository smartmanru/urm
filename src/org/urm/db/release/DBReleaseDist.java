package org.urm.db.release;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.run.EngineMethod;
import org.urm.meta.EngineLoader;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseDist;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DBReleaseDist {

	public static String ELEMENT_RELEASEPROPS = "version";
	public static String ELEMENT_RELEASEDISTPROPS = "dist";
	
	public static void exportxml( EngineLoader loader , ReleaseDist releaseDist , Document doc , Element root ) throws Exception {
		exportxmlReleaseProperties( loader , releaseDist.release , doc , root );
		exportxmlReleaseDistProperties( loader , releaseDist , doc , root );
		exportxmlReleaseSchedule( loader , releaseDist , doc , root );
		exportxmlReleaseChanges( loader , releaseDist , doc , root );
		exportxmlReleaseScope( loader , releaseDist , doc , root );
	}

	private static void exportxmlReleaseProperties( EngineLoader loader , Release release , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseMain;
		
		Element node = Common.xmlCreateElement( doc , root , ELEMENT_RELEASEPROPS );
		DBEngineEntities.exportxmlAppObject( doc , node , entity , new String[] {
				entity.exportxmlString( release.NAME ) ,
				entity.exportxmlString( release.DESC ) ,
				entity.exportxmlBoolean( release.MASTER ) ,
				entity.exportxmlEnum( release.TYPE ) ,
				entity.exportxmlString( release.RELEASEVER ) ,
				entity.exportxmlEnum( release.BUILDMODE ) ,
				entity.exportxmlString( release.COMPATIBILITY ) ,
				entity.exportxmlBoolean( release.CUMULATIVE ) ,
				entity.exportxmlBoolean( release.ARCHIVED )
		} , false );
	}
	
	private static void exportxmlReleaseDistProperties( EngineLoader loader , ReleaseDist releaseDist , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseDist;
		
		Element node = Common.xmlCreateElement( doc , root , ELEMENT_RELEASEDISTPROPS );
		DBEngineEntities.exportxmlAppObject( doc , node , entity , new String[] {
				entity.exportxmlString( releaseDist.DIST_VARIANT ) ,
				entity.exportxmlDate( releaseDist.DIST_DATE ) ,
				entity.exportxmlString( releaseDist.DATA_HASH )
		} , false );
	}
	
	private static void exportxmlReleaseSchedule( EngineLoader loader , ReleaseDist releaseDist , Document doc , Element root ) {
	}
	
	private static void exportxmlReleaseChanges( EngineLoader loader , ReleaseDist releaseDist , Document doc , Element root ) {
	}
	
	private static void exportxmlReleaseScope( EngineLoader loader , ReleaseDist releaseDist , Document doc , Element root ) {
	}
	
	public static ReleaseDist createReleaseDist( EngineMethod method , ActionBase action , Release release , String variant ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		
		ReleaseDist releaseDist = new ReleaseDist( release );
		releaseDist.create( variant );
		
		modifyReleaseDist( c , release , releaseDist , true );
		if( variant.isEmpty() )
			release.setDefaultDist( releaseDist );
		else
			release.addDist( releaseDist );
		
		return( releaseDist );
	}
	
	private static void modifyReleaseDist( DBConnection c , Release release , ReleaseDist releaseDist , boolean insert ) throws Exception {
		if( insert )
			releaseDist.ID = c.getNextSequenceValue();
		
		releaseDist.RV = c.getNextReleaseVersion( release );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppReleaseDist , releaseDist.ID , releaseDist.RV , new String[] {
				EngineDB.getObject( release.ID ) ,
				EngineDB.getString( releaseDist.DIST_VARIANT ) ,
				EngineDB.getDate( releaseDist.DIST_DATE ) ,
				EngineDB.getString( releaseDist.DATA_HASH )
				} , insert );
	}
	
}
