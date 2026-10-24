/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;

import net.neoforged.fml.loading.FMLPaths;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.expression.node.value.NodeVariableBoolean;

/** Remembers small per-GUI settings (which ledgers are open) between sessions, in
 * {@code config/buildcraftreborn-gui.json}. A simplified port of 1.12's {@code GuiConfigManager}: only booleans are
 * needed so far. Client only. */
public final class GuiConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, Property> PROPERTIES = new HashMap<>();
    private static JsonObject loaded;

    private GuiConfigManager() {}

    private static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve(BuildCraftReborn.MODID + "-gui.json");
    }

    private static JsonObject loaded() {
        if (loaded == null) {
            loaded = new JsonObject();
            Path path = file();
            if (Files.exists(path)) {
                try (Reader reader = Files.newBufferedReader(path)) {
                    JsonElement element = GSON.fromJson(reader, JsonElement.class);
                    if (element != null && element.isJsonObject()) {
                        loaded = element.getAsJsonObject();
                    }
                } catch (IOException | RuntimeException e) {
                    BuildCraftReborn.LOGGER.warn("[lib.gui.config] Couldn't read {}", path, e);
                }
            }
        }
        return loaded;
    }

    private static void save() {
        Path path = file();
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(loaded(), writer);
        } catch (IOException e) {
            BuildCraftReborn.LOGGER.warn("[lib.gui.config] Couldn't write {}", path, e);
        }
    }

    /** @return A boolean that is saved whenever it is {@link NodeVariableBoolean#set(boolean) set}. */
    public static NodeVariableBoolean getOrAddBoolean(ResourceLocation gui, String name, boolean defaultValue) {
        String key = gui + "#" + name;
        Property existing = PROPERTIES.get(key);
        if (existing != null) {
            return existing;
        }
        JsonObject guiObject = loaded().has(gui.toString()) && loaded().get(gui.toString()).isJsonObject()
            ? loaded().getAsJsonObject(gui.toString()) : null;
        boolean value = defaultValue;
        if (guiObject != null && guiObject.has(name) && guiObject.get(name).isJsonPrimitive()) {
            value = guiObject.get(name).getAsBoolean();
        }
        Property prop = new Property(gui.toString(), name);
        prop.value = value;
        PROPERTIES.put(key, prop);
        return prop;
    }

    private static final class Property extends NodeVariableBoolean {
        private final String gui, key;

        Property(String gui, String key) {
            super(key);
            this.gui = gui;
            this.key = key;
        }

        @Override
        public void set(boolean value) {
            boolean changed = this.value != value;
            super.set(value);
            if (changed) {
                JsonObject root = loaded();
                JsonObject guiObject = root.has(gui) && root.get(gui).isJsonObject() ? root.getAsJsonObject(gui)
                    : new JsonObject();
                guiObject.addProperty(key, value);
                root.add(gui, guiObject);
                save();
            }
        }
    }
}
