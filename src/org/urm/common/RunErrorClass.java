package org.urm.common;

public class RunErrorClass {

	public static int BaseCommon = 100000;
	public static int BaseCommonAction = 101000;
	public static int BaseCommonJmx = 102000;
	public static int BaseCommonMeta = 103000;
	public static int BaseEngine = 110000;
	public static int BaseEngineAction = 111000;
	public static int BaseEngineCustom = 112000;
	public static int BaseEngineDist = 113000;
	public static int BaseEngineExecutor = 114000;
	public static int BaseEngineShell = 116000;
	public static int BaseEngineStorage = 117000;
	public static int BaseEngineVCS = 118000;
	public static int BaseAction = 200000;
	public static int BaseActionBuild = 201000;
	public static int BaseActionConf = 202000;
	public static int BaseActionDatabase = 203000;
	public static int BaseActionDeploy = 204000;
	public static int BaseActionMain = 205000;
	public static int BaseActionMonitor = 206000;
	public static int BaseActionRelease = 207000;
	public static int BaseActionXDoc = 208000;
	public static int BaseMetaCommon = 250000;
	public static int BaseMetaEngine = 251000;
	public static int BaseMetaProduct = 252000;
	public static int BaseClient = 300000;
	public static int BasePlugin = 800000;
	
	public static int InternalBase = 500;

	public String groupName;
	public int groupValue;
	
	public static RunErrorClass[] errorClasses = { 
		new RunErrorClass( "Common" , 100000 ) ,
		new RunErrorClass( "CommonAction" , 101000 ) ,
		new RunErrorClass( "CommonJmx" , 102000 ) ,
		new RunErrorClass( "CommonMeta" , 103000 ) ,
		new RunErrorClass( "Engine" , 110000 ) ,
		new RunErrorClass( "EngineAction" , 111000 ) ,
		new RunErrorClass( "EngineCustom" , 112000 ) ,
		new RunErrorClass( "EngineDist" , 113000 ) ,
		new RunErrorClass( "EngineExecutor" , 114000 ) ,
		new RunErrorClass( "EngineShell" , 116000 ) ,
		new RunErrorClass( "EngineStorage" , 117000 ) ,
		new RunErrorClass( "EngineVCS" , 118000 ) ,
		new RunErrorClass( "Action" , 200000 ) ,
		new RunErrorClass( "ActionBuild" , 2010 ) ,
		new RunErrorClass( "ActionConf" , 202000 ) ,
		new RunErrorClass( "ActionDatabase" , 203000 ) ,
		new RunErrorClass( "ActionDeploy" , 204000 ) ,
		new RunErrorClass( "ActionMain" , 205000 ) ,
		new RunErrorClass( "ActionMonitor" , 206000 ) ,
		new RunErrorClass( "ActionRelease" , 207000 ) ,
		new RunErrorClass( "ActionXDoc" , 208000 ) ,
		new RunErrorClass( "MetaCommon" , 250000 ) ,
		new RunErrorClass( "MetaEngine" , 251000 ) ,
		new RunErrorClass( "MetaProduct" , 252000 ) ,
		new RunErrorClass( "Client" , 300000 ) ,
		new RunErrorClass( "Plugin" , 800000 )
	};
	
	public RunErrorClass( String groupName , int groupValue ) {
		this.groupName = groupName;
		this.groupValue = groupValue;
	}

	public static RunErrorClass getCodeClass( int code ) {
		int codeClass = code - ( code % 1000 );
		for( RunErrorClass ec : RunErrorClass.errorClasses ) {
			if( ec.groupValue == codeClass )
				return( ec );
		}
		return( null );
	}

	public static int getCodeClassError( int code ) {
		return( code % 1000 );
	}
	
}
