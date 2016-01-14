package ru.egov.urm.run.deploy;

import ru.egov.urm.Common;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeSet;
import ru.egov.urm.shell.Account;

public class ActionChangeKeys extends ActionBase {

	String cmd;

	String S_AUTHFILE = ".ssh/authorized_keys";
	
	public ActionChangeKeys( ActionBase action , String stream , String cmd ) {
		super( action , stream );
		
		this.cmd = cmd;
	}

	@Override protected boolean executeAccount( ActionScopeSet set , Account account ) throws Exception {
		String F_NEWKEY = meta.env.KEYNAME;
		String F_OLDKEY = F_NEWKEY;

		if( !options.OPT_NEWKEY.isEmpty() )
			F_NEWKEY = options.OPT_NEWKEY;
		if( !context.KEYNAME.isEmpty() )
			F_OLDKEY = context.KEYNAME;
		
		String P_KEYFILENEXT = F_NEWKEY;
		String P_KEYACCESS = F_OLDKEY;

		if( P_KEYFILENEXT.isEmpty() )
			P_KEYFILENEXT = "~/.ssh/id_dsa";
	
		// replace with public key data
		String P_KEYFILENEXTPUB;
		String P_KEYFILENEXTPRV; 
		if( !P_KEYFILENEXT.endsWith( ".pub" ) ) {
			P_KEYFILENEXTPUB = P_KEYFILENEXT + ".pub";
			P_KEYFILENEXTPRV = P_KEYFILENEXT;
		}
		else {
			P_KEYFILENEXTPUB = P_KEYFILENEXT;
			P_KEYFILENEXTPRV = Common.getPartBeforeLast( P_KEYACCESS , ".pub" );
		}

		boolean S_HASNEXTPRIVATEKEY = false;
		String F_KEYPUB = null;
		String F_KEYOWNER = null;
		String F_KEYDATA = null;
		if( !cmd.equals( "list" ) ) {
			if( !session.checkFileExists( this , P_KEYFILENEXTPUB ) )
				exitAction( "cannot find public key file " + P_KEYFILENEXTPUB );
	
			if( session.checkFileExists( this , P_KEYFILENEXTPRV ) )
				S_HASNEXTPRIVATEKEY = true;
			
			F_KEYPUB = session.getFileContentAsString( this , P_KEYFILENEXTPUB );
			F_KEYOWNER = Common.getListItem( F_KEYPUB , " " , 2 );
			F_KEYDATA = session.getFileContentAsString( this , P_KEYFILENEXTPUB );
		}
		
		// access using private key
		String F_ACCESSOPTION = "";
		String F_ACCESSMSG = "";
		if( !P_KEYACCESS.isEmpty() ) {
			if( P_KEYACCESS.endsWith( ".pub" ) )
				P_KEYACCESS = Common.getPartBeforeLast( P_KEYACCESS , ".pub" );
		
			if( !session.checkFileExists( this , P_KEYACCESS ) )
				exitAction( "invalid private key file " + P_KEYACCESS );

			F_ACCESSOPTION = "-i " + P_KEYACCESS;
			F_ACCESSMSG = " using access key " + P_KEYACCESS;
		}

		// check new key is already placed and access using old key is not avalable
		String F_HOSTUSER = account.USER;
		Account F_TARGETACCOUNT = account;
		if( F_HOSTUSER.equals( "root" ) == false && options.OPT_SUDO )
			F_TARGETACCOUNT = account.getRootAccount( this );
			
		if( cmd.equals( "delete" ) == false && cmd.equals( "list" ) == false ) {
			if( !tryConnect( F_TARGETACCOUNT , F_ACCESSOPTION ) ) {
				if( S_HASNEXTPRIVATEKEY ) {
					String tryOption = "-i " + P_KEYFILENEXTPRV;
					if( tryConnect( F_TARGETACCOUNT , tryOption ) ) {
						F_ACCESSOPTION = tryOption;
						F_ACCESSMSG = " using access key " + P_KEYFILENEXTPRV;
					}
				}
			}
		}

		String F_SETUPAUTH;
		Account F_BEHALFACCOUNT = account;
		if( F_HOSTUSER.equals( "root" ) == false && options.OPT_ROOTUSER ) {
			F_BEHALFACCOUNT = account.getRootAccount( this );

			if( !checkHostUser( F_BEHALFACCOUNT , F_ACCESSOPTION , F_HOSTUSER ) )
				exitAction( "unknown hostuser=" + F_HOSTUSER );

			// execute key operation under user on behalf of host user
			F_SETUPAUTH = getCreateSshOnBehalf( F_HOSTUSER ); 
		}
		else
		if( F_HOSTUSER.equals( "root" ) == false && options.OPT_SUDO ) {
			F_SETUPAUTH = getCreateSshOnSudo( F_HOSTUSER ); 
		}
		else
			F_SETUPAUTH = getCreateSshOwn(); 

		if( cmd.equals( "change" ) || cmd.equals( "add" ) ) {
			log( F_BEHALFACCOUNT + ": change key to " + P_KEYFILENEXTPUB + " (" + F_KEYOWNER + 
				") on " + account.HOSTLOGIN + F_ACCESSMSG + " ..." );
			if( !replaceKey( F_BEHALFACCOUNT , F_ACCESSOPTION , F_SETUPAUTH , F_KEYOWNER , F_KEYDATA ) )
				exitAction( "error executing key replacement" );
		}
		else
		if( cmd.equals( "set" ) ) {
			log( F_BEHALFACCOUNT.HOSTLOGIN + ": set the only key to " + P_KEYFILENEXTPUB + " (" + F_KEYOWNER + 
					") on " + account.HOSTLOGIN + F_ACCESSMSG + " ..." );
			if( !setOnlyKey( F_BEHALFACCOUNT , F_ACCESSOPTION , F_SETUPAUTH , F_KEYDATA ) )
				exitAction( "error executing key set. Exiting" );
		}
		else
		if( cmd.equals( "delete" ) ) {
			log( F_BEHALFACCOUNT + ": delete key " + P_KEYFILENEXTPUB + " (" + F_KEYOWNER + ") on " + 
					account.HOSTLOGIN + F_ACCESSMSG + " ..." );
			if( !deleteKey( F_BEHALFACCOUNT , F_ACCESSOPTION , F_SETUPAUTH , F_KEYOWNER ) )
				exitAction( "error executing key delete" );
		}
		else
		if( cmd.equals( "list" ) ) {
			if( !listKeys( F_BEHALFACCOUNT , F_ACCESSOPTION , F_SETUPAUTH ) )
				exitAction( "error executing key list" );
		}

		if( cmd.equals( "change" ) || cmd.equals( "set" ) ) {
			// check - if there is next key
			if( S_HASNEXTPRIVATEKEY ) {
				if( !tryConnect( F_TARGETACCOUNT , "-i " + P_KEYFILENEXTPRV ) )
					exitAction( "error executing new key check. Exiting" );
				
				log( account.HOSTLOGIN + ": new key successfully verified." );
			}
		}
		
		return( true );
	}

