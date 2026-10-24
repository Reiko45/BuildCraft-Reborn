/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.robots;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.nbt.CompoundTag;

/** Something a robot can reserve so other robots leave it alone (a block it's breaking, a request it's delivering). */
public abstract class ResourceId {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected ResourceId() {}

    public void writeToNBT(CompoundTag nbt) {
        nbt.putString("resourceName", RobotManager.getResourceIdName(getClass()));
    }

    protected void readFromNBT(CompoundTag nbt) {}

    @Nullable
    public static ResourceId load(CompoundTag nbt) {
        try {
            Class<? extends ResourceId> cls = RobotManager.getResourceIdByName(nbt.getString("resourceName"));
            if (cls == null) {
                return null;
            }
            ResourceId id = cls.getConstructor().newInstance();
            id.readFromNBT(nbt);

            return id;
        } catch (Throwable e) {
            LOGGER.error("Couldn't load the robot resource {}", nbt.getString("resourceName"), e);
        }

        return null;
    }
}
