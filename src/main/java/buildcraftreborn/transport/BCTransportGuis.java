/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.Item;

import buildcraftreborn.api.transport.pipe.IPipe;
import buildcraftreborn.api.transport.pipe.PipeApi;
import buildcraftreborn.lib.misc.CapUtil;
import buildcraftreborn.transport.container.ContainerDiamondPipe;
import buildcraftreborn.transport.container.ContainerDiamondWoodPipe;
import buildcraftreborn.transport.container.ContainerEmzuliPipe_BC8;
import buildcraftreborn.transport.pipe.behaviour.PipeBehaviourDiamond;
import buildcraftreborn.transport.pipe.behaviour.PipeBehaviourEmzuli;
import buildcraftreborn.transport.pipe.behaviour.PipeBehaviourWoodDiamond;

/** The pipe GUIs. The filtered buffer opens its own (it's a normal tile).
 * <p>
 * 1.21: opening sends the pipe position, and each menu finds the pipe behaviour from it on the client. */
public enum BCTransportGuis {
    PIPE_DIAMOND,
    PIPE_DIAMOND_WOOD,
    PIPE_EMZULI;

    public void openGui(Player player, BlockPos pos) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        IPipe pipe = CapUtil.getCapability(player.level().getBlockEntity(pos), PipeApi.CAP_PIPE, null);
        if (pipe == null) {
            return;
        }
        MenuConstructor constructor = switch (this) {
            case PIPE_DIAMOND -> pipe.getBehaviour() instanceof PipeBehaviourDiamond b
                ? (id, inv, p) -> new ContainerDiamondPipe(id, p, b) : null;
            case PIPE_DIAMOND_WOOD -> pipe.getBehaviour() instanceof PipeBehaviourWoodDiamond b
                ? (id, inv, p) -> new ContainerDiamondWoodPipe(id, p, b) : null;
            case PIPE_EMZULI -> pipe.getBehaviour() instanceof PipeBehaviourEmzuli b
                ? (id, inv, p) -> new ContainerEmzuliPipe_BC8(id, p, b) : null;
        };
        if (constructor == null) {
            return;
        }
        Component title = PipeApi.pipeRegistry.getItemForPipe(pipe.getDefinition()) instanceof Item item
            ? Component.translatable(item.getDescriptionId()) : Component.empty();
        serverPlayer.openMenu(new SimpleMenuProvider(constructor, title), buf -> buf.writeBlockPos(pos));
    }
}
