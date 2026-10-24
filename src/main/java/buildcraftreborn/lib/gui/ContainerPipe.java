/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraftreborn.api.transport.pipe.IPipe;
import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.api.transport.pipe.PipeApi;
import buildcraftreborn.api.transport.pipe.PipeBehaviour;
import buildcraftreborn.lib.misc.CapUtil;

/** A menu for a pipe (its behaviour's filters, say). Tells the pipe who is looking, so it sends GUI updates. */
public abstract class ContainerPipe extends ContainerBC_Neptune {
    public final IPipeHolder pipeHolder;

    public ContainerPipe(@Nullable MenuType<?> type, int windowId, Player player, IPipeHolder pipeHolder) {
        super(type, windowId, player);
        this.pipeHolder = pipeHolder;
        if (!player.level().isClientSide) {
            pipeHolder.onPlayerOpen(player);
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        pipeHolder.onPlayerClose(player);
    }

    @Override
    public final boolean stillValid(Player player) {
        return pipeHolder.canPlayerInteract(player);
    }

    /** Reads the pipe position written when the menu was opened and finds the client's copy of that pipe's
     * behaviour.
     *
     * @throws IllegalStateException if the pipe is missing or its behaviour has the wrong type */
    public static <B extends PipeBehaviour> B readBehaviour(Inventory inventory, RegistryFriendlyByteBuf buf,
        Class<B> type) {
        BlockPos pos = buf.readBlockPos();
        BlockEntity tile = inventory.player.level().getBlockEntity(pos);
        IPipe pipe = CapUtil.getCapability(tile, PipeApi.CAP_PIPE, null);
        if (pipe != null && type.isInstance(pipe.getBehaviour())) {
            return type.cast(pipe.getBehaviour());
        }
        throw new IllegalStateException("Expected a pipe with " + type.getSimpleName() + " at " + pos + ", found "
            + tile);
    }
}
