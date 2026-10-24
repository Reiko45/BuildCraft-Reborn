/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.item;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraftreborn.api.core.IAreaProvider;
import buildcraftreborn.api.core.IBox;
import buildcraftreborn.api.core.IPathProvider;
import buildcraftreborn.api.core.IZone;
import buildcraftreborn.api.items.IMapLocation;
import buildcraftreborn.lib.item.BCItemComponents;
import buildcraftreborn.lib.misc.StringUtilBC;
import buildcraftreborn.lib.misc.data.Box;
import buildcraftreborn.robotics.zone.ZonePlan;

/** The map location: right click a block to remember it (or the area of a marker, or a path of path markers); the
 * zone planner writes zones into it. Sneak-right click in the air to clean it. Creative only, as in 8.0.
 * <p>
 * 1.21: the type and the place are the {@code buildcraftreborn:map_location} data component ({@link MapLocationData});
 * clean ones stack to 16, used ones to 1. Using one from a stack of several now gives the used one back (1.12 lost it,
 * and wrote paths onto the rest of the stack). */
public class ItemMapLocation extends Item implements IMapLocation {
    public ItemMapLocation(Properties properties) {
        super(properties.stacksTo(16));
    }

    public static MapLocationData getData(ItemStack stack) {
        return stack.getOrDefault(BCItemComponents.MAP_LOCATION.get(), MapLocationData.CLEAN);
    }

    public static void setData(ItemStack stack, MapLocationData data) {
        if (data.type() == MapLocationType.CLEAN) {
            if (data.label().isEmpty()) {
                stack.remove(BCItemComponents.MAP_LOCATION.get());
            } else {
                stack.set(BCItemComponents.MAP_LOCATION.get(), MapLocationData.CLEAN.withLabel(data.label()));
            }
            // (setting the item's own value drops the override; removing it would leave the stack unstackable)
            stack.set(DataComponents.MAX_STACK_SIZE, stack.getItem().getDefaultMaxStackSize());
        } else {
            stack.set(BCItemComponents.MAP_LOCATION.get(), data);
            stack.set(DataComponents.MAX_STACK_SIZE, 1);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> strings, TooltipFlag flag) {
        MapLocationData data = getData(stack);
        if (!data.label().isEmpty()) {
            strings.add(Component.literal(data.label()));
        }

        switch (data.type()) {
            case SPOT -> strings.add(Component.literal("{" + data.pos().getX() + ", " + data.pos().getY() + ", "
                + data.pos().getZ() + ", " + data.side() + "}"));
            case AREA -> {
                BlockPos min = data.min();
                BlockPos max = data.max();
                strings.add(Component.literal("{" + min.getX() + ", " + min.getY() + ", " + min.getZ() + "} + {"
                    + (max.getX() - min.getX() + 1) + " x " + (max.getY() - min.getY() + 1) + " x "
                    + (max.getZ() - min.getZ() + 1) + "}"));
            }
            case PATH, PATH_REPEATING -> {
                if (!data.path().isEmpty()) {
                    strings.add(Component.literal("{" + StringUtilBC.blockPosToString(data.path().get(0)) + "}, (+"
                        + (data.path().size() - 1) + " elements)"));
                }
            }
            default -> {}
        }
        if (data.type() != MapLocationType.CLEAN) {
            strings.add(Component.translatable("buildcraft.item.nonclean.usage").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide) {
            return InteractionResultHolder.pass(stack);
        }
        if (player.isShiftKeyDown() && getType(stack) != MapLocationType.CLEAN) {
            setData(stack, MapLocationData.CLEAN.withLabel(getData(stack).label()));
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level world = context.getLevel();
        Player player = context.getPlayer();
        if (world.isClientSide || player == null || player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (getType(stack) != MapLocationType.CLEAN) {
            return InteractionResult.FAIL;
        }

        BlockPos pos = context.getClickedPos();
        String label = getData(stack).label();
        BlockEntity tile = world.getBlockEntity(pos);
        MapLocationData data;
        if (tile instanceof IPathProvider pathProvider) {
            List<BlockPos> path = pathProvider.getPath();
            boolean repeating = path.size() > 1 && path.get(0).equals(path.get(path.size() - 1));
            data = MapLocationData.path(label, path, repeating);
        } else if (tile instanceof IAreaProvider areaTile) {
            data = MapLocationData.area(label, areaTile.min(), areaTile.max());
        } else {
            data = MapLocationData.spot(label, pos, context.getClickedFace());
        }

        if (stack.getCount() > 1) {
            ItemStack modified = stack.split(1);
            setData(modified, data);
            if (!player.getInventory().add(modified)) {
                player.drop(modified, false);
            }
        } else {
            setData(stack, data);
        }
        return InteractionResult.SUCCESS;
    }

    // IMapLocation

    @Override
    public MapLocationType getType(ItemStack stack) {
        return getData(stack).type();
    }

    public static IBox getAreaBox(ItemStack item) {
        MapLocationData data = getData(item);
        return new Box(data.min(), data.max());
    }

    @Nullable
    public static IBox getPointBox(ItemStack item) {
        MapLocationData data = getData(item);
        if (data.type() == MapLocationType.SPOT) {
            return new Box(data.pos(), data.pos());
        }
        return null;
    }

    public static Direction getPointFace(ItemStack stack) {
        return getData(stack).side();
    }

    @Override
    @Nullable
    public IBox getBox(ItemStack item) {
        return switch (getType(item)) {
            case AREA -> getAreaBox(item);
            case SPOT -> getPointBox(item);
            default -> null;
        };
    }

    @Override
    @Nullable
    public Direction getPointSide(ItemStack item) {
        MapLocationData data = getData(item);
        return data.type() == MapLocationType.SPOT ? data.side() : null;
    }

    @Override
    @Nullable
    public BlockPos getPoint(ItemStack item) {
        MapLocationData data = getData(item);
        return data.type() == MapLocationType.SPOT ? data.pos() : null;
    }

    @Override
    @Nullable
    public IZone getZone(ItemStack item) {
        MapLocationData data = getData(item);
        return switch (data.type()) {
            case ZONE -> new ZonePlan().readFromNBT(data.zone());
            case AREA, SPOT -> getBox(item);
            default -> null;
        };
    }

    @Override
    @Nullable
    public List<BlockPos> getPath(ItemStack item) {
        MapLocationData data = getData(item);
        return switch (data.type()) {
            case PATH, PATH_REPEATING -> new ArrayList<>(data.path());
            case SPOT -> new ArrayList<>(List.of(data.pos()));
            default -> null;
        };
    }

    public static void setZone(ItemStack item, ZonePlan plan) {
        setData(item, MapLocationData.zone(getData(item).label(), plan.toNBT()));
    }

    @Override
    public String getLabel(ItemStack item) {
        return getData(item).label();
    }

    @Override
    public boolean setLabel(ItemStack item, String name) {
        setData(item, getData(item).withLabel(name));
        return true;
    }
}
