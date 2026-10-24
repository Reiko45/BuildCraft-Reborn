/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraftreborn.lib.tile.item.ItemHandlerSimple;
import buildcraftreborn.lib.tile.item.StackInsertionFunction;
import buildcraftreborn.robotics.BCRoboticsBlocks;
import buildcraftreborn.robotics.container.ContainerChargingTable;
import buildcraftreborn.robotics.item.ItemRobot;
import buildcraftreborn.silicon.tile.TileLaserTableBase;

/** The charging table: lasers charge the item in it. That's a programmed robot (in MJ), or any item that stores Forge
 * Energy (7.1: RF items), at the usual 10 RF to 1 MJ. */
public class TileChargingTable extends TileLaserTableBase implements MenuProvider {
    public final ItemHandlerSimple inv = itemManager.addInvHandler("inv", 1,
        (slot, stack) -> canCharge(stack), StackInsertionFunction.getInsertionFunction(1), EnumAccess.BOTH,
        EnumPipePart.VALUES);

    public TileChargingTable(BlockPos pos, BlockState state) {
        super(BCRoboticsBlocks.CHARGING_TABLE_TILE.get(), pos, state);
    }

    /** @return True if the item stores power that this table can fill (whether or not it's full). */
    public static boolean canCharge(ItemStack stack) {
        if (stack.getItem() instanceof ItemRobot) {
            return ItemRobot.hasBoard(stack);
        }
        IEnergyStorage storage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        return storage != null && storage.canReceive();
    }

    @Override
    public long getTarget() {
        ItemStack stack = inv.getStackInSlot(0);
        if (stack.isEmpty()) {
            return 0;
        }
        if (stack.getItem() instanceof ItemRobot) {
            return ItemRobot.hasBoard(stack) ? EntityRobotBase.MAX_POWER - ItemRobot.getPower(stack) : 0;
        }
        IEnergyStorage storage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (storage == null || !storage.canReceive()) {
            return 0;
        }
        long missing = Math.max(0, (long) storage.getMaxEnergyStored() - storage.getEnergyStored());
        return missing * MjAPI.getRfConversion().mjPerRf;
    }

    @Override
    public void update() {
        super.update();
        if (level.isClientSide || power <= 0) {
            return;
        }
        ItemStack stack = inv.getStackInSlot(0).copy();
        if (stack.getItem() instanceof ItemRobot) {
            power = ItemRobot.receivePower(stack, power, false);
        } else {
            IEnergyStorage storage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
            if (storage == null) {
                return;
            }
            long mjPerRf = MjAPI.getRfConversion().mjPerRf;
            int offered = (int) Math.min(Integer.MAX_VALUE, power / mjPerRf);
            if (offered <= 0) {
                return;
            }
            power -= storage.receiveEnergy(offered, false) * mjPerRf;
        }
        inv.setStackInSlot(0, stack);
    }

    // GUI

    @Override
    public boolean onActivated(Player player, InteractionHand hand, ItemStack held) {
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
        return new ContainerChargingTable(windowId, player, this);
    }
}
