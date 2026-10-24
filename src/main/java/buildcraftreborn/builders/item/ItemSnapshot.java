/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.item;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import buildcraftreborn.api.enums.EnumSnapshotType;
import buildcraftreborn.builders.BCBuildersItems;
import buildcraftreborn.builders.snapshot.Snapshot;
import buildcraftreborn.lib.misc.HashUtil;

/** A blueprint or a template: blank until an architect table writes a scan to it. The scan itself is kept in the
 * snapshot store; the item only holds its {@link Snapshot.Header} (in the {@code snapshot_header} component).
 * <p>
 * 1.21: one item per snapshot type ({@code blueprint} and {@code template}); 1.12 had a single {@code snapshot} item
 * with four metadata values. A used one stacks to 1, a blank one to 16, as in 1.12. */
public class ItemSnapshot extends Item {
    public final EnumSnapshotType snapshotType;

    public ItemSnapshot(EnumSnapshotType snapshotType, Properties properties) {
        super(properties.stacksTo(16));
        this.snapshotType = snapshotType;
    }

    public static ItemSnapshot itemFor(EnumSnapshotType type) {
        return type == EnumSnapshotType.BLUEPRINT ? BCBuildersItems.BLUEPRINT.get() : BCBuildersItems.TEMPLATE.get();
    }

    public static ItemStack getClean(EnumSnapshotType snapshotType) {
        return new ItemStack(itemFor(snapshotType));
    }

    public static ItemStack getUsed(EnumSnapshotType snapshotType, Snapshot.Header header) {
        ItemStack stack = new ItemStack(itemFor(snapshotType));
        stack.set(BCBuildersItems.SNAPSHOT_HEADER.get(), header.serializeNBT());
        stack.set(DataComponents.MAX_STACK_SIZE, 1);
        return stack;
    }

    public static boolean isUsed(ItemStack stack) {
        return stack.getItem() instanceof ItemSnapshot && stack.has(BCBuildersItems.SNAPSHOT_HEADER.get());
    }

    @Nullable
    public static Snapshot.Header getHeader(ItemStack stack) {
        if (stack.getItem() instanceof ItemSnapshot) {
            CompoundTag nbt = stack.get(BCBuildersItems.SNAPSHOT_HEADER.get());
            if (nbt != null) {
                try {
                    return new Snapshot.Header(nbt);
                } catch (RuntimeException e) {
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        Snapshot.Header header = getHeader(stack);
        return header == null ? Optional.empty() : Optional.of(new SnapshotTooltip(header, null));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Snapshot.Header header = getHeader(stack);
        if (header == null) {
            tooltip.add(Component.translatable("item.blueprint.blank").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal(header.name).withStyle(ChatFormatting.GRAY));
            Level level = context.level();
            Player owner = level == null ? null : header.getOwnerPlayer(level);
            if (owner != null) {
                tooltip.add(Component.translatable("item.blueprint.author").append(" ").append(owner.getName())
                    .withStyle(ChatFormatting.GRAY));
            }
            if (flag.isAdvanced()) {
                tooltip.add(Component.literal("Hash: " + HashUtil.convertHashToString(header.key.hash))
                    .withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(Component.literal("Date: " + header.created).withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(Component.literal("Owner UUID: " + header.owner).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }
}
