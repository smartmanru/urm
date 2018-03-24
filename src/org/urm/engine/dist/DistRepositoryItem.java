package org.urm.engine.dist;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.dist.DistRepository.DistOperation;
import org.urm.engine.storage.RemoteFolder;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.product.Meta;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseDist;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DistRepositoryItem {

	public DistRepository repo;
	
	public String RELEASEDIR;
	public String DISTPATH;

	public Dist dist;
	
	List<DistRepositoryItemAction> history;
	
	public DistRepositoryItem( DistRepository repo ) {
		this.repo = repo;
		history = new LinkedList<DistRepositoryItemAction>(); 
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		RELEASEDIR = ConfReader.getAttrValue( root , "releasedir" );
		
		Node[] items = ConfReader.xmlGetChildren( root , "action" );
		if( items == null )
			return;
		
		for( Node historyNode : items ) {
			DistRepositoryItemAction historyAction = new DistRepositoryItemAction( this );
			historyAction.load( action , historyNode );
			addHistory( historyAction );
		}
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "releasedir" , dist.RELEASEDIR );
		
		for( DistRepositoryItemAction historyAction : history ) {
			Element distElement = Common.xmlCreateElement( doc , root , "action" );
			historyAction.save( action , doc , distElement );
		}
	}

	public void readDist( ActionBase action , RemoteFolder distFolder , ReleaseDist releaseDist ) throws Exception {
		dist = read( action , repo , distFolder , releaseDist );
		RELEASEDIR = dist.RELEASEDIR;
	}
	
	public static Dist read( ActionBase action , DistRepository repo , RemoteFolder distFolder , ReleaseDist releaseDist ) throws Exception {
		if( !distFolder.checkExists( action ) ) {
			String path = distFolder.getLocalPath( action );
			action.exit1( _Error.MissingRelease1 , "release does not exist at " + path , path );
		}
		
		Dist dist = new Dist( repo.meta , repo , releaseDist );
		dist.setFolder( distFolder );
		dist.load( action );
		return( dist );
	}

	public void createItem( ActionBase action , String releaseDir , String distPath ) throws Exception {
		this.dist = null;
		this.RELEASEDIR = releaseDir;
		this.DISTPATH = distPath;
	}
	
	public void setDist( Dist dist ) throws Exception {
		if( !RELEASEDIR.equals( dist.RELEASEDIR ) )
			Common.exitUnexpected();
		
		this.dist = dist;
	}
	
	public static Dist createDistNormal( ActionBase action , Meta meta , DistRepository repo , RemoteFolder distFolder , Date releaseDate , ReleaseLifecycle lc , ReleaseDist releaseDist ) throws Exception {
		if( distFolder.checkExists( action ) ) {
			String path = distFolder.folderPath;
			action.ifexit( _Error.ReleaseAlreadyExists1 , "distributive already exists at " + path , new String[] { path } );
		}

		Dist dist = new Dist( repo.meta , repo , releaseDist );
		dist.setFolder( distFolder );
		dist.create( action , distFolder.folderName , releaseDate , lc );
		return( dist );
	}
	
	public static Dist createDistMaster( ActionBase action , DistRepository repo , RemoteFolder distFolder , ReleaseDist releaseDist ) throws Exception {
		if( distFolder.checkExists( action ) ) {
			String path = distFolder.folderPath;
			action.ifexit( _Error.ReleaseAlreadyExists1 , "distributive already exists at " + path , new String[] { path } );
			
			if( action.isForced() )
				distFolder.removeFiles( action , "history.txt state.txt" );
		}
		else
			distFolder.ensureExists( action );
		
		Release release = releaseDist.release;
		Dist dist = new Dist( repo.meta , repo , releaseDist );
		dist.setFolder( distFolder );
		dist.createMaster( action );
		distFolder.createFileFromString( action , DistRepository.RELEASEHISTORYFILE , getHistoryRecord( action , release.RELEASEVER , "add" ) );
		return( dist );
	}
	
	private static String getHistoryRecord( ActionBase action , String RELEASEVER , String operation ) throws Exception {
		String s = Common.getNameTimeStamp() + ":" + operation + ":" + RELEASEVER;
		return( s );
	}

	public void addAction( ActionBase action , boolean success , DistOperation op , String msg ) throws Exception {
		DistRepositoryItemAction historyAction = new DistRepositoryItemAction( this );
		historyAction.create( action , success , op , msg );
		addHistory( historyAction );
	}
	
	public void archiveItem( ActionBase action ) throws Exception {
	}

	private void addHistory( DistRepositoryItemAction historyAction ) {
		history.add( historyAction );
	}
	
	public synchronized DistRepositoryItemAction[] getHistory() {
		return( history.toArray( new DistRepositoryItemAction[0] ) );
	}

	public synchronized Dist createDistNormal( ActionBase action , ReleaseDist releaseDist ) throws Exception {
		return( null );
	}
	
}
