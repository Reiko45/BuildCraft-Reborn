/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.blocks.ICustomPaintHandler;
import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.transport.EnumWirePart;
import buildcraftreborn.api.transport.IItemPluggable;
import buildcraftreborn.api.transport.WireNode;
import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.api.transport.pluggable.PipePluggable;
import buildcraftreborn.lib.block.BlockBCTile_Neptune;
import buildcraftreborn.lib.misc.AdvancementUtil;
import buildcraftreborn.lib.misc.VecUtil;
import buildcraftreborn.lib.tile.TileBC_Neptune;
import buildcraftreborn.transport.item.ItemWire;
import buildcraftreborn.transport.pipe.Pipe;
import buildcraftreborn.transport.tile.TilePipeHolder;
import buildcraftreborn.transport.wire.EnumWireBetween;

/** The block every pipe lives in. What the block holds (the pipe, six pluggables and wires) is in its
 * {@link TilePipeHolder}; this block works out which of those parts a player is looking at for clicking, breaking and
 * picking.
 * <p>
 * Part numbers are 1.12's {@code subHit}: 0 the centre, 1-6 a pipe connection, 7-12 a pluggable, 13-20 a wire corner,
 * 21-56 a wire segment.
 * <p>
 * 1.21 notes: the shape (outline, collision and ray traces) is the union of every part, cached in the tile. Drops come
 * from {@link #getDrops} (there is no loot table: everything dropped depends on the tile). Breaking a pluggable or a
 * wire leaves the block in place ({@code onDestroyedByPlayer} returns false). */
public class BlockPipeHolder extends BlockBCTile_Neptune implements ICustomPaintHandler {
    private static final AABB FULL_BLOCK_AABB = new AABB(0, 0, 0, 1, 1, 1);
    private static final AABB BOX_CENTER = new AABB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
    private static final AABB BOX_DOWN = new AABB(0.25, 0, 0.25, 0.75, 0.25, 0.75);
    private static final AABB BOX_UP = new AABB(0.25, 0.75, 0.25, 0.75, 1, 0.75);
    private static final AABB BOX_NORTH = new AABB(0.25, 0.25, 0, 0.75, 0.75, 0.25);
    private static final AABB BOX_SOUTH = new AABB(0.25, 0.25, 0.75, 0.75, 0.75, 1);
    private static final AABB BOX_WEST = new AABB(0, 0.25, 0.25, 0.25, 0.75, 0.75);
    private static final AABB BOX_EAST = new AABB(0.75, 0.25, 0.25, 1, 0.75, 0.75);
    private static final AABB[] BOX_FACES = { BOX_DOWN, BOX_UP, BOX_NORTH, BOX_SOUTH, BOX_WEST, BOX_EAST };

    public static final int PART_CENTER = 0;
    public static final int PART_PLUG_START = 1 + 6;
    public static final int PART_WIRE_START = 1 + 6 + 6;
    public static final int PART_BETWEEN_START = 1 + 6 + 6 + 8;
    public static final int PART_FULL_BLOCK = 400;

    private static final ResourceLocation ADVANCEMENT_LOGIC_TRANSPORTATION =
        BuildCraftReborn.id("logic_transportation");

    public BlockPipeHolder(Properties properties) {
        super(properties);
    }

