/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.model;

import java.util.ArrayList;
import java.util.List;

/** The item model of a pluggable: its quads, shown with {@link ModelItemSimple#TRANSFORM_PLUG_AS_ITEM}. */
public class ModelPluggableItem extends ModelItemSimple {
    public ModelPluggableItem(MutableQuad[]... quads) {
        super(bake(quads), TRANSFORM_PLUG_AS_ITEM, false);
    }

    private static List<net.minecraft.client.renderer.block.model.BakedQuad> bake(MutableQuad[]... quads) {
        List<net.minecraft.client.renderer.block.model.BakedQuad> list = new ArrayList<>();
        for (MutableQuad[] qa : quads) {
            for (MutableQuad q : qa) {
                list.add(q.toBakedItem());
            }
        }
        return list;
    }
}
