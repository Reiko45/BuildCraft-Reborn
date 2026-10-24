/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.pipe.behaviour;

import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.fml.LogicalSide;
import net.minecraft.core.HolderLookup;
import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;


import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.transport.pipe.IPipe;
import buildcraftreborn.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraftreborn.api.transport.pipe.PipeBehaviour;
import buildcraftreborn.api.transport.pipe.PipeEventActionActivate;
import buildcraftreborn.api.transport.pipe.PipeEventHandler;
import buildcraftreborn.api.transport.pipe.PipeEventStatement;

import buildcraftreborn.lib.block.VanillaRotationHandlers;
import buildcraftreborn.lib.misc.EntityUtil;
import buildcraftreborn.lib.misc.NBTUtilBC;
import buildcraftreborn.lib.misc.collect.OrderedEnumMap;
import buildcraftreborn.lib.net.PacketBufferBC;

import buildcraftreborn.transport.BCTransportStatements;
import buildcraftreborn.transport.statements.ActionPipeDirection;

public abstract class PipeBehaviourDirectional extends PipeBehaviour {
    public static final OrderedEnumMap<Direction> ROTATION_ORDER = VanillaRotationHandlers.ROTATE_FACING;

    protected EnumPipePart currentDir = EnumPipePart.CENTER;

    public PipeBehaviourDirectional(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourDirectional(IPipe pipe, CompoundTag nbt, HolderLookup.Provider registries) {
        super(pipe, nbt, registries);
        setCurrentDir(NBTUtilBC.readEnum(nbt.get("currentDir"), Direction.class));
    }

    @Override
    public CompoundTag writeToNbt(HolderLookup.Provider registries) {
        CompoundTag nbt = super.writeToNbt(registries);
        nbt.put("currentDir", NBTUtilBC.writeEnum(getCurrentDir()));
        return nbt;
    }

    @Override
    public void writePayload(PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(buffer, side);
        buffer.writeEnum(currentDir);
    }

    @Override
    public void readPayload(PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx) throws IOException {
        super.readPayload(buffer, side, ctx);
        currentDir = buffer.readEnum(EnumPipePart.class);
    }

    @Override
    public boolean onPipeActivate(Player player, BlockHitResult trace, float hitX, float hitY, float hitZ,
        EnumPipePart part) {
        if (EntityUtil.getWrenchHand(player) != null) {
            EntityUtil.activateWrench(player, trace);

            if (part == EnumPipePart.CENTER) {
                return advanceFacing();
            } else if (part.face != getCurrentDir() && canFaceDirection(part.face)) {
                setCurrentDir(part.face);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onTick() {
        if (pipe.getHolder().getPipeWorld().isClientSide) {
            return;
        }

        if (!canFaceDirection(getCurrentDir())) {
            if (!advanceFacing()) {
                setCurrentDir(null);
            }
        }
    }

    protected abstract boolean canFaceDirection(Direction dir);

    /** @return True if the facing direction changed. */
    public boolean advanceFacing() {
        Direction current = currentDir.face;
        for (int i = 0; i < 6; i++) {
            current = ROTATION_ORDER.next(current);
            if (canFaceDirection(current)) {
                setCurrentDir(current);
                return true;
            }
        }
        return false;
    }

    @Nullable
    public Direction getCurrentDir() {
        return currentDir.face;
    }

    protected void setCurrentDir(Direction setTo) {
        if (this.currentDir.face == setTo) {
            return;
        }
        this.currentDir = EnumPipePart.fromFacing(setTo);
        // (No level yet while the pipe loads from NBT)
        if (pipe.getHolder().getPipeWorld() != null && !pipe.getHolder().getPipeWorld().isClientSide) {
            pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
        }
    }

    @PipeEventHandler
    public void addActions(PipeEventStatement.AddActionInternal event) {
        for (Direction face : Direction.values()) {
            if (canFaceDirection(face)) {
                event.actions.add(BCTransportStatements.ACTION_PIPE_DIRECTION[face.ordinal()]);
            }
        }
    }

    @PipeEventHandler
    public void onActionActivate(PipeEventActionActivate event) {
        if (event.action instanceof ActionPipeDirection) {
            ActionPipeDirection action = (ActionPipeDirection) event.action;
            if (canFaceDirection(action.direction)) {
                setCurrentDir(action.direction);
            }
        }
    }
}
