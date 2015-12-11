package ru.egov.urm.messenger.skype;

import ru.egov.urm.messenger.skype.wrapper.AppKeyPairMgr;
import ru.egov.urm.messenger.skype.wrapper.MySession;

public class SkypeMain {

    public static final String MY_CLASS_TAG = "skype-messenger.sh";
    public static final int ACCOUNT_NAME_IDX = 0;
    public static final int ACCOUNT_PWORD_IDX = 1;
    public static final int REQ_ARG_CNT = 2;
    public static final int OPT_ARG_CNT = 1;
    public static final int APP_KEY_PAIR_IDX = ((REQ_ARG_CNT + OPT_ARG_CNT) - 1);

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
	public static void main(String[] args) {
		if (args.length < REQ_ARG_CNT) {
			MySession.myConsole.printf("Usage is %s accountName accountPassword [appKeyPairPathname]%n%n", MY_CLASS_TAG);
			return;
		}
		if (args.length > (REQ_ARG_CNT + OPT_ARG_CNT)) {
			MySession.myConsole.printf("%s: Ignoring %d extraneous arguments.%n", MY_CLASS_TAG, (args.length - REQ_ARG_CNT));
		}

		// Ensure our certificate file name and contents are valid
		if (args.length > REQ_ARG_CNT) {
			// AppKeyPairMgrmethods will issue all appropriate status and/or error messages!
			if ((!myAppKeyPairMgr.resolveAppKeyPairPath(args[APP_KEY_PAIR_IDX])) ||
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
							MY_CLASS_TAG, args[ACCOUNT_NAME_IDX]);
		mySession.doCreateSession(MY_CLASS_TAG, args[ACCOUNT_NAME_IDX], myAppKeyPairMgr.getPemFilePathname());

		MySession.myConsole.printf("%s: main - Logging in w/ password %s%n",
				MY_CLASS_TAG, args[ACCOUNT_PWORD_IDX]);
		if (mySession.mySignInMgr.Login(MY_CLASS_TAG, mySession, args[ACCOUNT_PWORD_IDX])) {
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
