package org.urm.engine.run;

import org.urm.action.ActionBase;
import org.urm.common.action.CommandMeta;
import org.urm.common.meta.ReleaseCommandMeta;
import org.urm.engine.BlotterService;
import org.urm.engine.Engine;
import org.urm.engine.action.CommandMethod;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.meta.product.Meta;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseDist;
import org.urm.meta.release.ReleaseRepository;
import org.urm.meta.release.ReleaseRepository.ReleaseOperation;

public class EngineMethodProductRelease {

	public EngineMethodProduct emm;
	private Release release;
	
	public Release releaseNew;
	public Release releaseOld;
	
	public boolean create;
	public boolean update;
	public boolean delete;
	
	public EngineMethodProductRelease( EngineMethodProduct emm , Release release ) {
		this.emm = emm;
		this.release = release;
		create = false;
		update = false;
		delete = false;
	}

	public void setCreated() throws Exception {
		create = true;
		this.releaseNew = release;
	}
	
	public void setUpdated( Release releaseUpdated ) throws Exception {
		update = true;
		if( releaseNew != null )
			return;

		releaseNew = releaseUpdated;
		release.modify( false );
		releaseOld = release;
	}
	
	public void setDeleted() throws Exception {
		delete = true;
		release.modify( false );
		releaseOld = release;
	}

	public void commit() throws Exception {
		if( releaseNew != null ) {
			Meta meta = release.getMeta();
			ReleaseRepository repo = emm.getReleaseRepository( meta.getStorage() );
			if( releaseNew.repo != repo )
				releaseNew.setRepository( repo );
		}
			
		if( releaseOld != null )
			releaseOld.modify( true );
		
		DistRepository distrepo = emm.getDistRepository();
		if( releaseNew != null ) {
			ReleaseDist releaseDist = releaseNew.getDefaultReleaseDist();
			Dist dist = distrepo.findDefaultDist( releaseNew );
			if( dist != null )
				dist.setReleaseDist( releaseDist );
		}
		
		changeBlotter();
	}
	
	public void abort() throws Exception {
		if( releaseOld != null )
			releaseOld.modify( true );
	}

