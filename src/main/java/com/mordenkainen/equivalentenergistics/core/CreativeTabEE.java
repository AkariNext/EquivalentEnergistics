package com.mordenkainen.equivalentenergistics.core;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.mordenkainen.equivalentenergistics.items.ItemEnum;

public class CreativeTabEE extends CreativeTabs {

    public CreativeTabEE(final int tabID, final String label) {
        super(tabID, label);
    }

    @Override
    public Item getTabIconItem() {
        return null;
    }

    @Override
    public ItemStack getIconItemStack() {
        return ItemEnum.EMCCRYSTAL.getDamagedStack(4);
    }

}
