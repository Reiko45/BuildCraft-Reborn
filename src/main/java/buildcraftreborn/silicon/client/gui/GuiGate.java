/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.gui.BuildCraftGui;
import buildcraftreborn.lib.gui.GuiBC8;
import buildcraftreborn.lib.gui.GuiElementSimple;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.IInteractionElement;
import buildcraftreborn.lib.gui.pos.GuiRectangle;
import buildcraftreborn.lib.gui.pos.IGuiArea;
import buildcraftreborn.lib.gui.statement.GuiElementStatement;
import buildcraftreborn.lib.gui.statement.GuiElementStatementParam;
import buildcraftreborn.lib.gui.statement.GuiElementStatementSource;
import buildcraftreborn.silicon.container.ContainerGate;
import buildcraftreborn.silicon.gate.GateLogic;
import buildcraftreborn.silicon.gate.GateLogic.StatementPair;

/** The gate GUI: a row per trigger/action pair (two columns of them on the bigger gates), with the possible triggers
 * in a panel on the left and the possible actions on the right, to drag onto the slots. The lights between a trigger
 * and its action show what's on, and the buttons between rows join pairs together.
 * <p>
 * 1.12 laid this out with a JSON GUI ({@code buildcraftsilicon:gui/gate.json}); this builds the same layout in code. */
public class GuiGate extends GuiBC8<ContainerGate> {
    private static final ResourceLocation TEXTURE = BuildCraftReborn.id("textures/gui/gate_interface.png");
    private static final GuiIcon ICON_TOP = new GuiIcon(TEXTURE, 0, 0, 176, 16);
    private static final GuiIcon ICON_ROW = new GuiIcon(TEXTURE, 0, 23, 176, 18);
    private static final GuiIcon ICON_BOTTOM = new GuiIcon(TEXTURE, 0, 48, 176, 101);

    private final GateLogic gate;
    private final int columns;
    private final int rows;
    private final int pairWidth;
    private final int pairStart;
    private final int connectorPos;

