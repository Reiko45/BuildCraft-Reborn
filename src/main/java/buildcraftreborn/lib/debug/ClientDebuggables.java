/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.debug;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.lib.item.ItemDebugger;

/** The F3 screen's BuildCraft section: for the block the player looks at, its debug info from the server and from the
 * client (differences marked with a red "!"). Shown in creative, or while holding a debugger, as in 1.12. */
public final class ClientDebuggables {
    private static final List<String> SERVER_LEFT = new ArrayList<>();
    private static final List<String> SERVER_RIGHT = new ArrayList<>();

    private static final String DIFF_START = ChatFormatting.RED + "" + ChatFormatting.BOLD + "!" + ChatFormatting.RESET;
    private static final String DIFF_HEADER_FORMATTING = ChatFormatting.AQUA + "" + ChatFormatting.BOLD;

    private ClientDebuggables() {}

    static void setServerInfo(List<String> left, List<String> right) {
        SERVER_LEFT.clear();
        SERVER_LEFT.addAll(left);
        SERVER_RIGHT.clear();
        SERVER_RIGHT.addAll(right);
    }

    @Nullable
    public static IDebuggable getDebuggableObject(@Nullable HitResult mouseOver) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.reducedDebugInfo().get() || mc.player.isReducedDebugInfo()
            || !mc.getDebugOverlay().showDebugScreen() || !ItemDebugger.isShowDebugInfo(mc.player)) {
            return null;
        }
        ClientLevel world = mc.level;
        if (mouseOver == null || world == null) {
            return null;
        }
        if (mouseOver instanceof BlockHitResult blockHit && mouseOver.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = blockHit.getBlockPos();
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof IDebuggable debuggable) {
                return debuggable;
            }
        } else if (mouseOver instanceof EntityHitResult entityHit) {
            Entity entity = entityHit.getEntity();
            if (entity instanceof IDebuggable debuggable) {
                return debuggable;
            }
        }
        return null;
    }

    /** Asks the server for the debug info of the block or entity looked at (every tick while it is shown). */
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        HitResult mouseOver = mc.hitResult;
        IDebuggable debuggable = getDebuggableObject(mouseOver);
        if (debuggable instanceof BlockEntity tile && mouseOver instanceof BlockHitResult blockHit) {
            PacketDistributor.sendToServer(new DebugRequestPayload(tile.getBlockPos(), blockHit.getDirection()));
        } else if (debuggable instanceof Entity entity) {
            PacketDistributor.sendToServer(new DebugEntityRequestPayload(entity.getId()));
        }
    }

    public static void onDebugText(CustomizeGuiOverlayEvent.DebugText event) {
        Minecraft mc = Minecraft.getInstance();
        HitResult mouseOver = mc.hitResult;
        IDebuggable debuggable = getDebuggableObject(mouseOver);
        if (debuggable == null) {
            return;
        }
        var side = mouseOver instanceof BlockHitResult blockHit ? blockHit.getDirection() : null;
        List<String> clientLeft = new ArrayList<>();
        List<String> clientRight = new ArrayList<>();
        debuggable.getDebugInfo(clientLeft, clientRight, side);
        String headerFirst = DIFF_HEADER_FORMATTING + "SERVER:";
        String headerSecond = DIFF_HEADER_FORMATTING + "CLIENT:";
        appendDiff(event.getLeft(), SERVER_LEFT, clientLeft, headerFirst, headerSecond);
        appendDiff(event.getRight(), SERVER_RIGHT, clientRight, headerFirst, headerSecond);
        debuggable.getClientDebugInfo(event.getLeft(), event.getRight(), side);
    }

    private static void appendDiff(List<String> dest, List<String> first, List<String> second, String headerFirst,
        String headerSecond) {
        dest.add("");
        dest.add(headerFirst);
        dest.addAll(first);
        dest.add("");
        dest.add(headerSecond);
        if (first.size() != second.size()) {
            // no diffing
            dest.addAll(second);
        } else {
            for (int l = 0; l < first.size(); l++) {
                String shownLine = first.get(l);
                String diffLine = second.get(l);
                if (shownLine.equals(diffLine)) {
                    dest.add(diffLine);
                } else if (diffLine.startsWith(" ")) {
                    dest.add(DIFF_START + diffLine.substring(1));
                } else {
                    dest.add(DIFF_START + diffLine);
                }
            }
        }
    }
}
