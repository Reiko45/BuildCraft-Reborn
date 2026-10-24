/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.model;

import net.minecraft.resources.ResourceLocation;

/** Holds a BuildCraft JSON model that is reloaded from the resource packs whenever the client reloads its
 * resources. */
public abstract class ModelHolder {
    public final ResourceLocation modelLocation;
    protected String failReason = "";

    public ModelHolder(ResourceLocation modelLocation) {
        this.modelLocation = modelLocation;
        ModelHolderRegistry.HOLDERS.add(this);
    }

    public ModelHolder(String modelLocation) {
        this(ResourceLocation.parse(modelLocation));
    }

    /** Loads the model file (and for static models, bakes it). Called once the block atlas is stitched. */
    protected abstract void onModelBake();

    public abstract boolean hasBakedQuads();
}
