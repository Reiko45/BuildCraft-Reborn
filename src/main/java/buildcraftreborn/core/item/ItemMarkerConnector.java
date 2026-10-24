/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.item;

import java.util.Iterator;
import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.core.marker.PathSubCache;
import buildcraftreborn.core.marker.VolumeSubCache;
import buildcraftreborn.core.marker.volume.Addon;
import buildcraftreborn.core.marker.volume.EnumAddonSlot;
import buildcraftreborn.core.marker.volume.Lock;
import buildcraftreborn.core.marker.volume.VolumeBox;
import buildcraftreborn.core.marker.volume.WorldSavedDataVolumeBoxes;
import buildcraftreborn.lib.marker.MarkerCache;
import buildcraftreborn.lib.marker.MarkerSubCache;
import buildcraftreborn.lib.misc.AdvancementUtil;
import buildcraftreborn.lib.misc.PositionUtil;
import buildcraftreborn.lib.misc.PositionUtil.Line;
import buildcraftreborn.lib.misc.PositionUtil.LineSkewResult;
import buildcraftreborn.lib.misc.VecUtil;

/** Connects markers: while held, every connection two markers could make is drawn as a faint laser; right click while
 * looking at one to make it. It also resizes and removes volume boxes, and uses their addons. */
public class ItemMarkerConnector extends Item {
    private static final ResourceLocation ADVANCEMENT_VOLUME_MARKER = BuildCraftReborn.id("markers");
    private static final ResourceLocation ADVANCEMENT_PATH_MARKER = BuildCraftReborn.id("path_markers");

