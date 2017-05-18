package org.urm.action.database;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.storage.FileSet;
import org.urm.meta.product.MetaDatabaseSchema;

public class DatabaseScriptFile {

	public String PREFIX;
	public String PREFIXALIGNED;
	public String PREFIXTYPE;
	public String PREFIXINSTANCE;
	public String REGIONALINDEX;
	public String SRCINDEX;
	public String SRCSCHEMA;
	public String SRCTAIL;
	
	public DatabaseScriptFile() {
	}
	
	public void setDistFile( ActionBase action , String name ) throws Exception {
		String[] parts = Common.splitDashed( name );
		if( parts.length < 5 ) 
			action.exit1( _Error.InvalidScriptName1 , "invalid script name=" + name , name );
		
		PREFIX = parts[0];
		REGIONALINDEX = parts[1];
		SRCINDEX = parts[2];
		SRCSCHEMA = parts[3];
		
		String s = "";
		for( int k = 4; k < parts.length; k++ ) {
			if( k > 4 )
				s += "-";
			s += parts[ k ];
		}
		SRCTAIL = s;
		
		parts = Common.splitDotted( PREFIX );
		PREFIXALIGNED = parts[0];
		PREFIXTYPE = parts[1];
		PREFIXINSTANCE = parts[2];
	}

	public static boolean checkDistHasSchemaFiles( FileSet files , MetaDatabaseSchema schema ) {
		String regexp = "[^-]+-[^-]+-[^-]+" + schema.SCHEMA + "[-.].*";
		if( files.findFileMatched( regexp ) != null )
			return( true );
		return( false );
	}

	public static String[] getDistSchemaFiles( FileSet files , MetaDatabaseSchema schema ) {
		String regexp = "[^-]+-[^-]+-[^-]+" + schema.SCHEMA + "[-.].*";
		return( files.getFilesMatched( regexp ) );
	}
	
	public void setSrcFile( ActionBase action , String name ) throws Exception {
		String[] parts = Common.splitDashed( name );
		if( parts.length < 3 ) 
			action.exit1( _Error.InvalidScriptName1 , "invalid script name=" + name , name );
		
		SRCINDEX = parts[0];
		SRCSCHEMA = parts[1];
		String s = "";
		for( int k = 2; k < parts.length; k++ ) {
			if( k > 2 )
				s += "-";
			s += parts[ k ];
		}
		
		SRCTAIL = s;
	}
	
	public static String getPrefix( String ALIGNED , String TYPE , String INSTANCE ) {
		return( ALIGNED + "." + TYPE + "." + INSTANCE );
	}
	
	public String getDistFile() {
		return( PREFIX + "-" + REGIONALINDEX + "-" + SRCINDEX + "-" + SRCSCHEMA + "-" + SRCTAIL );
	}

	public String getDistKey() {
		return( PREFIX + "-" + REGIONALINDEX + "-" + SRCINDEX );
	}
}
