/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.misc;

import java.util.List;
import java.util.function.DoubleSupplier;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.lib.gui.elem.ToolTip;
import buildcraftreborn.lib.gui.pos.GuiRectangle;
import buildcraftreborn.lib.gui.pos.IGuiArea;
import buildcraftreborn.lib.gui.pos.IGuiPosition;

/** Client-side GUI drawing helpers, ported from 1.12's {@code GuiUtil} onto {@link GuiGraphics}. Client only. */
public final class GuiUtil {
    /** The whole screen, at the current GUI scale. */
    public static final IGuiArea AREA_WHOLE_SCREEN = IGuiArea.create(
        () -> Minecraft.getInstance().getWindow().getGuiScaledWidth(),
        () -> Minecraft.getInstance().getWindow().getGuiScaledHeight());

    private GuiUtil() {}

    /** Draws a textured quad with UV coordinates between 0 and 1, at fractional positions. This mirrors
     * {@code GuiGraphics.innerBlit}, which only takes whole-pixel positions. */
    public static void drawTexturedQuad(GuiGraphics graphics, ResourceLocation texture, double x0, double y0,
        double x1, double y1, double u0, double v0, double u1, double v1) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(matrix, (float) x0, (float) y0, 0).setUv((float) u0, (float) v0);
        buffer.addVertex(matrix, (float) x0, (float) y1, 0).setUv((float) u0, (float) v1);
        buffer.addVertex(matrix, (float) x1, (float) y1, 0).setUv((float) u1, (float) v1);
        buffer.addVertex(matrix, (float) x1, (float) y0, 0).setUv((float) u1, (float) v0);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    /** Sets the colour that following draws are multiplied by (as ARGB). Reset with {@link #resetColour}. */
    public static void setColour(GuiGraphics graphics, int argb) {
        float a = ((argb >>> 24) & 0xFF) / 255f;
        if (a == 0) a = 1;
        graphics.setColor(((argb >> 16) & 0xFF) / 255f, ((argb >> 8) & 0xFF) / 255f, (argb & 0xFF) / 255f, a);
    }

    public static void resetColour(GuiGraphics graphics) {
        graphics.setColor(1, 1, 1, 1);
    }

    /** @return An area of the given size, always in the middle of the screen. */
    public static IGuiArea moveRectangleToCentre(GuiRectangle area) {
        final double w = area.width;
        final double h = area.height;
        DoubleSupplier posX = () -> (AREA_WHOLE_SCREEN.getWidth() - w) / 2;
        DoubleSupplier posY = () -> (AREA_WHOLE_SCREEN.getHeight() - h) / 2;
        return IGuiArea.create(IGuiPosition.create(posX, posY), w, h);
    }

    /** Draws a textured quadrilateral with the given corners (bottom left, bottom right, top right, top left) and
     * the sprite's whole texture. 1.12 drew it with perspective-correct texture coordinates; 1.21's GUI shaders only
     * take two, so this draws it in thin vertical strips instead (close enough for shapes with vertical edges, like
     * a page turning). */
    public static void drawCustomQuad(GuiGraphics graphics, ISprite sprite, double x1, double y1, double x2,
        double y2, double x3, double y3, double x4, double y4) {
        RenderSystem.setShaderTexture(0, sprite.getTexture());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        int strips = 16;
        double vMin = sprite.getInterpV(0), vMax = sprite.getInterpV(1);
        for (int i = 0; i < strips; i++) {
            double a = (double) i / strips, b = (double) (i + 1) / strips;
            double uA = sprite.getInterpU(a), uB = sprite.getInterpU(b);
            // bottom edge from corner 1 to 2, top edge from corner 4 to 3
            buffer.addVertex(matrix, (float) lerp(a, x4, x3), (float) lerp(a, y4, y3), 0).setUv((float) uA, (float) vMin);
            buffer.addVertex(matrix, (float) lerp(a, x1, x2), (float) lerp(a, y1, y2), 0).setUv((float) uA, (float) vMax);
            buffer.addVertex(matrix, (float) lerp(b, x1, x2), (float) lerp(b, y1, y2), 0).setUv((float) uB, (float) vMax);
            buffer.addVertex(matrix, (float) lerp(b, x4, x3), (float) lerp(b, y4, y3), 0).setUv((float) uB, (float) vMin);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static double lerp(double t, double from, double to) {
        return from + (to - from) * t;
    }

    /** Restricts drawing to the given area (in GUI coordinates) until the returned scissor is closed. Nests like
     * 1.12's {@code GuiUtil.scissor}. */
    public static AutoScissor scissor(GuiGraphics graphics, double x, double y, double width, double height) {
        int x0 = (int) Math.floor(x);
        int y0 = (int) Math.floor(y);
        int x1 = (int) Math.ceil(x + Math.max(0, width));
        int y1 = (int) Math.ceil(y + Math.max(0, height));
        graphics.enableScissor(x0, y0, x1, y1);
        return graphics::disableScissor;
    }

    @FunctionalInterface
    public interface AutoScissor extends AutoCloseable {
        @Override
        void close();
    }

    /** Draws the given fluid filling {@code area} from the bottom, in proportion to {@code amount / capacity}. */
    public static void drawFluid(GuiGraphics graphics, IGuiArea area, FluidStack fluid, int capacity) {
        if (fluid.isEmpty() || capacity <= 0) {
            return;
        }
        IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fluid.getFluid());
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
            .apply(ext.getStillTexture(fluid));
        int colour = ext.getTintColor(fluid);
        double filled = area.getHeight() * Math.min(1.0, fluid.getAmount() / (double) capacity);
        if (filled <= 0) {
            return;
        }
        float a = ((colour >> 24) & 0xFF) / 255f;
        float r = ((colour >> 16) & 0xFF) / 255f;
        float g = ((colour >> 8) & 0xFF) / 255f;
        float b = (colour & 0xFF) / 255f;
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(r, g, b, a == 0 ? 1 : a);

        double xMin = area.getX();
        double xMax = area.getEndX();
        double yBottom = area.getEndY();
        double yTop = yBottom - filled;
        // Tile 16x16 pixel squares, starting from the bottom
        for (double y = yBottom; y > yTop; y -= 16) {
            double tileTop = Math.max(yTop, y - 16);
            double vFrac = (y - tileTop) / 16.0;
            for (double x = xMin; x < xMax; x += 16) {
                double tileRight = Math.min(xMax, x + 16);
                double uFrac = (tileRight - x) / 16.0;
                drawTexturedQuad(graphics, InventoryMenu.BLOCK_ATLAS, x, tileTop, tileRight, y,
                    sprite.getU0(), sprite.getV(1 - (float) vFrac), sprite.getU((float) uFrac), sprite.getV1());
            }
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    /** Draws each ready tooltip, stacked downwards from the mouse position. */
    public static void drawTooltips(GuiGraphics graphics, Font font, IGuiPosition mouse, List<ToolTip> tooltips) {
        int y = (int) mouse.getY();
        for (ToolTip tooltip : tooltips) {
            if (tooltip.isEmpty() || !tooltip.isReady()) {
                continue;
            }
            List<Component> lines = tooltip;
            graphics.renderComponentTooltip(font, lines, (int) mouse.getX(), y);
            y += lines.size() * (font.lineHeight + 1) + 8;
        }
    }
}