    public ItemMarkerConnector(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide) {
            for (MarkerCache<?> cache : MarkerCache.CACHES) {
                if (interactCache(cache.getSubCache(world), player)) {
                    player.swing(hand, true);
                    break;
                }
            }
        }
        InteractionResult result = onItemRightClickVolumeBoxes(world, player);
        return new InteractionResultHolder<>(result, player.getItemInHand(hand));
    }

    /** Right click: grab the volume box corner looked at (and click again to let go), or use the addon looked at.
     * Sneak and right click: remove the addon or volume box looked at, or cancel resizing. */
    private static InteractionResult onItemRightClickVolumeBoxes(Level world, Player player) {
        if (world.isClientSide) {
            return InteractionResult.PASS;
        }

        WorldSavedDataVolumeBoxes volumeBoxes = WorldSavedDataVolumeBoxes.get(world);

        VolumeBox currentEditing = volumeBoxes.getCurrentEditing(player);

        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(4));

        Pair<VolumeBox, EnumAddonSlot> selectingVolumeBoxAndSlot =
            EnumAddonSlot.getSelectingVolumeBoxAndSlot(player, volumeBoxes.volumeBoxes);
        VolumeBox addonVolumeBox = selectingVolumeBoxAndSlot.getLeft();
        EnumAddonSlot addonSlot = selectingVolumeBoxAndSlot.getRight();
        if (addonVolumeBox != null && addonSlot != null) {
            if (addonVolumeBox.addons.containsKey(addonSlot) && addonVolumeBox.getLockTargetsStream()
                .noneMatch(target -> target instanceof Lock.Target.TargetAddon addon && addon.slot == addonSlot)) {
                if (player.isShiftKeyDown()) {
                    addonVolumeBox.addons.get(addonSlot).onRemoved();
                    addonVolumeBox.addons.remove(addonSlot);
                    volumeBoxes.setDirty();
                } else {
                    addonVolumeBox.addons.get(addonSlot).onPlayerRightClick(player);
                    volumeBoxes.setDirty();
                }
            }
        } else if (player.isShiftKeyDown()) {
            if (currentEditing == null) {
                for (Iterator<VolumeBox> iterator = volumeBoxes.volumeBoxes.iterator(); iterator.hasNext();) {
                    VolumeBox volumeBox = iterator.next();
                    if (volumeBox.box.getBoundingBox().clip(start, end).isPresent()) {
                        if (volumeBox.getLockTargetsStream().noneMatch(Lock.Target.TargetResize.class::isInstance)) {
                            volumeBox.addons.values().forEach(Addon::onRemoved);
                            iterator.remove();
                            volumeBoxes.setDirty();
                            return InteractionResult.SUCCESS;
                        } else {
                            return InteractionResult.FAIL;
                        }
                    }
                }
            } else {
                currentEditing.cancelEditing();
                volumeBoxes.setDirty();
                return InteractionResult.SUCCESS;
            }
        } else {
            if (currentEditing == null) {
                VolumeBox bestVolumeBox = null;
                double bestDist = Double.MAX_VALUE;
                BlockPos editing = null;

                for (VolumeBox volumeBox : volumeBoxes.volumeBoxes) {
                    if (volumeBox.getLockTargetsStream().anyMatch(Lock.Target.TargetResize.class::isInstance)) {
                        continue;
                    }
                    for (BlockPos p : PositionUtil.getCorners(volumeBox.box.min(), volumeBox.box.max())) {
                        Optional<Vec3> ray = new AABB(p).clip(start, end);
                        if (ray.isPresent()) {
                            double dist = ray.get().distanceTo(start);
                            if (bestDist > dist) {
                                bestDist = dist;
                                bestVolumeBox = volumeBox;
                                editing = p;
                            }
                        }
                    }
                }

                if (bestVolumeBox != null) {
                    bestVolumeBox.setPlayer(player);

                    BlockPos min = bestVolumeBox.box.min();
                    BlockPos max = bestVolumeBox.box.max();

                    BlockPos held = min;
                    if (editing.getX() == min.getX()) {
                        held = VecUtil.replaceValue(held, Direction.Axis.X, max.getX());
                    }
                    if (editing.getY() == min.getY()) {
                        held = VecUtil.replaceValue(held, Direction.Axis.Y, max.getY());
                    }
                    if (editing.getZ() == min.getZ()) {
                        held = VecUtil.replaceValue(held, Direction.Axis.Z, max.getZ());
                    }
                    bestVolumeBox.setHeldDistOldMinOldMax(held, Math.max(1.5, bestDist + 0.5),
                        bestVolumeBox.box.min(), bestVolumeBox.box.max());
                    volumeBoxes.setDirty();
                    return InteractionResult.SUCCESS;
                }
            } else {
                currentEditing.confirmEditing();
                volumeBoxes.setDirty();
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }

    private static <S extends MarkerSubCache<?>> boolean interactCache(S cache, Player player) {
        MarkerLineInteraction best = null;
        Vec3 playerPos = player.getEyePosition();
        Vec3 playerLook = player.getLookAngle();
        for (BlockPos marker : cache.getAllMarkers()) {
            ImmutableList<BlockPos> possibles = cache.getValidConnections(marker);
            for (BlockPos possible : possibles) {
                MarkerLineInteraction interaction = new MarkerLineInteraction(marker, possible, playerPos, playerLook);
                if (interaction.didInteract()) {
                    best = interaction.getBetter(best);
                }
            }
        }
        if (best == null) {
            return false;
        }
        if (cache.tryConnect(best.marker1, best.marker2) || cache.tryConnect(best.marker2, best.marker1)) {
            if (cache instanceof VolumeSubCache) {
                AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_VOLUME_MARKER);
            } else if (cache instanceof PathSubCache) {
                AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_PATH_MARKER);
            }
            return true;
        }
        return false;
    }

    public static boolean doesInteract(BlockPos a, BlockPos b, Player player) {
        return new MarkerLineInteraction(a, b, player.getEyePosition(), player.getLookAngle()).didInteract();
    }

    private static class MarkerLineInteraction {
        final BlockPos marker1, marker2;
        final double distToPoint, distToLine;

        MarkerLineInteraction(BlockPos marker1, BlockPos marker2, Vec3 playerPos, Vec3 playerEndPos) {
            this.marker1 = marker1;
            this.marker2 = marker2;
            LineSkewResult interactionPoint = PositionUtil.findLineSkewPoint(
                new Line(VecUtil.convertCenter(marker1), VecUtil.convertCenter(marker2)), playerPos, playerEndPos);
            distToPoint = interactionPoint.closestPos().distanceTo(playerPos);
            distToLine = interactionPoint.distFromLine();
        }

        boolean didInteract() {
            return distToPoint <= 3 && distToLine < 0.3;
        }

        MarkerLineInteraction getBetter(@Nullable MarkerLineInteraction other) {
            if (other == null) {
                return this;
            }
            if (other.marker1 == marker2 && other.marker2 == marker1) {
                return other;
            }
            if (other.distToLine < distToLine) {
                return other;
            }
            if (other.distToLine > distToLine) {
                return this;
            }
            if (other.distToPoint < distToPoint) {
                return other;
            }
            return this;
        }
    }
}
