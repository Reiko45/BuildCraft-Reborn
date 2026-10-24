/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.robotics.client.gui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.gui.GuiBC8;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.pos.GuiRectangle;
import buildcraftreborn.robotics.client.BCRoboticsClient;
import buildcraftreborn.robotics.client.render.ZonePlannerMapRenderer;
import buildcraftreborn.robotics.container.ContainerZonePlanner;
import buildcraftreborn.robotics.tile.TileZonePlanner;
import buildcraftreborn.robotics.zone.ZonePlan;
import buildcraftreborn.robotics.zone.ZonePlannerMapChunk.MapColourData;
import buildcraftreborn.robotics.zone.ZonePlannerMapChunk;
import buildcraftreborn.robotics.zone.ZonePlannerMapChunkKey;
import buildcraftreborn.robotics.zone.ZonePlannerMapDataClient;

/** The zone planner's GUI: a 3D map of the area seen from above, with the zones drawn on it.
 * <ul>
 * <li>With an empty hand, drag the map to move around it, and scroll to zoom.</li>
 * <li>With a paintbrush (picked up from a slot), drag over the map to add an area to the brush colour's zone (left
 * button) or remove it (right button). Only that colour's zone is shown.</li>
 * </ul>
 * 1.21: the map is drawn with its own projection in a viewport, as in 1.12, but picking uses the matrices
 * directly; the area painted includes the block under the mouse (1.12 left out the last row and column), a single
 * click paints one block, and dragging moves the map as far as the mouse moves over the ground. */
public class GuiZonePlanner extends GuiBC8<ContainerZonePlanner> {
    private static final ResourceLocation TEXTURE_BASE = BuildCraftReborn.id("textures/gui/zone_planner.png");
    private static final int SIZE_X = 256, SIZE_Y = 228;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_PROGRESS_INPUT = new GuiIcon(TEXTURE_BASE, 9, 228, 28, 9);
    private static final GuiIcon ICON_PROGRESS_OUTPUT = new GuiIcon(TEXTURE_BASE, 0, 228, 9, 28);
    private static final GuiRectangle RECT_PROGRESS_INPUT = new GuiRectangle(44, 128, 28, 9);
    private static final GuiRectangle RECT_PROGRESS_OUTPUT = new GuiRectangle(236, 45, 9, 28);

    private static final int MAP_X = 8, MAP_Y = 9, MAP_WIDTH = 213, MAP_HEIGHT = 100;
    private static final float FOV = (float) Math.toRadians(70);
    /** How far the camera may be from the ground, and how many chunks may be drawn around the centre. */
    private static final int MIN_CAMERA_HEIGHT = 10, MAX_CAMERA_HEIGHT = 256, MAX_CHUNK_RANGE = 24;

    private float positionX, positionZ;
    private float camY;
    private float scaleSpeed = 0;
    private boolean canDrag = false;
    private double startMouseX, startMouseY;
    private float startPositionX, startPositionZ;
    /** The height of the ground at the centre of the map. */
    private int groundY;

    @Nullable
    private BlockPos lastSelected = null;
    @Nullable
    private BlockPos selectionStartXZ = null;
    @Nullable
    private BlockPos bufferEnd = null;
    private int paintButton;
    @Nullable
    private ZonePlan bufferLayer = null;

