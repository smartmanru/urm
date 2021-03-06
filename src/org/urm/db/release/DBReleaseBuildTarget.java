package org.urm.db.release;

import java.sql.ResultSet;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums.*;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.run.EngineMethod;
import org.urm.meta.loader.EngineLoader;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.product.MetaSources;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseBuildTarget;
import org.urm.meta.release.ReleaseChanges;
import org.urm.meta.release.ReleaseScope;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBReleaseBuildTarget {

	public static void modifyReleaseBuildTarget( DBConnection c , Release release , ReleaseBuildTarget target , boolean insert ) throws Exception {
		if( insert )
			target.ID = c.getNextSequenceValue();
		
		target.RV = c.getNextReleaseVersion( release );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppReleaseBuildTarget , target.ID , target.RV , new String[] {
				EngineDB.getObject( release.ID ) ,
				EngineDB.getBoolean( target.isScopeTarget() ) ,
				EngineDB.getEnum( target.TYPE ) ,
				EngineDB.getBoolean( target.ALL ) ,
				EngineDB.getMatchId( target.SRCSET ) ,
				EngineDB.getMatchName( target.SRCSET ) ,
				EngineDB.getMatchId( target.PROJECT ) ,
				EngineDB.getMatchName( target.PROJECT ) ,
				EngineDB.getString( target.BUILD_BRANCH ) ,
				EngineDB.getString( target.BUILD_TAG ) ,
				EngineDB.getString( target.BUILD_VERSION )
				} , insert );
	}

	public static void loaddbReleaseBuildTarget( EngineLoader loader , Release release , ReleaseChanges changes , ReleaseScope scope , ResultSet rs ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseBuildTarget;

		ReleaseBuildTarget target = null;
		boolean scopetarget = entity.loaddbBoolean( rs , ReleaseBuildTarget.PROPERTY_SCOPETARGET );
		if( scopetarget )
			target = new ReleaseBuildTarget( scope );
		else
			target = new ReleaseBuildTarget( changes );
		
		target.ID = entity.loaddbId( rs );
		target.RV = entity.loaddbVersion( rs );
		target.create(
				DBEnumBuildTargetType.getValue( entity.loaddbEnum( rs , ReleaseBuildTarget.PROPERTY_TARGETTYPE ) , true ) ,
				entity.loaddbMatchItem( rs , DBReleaseData.FIELD_BUILDTARGET_SRCSET_ID , ReleaseBuildTarget.PROPERTY_SRCSET ) ,
				entity.loaddbMatchItem( rs , DBReleaseData.FIELD_BUILDTARGET_PROJECT_ID , ReleaseBuildTarget.PROPERTY_PROJECT ) ,
				entity.loaddbString( rs , ReleaseBuildTarget.PROPERTY_BUILDBRANCH ) ,
				entity.loaddbString( rs , ReleaseBuildTarget.PROPERTY_BUILDTAG ) ,
				entity.loaddbString( rs , ReleaseBuildTarget.PROPERTY_BUILDVERSION ) ,
				entity.loaddbBoolean( rs , ReleaseBuildTarget.PROPERTY_ALL )
				);
		
		if( scopetarget )
			scope.addBuildTarget( target );
		else
			changes.addBuildTarget( target );
	}
	
	public static void exportxmlBuildTarget( EngineLoader loader , Release release , ReleaseBuildTarget target , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseBuildTarget;
		
		Meta meta = release.getMeta();
		MetaSources sources = meta.getSources();
		
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlEnum( target.TYPE ) ,
				entity.exportxmlBoolean( target.ALL ) ,
				entity.exportxmlString( sources.getProjectSetName( target.SRCSET ) ) ,
				entity.exportxmlString( sources.getProjectName( target.PROJECT ) ) ,
				entity.exportxmlString( target.BUILD_BRANCH ) ,
				entity.exportxmlString( target.BUILD_TAG ) ,
				entity.exportxmlString( target.BUILD_VERSION )
		} , true );
	}
	
	public static ReleaseBuildTarget importxmlBuildTarget( EngineLoader loader , Release release , ReleaseChanges changes , ReleaseScope scope , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseBuildTarget;
		
		ReleaseBuildTarget buildTarget = ( changes != null )? new ReleaseBuildTarget( changes ) : new ReleaseBuildTarget( scope );
		
		Meta meta = release.getMeta();
		MetaSources sources = meta.getSources();
		buildTarget.create(
				DBEnumBuildTargetType.getValue( entity.importxmlEnumAttr( root , ReleaseBuildTarget.PROPERTY_TARGETTYPE ) , true ) ,
				sources.getProjectMatchItem( null , entity.importxmlStringAttr( root , ReleaseBuildTarget.PROPERTY_SRCSET ) ) ,
				sources.getProjectMatchItem( null , entity.importxmlStringAttr( root , ReleaseBuildTarget.PROPERTY_PROJECT ) ) ,
				entity.importxmlStringAttr( root , ReleaseBuildTarget.PROPERTY_BUILDBRANCH ) ,
				entity.importxmlStringAttr( root , ReleaseBuildTarget.PROPERTY_BUILDTAG ) ,
				entity.importxmlStringAttr( root , ReleaseBuildTarget.PROPERTY_BUILDVERSION ) ,
				entity.importxmlBooleanAttr( root , ReleaseBuildTarget.PROPERTY_ALL , false )
				);
		modifyReleaseBuildTarget( c , release , buildTarget , true );
		return( buildTarget );
	}
	
	public static void deleteBuildTarget( DBConnection c , Release release , ReleaseBuildTarget target ) throws Exception {
		EngineEntities entities = c.getEntities();
		int version = c.getNextReleaseVersion( release );
		DBEngineEntities.deleteAppObject( c , entities.entityAppReleaseBuildTarget , target.ID , version );
	}
	
	public static ReleaseBuildTarget createSourceTarget( DBConnection c , Release release , ReleaseScope scope , boolean all ) throws Exception {
		ReleaseBuildTarget target = new ReleaseBuildTarget( scope );
		target.create( all );
		modifyReleaseBuildTarget( c , release , target , true );
		return( target );
	}
	
	public static ReleaseBuildTarget createSourceSetTarget( DBConnection c , Release release , ReleaseScope scope , MetaSourceProjectSet set , boolean all ) throws Exception {
		ReleaseBuildTarget target = new ReleaseBuildTarget( scope );
		target.create( set , all );
		modifyReleaseBuildTarget( c , release , target , true );
		return( target );
	}
	
	public static ReleaseBuildTarget createSourceProjectTarget( DBConnection c , Release release , ReleaseScope scope , MetaSourceProject project , boolean all ) throws Exception {
		ReleaseBuildTarget target = new ReleaseBuildTarget( scope );
		target.create( project , all );
		modifyReleaseBuildTarget( c , release , target , true );
		return( target );
	}

	public static void setTargetSpecifics( EngineMethod method , ActionBase action , Release release , ReleaseBuildTarget target , String branch , String tag , String version ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		method.checkUpdateRelease( release );
		
		if( release.isFinalized() )
			Common.exitUnexpected();
		
		release.setArchived();
		target.setSpecifics( branch , tag , version );
		modifyReleaseBuildTarget( c , release , target , false );
	}
	
}
