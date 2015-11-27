package ru.egov.urm.messenger;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class ChatAgent {
	
	String fileName;
	FileOutputStream streamSent;
	ChatConversation conversation;
	
	String fileRead;
	String fileWrite;
	
	public ChatAgent( ChatConversation conversation ) {
		this.conversation = conversation;
		fileName = conversation.getChatFileName();

		fileRead = "send/" + fileName;
		fileWrite = "sent/" + fileName;

		System.out.println( "## start chat support - reading file=" + fileRead + ", writing file=" + fileWrite );
		
		try {
			File fSent = new File( fileWrite );
			streamSent = new FileOutputStream( fSent );
		}
		catch( Throwable e ) {
			e.printStackTrace();
		}
	}
	
	public String getFileName() {
		return( fileName );
	}
	
	public void postMessages() throws Exception {
		if( streamSent == null )
			return;
		
		// rename to make safe processing
		File f = new File( fileRead );
		if( !f.exists() ) {
			// nothing placed to chat
			return;
		}
		
		// delete old if any
		File fTo = new File( fileRead + ".copy" );
		if( fTo.exists() ) {
			if( !fTo.delete() ) {
				throw new RuntimeException( "postMessages: unable to delete file " + fTo.getAbsolutePath() );
			}
		}
		
		// do rename
		if( !f.renameTo( fTo ) ) {
			throw new RuntimeException( "postMessages: unable to rename file " + f.getAbsolutePath() + " to " + fTo.getAbsolutePath() );
		}

		// open streams
		FileInputStream streamIn = new FileInputStream( fTo );
		DataInputStream in = new DataInputStream( streamIn );
		BufferedReader br = new BufferedReader( new InputStreamReader( in ) );

		// Read File Line By Line
		String strLine;
		while ((strLine = br.readLine()) != null) {
			// post message
			conversation.PostText( strLine );
			
			// add to sent
			strLine += "\n";
			streamSent.write( strLine.getBytes() );
		}
		
		// close streams
		in.close();
		streamSent.flush();
		
		// delete temporary file
		fTo.delete();
	}
}
