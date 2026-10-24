/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui;

/** A GUI element that reacts to the mouse and keyboard. 1.21: keyboard input arrives as GLFW key codes
 * ({@link #onKeyPressed}) and typed characters ({@link #onCharTyped}) instead of 1.12's single
 * {@code onKeyPress(char, int)}. */
public interface IInteractionElement extends IGuiElement {
    /** This is called EVEN IF the mouse is not inside your width and height! */
    default void onMouseClicked(int button) {}

    /** This is called EVEN IF the mouse is not inside your width and height! */
    default void onMouseDragged(int button, long ticksSinceClick) {}

    /** This is called EVEN IF the mouse is not inside your width and height! */
    default void onMouseReleased(int button) {}

    /** @return True if this element handled the key, in which case the screen won't (so escape won't close the GUI).
     *         It is <i>highly</i> recommended that you close something if escape is pressed. */
    default boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    /** @return True if this element consumed the typed character. */
    default boolean onCharTyped(char typedChar, int modifiers) {
        return false;
    }
}
