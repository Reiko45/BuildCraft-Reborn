/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.stripes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.transport.IStripesActivator;
import buildcraftreborn.api.transport.IWireManager;
import buildcraftreborn.api.transport.pipe.IItemPipe;
import buildcraftreborn.api.transport.pipe.IPipe;
import buildcraftreborn.api.transport.pipe.IPipeExtensionManager;
import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.api.transport.pipe.PipeApi;
import buildcraftreborn.api.transport.pipe.PipeBehaviour;
import buildcraftreborn.api.transport.pipe.PipeDefinition;
import buildcraftreborn.lib.misc.BlockUtil;
import buildcraftreborn.lib.misc.CapUtil;
import buildcraftreborn.lib.misc.FakePlayerProvider;
import buildcraftreborn.lib.misc.InventoryUtil;
import buildcraftreborn.lib.misc.SoundUtil;
import buildcraftreborn.transport.pipe.behaviour.PipeBehaviourStripes;
import buildcraftreborn.transport.wire.WireManager;

/** Extends (or, with a void pipe, retracts) stripes pipes: a pipe item sent into a stripes pipe moves the stripes pipe
 * one block forward and places the new pipe where it was. Requests are carried out at the end of the level tick.
 * <p>
 * 1.21: the stripes pipe is moved by saving its tile and loading that into the tile placed at the new position (1.12
 * did the same with block snapshots). Every placement and removal still fires NeoForge's place and break events for
 * the pipe owner's fake player, so claim mods can stop it. */
public enum PipeExtensionManager implements IPipeExtensionManager {
    INSTANCE;

    private final Map<ResourceKey<Level>, List<PipeExtensionRequest>> requests = new HashMap<>();
    private final Set<PipeDefinition> retractionPipeDefs = new HashSet<>();

    @Override
    public boolean requestPipeExtension(Level level, BlockPos pos, Direction dir, IStripesActivator stripes,
        ItemStack stack) {
        if (level.isClientSide || stack.isEmpty() || !(stack.getItem() instanceof IItemPipe itemPipe)) {
            return false;
        }
        List<PipeExtensionRequest> rList = requests.computeIfAbsent(level.dimension(), k -> new ArrayList<>());
        return rList.add(new PipeExtensionRequest(pos, dir, stripes, itemPipe.getDefinition(), stack.copy()));
    }

    @Override
    public void registerRetractionPipe(PipeDefinition pipeDefinition) {
        if (pipeDefinition != null) {
            retractionPipeDefs.add(pipeDefinition);
        }
    }

