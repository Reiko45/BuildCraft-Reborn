/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui.statement;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.StatementMouseClick;
import buildcraftreborn.lib.gui.BuildCraftGui;
import buildcraftreborn.lib.gui.GuiElementSimple;
import buildcraftreborn.lib.gui.IInteractionElement;
import buildcraftreborn.lib.gui.elem.ToolTip;
import buildcraftreborn.lib.gui.pos.IGuiArea;
import buildcraftreborn.lib.misc.data.IReference;
import buildcraftreborn.lib.statement.FullStatement;

/** A parameter slot of a trigger or action. Clicking it either changes it directly (with the held item, say) or opens
 * the variant popup. */
public class GuiElementStatementParam extends GuiElementSimple
    implements IInteractionElement, IReference<IStatementParameter> {

    private final IStatementContainer container;
    private final FullStatement<?> ref;
    private final int paramIndex;
    private final boolean draw;

    public GuiElementStatementParam(BuildCraftGui gui, IGuiArea element, IStatementContainer container,
        FullStatement<?> ref, int index, boolean draw) {
        super(gui, element);
        this.container = container;
        this.ref = ref;
        this.paramIndex = index;
        this.draw = draw;
    }

    // IReference

    @Override
    public IStatementParameter get() {
        return ref.get(paramIndex);
    }

    @Override
    public void set(IStatementParameter to) {
        ref.set(paramIndex, to);
        ref.postSetFromGui(paramIndex);
    }

    @Override
    public boolean canSet(IStatementParameter value) {
        return ref.canSet(paramIndex, value);
    }

    @Override
    public Class<IStatementParameter> getHeldType() {
        return IStatementParameter.class;
    }

    // ITooltipElement

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        if (contains(gui.mouse)) {
            IStatementParameter s = get();
            if (s != null) {
                tooltips.add(new ToolTip(s.getTooltip()));
            }
        }
    }

    // IGuiElement

    @Override
    public void drawBackground(GuiGraphics graphics, float partialTicks) {
        if (draw) {
            IStatement slot = ref.get();
            int max = slot == null ? 0 : slot.maxParameters();
            double x = getX();
            double y = getY();
            if (paramIndex >= max) {
                GuiElementStatement.SLOT_COLOUR.drawAt(graphics, x, y);
                GuiElementStatement.ICON_SLOT_BLOCKED.drawAt(graphics, x, y);
                return;
            }
            GuiElementStatementSource.drawGuiSlot(graphics, get(), x, y);
        }
    }

    // IInteractionElement

    @Override
    public void onMouseClicked(int button) {
        if (ref.canInteract && contains(gui.mouse) && (button == 0 || button == 1)) {
            IStatementParameter param = get();
            if (param == null) {
                return;
            }
            // 1.21: right clicks and shift reach the parameter too (to count down, or go back through options)
            StatementMouseClick clickEvent = new StatementMouseClick(button, Screen.hasShiftDown());

            Player currentPlayer = Minecraft.getInstance().player;
            // 1.12: the stack held on the mouse
            ItemStack heldStack = currentPlayer == null ? ItemStack.EMPTY
                : currentPlayer.containerMenu.getCarried();

            IStatementParameter pNew = param.onClick(container, ref.get(), heldStack, clickEvent);
            if (pNew != null) {
                set(pNew);
            } else if (button == 0) {
                IStatementParameter[] possible = param.getPossible(container);
                if (!param.isPossibleOrdered()) {
                    List<IStatementParameter> list = new ArrayList<>();
                    for (IStatementParameter p2 : possible) {
                        if (p2 != null) {
                            list.add(p2);
                        }
                    }
                    possible = list.toArray(new IStatementParameter[0]);
                }
                gui.currentMenu = GuiElementStatementVariant.create(gui, this, this, possible);
            }
        }
    }
}
