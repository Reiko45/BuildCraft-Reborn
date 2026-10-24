/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.factory.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import net.neoforged.neoforge.fluids.FluidStack;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.recipes.BuildcraftRecipeRegistry;
import buildcraftreborn.api.recipes.IRefineryRecipeManager.IDistillationRecipe;
import buildcraftreborn.factory.container.ContainerDistiller;
import buildcraftreborn.lib.client.render.fluid.FluidRenderer;
import buildcraftreborn.lib.gui.GuiBC8;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.elem.GuiElementFluidTank;
import buildcraftreborn.lib.gui.pos.GuiRectangle;
import buildcraftreborn.lib.misc.GuiUtil;
import buildcraftreborn.lib.misc.MathUtil;

/** The distiller screen: the input tank, the gas and liquid output tanks, and an animation of the fluid flowing
 * through while it works (or markers showing why it is stopped). */
public class GuiDistiller extends GuiBC8<ContainerDistiller> {
    private static final ResourceLocation TEXTURE_BASE = BuildCraftReborn.id("textures/gui/distiller.png");
    private static final int SIZE_X = 176, SIZE_Y = 161;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_TANK_VERTICAL_OVERLAY = new GuiIcon(TEXTURE_BASE, 0, 161, 16, 38);
    private static final GuiIcon ICON_TANK_HORIZONTAL_OVERLAY = new GuiIcon(TEXTURE_BASE, 17, 161, 34, 17);
    private static final GuiIcon ICON_OFF_VALID_INPUT = new GuiIcon(TEXTURE_BASE, 176, 14, 17, 29);
    private static final GuiRectangle RECT_OFF_VALID_INPUT = new GuiRectangle(61, 26, 17, 29);
    private static final GuiIcon ICON_OFF_OUTPUT_GAS = new GuiIcon(TEXTURE_BASE, 192, 0, 20, 13);
    private static final GuiRectangle RECT_OFF_OUTPUT_GAS = new GuiRectangle(77, 12, 20, 13);
    private static final GuiIcon ICON_OFF_OUTPUT_LIQUID = new GuiIcon(TEXTURE_BASE, 192, 44, 20, 13);
    private static final GuiRectangle RECT_OFF_OUTPUT_LIQUID = new GuiRectangle(77, 56, 20, 13);

    private static final GuiIcon ICON_ACTIVE_BACKGROUND = new GuiIcon(TEXTURE_BASE, 176, 57, 36, 57);
    private static final GuiRectangle RECT_ACTIVE_BACKGROUND = new GuiRectangle(61, 12, 36, 57);
    private static final GuiIcon ICON_ACTIVE_ANIM_1 = new GuiIcon(TEXTURE_BASE, 212, 26, 26, 5);
    private static final GuiRectangle RECT_ACTIVE_ANIM_1 = new GuiRectangle(61, 38, 26, 5);
    private static final GuiIcon ICON_ACTIVE_ANIM_2A = new GuiIcon(TEXTURE_BASE, 225, 13, 13, 16);
    private static final GuiRectangle RECT_ACTIVE_ANIM_2A = new GuiRectangle(74, 25, 13, 16);
    private static final GuiIcon ICON_ACTIVE_ANIM_2B = new GuiIcon(TEXTURE_BASE, 225, 28, 13, 16);
    private static final GuiRectangle RECT_ACTIVE_ANIM_2B = new GuiRectangle(74, 40, 13, 16);
    private static final GuiIcon ICON_ACTIVE_ANIM_3A = new GuiIcon(TEXTURE_BASE, 230, 5, 3, 8);
    private static final GuiRectangle RECT_ACTIVE_ANIM_3A = new GuiRectangle(79, 17, 3, 8);
    private static final GuiIcon ICON_ACTIVE_ANIM_3B = new GuiIcon(TEXTURE_BASE, 230, 44, 3, 8);
    private static final GuiRectangle RECT_ACTIVE_ANIM_3B = new GuiRectangle(79, 56, 3, 8);
    private static final GuiIcon ICON_ACTIVE_ANIM_4A = new GuiIcon(TEXTURE_BASE, 230, 5, 18, 3);
    private static final GuiRectangle RECT_ACTIVE_ANIM_4A = new GuiRectangle(79, 17, 18, 3);
    private static final GuiIcon ICON_ACTIVE_ANIM_4B = new GuiIcon(TEXTURE_BASE, 230, 49, 18, 3);
    private static final GuiRectangle RECT_ACTIVE_ANIM_4B = new GuiRectangle(79, 61, 18, 3);

    /** Pixels per second */
    private static final double ACTIVE_SPEED = 300;

    private static final double ACTIVE_PIXEL_WIDTH = 150;

