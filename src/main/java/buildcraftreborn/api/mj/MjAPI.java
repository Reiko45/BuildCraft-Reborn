/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.mj;

import java.text.DecimalFormat;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.capabilities.BlockCapability;

public class MjAPI {
    // ################################
    //
    // Useful constants (Public API)
    //
    // ################################

    /** A single minecraft joule, in micro joules (the power system base unit) */
    public static final long ONE_MINECRAFT_JOULE = getMjValue();
    /** The same as {@link #ONE_MINECRAFT_JOULE}, but a shorter field name */
    public static final long MJ = ONE_MINECRAFT_JOULE;

    /** The decimal format used to display values of MJ to the player. */
    public static final DecimalFormat MJ_DISPLAY_FORMAT = new DecimalFormat("#,##0.##");

    public static IMjEffectManager EFFECT_MANAGER = NullaryEffectManager.INSTANCE;

    // ###############
    //
    // Helpful methods
    //
    // ###############

    /** Formats a given MJ value to a player-oriented string. Note that this does not append "MJ" to the value. */
    public static String formatMj(long microMj) {
        return formatMjInternal(microMj / (double) MJ);
    }

    private static String formatMjInternal(double val) {
        return MJ_DISPLAY_FORMAT.format(val);
    }

    public static MjRfConversion getRfConversion() {
        return IMjToRfStatus.get().getConversion();
    }

    public static boolean isRfAutoConversionEnabled() {
        return IMjToRfStatus.get().isAutoconvertEnabled();
    }

    /** @return Whether MJ receivers also take RF (Forge Energy), converted at {@link #getRfConversion()}. */
    public static boolean acceptsRf() {
        return IMjToRfStatus.get().acceptsRf();
    }

    // ########################################
    //
    // Null based classes
    //
    // ########################################

    public enum NullaryEffectManager implements IMjEffectManager {
        INSTANCE;

        @Override
        public void createPowerLossEffect(Level world, Vec3 center, long microJoulesLost) {}

        @Override
        public void createPowerLossEffect(Level world, Vec3 center, Direction direction, long microJoulesLost) {}

        @Override
        public void createPowerLossEffect(Level world, Vec3 center, Vec3 direction, long microJoulesLost) {}
    }

    // ###############
    //
    // Capabilities
    //
    // ###############

    // 1.21: NeoForge block capabilities replace the Forge Capability objects. Query them with
    // level.getCapability(MjAPI.CAP_RECEIVER, pos, side), or cache them with BlockCapabilityCache.

    public static final BlockCapability<IMjConnector, @Nullable Direction> CAP_CONNECTOR =
        BlockCapability.createSided(id("mj_connector"), IMjConnector.class);
    public static final BlockCapability<IMjReceiver, @Nullable Direction> CAP_RECEIVER =
        BlockCapability.createSided(id("mj_receiver"), IMjReceiver.class);
    public static final BlockCapability<IMjRedstoneReceiver, @Nullable Direction> CAP_REDSTONE_RECEIVER =
        BlockCapability.createSided(id("mj_redstone_receiver"), IMjRedstoneReceiver.class);
    public static final BlockCapability<IMjReadable, @Nullable Direction> CAP_READABLE =
        BlockCapability.createSided(id("mj_readable"), IMjReadable.class);
    public static final BlockCapability<IMjPassiveProvider, @Nullable Direction> CAP_PASSIVE_PROVIDER =
        BlockCapability.createSided(id("mj_passive_provider"), IMjPassiveProvider.class);

    private static long getMjValue() {
        return 1_000_000L;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("buildcraftreborn", path);
    }
}
