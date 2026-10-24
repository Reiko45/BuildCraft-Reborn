/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.EventHooks;

import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.lib.misc.BlockUtil;
import buildcraftreborn.lib.misc.FakePlayerProvider;
import buildcraftreborn.lib.misc.NBTUtilBC;
import buildcraftreborn.robotics.RobotUtils;

/** Breaks a block with the held tool, as fast as a player with that tool would. */
public class AIRobotBreak extends AIRobot {
    /** 7.1's {@code BuilderAPI.BREAK_ENERGY}: 160 RF. */
    public static final long BREAK_POWER = 16 * MjAPI.MJ;

    private BlockPos blockToBreak;
    private float blockDamage = 0;

    private BlockState state;
    private float hardness;
    private float speed;

    public AIRobotBreak(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotBreak(EntityRobotBase iRobot, BlockPos iBlockToBreak) {
        this(iRobot);

        blockToBreak = iBlockToBreak;
    }

    @Override
    public void start() {
        if (blockToBreak == null) {
            return;
        }
        robot.aimItemAt(blockToBreak);

        robot.setItemActive(true);
        state = robot.level().getBlockState(blockToBreak);
        hardness = state.getDestroySpeed(robot.level(), blockToBreak);
        speed = getBreakSpeed(robot, robot.getMainHandItem(), state, blockToBreak);
    }

    @Override
    public void update() {
        if (blockToBreak == null || !(robot.level() instanceof ServerLevel world)) {
            setSuccess(false);
            terminate();
            return;
        }
        if (state == null) {
            state = world.getBlockState(blockToBreak);
            if (state.isAir()) {
                setSuccess(false);
                terminate();
                return;
            }
            hardness = state.getDestroySpeed(world, blockToBreak);
            speed = getBreakSpeed(robot, robot.getMainHandItem(), state, blockToBreak);
        }

        if (world.getBlockState(blockToBreak).isAir() || hardness < 0) {
            setSuccess(false);
            terminate();
            return;
        }

        if (hardness != 0) {
            blockDamage += speed / hardness / 30F;
        } else {
            // Instantly break the block
            blockDamage = 1.1F;
        }

        if (blockDamage > 1.0F) {
            world.destroyBlockProgress(robot.getId(), blockToBreak, -1);
            blockDamage = 0;

            ItemStack held = robot.getMainHandItem();
            if (BlockUtil.harvestBlock(world, blockToBreak, held, FakePlayerProvider.NULL_PROFILE)) {
                world.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, blockToBreak, Block.getId(state));

                if (!held.isEmpty()) {
                    held.getItem().mineBlock(held, world, state, blockToBreak, robot);
                    robot.setItemInUse(held.isEmpty() ? ItemStack.EMPTY : held);
                }
            } else {
                setSuccess(false);
            }

            terminate();
        } else {
            world.destroyBlockProgress(robot.getId(), blockToBreak, (int) (blockDamage * 10.0F) - 1);
        }
    }

    @Override
    public void end() {
        robot.setItemActive(false);
        if (blockToBreak != null) {
            robot.level().destroyBlockProgress(robot.getId(), blockToBreak, -1);
        }
    }

    private float getBreakSpeed(EntityRobotBase robot, ItemStack usingItem, BlockState state, BlockPos pos) {
        float f = usingItem.isEmpty() ? 1.0F : usingItem.getDestroySpeed(state);

        if (f > 1.0F) {
            int i = EnchantmentHelper.getItemEnchantmentLevel(robot.level().registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.EFFICIENCY), usingItem);

            if (i > 0) {
                f += i * i + 1;
            }
        }

        if (robot.level() instanceof ServerLevel) {
            FakePlayer player = RobotUtils.getFakePlayer(robot, pos);
            player.setItemInHand(InteractionHand.MAIN_HAND, usingItem);
            f = EventHooks.getBreakSpeed(player, state, f, pos);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        return f < 0 ? 0 : f;
    }

    @Override
    public long getPowerCost() {
        // 7.1: ceil(BREAK_ENERGY * 2 / 30) RF per tick
        return BREAK_POWER * 2 / 30;
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        super.writeSelfToNBT(nbt);

        if (blockToBreak != null) {
            nbt.put("blockToBreak", NBTUtilBC.writeBlockPos(blockToBreak));
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        super.loadSelfFromNBT(nbt);

        if (nbt.contains("blockToBreak")) {
            blockToBreak = NBTUtilBC.readBlockPos(nbt.get("blockToBreak"));
        }
    }
}
