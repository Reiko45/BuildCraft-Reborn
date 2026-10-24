/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui.statement;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;

import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.api.statements.IGuiSlot;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.lib.gui.BuildCraftGui;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.IGuiElement;
import buildcraftreborn.lib.gui.IMenuElement;
import buildcraftreborn.lib.misc.data.IReference;
import buildcraftreborn.lib.statement.StatementWrapper;

/** Drags a statement (from a source panel, or right-dragged out of a slot) to drop on a slot. */
public class GuiElementStatementDrag implements IMenuElement {

    public final BuildCraftGui gui;

    private boolean isDragging;

    @Nullable
    private IGuiSlot dragging;

    public GuiElementStatementDrag(BuildCraftGui gui) {
        this.gui = gui;
    }

    // Dragging

    public void startDragging(IGuiSlot slot) {
        isDragging = true;
        dragging = slot;
        gui.currentMenu = this;
    }

    // IGuiElement

    @Override
    public double getX() {
        return 0;
    }

    @Override
    public double getY() {
        return 0;
    }

    @Override
    public double getWidth() {
        return 0;
    }

    @Override
    public double getHeight() {
        return 0;
    }

    @Override
    public void drawForeground(GuiGraphics graphics, float partialTicks) {
        if (isDragging) {
            boolean canPlace = false;
            for (IGuiElement element : gui.getElementsAt(gui.mouse.getX(), gui.mouse.getY())) {
                if (element instanceof IReference<?> ref) {
                    if (checkCanSet(ref, dragging)) {
                        canPlace = true;
                        break;
                    }
                }
            }
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 400);
            if (!canPlace) {
                RenderSystem.setShaderColor(1.0f, 0.7f, 0.7f, 1.0f);
            }
            double x = gui.mouse.getX() - 9;
            double y = gui.mouse.getY() - 9;
            if (dragging instanceof IStatementParameter param) {
                ParameterRenderer.draw(graphics, param, x, y);
            } else {
                GuiIcon background = GuiElementStatement.SLOT_COLOUR;
                if (dragging instanceof StatementWrapper wrapper) {
                    EnumPipePart part = wrapper.sourcePart;
                    if (part != EnumPipePart.CENTER) {
                        background = background.offset(0, (1 + part.getIndex()) * 18);
                    }
                }
                background.drawAt(graphics, x, y);
                if (dragging != null) {
                    ISprite sprite = dragging.getSprite();
                    if (sprite != null) {
                        GuiIcon.draw(graphics, sprite, x + 1, y + 1, x + 17, y + 17);
                    }
                }
            }
            RenderSystem.setShaderColor(1, 1, 1, 1);
            graphics.pose().popPose();
        }
    }

    private static <T> boolean checkCanSet(IReference<T> ref, Object value) {
        if (value == null) {
            return ref.canSet(null);
        }
        T obj = ref.convertToType(value);
        return obj != null && ref.canSet(obj);
    }

    // IInteractionElement

    @Override
    public void onMouseClicked(int button) {
        if (button != 1) {
            return;
        }
        for (IGuiElement element : gui.getElementsAt(gui.mouse.getX(), gui.mouse.getY())) {
            if (element instanceof IReference<?> ref) {
                Object obj = ref.get();
                if (obj == null || obj instanceof IGuiSlot) {
                    startDragging((IGuiSlot) obj);
                    break;
                }
            }
        }
    }

    @Override
    public void onMouseReleased(int button) {
        if (!isDragging) {
            return;
        }
        for (IGuiElement element : gui.getElementsAt(gui.mouse.getX(), gui.mouse.getY())) {
            if (element instanceof IReference<?> ref) {
                ref.setIfCan(dragging);
            }
        }
        isDragging = false;
        dragging = null;
        if (gui.currentMenu == this) {
            gui.currentMenu = null;
        }
    }

    // IMenuElement

    @Override
    public boolean shouldFullyOverride() {
        return false;
    }
}
