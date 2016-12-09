package org.urm.common.jmx;

import java.util.Collections;

import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;

import org.urm.engine.ServerEngine;
import org.urm.meta.engine.ServerAuth;

public class ServerMBeanAuth implements JMXAuthenticator {

	ServerMBean server;
	
	public ServerMBeanAuth( ServerMBean server ) {
		this.server = server;
	}

	@Override
	public Subject authenticate( Object credentials ) {

        // Verify that credentials is of type String[].
        if (!(credentials instanceof String[])) {
            // Special case for null so we get a more informative message
            if (credentials == null) {
                throw new SecurityException("Credentials required");
            }
            throw new SecurityException("Credentials should be String[]");
        }

        // Verify that the array contains 2 elements (username/password).
        final String[] aCredentials = (String[]) credentials;
        if (aCredentials.length != 3) {
            throw new SecurityException("Credentials should have s elements");
        }

        // Perform authentication
        String username = (String) aCredentials[0];
        String password = (String) aCredentials[1];

        // perform authentication based on the (username/password) ...
        ServerEngine engine = server.engine;
        ServerAuth auth = engine.getAuth();
        
        if( auth.checkLogin( username , password ) ) {
            return new Subject( true , Collections.singleton( new JMXPrincipal( username ) ) , Collections.EMPTY_SET , Collections.EMPTY_SET );
        } else {
            throw new SecurityException("Invalid credentials");
        }
    }
	
}
