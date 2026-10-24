/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.client.model.data.ModelData;

import buildcraftreborn.api.schematics.ISchematicBlock;
import buildcraftreborn.builders.snapshot.Blueprint;
import buildcraftreborn.builders.snapshot.ClientSnapshots;
import buildcraftreborn.builders.snapshot.SchematicBlockDefault;
import buildcraftreborn.builders.snapshot.Snapshot;
import buildcraftreborn.builders.snapshot.Template;

/** Draws a slowly turning 3D preview of a blueprint or template in a GUI (the replacer, and the tooltips of
 * blueprints, templates and single schematics). Templates are drawn as quartz, as in 1.12.
 * <p>
 * 1.21: the blocks are drawn from their models, without 1.12's fake world, so tile entities, entities and fluids
 * aren't shown, and very large snapshots aren't previewed at all. */
public final class SnapshotPreview {
    /** Snapshots with more positions than this aren't previewed. */
    private static final int MAX_VOLUME = 128 * 128 * 128;
    private static final int MAX_QUADS = 200_000;

    private static final Cache<Snapshot.Key, List<Entry>> CACHE =
        CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).maximumSize(16).build();

    private record Entry(int x, int y, int z, BakedQuad quad, int colour) {}

    private SnapshotPreview() {}

    public static void render(GuiGraphics graphics, @Nullable Snapshot.Header header, int x, int y, int width,
        int height) {
        if (header == null) {
            return;
        }
        Snapshot snapshot = ClientSnapshots.INSTANCE.getSnapshot(header.key);
        if (snapshot != null) {
            render(graphics, snapshot, x, y, width, height);
        }
    }

    public static void render(GuiGraphics graphics, Snapshot snapshot, int x, int y, int width, int height) {
        BlockPos size = snapshot.size;
        if (size == null || (long) size.getX() * size.getY() * size.getZ() > MAX_VOLUME) {
            return;
        }
        List<Entry> entries = CACHE.getIfPresent(snapshot.key);
        if (entries == null) {
            entries = build(snapshot);
            CACHE.put(snapshot.key, entries);
        }
        if (entries.isEmpty()) {
            return;
        }
        double diameter = Math.sqrt(size.getX() * size.getX() + size.getY() * size.getY()
            + size.getZ() * size.getZ());
        float scale = (float) (Math.min(width, height) / Math.max(1, diameter));

        graphics.flush();
        graphics.enableScissor(x, y, x + width, y + height);
        PoseStack pose = graphics.pose();
        pose.pushPose();
        // Above the GUI background, below tooltips (scale * diameter / 2 is at most the preview's half size)
        pose.translate(x + width / 2F, y + height / 2F, 150 + Math.min(width, height) / 2F);
        pose.scale(scale, -scale, scale);
        pose.mulPose(Axis.XP.rotationDegrees(20));
        pose.mulPose(Axis.YP.rotationDegrees((System.currentTimeMillis() % 36000) / 100F));
        pose.translate(-size.getX() / 2F, -size.getY() / 2F, -size.getZ() / 2F);
        Lighting.setupFor3DItems();
        VertexConsumer buffer = graphics.bufferSource().getBuffer(Sheets.cutoutBlockSheet());
        for (Entry entry : entries) {
            pose.pushPose();
            pose.translate(entry.x, entry.y, entry.z);
            int c = entry.colour;
            buffer.putBulkData(pose.last(), entry.quad, ((c >> 16) & 0xFF) / 255F, ((c >> 8) & 0xFF) / 255F,
                (c & 0xFF) / 255F, 1, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            pose.popPose();
        }
        graphics.flush();
        pose.popPose();
        graphics.disableScissor();
        Lighting.setupForFlatItems();
    }

    @Nullable
    private static BlockState stateAt(Snapshot snapshot, int x, int y, int z) {
        BlockPos size = snapshot.size;
        if (x < 0 || y < 0 || z < 0 || x >= size.getX() || y >= size.getY() || z >= size.getZ()) {
            return null;
        }
        int index = snapshot.posToIndex(x, y, z);
        if (snapshot instanceof Template template) {
            return template.data.get(index) ? Blocks.QUARTZ_BLOCK.defaultBlockState() : null;
        }
        if (snapshot instanceof Blueprint blueprint) {
            if (index >= blueprint.data.length) {
                return null;
            }
            int paletteIndex = blueprint.data[index];
            if (paletteIndex < 0 || paletteIndex >= blueprint.palette.size()) {
                return null;
            }
            ISchematicBlock schematic = blueprint.palette.get(paletteIndex);
            return schematic instanceof SchematicBlockDefault def ? def.getBlockState() : null;
        }
        return null;
    }

    private static List<Entry> build(Snapshot snapshot) {
        List<Entry> entries = new ArrayList<>();
        Minecraft mc = Minecraft.getInstance();
        RandomSource random = RandomSource.create(42);
        BlockPos size = snapshot.size;
        for (int z = 0; z < size.getZ(); z++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int x = 0; x < size.getX(); x++) {
                    BlockState state = stateAt(snapshot, x, y, z);
                    if (state == null || state.isAir() || state.getRenderShape() != RenderShape.MODEL) {
                        continue;
                    }
                    BakedModel model = mc.getBlockRenderer().getBlockModel(state);
                    for (Direction side : Direction.values()) {
                        BlockState next = stateAt(snapshot, x + side.getStepX(), y + side.getStepY(),
                            z + side.getStepZ());
                        if (next != null && next.isSolidRender(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)) {
                            continue;
                        }
                        random.setSeed(42);
                        addQuads(entries, x, y, z, state, model.getQuads(state, side, random, ModelData.EMPTY, null));
                    }
                    random.setSeed(42);
                    addQuads(entries, x, y, z, state, model.getQuads(state, null, random, ModelData.EMPTY, null));
                    if (entries.size() > MAX_QUADS) {
                        return entries;
                    }
                }
            }
        }
        return entries;
    }

    private static void addQuads(List<Entry> entries, int x, int y, int z, BlockState state, List<BakedQuad> quads) {
        for (BakedQuad quad : quads) {
            int colour = 0xFFFFFF;
            if (quad.isTinted()) {
                try {
                    colour = Minecraft.getInstance().getBlockColors().getColor(state, null, null, quad.getTintIndex());
                } catch (RuntimeException e) {
                    colour = 0xFFFFFF;
                }
                if (colour == -1) {
                    colour = 0xFFFFFF;
                }
            }
            entries.add(new Entry(x, y, z, quad, colour));
        }
    }
}
