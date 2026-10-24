/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.model.json;

import java.util.Collections;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import buildcraftreborn.lib.client.model.MutableQuad;
import buildcraftreborn.lib.client.model.ResourceLoaderContext;
import buildcraftreborn.lib.client.model.json.JsonVariableModel.ITextureGetter;
import buildcraftreborn.lib.expression.FunctionContext;

public class VariablePartContainer extends JsonVariableModelPart {

    public final JsonVariableModel model;

    public VariablePartContainer(JsonObject obj, FunctionContext fnCtx, ResourceLoaderContext ctx) {
        if (obj.has("textures")) {
            throw new JsonSyntaxException("Contained variable parts must not have 'textures'");
        }
        if (obj.has("variables")) {
            throw new JsonSyntaxException("Contained variable parts must not have 'variables'");
        }
        if (obj.has("translucent")) {
            throw new JsonSyntaxException("Contained variable parts must not have 'translucent'");
        }
        model = new JsonVariableModel(obj, fnCtx, ctx);
    }

    @Override
    public void addQuads(List<MutableQuad> to, ITextureGetter spriteLookup) {
        Collections.addAll(to, model.bakePart(model.cutoutElements, spriteLookup));
    }
}
