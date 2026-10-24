/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/** The robotics config ({@code buildcraftreborn-robotics.toml}). 7.1 had only the board blacklist. */
public final class BCRoboticsConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.ConfigValue<List<? extends String>> BOARD_BLACKLIST;

    private static final Set<String> blacklistedBoards = new HashSet<>();

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("general");
        BOARD_BLACKLIST = b.comment("Robot boards that can't be programmed, by id (such as \"buildcraftreborn:bomber\").")
            .defineListAllowEmpty("boards.blacklist", List.of(), () -> "", o -> o instanceof String);
        b.pop();
        SPEC = b.build();
    }

    private BCRoboticsConfig() {}

    public static void onConfigLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            reloadConfig();
        }
    }

    public static void reloadConfig() {
        blacklistedBoards.clear();
        if (SPEC.isLoaded()) {
            blacklistedBoards.addAll(BOARD_BLACKLIST.get());
        }
    }

    public static boolean isBoardBlacklisted(String id) {
        return blacklistedBoards.contains(id);
    }
}
