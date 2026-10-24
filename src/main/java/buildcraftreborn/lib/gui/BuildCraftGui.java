/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import buildcraftreborn.lib.gui.elem.ToolTip;
import buildcraftreborn.lib.gui.pos.IGuiArea;
import buildcraftreborn.lib.gui.pos.IGuiPosition;
import buildcraftreborn.lib.gui.pos.MousePosition;
import buildcraftreborn.lib.misc.GuiUtil;

/** Holds and drives the {@link IGuiElement}s of one screen: ticking, drawing, tooltips and input. Client only.
 * <p>
 * A screen using this must call {@link #tick()} once per tick, {@link #drawBackgroundLayer} and
 * {@link #drawElementBackgrounds} while drawing its background, {@link #drawElementForegrounds} after everything else,
 * and forward mouse and key events.
 * <p>
 * 1.21 notes: every draw call takes the screen's {@link GuiGraphics}. Elements draw in absolute screen coordinates
 * (1.12 needed {@code preDrawForeground}/{@code postDrawForeground} to undo the container's GL translation; those are
 * gone). The in-GUI debug overlay (1.12's {@code GuiConfigManager} toggles) is reduced to the
 * {@code buildcraftreborn.debug.gui} system property. */
public class BuildCraftGui {
    /** Draw element outlines and names under the mouse. */
    public static final boolean DEBUG = Boolean.getBoolean("buildcraftreborn.debug.gui");

    public final Minecraft mc = Minecraft.getInstance();
    public final Screen gui;
    public final MousePosition mouse = new MousePosition();

    /** The area that encompasses the entire screen. */
    public final IGuiArea screenElement;

    /** The area that most of the GUI elements should be in. For most container-based gui's this will be a rectangle
     * smaller than the entire screen. For gui's that display outside of a world this will probably be the entire
     * screen, and then this will equal the {@link #screenElement}. */
    public final IGuiArea rootElement;

    /** All of the {@link IGuiElement} which will be drawn by this gui. */
    public final List<IGuiElement> shownElements = new ArrayList<>();
    public IMenuElement currentMenu;

    /** Ledger-style elements. */
    public IGuiPosition lowerLeftLedgerPos, lowerRightLedgerPos;
    private float lastPartialTicks;
    private long lastClickTime;

    public BuildCraftGui(Screen gui, IGuiArea rootElement) {
        this.gui = gui;
        this.screenElement = GuiUtil.AREA_WHOLE_SCREEN;
        this.rootElement = rootElement;

        lowerLeftLedgerPos = rootElement.offset(0, 5);
        lowerRightLedgerPos = rootElement.getPosition(1, -1).offset(0, 5);
    }

    /** Creates a new {@link BuildCraftGui} that uses the entire screen for display. Ledgers are displayed on the
     * opposite side (so that they expand properly). */
    public BuildCraftGui(Screen gui) {
        this.gui = gui;
        this.screenElement = GuiUtil.AREA_WHOLE_SCREEN;
        this.rootElement = screenElement;

        lowerLeftLedgerPos = screenElement.getPosition(1, -1).offset(-5, 5);
        lowerRightLedgerPos = screenElement.offset(5, 5);
    }

    /** Creates an area that follows the container screen's position and size. */
    public static IGuiArea createWindowedArea(AbstractContainerScreen<?> gui) {
        return IGuiArea.create(gui::getGuiLeft, gui::getGuiTop, gui::getXSize, gui::getYSize);
    }

    /** @return The current partial ticks value. */
    public final float getLastPartialTicks() {
        return lastPartialTicks;
    }

    public void tick() {
        if (currentMenu != null) {
            currentMenu.tick();
        }
        for (IGuiElement element : shownElements) {
            element.tick();
        }
    }

    public List<IGuiElement> getElementsAt(double x, double y) {
        List<IGuiElement> elements = new ArrayList<>();
        IMenuElement m = currentMenu;
        if (m != null) {
            elements.addAll(m.getThisAndChildrenAt(x, y));
            if (m.shouldFullyOverride()) {
                return elements;
            }
        }
        for (IGuiElement elem : shownElements) {
            elements.addAll(elem.getThisAndChildrenAt(x, y));
        }
        return elements;
    }

    private List<ToolTip> getAllTooltips() {
        List<ToolTip> tooltips = new ArrayList<>();

        IMenuElement m = currentMenu;
        if (m != null) {
            m.addToolTips(tooltips);
            if (m.shouldFullyOverride()) {
                return tooltips;
            }
        }

        if (gui instanceof ITooltipElement tooltipGui) {
            tooltipGui.addToolTips(tooltips);
        }
        for (IGuiElement elem : shownElements) {
            elem.addToolTips(tooltips);
        }
        return tooltips;
    }

    /** @param menuBackgroundRenderer Draws the screen's normal background, unless a full-screen menu is open. */
    public void drawBackgroundLayer(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY,
        Runnable menuBackgroundRenderer) {
        this.lastPartialTicks = partialTicks;
        mouse.setMousePosition(mouseX, mouseY);
        if (currentMenu == null || !currentMenu.shouldFullyOverride()) {
            menuBackgroundRenderer.run();
        }
    }