    @SubscribeEvent
    public void tick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        List<PipeExtensionRequest> rList = requests.get(level.dimension());
        if (rList == null || rList.isEmpty()) {
            return;
        }
        List<PipeExtensionRequest> toProcess = new ArrayList<>(rList);
        rList.clear();
        for (PipeExtensionRequest r : toProcess) {
            if (retractionPipeDefs.contains(r.pipeDef)) {
                retract(level, r);
            } else {
                extend(level, r);
            }
        }
    }

    private static void retract(ServerLevel w, PipeExtensionRequest r) {
        Direction retractDir = r.dir.getOpposite();
        if (!isValidRetractionPath(w, r, retractDir)) {
            // check other directions
            List<Direction> possible = new ArrayList<>();
            for (Direction facing : Direction.values()) {
                if (facing.getAxis() != r.dir.getAxis() && isValidRetractionPath(w, r, facing)) {
                    possible.add(facing);
                }
            }
            if (possible.isEmpty()) {
                r.stripes.sendItem(r.stack.copy(), r.dir);
                return;
            }
            retractDir = possible.get(w.random.nextInt(possible.size()));
        }
        BlockPos p = r.pos.relative(retractDir);

        NonNullList<ItemStack> stacksToSendBack = NonNullList.create();
        // Always send back the catalyst pipe
        stacksToSendBack.add(r.stack);

        // Step 1: Copy the existing stripes pipe
        BlockState stripesState = w.getBlockState(r.pos);
        BlockEntity stripesTileOld = w.getBlockEntity(r.pos);
        IPipeHolder holder = CapUtil.getCapability(stripesTileOld, PipeApi.CAP_PIPE_HOLDER, null);
        if (stripesTileOld == null || holder == null || holder.getPipe() == null) {
            BuildCraftReborn.LOGGER.warn("Found an invalid request at " + r.pos + " as " + stripesTileOld
                + " was not a pipe tile!");
            return;
        }
        GameProfile owner = holder.getOwner();
        PipeBehaviour behaviour = holder.getPipe().getBehaviour();
        if (behaviour instanceof PipeBehaviourStripes stripesBehaviour) {
            stripesBehaviour.direction = retractDir.getOpposite();
        }
        CompoundTag stripesNbt = stripesTileOld.saveWithoutMetadata(w.registryAccess());

        // Step 2: Remove the previous pipe
        BlockSnapshot previousPipe = BlockSnapshot.create(w.dimension(), w, p);
        BlockState previousState = w.getBlockState(p);
        Optional<List<ItemStack>> drops = BlockUtil.breakBlockAndGetDrops(w, p, ItemStack.EMPTY, owner);
        boolean canceled = drops.isEmpty();

        // Step 3: Place the stripes pipe there and remove the old one
        if (!canceled) {
            FakePlayer player = FakePlayerProvider.INSTANCE.getFakePlayer(w, owner, p);
            player.getInventory().clearContent();
            BlockSnapshot placed = BlockSnapshot.create(w.dimension(), w, p);
            w.setBlock(p, stripesState, Block.UPDATE_ALL);
            if (EventHooks.onBlockPlace(player, placed, r.dir)) {
                // Put the previous pipe back (its drops are thrown away, as they're in the restored pipe)
                canceled = true;
                previousPipe.restore(Block.UPDATE_ALL);
            } else {
                SoundUtil.playBlockBreak(w, p, previousState);
                if (!canRemove(w, r.pos, player)) {
                    canceled = true;
                    previousPipe.restore(Block.UPDATE_ALL);
                } else {
                    loadInto(w, p, stripesNbt);
                    // The stripes pipe moved: its old block goes (without drops)
                    w.setBlock(r.pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    stacksToSendBack.addAll(drops.get());
                }
            }
        }

        // Step 4: Hope for the best, clean up.
        cleanup(w, r, p, stacksToSendBack, canceled);
    }

    private static void extend(ServerLevel w, PipeExtensionRequest r) {
        BlockPos p = r.pos.relative(r.dir);
        BlockState atP = w.getBlockState(p);
        if (!atP.isAir() && !atP.canBeReplaced()) {
            r.stripes.sendItem(r.stack.copy(), r.dir);
            return;
        }

        NonNullList<ItemStack> stacksToSendBack = NonNullList.create();

        // Step 1: Copy and remove the existing stripes pipe
        BlockState stripesState = w.getBlockState(r.pos);
        BlockEntity stripesTileOld = w.getBlockEntity(r.pos);
        IPipeHolder holder = CapUtil.getCapability(stripesTileOld, PipeApi.CAP_PIPE_HOLDER, null);
        if (stripesTileOld == null || holder == null) {
            BuildCraftReborn.LOGGER.warn("Found an invalid request at " + r.pos + " as " + stripesTileOld
                + " was not a pipe tile!");
            return;
        }
        GameProfile owner = holder.getOwner();
        CompoundTag stripesNbt = stripesTileOld.saveWithoutMetadata(w.registryAccess());
        FakePlayer player = FakePlayerProvider.INSTANCE.getFakePlayer(w, owner, r.pos);
        boolean canceled = !canRemove(w, r.pos, player);
        if (canceled) {
            stacksToSendBack.add(r.stack);
        } else {
            w.setBlock(r.pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }

        NonNullList<ItemStack> list = NonNullList.create();

        // Step 2: Place the new pipe
        if (!canceled) {
            player.getInventory().clearContent();
            player.setItemInHand(InteractionHand.MAIN_HAND, r.stack.copy());
            ItemStack held = player.getMainHandItem();
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(r.pos), r.dir.getOpposite(), r.pos, false);
            InteractionResult result = held.useOn(new UseOnContext(w, player, InteractionHand.MAIN_HAND, held, hit));
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().removeItemNoUpdate(i);
                if (!stack.isEmpty()) {
                    list.add(stack);
                }
            }
            if (!result.consumesAction()) {
                canceled = true;
                restoreStripes(w, r.pos, stripesState, stripesNbt);
            }
        }

        // Step 3: Place the stripes pipe in front
        if (!canceled) {
            player = FakePlayerProvider.INSTANCE.getFakePlayer(w, owner, p);
            player.getInventory().clearContent();
            BlockSnapshot placed = BlockSnapshot.create(w.dimension(), w, p);
            w.setBlock(p, stripesState, Block.UPDATE_ALL);
            if (EventHooks.onBlockPlace(player, placed, r.dir.getOpposite())) {
                canceled = true;
                stacksToSendBack.add(r.stack);
                placed.restore(Block.UPDATE_ALL);
                // The new pipe goes again: the stripes pipe takes its place back
                restoreStripes(w, r.pos, stripesState, stripesNbt);
            } else {
                loadInto(w, p, stripesNbt);
                stacksToSendBack.addAll(list);
            }
        } else {
            stacksToSendBack.addAll(list);
        }

        // Step 4: Hope for the best, clean up.
        cleanup(w, r, p, stacksToSendBack, canceled);
    }

    private static boolean canRemove(ServerLevel w, BlockPos pos, FakePlayer player) {
        return !CommonHooks.fireBlockBreak(w, GameType.SURVIVAL, player, pos, w.getBlockState(pos)).isCanceled();
    }

    /** Puts the stripes pipe back where it was (replacing anything there without drops). */
    private static void restoreStripes(ServerLevel w, BlockPos pos, BlockState state, CompoundTag nbt) {
        w.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        w.setBlock(pos, state, Block.UPDATE_ALL);
        loadInto(w, pos, nbt);
    }

    private static void loadInto(ServerLevel w, BlockPos pos, CompoundTag nbt) {
        BlockEntity tile = w.getBlockEntity(pos);
        if (tile != null) {
            tile.loadWithComponents(nbt, w.registryAccess());
            tile.setChanged();
            w.sendBlockUpdated(pos, tile.getBlockState(), tile.getBlockState(), Block.UPDATE_ALL);
        }
    }

    private static void cleanup(ServerLevel w, PipeExtensionRequest r, BlockPos p,
        NonNullList<ItemStack> stacksToSendBack, boolean canceled) {
        BlockEntity stripesTileNew = w.getBlockEntity(canceled ? r.pos : p);
        IPipeHolder stripesPipeHolderNew = CapUtil.getCapability(stripesTileNew, PipeApi.CAP_PIPE_HOLDER, null);
        if (stripesPipeHolderNew == null || stripesPipeHolderNew.getPipe() == null) {
            InventoryUtil.dropAll(w, p, stacksToSendBack);
            return;
        }
        if (!canceled) {
            IWireManager wireManager = stripesPipeHolderNew.getWireManager();
            if (wireManager instanceof WireManager manager) {
                manager.getWireSystems().rebuildWireSystemsAround(stripesPipeHolderNew);
            }
        }
        PipeBehaviour behaviour = stripesPipeHolderNew.getPipe().getBehaviour();
        if (behaviour instanceof IStripesActivator stripesNew) {
            for (ItemStack s : stacksToSendBack) {
                s = s.copy();
                if (!stripesNew.sendItem(s, r.dir)) {
                    stripesNew.dropItem(s, r.dir);
                }
            }
        } else {
            InventoryUtil.dropAll(w, p, stacksToSendBack);
        }
    }

    private static boolean isValidRetractionPath(Level w, PipeExtensionRequest r, Direction retractDir) {
        BlockEntity tile = w.getBlockEntity(r.pos.relative(retractDir));
        IPipe pipe = CapUtil.getCapability(tile, PipeApi.CAP_PIPE, null);
        if (pipe == null) {
            return false;
        }
        boolean connected = false;
        for (Direction facing : Direction.values()) {
            IPipe.ConnectedType type = pipe.getConnectedType(facing);
            if (type == IPipe.ConnectedType.TILE) {
                return false;
            }
            if (facing == retractDir.getOpposite()) {
                if (type != IPipe.ConnectedType.PIPE) {
                    return false;
                }
            } else if (type != null) {
                if (connected) {
                    return false;
                }
                connected = true;
            }
        }
        return true;
    }

    private record PipeExtensionRequest(BlockPos pos, Direction dir, IStripesActivator stripes, PipeDefinition pipeDef,
        ItemStack stack) {}
}
