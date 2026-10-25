/*
 * Based on BuildCraftCompat, (c) the BuildCraft Team, http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraftCompat is distributed under the terms of the Minecraft Mod Public License 1.0.1, or MMPL (a copy is in
 * LICENSE-ASSETS).
 */
package buildcraftreborn.compat.jei;

import java.util.List;

import net.minecraft.client.renderer.Rect2i;

import buildcraftreborn.compat.viewer.ScreenAreas;
import buildcraftreborn.lib.gui.GuiBC8;

import mezz.jei.api.gui.handlers.IGuiContainerHandler;

/** Tells JEI where BuildCraft GUIs draw outside their main area (the ledgers), so its item list moves out of the
 * way. */
public class GuiHandlerBuildCraft implements IGuiContainerHandler<GuiBC8<?>> {
    @Override
    public List<Rect2i> getGuiExtraAreas(GuiBC8<?> gui) {
        return ScreenAreas.ledgerAreas(gui);
    }
}
