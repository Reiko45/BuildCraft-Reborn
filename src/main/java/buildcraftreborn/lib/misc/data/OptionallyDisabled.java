/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.misc.data;

import java.util.NoSuchElementException;

import org.jetbrains.annotations.Nullable;

/** Either a value, or the reason why there isn't one (1.12's {@code OptionallyDisabled}). */
public final class OptionallyDisabled<T> {
    @Nullable
    private final T value;
    @Nullable
    private final String disabledReason;

    public OptionallyDisabled(T value) {
        this.value = value;
        this.disabledReason = null;
    }

    public OptionallyDisabled(String disabledReason) {
        this.value = null;
        this.disabledReason = disabledReason;
    }

    public boolean isPresent() {
        return disabledReason == null;
    }

    public T get() {
        if (!isPresent()) {
            throw new NoSuchElementException(disabledReason);
        }
        return value;
    }

    public String getDisabledReason() {
        if (isPresent()) {
            throw new IllegalStateException("Not disabled");
        }
        return disabledReason;
    }

    @Override
    public String toString() {
        return isPresent() ? "Present: " + value : "Disabled: " + disabledReason;
    }
}
