/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.wire;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import net.neoforged.neoforge.network.PacketDistributor;


import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.transport.EnumWirePart;
import buildcraftreborn.api.transport.IWireEmitter;
import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.api.transport.pluggable.PipePluggable;


/** Every wire system in a level, and whether each is powered. Saved with the level; ticked on the server. */
public class WorldSavedDataWireSystems extends SavedData {
    public static final String DATA_NAME = "buildcraft_wire_systems";
    public static final SavedData.Factory<WorldSavedDataWireSystems> FACTORY =
        new SavedData.Factory<>(WorldSavedDataWireSystems::new, WorldSavedDataWireSystems::load);
    public ServerLevel level;
    public final Map<WireSystem, Boolean> wireSystems = new HashMap<>();
    public boolean gatesChanged = true;
    public boolean structureChanged = true;
    public final List<WireSystem> changedSystems = new ArrayList<>();
    public final List<ServerPlayer> changedPlayers = new ArrayList<>();
    public final Map<WireSystem.WireElement, IWireEmitter> emittersCache = new HashMap<>();

    private final Map<WireSystem.WireElement, List<WireSystem>> elementsToWireSystemsIndex = new HashMap<>();

    public WorldSavedDataWireSystems() {}

    public void markStructureChanged() {
        structureChanged = true;
        gatesChanged = true;
        emittersCache.clear();
    }

    public List<WireSystem> getWireSystemsWithElement(WireSystem.WireElement element) {
        List<WireSystem> wireSystemsWithElement = this.elementsToWireSystemsIndex.get(element);
        return wireSystemsWithElement != null ? new ArrayList<>(wireSystemsWithElement) : Collections.emptyList();
    }

    public List<WireSystem> getWireSystemsWithElementAsReadOnlyList(WireSystem.WireElement element) {
        return this.elementsToWireSystemsIndex.getOrDefault(element, Collections.emptyList());
    }

    public void removeWireSystem(WireSystem wireSystem) {
        wireSystems.remove(wireSystem);
        wireSystem.elements.forEach(elementIn -> {
            elementsToWireSystemsIndex.computeIfPresent(elementIn, (element, wireSystems) -> {
                wireSystems.remove(wireSystem);
                return wireSystems.isEmpty() ? null : wireSystems;
            });
        });
        markStructureChanged();
    }

    public void addWireSystem(WireSystem wireSystem, boolean powered) {
        if (this.wireSystems.put(wireSystem, powered) == null) {
            wireSystem.elements.forEach(systemElement -> {
                List<WireSystem> wireSystemsWithElement = this.elementsToWireSystemsIndex.computeIfAbsent(systemElement, unused -> new ArrayList<>());
                if (wireSystemsWithElement.contains(wireSystem)) {
                    throw new IllegalStateException();
                }
                wireSystemsWithElement.add(wireSystem);
            });
        }
    }

    public void buildAndAddWireSystem(WireSystem.WireElement element) {
        WireSystem wireSystem = new WireSystem(this, element);
        if(!wireSystem.isEmpty()) {
            this.addWireSystem(wireSystem, false);
            wireSystems.put(wireSystem, wireSystem.update(this));
        }
        markStructureChanged();
    }

    public void rebuildWireSystemsAround(IPipeHolder holder) {
        Arrays.stream(EnumWirePart.values())
                .flatMap(part -> WireSystem.getConnectedElementsOfElement(level, new WireSystem.WireElement(holder.getPipePos(), part)).stream())
                .distinct()
                .forEach(this::buildAndAddWireSystem);
    }

    public IWireEmitter getEmitter(WireSystem.WireElement element) {
        if (element.type == WireSystem.WireElement.Type.EMITTER_SIDE) {
            if (!emittersCache.containsKey(element)) {
                if (!level.isLoaded(element.blockPos)) {
                    BuildCraftReborn.LOGGER.warn("[transport.wire] Ghost loading " + element.blockPos + " to look for an emitter!");
                }
                BlockEntity tile = level.getBlockEntity(element.blockPos);
                if (tile instanceof IPipeHolder holder) {
                    PipePluggable plug = holder.getPluggable(element.emitterSide);
                    if(plug instanceof IWireEmitter) {
                        emittersCache.put(element, (IWireEmitter) plug);
                    }
                }
                if (!emittersCache.containsKey(element)) {
                    throw new IllegalStateException("Tried to get a wire element when none existed! THIS IS A BUG " + element);
                }
            }
            return emittersCache.get(element);
        }
        return null;
    }

