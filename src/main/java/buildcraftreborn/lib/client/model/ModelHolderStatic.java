/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.model;

import net.minecraft.resources.ResourceLocation;

import buildcraftreborn.lib.expression.DefaultContexts;

/** Holds a model that never changes (except when resources reload). Baked once after every reload.
 * <p>
 * 1.21: loaded with the variable model loader (a model without variables is simply constant), which 1.12 also used for
 * most of these. */
public class ModelHolderStatic extends ModelHolder {
    private final ModelHolderVariable delegate;
    private MutableQuad[] cutout = MutableQuad.EMPTY_ARRAY, translucent = MutableQuad.EMPTY_ARRAY;
    private boolean baked = false;

    public ModelHolderStatic(String location) {
        this(ResourceLocation.parse(location));
    }

    public ModelHolderStatic(ResourceLocation location) {
        super(location);
        // The delegate registers itself too, but is baked here so the order is right.
        ModelHolderRegistry.HOLDERS.remove(this);
        delegate = new ModelHolderVariable(location, DefaultContexts.createWithAll());
        ModelHolderRegistry.HOLDERS.remove(delegate);
        ModelHolderRegistry.HOLDERS.add(this);
    }

    @Override
    public boolean hasBakedQuads() {
        return baked;
    }

    @Override
    protected void onModelBake() {
        delegate.onModelBake();
        failReason = delegate.failReason;
        cutout = delegate.getCutoutQuads();
        translucent = delegate.getTranslucentQuads();
        baked = delegate.hasBakedQuads();
    }

    public MutableQuad[] getCutoutQuads() {
        return cutout;
    }

    public MutableQuad[] getTranslucentQuads() {
        return translucent;
    }
}
