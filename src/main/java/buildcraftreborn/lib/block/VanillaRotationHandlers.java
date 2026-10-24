/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.block;

import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.GlazedTerracottaBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;
import net.minecraft.world.level.block.RodBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import buildcraftreborn.api.blocks.CustomRotationHelper;
import buildcraftreborn.api.blocks.ICustomRotationHandler;
import buildcraftreborn.lib.misc.collect.OrderedEnumMap;

/** Wrench rotation for vanilla blocks.
 * <p>
 * 1.21 differences from 1.12: buttons and levers share the face-attached (floor/wall/ceiling) rotation; floor
 * torches are separate blocks from wall torches, so only wall torches rotate (around their wall); skulls, standing
 * signs and banners turn by one of their 16 steps; double chests swap their left/right halves when flipped; barrels
 * and lightning rods rotate freely like dispensers and end rods. */
public class VanillaRotationHandlers {
    /* Player friendly rotations- these only rotate through sides that are touching (only 90 degree changes, in any
     * axis), rather than jumping around. */
    public static final OrderedEnumMap<Direction> ROTATE_HORIZONTAL, ROTATE_FACING, ROTATE_TORCH, ROTATE_HOPPER;

    /** Face-attached orientations (buttons and levers), in 1.12's lever order. */
    private static final AttachFace[] LEVER_FACES;
    private static final Direction[] LEVER_FACINGS;

    static {
        Direction e = Direction.EAST, w = Direction.WEST;
        Direction u = Direction.UP, d = Direction.DOWN;
        Direction n = Direction.NORTH, s = Direction.SOUTH;
        ROTATE_HORIZONTAL = new OrderedEnumMap<>(Direction.class, e, s, w, n);
        ROTATE_FACING = new OrderedEnumMap<>(Direction.class, e, s, d, w, n, u);
        ROTATE_TORCH = new OrderedEnumMap<>(Direction.class, e, s, w, n, u);
        ROTATE_HOPPER = new OrderedEnumMap<>(Direction.class, e, s, w, n, d);

        LEVER_FACES = new AttachFace[8];
        LEVER_FACINGS = new Direction[8];
        int index = 0;
        for (Direction face : ROTATE_FACING.getOrder()) {
            if (face == Direction.DOWN) {
                // 1.12 DOWN_Z, DOWN_X: a lever on a ceiling pointing along Z, then along X
                LEVER_FACES[index] = AttachFace.CEILING;
                LEVER_FACINGS[index++] = Direction.NORTH;
                LEVER_FACES[index] = AttachFace.CEILING;
                LEVER_FACINGS[index++] = Direction.EAST;
            } else if (face == Direction.UP) {
                LEVER_FACES[index] = AttachFace.FLOOR;
                LEVER_FACINGS[index++] = Direction.NORTH;
                LEVER_FACES[index] = AttachFace.FLOOR;
                LEVER_FACINGS[index++] = Direction.EAST;
            } else {
                LEVER_FACES[index] = AttachFace.WALL;
                LEVER_FACINGS[index++] = face;
            }
        }
    }

