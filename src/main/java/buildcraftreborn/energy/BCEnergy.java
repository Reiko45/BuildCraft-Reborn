/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.energy;

import buildcraftreborn.api.enums.EnumSpring;
import buildcraftreborn.api.fuels.BuildcraftFuelRegistry;
import buildcraftreborn.lib.BCRegistries;
import buildcraftreborn.lib.fluid.CoolantRegistry;
import buildcraftreborn.lib.fluid.FuelRegistry;
import buildcraftreborn.lib.recipe.ConfigCondition;

/** Energy module (1.12's {@code buildcraftenergy} mod): engines, oil and fuels, oil wells and the oil spring. Initialised from
 * {@code BuildCraftReborn}'s constructor. */
public final class BCEnergy {
    /** The {@code buildcraftreborn:config} condition names of the energy config's switches. */
    public static final String FLAG_RF_ENGINE = "energy.enable_rf_engine";
    public static final String FLAG_MJ_DYNAMO = "energy.enable_mj_dynamo";

    private BCEnergy() {}

    public static void init() {
        BuildcraftFuelRegistry.fuel = FuelRegistry.INSTANCE;
        BuildcraftFuelRegistry.coolant = CoolantRegistry.INSTANCE;
        BCEnergyFluids.init();
        BCEnergyBlocks.init();
        ConfigCondition.registerFlag(FLAG_RF_ENGINE, BCEnergyConfig::enableRfEngine);
        ConfigCondition.registerFlag(FLAG_MJ_DYNAMO, BCEnergyConfig::enableMjDynamo);
        BCRegistries.CREATIVE_TAB_SWITCHES.put(BCEnergyBlocks.ENGINE_RF.getId(), FLAG_RF_ENGINE);
        BCRegistries.CREATIVE_TAB_SWITCHES.put(BCEnergyBlocks.MJ_DYNAMO.getId(), FLAG_MJ_DYNAMO);
    }

    /** Called from common setup, once every block exists. */
    public static void commonSetup() {
        EnumSpring.OIL.liquidBlock = BCEnergyFluids.crudeOil[0].getBlockOrNull().defaultBlockState();
    }
}
