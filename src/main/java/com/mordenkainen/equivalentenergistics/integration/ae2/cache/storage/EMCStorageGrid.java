package com.mordenkainen.equivalentenergistics.integration.ae2.cache.storage;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPostCacheConstruction;
import appeng.api.networking.storage.IStorageGrid;

import com.mordenkainen.equivalentenergistics.util.EMCPool;

public class EMCStorageGrid implements IEMCStorageGrid {

    private final IGrid grid;
    private final EMCPool pool = new EMCPool();
    private final EMCGridCellHandler cellHandler = new EMCGridCellHandler(this);
    private final EMCGridCrystalHandler crystalHandler = new EMCGridCrystalHandler(this);

    public EMCStorageGrid(final IGrid grid) {
        this.grid = grid;
    }

    @MENetworkEventSubscribe
    public void afterCacheConstruction(final MENetworkPostCacheConstruction cacheConstruction) {
        ((IStorageGrid) grid.getCache(IStorageGrid.class)).registerCellProvider(crystalHandler);
    }

    @MENetworkEventSubscribe
    public void cellUpdate(final MENetworkCellArrayUpdate cellUpdate) {
        cellHandler.cellUpdate(cellUpdate);
    }

    @Override
    public void onUpdateTick() {
        crystalHandler.updateDisplay();
    }

    @Override
    public void removeNode(final IGridNode gridNode, final IGridHost machine) {
        cellHandler.removeNode(gridNode, machine);
    }

    @Override
    public void addNode(final IGridNode gridNode, final IGridHost machine) {
        cellHandler.addNode(gridNode, machine);
    }

    @Override
    public IGrid getGrid() {
        return grid;
    }

    @Override
    public double getCurrentEMC() {
        return pool.getCurrentEMC();
    }

    @Override
    public double getMaxEMC() {
        return pool.getMaxEMC();
    }

    @Override
    public double getAvail() {
        return pool.getAvail();
    }

    @Override
    public boolean isFull() {
        return pool.isFull();
    }

    @Override
    public boolean isEmpty() {
        return pool.isEmpty();
    }

    @Override
    public void setCurrentEMC(final double currentEMC) {
        pool.setCurrentEMC(currentEMC);
    }

    @Override
    public void setMaxEMC(final double maxEMC) {
        pool.setMaxEMC(maxEMC);
    }

    @Override
    public double addEMC(final double emc) {
        return pool.addEMC(emc);
    }

    @Override
    public double addEMC(final double emc, final Actionable mode) {
        return cellHandler.injectEMC(emc, mode);
    }

    @Override
    public double extractEMC(final double emc) {
        return pool.extractEMC(emc);
    }

    @Override
    public double extractEMC(final double emc, final Actionable mode) {
        return cellHandler.extractEMC(emc, mode);
    }

    public void markDirty() {
        crystalHandler.markDirty();
    }

}
