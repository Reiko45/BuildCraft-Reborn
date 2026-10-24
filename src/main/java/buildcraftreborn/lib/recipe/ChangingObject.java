/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.recipe;

import java.util.Arrays;
import java.util.List;

/** One of several objects, changing every second, for showing the choices of a recipe. */
public class ChangingObject<T> {
    protected final T[] options;
    private final int hash;
    private int timeGap = 1000;

    public ChangingObject(T[] options) {
        if (options.length == 0) {
            throw new IllegalStateException("Must provide at least 1 option!");
        }
        this.options = options;
        hash = computeHash();
    }

    protected int computeHash() {
        return Arrays.hashCode(options);
    }

    /** @return The option that should be displayed at the current time. */
    public T get() {
        return get(0);
    }

    public T get(int indexOffset) {
        long now = (System.currentTimeMillis() / timeGap) % options.length;
        int i = (int) now + indexOffset;
        return options[i % options.length];
    }

    public List<T> getOptions() {
        return Arrays.asList(options);
    }

    /** Sets the time gap between different options showing, in milliseconds. Defaults to 1000 (1 second) */
    public void setTimeGap(int timeGap) {
        this.timeGap = timeGap;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        ChangingObject<?> other = (ChangingObject<?>) obj;
        if (hash != other.hash) return false;
        return Arrays.equals(options, other.options);
    }
}
