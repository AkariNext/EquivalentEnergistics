package com.mordenkainen.equivalentenergistics.integration.ae2;

import java.lang.reflect.Method;

import com.mordenkainen.equivalentenergistics.registries.ItemEnum;
import com.mordenkainen.equivalentenergistics.util.CommonUtils;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import net.minecraft.item.Item;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.oredict.OreDictionary;

public final class AppliedEnergistics2 {
	
	private static AppliedEnergistics2 instance;
	
	private AppliedEnergistics2() {}
	
	public static void init() {
		instance = new AppliedEnergistics2();
		MinecraftForge.EVENT_BUS.register(instance);
	}
	
	@SubscribeEvent
	public void worldLoad(final WorldEvent.Load event) {
		try {
			final Class<?> cellInv = Class.forName("appeng.me.storage.CellInventory");
			final Method blackList = cellInv.getDeclaredMethod("addBasicBlackList", int.class, int.class);
			blackList.invoke(null, Item.getIdFromItem(ItemEnum.EMCCRYSTAL.getItem()), OreDictionary.WILDCARD_VALUE);
			blackList.invoke(null, Item.getIdFromItem(ItemEnum.EMCCRYSTALOLD.getItem()), OreDictionary.WILDCARD_VALUE);
		} catch (final IllegalArgumentException e) {
			logReflectionError(e);
		} catch (final SecurityException e) {
			logReflectionError(e);
		} catch (final ReflectiveOperationException e) {
			logReflectionError(e);
		}
	}
	
	private void logReflectionError(final Exception e) {
		CommonUtils.debugLog("Failed to blacklist EMC Crystals from AE Cells", e);
	}
	
}