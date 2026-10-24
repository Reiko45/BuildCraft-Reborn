/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

import buildcraftreborn.lib.BCRegistries;
import buildcraftreborn.lib.block.BlockBCBase_Neptune;
import buildcraftreborn.transport.block.BlockFilteredBuffer;
import buildcraftreborn.transport.block.BlockPipeHolder;
import buildcraftreborn.transport.container.ContainerDiamondPipe;
import buildcraftreborn.transport.container.ContainerDiamondWoodPipe;
import buildcraftreborn.transport.container.ContainerEmzuliPipe_BC8;
import buildcraftreborn.transport.container.ContainerFilteredBuffer_BC8;
import buildcraftreborn.transport.tile.TileFilteredBuffer;
import buildcraftreborn.transport.tile.TilePipeHolder;

/** Transport blocks (the pipe holder and the filtered buffer), their tile types, and the transport menus. */
public final class BCTransportBlocks {
    public static final DeferredBlock<BlockFilteredBuffer> FILTERED_BUFFER = BCRegistries.BLOCKS.register(
        "filtered_buffer", () -> new BlockFilteredBuffer(BlockBCBase_Neptune.defaultProperties()));
    /** The block every pipe is. It has no item: each pipe type has its own item that places it. Everything it drops
     * comes from the tile (see {@link BlockPipeHolder#getDrops}). */
    public static final DeferredBlock<BlockPipeHolder> PIPE_HOLDER = BCRegistries.BLOCKS.register("pipe_holder",
        () -> new BlockPipeHolder(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(0.25F, 3.0F)
            .sound(SoundType.METAL).noOcclusion().noLootTable().dynamicShape().pushReaction(PushReaction.BLOCK)
            .isRedstoneConductor((state, level, pos) -> false).isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos) -> false)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileFilteredBuffer>> FILTERED_BUFFER_TILE =
        BCRegistries.registerTile("filtered_buffer", TileFilteredBuffer::new, FILTERED_BUFFER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TilePipeHolder>> PIPE_HOLDER_TILE =
        BCRegistries.registerTile("pipe_holder", TilePipeHolder::new, PIPE_HOLDER);

    public static final DeferredHolder<MenuType<?>, MenuType<ContainerFilteredBuffer_BC8>> FILTERED_BUFFER_MENU =
        BCRegistries.MENU_TYPES.register("filtered_buffer",
            () -> IMenuTypeExtension.create(ContainerFilteredBuffer_BC8::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerDiamondPipe>> PIPE_DIAMOND_MENU =
        BCRegistries.MENU_TYPES.register("pipe_diamond",
            () -> IMenuTypeExtension.create(ContainerDiamondPipe::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerDiamondWoodPipe>> PIPE_DIAMOND_WOOD_MENU =
        BCRegistries.MENU_TYPES.register("pipe_diamond_wood",
            () -> IMenuTypeExtension.create(ContainerDiamondWoodPipe::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerEmzuliPipe_BC8>> PIPE_EMZULI_MENU =
        BCRegistries.MENU_TYPES.register("pipe_emzuli",
            () -> IMenuTypeExtension.create(ContainerEmzuliPipe_BC8::fromNetwork));

    static {
        BCRegistries.registerBlockItem(FILTERED_BUFFER);
    }

    private BCTransportBlocks() {}

    static void init() {}
}
