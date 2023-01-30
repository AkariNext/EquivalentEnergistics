package com.mordenkainen.equivalentenergistics.items;

import com.mordenkainen.equivalentenergistics.core.textures.TextureEnum;
import com.mordenkainen.equivalentenergistics.items.base.ItemMultiBase;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ItemStorageComponent extends ItemMultiBase {

    public ItemStorageComponent() {
        super(8);
    }

    @Override
    public EnumRarity getRarity(final ItemStack stack) {
        return EnumRarity.values()[stack.getItemDamage() / 2];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(final int damage) {
        return TextureEnum.EMCSTORAGECOMPONENT.getTexture(damage);
    }

}
