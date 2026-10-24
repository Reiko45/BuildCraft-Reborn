/*
 * Based on BuildCraftCompat, (c) the BuildCraft Team, http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraftCompat is distributed under the terms of the Minecraft Mod Public License 1.0.1, or MMPL (a copy is in
 * LICENSE-ASSETS).
 */
package buildcraftreborn.compat.jei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.Rect2i;

import buildcraftreborn.lib.gui.GuiBC8;
import buildcraftreborn.lib.gui.IGuiElement;
import buildcraftreborn.lib.gui.pos.IGuiArea;

import mezz.jei.api.gui.handlers.IGuiContainerHandler;

/** Tells JEI where BuildCraft GUIs draw outside their main area (the ledgers), so its item list moves out of the
 * way. */
public class GuiHandlerBuildCraft implements IGuiContainerHandler<GuiBC8<?>> {
    @Override
    public List<Rect2i> getGuiExtraAreas(GuiBC8<?> gui) {
        IGuiArea root = gui.mainGui.rootElement;
        List<Rect2i> list = new ArrayList<>();
        for (IGuiElement element : gui.mainGui.shownElements) {
            // Ledger style elements are all top level, and outside (or partly outside) the main area
            if (element.getX() >= root.getX() && element.getEndX() <= root.getEndX()
                && element.getY() >= root.getY() && element.getEndY() <= root.getEndY()) {
                continue;
            }
            int x = (int) Math.floor(element.getX());
            int y = (int) Math.floor(element.getY());
            int endX = (int) Math.ceil(element.getEndX());
            int endY = (int) Math.ceil(element.getEndY());
            list.add(new Rect2i(x, y, endX - x, endY - y));
        }
        return list;
    }
}
