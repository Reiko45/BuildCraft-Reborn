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

/** Everything drawn in a pipe's cutout layer: the pipe itself and its pluggables. */
public final class PipeAllCutoutKey {
    private final PipeBaseCutoutKey cutout;
    private final PluggableKey pluggable;
    private final int hash;

    public PipeAllCutoutKey(PipeModelKey pipeModel, IPipeHolder holder) {
        cutout = new PipeBaseCutoutKey(pipeModel);
        pluggable = new PluggableKey(EnumRenderLayer.CUTOUT, holder);
        hash = Objects.hash(cutout, pluggable);
    }

    public PipeBaseCutoutKey getBaseCutout() {
        return cutout;
    }

    public PluggableKey getPluggable() {
        return pluggable;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PipeAllCutoutKey other = (PipeAllCutoutKey) obj;
        return cutout.equals(other.cutout) && pluggable.equals(other.pluggable);
    }

    @Override
    public String toString() {
        return "PipeAllCutoutKey [base=" + cutout + ", pluggable = " + pluggable + "]";
    }
}