    public boolean isEmitterEmitting(WireSystem.WireElement element, DyeColor color) {
        if (!level.isLoaded(element.blockPos)) {
            BuildCraftReborn.LOGGER.warn("[transport.wire] Ghost loading " + element.blockPos + " to look for an emitter!");
        }
        BlockEntity tile = level.getBlockEntity(element.blockPos);
        if (tile instanceof IPipeHolder holder) {
            if (holder.getPluggable(element.emitterSide) instanceof IWireEmitter) {
                return getEmitter(element).isEmitting(color);
            }
        }
        return false;
    }

    public void tick() {
        if(gatesChanged) {
            wireSystems.replaceAll((wireSystem, oldPowered) -> {
                boolean newPowered = wireSystem.update(this);
                if (oldPowered != newPowered) {
                    changedSystems.add(wireSystem);
                }
                return newPowered;
            });
        }
        level.players().forEach(player -> {
            Map<Integer, WireSystem> changedWires = this.wireSystems.keySet().stream()
                    .filter(wireSystem -> wireSystem.isPlayerWatching(player) && (structureChanged || changedPlayers.contains(player)))
                    .collect(Collectors.toMap(ws -> ws.networkId, Function.identity()));
            if(!changedWires.isEmpty()) {
                PacketDistributor.sendToPlayer(player, WireSystemsPayload.of(changedWires));
            }
            Map<Integer, Boolean> hashesPowered = this.wireSystems.entrySet().stream()
                    .filter(systemPower ->
                            systemPower.getKey().isPlayerWatching(player) &&
                                    (structureChanged || changedSystems.contains(systemPower.getKey()) || changedPlayers.contains(player))
                    )
                    .map(systemPowered -> Pair.of(systemPowered.getKey().networkId, systemPowered.getValue()))
                    .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
            if(!hashesPowered.isEmpty()) {
                PacketDistributor.sendToPlayer(player, new WireSystemsPoweredPayload(hashesPowered));
            }
        });
        if(structureChanged || !changedSystems.isEmpty()) {
            setDirty();
        }
        structureChanged = false;
        changedSystems.clear();
        changedPlayers.clear();
    }

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider registries) {
        ListTag entriesList = new ListTag();
        wireSystems.forEach((wireSystem, powered) -> {
            CompoundTag entry = new CompoundTag();
            entry.put("wireSystem", wireSystem.writeToNBT());
            entry.putBoolean("powered", powered);
            entriesList.add(entry);
        });
        nbt.put("entries", entriesList);
        return nbt;
    }

    private static WorldSavedDataWireSystems load(CompoundTag nbt, HolderLookup.Provider registries) {
        WorldSavedDataWireSystems data = new WorldSavedDataWireSystems();
        data.readFromNBT(nbt);
        return data;
    }

    private void readFromNBT(CompoundTag nbt) {
        wireSystems.clear();
        this.elementsToWireSystemsIndex.clear();

        ListTag entriesList = nbt.getList("entries", Tag.TAG_COMPOUND);
        for(int i = 0; i < entriesList.size(); i++) {
            CompoundTag entry = entriesList.getCompound(i);
            try {
                this.addWireSystem(new WireSystem(entry.getCompound("wireSystem")), entry.getBoolean("powered"));
            } catch (RuntimeException e) {
                BuildCraftReborn.LOGGER.warn("[transport.wire] Skipping a wire system that couldn't be read", e);
            }
        }
    }

    public static WorldSavedDataWireSystems get(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            throw new UnsupportedOperationException("Attempted to get WorldSavedDataWireSystems on the client!");
        }
        WorldSavedDataWireSystems instance = serverLevel.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
        instance.level = serverLevel;
        return instance;
    }
}
