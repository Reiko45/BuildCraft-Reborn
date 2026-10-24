/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import net.neoforged.fml.ModLoadingContext;

/** 1.21: the name helper and the world properties robots use ("soft", "wood", "leaves", ...). The fake player
 * provider lives in the mod itself. */
public final class BuildCraftAPI {
    private static final Logger LOGGER = LogUtils.getLogger();

    /** Blocks that count as soft (robots move through them) on top of air, replaceable blocks, fluids and plants. */
    public static final Set<net.minecraft.world.level.block.Block> softBlocks = new HashSet<>();
    public static final Map<String, IWorldProperty> worldProperties = new HashMap<>();

    private BuildCraftAPI() {}

    public static IWorldProperty getWorldProperty(String name) {
        return worldProperties.get(name);
    }

    public static void registerWorldProperty(String name, IWorldProperty property) {
        if (worldProperties.containsKey(name)) {
            LOGGER.warn("The WorldProperty key '{}' is being overridden with {}!", name,
                property.getClass().getSimpleName());
        }
        worldProperties.put(name, property);
    }

    /** @return True if a robot (or a builder) can move through the block: air, and blocks like grass and snow. */
    public static boolean isSoftBlock(Level world, BlockPos pos) {
        IWorldProperty soft = worldProperties.get("soft");
        return soft != null ? soft.get(world, pos) : world.isEmptyBlock(pos);
    }

    /** @return the name as a resource location, in the namespace of the mod being loaded when it has none. */
    public static ResourceLocation nameToResourceLocation(String name) {
        if (name.indexOf(':') > 0) {
            return ResourceLocation.parse(name);
        }
        String namespace = ModLoadingContext.get().getActiveNamespace();
        if (namespace == null || namespace.isEmpty() || "minecraft".equals(namespace)) {
            throw new IllegalStateException("Illegal name " + name + ". Provide a namespace to register it correctly.");
        }
        return ResourceLocation.fromNamespaceAndPath(namespace, name);
    }
}
