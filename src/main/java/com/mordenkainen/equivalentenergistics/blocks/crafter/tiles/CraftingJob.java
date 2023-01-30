package com.mordenkainen.equivalentenergistics.blocks.crafter.tiles;

import net.minecraft.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.security.MachineSource;

import com.mordenkainen.equivalentenergistics.integration.ae2.grid.AEProxy;
import com.mordenkainen.equivalentenergistics.integration.ae2.grid.GridUtils;

public class CraftingJob {

    private double craftingTicks;
    private final ItemStack outputStack;
    private final AEProxy proxy;
    private final MachineSource source;
    private boolean finished;
    private final double cost;

    public CraftingJob(final double craftingTicks, final ItemStack outputStack, final double cost, final AEProxy proxy,
            final MachineSource source) {
        this.craftingTicks = craftingTicks;
        this.outputStack = outputStack;
        this.proxy = proxy;
        this.source = source;
        this.cost = cost;
    }

    public boolean isFinished() {
        return finished;
    }

    public ItemStack getOutput() {
        return outputStack;
    }

    public double getCost() {
        return cost;
    }

    public boolean craftingTick() {
        if (craftingTicks <= 0) {
            final ItemStack rejected = GridUtils.injectItems(proxy, outputStack, Actionable.SIMULATE, source);

            if (rejected == null || rejected.stackSize == 0) {
                GridUtils.injectItems(proxy, outputStack, Actionable.MODULATE, source);
                finished = true;
            } else {
                return false;
            }
        } else {
            final double powerExtracted = GridUtils
                    .extractAEPower(proxy, cost, Actionable.SIMULATE, PowerMultiplier.CONFIG) + 0.9;
            if (powerExtracted - cost >= 0.0D) {
                GridUtils.extractAEPower(proxy, cost, Actionable.MODULATE, PowerMultiplier.CONFIG);
                craftingTicks--;
            } else {
                return false;
            }
        }

        return true;
    }

    public double getRemainingTicks() {
        return craftingTicks;
    }

}
