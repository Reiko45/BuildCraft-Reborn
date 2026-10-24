/*
 * Based on BuildCraftCompat, (c) the BuildCraft Team, http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraftCompat is distributed under the terms of the Minecraft Mod Public License 1.0.1, or MMPL (a copy is in
 * LICENSE-ASSETS).
 */
package buildcraftreborn.compat.jade;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.compat.BCProbeInfo;
import buildcraftreborn.compat.BCProbeInfo.Line;
import buildcraftreborn.lib.tile.TileBC_Neptune;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

/** Jade support (1.12: Hwyla): shows {@link BCProbeInfo}'s lines for BuildCraft machines. Jade finds this class by
 * its annotation, and only loads it when Jade is installed. */
@WailaPlugin(BuildCraftReborn.MODID)
public class BCPluginJade implements IWailaPlugin {
    static final ResourceLocation UID = BuildCraftReborn.id("info");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(InfoProvider.INSTANCE, TileBC_Neptune.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(InfoProvider.INSTANCE, Block.class);
    }

    enum InfoProvider implements IBlockComponentProvider, StreamServerDataProvider<BlockAccessor, List<Line>> {
        INSTANCE;

        @Override
        public ResourceLocation getUid() {
            return UID;
        }

        @Override
        @Nullable
        public List<Line> streamData(BlockAccessor accessor) {
            List<Line> lines = BCProbeInfo.collect(accessor.getBlockEntity());
            return lines.isEmpty() ? null : lines;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, List<Line>> streamCodec() {
            return Line.LIST_STREAM_CODEC;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            decodeFromData(accessor).ifPresent(lines -> {
                IElementHelper elements = IElementHelper.get();
                for (Line line : lines) {
                    tooltip.add(line.text());
                    for (ItemStack stack : line.items()) {
                        tooltip.append(elements.smallItem(stack));
                    }
                }
            });
        }
    }
}
