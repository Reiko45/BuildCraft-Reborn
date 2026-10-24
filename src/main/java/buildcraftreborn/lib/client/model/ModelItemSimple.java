/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.model;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.lib.misc.SpriteUtil;

/** An item model made of a list of quads, with a choice of display transforms (as a block, an item or a pluggable). */
public class ModelItemSimple implements BakedModel {
    public static final ItemTransforms TRANSFORM_DEFAULT = ItemTransforms.NO_TRANSFORMS;
    public static final ItemTransforms TRANSFORM_BLOCK;
    public static final ItemTransforms TRANSFORM_PLUG_AS_ITEM;
    public static final ItemTransforms TRANSFORM_PLUG_AS_ITEM_BIGGER;
    public static final ItemTransforms TRANSFORM_PLUG_AS_BLOCK;
    public static final ItemTransforms TRANSFORM_ITEM;

    static {
        // Values taken from "minecraft:models/block/block.json"
        ItemTransform thirdp_left = def(75, 45, 0, 0, 2.5, 0, 0.375);
        ItemTransform thirdp_right = def(75, 225, 0, 0, 2.5, 0, 0.375);
        ItemTransform firstp_left = def(0, 225, 0, 0, 0, 0, 0.4);
        ItemTransform firstp_right = def(0, 45, 0, 0, 0, 0, 0.4);
        ItemTransform head = def(0, 0, 0, 0, 0, 0, 1);
        ItemTransform gui = def(30, 225, 0, 0, 0, 0, 0.625);
        ItemTransform ground = def(0, 0, 0, 0, 3, 0, 0.25);
        ItemTransform fixed = def(0, 0, 0, 0, 0, 0, 0.5);
        TRANSFORM_BLOCK = transforms(thirdp_left, thirdp_right, firstp_left, firstp_right, head, gui, ground, fixed);

        ItemTransform item_head = def(0, 0, 0, 0, 0, 0, 1);
        ItemTransform item_gui = def(0, 90, 0, 0, 0, 0, 1);
        ItemTransform item_ground = def(0, 0, 0, 0, 3, 0, 0.5);
        ItemTransform item_fixed = def(0, 0, 0, 0, 0, 0, 0.85);
        firstp_left = def(0, 225, 0, 0, 0, -4, 0.4);
        firstp_right = def(0, 45, 0, 0, 0, -4, 0.4);
        TRANSFORM_PLUG_AS_ITEM = transforms(thirdp_left, thirdp_right, firstp_left, firstp_right, item_head, item_gui,
            item_ground, item_fixed);
        TRANSFORM_PLUG_AS_ITEM_BIGGER = scale(TRANSFORM_PLUG_AS_ITEM, 1.8);

        thirdp_left = def(75, 45, 0, 0, 2.5, 0, 0.375);
        thirdp_right = def(75, 225, 0, 0, 2.5, 0, 0.375);
        firstp_left = def(0, 45, 0, 0, 0, 0, 0.4);
        firstp_right = def(0, 225, 0, 0, 0, 0, 0.4);
        gui = def(30, 135, 0, -3, 1.5, 0, 0.625);
        TRANSFORM_PLUG_AS_BLOCK = transforms(thirdp_left, thirdp_right, firstp_left, firstp_right, head, gui, ground,
            fixed);

        ground = def(0, 0, 0, 0, 2, 0, 0.5);
        head = def(0, 180, 0, 0, 13, 7, 1);
        thirdp_right = def(0, 0, 0, 0, 3, 1, 0.55);
        firstp_right = def(0, -90, 25, 1.13, 3.2, 1.13, 0.68);
        thirdp_left = thirdp_right;
        firstp_left = firstp_right;
        fixed = def(0, 180, 0, 0, 0, 0, 1);
        gui = def(0, 0, 0, 0, 0, 0, 1);
        TRANSFORM_ITEM = transforms(thirdp_left, thirdp_right, firstp_left, firstp_right, head, gui, ground, fixed);
    }

    private static ItemTransforms transforms(ItemTransform thirdLeft, ItemTransform thirdRight,
        ItemTransform firstLeft, ItemTransform firstRight, ItemTransform head, ItemTransform gui,
        ItemTransform ground, ItemTransform fixed) {
        return new ItemTransforms(thirdLeft, thirdRight, firstLeft, firstRight, head, gui, ground, fixed);
    }

    private static ItemTransforms scale(ItemTransforms from, double by) {
        return transforms(scale(from.thirdPersonLeftHand, by), scale(from.thirdPersonRightHand, by),
            scale(from.firstPersonLeftHand, by), scale(from.firstPersonRightHand, by), scale(from.head, by),
            scale(from.gui, by), scale(from.ground, by), scale(from.fixed, by));
    }

    private static ItemTransform scale(ItemTransform from, double by) {
        return new ItemTransform(from.rotation, from.translation, new Vector3f(from.scale).mul((float) by));
    }

    private static ItemTransform def(double rx, double ry, double rz, double tx, double ty, double tz,
        double scale) {
        Vector3f rot = new Vector3f((float) rx, (float) ry, (float) rz);
        Vector3f translate = new Vector3f((float) tx / 16f, (float) ty / 16f, (float) tz / 16f);
        return new ItemTransform(rot, translate, new Vector3f((float) scale, (float) scale, (float) scale));
    }

    private final boolean isGui3d;
    private final List<BakedQuad> quads;
    private final TextureAtlasSprite particle;
    private final ItemTransforms transforms;

    public ModelItemSimple(List<BakedQuad> quads, ItemTransforms transforms, boolean isGui3d) {
        this.quads = quads == null ? ImmutableList.of() : quads;
        this.isGui3d = isGui3d;
        this.particle = this.quads.isEmpty() ? SpriteUtil.missingSprite() : this.quads.get(0).getSprite();
        this.transforms = transforms;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return side == null ? quads : ImmutableList.of();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return isGui3d;
    }

    @Override
    public boolean usesBlockLight() {
        return isGui3d;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return particle;
    }

    @Override
    public ItemTransforms getTransforms() {
        return transforms;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