    public GuiZonePlanner(ContainerZonePlanner container, Inventory inventory, Component title) {
        super(container, inventory, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
        BlockPos tilePos = container.tile.getBlockPos();
        positionX = tilePos.getX();
        positionZ = tilePos.getZ();
        groundY = tilePos.getY();
        camY = tilePos.getY() + 64;
    }

    @Nullable
    private DyeColor getPaintbrushColour() {
        return TileZonePlanner.getBrushColour(menu.getCarried());
    }

    private boolean isInMap(double mouseX, double mouseY) {
        return mouseX >= leftPos + MAP_X && mouseX < leftPos + MAP_X + MAP_WIDTH && mouseY >= topPos + MAP_Y
            && mouseY < topPos + MAP_Y + MAP_HEIGHT;
    }

    @Nullable
    private ZonePlannerMapChunk getMapChunk(Level level, int chunkX, int chunkZ, int mapLevel) {
        return ZonePlannerMapDataClient.INSTANCE.getChunk(level,
            new ZonePlannerMapChunkKey(new ChunkPos(chunkX, chunkZ), level.dimension(), mapLevel));
    }

    // Mouse

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isInMap(mouseX, mouseY) && (button == 0 || button == 1)) {
            canDrag = false;
            if (getPaintbrushColour() != null) {
                if (lastSelected != null) {
                    selectionStartXZ = lastSelected;
                    paintButton = button;
                    bufferEnd = null;
                    updateBufferLayer();
                }
            } else if (menu.getCarried().isEmpty() && button == 0) {
                startPositionX = positionX;
                startPositionZ = positionZ;
                startMouseX = mouseX;
                startMouseY = mouseY;
                canDrag = true;
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (canDrag) {
            // move the map as far as the mouse moved over the ground
            float blocksPerPixel = (float) (2 * (camY - groundY) * Math.tan(FOV / 2) / MAP_HEIGHT);
            positionX = startPositionX - (float) (mouseX - startMouseX) * blocksPerPixel;
            positionZ = startPositionZ - (float) (mouseY - startMouseY) * blocksPerPixel;
            return true;
        }
        if (selectionStartXZ != null) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (canDrag) {
            canDrag = false;
            return true;
        }
        if (selectionStartXZ != null) {
            DyeColor colour = getPaintbrushColour();
            if (colour != null && bufferLayer != null) {
                container.tile.layers[colour.getId()] = bufferLayer;
                container.tile.sendLayerToServer(colour.getId());
            }
            selectionStartXZ = null;
            bufferLayer = null;
            bufferEnd = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isInMap(mouseX, mouseY)) {
            scaleSpeed -= (float) scrollY * 4;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    /** Paints (or clears) the rectangle from where the drag started to the block under the mouse, on a copy of the
     * brush colour's zone. */
    private void updateBufferLayer() {
        DyeColor colour = getPaintbrushColour();
        if (colour == null || selectionStartXZ == null || lastSelected == null || lastSelected.equals(bufferEnd)) {
            return;
        }
        bufferEnd = lastSelected;
        BlockPos tilePos = container.tile.getBlockPos();
        ZonePlan buffer = new ZonePlan(container.tile.layers[colour.getId()]);
        int minX = Math.min(selectionStartXZ.getX(), lastSelected.getX());
        int maxX = Math.max(selectionStartXZ.getX(), lastSelected.getX());
        int minZ = Math.min(selectionStartXZ.getZ(), lastSelected.getZ());
        int maxZ = Math.max(selectionStartXZ.getZ(), lastSelected.getZ());
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                buffer.set(x - tilePos.getX(), z - tilePos.getZ(), paintButton == 0);
            }
        }
        if (buffer.getChunkPoses().size() <= TileZonePlanner.MAX_SENT_CHUNKS) {
            bufferLayer = buffer;
        }
    }

    // Drawing

    @Override
    protected void drawBackgroundLayer(GuiGraphics graphics, float partialTicks) {
        ICON_GUI.drawAt(graphics, mainGui.rootElement);
        drawProgress(graphics, RECT_PROGRESS_INPUT, ICON_PROGRESS_INPUT,
            container.tile.deltaProgressInput.getDynamic(partialTicks), 1);
        drawProgress(graphics, RECT_PROGRESS_OUTPUT, ICON_PROGRESS_OUTPUT, 1,
            container.tile.deltaProgressOutput.getDynamic(partialTicks));
        graphics.flush();
        renderMap(mainGui.mouse.getX(), mainGui.mouse.getY());
    }

    @Override
    protected void drawForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY) {
        if (lastSelected != null) {
            String text = "X: " + lastSelected.getX() + " Y: " + lastSelected.getY() + " Z: " + lastSelected.getZ();
            graphics.drawString(font, text, leftPos + 130, topPos + 130, 0x404040, false);
        }
    }

    private void renderMap(double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) {
            return;
        }
        TileZonePlanner tile = container.tile;
        BlockPos tilePos = tile.getBlockPos();
        int mapLevel = BCRoboticsClient.getMapLevel();
        int bottom = level.getMinBuildHeight();

