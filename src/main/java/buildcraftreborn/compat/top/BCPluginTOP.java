/*
 * Based on BuildCraftCompat, (c) the BuildCraft Team, http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraftCompat is distributed under the terms of the Minecraft Mod Public License 1.0.1, or MMPL (a copy is in
 * LICENSE-ASSETS).
 */
package buildcraftreborn.compat.top;

import java.util.function.Function;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.fml.InterModComms;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.compat.BCProbeInfo;
import buildcraftreborn.compat.BCProbeInfo.Line;

import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ProbeMode;

/** The One Probe support: shows {@link BCProbeInfo}'s lines for BuildCraft machines. Only loaded when The One Probe is
 * installed ({@link BuildCraftReborn} checks before adding {@link #enqueue}). */
public final class BCPluginTOP implements Function<ITheOneProbe, Void>, IProbeInfoProvider {
    public static final String MOD_ID = "theoneprobe";
    private static final ResourceLocation UID = BuildCraftReborn.id("info");

    public static void enqueue(InterModEnqueueEvent event) {
        InterModComms.sendTo(MOD_ID, "getTheOneProbe", BCPluginTOP::new);
    }

    @Override
    public Void apply(ITheOneProbe top) {
        top.registerProvider(this);
        return null;
    }

    @Override
    public ResourceLocation getID() {
        return UID;
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level, BlockState state,
        IProbeHitData data) {
        BlockEntity tile = level.getBlockEntity(data.getPos());
        if (tile == null) {
            return;
        }
        for (Line line : BCProbeInfo.collect(tile)) {
            IProbeInfo row = probeInfo.horizontal(probeInfo.defaultLayoutStyle()
                .alignment(ElementAlignment.ALIGN_CENTER));
            row.text(line.text());
            for (ItemStack stack : line.items()) {
                row.item(stack);
            }
        }
    }
}
