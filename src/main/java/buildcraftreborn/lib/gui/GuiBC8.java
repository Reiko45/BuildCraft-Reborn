/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui;

import java.util.function.Function;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import buildcraftreborn.lib.gui.ledger.LedgerHelp;
import buildcraftreborn.lib.gui.ledger.LedgerOwnership;

/** Base screen for BuildCraft menus. Client only.
 * <p>
 * 1.21 notes: built on {@link AbstractContainerScreen} and {@link GuiGraphics}. Subclasses draw with
 * {@link #drawBackgroundLayer} and {@link #drawForegroundLayer}, both in absolute screen coordinates; the element
 * system ({@link #mainGui}) draws backgrounds after the screen background and foregrounds after the slots.
 * <p>
 * Like 1.12, every screen gets the ownership ledger (for tile menus) and the help ledger. JSON-defined GUIs are not
 * ported yet. */
public abstract class GuiBC8<C extends ContainerBC_Neptune> extends AbstractContainerScreen<C> {
    public final BuildCraftGui mainGui;
    public final C container;

    public GuiBC8(C container, Inventory inventory, Component title) {
        this(container, inventory, title, g -> new BuildCraftGui(g, BuildCraftGui.createWindowedArea(g)));
    }

    public GuiBC8(C container, Inventory inventory, Component title, Function<GuiBC8<?>, BuildCraftGui> constructor) {
        super(container, inventory, title);
        this.container = container;
        this.mainGui = constructor.apply(this);
        standardLedgerInit();
    }

    private void standardLedgerInit() {
        if (container instanceof ContainerBCTile<?> tileContainer) {
            mainGui.shownElements.add(new LedgerOwnership(mainGui, tileContainer.tile, true));
        }
        if (shouldAddHelpLedger()) {
            mainGui.shownElements.add(new LedgerHelp(mainGui, false));
        }
    }

    protected boolean shouldAddHelpLedger() {
        return true;
    }

    /** Clicking a ledger (outside the main window) mustn't drop the item held on the cursor. */
    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        for (IGuiElement element : mainGui.shownElements) {
            if (element.contains(mouseX, mouseY)) {
                return false;
            }
        }
        return super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, mouseButton);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        drawForegroundLayer(graphics, mouseX, mouseY);
        mainGui.drawElementForegrounds(graphics, () -> renderTransparentBackground(graphics));
        drawForegroundLayerAboveElements(graphics, mouseX, mouseY);
        if (mainGui.currentMenu == null || !mainGui.currentMenu.shouldFullyOverride()) {
            renderTooltip(graphics, mouseX, mouseY);
        }
    }

    @Override
    protected final void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        mainGui.drawBackgroundLayer(graphics, partialTicks, mouseX, mouseY, () -> {});
        drawBackgroundLayer(graphics, partialTicks);
        mainGui.drawElementBackgrounds(graphics);
    }

    /** Labels are drawn by {@link #drawForegroundLayer} instead (in screen coordinates). */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {}

    @Override
    protected void containerTick() {
        super.containerTick();
        mainGui.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mainGui.onMouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        mainGui.onMouseDragged(mouseX, mouseY, button);
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        mainGui.onMouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (mainGui.onKeyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (mainGui.onCharTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    /** Draw the GUI background texture here (absolute screen coordinates; {@code mainGui.rootElement} is the window). */
    protected void drawBackgroundLayer(GuiGraphics graphics, float partialTicks) {}

    /** Draw titles and labels here (absolute screen coordinates). */
    protected void drawForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY) {}

    /** Like {@link #drawForegroundLayer}, but is called after all {@link IGuiElement}'s have been drawn. */
    protected void drawForegroundLayerAboveElements(GuiGraphics graphics, int mouseX, int mouseY) {}
    /** Draws the first {@code widthPercent} (and {@code heightPercent}) of the icon in the given rectangle (relative to
     * the window), for progress bars. Uses the matching part of the icon's texture, so the bar is cut rather than
     * squashed. */
    public void drawProgress(GuiGraphics graphics, buildcraftreborn.lib.gui.pos.GuiRectangle rect, GuiIcon icon,
        double widthPercent, double heightPercent) {
        double nWidth = rect.width * Math.abs(widthPercent);
        double nHeight = rect.height * Math.abs(heightPercent);
        double x = rect.x + mainGui.rootElement.getX();
        double y = rect.y + mainGui.rootElement.getY();
        GuiIcon.drawCustomScaledAt(graphics, icon.sprite, x, y, x + nWidth, y + nHeight, 0, 0, widthPercent,
            heightPercent);
    }
}