    /** Called from common setup, once every block is registered. */
    public static void fmlInit() {
        CustomRotationHelper rh = CustomRotationHelper.INSTANCE;
        rh.registerHandlerForAll(ButtonBlock.class, VanillaRotationHandlers::rotateFaceAttached);
        rh.registerHandlerForAll(LeverBlock.class, VanillaRotationHandlers::rotateFaceAttached);
        rh.registerHandlerForAll(TripWireHookBlock.class,
            surviving(TripWireHookBlock.class, TripWireHookBlock.FACING, ROTATE_HORIZONTAL));
        rh.registerHandlerForAll(DoorBlock.class, VanillaRotationHandlers::rotateDoor);
        rh.registerHandlerForAll(PistonBaseBlock.class, VanillaRotationHandlers::rotatePiston);
        rh.registerHandlerForAll(ShulkerBoxBlock.class, freely(ShulkerBoxBlock.class, ShulkerBoxBlock.FACING));
        rh.registerHandlerForAll(DispenserBlock.class, freely(DispenserBlock.class, DispenserBlock.FACING));
        rh.registerHandlerForAll(ObserverBlock.class, freely(ObserverBlock.class, ObserverBlock.FACING));
        rh.registerHandlerForAll(RodBlock.class, freely(RodBlock.class, RodBlock.FACING));
        rh.registerHandlerForAll(BarrelBlock.class, freely(BarrelBlock.class, BarrelBlock.FACING));
        rh.registerHandlerForAll(FenceGateBlock.class, horizontally(FenceGateBlock.class));
        rh.registerHandlerForAll(DiodeBlock.class, horizontally(DiodeBlock.class));
        rh.registerHandlerForAll(CarvedPumpkinBlock.class, horizontally(CarvedPumpkinBlock.class));
        rh.registerHandlerForAll(GlazedTerracottaBlock.class, horizontally(GlazedTerracottaBlock.class));
        rh.registerHandlerForAll(AnvilBlock.class, horizontally(AnvilBlock.class));
        rh.registerHandlerForAll(EnderChestBlock.class, horizontally(EnderChestBlock.class));
        rh.registerHandlerForAll(AbstractFurnaceBlock.class, horizontally(AbstractFurnaceBlock.class));
        rh.registerHandlerForAll(CocoaBlock.class, surviving(CocoaBlock.class, CocoaBlock.FACING, ROTATE_HORIZONTAL));
        rh.registerHandlerForAll(WallTorchBlock.class,
            surviving(WallTorchBlock.class, WallTorchBlock.FACING, ROTATE_HORIZONTAL));
        rh.registerHandlerForAll(RedstoneWallTorchBlock.class,
            surviving(RedstoneWallTorchBlock.class, RedstoneWallTorchBlock.FACING, ROTATE_HORIZONTAL));
        rh.registerHandlerForAll(LadderBlock.class, surviving(LadderBlock.class, LadderBlock.FACING, ROTATE_HORIZONTAL));
        rh.registerHandlerForAll(HopperBlock.class, VanillaRotationHandlers::rotateHopper);
        rh.registerHandlerForAll(ChestBlock.class, VanillaRotationHandlers::rotateChest);
        rh.registerHandlerForAll(TrapDoorBlock.class, VanillaRotationHandlers::rotateTrapDoor);
        rh.registerHandlerForAll(StairBlock.class, VanillaRotationHandlers::rotateStairs);
        rh.registerHandlerForAll(WallSkullBlock.class, horizontally(WallSkullBlock.class));
        rh.registerHandlerForAll(SkullBlock.class, rotation16(SkullBlock.class, SkullBlock.ROTATION));
        rh.registerHandlerForAll(WallBannerBlock.class,
            surviving(WallBannerBlock.class, WallBannerBlock.FACING, ROTATE_HORIZONTAL));
        rh.registerHandlerForAll(WallSignBlock.class, surviving(WallSignBlock.class, WallSignBlock.FACING, ROTATE_HORIZONTAL));
        rh.registerHandlerForAll(BannerBlock.class, rotation16(BannerBlock.class, BannerBlock.ROTATION));
        rh.registerHandlerForAll(StandingSignBlock.class, rotation16(StandingSignBlock.class, StandingSignBlock.ROTATION));
    }

