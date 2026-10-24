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

import buildcraftreborn.lib.gui.elem.ToolTip;
import buildcraftreborn.lib.gui.help.ElementHelpInfo.HelpPosition;
import buildcraftreborn.lib.gui.pos.IGuiPosition;

/** An element that holds other elements and forwards events to them. */
public interface IContainingElement extends IInteractionElement {
    List<IGuiElement> getChildElements();

    default IGuiPosition getChildElementPosition() {
        return this;
    }

    /** Called after {@link #getChildElements()} is added to, possibly last (so it might not be called after every
     * addition). */
    default void calculateSizes() {}

    @Override
    default void addToolTips(List<ToolTip> tooltips) {
        for (IGuiElement elem : getChildElements()) {
            elem.addToolTips(tooltips);
        }
    }

    @Override
    default void addHelpElements(List<HelpPosition> elements) {
        for (IGuiElement elem : getChildElements()) {
            elem.addHelpElements(elements);
        }
    }

    @Override
    default List<IGuiElement> getThisAndChildrenAt(double x, double y) {
        List<IGuiElement> list = new ArrayList<>();
        if (contains(x, y)) {
            list.add(this);
            for (IGuiElement elem : getChildElements()) {
                list.addAll(elem.getThisAndChildrenAt(x, y));
            }
        }
        return list;
    }

    @Override
    default void onMouseClicked(int button) {
        for (IGuiElement elem : getChildElements()) {
            if (elem instanceof IInteractionElement interaction) {
                interaction.onMouseClicked(button);
            }
        }
    }

    @Override
    default void onMouseReleased(int button) {
        for (IGuiElement elem : getChildElements()) {
            if (elem instanceof IInteractionElement interaction) {
                interaction.onMouseReleased(button);
            }
        }
    }

    @Override
    default void onMouseDragged(int button, long ticksSinceClick) {
        for (IGuiElement elem : getChildElements()) {
            if (elem instanceof IInteractionElement interaction) {
                interaction.onMouseDragged(button, ticksSinceClick);
            }
        }
    }

    @Override
    default boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        boolean action = false;
        for (IGuiElement elem : getChildElements()) {
            if (elem instanceof IInteractionElement interaction) {
                action |= interaction.onKeyPressed(keyCode, scanCode, modifiers);
            }
        }
        return action;
    }

    @Override
    default boolean onCharTyped(char typedChar, int modifiers) {
        boolean action = false;
        for (IGuiElement elem : getChildElements()) {
            if (elem instanceof IInteractionElement interaction) {
                action |= interaction.onCharTyped(typedChar, modifiers);
            }
        }
        return action;
    }
}
