package com.mordenkainen.equivalentenergistics.integration.ae2.cells;

import com.mordenkainen.equivalentenergistics.util.EMCPool;

import appeng.api.storage.ISaveProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class HandlerEMCCell extends HandlerEMCCellBase {

	private static final String EMC_TAG = "emc";

    private final NBTTagCompound cellData;
    private EMCPool pool = new EMCPool();
    
	public HandlerEMCCell(final ItemStack storageStack, final ISaveProvider saveProvider, final float capacity) {
        super(saveProvider);
        if (!storageStack.hasTagCompound()) {
            storageStack.setTagCompound(new NBTTagCompound());
        }

        pool.setMaxEMC(capacity);
        cellData = storageStack.getTagCompound();
        if (cellData.hasKey(EMC_TAG)) {
        	pool.setCurrentEMC(cellData.getLong(EMC_TAG));
        }
    }

	@Override
    public int getCellStatus() {
        if (pool.getCurrentEMC() >= pool.getMaxEMC()) {
            return 3;
        }
        if (pool.getCurrentEMC() >= pool.getMaxEMC() * 0.75) {
            return 2;
        }
        return 1;
    }

	@Override
	public float getCurrentEMC() {
		return pool.getCurrentEMC();
	}

	@Override
	public void setCurrentEMC(float currentEMC) {}

	@Override
	public float getMaxEMC() {
		return pool.getMaxEMC();
	}

	@Override
	public void setMaxEMC(float maxEMC) {}

	@Override
	public float getAvail() {
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
	public float addEMC(float emc) {
		float added = pool.addEMC(emc);
		if (added > 0) {
			updateEMC();
		}
		return added;
	}

	@Override
	public float extractEMC(float emc) {
		float extracted = pool.extractEMC(emc);
		if (extracted > 0) {
			updateEMC();
		}
		return extracted;
	}
	
	private void updateEMC() {
        cellData.setFloat(EMC_TAG, pool.getCurrentEMC());
        if (saveProvider != null) {
            saveProvider.saveChanges(this);
        }
    }
	
}