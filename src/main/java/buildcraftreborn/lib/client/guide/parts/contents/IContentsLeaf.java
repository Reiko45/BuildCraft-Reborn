/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.parts.contents;

/** A node of the contents page that has no children. */
public interface IContentsLeaf extends IContentsNode {
    @Override
    default void calcVisibility() {
        // NO-OP
    }

    @Override
    default void sort() {
        // NO-OP
    }

    @Override
    default void addChild(IContentsNode node) {
        // NO-OP
    }

    @Override
    default IContentsNode[] getVisibleChildren() {
        return new IContentsNode[0];
    }
}
