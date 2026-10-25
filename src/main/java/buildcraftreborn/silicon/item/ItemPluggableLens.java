/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.item;

import java.util.Optional;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import buildcraftreborn.api.transport.IItemPluggable;
import buildcraftreborn.lib.item.IItemSubtypes;
import buildcraftreborn.api.transport.pipe.IFlowItems;
import buildcraftreborn.api.transport.pipe.IPipe;
import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.api.transport.pluggable.PipePluggable;
import buildcraftreborn.lib.misc.ColourUtil;
import buildcraftreborn.lib.misc.SoundUtil;
import buildcraftreborn.silicon.BCSiliconItems;
import buildcraftreborn.silicon.BCSiliconPlugs;
import buildcraftreborn.silicon.plug.PluggableLens;

/** A lens (paints items leaving the pipe) or filter (only lets items of its colour through), clear or coloured. Only
 * fits item pipes.
 * <p>
 * 1.21: the colour and kind are the {@code lens} data component ({@link LensData}); 1.12 used 34 metadata values. */
public class ItemPluggableLens extends Item implements IItemPluggable, IItemSubtypes {
    public ItemPluggableLens(Properties properties) {
        super(properties);
    }

    public static LensData getData(ItemStack stack) {
        LensData data = stack.get(BCSiliconItems.LENS.get());
        return data == null ? LensData.CLEAR_LENS : data;
    }

    /** 1.12's order: the coloured lenses (in dye damage order, black first), the coloured filters, then the clear
     * lens and the clear filter. */
    @Override
    public void addSubtypes(Consumer<ItemStack> output) {
        for (int i = 0; i < 32; i++) {
            output.accept(getStack(DyeColor.byId(15 - (i & 15)), i >= 16));
        }
        output.accept(getStack(null, false));
        output.accept(getStack(null, true));
    }

    public ItemStack getStack(@Nullable DyeColor colour, boolean isFilter) {
        ItemStack stack = new ItemStack(this);
        stack.set(BCSiliconItems.LENS.get(), new LensData(Optional.ofNullable(colour), isFilter));
        return stack;
    }

    @Override
    public PipePluggable onPlace(ItemStack stack, IPipeHolder holder, Direction side, Player player,
        InteractionHand hand) {
        IPipe pipe = holder.getPipe();
        if (pipe == null || !(pipe.getFlow() instanceof IFlowItems)) {
            return null;
        }
        LensData data = getData(stack);
        SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos(), Blocks.STONE.defaultBlockState());
        return new PluggableLens(BCSiliconPlugs.lens, holder, side, data.colour(), data.isFilter());
    }

    @Override
    public Component getName(ItemStack stack) {
        LensData data = getData(stack);
        return ColourUtil.getColouredName(data.colourOpt().orElse(null), Component.translatable("color.clear"),
            Component.translatable(data.isFilter() ? "item.Filter.name" : "item.Lens.name"));
    }

    /** The colour (none for clear) and kind of a lens item. */
    public record LensData(Optional<DyeColor> colourOpt, boolean isFilter) {
        public static final LensData CLEAR_LENS = new LensData(Optional.empty(), false);

        public static final Codec<LensData> CODEC = RecordCodecBuilder.create(i -> i.group(
            DyeColor.CODEC.optionalFieldOf("colour").forGetter(LensData::colourOpt),
            Codec.BOOL.optionalFieldOf("filter", false).forGetter(LensData::isFilter)).apply(i, LensData::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, LensData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(DyeColor.STREAM_CODEC), LensData::colourOpt,
            ByteBufCodecs.BOOL, LensData::isFilter,
            LensData::new);

        @Nullable
        public DyeColor colour() {
            return colourOpt.orElse(null);
        }
    }
}
