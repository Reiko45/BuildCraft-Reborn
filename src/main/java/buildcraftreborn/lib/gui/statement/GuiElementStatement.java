/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui.statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.lib.client.BCLibSprites;
import buildcraftreborn.lib.client.sprite.SpriteNineSliced;
import buildcraftreborn.lib.client.sprite.SpriteRaw;
import buildcraftreborn.lib.gui.BuildCraftGui;
import buildcraftreborn.lib.gui.GuiElementSimple;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.IInteractionElement;
import buildcraftreborn.lib.gui.elem.ToolTip;
import buildcraftreborn.lib.gui.pos.IGuiArea;
import buildcraftreborn.lib.misc.data.IReference;
import buildcraftreborn.lib.statement.FullStatement;
import buildcraftreborn.lib.statement.StatementContext;
import buildcraftreborn.lib.statement.StatementContext.StatementGroup;

/** A trigger or action slot. Click it to pick one of its variants, shift-click to clear it, or drop a statement
 * dragged from the side panels on it. */
public class GuiElementStatement<S extends IStatement> extends GuiElementSimple
    implements IInteractionElement, IReference<S> {

    public static final ResourceLocation TEXTURE_SELECTOR;

    public static final GuiIcon SLOT_COLOUR;
    public static final GuiIcon ICON_SLOT_BLOCKED;
    public static final GuiIcon ICON_SLOT_NOT_SET;

    public static final SpriteRaw ICON_SELECT_HOVER;
    public static final SpriteNineSliced SELECTION_HOVER;

    static {
        TEXTURE_SELECTOR = BuildCraftReborn.id("textures/gui/misc_slots.png");
        SLOT_COLOUR = new GuiIcon(TEXTURE_SELECTOR, 0, 0, 18, 18);
        ICON_SLOT_BLOCKED = SLOT_COLOUR.offset(18, 0);
        ICON_SLOT_NOT_SET = ICON_SLOT_BLOCKED.offset(18, 0);
        ICON_SELECT_HOVER = new SpriteRaw(TEXTURE_SELECTOR, 18, 18, 36, 36, 256);
        SELECTION_HOVER = new SpriteNineSliced(ICON_SELECT_HOVER, 8, 8, 28, 28, 36);
    }

    private final FullStatement<S> ref;
    private final StatementContext<?> ctx;
    private final boolean draw;

    public GuiElementStatement(BuildCraftGui gui, IGuiArea element, FullStatement<S> ref, StatementContext<?> ctx,
        boolean draw) {
        super(gui, element);
        this.ref = ref;
        this.ctx = ctx;
        this.draw = draw;
    }

    // IReference

    @Override
    public S get() {
        return ref.get();
    }

    @Override
    public void set(S to) {
        ref.set(to);
        ref.postSetFromGui(-1);
    }

    @Override
    public boolean canSet(S value) {
        return ref.canSet(value);
    }

    @Override
    public S convertToType(Object value) {
        return ref.convertToType(value);
    }

    @Override
    public Class<S> getHeldType() {
        return ref.getHeldType();
    }

    // ITooltipElement

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        if (contains(gui.mouse)) {
            S s = get();
            if (s != null) {
                tooltips.add(new ToolTip(s.getTooltip()));
            }
        }
    }

    // IGuiElement

    @Override
    public void drawBackground(GuiGraphics graphics, float partialTicks) {
        if (draw) {
            S statement = ref.get();
            double x = getX();
            double y = getY();
            GuiElementStatementSource.drawGuiSlot(graphics, statement, x, y);
            if (!ref.canInteract) {
                GuiIcon.draw(graphics, BCLibSprites.LOCK, x + 1, y + 1, x + 17, y + 17);
            }
        }
    }

    // IInteractionElement

    @Override
    public void onMouseClicked(int button) {
        if (!contains(gui.mouse)) {
            return;
        }
        if (ref.canInteract && button == 0) {
            if (Screen.hasShiftDown()) {
                set(null);
                return;
            }
            S s = get();
            if (s == null) {
                return;
            }
            List<IStatement> possible = new ArrayList<>();
            Collections.addAll(possible, s.getPossible());
            if (!s.isPossibleOrdered()) {
                List<IStatement> list = new ArrayList<>();
                list.add(null);
                for (IStatement p2 : possible) {
                    if (p2 != null) {
                        list.add(p2);
                    }
                }
                possible = list;
            }
            if (ctx != null) {
                possible.removeIf(f -> {
                    for (StatementGroup<?> group : ctx.getAllPossible()) {
                        if (group.getValues().contains(f)) {
                            return false;
                        }
                    }
                    return true;
                });
            }
            gui.currentMenu = GuiElementStatementVariant.create(gui, this, this, possible.toArray(new IStatement[0]));
        }
    }
}
