package org.urm.engine.dist;

import org.urm.common.RunErrorClass;

public class _Error {

	public static final int ErrorBase = RunErrorClass.BaseEngineDist;
	public static final int ErrorInternalBase = ErrorBase + RunErrorClass.InternalBase;
	
	public static final int DistributiveNotOpened0 = ErrorBase + 1;
	public static final int DistributiveNotUse0 = ErrorBase + 2;
	public static final int UnknownReleaseDelivery1 = ErrorBase + 3;
	public static final int DistPathNotDefined0 = ErrorBase + 4;
	public static final int MissingRelease1 = ErrorBase + 5;
	public static final int ReleaseAlreadyExists1 = ErrorBase + 6;
	public static final int InvalidReleaseVersion1 = ErrorBase + 7;
	public static final int UnableFindProdDistributive0 = ErrorBase + 8;
	public static final int LastMinorVersionNotSet0 = ErrorBase + 9;
	public static final int NextMinorVersionNotSet0 = ErrorBase + 10;
	public static final int UnexpectedReleaseLabel1 = ErrorBase + 11;
	public static final int MissingProdFolder1 = ErrorBase + 12;
	public static final int ProdFolderAlreadyInitialized1 = ErrorBase + 13;
	public static final int UnableChangeReleaseState2 = ErrorBase + 14;
	public static final int CannotCreateExistingDistributive0 = ErrorBase + 15;
	public static final int MissingProdDistributiveDirectory0 = ErrorBase + 16;
	public static final int StateFileExists0 = ErrorBase + 17;
	public static final int DistributiveNotReadyForChange1 = ErrorBase + 18;
	public static final int DistributiveNotOpenedForChange1 = ErrorBase + 19;
	public static final int DistributiveOpenedForConcurrentChange1 = ErrorBase + 20;
	public static final int DistributiveNotReleased1 = ErrorBase + 21;
	public static final int DistributiveNotReadyForUse1 = ErrorBase + 22;
	public static final int DistributiveNotReadyForProd1 = ErrorBase + 23;
	public static final int DistributiveHashDiffers0 = ErrorBase + 24;
	public static final int DistributiveNotClosed1 = ErrorBase + 25;
	public static final int DistributiveProtected1 = ErrorBase + 26;
	public static final int CompatibilityExpectedForEarlierRelease1 = ErrorBase + 27;
	public static final int UnknownReleaseSet1 = ErrorBase + 28;
	public static final int UnknownReleaseCategorySet1 = ErrorBase + 29;
	public static final int ReleaseVersionNotSet0 = ErrorBase + 30;
	public static final int UnknownReleaseProject1 = ErrorBase + 31;
	public static final int UnknownReleaseComponent1 = ErrorBase + 32;
	public static final int UnknownReleaseDeliveryFolder1 = ErrorBase + 33;
	public static final int UnexpectedInternalItem1 = ErrorBase + 34;
	public static final int UnexpectedNonManualItem1 = ErrorBase + 35;
	public static final int DatabaseItemAlreadyAdded0 = ErrorBase + 36;
	public static final int UnexpectedReleaseSourceType1 = ErrorBase + 37;
	public static final int UnexpectedFullSetProjects1 = ErrorBase + 38;
	public static final int UnexpectedFullSetConfigurationItems0 = ErrorBase + 39;
	public static final int UnexpectedFullSetDatabaseItems0 = ErrorBase + 40;
	public static final int UnexpectedFullSetManuaItems0 = ErrorBase + 41;
	public static final int UnknownReleaseTarget1 = ErrorBase + 42;	
	public static final int ReleaseRepositoryExists1 = ErrorBase + 43;
	public static final int MissingReleaseRepositoryParent1 = ErrorBase + 44;
	public static final int MissingReleaseRepository1 = ErrorBase + 45;
	public static final int NotExpectedReleasecycleType1 = ErrorBase + 46;
	public static final int MissingReleaseDate0 = ErrorBase + 47;
	public static final int MissingReleasecycleType0 = ErrorBase + 48;
	public static final int DisabledLifecycle1 = ErrorBase + 49;
	public static final int AlreadyReleased1 = ErrorBase + 50;
	
}
