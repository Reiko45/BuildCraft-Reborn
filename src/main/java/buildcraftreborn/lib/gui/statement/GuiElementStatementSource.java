/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui.statement;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphics;

import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.api.statements.IGuiSlot;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.lib.gui.BuildCraftGui;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.IGuiElement;
import buildcraftreborn.lib.gui.IInteractionElement;
import buildcraftreborn.lib.gui.elem.ToolTip;
import buildcraftreborn.lib.gui.pos.GuiRectangle;
import buildcraftreborn.lib.gui.pos.IGuiPosition;
import buildcraftreborn.lib.statement.StatementContext;
import buildcraftreborn.lib.statement.StatementContext.StatementGroup;
import buildcraftreborn.lib.statement.StatementWrapper;

/** A panel beside the GUI (like a ledger) with every statement that can be used, grouped by the side they come from.
 * Statements are dragged from here onto the slots. */
public class GuiElementStatementSource<S extends IGuiSlot> implements IInteractionElement {
    public final BuildCraftGui gui;
    public final IGuiPosition position;
    public final StatementContext<S> ctx;
    private final boolean left;

    public final GuiElementStatementDrag dragger;

    public GuiElementStatementSource(BuildCraftGui gui, boolean left, StatementContext<S> ctx) {
        this.gui = gui;
        this.left = left;
        this.ctx = ctx;
        if (left) {
            position = gui.lowerLeftLedgerPos.offset(() -> -getWidth(), () -> 0);
            gui.lowerLeftLedgerPos = getPosition(1, 1);
        } else {
            position = gui.lowerRightLedgerPos;
            gui.lowerRightLedgerPos = getPosition(-1, 1);
        }
        GuiElementStatementDrag drag = null;
        for (IGuiElement element : gui.shownElements) {
            if (element instanceof GuiElementStatementDrag d) {
                drag = d;
                break;
            }
        }
        if (drag == null) {
            drag = new GuiElementStatementDrag(gui);
            gui.shownElements.add(drag);
        }
        dragger = drag;
    }

    @Override
    public double getX() {
        return position.getX();
    }

    @Override
    public double getY() {
        return position.getY();
    }

    @Override
    public double getWidth() {
        int width = 0;
        for (StatementGroup<S> group : ctx.getAllPossible()) {
            int count = group.getValues().size();
            width = Math.max(width, count);
        }
        return Math.min(4, width) * 18;
    }

    @Override
    public double getHeight() {
        int height = 0;
        for (StatementGroup<S> group : ctx.getAllPossible()) {
            int count = group.getValues().size();
            height += (count + 3) / 4;
        }
        return height * 18 + 4;
    }

    private void iterateSlots(ISlotIter<S> iter) {
        int dx = left ? -1 : 1;
        int sx = left ? 3 : 0;
        int ex = sx + dx * 4;
        int x = sx;
        int y = 0;
        for (StatementGroup<S> group : ctx.getAllPossible()) {
            int visited = 0;
            for (S slot : group.getValues()) {
                double px = getX() + x * 18;
                double py = getY() + y * 18;
                iter.iterate(slot, new GuiRectangle(px, py, 18, 18));
                visited++;
                x += dx;
                if (x == ex) {
                    x = sx;
                    y++;
                }
            }

            if (visited > 0 && x != sx) {
                x = sx;
                y++;
            }
        }
    }

    interface ISlotIter<S extends IGuiSlot> {
        void iterate(@Nullable S slot, GuiRectangle area);
    }

    @Override
    public void drawBackground(GuiGraphics graphics, float partialTicks) {
        iterateSlots((s, area) -> drawGuiSlot(graphics, s, area.x, area.y));
    }

    public static void drawGuiSlot(GuiGraphics graphics, @Nullable IGuiSlot guiSlot, double x, double y) {
        if (guiSlot instanceof IStatementParameter param) {
            ParameterRenderer.draw(graphics, param, x, y);
            return;
        }
        GuiIcon background = GuiElementStatement.SLOT_COLOUR;
        if (guiSlot instanceof StatementWrapper wrapper) {
            EnumPipePart part = wrapper.sourcePart;
            if (part != EnumPipePart.CENTER) {
                background = background.offset(0, (1 + part.getIndex()) * 18);
            }
        }
        background.drawAt(graphics, x, y);
        if (guiSlot != null) {
            ISprite sprite = guiSlot.getSprite();
            if (sprite != null) {
                GuiIcon.draw(graphics, sprite, x + 1, y + 1, x + 17, y + 17);
            }
        }
    }

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        iterateSlots((slot, area) -> {
            if (slot == null) return;
            if (area.contains(gui.mouse)) {
                tooltips.add(new ToolTip(slot.getTooltip()));
            }
        });
    }

    @Override
    public void onMouseClicked(int button) {
        if (button == 0) {
            iterateSlots((slot, area) -> {
                if (area.contains(gui.mouse)) {
                    dragger.startDragging(slot);
                }
            });
        }
    }
}
