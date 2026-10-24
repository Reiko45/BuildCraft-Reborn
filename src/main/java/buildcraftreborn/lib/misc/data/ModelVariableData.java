/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.misc.data;

import java.util.Arrays;
import java.util.List;

import buildcraftreborn.lib.expression.node.value.ITickableNode;
import buildcraftreborn.lib.expression.node.value.NodeStateful;
import buildcraftreborn.lib.expression.node.value.NodeUpdatable;

/** Per-tile state for variable models (stateful and updatable model variables). Reset whenever models reload. */
public class ModelVariableData {
    private static int currentBakeId = 0;
    private int bakeId = -1;
    private ITickableNode[] tickableNodes;

    public static void onModelBake() {
        currentBakeId++;
    }

    public boolean hasNoNodes() {
        return tickableNodes == null;
    }

    public void setNodes(ITickableNode[] nodes) {
        bakeId = currentBakeId;
        tickableNodes = nodes;
    }

    public void addNodes(ITickableNode[] additional) {
        int originalLength = tickableNodes.length;
        tickableNodes = Arrays.copyOf(tickableNodes, originalLength + additional.length);
        System.arraycopy(additional, 0, tickableNodes, originalLength, additional.length);
    }

    private boolean checkModelBake() {
        if (tickableNodes == null) {
            return false;
        }
        if (currentBakeId == bakeId) {
            return true;
        }
        tickableNodes = null;
        return false;
    }

    /** @see ITickableNode#refresh() */
    public void refresh() {
        if (checkModelBake()) {
            for (ITickableNode node : tickableNodes) {
                node.refresh();
            }
        }
    }

    /** @see ITickableNode#tick() */
    public void tick() {
        if (checkModelBake()) {
            for (ITickableNode node : tickableNodes) {
                node.tick();
            }
        }
    }

    public void addDebugInfo(List<String> to) {
        if (tickableNodes != null) {
            for (ITickableNode node : tickableNodes) {
                if (node instanceof NodeUpdatable nU) {
                    to.add("  " + nU.name + " = " + nU.variable.evaluateAsString());
                } else if (node instanceof NodeStateful.Instance nS) {
                    to.add("  " + nS.getContainer().name + " = " + nS.storedVar.evaluateAsString());
                }
            }
        }
    }
}
