package org.urm.messenger;

public interface ChatConversation {

	abstract public String getChatFileName();
	abstract public void PostText( String text );
}