	private boolean tryConnect( Account account , String ACCESSOPTION ) throws Exception {
		String F_CHECK = session.customGetValueNoCheck( this , "ssh -n " + ACCESSOPTION + 
				" -o PasswordAuthentication=no " + account.HOSTLOGIN + " " + Common.getQuoted( "echo ok" ) );
		if( F_CHECK.equals( "ok" ) )
			return( true );
		return( false );
	}

	private boolean checkHostUser( Account account , String ACCESSOPTION , String user ) throws Exception {
		int status = session.customGetStatus( this , "ssh -n " + ACCESSOPTION + " " + account.HOSTLOGIN + " " +
				Common.getQuoted( "cd ~" + user ) );
		if( status != 0 )
			return( false );
		return( true );
	}

	private String getCreateSshOnBehalf( String HOSTUSER ) throws Exception {
		if( cmd.equals( "list" ) ) {
			return( "cd ~" + HOSTUSER + "; if [ ! -f " + S_AUTHFILE + 
					" ]; then echo NOAUTHFILE; else cat " + S_AUTHFILE + " | cut -d \" \" -f3 | grep -v ^$; fi" );
		}
		
		return( "cd ~" + HOSTUSER + "; if [ ! -f " + S_AUTHFILE + 
				" ]; then mkdir -p .ssh; chmod 700 .ssh; echo \"\" > " + S_AUTHFILE + 
				"; chmod 600 " + S_AUTHFILE + "; chown " + HOSTUSER + ": .ssh " + S_AUTHFILE + "; fi" );
	}
	