        camY += scaleSpeed;
        scaleSpeed *= 0.7F;
        int posX = Mth.floor(positionX);
        int posZ = Mth.floor(positionZ);
        ZonePlannerMapChunk centre = getMapChunk(level, posX >> 4, posZ >> 4, mapLevel);
        MapColourData centreData = centre == null ? null : centre.getData(posX, posZ);
        if (centreData != null) {
            groundY = centreData.posY();
        }
        camY = Mth.clamp(camY, groundY + MIN_CAMERA_HEIGHT, groundY + MAX_CAMERA_HEIGHT);

        Window window = mc.getWindow();
        double scale = window.getGuiScale();
        int viewportX = (int) Math.round((leftPos + MAP_X) * scale);
        int viewportY = window.getHeight() - (int) Math.round((topPos + MAP_Y + MAP_HEIGHT) * scale);
        int viewportWidth = (int) Math.round(MAP_WIDTH * scale);
        int viewportHeight = (int) Math.round(MAP_HEIGHT * scale);
        int[] viewport = { viewportX, viewportY, viewportWidth, viewportHeight };

        Matrix4f projection = new Matrix4f().perspective(FOV, (float) viewportWidth / viewportHeight, 1, 10000);
        // look down: north is up, east is right
        Matrix4f view = new Matrix4f().rotationX((float) Math.toRadians(90)).translate(-positionX, -camY, -positionZ);
        Matrix4f projView = new Matrix4f(projection).mul(view);

