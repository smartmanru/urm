package org.urm.messenger.skype;

import java.util.Properties;

import org.urm.messenger.skype.wrapper.AppKeyPairMgr;
import org.urm.messenger.skype.wrapper.MySession;

public class SkypeMain {

    public static final String MY_CLASS_TAG = "SkypeMain";
    private static AppKeyPairMgr myAppKeyPairMgr = new AppKeyPairMgr();
    private static MySession mySession = new MySession();
    
	/**
	 * Main loop
	 * 
	 * @param args
	 * <ol>
	 *   <li>name of the target Skype account</li>
	 *   <li>password for the target Skype account</li>
     *   <li>optional pathname of an AppKeyPair PEM file</li>
	 * </ol>
	 * 
	 * @since 1.0
	 */
	public void execute( Properties props ) throws Exception {
		String accountName = props.getProperty( "account" );
		String accountPassword = props.getProperty( "password" );
		String appKeyPairPathname = props.getProperty( "keypair" );
		
		if (accountPassword.isEmpty()) {
			MySession.myConsole.printf("Usage is %s accountName accountPassword [appKeyPairPathname]%n%n", MY_CLASS_TAG);
			return;
		}
		if (appKeyPairPathname.isEmpty()) {
			MySession.myConsole.printf("%s: Ignoring extraneous arguments.%n", MY_CLASS_TAG);
		}
		
		execute( accountName , accountPassword , appKeyPairPathname );
	}
	
	private void execute( String accountName , String accountPassword , String appKeyPairPathname ) {
		// Ensure our certificate file name and contents are valid
		if (!appKeyPairPathname.isEmpty()) {
			// AppKeyPairMgrmethods will issue all appropriate status and/or error messages!
			if ((!myAppKeyPairMgr.resolveAppKeyPairPath(appKeyPairPathname)) ||
				(!myAppKeyPairMgr.isValidCertificate())) {
				return;
			}
		}
		else {
			if ((!myAppKeyPairMgr.resolveAppKeyPairPath()) ||
				(!myAppKeyPairMgr.isValidCertificate())) {
				return;
			}
		}

		MySession.myConsole.printf("%s: main - Creating session - Account = %s%n",
							MY_CLASS_TAG, accountName);
		mySession.doCreateSession(MY_CLASS_TAG, accountName, myAppKeyPairMgr.getPemFilePathname());

		MySession.myConsole.printf("%s: main - Logging in w/ password %s%n",
				MY_CLASS_TAG, accountPassword);
		if (mySession.mySignInMgr.Login(MY_CLASS_TAG, mySession, accountPassword)) {
			try {
				MySession.myConsole.printf("%s: main - obtain conference list...%n", MY_CLASS_TAG);
				SkypeMain main = new SkypeMain();
				main.execute( mySession );
			}
			catch( Throwable e ) {
				MySession.myConsole.printf("%s: main - ERROR in execution%n", MY_CLASS_TAG);
				e.printStackTrace();
			}
			
			MySession.myConsole.printf("%s: main - Logging out...%n", MY_CLASS_TAG);
			mySession.mySignInMgr.Logout(MY_CLASS_TAG, mySession);
		}
		// SkypeKitListeners, SignInMgr, and MySession will have logged/written
		// all appropriate diagnostics if login is not successful

		MySession.myConsole.printf("%s: Cleaning up...%n", MY_CLASS_TAG);
		if (mySession != null) {
			mySession.doTearDownSession();
		}
		MySession.myConsole.printf("%s: Done!%n", MY_CLASS_TAG);
	}

	private void execute( MySession session ) throws Exception {
		// main loop
		// get chat rooms
		SkypeChatSet chatSet = new SkypeChatSet( session );
		chatSet.setUpdateOncePerMinute();
		
		// infinite loop
        synchronized (this) {
        	wait(0);
        }
	}
}
