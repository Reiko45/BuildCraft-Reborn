/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.statements;

public final class StatementMouseClick {
    private int button;
    private boolean shift;

    public StatementMouseClick(int button, boolean shift) {
        this.button = button;
        this.shift = shift;
    }

    public boolean isShift() {
        return shift;
    }

    public int getButton() {
        return button;
    }
}
