package com.mordenkainen.equivalentenergistics.blocks.condenser.tiles;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.mordenkainen.equivalentenergistics.blocks.BlockEnum;

public class TileEMCCondenserUlt extends TileEMCCondenserExt {

    public TileEMCCondenserUlt() {
        super(new ItemStack(Item.getItemFromBlock(BlockEnum.EMCCONDENSER.getBlock()), 1, 3));
    }

    @Override
    protected double getEMCPerTick() {
        return Float.MAX_VALUE;
    }

    @Override
    protected int itemsToTransfer() {
        return 256;
    }
}
