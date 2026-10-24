/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.client.model.key;

import java.util.Objects;

import buildcraftreborn.api.core.render.EnumRenderLayer;
import buildcraftreborn.api.transport.pipe.IPipeHolder;

/** Everything drawn in a pipe's translucent layer: paint (for translucent colouring) and pluggables. */
public final class PipeAllTranslucentKey {
    private final PipeBaseTranslucentKey translucent;
    private final PluggableKey pluggable;
    private final int hash;

    public PipeAllTranslucentKey(PipeModelKey pipeModel, IPipeHolder holder) {
        translucent = new PipeBaseTranslucentKey(pipeModel);
        pluggable = new PluggableKey(EnumRenderLayer.TRANSLUCENT, holder);
        hash = Objects.hash(translucent, pluggable);
    }

    public PipeBaseTranslucentKey getBaseTranslucent() {
        return translucent;
    }

    public PluggableKey getPluggable() {
        return pluggable;
    }

    /** @return true if there's nothing to draw. */
    public boolean isEmpty() {
        return !translucent.shouldRender() && pluggable.getKeys().isEmpty();
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PipeAllTranslucentKey other = (PipeAllTranslucentKey) obj;
        return translucent.equals(other.translucent) && pluggable.equals(other.pluggable);
    }

    @Override
    public String toString() {
        return "PipeAllTranslucentKey [base=" + translucent + ", pluggable = " + pluggable + "]";
    }
}