        // The chunks in view: where the corners of the view meet the ground
        int minChunkX = (posX >> 4) - 1, maxChunkX = (posX >> 4) + 1;
        int minChunkZ = (posZ >> 4) - 1, maxChunkZ = (posZ >> 4) + 1;
        for (int corner = 0; corner < 4; corner++) {
            float cx = viewportX + ((corner & 1) == 0 ? 0 : viewportWidth);
            float cy = viewportY + ((corner & 2) == 0 ? 0 : viewportHeight);
            Vector3f near = projView.unproject(cx, cy, 0, viewport, new Vector3f());
            Vector3f far = projView.unproject(cx, cy, 1, viewport, new Vector3f());
            float dy = far.y - near.y;
            if (dy >= 0) {
                continue;
            }
            float t = (groundY - near.y) / dy;
            int chunkX = Mth.floor(near.x + (far.x - near.x) * t) >> 4;
            int chunkZ = Mth.floor(near.z + (far.z - near.z) * t) >> 4;
            minChunkX = Math.min(minChunkX, chunkX - 1);
            maxChunkX = Math.max(maxChunkX, chunkX + 1);
            minChunkZ = Math.min(minChunkZ, chunkZ - 1);
            maxChunkZ = Math.max(maxChunkZ, chunkZ + 1);
        }
        minChunkX = Math.max(minChunkX, (posX >> 4) - MAX_CHUNK_RANGE);
        maxChunkX = Math.min(maxChunkX, (posX >> 4) + MAX_CHUNK_RANGE);
        minChunkZ = Math.max(minChunkZ, (posZ >> 4) - MAX_CHUNK_RANGE);
        maxChunkZ = Math.min(maxChunkZ, (posZ >> 4) + MAX_CHUNK_RANGE);

        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(projection, VertexSorting.DISTANCE_TO_ORIGIN);
        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.set(view);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.viewport(viewportX, viewportY, viewportWidth, viewportHeight);
        RenderSystem.enableScissor(viewportX, viewportY, viewportWidth, viewportHeight);
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableCull();
        RenderSystem.disableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // The terrain
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                VertexBuffer buffer = ZonePlannerMapRenderer.INSTANCE.getChunkBuffer(
                    new ZonePlannerMapChunkKey(new ChunkPos(chunkX, chunkZ), level.dimension(), mapLevel));
                if (buffer != null) {
                    buffer.bind();
                    buffer.drawWithShader(view, projection, GameRenderer.getPositionColorShader());
                }
            }
        }
        VertexBuffer.unbind();

        // The block under the mouse
        BlockPos found = null;
        int foundColour = 0;
        if (isInMap(mouseX, mouseY)) {
            found = rayTrace(level, projView, viewport, (float) (mouseX * scale),
                (float) (window.getHeight() - mouseY * scale), mapLevel);
        }
        if (found != null) {
            ZonePlannerMapChunk chunk = getMapChunk(level, found.getX() >> 4, found.getZ() >> 4, mapLevel);
            MapColourData data = chunk == null ? null : chunk.getData(found.getX(), found.getZ());
            if (data != null) {
                foundColour = data.colour();
            }
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.polygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
            int r = (int) (((foundColour >> 16) & 0xFF) * 0.7);
            int g = (int) (((foundColour >> 8) & 0xFF) * 0.7);
            int b = (int) ((foundColour & 0xFF) * 0.7);
            ZonePlannerMapRenderer.INSTANCE.setColor(r << 16 | g << 8 | b | 0x77 << 24);
            BufferBuilder builder =
                Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            ZonePlannerMapRenderer.INSTANCE.drawBlockCuboid(builder, found.getX(), found.getY(), found.getZ());
            draw(builder);
            RenderSystem.polygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
            RenderSystem.enableDepthTest();
        }
        lastSelected = found;
        if (selectionStartXZ != null) {
            updateBufferLayer();
        }

        // The zones
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        DyeColor brush = getPaintbrushColour();
        int minX = minChunkX << 4, maxX = (maxChunkX << 4) + 15;
        int minZ = minChunkZ << 4, maxZ = (maxChunkZ << 4) + 15;
        for (int i = 0; i < tile.layers.length; i++) {
            if (brush != null && brush.getId() != i) {
                continue;
            }
            ZonePlan layer = brush != null && bufferLayer != null ? bufferLayer : tile.layers[i];
            if (layer.isEmpty()) {
                continue;
            }
            int colour = DyeColor.byId(i).getTextureDiffuseColor();
            ZonePlannerMapRenderer.INSTANCE.setColor(colour & 0xFF_FF_FF | 0x55 << 24);
            BufferBuilder builder =
                Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            for (ZonePlan.Point point : layer.getAll()) {
                int blockX = point.x() + tilePos.getX();
                int blockZ = point.z() + tilePos.getZ();
                if (blockX < minX || blockX > maxX || blockZ < minZ || blockZ > maxZ) {
                    continue;
                }
                ZonePlannerMapChunk chunk = getMapChunk(level, blockX >> 4, blockZ >> 4, mapLevel);
                MapColourData data = chunk == null ? null : chunk.getData(blockX, blockZ);
                if (data == null) {
                    continue;
                }
                ZonePlannerMapRenderer.INSTANCE.drawBlockCuboid(builder, blockX, data.posY() + 0.1, blockZ,
                    data.posY() - bottom, 0.6);
            }
            draw(builder);
        }
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();

        // Back to the GUI: what was drawn here mustn't hide what the GUI draws over it (items, tooltips)
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.disableScissor();
        RenderSystem.enableCull();
        RenderSystem.viewport(0, 0, window.getWidth(), window.getHeight());
        modelView.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.restoreProjectionMatrix();
    }

    private static void draw(BufferBuilder builder) {
        MeshData mesh = builder.build();
        if (mesh != null) {
            BufferUploader.drawWithShader(mesh);
        }
    }

    /** Walks along the ray through a pixel (in window pixels, from the bottom left) until it is below the top of a
     * column of the map.
     * @return The top block of that column, or null if the ray left the map first. */
    @Nullable
    private BlockPos rayTrace(Level level, Matrix4f projView, int[] viewport, float screenX, float screenY,
        int mapLevel) {
        Vector3f near = projView.unproject(screenX, screenY, 0, viewport, new Vector3f());
        Vector3f far = projView.unproject(screenX, screenY, 1, viewport, new Vector3f());
        Vector3d rayPosition = new Vector3d(near);
        Vector3d rayDirection = new Vector3d(far).sub(rayPosition).normalize();

        for (int i = 0; i < 10000; i++) {
            int blockX = (int) Math.round(rayPosition.x);
            int blockZ = (int) Math.round(rayPosition.z);
            ZonePlannerMapChunk chunk = getMapChunk(level, blockX >> 4, blockZ >> 4, mapLevel);
            if (chunk == null) {
                return null;
            }
            MapColourData data = chunk.getData(blockX, blockZ);
            if (data != null && data.posY() >= Math.round(rayPosition.y)) {
                return new BlockPos(blockX, data.posY(), blockZ);
            }
            rayPosition.add(rayDirection);
        }
        return null;
    }
}