	private String getCreateSshOnSudo( String HOSTUSER ) throws Exception {
		if( cmd.equals( "list" ) ) {
			return( "if sudo bash -c '[[ ! -f ~root/" + S_AUTHFILE + 
					" ]]'; then echo NOAUTHFILE; else sudo cat ~root/" + S_AUTHFILE + " | cut -d \" \" -f3 | grep -v ^$; fi" );
		}
		
		return( "if sudo bash -c '[[ ! -f ~root/" + S_AUTHFILE + 
				" ]]'; then sudo mkdir -p ~root/.ssh; sudo chmod 700 ~root/.ssh; sudo touch ~root/" + S_AUTHFILE + 
				"; sudo chmod 600 ~root/" + S_AUTHFILE + "; fi" );
	}
	
	private String getCreateSshOwn() throws Exception {
		if( cmd.equals( "list" ) ) {
			return( "if [ ! -f " + S_AUTHFILE +   
					" ]; then echo NOAUTHFILE; else cat " + S_AUTHFILE + " | cut -d \" \" -f3 | grep -v ^$; fi" );
		}
		
		return( "if [ ! -f " + S_AUTHFILE + " ]; then mkdir -p .ssh; chmod 700 .ssh; echo \"\" > " + 
			S_AUTHFILE + "; chmod 600 " + S_AUTHFILE + "; fi" );
	}

	private boolean replaceKey( Account account , String ACCESSOPTION , String SETUPAUTH , String KEYOWNER , String KEYDATA ) throws Exception {
		if( options.OPT_SUDO )
			exit( "unsupported with sudo" );
		
		session.setTimeoutUnlimited( this );
		int status = session.customGetStatus( this , "ssh -n " + ACCESSOPTION + " " + account.HOSTLOGIN + " " + 
			Common.getQuoted( SETUPAUTH + "; cat " + S_AUTHFILE + 
				" | grep -v " + KEYOWNER + "\\$ > " + S_AUTHFILE + ".2; echo " + Common.getQuoted( KEYDATA ) + 
				" >> " + S_AUTHFILE + ".2; cp " + S_AUTHFILE + ".2 " + S_AUTHFILE + 
				"; rm -rf " + S_AUTHFILE + ".2;" ) );
		if( status != 0 )
			return( false );
		return( true );
	}

	private boolean setOnlyKey( Account account , String ACCESSOPTION , String SETUPAUTH , String KEYDATA ) throws Exception {
		int status;
		
		session.setTimeoutUnlimited( this );
		if( options.OPT_SUDO ) {
			status = session.customGetStatus( this , "ssh -n -t -t " + ACCESSOPTION + " " + account.HOSTLOGIN + " " + 
				Common.getQuoted( SETUPAUTH + "; echo " + Common.getQuoted( KEYDATA ) + " | sudo tee ~root/" + S_AUTHFILE ) );
		}
		else {
			status = session.customGetStatus( this , "ssh -n " + ACCESSOPTION + " " + account.HOSTLOGIN + " " + 
					Common.getQuoted( SETUPAUTH + "; echo " + Common.getQuoted( KEYDATA ) + " > " + S_AUTHFILE ) );
		}
		
		if( status != 0 )
			return( false );
		return( true );
	}

	private boolean deleteKey( Account account , String ACCESSOPTION , String SETUPAUTH , String KEYOWNER ) throws Exception {
		if( options.OPT_SUDO )
			exit( "unsupported with sudo" );
		
		int status = session.customGetStatus( this , "ssh -n " + ACCESSOPTION + " " + account.HOSTLOGIN + " " +
		Common.getQuoted( SETUPAUTH + "; cat " + S_AUTHFILE + " | grep -v " + KEYOWNER + "\\$ > " +
			S_AUTHFILE + ".2; cp " + S_AUTHFILE + ".2 " + S_AUTHFILE + "; rm -rf " + S_AUTHFILE + ".2;" ) );
		if( status != 0 )
			return( false );
		return( true );
	}
	
	private boolean listKeys( Account account , String ACCESSOPTION , String SETUPAUTH ) throws Exception {
		if( options.OPT_SUDO )
			exit( "unsupported with sudo" );
		
		session.setTimeoutUnlimited( this );
		String[] list = session.customGetLines( this , "ssh -n " + ACCESSOPTION + " " + account.HOSTLOGIN + " " +
				Common.getQuoted( SETUPAUTH ) );
		if( list.length > 0 && list[0].equals( "NOAUTHFILE" ) )
			exit( S_AUTHFILE + " is not found" );
		
		for( String s : list )
			super.comment( "\tkey: " + s );
		
		return( true );
	}
	
}
