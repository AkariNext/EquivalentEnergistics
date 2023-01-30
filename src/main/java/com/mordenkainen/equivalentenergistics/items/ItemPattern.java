package com.mordenkainen.equivalentenergistics.items;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;

import com.mordenkainen.equivalentenergistics.integration.ae2.cache.crafting.EMCCraftingGrid;
import com.mordenkainen.equivalentenergistics.items.base.ItemBase;
import cpw.mods.fml.common.Optional;

@Optional.Interface(iface = "appeng.api.implementations.ICraftingPatternItem", modid = "appliedenergistics2") // NOPMD
public class ItemPattern extends ItemBase implements ICraftingPatternItem {

    public ItemPattern() {
        super();
        setMaxStackSize(1);
    }

    @Optional.Method(modid = "appliedenergistics2")
    @Override
    public ICraftingPatternDetails getPatternForItem(final ItemStack stack, final World world) {
        return EMCCraftingGrid.getPattern(ItemStack.loadItemStackFromNBT(stack.getTagCompound()));
    }

    public static ItemStack getItemForPattern(final ItemStack target) {
        final ItemStack pattern = new ItemStack(ItemEnum.EMCPATTERN.getItem());
        pattern.setTagCompound(new NBTTagCompound());
        target.writeToNBT(pattern.getTagCompound());
        return pattern;
    }

}