    public void drawElementBackgrounds(GuiGraphics graphics) {
        for (IGuiElement element : shownElements) {
            if (element != currentMenu) {
                element.drawBackground(graphics, lastPartialTicks);
            }
        }
    }

    /** @param menuBackgroundRenderer Called to draw the background if the current menu fully overrides the screen.
     *            This draws above all of the normal elements. */
    public void drawElementForegrounds(GuiGraphics graphics, Runnable menuBackgroundRenderer) {
        for (IGuiElement element : shownElements) {
            if (element != currentMenu) {
                element.drawForeground(graphics, lastPartialTicks);
            }
        }

        IMenuElement m = currentMenu;
        if (m != null) {
            if (m.shouldFullyOverride() && menuBackgroundRenderer != null) {
                menuBackgroundRenderer.run();
            }
            m.drawBackground(graphics, lastPartialTicks);
            m.drawForeground(graphics, lastPartialTicks);
        }

        GuiUtil.drawTooltips(graphics, mc.font, mouse, getAllTooltips());

        if (DEBUG) {
            drawDebugOverlay(graphics);
        }
    }

    private void drawDebugOverlay(GuiGraphics graphics) {
        int y = 18;
        List<String> info = new ArrayList<>();
        for (IGuiElement elem : getElementsAt(mouse.getX(), mouse.getY())) {
            String name = elem.getDebugInfo(info);
            int sx = (int) elem.getX() - 1;
            int sy = (int) elem.getY() - 1;
            int ex = (int) elem.getEndX() + 1;
            int ey = (int) elem.getEndY() + 1;
            int colour = name.hashCode() | 0xFF_00_00_00;
            graphics.renderOutline(sx, sy, ex - sx, ey - sy, colour);
            graphics.drawString(mc.font, name, 6, y, -1);
            y += mc.font.lineHeight + 2;
            for (String line : info) {
                graphics.drawString(mc.font, line, 13, y, -1);
                y += mc.font.lineHeight + 2;
            }
            info.clear();
        }
    }

    /** @return True if the {@link #currentMenu} {@link IMenuElement#shouldFullyOverride() fully overrides} other mouse
     *         clicks, false otherwise. */
    public boolean onMouseClicked(double mouseX, double mouseY, int mouseButton) {
        mouse.setMousePosition(mouseX, mouseY);
        lastClickTime = System.currentTimeMillis();

        IMenuElement m = currentMenu;
        if (m != null) {
            m.onMouseClicked(mouseButton);
            if (m.shouldFullyOverride()) {
                return true;
            }
        }

        for (IGuiElement element : shownElements) {
            if (element instanceof IInteractionElement interaction) {
                interaction.onMouseClicked(mouseButton);
            }
        }
        return false;
    }

    public void onMouseDragged(double mouseX, double mouseY, int clickedMouseButton) {
        mouse.setMousePosition(mouseX, mouseY);
        long ticksSinceClick = (System.currentTimeMillis() - lastClickTime) / 50;

        IMenuElement m = currentMenu;
        if (m != null) {
            m.onMouseDragged(clickedMouseButton, ticksSinceClick);
            if (m.shouldFullyOverride()) {
                return;
            }
        }

        for (IGuiElement element : shownElements) {
            if (element instanceof IInteractionElement interaction) {
                interaction.onMouseDragged(clickedMouseButton, ticksSinceClick);
            }
        }
    }

    public void onMouseReleased(double mouseX, double mouseY, int state) {
        mouse.setMousePosition(mouseX, mouseY);

        IMenuElement m = currentMenu;
        if (m != null) {
            m.onMouseReleased(state);
            if (m.shouldFullyOverride()) {
                return;
            }
        }

        for (IGuiElement element : shownElements) {
            if (element instanceof IInteractionElement interaction) {
                interaction.onMouseReleased(state);
            }
        }
    }

    public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        boolean action = false;
        IMenuElement m = currentMenu;
        if (m != null) {
            action = m.onKeyPressed(keyCode, scanCode, modifiers);
            if (action && m.shouldFullyOverride()) {
                return true;
            }
        }
        for (IGuiElement element : shownElements) {
            if (element instanceof IInteractionElement interaction) {
                action |= interaction.onKeyPressed(keyCode, scanCode, modifiers);
            }
        }
        return action;
    }

    public boolean onCharTyped(char typedChar, int modifiers) {
        boolean action = false;
        IMenuElement m = currentMenu;
        if (m != null) {
            action = m.onCharTyped(typedChar, modifiers);
            if (action && m.shouldFullyOverride()) {
                return true;
            }
        }
        for (IGuiElement element : shownElements) {
            if (element instanceof IInteractionElement interaction) {
                action |= interaction.onCharTyped(typedChar, modifiers);
            }
        }
        return action;
    }
}
