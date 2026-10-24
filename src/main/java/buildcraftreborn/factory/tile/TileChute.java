/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.factory.tile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.inventory.IItemTransactor;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.mj.MjBattery;
import buildcraftreborn.api.mj.MjCapabilityHelper;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.factory.BCFactoryBlocks;
import buildcraftreborn.factory.block.BlockChute;
import buildcraftreborn.factory.container.ContainerChute;
import buildcraftreborn.lib.block.BlockBCBase_Neptune;
import buildcraftreborn.lib.inventory.ItemTransactorHelper;
import buildcraftreborn.lib.inventory.NoSpaceTransactor;
import buildcraftreborn.lib.inventory.TransactorEntityItem;
import buildcraftreborn.lib.misc.AdvancementUtil;
import buildcraftreborn.lib.misc.BoundingBoxUtil;
import buildcraftreborn.lib.mj.MjBatteryReceiver;
import buildcraftreborn.lib.tile.ITickable;
import buildcraftreborn.lib.tile.TileBC_Neptune;
import buildcraftreborn.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraftreborn.lib.tile.item.ItemHandlerSimple;

/** A powered hopper: it picks up items lying in front of its mouth (for free when it faces up, using MJ otherwise)
 * and pushes its contents into the inventories and storage entities on its other sides. */
public class TileChute extends TileBC_Neptune implements ITickable, IDebuggable, MenuProvider {
    private static final ResourceLocation ADVANCEMENT_DID_INSERT = BuildCraftReborn.id("retired_hopper");

    private static final int PICKUP_MAX = 3;

    public final ItemHandlerSimple inv = itemManager.addInvHandler("inv", 4, EnumAccess.INSERT, EnumPipePart.VALUES);

    private final MjBattery battery = new MjBattery(1 * MjAPI.MJ);
    private int progress = 0;

    public TileChute(BlockPos pos, BlockState state) {
        super(BCFactoryBlocks.CHUTE_TILE.get(), pos, state);
        caps.addMj(new MjCapabilityHelper(new MjBatteryReceiver(battery)));
    }

    /** @param side The side of the chute that the position is on. */
    public static boolean hasInventoryAtPosition(Level world, BlockPos pos, Direction side) {
        if (world.getBlockEntity(pos) == null) {
            return false;
        }
        return ItemTransactorHelper.getTransactor(world, pos, side.getOpposite()) != NoSpaceTransactor.INSTANCE;
    }

    private void pickupItems(Direction currentSide) {
        AABB aabb = BoundingBoxUtil.extrudeFace(getBlockPos(), currentSide, 0.25);
        int count = PICKUP_MAX;
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, aabb, Entity::isAlive)) {
            int moved = ItemTransactorHelper.move(new TransactorEntityItem(entity), inv, count);
            count -= moved;
            if (count <= 0) {
                return;
            }
        }
    }

    private void putInNearInventories(Direction currentSide) {
        boolean didWork = false;
        List<Direction> sides = new ArrayList<>(Arrays.asList(Direction.values()));
        Collections.shuffle(sides);
        sides.removeIf(Predicate.isEqual(currentSide));
        List<IItemTransactor> targets = new ArrayList<>();
        for (Direction side : sides) {
            BlockPos offset = worldPosition.relative(side);
            if (level.getBlockEntity(offset) != null) {
                targets.add(ItemTransactorHelper.getTransactor(level, offset, side.getOpposite()));
            }
        }
        for (Direction side : sides) {
            for (Entity entity : level.getEntitiesOfClass(Entity.class, new AABB(worldPosition.relative(side)))) {
                if (!(entity instanceof LivingEntity)) {
                    targets.add(ItemTransactorHelper.getTransactor(entity, side.getOpposite()));
                }
            }
        }
        for (IItemTransactor transactor : targets) {
            if (transactor == NoSpaceTransactor.INSTANCE) {
                continue;
            }
            if (ItemTransactorHelper.move(inv, transactor, 1) > 0) {
                didWork = true;
            }
        }
        if (didWork) {
            AdvancementUtil.unlockAdvancement(getOwner().getId(), ADVANCEMENT_DID_INSERT);
        }
    }

    // ITickable

    @Override
    public void update() {
        if (level.isClientSide) {
            return;
        }

        BlockState state = level.getBlockState(worldPosition);
        if (!(state.getBlock() instanceof BlockChute)) {
            return;
        }

        battery.tick(level, worldPosition);

        Direction currentSide = state.getValue(BlockBCBase_Neptune.BLOCK_FACING_6);

        int target = 100000;
        if (currentSide == Direction.UP) {
            progress += 1000; // can be free because of gravity
        }
        progress += (int) battery.extractPower(0, target - progress);
        if (progress >= target) {
            progress = 0;
            pickupItems(currentSide);
        }

        putInNearInventories(currentSide);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        progress = nbt.getInt("progress");
        battery.deserializeNBT(nbt.getCompound("battery"));
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.putInt("progress", progress);
        nbt.put("battery", battery.serializeNBT());
    }

    // GUI

    @Override
    public boolean onActivated(Player player, net.minecraft.world.InteractionHand hand,
        net.minecraft.world.item.ItemStack held) {
        if (!level.isClientSide) {
            openGui(player);
        }
        return true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        return new ContainerChute(windowId, player, this);
    }

    // IDebuggable

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        left.add("battery = " + battery.getDebugString());
        left.add("progress = " + progress);
    }
}
