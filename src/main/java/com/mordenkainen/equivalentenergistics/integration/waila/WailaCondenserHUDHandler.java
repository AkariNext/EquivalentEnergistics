package com.mordenkainen.equivalentenergistics.integration.waila;

import java.util.List;

import com.mordenkainen.equivalentenergistics.tiles.condenser.TileEMCCondenserAdv;
import com.mordenkainen.equivalentenergistics.tiles.condenser.TileEMCCondenserAdv.RedstoneMode;
import com.mordenkainen.equivalentenergistics.tiles.condenser.TileEMCCondenserBase;
import com.mordenkainen.equivalentenergistics.tiles.condenser.TileEMCCondenserBase.CondenserState;
import com.mordenkainen.equivalentenergistics.util.CommonUtils;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.SpecialChars;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class WailaCondenserHUDHandler implements IWailaDataProvider {
	private final static String EMC_TAG = "CurrentEMC";
    private final static String TAG_NAME = "TileData";

    @Override
    public List<String> getWailaBody(final ItemStack itemStack, final List<String> currenttip, final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        final NBTTagCompound tag = accessor.getNBTData();
        
        if (tag.hasKey(TAG_NAME)) {
        	final NBTTagCompound innerTag = tag.getCompoundTag(TAG_NAME);
        	if (innerTag.hasKey(EMC_TAG)) {
        		currenttip.add(StatCollector.translateToLocal("tooltip.emc.name") + " " + SpecialChars.RED + CommonUtils.formatEMC(innerTag.getFloat(EMC_TAG)) + SpecialChars.RESET);
        	}
        }
        
        final CondenserState state = ((TileEMCCondenserBase) accessor.getTileEntity()).getState();
        currenttip.add(StatCollector.translateToLocal("tooltip.status.name") + " " + (state == CondenserState.ACTIVE || state == CondenserState.IDLE ? SpecialChars.GREEN : SpecialChars.RED) + state.getName() + SpecialChars.RESET);
        
        if(accessor.getMetadata() > 0) {
	        final RedstoneMode mode = ((TileEMCCondenserAdv) accessor.getTileEntity()).getMode();
	        currenttip.add(StatCollector.translateToLocal("tooltip.mode.name") + " " + mode.description());
        }
	        
        return currenttip;
    }

    @Override
    public ItemStack getWailaStack(final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(final ItemStack itemStack, final List<String> currenttip, final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaTail(final ItemStack itemStack, final List<String> currenttip, final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        return null;
    }

    @Override
    public NBTTagCompound getNBTData(final EntityPlayerMP player, final TileEntity tile, final NBTTagCompound tag, final World world, final int x, final int y, final int z) {
        if (tile instanceof IWailaNBTProvider) {
            tag.setTag(TAG_NAME, ((IWailaNBTProvider) tile).getWailaTag(new NBTTagCompound()));
        }
        return tag;
    }

}