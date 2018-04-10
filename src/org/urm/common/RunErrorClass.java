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
	public static int BaseEngineProperties = 119000;
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
	public static int BaseMetaEnv = 253000;
	public static int BaseMetaRelease = 254000;
	public static int BaseClient = 300000;
	public static int BaseDB = 400000;
	public static int BaseDBCore = 401000;
	public static int BaseDBEngine = 402000;
	public static int BaseDBRelease = 403000;
	public static int BasePlugin = 800000;
	
	public static int InternalBase = 500;

	public String groupName;
	public int groupValue;
	
	public static RunErrorClass[] errorClasses = { 
		new RunErrorClass( "Common" , BaseCommon ) ,
		new RunErrorClass( "CommonAction" , BaseCommonAction ) ,
		new RunErrorClass( "CommonJmx" , BaseCommonJmx ) ,
		new RunErrorClass( "CommonMeta" , BaseCommonMeta ) ,
		new RunErrorClass( "Engine" , BaseEngine ) ,
		new RunErrorClass( "EngineAction" , BaseEngineAction ) ,
		new RunErrorClass( "EngineCustom" , BaseEngineCustom ) ,
		new RunErrorClass( "EngineDist" , BaseEngineDist ) ,
		new RunErrorClass( "EngineExecutor" , BaseEngineExecutor ) ,
		new RunErrorClass( "EngineShell" , BaseEngineShell ) ,
		new RunErrorClass( "EngineStorage" , BaseEngineStorage ) ,
		new RunErrorClass( "EngineVCS" , BaseEngineVCS ) ,
		new RunErrorClass( "EngineProperties" , BaseEngineProperties ) ,
		new RunErrorClass( "Action" , BaseAction ) ,
		new RunErrorClass( "ActionBuild" , BaseActionBuild ) ,
		new RunErrorClass( "ActionConf" , BaseActionConf ) ,
		new RunErrorClass( "ActionDatabase" , BaseActionDatabase ) ,
		new RunErrorClass( "ActionDeploy" , BaseActionDeploy ) ,
		new RunErrorClass( "ActionMain" , BaseActionMain ) ,
		new RunErrorClass( "ActionMonitor" , BaseActionMonitor ) ,
		new RunErrorClass( "ActionRelease" , BaseActionRelease ) ,
		new RunErrorClass( "ActionXDoc" , BaseActionXDoc ) ,
		new RunErrorClass( "MetaCommon" , BaseMetaCommon ) ,
		new RunErrorClass( "MetaEngine" , BaseMetaEngine ) ,
		new RunErrorClass( "MetaProduct" , BaseMetaProduct ) ,
		new RunErrorClass( "Client" , BaseClient ) ,
		new RunErrorClass( "Database" , BaseDB ) ,
		new RunErrorClass( "DatabaseCore" , BaseDBCore ) ,
		new RunErrorClass( "DatabaseEngine" , BaseDBEngine ) ,
		new RunErrorClass( "Plugin" , BasePlugin )
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
