/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.addon;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraftreborn.api.core.IBox;
import buildcraftreborn.api.filler.IFillerPattern;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.containers.IFillerStatementContainer;
import buildcraftreborn.builders.container.ContainerFillerPlanner;
import buildcraftreborn.builders.filler.FillerType;
import buildcraftreborn.builders.filler.FillerUtil;
import buildcraftreborn.builders.snapshot.Template;
import buildcraftreborn.core.marker.volume.Addon;
import buildcraftreborn.core.marker.volume.ISingleAddon;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.statement.FullStatement;

/** The filler planner addon for volume boxes: shows a filler pattern inside the box, and a filler placed next to the
 * box builds that pattern. Right clicking it (with the marker connector) opens the pattern GUI.
 * <p>
 * 1.21: the GUI is told which addon it is for when it opens, instead of the client ray tracing for it again. The
 * renderer is registered separately ({@code AddonRendererFillerPlanner}). */
public class AddonFillerPlanner extends Addon implements ISingleAddon, IFillerStatementContainer {
    public final FullStatement<IFillerPattern> patternStatement = new FullStatement<>(FillerType.INSTANCE, 4, null);
    public boolean inverted;
    @Nullable
    public Template.BuildingInfo buildingInfo;

    public void updateBuildingInfo() {
        buildingInfo = FillerUtil.createBuildingInfo(this, patternStatement,
            IntStream.range(0, patternStatement.maxParams).mapToObj(patternStatement::get)
                .toArray(IStatementParameter[]::new),
            inverted);
    }

    @Override
    public void onVolumeBoxSizeChange() {
        updateBuildingInfo();
    }

    @Override
    public void onAdded() {
        super.onAdded();
        updateBuildingInfo();
    }

    @Override
    public void postReadFromNbt() {
        super.postReadFromNbt();
        updateBuildingInfo();
    }

    @Override
    public void onPlayerRightClick(Player player) {
        super.onPlayerRightClick(player);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                (windowId, inventory, p) -> new ContainerFillerPlanner(windowId, p, this),
                Component.translatable("item.buildcraftreborn.filler_planner")),
                buf -> ContainerFillerPlanner.writeAddon(buf, this));
        }
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag nbt, HolderLookup.Provider registries) {
        nbt.put("patternStatement", patternStatement.writeToNbt(registries));
        nbt.putBoolean("inverted", inverted);
        return nbt;
    }

    @Override
    public void readFromNBT(CompoundTag nbt, HolderLookup.Provider registries) {
        patternStatement.readFromNbt(nbt.getCompound("patternStatement"), registries);
        inverted = nbt.getBoolean("inverted");
    }

    @Override
    public void toBytes(PacketBufferBC buf) {
        patternStatement.writeToBuffer(buf);
        buf.writeBoolean(inverted);
    }

    @Override
    public void fromBytes(PacketBufferBC buf) throws IOException {
        patternStatement.readFromBuffer(buf);
        inverted = buf.readBoolean();
        updateBuildingInfo();
    }

    // IFillerStatementContainer

    @Override
    @Nullable
    public BlockEntity getNeighbourTile(Direction side) {
        return null;
    }

    @Override
    @Nullable
    public BlockEntity getTile() {
        return null;
    }

    @Override
    public Level getFillerWorld() {
        return volumeBox.level;
    }

    @Override
    public boolean hasBox() {
        return true;
    }

    @Override
    public IBox getBox() {
        return volumeBox.box;
    }

    @Override
    public void setPattern(IFillerPattern pattern, IStatementParameter[] params) {
        IFillerPattern before = patternStatement.get();
        IStatementParameter[] beforeParams = patternStatement.getParameters().clone();
        patternStatement.set(pattern, params);
        // (Gates call this every tick: only rebuild when something changed.)
        if (before != pattern || !Arrays.equals(beforeParams, patternStatement.getParameters())) {
            updateBuildingInfo();
        }
    }
}
