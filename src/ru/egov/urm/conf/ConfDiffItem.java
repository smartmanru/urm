package ru.egov.urm.conf;

import java.io.BufferedWriter;

import ru.egov.urm.action.ActionBase;

public class ConfDiffItem {

	enum DIFFTYPE {
		DIFF_NEWDIR ,
		DIFF_OLDDIR ,
		DIFF_NEWFILE ,
		DIFF_OLDFILE ,
		DIFF_UPDFILE
	};
	
	DIFFTYPE type;
	String comment;
	String item;
	ConfFileDiff fileDiff;
	
	private ConfDiffItem( DIFFTYPE type , String comment , String item ) {
		this.type = type;
		this.comment = comment;
		this.item = item;
	}
	
	private ConfDiffItem( DIFFTYPE type , String comment , String item , ConfFileDiff fileDiff ) {
		this.type = type;
		this.comment = comment;
		this.item = item;
		this.fileDiff = fileDiff;
	}
	
	public static ConfDiffItem createNewDirDiff( String dir ) {
		return( new ConfDiffItem( DIFFTYPE.DIFF_NEWDIR , "new directory=" + dir , dir ) );
	}
	
	public static ConfDiffItem createOldDirDiff( String dir ) {
		return( new ConfDiffItem( DIFFTYPE.DIFF_OLDDIR , "missing directory=" + dir , dir ) );
	}

	public static ConfDiffItem createNewFileDiff( String file ) {
		return( new ConfDiffItem( DIFFTYPE.DIFF_NEWFILE , "new file=" + file , file ) );
	}
	
	public static ConfDiffItem createOldFileDiff( String file ) {
		return( new ConfDiffItem( DIFFTYPE.DIFF_OLDFILE , "missing file=" + file , file ) );
	}
	
	public static ConfDiffItem createFileChangeDiff( String file , ConfFileDiff fileDiff ) {
		return( new ConfDiffItem( DIFFTYPE.DIFF_UPDFILE , "missing file=" + file , file , fileDiff ) );
	}
	
	public void write( ActionBase action , int nPos , BufferedWriter writer ) throws Exception {
		String prefix = "#" + (nPos+1) + " - ";
		if( type == DIFFTYPE.DIFF_NEWDIR ) {
			writer.write( prefix + item + ": TOBE directory is missing in PROD\n" );
			return;
		}
			
		if( type == DIFFTYPE.DIFF_OLDDIR ) {
			writer.write( prefix + item + ": PROD directory is missing in TOBE set\n" );
			return;
		}
		
		if( type == DIFFTYPE.DIFF_NEWFILE ) {
			writer.write( prefix + item + ": TOBE file is missing in PROD\n" );
			return;
		}
			
		if( type == DIFFTYPE.DIFF_OLDFILE ) {
			writer.write( prefix + item + ": PROD file is missing in TOBE set\n" );
			return;
		}
		
		if( type == DIFFTYPE.DIFF_UPDFILE ) {
			fileDiff.write( action , prefix , writer );
			return;
		}
	}
	
}