    private static InteractionResult rotateDoor(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (!(state.getBlock() instanceof DoorBlock)) {
            return InteractionResult.PASS;
        }
        BlockPos upperPos, lowerPos;
        BlockState upperState, lowerState;
        if (state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            upperPos = pos;
            upperState = state;
            lowerPos = upperPos.below();
            lowerState = world.getBlockState(lowerPos);
            if (!(lowerState.getBlock() instanceof DoorBlock)) {
                return InteractionResult.PASS;
            }
        } else {
            lowerPos = pos;
            lowerState = state;
            upperPos = lowerPos.above();
            upperState = world.getBlockState(upperPos);
            if (!(upperState.getBlock() instanceof DoorBlock)) {
                return InteractionResult.PASS;
            }
        }
        // 1.21 doors store every property on both halves, so both are updated together.
        Direction facing = ROTATE_HORIZONTAL.next(lowerState.getValue(DoorBlock.FACING));
        DoorHingeSide hinge = lowerState.getValue(DoorBlock.HINGE);
        if (lowerState.getValue(DoorBlock.FACING) == ROTATE_HORIZONTAL.get(0)) {
            hinge = hinge == DoorHingeSide.LEFT ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT;
        }
        int flags = 2 | 16; // send to clients, don't drop the other half while it's half-updated
        world.setBlock(lowerPos, lowerState.setValue(DoorBlock.FACING, facing).setValue(DoorBlock.HINGE, hinge), flags);
        world.setBlock(upperPos, upperState.setValue(DoorBlock.FACING, facing).setValue(DoorBlock.HINGE, hinge), flags);
        return InteractionResult.SUCCESS;
    }