    long lastActiveTime = -1;
    double activeStart = -ACTIVE_PIXEL_WIDTH;
    double activeEnd = 0;

    private GuiGraphics graphics;

    public GuiDistiller(ContainerDistiller container, Inventory inventory, Component title) {
        super(container, inventory, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;

        mainGui.shownElements.add(new GuiElementFluidTank(mainGui,
            new GuiRectangle(44, 23, 16, 38).offset(mainGui.rootElement), container.widgetInputTank,
            ICON_TANK_VERTICAL_OVERLAY));
        mainGui.shownElements.add(new GuiElementFluidTank(mainGui,
            new GuiRectangle(98, 10, 34, 17).offset(mainGui.rootElement), container.widgetOutputGasTank,
            ICON_TANK_HORIZONTAL_OVERLAY));
        mainGui.shownElements.add(new GuiElementFluidTank(mainGui,
            new GuiRectangle(98, 54, 34, 17).offset(mainGui.rootElement), container.widgetOutputLiquidTank,
            ICON_TANK_HORIZONTAL_OVERLAY));
    }

    @Override
    protected void drawBackgroundLayer(GuiGraphics graphics, float partialTicks) {
        this.graphics = graphics;
        ICON_GUI.drawAt(graphics, mainGui.rootElement);

        boolean isOn = container.tile.isActive();

        FluidStack currentInput = container.tile.tankIn.getFluidForRender();
        FluidStack currentGas = container.tile.tankGasOut.getFluidForRender();
        FluidStack currentLiquid = container.tile.tankLiquidOut.getFluidForRender();

        if (isOn) {

            ICON_ACTIVE_BACKGROUND.drawAt(graphics, RECT_ACTIVE_BACKGROUND.offset(mainGui.rootElement));

            long now = System.currentTimeMillis();
            if (lastActiveTime != -1) {
                double change = ACTIVE_SPEED * (now - lastActiveTime) / 1000.0;
                activeStart += change;
                activeEnd += change;
            }
            lastActiveTime = now;

            double distance = 0;

            double startStore = activeStart;
            double endStore = activeEnd;

            int inputColour = FluidRenderer.getAverageFluidColour(currentInput);
            int liquidColour = FluidRenderer.getAverageFluidColour(currentLiquid);
            int gasColour = FluidRenderer.getAverageFluidColour(currentGas);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            for (int i = 0; i < 10; i++) {
                distance = drawAnimation(inputColour, gasColour, liquidColour);

                activeStart -= distance * 3 / 4;
                activeEnd -= distance * 3 / 4;
            }

            activeStart = startStore;
            activeEnd = endStore;

            if (activeStart >= distance * 3) {
                activeStart -= distance * 3 / 4;
                activeEnd -= distance * 3 / 4;
            }

        } else {
            activeStart = -ACTIVE_PIXEL_WIDTH;
            activeEnd = 0;
            lastActiveTime = -1;

            IDistillationRecipe recipe =
                BuildcraftRecipeRegistry.refineryRecipes.getDistillationRegistry().getRecipeForInput(currentInput);
            boolean validInput = recipe != null;

            if (validInput) {
                ICON_OFF_VALID_INPUT.drawAt(graphics, RECT_OFF_VALID_INPUT.offset(mainGui.rootElement));
            }

            boolean gasBlocking = !currentGas.isEmpty()
                && currentGas.getAmount() >= container.tile.tankGasOut.getCapacity();

            if (!gasBlocking) {
                if (currentGas.isEmpty()) {
                    gasBlocking = false;
                } else if (recipe != null && !FluidStack.isSameFluidSameComponents(recipe.outGas(), currentGas)) {
                    gasBlocking = true;
                }
            }

            if (gasBlocking) {
                ICON_OFF_OUTPUT_GAS.drawAt(graphics, RECT_OFF_OUTPUT_GAS.offset(mainGui.rootElement));
            }

            boolean liquidBlocking = !currentLiquid.isEmpty()
                && currentLiquid.getAmount() >= container.tile.tankLiquidOut.getCapacity();

            if (!liquidBlocking) {
                if (!currentLiquid.isEmpty() && recipe != null
                    && !FluidStack.isSameFluidSameComponents(recipe.outLiquid(), currentLiquid)) {
                    liquidBlocking = true;
                }
            }

            if (liquidBlocking) {
                ICON_OFF_OUTPUT_LIQUID.drawAt(graphics, RECT_OFF_OUTPUT_LIQUID.offset(mainGui.rootElement));
            }
        }
        this.graphics = null;
    }

    private double drawAnimation(int colourInput, int colourGas, int colourLiquid) {

        double distance = 0;

        distance += RECT_ACTIVE_ANIM_1.width * RECT_ACTIVE_ANIM_1.height;
        drawAnimationBottomToTop(colourGas, ICON_ACTIVE_ANIM_2A.offset(0, 114), RECT_ACTIVE_ANIM_2A, distance);
        drawAnimationTopToBottom(colourLiquid, ICON_ACTIVE_ANIM_2B.offset(0, 114), RECT_ACTIVE_ANIM_2B, distance);
        distance += RECT_ACTIVE_ANIM_2A.height * RECT_ACTIVE_ANIM_2A.width;
        drawAnimationBottomToTop(colourGas, ICON_ACTIVE_ANIM_3A.offset(0, 114), RECT_ACTIVE_ANIM_3A, distance);
        drawAnimationTopToBottom(colourLiquid, ICON_ACTIVE_ANIM_3B.offset(0, 114), RECT_ACTIVE_ANIM_3B, distance);
        distance += RECT_ACTIVE_ANIM_3A.height * RECT_ACTIVE_ANIM_3A.width;
        drawAnimationLeftToRight(colourGas, ICON_ACTIVE_ANIM_4A.offset(0, 114), RECT_ACTIVE_ANIM_4A, distance);
        drawAnimationLeftToRight(colourLiquid, ICON_ACTIVE_ANIM_4B.offset(0, 114), RECT_ACTIVE_ANIM_4B, distance);
        distance += RECT_ACTIVE_ANIM_4A.width * RECT_ACTIVE_ANIM_4A.height;

        double d2 = 0;

        drawAnimationLeftToRight(colourInput, ICON_ACTIVE_ANIM_1.offset(-36, 114), RECT_ACTIVE_ANIM_1, d2);
        d2 += RECT_ACTIVE_ANIM_1.width * RECT_ACTIVE_ANIM_1.height;
        drawAnimationBottomToTop(colourInput, ICON_ACTIVE_ANIM_2A.offset(-36, 114), RECT_ACTIVE_ANIM_2A, d2);
        drawAnimationTopToBottom(colourInput, ICON_ACTIVE_ANIM_2B.offset(-36, 114), RECT_ACTIVE_ANIM_2B, d2);

        return distance;
    }

    private void drawAnimationLeftToRight(int colour, GuiIcon icon, GuiRectangle rectangle, double startPoint) {
        double start = activeStart - startPoint;
        double end = activeEnd - startPoint;

        double size = rectangle.width * rectangle.height;

        if (end < 0 || start >= size) {
            return;
        }

        drawAnimationPart(colour, icon, rectangle, Math.max(0, start / size), 0, Math.min(1, end / size), 1);
    }

    private void drawAnimationBottomToTop(int colour, GuiIcon icon, GuiRectangle rectangle, double startPoint) {
        double start = activeStart - startPoint;
        double end = activeEnd - startPoint;

        double size = rectangle.width * rectangle.height;

        if (end < 0 || start >= size) {
            return;
        }

        drawAnimationPart(colour, icon, rectangle, 0, 1 - MathUtil.clamp(end / size, 0.0, 1.0), 1,
            1 - MathUtil.clamp(start / size, 0.0, 1.0));
    }

    private void drawAnimationTopToBottom(int colour, GuiIcon icon, GuiRectangle rectangle, double startPoint) {
        double start = activeStart - startPoint;
        double end = activeEnd - startPoint;

        double size = rectangle.width * rectangle.height;

        if (end < 0 || start >= size) {
            return;
        }

        drawAnimationPart(colour, icon, rectangle, 0, Math.max(0, start / size), 1, Math.min(1, end / size));
    }

    private void drawAnimationPart(int colour, GuiIcon icon, GuiRectangle rectangle, double u0, double v0, double u1,
        double v1) {

        double x0 = rectangle.x + mainGui.rootElement.getX();
        double y0 = rectangle.y + mainGui.rootElement.getY();
        double x1 = x0 + rectangle.width;
        double y1 = y0 + rectangle.height;

        GuiUtil.setColour(graphics, colour);
        GuiIcon.drawCustomScaledAt(graphics, icon.sprite,
            MathUtil.interp(u0, x0, x1),
            MathUtil.interp(v0, y0, y1),
            MathUtil.interp(u1, x0, x1),
            MathUtil.interp(v1, y0, y1),
            u0, v0, u1, v1);
        GuiUtil.resetColour(graphics);
    }

    @Override
    protected void drawForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY) {
        double titleX = mainGui.rootElement.getX() + 6;
        double titleY = mainGui.rootElement.getY() + 6;
        graphics.drawString(font, title, (int) titleX, (int) titleY, 0x404040, false);

        double invX = mainGui.rootElement.getX() + 8;
        double invY = mainGui.rootElement.getY() + SIZE_Y - 96;
        graphics.drawString(font, playerInventoryTitle, (int) invX, (int) invY, 0x404040, false);
    }
}
