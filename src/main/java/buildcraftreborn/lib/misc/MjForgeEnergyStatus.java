/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.lib.misc;

import buildcraftreborn.api.mj.IMjToRfStatus;
import buildcraftreborn.api.mj.MjRfConversion;
import buildcraftreborn.lib.BCLibConfig;

/** The MJ and Forge Energy settings from the common config: MJ machines take FE ({@code acceptForgeEnergy}, on by
 * default) at {@code forgeEnergyPerMj} FE to the MJ. Full two-way auto-conversion (engines and kinesis pipes powering
 * FE machines) stays off, as in 1.12, where nothing turned it on. */
public final class MjForgeEnergyStatus implements IMjToRfStatus {
    public static final MjForgeEnergyStatus INSTANCE = new MjForgeEnergyStatus();

    private volatile double lastRate = Double.NaN;
    private volatile MjRfConversion conversion = MjRfConversion.createDefault();

    private MjForgeEnergyStatus() {}

    @Override
    public MjRfConversion getConversion() {
        double rate = BCLibConfig.forgeEnergyPerMj();
        if (rate != lastRate) {
            conversion = MjRfConversion.createParsed(1 / rate);
            lastRate = rate;
        }
        return conversion;
    }

    @Override
    public boolean isAutoconvertEnabled() {
        return false;
    }

    @Override
    public boolean acceptsRf() {
        return BCLibConfig.acceptForgeEnergy();
    }
}
