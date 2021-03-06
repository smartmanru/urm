package org.urm.action.codebase;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.RemoteFolder;
import org.urm.meta.product.MetaSourceProjectItem;

public class ActionPatch extends ActionBase {

	public Builder builder;
	public LocalFolder logDir;
	public String logFile;
	ShellExecutor localShell;
	
	public ActionPatch( ActionBase action , String stream , Builder builder , LocalFolder logDir , String logFile , ShellExecutor localShell ) {
		super( action , stream , "build project=" + builder.project.NAME + ", mode=" + Common.getEnumLower( action.context.buildMode ) );
		this.builder = builder;
		this.logDir = logDir;
		this.logFile = logFile;
		this.localShell = localShell;
	}

	@Override 
	protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		builder.createShell( this );
		if( !executePatch() )
			return( SCOPESTATE.RunFail );
		
		if( !executeGetArtefacts() )
			return( SCOPESTATE.RunFail );
		
		// remove directory if build was successful
		if( !context.CTX_SHOWALL ) {
			debug( "patch: remove exported code" );
			builder.removeExportedCode( this );
		}

		info( "patch: build successfully done" );
		return( SCOPESTATE.RunSuccess );
	}
	
	private boolean executePatch() throws Exception {
		// checkout sources
		if( !builder.exportCode( this ) ) {
			error( "patch: checkout failed" );
			return( false );
		}

		// execute source preprocessing
		if( !builder.prepareSource( this ) ) {
			error( "patch: prepare source failed" );
			return( false );
		}

		// check source code
		if( context.CTX_CHECK ) {
			if( !builder.checkSourceCode( this ) ) {
				error( "patch: source code invalid (" + builder.storage.buildFolder.folderPath + ". Exiting" );
				return( false );
			}
		}

		// build
		if( !builder.runBuild( this ) ) {
			error( "patch: build failed" );
			return( false );
		}

		return( true );
	}
	
	private boolean executeGetArtefacts() throws Exception {
		for( MetaSourceProjectItem item : builder.project.getItems() ) {
			if( builder.builder.isTargetLocal() || item.isTargetLocal() ) {
				if( !executeGetProjectArtefact( item ) )
					return( false );
			}
		}
		return( true );
	}

	private boolean executeGetProjectArtefact( MetaSourceProjectItem item ) throws Exception {
		LocalFolder downloadFolder = artefactory.getArtefactFolder( this , item.meta );
		RemoteFolder codeFolder = builder.CODEPATH;
		RemoteFolder targetFolder = codeFolder;
		if( item.project.isBuildable() )
			targetFolder = codeFolder.getSubFolder( this , builder.builder.TARGET_PATH );
		
		if( item.isSourceDirectory() ) {
			RemoteFolder targetDirFolder = targetFolder.getSubFolder( this , item.PATH );
			LocalFolder downloadDirFolder = downloadFolder.getSubFolder( this , item.BASENAME );
			if( !shell.checkDirExists( this , targetDirFolder.folderPath ) ) {
				String dir = shell.getLocalPath( targetDirFolder.folderPath );
				super.fail1( _Error.MissingProjectItemDirectory1 , "Missing project item directory: " + dir , dir );
				return( false );
			}
			
			shell.recreateDir( this , downloadDirFolder.folderPath );
			shell.copyDirContent( this , targetDirFolder.folderPath , downloadDirFolder.folderPath );
			return( true );
		}
		
		if( item.isSourceBasic() || item.isSourcePackage() ) {
			RemoteFolder targetDirFolder = targetFolder.getSubFolder( this , item.PATH );
			return( copyFile( targetDirFolder , downloadFolder , item.BASENAME , item.EXT ) );
		}
			
		if( item.isSourceStaticWar() ) {
			RemoteFolder targetDirFolder = targetFolder.getSubFolder( this , item.PATH );
			if( !copyFile( targetDirFolder , downloadFolder , item.BASENAME , item.EXT ) )
				return( false );
			if( !copyFile( targetDirFolder , downloadFolder , item.BASENAME , item.STATICEXT ) )
				return( false );
			return( true );
		}

		super.exitUnexpectedState();
		return( false );
	}

	private boolean copyFile( RemoteFolder codeDirFolder , LocalFolder downloadFolder , String basename , String ext ) throws Exception {
		String srcname = shell.findVersionedFile( this , codeDirFolder.folderPath , basename , ext );
		String srcfile = basename + ext;
		if( srcname.isEmpty() ) {
			super.fail2( _Error.MissingProjectItemFile2 , "Missing project item file=" + srcfile + ", dir=" + codeDirFolder.folderPath , srcfile , codeDirFolder.folderPath );
			return( false );
		}
		
		shell.copyFile( this , codeDirFolder.getFilePath( this , srcname ) , downloadFolder.getFilePath( this , srcname ) );
		return( true );
	}
	
}
