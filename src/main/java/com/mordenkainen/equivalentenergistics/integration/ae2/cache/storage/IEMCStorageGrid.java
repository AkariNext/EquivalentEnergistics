package com.mordenkainen.equivalentenergistics.integration.ae2.cache.storage;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;

import com.mordenkainen.equivalentenergistics.integration.ae2.cache.ICacheBase;
import com.mordenkainen.equivalentenergistics.util.IEMCStorage;

public interface IEMCStorageGrid extends ICacheBase, IEMCStorage {

    IGrid getGrid();

    double addEMC(double emc, Actionable mode);

    double extractEMC(double emc, Actionable mode);

}