    @Override
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return new TilePipeHolder(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // Parts

    /** A part of a pipe that a ray hit. */
    public record PartHit(int part, Vec3 location, Direction side) {
        @Nullable
        public Direction getPartSide() {
            return getPartSideHit(this);
        }

        public BlockHitResult toBlockHit(BlockPos pos) {
            return new BlockHitResult(location, side, pos, false);
        }
    }

    private static AABB getConnectionBox(Pipe pipe, Direction face) {
        float conSize = pipe.getConnectedDist(face);
        AABB aabb = BOX_FACES[face.ordinal()];
        if (conSize > 0 && conSize != 0.25f) {
            Vec3 center = VecUtil.offset(new Vec3(0.5, 0.5, 0.5), face, 0.25 + (conSize / 2));
            Vec3 radius = new Vec3(0.25, 0.25, 0.25);
            radius = VecUtil.replaceValue(radius, face.getAxis(), conSize / 2);
            aabb = new AABB(center.subtract(radius), center.add(radius));
        }
        return aabb;
    }

    /** @return Every box of the tile's parts, paired with its part number (see the class comment). */
    public static List<PartBox> getPartBoxes(TilePipeHolder tile) {
        List<PartBox> boxes = new ArrayList<>();
        Pipe pipe = tile.getPipe();
        if (pipe != null) {
            boxes.add(new PartBox(PART_CENTER, BOX_CENTER));
            for (Direction face : Direction.values()) {
                if (pipe.getConnectedDist(face) > 0) {
                    boxes.add(new PartBox(face.ordinal() + 1, getConnectionBox(pipe, face)));
                }
            }
        }
        for (Direction face : Direction.values()) {
            PipePluggable pluggable = tile.getPluggable(face);
            if (pluggable != null) {
                boxes.add(new PartBox(face.ordinal() + PART_PLUG_START, pluggable.getBoundingBox()));
            }
        }
        for (EnumWirePart part : tile.getWireManager().parts.keySet()) {
            boxes.add(new PartBox(part.ordinal() + PART_WIRE_START, part.boundingBox));
        }
        for (EnumWireBetween between : tile.getWireManager().betweens.keySet()) {
            boxes.add(new PartBox(between.ordinal() + PART_BETWEEN_START, between.boundingBox));
        }
        return boxes;
    }

    /** A part's box, relative to the block. */
    public record PartBox(int part, AABB box) {}

    // Shapes

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!(level.getBlockEntity(pos) instanceof TilePipeHolder tile)) {
            return Shapes.block();
        }
        List<PartBox> boxes = getPartBoxes(tile);
        if (boxes.isEmpty()) {
            return Shapes.block();
        }
        List<AABB> key = new ArrayList<>(boxes.size());
        for (PartBox box : boxes) {
            key.add(box.box());
        }
        if (key.equals(tile.shapeKey) && tile.shape != null) {
            return tile.shape;
        }
        VoxelShape shape = Shapes.empty();
        for (AABB box : key) {
            shape = Shapes.joinUnoptimized(shape, Shapes.create(box), net.minecraft.world.phys.shapes.BooleanOp.OR);
        }
        shape = shape.optimize();
        tile.shapeKey = key;
        tile.shape = shape;
        return shape;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
        CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0f;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    // Ray tracing

    /** Ray traces from the player's eyes along their look to their reach. */
    @Nullable
    public static PartHit rayTrace(Level level, BlockPos pos, Player player) {
        Vec3 start = player.getEyePosition();
        double reach = player.blockInteractionRange();
        Vec3 end = start.add(player.getViewVector(1).normalize().scale(reach));
        return rayTrace(level, pos, start, end);
    }

    @Nullable
    public static PartHit rayTrace(BlockGetter level, BlockPos pos, Vec3 start, Vec3 end) {
        if (!(level.getBlockEntity(pos) instanceof TilePipeHolder tile)) {
            return computeTrace(null, pos, start, end, FULL_BLOCK_AABB, PART_FULL_BLOCK);
        }
        List<PartBox> boxes = getPartBoxes(tile);
        if (boxes.isEmpty()) {
            return computeTrace(null, pos, start, end, FULL_BLOCK_AABB, PART_FULL_BLOCK);
        }
        PartHit best = null;
        for (PartBox box : boxes) {
            best = computeTrace(best, pos, start, end, box.box(), box.part());
        }
        return best;
    }

    /** Finds which part contains the given hit location (used when only a vanilla hit result is known). */
    @Nullable
    public static PartHit findPartAt(BlockGetter level, BlockPos pos, Vec3 location, Direction side) {
        if (!(level.getBlockEntity(pos) instanceof TilePipeHolder tile)) {
            return null;
        }
        Vec3 rel = location.subtract(pos.getX(), pos.getY(), pos.getZ());
        PartHit best = null;
        double bestDist = Double.MAX_VALUE;
        for (PartBox box : getPartBoxes(tile)) {
            AABB grown = box.box().inflate(0.001);
            if (grown.contains(rel)) {
                // Prefer the smallest box (pluggables and wires sit on top of the pipe)
                double dist = box.box().getSize();
                if (dist < bestDist) {
                    bestDist = dist;
                    best = new PartHit(box.part(), location, side);
                }
            }
        }
        return best;
    }

    @Nullable
    public static EnumWirePart rayTraceWire(BlockPos pos, Vec3 start, Vec3 end) {
        Vec3 realStart = start.subtract(pos.getX(), pos.getY(), pos.getZ());
        Vec3 realEnd = end.subtract(pos.getX(), pos.getY(), pos.getZ());
        EnumWirePart best = null;
        double dist = 1000;
        for (EnumWirePart part : EnumWirePart.VALUES) {
            Optional<Vec3> trace = part.boundingBoxPossible.clip(realStart, realEnd);
            if (trace.isPresent()) {
                double nextDist = trace.get().distanceToSqr(realStart);
                if (best == null || dist > nextDist) {
                    best = part;
                    dist = nextDist;
                }
            }
        }
        return best;
    }

