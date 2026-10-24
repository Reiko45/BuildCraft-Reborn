/*
 * Based on BuildCraftCompat, (c) the BuildCraft Team, http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraftCompat is distributed under the terms of the Minecraft Mod Public License 1.0.1, or MMPL (a copy is in
 * LICENSE-ASSETS).
 */
package buildcraftreborn.compat;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.neoforged.neoforge.items.IItemHandler;

import buildcraftreborn.api.mj.ILaserTarget;
import buildcraftreborn.api.mj.IMjReadable;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.lib.engine.IEngineLikeForLedger;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.lib.tile.TileBC_Neptune;
import buildcraftreborn.lib.tile.craft.IAutoCraft;

/** What Jade and The One Probe show for a BuildCraft block, worked out on the server: what an auto crafter is making
 * and from what, and how much power a laser target is waiting for (both as in 1.12's BuildCraftCompat), plus an
 * engine's output, stored power and heat and how much power other machines hold (new). Neither mod is needed for this
 * class. */
public final class BCProbeInfo {
    private BCProbeInfo() {}

    /** One line: some text, then some items. */
    public record Line(Component text, List<ItemStack> items) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Line> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC, Line::text,
            ItemStack.OPTIONAL_LIST_STREAM_CODEC, Line::items,
            Line::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, List<Line>> LIST_STREAM_CODEC =
            STREAM_CODEC.apply(ByteBufCodecs.list(32));

        static Line of(Component text) {
            return new Line(text, List.of());
        }
    }

    /** @return The lines to show for the tile (empty if there's nothing to show). Call this on the server. */
    public static List<Line> collect(BlockEntity tile) {
        List<Line> lines = new ArrayList<>();
        if (!(tile instanceof TileBC_Neptune) || tile instanceof IPipeHolder || tile.getLevel() == null) {
            return lines;
        }
        if (tile instanceof IEngineLikeForLedger engine) {
            lines.add(Line.of(label("gui.currentOutput", LocaleUtil.localizeMjFlow(engine.getCurrentMjOutput()))));
            lines.add(Line.of(label("gui.stored", LocaleUtil.localizeMj(engine.getMjStored()))));
            lines.add(Line.of(label("gui.heat", LocaleUtil.localizeHeat(engine.getHeat()))));
        } else {
            IMjReadable readable = tile.getLevel().getCapability(MjAPI.CAP_READABLE, tile.getBlockPos(), null);
            if (readable != null && readable.getCapacity() > 0) {
                lines.add(Line.of(label("gui.stored", Component.translatable("buildcraftreborn.probe.of",
                    LocaleUtil.localizeMj(readable.getStored()), LocaleUtil.localizeMj(readable.getCapacity())))));
            }
        }
        if (tile instanceof IAutoCraft crafter && !crafter.getCurrentRecipeOutput().isEmpty()) {
            lines.add(new Line(Component.translatable("buildcraftreborn.probe.making"),
                List.of(crafter.getCurrentRecipeOutput())));
            lines.add(new Line(Component.translatable("buildcraftreborn.probe.from"),
                compact(crafter.getInvBlueprint())));
        }
        if (tile instanceof ILaserTarget target) {
            long power = target.getRequiredLaserPower();
            if (power > 0) {
                lines.add(Line.of(Component.translatable("buildcraftreborn.probe.laser",
                    LocaleUtil.localizeMj(power))));
            }
        }
        return lines;
    }

    private static Component label(String key, Component value) {
        return Component.translatable(key).append(": ").append(value);
    }

    /** @return The stacks in the inventory, with equal ones merged (and counts allowed past a stack's size). */
    static List<ItemStack> compact(IItemHandler inv) {
        List<ItemStack> stacks = new ArrayList<>();
        outer:
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            for (ItemStack existing : stacks) {
                if (ItemStack.isSameItemSameComponents(existing, stack)) {
                    existing.grow(stack.getCount());
                    continue outer;
                }
            }
            stacks.add(stack.copy());
        }
        return stacks;
    }
}
