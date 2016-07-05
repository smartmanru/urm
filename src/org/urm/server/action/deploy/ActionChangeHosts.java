package org.urm.server.action.deploy;

import org.urm.common.Common;
import org.urm.server.action.ActionBase;
import org.urm.server.action.ActionScopeSet;
import org.urm.server.shell.Account;

public class ActionChangeHosts extends ActionBase {

	String cmd;
	String opHost;
	String opAddress;
	
	public ActionChangeHosts( ActionBase action , String stream , String cmd , String opHost , String opAddress ) {
		super( action , stream );
		
		this.cmd = cmd;
		this.opHost = opHost;
		this.opAddress = opAddress;
	}

	@Override protected boolean executeAccount( ActionScopeSet set , Account account ) throws Exception {
		if( cmd.equals( "set" ) )
			executeSet( account );
		
		if( cmd.equals( "delete" ) )
			executeDelete( account );
		
		if( cmd.equals( "check" ) )
			executeCheck( account );
		
		return( true );
	}
	
	private void executeSet( Account account ) throws Exception {
		super.executeCmdLive( account , "cat /etc/hosts | grep -v " + opHost + " | grep -v " + opAddress + 
			" > /etc/hosts.new; echo " + Common.getQuoted( opHost + " " + opAddress ) + 
			" >> /etc/hosts.new; mv /etc/hosts.new /etc/hosts" );
	}

	private void executeDelete( Account account ) throws Exception {
		if( !opAddress.isEmpty() )
			super.executeCmdLive( account , "cat /etc/hosts | grep -v " + opHost + " | grep -v " + opAddress +
				" > /etc/hosts.new; mv /etc/hosts.new /etc/hosts" );
		else
			super.executeCmdLive( account , "cat /etc/hosts | grep -v " + opHost + 
				" > /etc/hosts.new; mv /etc/hosts.new /etc/hosts" );
	}

	private void executeCheck( Account account ) throws Exception {
		if( opAddress.isEmpty() ) {
			String res = super.executeCmdGetValue( account , "cat /etc/hosts | grep " + opHost );
			if( res.isEmpty() ) {
				error( account.getPrintName() + ": missing " + opHost );
				return;
			}
	
			if( res.indexOf( "\n" ) >= 0 ) {
				error( account.getPrintName() + ": duplicate " + opHost + " (" + res + ")" );
				return;
			}
			
			info( account.getPrintName() + ": " + res );
		}	
		else {
			String res = super.executeCmdGetValue( account , "cat /etc/hosts | egrep " + 
				Common.getQuoted( opHost + "|" + opAddress ) );
			if( res.isEmpty() ) {
				info( account.getPrintName() + ": missing " + opHost );
				return;
			}
	
			if( res.indexOf( "\n" ) >= 0 ) {
				error( account.getPrintName() + ": duplicate " + opHost + " (" + res + ")" );
				return;
			}
	
			String F_HOSTADDR = Common.getPartAfterLast( res , " " );
			String F_HOSTNAME = Common.getPartBeforeFirst( res ,  " " );
	
			if( F_HOSTNAME.equals( opHost ) == false || F_HOSTADDR.equals( opAddress ) == false ) {
				error( account.getPrintName() + ": " + res + " - not matched (" + opHost + " " + opAddress + ")" );
				return;
			}
	
			info( account.getPrintName() + ": " + res + " - ok" );
		}
	}

}
