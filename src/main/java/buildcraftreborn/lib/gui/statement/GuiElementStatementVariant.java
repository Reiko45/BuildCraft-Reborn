/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui.statement;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;

import buildcraftreborn.api.statements.IGuiSlot;
import buildcraftreborn.lib.client.sprite.SpriteNineSliced;
import buildcraftreborn.lib.gui.BuildCraftGui;
import buildcraftreborn.lib.gui.GuiElementSimple;
import buildcraftreborn.lib.gui.IMenuElement;
import buildcraftreborn.lib.gui.elem.ToolTip;
import buildcraftreborn.lib.gui.pos.GuiRectangle;
import buildcraftreborn.lib.gui.pos.IGuiArea;
import buildcraftreborn.lib.misc.data.IReference;

/** The popup of a slot's variants, laid out in rings around the slot. Releasing the mouse over one picks it. */
public class GuiElementStatementVariant extends GuiElementSimple implements IMenuElement {
    public static final SpriteNineSliced SELECTION_HOVER = GuiElementStatement.SELECTION_HOVER;

    /** An array containing [offset][X,Y] */
    private static final int[][] OFFSET_HOVER = {
        // Centre
        { 0, 0 },
        // First 8
        { -1, -1 }, { 0, -1 }, { 1, -1 }, { 1, 0 }, { 1, 1 }, { 0, 1 }, { -1, 1 }, { -1, 0 },
        // Top row + going down
        { -2, -2 }, { -1, -2 }, { 0, -2 }, { 1, -2 }, { 2, -2 }, { 2, -1 }, { 2, 0 }, { 2, 1 },
        // Bottom row + going up
        { 2, 2 }, { 1, 2 }, { 0, 2 }, { -1, 2 }, { -2, 2 }, { -2, 1 }, { -2, 0 }, { -2, -1 } //
    };

    private final IReference<? extends IGuiSlot> ref;
    private final IGuiSlot[] possible;
    private final IGuiArea[] posPossible;

    public GuiElementStatementVariant(BuildCraftGui gui, IGuiArea element, IReference<? extends IGuiSlot> ref,
        IGuiSlot[] possible, IGuiArea[] posPossible) {
        super(gui, element);
        this.ref = ref;
        this.possible = possible;
        this.posPossible = posPossible;
    }

    public static GuiElementStatementVariant create(BuildCraftGui gui, IGuiArea parent,
        IReference<? extends IGuiSlot> ref, IGuiSlot[] possible) {
        int count = Math.min(OFFSET_HOVER.length, possible.length);
        possible = possible.length == count ? possible : Arrays.copyOf(possible, count);
        IGuiArea[] posPossible = new IGuiArea[count];
        IGuiArea base = new GuiRectangle(18, 18).offset(parent);
        for (int i = 0; i < count; i++) {
            posPossible[i] = base.offset(OFFSET_HOVER[i][0] * 18, OFFSET_HOVER[i][1] * 18);
        }
        int sub = 18 * (count > 9 ? 2 : 1);
        int add = 18 * (count > 9 ? 3 : 1);
        int offset = -sub - 4;
        int size = 8 + add + 18 * 2;
        IGuiArea area = new GuiRectangle(offset, offset, size, size).offset(parent);
        return new GuiElementStatementVariant(gui, area, ref, possible, posPossible);
    }

    interface ISlotIter {
        void iterate(IGuiArea area, IGuiSlot slot);
    }

    private void iteratePossible(ISlotIter iter) {
        for (int p = 0; p < possible.length; p++) {
            IGuiSlot slot = possible[p];
            if (slot != null) {
                iter.iterate(posPossible[p], slot);
            }
        }
    }

    // IGuiElement

    @Override
    public void drawBackground(GuiGraphics graphics, float partialTicks) {
        graphics.pose().pushPose();
        // Render above items in the players inventory
        graphics.pose().translate(0, 0, 400);
        SELECTION_HOVER.draw(graphics, this);
        iteratePossible((pos, slot) -> GuiElementStatementSource.drawGuiSlot(graphics, slot, pos.getX(), pos.getY()));
        graphics.pose().popPose();
    }

    // ITooltipElement

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        iteratePossible((pos, slot) -> {
            if (pos.contains(gui.mouse)) {
                tooltips.add(new ToolTip(slot.getTooltip()));
            }
        });
    }

    // IInteractionElement

    @Override
    public void onMouseReleased(int button) {
        gui.currentMenu = null;
        iteratePossible((pos, slot) -> {
            if (pos.contains(gui.mouse)) {
                ref.setIfCan(slot);
            }
        });
    }
}