	private void changeBlotter() {
		Engine engine = emm.method.engine;
		ActionBase action = emm.method.action;
		
		BlotterService blotter = engine.getBlotterService();
		CommandMethod method = emm.method.getMethod();
		CommandMeta command = method.method.command;

		String cn = command.name;
		String mn = method.method.name;
		if( cn.equals( ReleaseCommandMeta.NAME ) ) {
			if( mn.equals( ReleaseCommandMeta.METHOD_CREATE ) )
				blotter.runReleaseAction( action , releaseNew , ReleaseOperation.CREATE , "create release " + releaseNew.RELEASEVER );
			else
			if( mn.equals( ReleaseCommandMeta.METHOD_MODIFY ) )
				blotter.runReleaseAction( action , releaseNew , ReleaseOperation.MODIFY , "modify release " + releaseNew.RELEASEVER );
			else
			if( mn.equals( ReleaseCommandMeta.METHOD_PHASE ) )
				blotter.runReleaseAction( action , releaseNew , ReleaseOperation.PHASE , "change schedule of release " + releaseNew.RELEASEVER );
			else
			if( mn.equals( ReleaseCommandMeta.METHOD_DROP ) )
				blotter.runReleaseAction( action , releaseOld , ReleaseOperation.DROP , "drop release " + releaseOld.RELEASEVER );
			else
			if( mn.equals( ReleaseCommandMeta.METHOD_CLEANUP ) )
				blotter.runReleaseAction( action , releaseNew , ReleaseOperation.STATUS , "cleanup release " + releaseNew.RELEASEVER );
			else
			if( mn.equals( ReleaseCommandMeta.METHOD_COPY ) )
				blotter.runReleaseAction( action , releaseNew , ReleaseOperation.CREATE , "copy to release " + releaseNew.RELEASEVER );
			else
			if( mn.equals( ReleaseCommandMeta.METHOD_IMPORT ) )
				blotter.runReleaseAction( action , releaseNew , ReleaseOperation.CREATE , "import release " + releaseNew.RELEASEVER );
			else
			if( mn.equals( ReleaseCommandMeta.METHOD_FINISH ) )
				blotter.runReleaseAction( action , releaseNew , ReleaseOperation.FINISH , "finish release " + releaseNew.RELEASEVER );
			else
			if( mn.equals( ReleaseCommandMeta.METHOD_COMPLETE ) )
				blotter.runReleaseAction( action , releaseNew , ReleaseOperation.COMPLETE , "complete release " + releaseNew.RELEASEVER );
			else
			if( mn.equals( ReleaseCommandMeta.METHOD_REOPEN ) )
				blotter.runReleaseAction( action , releaseNew , ReleaseOperation.REOPEN , "reopen release " + releaseNew.RELEASEVER );
			else
			if( mn.equals( ReleaseCommandMeta.METHOD_MASTER ) ) {
				if( releaseOld == null )
					blotter.runReleaseAction( action , releaseNew , ReleaseOperation.CREATE , "create master " + releaseNew.RELEASEVER );
				else
				if( releaseNew == null )
					blotter.runReleaseAction( action , releaseOld , ReleaseOperation.DROP , "drop master " + releaseOld.RELEASEVER );
				else
					blotter.runReleaseAction( action , releaseNew , ReleaseOperation.MODIFY , "modify master " + releaseNew.RELEASEVER );
			}
			else
			if( mn.equals( ReleaseCommandMeta.METHOD_ARCHIVE ) )
				blotter.runReleaseAction( action , releaseOld , ReleaseOperation.ARCHIVE , "archive release " + releaseOld.RELEASEVER );
			else
			if( mn.equals( ReleaseCommandMeta.METHOD_TOUCH ) )
				blotter.runReleaseAction( action , releaseOld , ReleaseOperation.STATUS , "refresh release " + releaseOld.RELEASEVER );
			else
			if( mn.equals( ReleaseCommandMeta.METHOD_SCOPEADD ) )
				blotter.runReleaseAction( action , releaseNew , ReleaseOperation.MODIFY , "extend scope of release " + releaseNew.RELEASEVER );
			else
			if( mn.equals( ReleaseCommandMeta.METHOD_SCOPESPEC ) )
				blotter.runReleaseAction( action , releaseNew , ReleaseOperation.MODIFY , "change scope attributes of release " + releaseNew.RELEASEVER );
			else
			if( mn.equals( ReleaseCommandMeta.METHOD_SCOPEITEMS ) )
				blotter.runReleaseAction( action , releaseNew , ReleaseOperation.MODIFY , "change scope of release " + releaseNew.RELEASEVER );
			else
			if( mn.equals( ReleaseCommandMeta.METHOD_SCOPEDB ) )
				blotter.runReleaseAction( action , releaseNew , ReleaseOperation.MODIFY , "change database scope of release " + releaseNew.RELEASEVER );
			else
			if( mn.equals( ReleaseCommandMeta.METHOD_SCOPECONF ) )
				blotter.runReleaseAction( action , releaseNew , ReleaseOperation.MODIFY , "change configuration scope of release " + releaseNew.RELEASEVER );
			else
			if( mn.equals( ReleaseCommandMeta.METHOD_BUILD ) )
				blotter.runReleaseAction( action , releaseNew , ReleaseOperation.BUILD , "build release " + releaseNew.RELEASEVER );
			else
			if( mn.equals( ReleaseCommandMeta.METHOD_GETDIST ) )
				blotter.runReleaseAction( action , releaseOld , ReleaseOperation.PUT , "change distributive of release " + releaseOld.RELEASEVER );
			else
			if( mn.equals( ReleaseCommandMeta.METHOD_DESCOPE ) )
				blotter.runReleaseAction( action , releaseNew , ReleaseOperation.MODIFY , "descope items from release " + releaseNew.RELEASEVER );
			else
			if( mn.equals( ReleaseCommandMeta.METHOD_SCHEDULE ) )
				blotter.runReleaseAction( action , releaseNew , ReleaseOperation.MODIFY , "change schedule of release " + releaseNew.RELEASEVER );
			else
			if( mn.equals( ReleaseCommandMeta.METHOD_SCOPESET ) )
				blotter.runReleaseAction( action , releaseNew , ReleaseOperation.MODIFY , "set scope from release " + releaseNew.RELEASEVER );
			else
			if( mn.equals( ReleaseCommandMeta.METHOD_TICKETS ) )
				blotter.runReleaseAction( action , releaseNew , ReleaseOperation.MODIFY , "declare changes of release " + releaseNew.RELEASEVER );
		}
	}
	
}
