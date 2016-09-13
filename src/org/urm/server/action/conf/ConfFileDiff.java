package org.urm.server.action.conf;

import java.io.BufferedWriter;
import java.util.LinkedList;
import java.util.List;

import org.urm.server.action.ActionBase;
import org.urm.server.storage.Folder;

public class ConfFileDiff {

	public String compFile;
	public Folder relFolder;
	public Folder prodFolder;
	
	public String relFile;
	public String prodFile;

	boolean binaryDiff;
	boolean extraSpacingLines;
	boolean extraLineSpaces;
	
	List<String> rel; 
	List<String> prod;
	
	List<ConfFileDiffItem> diffs;
	
	int nextRelLine;
	int nextProdLine;
	int lastRelLine;
	int lastProdLine;

	public ConfFileDiff( String compFile , Folder relFolder , Folder prodFolder ) {
		this.compFile = compFile;
		this.relFolder = relFolder;
		this.prodFolder = prodFolder;
	}
	
	public boolean getDiff( ActionBase action ) throws Exception {
		this.relFile = relFolder.getFilePath( action , compFile );
		this.prodFile = prodFolder.getFilePath( action , compFile );
		
		diffs = new LinkedList<ConfFileDiffItem>(); 
		
		binaryDiff = false;
		extraSpacingLines = false;
		extraLineSpaces = false;

		if( action.meta.isConfigurableFile( action , relFile ) )
			return( getConfigurableDiff( action ) );

		return( getBinaryDiff( action ) );
	}
		
	private boolean getConfigurableDiff( ActionBase action ) throws Exception {
		rel = action.readFileLines( relFile ); 
		prod = action.readFileLines( prodFile );
		
		nextRelLine = 0;
		nextProdLine = 0;
		lastRelLine = rel.size() - 1;
		lastProdLine = prod.size() - 1;
		
		while( true ) {
			skipSame( action );
			
			if( skipSpaces( action ) ) {
				extraSpacingLines = true;
				continue;
			}
			
			if( !getDiffItem( action ) )
				break;
		}
		
		if( diffs.isEmpty() == false || extraSpacingLines || extraLineSpaces )
			return( true );
		
		return( false );
	}

	private boolean getBinaryDiff( ActionBase action ) throws Exception {
		String relMD5 = action.shell.getMD5( action , relFile );
		String prodMD5 = action.shell.getMD5( action , prodFile );
		if( relMD5.equals( prodMD5 ) )
			return( false );
		
		binaryDiff = true;
		return( true );
	}
	
	private boolean skipSpaces( ActionBase action ) throws Exception {
		int nRel = getSpacingLines( action , rel , nextRelLine , lastRelLine );
		int nProd = getSpacingLines( action , prod , nextProdLine , lastProdLine );
		nextRelLine += nRel;
		nextProdLine += nProd;
		if( nRel == 0 && nProd == 0 )
			return( false );
		
		return( true );
	}

	private int getSpacingLines( ActionBase action , List<String> lines , int next , int last ) throws Exception {
		int n = 0;
		for( int k = next; k <= last; k++ ) {
			String s = lines.get( k );
			if( !s.trim().isEmpty() )
				break;
			
			n++;
		}
		
		return( n );
	}

	private void skipSame( ActionBase action ) throws Exception {
		while( nextRelLine <= lastRelLine && nextProdLine <= lastProdLine ) {
			String sRel = rel.get( nextRelLine );
			String sProd = prod.get( nextProdLine );
			if( sRel.equals( sProd ) ) {
				nextRelLine++;
				nextProdLine++;
				continue;
			}

			if( sRel.trim().equals( sProd.trim() ) ) {
				extraLineSpaces = true;
				nextRelLine++;
				nextProdLine++;
				continue;
			}

			break;
		}
	}

	private boolean getDiffItem( ActionBase action ) throws Exception {
		if( nextRelLine > lastRelLine && nextProdLine > lastProdLine )
			return( false );

		// try match release to prod
		int oneLastRel = -1;
		int oneLastProd = -1;
		for( int k = nextRelLine; k <= lastRelLine; k++ ) {
			String s = rel.get( k );
			
			// do not match spacing lines
			if( s.trim().isEmpty() )
				continue;
			
			int matched = getMatchedProd( action , s );
			if( matched >= 0 ) {
				oneLastRel = k - 1;
				oneLastProd = matched - 1;
				break;
			}
		}

		// try match prod to release
		if( oneLastRel >= 0 || oneLastProd >= 0 ) {
			int twoLastRel = -1;
			int twoLastProd = -1;
			
			for( int k = nextProdLine; k <= lastProdLine; k++ ) {
				String s = prod.get( k );
				
				// do not match spacing lines
				if( s.trim().isEmpty() )
					continue;
				
				int matched = getMatchedRel( action , s );
				if( matched >= 0 ) {
					twoLastProd = k - 1;
					twoLastRel = matched - 1;
					break;
				}
			}
			
			// choose best
			if( twoLastRel > 0 || twoLastProd > 0 ) {
				int oneLength = ( oneLastRel - nextRelLine + 1 ) + ( oneLastProd - nextProdLine + 1 );
				int twoLength = ( twoLastRel - nextRelLine + 1 ) + ( twoLastProd - nextProdLine + 1 );
				if( twoLength < oneLength ) {
					addDiffItem( action , nextRelLine , twoLastRel , nextProdLine , twoLastProd );
					return( true );
				}
			}
			
			addDiffItem( action , nextRelLine , oneLastRel , nextProdLine , oneLastProd );
			return( true );
		}
		
		addDiffItem( action , nextRelLine , lastRelLine , nextProdLine , lastProdLine );
		return( true );
	}

