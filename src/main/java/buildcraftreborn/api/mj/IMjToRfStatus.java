/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.mj;

/** The current MJ &lt;-&gt; RF conversion settings. BuildCraft Reborn's lib module installs the configured status with
 * {@link #set(IMjToRfStatus)}; until then auto-conversion is off and the default rate applies. (1.12 looked the
 * implementation up by reflection instead.) */
public interface IMjToRfStatus {
    static IMjToRfStatus get() {
        return MjToRfStatusHolder.status;
    }

    static void set(IMjToRfStatus status) {
        MjToRfStatusHolder.status = status == null ? MjToRfStatusHolder.DEFAULT : status;
    }

    MjRfConversion getConversion();

    /** @return Whether MJ and RF convert both ways on their own: MJ machines take and give RF, and MJ sources (engines,
     *         kinesis pipes) power RF machines. */
    boolean isAutoconvertEnabled();

    /** @return Whether MJ receivers also take RF (Forge Energy), converted to MJ. Engines and kinesis pipes still only
     *         give MJ unless {@link #isAutoconvertEnabled()}. */
    default boolean acceptsRf() {
        return isAutoconvertEnabled();
    }
}

final class MjToRfStatusHolder implements IMjToRfStatus {
    static final IMjToRfStatus DEFAULT = new MjToRfStatusHolder();
    static volatile IMjToRfStatus status = DEFAULT;

    private final MjRfConversion defaultConversion = MjRfConversion.createDefault();

    @Override
    public MjRfConversion getConversion() {
        return defaultConversion;
    }

    @Override
    public boolean isAutoconvertEnabled() {
        return false;
    }
}
