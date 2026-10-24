/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.sprite;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import buildcraftreborn.api.core.render.ISprite;

/** Holders for block atlas sprites used by in-world renderers and models (pipes, wires, power flow). Client only.
 * <p>
 * 1.21: there's no stitching step to hook into; every texture under {@code textures/block/} is in the block atlas, so
 * a holder just looks its sprite up (and caches it until the atlas is rebuilt on a resource reload). */
public final class SpriteHolderRegistry {
    private static final Map<ResourceLocation, SpriteHolder> HOLDER_MAP = new ConcurrentHashMap<>();

    private SpriteHolderRegistry() {}

    /** @param location A block atlas location, like {@code buildcraftreborn:block/pipes/overlay_stained}. */
    public static SpriteHolder getHolder(ResourceLocation location) {
        return HOLDER_MAP.computeIfAbsent(location, SpriteHolder::new);
    }

    public static SpriteHolder getHolder(String location) {
        return getHolder(ResourceLocation.parse(location));
    }

    /** Forgets every looked up sprite. Listener for {@code ModelEvent.BakingCompleted}, which follows every rebuild of
     * the block atlas. */
    public static void onModelBake(net.neoforged.neoforge.client.event.ModelEvent.BakingCompleted event) {
        for (SpriteHolder holder : HOLDER_MAP.values()) {
            holder.sprite = null;
        }
    }

    /** A block atlas sprite that follows resource reloads. */
    public static final class SpriteHolder implements ISprite {
        public final ResourceLocation spriteLocation;
        private TextureAtlasSprite sprite;

        private SpriteHolder(ResourceLocation spriteLocation) {
            this.spriteLocation = spriteLocation;
        }

        public TextureAtlasSprite getSprite() {
            if (sprite == null) {
                sprite = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS)
                    .getSprite(spriteLocation);
            }
            return sprite;
        }

        @Override
        public ResourceLocation getTexture() {
            return InventoryMenu.BLOCK_ATLAS;
        }

        @Override
        public double getInterpU(double u) {
            TextureAtlasSprite s = getSprite();
            return s.getU0() + u * (s.getU1() - s.getU0());
        }

        @Override
        public double getInterpV(double v) {
            TextureAtlasSprite s = getSprite();
            return s.getV0() + v * (s.getV1() - s.getV0());
        }

        @Override
        public String toString() {
            return "SpriteHolder[" + spriteLocation + "]";
        }
    }
}