    @Nullable
    private static PartHit computeTrace(@Nullable PartHit lastBest, BlockPos pos, Vec3 start, Vec3 end, AABB aabb,
        int part) {
        BlockHitResult next = AABB.clip(List.of(aabb), start, end, pos);
        if (next == null) {
            return lastBest;
        }
        PartHit hit = new PartHit(part, next.getLocation(), next.getDirection());
        if (lastBest == null) {
            return hit;
        }
        double distLast = lastBest.location().distanceToSqr(start);
        double distNext = hit.location().distanceToSqr(start);
        return distLast > distNext ? hit : lastBest;
    }

    @Nullable
    public static Direction getPartSideHit(PartHit trace) {
        if (trace.part() <= 0 || trace.part() == PART_FULL_BLOCK) {
            return trace.side();
        }
        if (trace.part() <= 6) {
            return Direction.values()[trace.part() - 1];
        }
        if (trace.part() <= 6 + 6) {
            return Direction.values()[trace.part() - 1 - 6];
        }
        return null;
    }

    @Nullable
    public static EnumWirePart getWirePartHit(PartHit trace) {
        int p = trace.part();
        if (p >= PART_WIRE_START && p < PART_BETWEEN_START) {
            return EnumWirePart.VALUES[p - PART_WIRE_START];
        }
        return null;
    }

    @Nullable
    public static EnumWireBetween getWireBetweenHit(PartHit trace) {
        int p = trace.part();
        if (p >= PART_BETWEEN_START && p < PART_BETWEEN_START + EnumWireBetween.VALUES.length) {
            return EnumWireBetween.VALUES[p - PART_BETWEEN_START];
        }
        return null;
    }

    /** @return The box of the given part (relative to the block), for drawing the selection outline. */
    @Nullable
    public static AABB getPartBox(TilePipeHolder tile, int part) {
        for (PartBox box : getPartBoxes(tile)) {
            if (box.part() == part) {
                return part < PART_PLUG_START ? box.box().inflate(1 / 32.0) : box.box();
            }
        }
        return null;
    }