	private int getMatchedProd( ActionBase action , String s ) throws Exception {
		String st = s.trim();
		for( int k = nextProdLine; k <= lastProdLine; k++ ) {
			String v = prod.get( k );
			if( s.equals( v ) )
				return( k );
			
			if( st.equals( v.trim() ) )
				return( k );
		}
		
		return( -1 );
	}

	private int getMatchedRel( ActionBase action , String s ) throws Exception {
		String st = s.trim();
		for( int k = nextRelLine; k <= lastRelLine; k++ ) {
			String v = rel.get( k );
			if( s.equals( v ) )
				return( k );
			
			if( st.equals( v.trim() ) )
				return( k );
		}
		
		return( -1 );
	}

	private void addDiffItem( ActionBase action , int relFrom , int relTo , int prodFrom , int prodTo ) throws Exception {
		if( relFrom > relTo && prodFrom > prodTo )
			action.exit4( _Error.InvalidDiff4 , "invalid diff: relFrom=" + relFrom + ", relTo=" + relTo + ", prodFrom=" + prodFrom + ", prodTo=" + prodTo , "" + relFrom , "" + relTo , "" + prodFrom , "" + prodTo );
		
		if( relTo >= relFrom )
			nextRelLine = relTo + 1; 
		if( prodTo >= prodFrom )
			nextProdLine = prodTo + 1; 

		// remove tailing spacings if any
		while( relTo >= relFrom ) {
			if( !rel.get( relTo ).trim().isEmpty() )
				break;
			
			relTo--;
		}

		while( prodTo >= prodFrom ) {
			if( !prod.get( prodTo ).trim().isEmpty() )
				break;
			
			prodTo--;
		}
		
		ConfFileDiffItem diff = new ConfFileDiffItem( relFrom , relTo , prodFrom , prodTo );
		diffs.add( diff );
	}

	public void write( ActionBase action , String prefix , BufferedWriter writer ) throws Exception {
		if( binaryDiff ) {
			writer.write( prefix + compFile + ": binary modifications\n" );
			return;
		}
		
		writer.write( prefix + compFile + ":\n" );
		writer.write( "-------------------------\n" );

		if( extraSpacingLines )
			writer.write( "extra spacing lines\n" );
		if( extraLineSpaces )
			writer.write( "extra spaces in lines\n" );
		
		for( ConfFileDiffItem item : diffs )
			write( action , writer , item );
		
		writer.write( "-------------------------\n" );
		writer.newLine();
	}

	public void write( ActionBase action , BufferedWriter writer , ConfFileDiffItem diff ) throws Exception {
		writer.newLine();
		String relInfo;
		String prodInfo;
		boolean relEmpty = false;
		boolean prodEmpty = false;
		
		if( diff.relFrom <= diff.relTo )
			relInfo = "lines " + (diff.relFrom+1) + "-" + (diff.relTo+1);
		else {
			relInfo = "empty";
			relEmpty = true;
		}

		if( diff.prodFrom <= diff.prodTo )
			prodInfo = "lines " + (diff.prodFrom+1) + "-" + (diff.prodTo+1);
		else {
			prodInfo = "empty";
			prodEmpty = true;
		}
		
		writer.write( "release (" + relInfo + ") / prod (" + prodInfo + ")\n" );
		if( !relEmpty ) {
			writer.write( ">> release:\n" );
			write( action , writer , rel , diff.relFrom , diff.relTo );
		}

		if( !prodEmpty ) {
			writer.write( "<< prod:\n" );
			write( action , writer , prod , diff.prodFrom , diff.prodTo );
		}
	}

	public void write( ActionBase action , BufferedWriter writer , List<String> lines , int from , int to ) throws Exception {
		for( int k = from; k <= to; k++ )
			writer.write( lines.get( k ) + "\n" );
	}
	
}
