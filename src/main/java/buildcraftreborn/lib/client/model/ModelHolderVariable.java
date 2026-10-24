/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonParseException;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.client.model.ModelUtil.TexturedFace;
import buildcraftreborn.lib.client.model.json.JsonTexture;
import buildcraftreborn.lib.client.model.json.JsonVariableModel;
import buildcraftreborn.lib.expression.FunctionContext;
import buildcraftreborn.lib.expression.node.value.ITickableNode;
import buildcraftreborn.lib.misc.SpriteUtil;

/** Holds a model that can be changed by variables. Define the variables in a {@link FunctionContext} first, then
 * create the holder (usually in a static initializer). The JSON format matches vanilla's, except that any number may be
 * an expression over those variables.
 * <p>
 * 1.21: BuildCraft's JSON models live under {@code assets/<namespace>/bcmodels/} so the vanilla model loader doesn't
 * try to read them. */
public class ModelHolderVariable extends ModelHolder {
    public final Map<String, TextureAtlasSprite> customSprites = new HashMap<>();
    private final FunctionContext context;
    private JsonVariableModel rawModel;
    private boolean unseen = true;

    public ModelHolderVariable(String modelLocation, FunctionContext context) {
        super(modelLocation);
        this.context = context;
    }

    public ModelHolderVariable(ResourceLocation modelLocation, FunctionContext context) {
        super(modelLocation);
        this.context = context;
    }

    @Override
    public boolean hasBakedQuads() {
        return rawModel != null;
    }

    @Override
    protected void onModelBake() {
        rawModel = null;
        failReason = null;
        try {
            rawModel = JsonVariableModel.deserialize(modelLocation, context);
        } catch (JsonParseException | IOException e) {
            failReason = "The model could not be loaded: " + e.getMessage();
            BuildCraftReborn.LOGGER.warn("[lib.model.holder] Failed to load the model {}", modelLocation, e);
        }
    }

    private TexturedFace lookupTexture(String lookup) {
        int attempts = 0;
        JsonTexture texture = new JsonTexture(lookup);
        while (texture.location.startsWith("#") && attempts < 10) {
            JsonTexture tex = rawModel.textures.get(texture.location);
            if (tex == null) break;
            texture = texture.inParent(tex);
            attempts++;
        }
        lookup = texture.location;
        TextureAtlasSprite sprite;
        if (lookup.startsWith("~")) {
            sprite = customSprites.get(lookup.substring(1));
            if (sprite == null) {
                sprite = SpriteUtil.missingSprite();
            }
        } else if (lookup.startsWith("#")) {
            sprite = SpriteUtil.missingSprite();
        } else {
            sprite = SpriteUtil.getBlockSprite(lookup);
        }
        TexturedFace face = new TexturedFace();
        face.sprite = sprite;
        face.faceData = texture.faceData;
        return face;
    }

    private void printNoModelWarning() {
        if (unseen) {
            unseen = false;
            BuildCraftReborn.LOGGER.warn("[lib.model.holder] Tried to use the model {} before it was baked!",
                modelLocation);
        }
    }

    @Nullable
    public JsonVariableModel getModel() {
        if (rawModel == null) {
            printNoModelWarning();
        }
        return rawModel;
    }

    public ITickableNode[] createTickableNodes() {
        if (rawModel == null) {
            printNoModelWarning();
            return new ITickableNode[0];
        }
        return rawModel.createTickableNodes();
    }

    public MutableQuad[] getCutoutQuads() {
        if (rawModel == null) {
            printNoModelWarning();
            return MutableQuad.EMPTY_ARRAY;
        }
        return rawModel.bakePart(rawModel.cutoutElements, this::lookupTexture);
    }

    public MutableQuad[] getTranslucentQuads() {
        if (rawModel == null) {
            printNoModelWarning();
            return MutableQuad.EMPTY_ARRAY;
        }
        return rawModel.bakePart(rawModel.translucentElements, this::lookupTexture);
    }
}