    // Picking

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos,
        Player player) {
        if (!(level.getBlockEntity(pos) instanceof TilePipeHolder tile)) {
            return ItemStack.EMPTY;
        }
        PartHit hit = findPartAt(level, pos, target.getLocation(),
            target instanceof BlockHitResult bhr ? bhr.getDirection() : Direction.UP);
        int part = hit == null ? PART_CENTER : hit.part();
        if (part <= 6) {
            Pipe pipe = tile.getPipe();
            return pipe == null ? ItemStack.EMPTY : pipe.createItemStack();
        } else if (part <= 12) {
            PipePluggable plug = tile.getPluggable(Direction.values()[part - PART_PLUG_START]);
            return plug == null ? ItemStack.EMPTY : plug.getPickStack();
        } else {
            EnumWirePart wirePart = getWirePartHit(hit);
            EnumWireBetween between = getWireBetweenHit(hit);
            DyeColor colour = null;
            if (wirePart != null) {
                colour = tile.wireManager.getColorOfPart(wirePart);
            } else if (between != null) {
                colour = tile.wireManager.getColorOfPart(between.parts[0]);
            }
            return colour == null ? ItemStack.EMPTY : new ItemStack(ItemWire.get(colour));
        }
    }

    // Interaction

    @Override
    protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
        Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof TilePipeHolder tile)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (onActivated(tile, level, pos, player, hand, held, hit)) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
    }

    private boolean onActivated(TilePipeHolder tile, Level level, BlockPos pos, Player player, InteractionHand hand,
        ItemStack held, BlockHitResult vanillaHit) {
        PartHit trace = rayTrace(level, pos, player);
        if (trace == null) {
            trace = findPartAt(level, pos, vanillaHit.getLocation(), vanillaHit.getDirection());
            if (trace == null) {
                return false;
            }
        }
        BlockHitResult blockHit = trace.toBlockHit(pos);
        Vec3 rel = trace.location().subtract(pos.getX(), pos.getY(), pos.getZ());
        float hitX = (float) rel.x, hitY = (float) rel.y, hitZ = (float) rel.z;
        Direction realSide = getPartSideHit(trace);
        if (realSide == null) {
            realSide = trace.side();
        }
        if (trace.part() >= PART_PLUG_START && trace.part() < PART_WIRE_START) {
            PipePluggable existing = tile.getPluggable(realSide);
            if (existing != null) {
                return existing.onPluggableActivate(player, blockHit, hitX, hitY, hitZ);
            }
        }

        EnumPipePart part = trace.part() == PART_CENTER ? EnumPipePart.CENTER : EnumPipePart.fromFacing(realSide);

        Item item = held.isEmpty() ? null : held.getItem();
        PipePluggable existing = tile.getPluggable(realSide);
        if (item instanceof IItemPluggable itemPlug && existing == null) {
            PipePluggable plug = itemPlug.onPlace(held, tile, realSide, player, hand);
            if (plug == null) {
                return false;
            }
            if (!level.isClientSide) {
                tile.replacePluggable(realSide, plug);
                plug.onPlacedBy(player);
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
            }
            return true;
        }
        if (item instanceof ItemWire wireItem) {
            return placeWire(tile, level, pos, player, held, wireItem.colour, trace);
        }
        Pipe pipe = tile.getPipe();
        if (pipe == null) {
            return false;
        }
        if (pipe.behaviour.onPipeActivate(player, blockHit, hitX, hitY, hitZ, part)) {
            return true;
        }
        return pipe.flow.onFlowActivate(player, blockHit, hitX, hitY, hitZ, part);
    }

    private static boolean placeWire(TilePipeHolder tile, Level level, BlockPos pos, Player player, ItemStack held,
        DyeColor colour, PartHit trace) {
        EnumWirePart wirePartHit = getWirePartHit(trace);
        EnumWirePart wirePart;
        TilePipeHolder attachTile = tile;
        if (wirePartHit != null) {
            WireNode node = new WireNode(pos, wirePartHit);
            node = node.offset(trace.side());
            wirePart = node.part;
            if (!node.pos.equals(pos)) {
                attachTile = level.getBlockEntity(node.pos) instanceof TilePipeHolder other ? other : null;
            }
        } else {
            Vec3 hit = trace.location();
            wirePart = EnumWirePart.get((hit.x % 1 + 1) % 1 > 0.5, (hit.y % 1 + 1) % 1 > 0.5,
                (hit.z % 1 + 1) % 1 > 0.5);
        }
        if (wirePart == null || attachTile == null) {
            return false;
        }
        if (level.isClientSide) {
            return attachTile.getWireManager().getColorOfPart(wirePart) == null;
        }
        boolean attached = attachTile.getWireManager().addPart(wirePart, colour);
        attachTile.scheduleNetworkUpdate(IPipeHolder.PipeMessageReceiver.WIRES);
        if (!attached) {
            return false;
        }
        WireNode from = new WireNode(attachTile.getPipePos(), wirePart);
        boolean isNowConnected = false;
        for (Direction dir : Direction.values()) {
            WireNode to = from.offset(dir);
            if (to.pos.equals(attachTile.getPipePos())) {
                if (attachTile.getWireManager().getColorOfPart(to.part) == colour) {
                    isNowConnected = true;
                    break;
                }
            } else if (level.getBlockEntity(to.pos) instanceof TilePipeHolder other
                && other.getWireManager().getColorOfPart(to.part) == colour) {
                isNowConnected = true;
                break;
            }
        }
        if (isNowConnected) {
            AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_LOGIC_TRANSPORTATION);
        }
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        return true;
    }

    // Breaking

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player,
        boolean willHarvest, FluidState fluid) {
        if (!(level.getBlockEntity(pos) instanceof TilePipeHolder tile)) {
            return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        }
        PartHit trace = rayTrace(level, pos, player);
        Direction side = null;
        EnumWirePart part = null;
        EnumWireBetween between = null;
        if (trace != null && trace.part() > 6 && trace.part() != PART_FULL_BLOCK) {
            side = getPartSideHit(trace);
            part = getWirePartHit(trace);
            between = getWireBetweenHit(trace);
        }
        boolean partial = side != null || part != null || between != null;
        if (!partial) {
            return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        }
        if (level.isClientSide) {
            // Only part of the pipe breaks, so the block stays
            return false;
        }
        NonNullList<ItemStack> toDrop = NonNullList.create();
        if (side != null) {
            removePluggable(side, tile, toDrop);
        } else if (part != null) {
            DyeColor colour = tile.wireManager.getColorOfPart(part);
            if (colour != null) {
                toDrop.add(new ItemStack(ItemWire.get(colour)));
            }
            tile.wireManager.removePart(part);
            tile.scheduleNetworkUpdate(IPipeHolder.PipeMessageReceiver.WIRES);
        } else {
            DyeColor colour = tile.wireManager.getColorOfPart(between.parts[0]);
            if (colour != null) {
                toDrop.add(new ItemStack(ItemWire.get(colour), between.to == null ? 2 : 1));
            }
            if (between.to == null) {
                tile.wireManager.removeParts(Arrays.asList(between.parts));
            } else {
                tile.wireManager.removePart(between.parts[0]);
            }
            tile.scheduleNetworkUpdate(IPipeHolder.PipeMessageReceiver.WIRES);
        }
        if (!player.getAbilities().instabuild) {
            for (ItemStack stack : toDrop) {
                popResource(level, pos, stack);
            }
        }
        tile.scheduleRenderUpdate();
        return false;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        NonNullList<ItemStack> toDrop = NonNullList.create();
        if (be instanceof TilePipeHolder tile) {
            addDrops(tile, toDrop);
        }
        return toDrop;
    }

    /** Adds everything a pipe drops when broken: pluggables, wires, the pipe and what it carries. */
    public static void addDrops(TilePipeHolder tile, NonNullList<ItemStack> toDrop) {
        for (Direction face : Direction.values()) {
            PipePluggable pluggable = tile.getPluggable(face);
            if (pluggable != null) {
                pluggable.addDrops(toDrop);
            }
        }
        for (DyeColor color : tile.wireManager.parts.values()) {
            toDrop.add(new ItemStack(ItemWire.get(color)));
        }
        Pipe pipe = tile.getPipe();
        if (pipe != null) {
            pipe.addDrops(toDrop);
        }
    }

    private static void removePluggable(Direction side, TilePipeHolder tile, NonNullList<ItemStack> toDrop) {
        PipePluggable removed = tile.replacePluggable(side, null);
        if (removed != null) {
            removed.onRemove();
            removed.addDrops(toDrop);
        }
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        Entity exploder = explosion.getDirectSourceEntity();
        if (exploder != null && level.getBlockEntity(pos) instanceof TilePipeHolder tile) {
            Vec3 subtract = exploder.position().subtract(Vec3.atCenterOf(pos)).normalize();
            Direction side = Arrays.stream(Direction.values())
                .min(Comparator.comparing(facing -> Vec3.atLowerCornerOf(facing.getNormal()).distanceTo(subtract)))
                .orElseThrow(IllegalArgumentException::new);
            PipePluggable pluggable = tile.getPluggable(side);
            if (pluggable != null) {
                float explosionResistance = pluggable.getExplosionResistance(exploder, explosion);
                if (explosionResistance > 0) {
                    return explosionResistance;
                }
            }
        }
        return super.getExplosionResistance(state, level, pos, explosion);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.getBlockEntity(pos) instanceof TilePipeHolder tile) {
            Pipe pipe = tile.getPipe();
            if (pipe != null) {
                pipe.getBehaviour().onEntityCollide(entity);
            }
        }
    }

    // Redstone

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos,
        @Nullable Direction side) {
        if (side == null) return false;
        if (level.getBlockEntity(pos) instanceof TilePipeHolder tile) {
            PipePluggable pluggable = tile.getPluggable(side.getOpposite());
            return pluggable != null && pluggable.canConnectToRedstone(side);
        }
        return false;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        if (level.getBlockEntity(pos) instanceof TilePipeHolder tile) {
            return tile.getRedstoneOutput(side.getOpposite());
        }
        return 0;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return getDirectSignal(state, level, pos, side);
    }

    // Paint

    @Override
    public InteractionResult attemptPaint(Level level, BlockPos pos, BlockState state, Vec3 hitPos,
        @Nullable Direction hitSide, @Nullable DyeColor paintColour) {
        if (!(level.getBlockEntity(pos) instanceof TilePipeHolder tile)) {
            return InteractionResult.PASS;
        }
        Pipe pipe = tile.getPipe();
        if (pipe == null) {
            return InteractionResult.FAIL;
        }
        if (pipe.getColour() == paintColour || !pipe.definition.canBeColoured) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide) {
            pipe.setColour(paintColour);
        }
        return InteractionResult.SUCCESS;
    }
}
