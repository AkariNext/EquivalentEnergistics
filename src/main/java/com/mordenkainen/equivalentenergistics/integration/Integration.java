package com.mordenkainen.equivalentenergistics.integration;

import com.mordenkainen.equivalentenergistics.EquivalentEnergistics;
import com.mordenkainen.equivalentenergistics.integration.waila.Waila;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModAPIManager;
import cpw.mods.fml.relauncher.Side;

import net.minecraftforge.common.config.Configuration;

public class Integration {

	public enum Mods {
		WAILA("Waila"),
		NEI("NotEnoughItems", Side.CLIENT),
		EE3("EE3", "EquivalentExchange3"),
		PROJECTE("ProjectE");
		
		private final String modID;
		
		private boolean shouldLoad = true;
		
		private final String name;

		private final Side side;

		private Mods(final String modid) {
			this(modid, modid);
		}

		private Mods(final String modid, final String modName) {
			this(modid, modName, null);
		}

		private Mods(final String modid, final Side side) {
			this(modid, modid, side);
		}
		
		private Mods(final String modid, final String modName, final Side _side) {
			modID = modid;
			name = modName;
			side = _side;
		}
		
		public String getModID() {
			return modID;
		}

		public String getModName() {
			return name;
		}

		public boolean isOnClient() {
			return side != Side.SERVER;
		}

		public boolean isOnServer() {
			return side != Side.CLIENT;
		}

		public void loadConfig(final Configuration config) {
			shouldLoad = config.get("Integration", "enable" + getModName(), true, "Enable " + getModName() + " Integration.").getBoolean(true);
		}
		
		public boolean isEnabled() {
			return (Loader.isModLoaded(getModID()) || ModAPIManager.INSTANCE.hasAPI(getModID())) && shouldLoad && correctSide();
		}

		private boolean correctSide() {
			return EquivalentEnergistics.proxy.isClient() ? isOnClient() : isOnServer();
		}
	}
	
	public void loadConfig(final Configuration config) {
		for (final Mods mod : Mods.values()) {
			mod.loadConfig(config);
		}
	}
	
	public void preInit() {}
	
	public void init() {
		if (Mods.WAILA.isEnabled()) {
			Waila.init();
		}
	}
	
	public void postInit() {}
}