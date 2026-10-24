/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui.statement;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.IStatementParameter.DrawType;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.ISimpleDrawable;

/** Draws statement parameters in their slot background. */
public final class ParameterRenderer {
    private static final ISimpleDrawable BACKGROUND_DRAWABLE = (g, x, y) -> GuiElementStatement.SLOT_COLOUR.drawAt(g,
        x, y);
    private static final Map<DrawType, Function<IStatementParameter, ISimpleDrawable>> DRAW_TYPES;

    static {
        DRAW_TYPES = new EnumMap<>(DrawType.class);
        DRAW_TYPES.put(DrawType.SPRITE_ONLY, ParameterRenderer::getSpriteDrawable);
        DRAW_TYPES.put(DrawType.STACK_ONLY, p -> getStackDrawable(p, false));
        DRAW_TYPES.put(DrawType.STACK_ONLY_OR_QUESTION_MARK, p -> getStackDrawable(p, true));
        DRAW_TYPES.put(DrawType.SPRITE_STACK, p -> getSpriteDrawable(p).andThen(getStackDrawable(p, false)));
        DRAW_TYPES.put(DrawType.STACK_SPRITE, p -> getStackDrawable(p, false).andThen(getSpriteDrawable(p)));
        DRAW_TYPES.put(DrawType.SPRITE_STACK_OR_QUESTION_MARK,
            p -> getSpriteDrawable(p).andThen(getStackDrawable(p, true)));
        DRAW_TYPES.put(DrawType.STACK_OR_QUESTION_MARK_THEN_SPRITE,
            p -> getStackDrawable(p, true).andThen(getSpriteDrawable(p)));
    }

    private ParameterRenderer() {}

    public static ISimpleDrawable getSpriteDrawable(IStatementParameter param) {
        return (g, x, y) -> {
            ISprite sprite = param.getSprite();
            if (sprite != null) {
                GuiIcon.draw(g, sprite, x + 1, y + 1, x + 17, y + 17);
            }
        };
    }

    public static ISimpleDrawable getStackDrawable(IStatementParameter param, boolean orQuestionMark) {
        return (g, x, y) -> {
            ItemStack stack = param.getItemStack();
            if (!stack.isEmpty()) {
                g.renderItem(stack, (int) x + 1, (int) y + 1);
                g.renderItemDecorations(net.minecraft.client.Minecraft.getInstance().font, stack, (int) x + 1,
                    (int) y + 1);
            } else if (orQuestionMark) {
                GuiElementStatement.ICON_SLOT_NOT_SET.drawAt(g, x + 1, y + 1);
            }
        };
    }

    public static ISimpleDrawable getDrawable(IStatementParameter param) {
        if (param instanceof IDrawingParameter drawing) {
            return BACKGROUND_DRAWABLE.andThen(drawing.getDrawable());
        }
        DrawType type = param.getDrawType();
        return BACKGROUND_DRAWABLE.andThen(DRAW_TYPES.get(type).apply(param));
    }

    public static void draw(GuiGraphics graphics, IStatementParameter param, double x, double y) {
        getDrawable(param).drawAt(graphics, x, y);
    }
}
