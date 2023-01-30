package com.mordenkainen.equivalentenergistics.integration.ae2.cache.crafting;

import net.minecraft.item.ItemStack;

import com.mordenkainen.equivalentenergistics.integration.ae2.EMCCraftingPattern;
import com.mordenkainen.equivalentenergistics.integration.ae2.cache.ICacheBase;

public interface IEMCCraftingGrid extends ICacheBase {

    EMCCraftingPattern[] getPatterns();

    void updatePatterns();

    boolean allCraftersBusy();

    boolean addJob(ItemStack stack, double inputCost, double outputCost);

}