    /** Buttons and levers: walls in 1.12's facing order, then the ceiling and floor along Z and X. */
    private static InteractionResult rotateFaceAttached(Level world, BlockPos pos, BlockState state,
        Direction sideWrenched) {
        if (!state.hasProperty(BlockStateProperties.ATTACH_FACE)
            || !state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return InteractionResult.PASS;
        }
        AttachFace face = state.getValue(BlockStateProperties.ATTACH_FACE);
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int current = 0;
        for (int i = 0; i < LEVER_FACES.length; i++) {
            if (LEVER_FACES[i] == face && (face == AttachFace.WALL ? LEVER_FACINGS[i] == facing
                : LEVER_FACINGS[i].getAxis() == facing.getAxis())) {
                current = i;
                break;
            }
        }
        for (int i = 1; i < LEVER_FACES.length; i++) {
            int index = (current + i) % LEVER_FACES.length;
            BlockState toTry = state.setValue(BlockStateProperties.ATTACH_FACE, LEVER_FACES[index])
                .setValue(BlockStateProperties.HORIZONTAL_FACING, LEVER_FACINGS[index]);
            if (toTry.canSurvive(world, pos)) {
                world.setBlockAndUpdate(pos, toTry);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }

    private static InteractionResult rotatePiston(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof PistonBaseBlock) {
            if (state.getValue(PistonBaseBlock.EXTENDED)) return InteractionResult.FAIL;
            return rotateOnce(world, pos, state, PistonBaseBlock.FACING, ROTATE_FACING);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateHopper(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof HopperBlock) {
            return rotateOnce(world, pos, state, HopperBlock.FACING, ROTATE_HOPPER);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateChest(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (!(state.getBlock() instanceof ChestBlock)) {
            return InteractionResult.PASS;
        }
        ChestType type = state.getValue(ChestBlock.TYPE);
        if (type != ChestType.SINGLE) {
            // A double chest: turn both halves around. Left and right swap because they're relative to the facing.
            BlockPos otherPos = pos.relative(ChestBlock.getConnectedDirection(state));
            BlockState otherState = world.getBlockState(otherPos);
            if (otherState.getBlock() == state.getBlock()) {
                Direction facing = state.getValue(ChestBlock.FACING).getOpposite();
                world.setBlock(pos, state.setValue(ChestBlock.FACING, facing)
                    .setValue(ChestBlock.TYPE, type.getOpposite()), 2 | 16);
                world.setBlock(otherPos, otherState.setValue(ChestBlock.FACING, facing)
                    .setValue(ChestBlock.TYPE, otherState.getValue(ChestBlock.TYPE).getOpposite()), 2 | 16);
                return InteractionResult.SUCCESS;
            }
        }
        return rotateOnce(world, pos, state, ChestBlock.FACING, ROTATE_HORIZONTAL);
    }

    private static InteractionResult rotateTrapDoor(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof TrapDoorBlock) {
            if (state.getValue(TrapDoorBlock.FACING) == ROTATE_HORIZONTAL.get(0)) {
                Half half = state.getValue(TrapDoorBlock.HALF);
                state = state.setValue(TrapDoorBlock.HALF, half == Half.TOP ? Half.BOTTOM : Half.TOP);
            }
            return rotateOnce(world, pos, state, TrapDoorBlock.FACING, ROTATE_HORIZONTAL);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateStairs(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof StairBlock) {
            if (state.getValue(StairBlock.FACING) == ROTATE_HORIZONTAL.get(0)) {
                Half half = state.getValue(StairBlock.HALF);
                state = state.setValue(StairBlock.HALF, half == Half.TOP ? Half.BOTTOM : Half.TOP);
            }
            return rotateOnce(world, pos, state, StairBlock.FACING, ROTATE_HORIZONTAL);
        }
        return InteractionResult.PASS;
    }

    private static ICustomRotationHandler freely(Class<?> blockClass, Property<Direction> prop) {
        return (world, pos, state, side) -> blockClass.isInstance(state.getBlock()) && state.hasProperty(prop)
            ? rotateOnce(world, pos, state, prop, ROTATE_FACING) : InteractionResult.PASS;
    }

    private static ICustomRotationHandler horizontally(Class<?> blockClass) {
        DirectionProperty prop = BlockStateProperties.HORIZONTAL_FACING;
        return (world, pos, state, side) -> blockClass.isInstance(state.getBlock()) && state.hasProperty(prop)
            ? rotateOnce(world, pos, state, prop, ROTATE_HORIZONTAL) : InteractionResult.PASS;
    }

    /** Rotates through the given directions, skipping any where the block couldn't stay (like 1.12's ladders). */
    private static ICustomRotationHandler surviving(Class<?> blockClass, Property<Direction> prop,
        OrderedEnumMap<Direction> possible) {
        return (world, pos, state, side) -> {
            if (!blockClass.isInstance(state.getBlock()) || !state.hasProperty(prop)) {
                return InteractionResult.PASS;
            }
            return rotateAnyTypeManual(world, pos, state, prop, possible,
                toTry -> state.setValue(prop, toTry).canSurvive(world, pos));
        };
    }

    private static ICustomRotationHandler rotation16(Class<?> blockClass, IntegerProperty prop) {
        return (world, pos, state, side) -> {
            if (!blockClass.isInstance(state.getBlock()) || !state.hasProperty(prop)) {
                return InteractionResult.PASS;
            }
            world.setBlockAndUpdate(pos, state.setValue(prop, (state.getValue(prop) + 1) % 16));
            return InteractionResult.SUCCESS;
        };
    }

    public static <T> int getOrdinal(T side, T[] array) {
        for (int i = 0; i < array.length; i++) {
            if (side == array[i]) return i;
        }
        return 0;
    }

    public static InteractionResult rotateEnumFacing(Level world, BlockPos pos, BlockState state,
        Property<Direction> prop, OrderedEnumMap<Direction> possible) {
        return rotateAnyTypeManual(world, pos, state, prop, possible,
            toTry -> state.setValue(prop, toTry).canSurvive(world, pos));
    }

    public static <E extends Enum<E> & Comparable<E>> InteractionResult rotateOnce(Level world, BlockPos pos,
        BlockState state, Property<E> prop, OrderedEnumMap<E> possible) {
        E current = state.getValue(prop);
        current = possible.next(current);
        world.setBlockAndUpdate(pos, state.setValue(prop, current));
        return InteractionResult.SUCCESS;
    }

    public static <E extends Enum<E> & Comparable<E>> InteractionResult rotateAnyTypeManual(Level world,
        BlockPos pos, BlockState state, Property<E> prop, OrderedEnumMap<E> possible, Predicate<E> canPlace) {
        E current = state.getValue(prop);
        for (int i = possible.getOrderLength(); i > 1; i--) {
            current = possible.next(current);
            if (canPlace.test(current)) {
                world.setBlockAndUpdate(pos, state.setValue(prop, current));
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }
}
