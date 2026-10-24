/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.debug;

import org.jetbrains.annotations.Nullable;

/** Holds the current {@link IAdvDebugTarget} (picked with the debugger item). Use {@link DebugRenderHelper} for helpers
 * when rendering debuggables.
 * <p>
 * 1.21: the client also forgets its target when the server stops debugging it (1.12 kept drawing it). */
public enum BCAdvDebugging {
    INSTANCE;

    @Nullable
    private IAdvDebugTarget target = null;
    @Nullable
    IAdvDebugTarget targetClient = null;

    public static boolean isBeingDebugged(IAdvDebugTarget target) {
        return INSTANCE.target == target;
    }

    public static void setCurrentDebugTarget(@Nullable IAdvDebugTarget target) {
        if (INSTANCE.target != null && INSTANCE.target != target) {
            INSTANCE.target.disableDebugging();
        }
        INSTANCE.target = target;
    }

    public static void setClientDebugTarget(@Nullable IAdvDebugTarget target) {
        INSTANCE.targetClient = target;
    }

    /** Called when the server says it stopped debugging {@code target}. */
    public static void clearClientDebugTarget(IAdvDebugTarget target) {
        if (INSTANCE.targetClient == target) {
            INSTANCE.targetClient = null;
        }
    }

    @Nullable
    public static IAdvDebugTarget getClientDebugTarget() {
        return INSTANCE.targetClient;
    }

    public void onServerPostTick() {
        if (target != null) {
            if (!target.doesExistInWorld()) {
                target.disableDebugging();
                target = null;
            } else {
                target.sendDebugState();
            }
        }
    }

    /** Forgets the server target (when the server stops). */
    public void onServerStopped() {
        target = null;
    }
}
