/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide;

import net.minecraft.client.Minecraft;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;

import buildcraftreborn.lib.item.ItemGuide;

/** Sets up the guide book on the client: (re)loading it, and opening it from the guide item. */
public final class GuideClient {
    private GuideClient() {}

    public static void init(IEventBus modBus) {
        modBus.addListener((RegisterClientReloadListenersEvent event) -> event.registerReloadListener(
            GuideManager.INSTANCE));
        // The contents need the world's items and recipes
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingIn event) -> GuideManager.INSTANCE
            .invalidate());
        NeoForge.EVENT_BUS.addListener((RecipesUpdatedEvent event) -> GuideManager.INSTANCE.invalidate());
        ItemGuide.openGui = (player, book) -> Minecraft.getInstance().setScreen(new GuiGuide(book.toString()));
        ItemGuide.bookTitle = book -> {
            GuideBook guideBook = GuideBookRegistry.INSTANCE.getBook(book);
            return guideBook == null ? null : guideBook.title;
        };
    }
}
