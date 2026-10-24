/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.lib.misc;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;

import net.neoforged.neoforge.server.ServerLifecycleHooks;

/** The registries to read item stacks and other registry-backed NBT with, where no level is at hand (the builders'
 * schematics work out the items they need from saved NBT alone). 1.12 needed none. */
public final class RegistryAccessUtil {
    /** Set by the client: the registries of the level it is in, or null. */
    @Nullable
    public static Supplier<RegistryAccess> clientRegistries;

    private RegistryAccessUtil() {}

    public static RegistryAccess current() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.registryAccess();
        }
        if (clientRegistries != null) {
            RegistryAccess access = clientRegistries.get();
            if (access != null) {
                return access;
            }
        }
        return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }
}
