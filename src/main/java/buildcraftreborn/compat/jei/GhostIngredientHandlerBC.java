/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.compat.jei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.lib.gui.GuiBC8;
import buildcraftreborn.lib.gui.slot.SlotPhantom;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;

/** Lets items be dragged from JEI into phantom slots (filters, lists, blueprints): the slot is set to one of the
 * item, the same as clicking it with the item would. New in BuildCraft Reborn. */
public class GhostIngredientHandlerBC implements IGhostIngredientHandler<GuiBC8<?>> {
    @Override
    public <I> List<Target<I>> getTargetsTyped(GuiBC8<?> gui, ITypedIngredient<I> ingredient, boolean doStart) {
        List<Target<I>> targets = new ArrayList<>();
        ItemStack stack = ingredient.getIngredient(VanillaTypes.ITEM_STACK).orElse(ItemStack.EMPTY);
        if (stack.isEmpty()) {
            return targets;
        }
        for (Slot slot : gui.getMenu().slots) {
            // (hidden phantom slots are moved far off screen; a phantom slot's mayPlace is always false, so this
            // asks its inventory, as the server will)
            if (!(slot instanceof SlotPhantom phantom) || !slot.isActive() || slot.x < -1000 || slot.y < -1000
                || !phantom.itemHandler.canSet(phantom.handlerIndex, stack)) {
                continue;
            }
            Rect2i area = new Rect2i(gui.getGuiLeft() + slot.x, gui.getGuiTop() + slot.y, 16, 16);
            targets.add(new Target<>() {
                @Override
                public Rect2i getArea() {
                    return area;
                }

                @Override
                public void accept(I ingredient) {
                    gui.getMenu().sendSetPhantomSlot(phantom, stack.copyWithCount(1));
                }
            });
        }
        return targets;
    }

    @Override
    public void onComplete() {}
}
