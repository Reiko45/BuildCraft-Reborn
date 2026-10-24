/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.snapshot;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.misc.BlockUtil;

/** Loads the schematic rules: how each block and entity is copied into a blueprint, and what building it takes.
 * <p>
 * Each mod can ship rules in {@code assets/<modid>/compat/buildcraft/builders/}, listed in that folder's
 * {@code index.json}. The rule format is 1.12's; the block names and properties in selectors are 1.21's (for
 * example {@code minecraft:oak_slab[type=double]}). */
public class RulesLoader {
    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(BlockPos.class, (JsonDeserializer<BlockPos>) (json, typeOfT, context) ->
            new BlockPos(json.getAsJsonArray().get(0).getAsInt(), json.getAsJsonArray().get(1).getAsInt(),
                json.getAsJsonArray().get(2).getAsInt()))
        .registerTypeAdapter(RequiredExtractor.class, RequiredExtractor.DESERIALIZER)
        .registerTypeAdapter(EnumNbtCompareOperation.class, EnumNbtCompareOperation.DESERIALIZER)
        .registerTypeAdapter(NbtPath.class, NbtPath.DESERIALIZER)
        .registerTypeHierarchyAdapter(Tag.class, RuleNbt.DESERIALIZER)
        .registerTypeAdapterFactory(JsonSelector.TYPE_ADAPTER_FACTORY)
        .registerTypeAdapterFactory(NbtRef.TYPE_ADAPTER_FACTORY)
        .create();

    private static final List<JsonRule> RULES = new ArrayList<>();
    /** The namespaces whose blocks and entities are copied into blueprints. */
    public static final Set<String> READ_DOMAINS = new HashSet<>();
    private static final LoadingCache<Pair<BlockState, Optional<CompoundTag>>, Set<JsonRule>> BLOCK_RULES_CACHE =
        CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(CacheLoader.from(pair -> getBlockRulesInternal(pair.getLeft(), pair.getRight().orElse(null))));

    public static synchronized void loadAll() {
        RULES.clear();
        READ_DOMAINS.clear();
        BLOCK_RULES_CACHE.invalidateAll();
        for (IModInfo mod : ModList.get().getMods()) {
            String domain = mod.getModId();
            if (READ_DOMAINS.contains(domain)) {
                continue;
            }
            IModFileInfo fileInfo = ModList.get().getModFileById(domain);
            if (fileInfo == null) {
                continue;
            }
            String base = "assets/" + domain + "/compat/buildcraft/builders/";
            Path index = fileInfo.getFile().findResource(base + "index.json");
            if (index == null || !Files.exists(index)) {
                continue;
            }
            try {
                List<String> names;
                try (Reader reader = Files.newBufferedReader(index, StandardCharsets.UTF_8)) {
                    names = GSON.fromJson(reader, new TypeToken<List<String>>() {}.getType());
                }
                for (String name : names) {
                    Path file = fileInfo.getFile().findResource(base + name + ".json");
                    if (file == null || !Files.exists(file)) {
                        throw new IOException("Can't read " + base + name + ".json");
                    }
                    try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                        List<JsonRule> rules = GSON.fromJson(reader, new TypeToken<List<JsonRule>>() {}.getType());
                        RULES.addAll(rules);
                    }
                }
                READ_DOMAINS.add(domain);
            } catch (IOException | RuntimeException e) {
                BuildCraftReborn.LOGGER.error("[builders.rules] Failed to load the schematic rules of {}", domain, e);
            }
        }
        READ_DOMAINS.add("minecraft");
        READ_DOMAINS.add(BuildCraftReborn.MODID);
        if (FMLEnvironment.production) {
            // As in 8.0: BuildCraft's own blocks are only copied in development runs
            READ_DOMAINS.remove(BuildCraftReborn.MODID);
        }
        BuildCraftReborn.LOGGER.info("[builders.rules] Loaded {} schematic rules", RULES.size());
    }

    /** @return the rules whose selector names this block: {@code namespace:block} or
     *         {@code namespace:block[property=value, ...]}. */
    static boolean selectorMatches(String base, BlockState blockState) {
        boolean complex = base.contains("[");
        String name = complex ? base.substring(0, base.indexOf('[')) : base;
        // 1.21: "#namespace:tag" matches every block in a block tag (1.12 had to list each block).
        boolean isTag = name.startsWith("#");
        ResourceLocation id = ResourceLocation.tryParse(isTag ? name.substring(1) : name);
        if (id == null) {
            return false;
        }
        if (isTag) {
            if (!blockState.is(TagKey.create(Registries.BLOCK, id))) {
                return false;
            }
        } else {
            Block block = BuiltInRegistries.BLOCK.get(id);
            if (block == Blocks.AIR && !id.getPath().equals("air") || block != blockState.getBlock()) {
                return false;
            }
        }
        if (!complex) {
            return true;
        }
        for (String nameValueString : base.substring(base.indexOf('[') + 1, base.indexOf(']')).split(",")) {
            String[] nameValue = nameValueString.trim().split("=");
            if (nameValue.length != 2) {
                return false;
            }
            Property<?> property = blockState.getBlock().getStateDefinition().getProperty(nameValue[0]);
            if (property == null || !BlockUtil.getPropertyStringValue(blockState, property).equals(nameValue[1])) {
                return false;
            }
        }
        return true;
    }

    private static Set<JsonRule> getBlockRulesInternal(BlockState blockState, @Nullable CompoundTag tileNbt) {
        CompoundTag nbt = tileNbt == null ? new CompoundTag() : tileNbt;
        Set<JsonRule> found = new HashSet<>();
        synchronized (RulesLoader.class) {
            for (JsonRule rule : RULES) {
                if (rule.selectors != null && rule.selectors.stream().filter(Objects::nonNull)
                    .anyMatch(selector -> selector.matches(base -> selectorMatches(base, blockState), nbt))) {
                    found.add(rule);
                }
            }
        }
        return found;
    }

    public static Set<JsonRule> getRules(BlockState blockState, @Nullable CompoundTag tileNbt) {
        return BLOCK_RULES_CACHE.getUnchecked(Pair.of(blockState, Optional.ofNullable(tileNbt)));
    }

    public static Set<JsonRule> getRules(ResourceLocation entityId, CompoundTag entityNbt) {
        Set<JsonRule> found = new HashSet<>();
        synchronized (RulesLoader.class) {
            for (JsonRule rule : RULES) {
                if (rule.selectors != null && rule.selectors.stream().filter(Objects::nonNull)
                    .anyMatch(selector -> selector.matches(entityId.toString()::equals, entityNbt))) {
                    found.add(rule);
                }
            }
        }
        return found;
    }
}
