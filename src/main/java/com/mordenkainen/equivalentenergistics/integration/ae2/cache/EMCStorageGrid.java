package com.mordenkainen.equivalentenergistics.integration.ae2.cache;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.mordenkainen.equivalentenergistics.integration.Integration;
import com.mordenkainen.equivalentenergistics.integration.ae2.HandlerEMCCell;
import com.mordenkainen.equivalentenergistics.items.ItemEMCCrystal;
import com.mordenkainen.equivalentenergistics.registries.ItemEnum;
import com.mordenkainen.equivalentenergistics.util.CommonUtils;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPostCacheConstruction;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.ICellProvider;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;

import net.minecraft.item.ItemStack;

public class EMCStorageGrid implements IGridCache, ICellProvider, IMEInventoryHandler<IAEItemStack> {
	
	private static Field intHandler;
	private static Field extHandler;
	
	private float currentEMC;
	private float maxEMC;
	private final IGrid grid;
	private boolean dirty = true;
	private final List<ICellProvider> driveBays = new ArrayList<ICellProvider>();
	private IItemList<IAEItemStack> cachedList = AEApi.instance().storage().createItemList();
	
	public EMCStorageGrid(final IGrid _grid) {
		grid = _grid;
	}
	
	@MENetworkEventSubscribe
	public void afterCacheConstruction(final MENetworkPostCacheConstruction cacheConstruction) {
		((IStorageGrid) grid.getCache(IStorageGrid.class)).registerCellProvider(this);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" }) // NOPMD
	@MENetworkEventSubscribe
	public void cellUpdate(final MENetworkCellArrayUpdate cellUpdate) {
		float newEMC = 0;
		float newMax = 0;
		for (final ICellProvider provider : driveBays) {
			final List<IMEInventoryHandler> cells = provider.getCellArray(StorageChannel.ITEMS);
			for (final IMEInventoryHandler cell : cells) {
				final HandlerEMCCell handler = getHandler(cell);
				if (handler != null) {
					newEMC += handler.getEMC();
					newMax += handler.getCapacity();
				}
			}
		}
		if (newMax != maxEMC || newEMC != currentEMC) {
			maxEMC = newMax;
			currentEMC = newEMC;
			dirty = true;
		}
	}

	// IGridCache Overrides
	// ------------------------
	@Override
	public void onUpdateTick() {
		if(dirty) {
			updateDisplay();
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked" }) // NOPMD
	@Override
	public void removeNode(final IGridNode gridNode, final IGridHost machine) {
		if (machine instanceof ICellProvider) {
			float newEMC = currentEMC;
			float newMax = maxEMC;
			driveBays.remove((ICellProvider) machine);
			final List<IMEInventoryHandler> cells = ((ICellProvider) machine).getCellArray(StorageChannel.ITEMS);
			for (final IMEInventoryHandler cell : cells) {
				final HandlerEMCCell handler = getHandler(cell);
				if (handler != null) {
					newEMC -= handler.getEMC();
					newMax -= handler.getCapacity();
				}
			}
			if (newMax != maxEMC || newEMC != currentEMC) {
				maxEMC = newMax;
				currentEMC = newEMC;
				dirty = true;
			}
		}
	}

	@Override
	public void addNode(final IGridNode gridNode, final IGridHost machine) {
		if (machine instanceof ICellProvider) {
			driveBays.add((ICellProvider) machine);
		}
	}

	@Override
	public void onSplit(final IGridStorage dstStorage) {}

	@Override
	public void onJoin(final IGridStorage sourceStorage) {}

	@Override
	public void populateGridStorage(final IGridStorage dstStorage) {}
	// ------------------------
	
	// IMEInventoryHandler Overrides
	// ------------------------
	@Override
	public IAEItemStack injectItems(final IAEItemStack stack, final Actionable mode, final BaseActionSource src) {
		float itemEMC = 0;
		if (ItemEnum.EMCCRYSTAL.getItem().equals(stack.getItem())) {
			itemEMC = ItemEMCCrystal.CRYSTAL_VALUES[stack.getItemDamage()];
		} else if (ItemEnum.EMCCRYSTALOLD.getItem().equals(stack.getItem())) {
			itemEMC = Integration.emcHandler.getSingleEnergyValue(stack.getItemStack());
		}
		
		if(itemEMC > 0) {
			final int toAdd = (int) Math.min(stack.getStackSize(), (maxEMC - currentEMC) / itemEMC);
			if (toAdd > 0) {
				injectEMC(toAdd * itemEMC, mode);
				return toAdd == stack.getStackSize() ? null : stack.copy().setStackSize(stack.getStackSize() - toAdd);
			}
		}
		
		return stack;
	}

	@Override
	public IAEItemStack extractItems(final IAEItemStack request, final Actionable mode, final BaseActionSource src) {
		float itemEMC = 0;
		if (ItemEnum.EMCCRYSTAL.getItem().equals(request.getItem())) {
			itemEMC = ItemEMCCrystal.CRYSTAL_VALUES[request.getItemDamage()];
		} else if (ItemEnum.EMCCRYSTALOLD.getItem().equals(request.getItem())) {
			itemEMC = Integration.emcHandler.getSingleEnergyValue(request.getItemStack());
		}
		
		if(itemEMC > 0) {
			final int toRemove = (int) Math.min(request.getStackSize(), currentEMC / itemEMC);
			if (toRemove > 0) {
				extractEMC(toRemove * itemEMC, mode);
				return request.copy().setStackSize(toRemove);
			}
		}
		
		return null;		
	}

	@Override
	public IItemList<IAEItemStack> getAvailableItems(final IItemList<IAEItemStack> stacks) {
		for (final IAEItemStack stack : cachedList) {
			stacks.add(stack);
		}

		return stacks;
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
		return ItemEnum.EMCCRYSTAL.getItem().equals(stack.getItem()) || ItemEnum.EMCCRYSTALOLD.getItem().equals(stack.getItem());
	}

	@Override
	public boolean canAccept(final IAEItemStack stack) {
		return ItemEnum.EMCCRYSTAL.getItem().equals(stack.getItem()) || ItemEnum.EMCCRYSTALOLD.getItem().equals(stack.getItem());
	}

	@Override
	public int getSlot() {
		return 0;
	}

	@Override
	public boolean validForPass(final int pass) {
		return pass == 1;
	}
	// ------------------------

	// ICellProvider Overrides
	// ------------------------
	@SuppressWarnings("rawtypes") // NOPMD
	@Override
	public List<IMEInventoryHandler> getCellArray(final StorageChannel channel) {
		final List<IMEInventoryHandler> list = new ArrayList<IMEInventoryHandler>();

		if (channel == StorageChannel.ITEMS) {
			list.add(this);
		}

		return list;
	}

	@Override
	public int getPriority() {
		return Integer.MAX_VALUE - 1;
	}
	// ------------------------
	
	private void updateDisplay() {
		dirty = false;
		for (final IAEItemStack stack : cachedList) {
			stack.setStackSize(-stack.getStackSize());
		}
		((IStorageGrid) grid.getCache(IStorageGrid.class)).postAlterationOfStoredItems(StorageChannel.ITEMS, cachedList, new BaseActionSource());
		
		cachedList = AEApi.instance().storage().createItemList();
		if (currentEMC > 0) {
			float remainingEMC = currentEMC;
			for(int i = 4; i >= 0; i--) {
				final long crystalcount = (long) (remainingEMC / ItemEMCCrystal.CRYSTAL_VALUES[i]);
				if (crystalcount > 0) {
					cachedList.add(AEApi.instance().storage().createItemStack(ItemEnum.EMCCRYSTAL.getDamagedStack(i)).setStackSize(crystalcount));
					remainingEMC -= crystalcount * ItemEMCCrystal.CRYSTAL_VALUES[i];
				}
			}
		}
		final ItemStack totStack = ItemEnum.MISCITEM.getDamagedStack(1);
		cachedList.add(AEApi.instance().storage().createItemStack(totStack).setStackSize((long) currentEMC));
		((IStorageGrid) grid.getCache(IStorageGrid.class)).postAlterationOfStoredItems(StorageChannel.ITEMS, cachedList, new BaseActionSource());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" }) // NOPMD
	public float handleCells(final float emc) {
		float toAdjust = emc;
		for (final ICellProvider provider : driveBays) {
			final List<IMEInventoryHandler> cells = provider.getCellArray(StorageChannel.ITEMS);
			for (final IMEInventoryHandler cell : cells) {
				final HandlerEMCCell handler = getHandler(cell);
				if (handler != null) {
					toAdjust -= handler.adjustEMC(toAdjust);
					dirty = true;
					if (toAdjust == 0) {
						return emc;
					}
				}
			}
		}
		return emc;
	}
	
	public float injectEMC(final float emc, final Actionable mode) {
		final float toAdd = Math.min(emc, maxEMC - currentEMC);
		if (mode == Actionable.MODULATE) {
			final float added = handleCells(toAdd);
			currentEMC += added;
			return added;
		}
		return toAdd;
	}
	
	public float extractEMC(final float emc, final Actionable mode) {
		final float toAdd = Math.min(emc, currentEMC);
		if (mode == Actionable.MODULATE) {
			final float extracted = handleCells(-toAdd);
			currentEMC += extracted;
			return -extracted;
		}
		return toAdd;
	}
	
	@SuppressWarnings("unchecked") // NOPMD
	private HandlerEMCCell getHandler(final IMEInventoryHandler<IAEItemStack> cell) {
		if (cell instanceof HandlerEMCCell) {
			return (HandlerEMCCell) cell;
		}
		
		if (cell == null || intHandler == null && !reflectFields()) {
			return null;
		}
		
		IMEInventoryHandler<IAEItemStack> realHandler = null;
		try {
			if ("DriveWatcher".equals(cell.getClass().getSimpleName())) {
					realHandler = (IMEInventoryHandler<IAEItemStack>) intHandler.get(cell);
			} else if ("ChestMonitorHandler".equals(cell.getClass().getSimpleName())) {
				final IMEInventoryHandler<IAEItemStack> monHandler = (IMEInventoryHandler<IAEItemStack>) extHandler.get(cell);
				realHandler = (IMEInventoryHandler<IAEItemStack>) intHandler.get(monHandler);
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			CommonUtils.debugLog("Failed to reflect into AE", e);
		}
		
		if (realHandler instanceof HandlerEMCCell) {
			return (HandlerEMCCell) realHandler;
		}
		
		return null;
	}
	
	private static boolean reflectFields() {
		try {
			Class<?> clazz;
			clazz = Class.forName("appeng.me.storage.MEInventoryHandler");
			intHandler = clazz.getDeclaredField("internal");
			intHandler.setAccessible(true);
			clazz = Class.forName("appeng.api.storage.MEMonitorHandler");
			extHandler = clazz.getDeclaredField("internalHandler");
			extHandler.setAccessible(true);
		} catch (ClassNotFoundException | NoSuchFieldException | SecurityException e) {
			CommonUtils.debugLog("Failed to reflect into AE", e);
			return false;
		}

		return true;
	}
	
}
