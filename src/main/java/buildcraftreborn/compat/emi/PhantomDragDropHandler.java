/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.compat.emi;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.lib.gui.GuiBC8;
import buildcraftreborn.lib.gui.slot.SlotPhantom;

import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;

/** Lets items be dragged from EMI into the phantom slots of BuildCraft's screens (filters, blueprints, the packager's
 * pattern), as JEI's ghost ingredients can. While dragging, the slots the item can go in are highlighted. EMI only
 * matches a screen's exact class, so this is registered for every screen and checks for {@link GuiBC8} itself. */
public class PhantomDragDropHandler implements EmiDragDropHandler<Screen> {
    private static final int HIGHLIGHT = 0x88_22_BB_33;

    private record Target(SlotPhantom slot, Bounds bounds) {}

    @Override
    public boolean dropStack(Screen screen, EmiIngredient ingredient, int x, int y) {
        if (!(screen instanceof GuiBC8<?> gui)) {
            return false;
        }
        ItemStack stack = itemOf(ingredient);
        for (Target target : targets(gui, stack)) {
            if (target.bounds().contains(x, y)) {
                gui.getMenu().sendSetPhantomSlot(target.slot(), stack.copyWithCount(1));
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(Screen screen, EmiIngredient dragged, GuiGraphics graphics, int mouseX, int mouseY,
        float delta) {
        if (screen instanceof GuiBC8<?> gui) {
            for (Target target : targets(gui, itemOf(dragged))) {
                Bounds b = target.bounds();
                graphics.fill(b.left(), b.top(), b.right(), b.bottom(), HIGHLIGHT);
            }
        }
    }

    private static ItemStack itemOf(EmiIngredient ingredient) {
        for (EmiStack stack : ingredient.getEmiStacks()) {
            ItemStack item = stack.getItemStack();
            if (!item.isEmpty()) {
                return item;
            }
        }
        return ItemStack.EMPTY;
    }

    private static List<Target> targets(GuiBC8<?> gui, ItemStack stack) {
        List<Target> targets = new ArrayList<>();
        if (stack.isEmpty()) {
            return targets;
        }
        for (Slot slot : gui.getMenu().slots) {
            // (hidden phantom slots are moved far off screen; a phantom slot's mayPlace is always false, so this
            // asks its inventory, as the server will)
            if (slot instanceof SlotPhantom phantom && slot.isActive() && slot.x > -1000 && slot.y > -1000
                && phantom.itemHandler.canSet(phantom.handlerIndex, stack)) {
                targets.add(new Target(phantom,
                    new Bounds(gui.getGuiLeft() + slot.x - 1, gui.getGuiTop() + slot.y - 1, 18, 18)));
            }
        }
        return targets;
    }
}
