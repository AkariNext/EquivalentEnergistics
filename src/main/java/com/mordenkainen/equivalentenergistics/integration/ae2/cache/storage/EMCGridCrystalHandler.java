package com.mordenkainen.equivalentenergistics.integration.ae2.cache.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.ICellProvider;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;

import com.mordenkainen.equivalentenergistics.integration.Integration;
import com.mordenkainen.equivalentenergistics.items.ItemEMCCrystalOld;
import com.mordenkainen.equivalentenergistics.items.ItemEnum;

public class EMCGridCrystalHandler implements ICellProvider, IMEInventoryHandler<IAEItemStack> {

    private final EMCStorageGrid hostGrid;
    private boolean dirty = true;
    private IItemList<IAEItemStack> cachedList = AEApi.instance().storage().createItemList();

    public EMCGridCrystalHandler(final EMCStorageGrid hostGrid) {
        this.hostGrid = hostGrid;
    }

    @Override
    public IAEItemStack injectItems(final IAEItemStack stack, final Actionable mode, final BaseActionSource src) {
        double itemEMC = 0;

        // TODO fix this?
        if (ItemEnum.EMCCRYSTAL.isSameItem(stack.getItemStack())) {
            itemEMC = Integration.emcHandler.getCrystalEMC(stack.getItemDamage());
        } else if (ItemEnum.EMCCRYSTALOLD.isSameItem(stack.getItemStack())) {
            itemEMC = ItemEMCCrystalOld.CRYSTAL_VALUES[stack.getItemDamage()];
        }

        if (itemEMC > 0) {
            final int toAdd = (int) Math.min(stack.getStackSize(), hostGrid.getAvail() / itemEMC);
            if (toAdd > 0) {
                hostGrid.addEMC(toAdd * itemEMC, mode);
                return toAdd == stack.getStackSize() ? null : stack.copy().setStackSize(stack.getStackSize() - toAdd);
            }
        }

        return stack;
    }

    @Override
    public IAEItemStack extractItems(final IAEItemStack stack, final Actionable mode, final BaseActionSource src) {
        double itemEMC = 0;

        // TODO fix this?
        if (ItemEnum.EMCCRYSTAL.isSameItem(stack.getItemStack())) {
            itemEMC = Integration.emcHandler.getCrystalEMC(stack.getItemDamage());
        } else if (ItemEnum.EMCCRYSTALOLD.isSameItem(stack.getItemStack())) {
            itemEMC = ItemEMCCrystalOld.CRYSTAL_VALUES[stack.getItemDamage()];
        }

        if (itemEMC > 0) {
            final int toRemove = (int) Math.min(stack.getStackSize(), hostGrid.getCurrentEMC() / itemEMC);
            if (toRemove > 0) {
                hostGrid.extractEMC(toRemove * itemEMC, mode);
                return stack.copy().setStackSize(toRemove);
            }
        }

        return null;
    }

    @Override
    public IItemList<IAEItemStack> getAvailableItems(final IItemList<IAEItemStack> items) {
        for (final IAEItemStack stack : cachedList) {
            items.add(stack);
        }

        return items;
    }

    @Override
    public StorageChannel getChannel() {
        return StorageChannel.ITEMS;
    }

    @Override
    public AccessRestriction getAccess() {
        return AccessRestriction.READ_WRITE;
    }

    @Override
    public boolean isPrioritized(final IAEItemStack stack) {
        return ItemEnum.isCrystal(stack.getItemStack());
    }

    @Override
    public boolean canAccept(final IAEItemStack stack) {
        return ItemEnum.isCrystal(stack.getItemStack());
    }

    @Override
    public int getSlot() {
        return 0;
    }

    @Override
    public boolean validForPass(final int pass) {
        return pass == 1;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<IMEInventoryHandler> getCellArray(final StorageChannel channel) {
        if (channel == StorageChannel.ITEMS) {
            return new ArrayList<IMEInventoryHandler>(Arrays.asList(new IMEInventoryHandler[] { this }));
        }

        return new ArrayList<IMEInventoryHandler>();
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE - 1;
    }

    public void markDirty() {
        dirty = true;
    }

    public void updateDisplay() {
        if (!dirty) {
            return;
        }

        dirty = false;
        final IStorageGrid storageGrid = (IStorageGrid) hostGrid.getGrid().getCache(IStorageGrid.class);

        for (final IAEItemStack stack : cachedList) {
            stack.setStackSize(-stack.getStackSize());
        }
        storageGrid.postAlterationOfStoredItems(StorageChannel.ITEMS, cachedList, new BaseActionSource());

        cachedList = AEApi.instance().storage().createItemList();
        if (hostGrid.getCurrentEMC() > 0) {
            double remainingEMC = hostGrid.getCurrentEMC();
            for (int i = 4; i >= 0; i--) {
                final double crystalEMC = Integration.emcHandler.getCrystalEMC(i);
                final long crystalcount = (long) (remainingEMC / crystalEMC);
                if (crystalcount > 0) {
                    cachedList.add(
                            AEApi.instance().storage().createItemStack(ItemEnum.EMCCRYSTAL.getDamagedStack(i))
                                    .setStackSize(crystalcount));
                    remainingEMC -= crystalcount * crystalEMC;
                }
            }
        }

        cachedList.add(
                AEApi.instance().storage().createItemStack(ItemEnum.MISCITEM.getDamagedStack(1))
                        .setStackSize((long) hostGrid.getCurrentEMC()));
        storageGrid.postAlterationOfStoredItems(StorageChannel.ITEMS, cachedList, new BaseActionSource());
    }

}
