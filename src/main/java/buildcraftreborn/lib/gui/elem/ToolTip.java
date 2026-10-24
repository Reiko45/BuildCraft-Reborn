/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui.elem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;

import com.google.common.collect.ForwardingList;

import net.minecraft.network.chat.Component;

/** A tooltip: lines of text, with an optional hover delay. 1.21: lines are {@link Component}s (1.12 used strings). */
public class ToolTip extends ForwardingList<Component> implements RandomAccess {

    /* If the impl list class does not implement RandomAccess then the interface MUST be removed from this class */
    private final List<Component> delegate = new ArrayList<>();
    private final long delay;
    private long mouseOverStart;

    /** Creates a {@link ToolTip} from translation keys. A translation may contain "\n" to split into lines. */
    public static ToolTip createLocalized(String... localeKeys) {
        List<Component> allLines = new ArrayList<>();
        for (String key : localeKeys) {
            for (String line : Component.translatable(key).getString().split("\\\\n|\n")) {
                allLines.add(Component.literal(line));
            }
        }
        return new ToolTip(allLines);
    }

    public ToolTip(Component... lines) {
        this.delay = 0;
        Collections.addAll(delegate, lines);
    }

    public ToolTip(int delay, Component... lines) {
        this.delay = delay;
        Collections.addAll(delegate, lines);
    }

    public ToolTip(List<Component> lines) {
        this.delay = 0;
        delegate.addAll(lines);
    }

    @Override
    protected final List<Component> delegate() {
        return delegate;
    }

    public void onTick(boolean mouseOver) {
        if (delay == 0) {
            return;
        }
        if (mouseOver) {
            if (mouseOverStart == 0) {
                mouseOverStart = System.currentTimeMillis();
            }
        } else {
            mouseOverStart = 0;
        }
    }

    public boolean isReady() {
        if (delay == 0) {
            return true;
        }
        if (mouseOverStart == 0) {
            return false;
        }
        return System.currentTimeMillis() - mouseOverStart >= delay;
    }

    /** Called before the tooltip is shown; dynamic tooltips rebuild their lines here. */
    public void refresh() {}
}
