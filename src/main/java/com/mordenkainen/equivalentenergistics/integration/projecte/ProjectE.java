package com.mordenkainen.equivalentenergistics.integration.projecte;

import com.mordenkainen.equivalentenergistics.integration.IEMCHandler;
import com.mordenkainen.equivalentenergistics.registries.ItemEnum;
import com.mordenkainen.equivalentenergistics.tiles.TileEMCCrafter;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.event.EMCRemapEvent;
import moze_intel.projecte.api.event.PlayerKnowledgeChangeEvent;

import net.minecraft.item.ItemStack;

public class ProjectE implements IEMCHandler {
	
	private float[] crystalValues = {0.0F, 0.0F, 0.0F};

	@Override
	public void relearnCrystals() {
		crystalValues = new float[] {0.0F, 0.0F, 0.0F};
		for (int i = 0; i <= 2; i++) {
			crystalValues[i] = getCrystalEMC(i);
		}
	}

	@Override
	public boolean hasEMC(final ItemStack itemStack) {
		// horrible hack for bug in PE API
		// return ProjectEAPI.getEMCProxy().hasValue(itemStack));
		return ProjectEAPI.getEMCProxy().getValue(itemStack) > 0;
	}

	@Override
	public float getEnergyValue(final ItemStack itemStack) {
		return ProjectEAPI.getEMCProxy().getValue(itemStack);
	}

	@Override
	public float getCrystalEMC() {
		return getCrystalEMC(0);
	}

	@Override
	public float getCrystalEMC(final int tier) {
		if (this.crystalValues[tier] == 0.0F) {
			crystalValues[tier] = ProjectEAPI.getEMCProxy().getValue(new ItemStack(ItemEnum.EMCCRYSTAL.getItem(), 1, tier));
		}
		return crystalValues[tier];
	}

	@Override
	public List<ItemStack> getTransmutations(final TileEMCCrafter tile) {
		List<ItemStack> transmutations;

		transmutations = ProjectEAPI.getTransmutationProxy().getKnowledge(UUID.fromString(tile.getCurrentTome().getTagCompound().getString("OwnerUUID")));
		
		if (transmutations == null) {
			transmutations = new ArrayList<ItemStack>();
		}
		
		final Iterator<ItemStack> iter = transmutations.iterator();
		while (iter.hasNext()) {
			final ItemStack currentItem = (ItemStack)iter.next();
			if (currentItem == null || currentItem.getItem() == ItemEnum.EMCCRYSTAL.getItem()) {
				iter.remove();
			}
		}
		return transmutations;
	}

	@Override
	public boolean isValidTome(final ItemStack itemStack)	{
		return itemStack != null && itemStack.getItem() == ItemEnum.EMCBOOK.getItem() && itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("OwnerUUID");
	}

	@Override
	public void setCrystalEMC(final float emc) {
		ProjectEAPI.getEMCProxy().registerCustomEMC(new ItemStack(ItemEnum.EMCCRYSTAL.getItem(), 1, 0), (int)1);
		ProjectEAPI.getEMCProxy().registerCustomEMC(new ItemStack(ItemEnum.EMCCRYSTAL.getItem(), 1, 1), (int)64);
		ProjectEAPI.getEMCProxy().registerCustomEMC(new ItemStack(ItemEnum.EMCCRYSTAL.getItem(), 1, 2), (int)emc);
		ProjectEAPI.getEMCProxy().registerCustomEMC(new ItemStack(ItemEnum.EMCCRYSTAL.getItem(), 1, 3), (int)emc * 576);
		ProjectEAPI.getEMCProxy().registerCustomEMC(new ItemStack(ItemEnum.EMCCRYSTAL.getItem(), 1, 4), (int)(emc * Math.pow(576.0D, 2.0D)));
		
		ProjectEAPI.getEMCProxy().registerCustomEMC(new ItemStack(ItemEnum.EMCCRYSTALOLD.getItem(), 1, 0), (int)emc);
		ProjectEAPI.getEMCProxy().registerCustomEMC(new ItemStack(ItemEnum.EMCCRYSTALOLD.getItem(), 1, 1), (int)emc * 576);
		ProjectEAPI.getEMCProxy().registerCustomEMC(new ItemStack(ItemEnum.EMCCRYSTALOLD.getItem(), 1, 2), (int)(emc * Math.pow(576.0D, 2.0D)));
	}

	@Override
	public UUID getTomeUUID(final ItemStack currentTome) {
		return UUID.fromString(currentTome.getTagCompound().getString("OwnerUUID"));
	}

	@Override
	public String getTomeOwner(final ItemStack currentTome) {
		return currentTome.getTagCompound().getString("Owner");
	}

	@Override
	public float getSingleEnergyValue(final ItemStack stack) {
		final ItemStack singleStack = stack.copy();
		singleStack.stackSize = 1;
		return getEnergyValue(singleStack);
	}
	
	@SubscribeEvent
	public void onPlayerKnowledgeChange(final PlayerKnowledgeChangeEvent event) {
		TileEMCCrafter.postKnowledgeChange(event.playerUUID);
	}

	@SubscribeEvent
	public void onEnergyValueChange(final EMCRemapEvent event) {
		TileEMCCrafter.postEnergyValueChange();
	}
	
}
