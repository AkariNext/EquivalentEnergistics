package com.mordenkainen.equivalentenergistics.blocks.condenser;

import net.minecraft.util.StatCollector;

import appeng.api.networking.ticking.TickRateModulation;

public enum CondenserState {

    IDLE(TickRateModulation.IDLE, "message.condenser.statename.idle", false),
    ACTIVE(TickRateModulation.URGENT, "message.condenser.statename.active", false),
    NOEMCSTORAGE(TickRateModulation.IDLE, "message.condenser.statename.noemcstorage", true),
    NOITEMSTORAGE(TickRateModulation.IDLE, "message.condenser.statename.noitemstorage", true),
    NOPOWER(TickRateModulation.IDLE, "message.condenser.statename.nopower", true);

    private final String stateName;
    private final TickRateModulation tickRate;
    private final boolean errorCondition;

    CondenserState(final TickRateModulation tickRate, final String stateName, final boolean errorCondition) {
        this.stateName = StatCollector.translateToLocal(stateName);
        this.tickRate = tickRate;
        this.errorCondition = errorCondition;
    }

    public String getStateName() {
        return stateName;
    }

    public TickRateModulation getTickRate() {
        return tickRate;
    }

    public boolean isError() {
        return errorCondition;
    }
}