    public GuiGate(ContainerGate container, Inventory inventory, Component title) {
        super(container, inventory, title);
        this.gate = container.gate;
        boolean twoColumns = gate.isSplitInTwo();
        columns = twoColumns ? 2 : 1;
        rows = gate.variant.numSlots / columns;
        pairWidth = 18 * (3 + gate.variant.numTriggerArgs + gate.variant.numActionArgs);
        pairStart = (162 - (pairWidth + (twoColumns ? (pairWidth + 18) : 0))) / 2;
        connectorPos = 18 * (1 + gate.variant.numTriggerArgs);
        imageWidth = 176;
        imageHeight = 16 + 101 + rows * 18;

        // The panels of statements to pick from
        mainGui.shownElements.add(new GuiElementStatementSource<>(mainGui, true, container.possibleTriggersContext));
        mainGui.shownElements.add(new GuiElementStatementSource<>(mainGui, false, container.possibleActionsContext));

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int index = row + column * rows;
                StatementPair pair = gate.statements[index];
                int x = pairX(column);
                int y = 16 + row * 18;
                mainGui.shownElements.add(new GuiElementStatement<>(mainGui, area(x, y),
                    pair.trigger, container.possibleTriggersContext, true));
                for (int p = 0; p < gate.variant.numTriggerArgs; p++) {
                    mainGui.shownElements.add(new GuiElementStatementParam(mainGui, area(x + 18 * (p + 1), y), gate,
                        pair.trigger, p, true));
                }
                int ax = x + 18 * (2 + gate.variant.numTriggerArgs);
                mainGui.shownElements.add(new GuiElementStatement<>(mainGui, area(ax, y), pair.action,
                    container.possibleActionsContext, true));
                for (int p = 0; p < gate.variant.numActionArgs; p++) {
                    mainGui.shownElements.add(new GuiElementStatementParam(mainGui, area(ax + 18 * (p + 1), y), gate,
                        pair.action, p, true));
                }
            }
        }

        for (int row = 0; row < rows - 1; row++) {
            for (int column = 0; column < columns; column++) {
                int index = row + column * rows;
                mainGui.shownElements.add(new ConnectionButton(mainGui,
                    area(pairX(column) + connectorPos, 16 + 9 + row * 18), index));
            }
        }

        // Ask the server what can go in the gate
        container.sendMessage(ContainerGate.ID_VALID_STATEMENTS);
    }

    private int pairX(int column) {
        return pairStart + 7 + column * (18 + pairWidth);
    }

    private IGuiArea area(int x, int y) {
        return new GuiRectangle(x, y, 18, 18).offset(mainGui.rootElement);
    }

    private boolean triggerOn(int i) {
        return i >= 0 && i < gate.triggerOn.length && gate.triggerOn[i];
    }

    private boolean setOn(int i) {
        return i >= 0 && i < gate.actionOn.length && gate.actionOn[i];
    }

    private boolean actionOn(int i) {
        return setOn(i) && gate.statements[i].action.get() != null;
    }

    private boolean isConnected(int i) {
        return i >= 0 && i < gate.connections.length && gate.connections[i];
    }

    @Override
    protected void drawBackgroundLayer(GuiGraphics graphics, float partialTicks) {
        int x0 = leftPos;
        int y0 = topPos;
        ICON_TOP.drawAt(graphics, x0, y0);
        for (int row = 0; row < rows; row++) {
            ICON_ROW.drawAt(graphics, x0, y0 + 16 + row * 18);
        }
        ICON_BOTTOM.drawAt(graphics, x0, y0 + 16 + rows * 18);

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int index = row + column * rows;
                int x = x0 + pairX(column) + connectorPos;
                int y = y0 + 16 + row * 18;
                draw(graphics, x, y, 7, 18, 176 + (triggerOn(index) ? 18 : 0), 18);
                draw(graphics, x + 11, y, 7, 18, 187 + (actionOn(index) ? 18 : 0), 18);
                draw(graphics, x + 7, y, 4, 18, 180 + (setOn(index) ? 18 : 0), 18);
            }
        }
        for (int row = 0; row < rows - 1; row++) {
            for (int column = 0; column < columns; column++) {
                int index = row + column * rows;
                int x = x0 + pairX(column) + connectorPos;
                int y = y0 + 16 + 9 + row * 18;
                int connected = isConnected(index) ? 18 : 0;
                draw(graphics, x, y, 18, 9, 176 + (setOn(index) ? 18 : 0), 36 + connected);
                draw(graphics, x, y + 9, 18, 9, 176 + (setOn(index + 1) ? 18 : 0), 36 + 9 + connected);
            }
        }
    }

    private static void draw(GuiGraphics graphics, int x, int y, int width, int height, int u, int v) {
        new GuiIcon(TEXTURE, u, v, width, height).drawAt(graphics, x, y);
    }

    @Override
    protected void drawForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY) {
        Component name = gate.variant.getDisplayName();
        graphics.drawString(font, name, leftPos + (imageWidth - font.width(name)) / 2, topPos + 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, leftPos + 5, topPos + 16 + rows * 18 + 4, 0x404040, false);
    }

    /** Joins (or splits) a pair of statements and the one below it. */
    private final class ConnectionButton extends GuiElementSimple implements IInteractionElement {
        private final int index;

        ConnectionButton(BuildCraftGui gui, IGuiArea area, int index) {
            super(gui, area);
            this.index = index;
        }

        @Override
        public void drawBackground(GuiGraphics graphics, float partialTicks) {
            int u = contains(gui.mouse) ? 194 : 176;
            draw(graphics, (int) getX(), (int) getY(), 18, 18, u, 72);
        }

        @Override
        public void onMouseClicked(int button) {
            if (button == 0 && contains(gui.mouse)) {
                boolean to = !isConnected(index);
                gate.connections[index] = to;
                container.setConnected(index, to);
            }
        }
    }
}
