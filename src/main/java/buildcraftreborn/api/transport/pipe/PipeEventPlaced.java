/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.transport.pipe;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Called in
 * {@link Block#onBlockPlacedBy(net.minecraft.world.Level, net.minecraft.util.math.BlockPos, net.minecraft.block.state.BlockState, LivingEntity, ItemStack)} */
public class PipeEventPlaced extends PipeEvent {

    public final LivingEntity placer;
    public final ItemStack placeStack;

    public PipeEventPlaced(IPipeHolder holder, LivingEntity placer, ItemStack placeStack) {
        super(holder);
        this.placer = placer;
        this.placeStack = placeStack;
    }
}
