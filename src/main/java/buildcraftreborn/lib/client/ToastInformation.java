/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.resources.ResourceLocation;

import buildcraftreborn.lib.gui.ISimpleDrawable;
import buildcraftreborn.lib.misc.LocaleUtil;

/** A toast with an icon and a line of text, shown for five seconds. */
public class ToastInformation implements Toast {
    private static final ResourceLocation BACKGROUND = ResourceLocation.withDefaultNamespace("toast/advancement");

    public final String localeKey;
    @Nullable
    public final ISimpleDrawable icon;
    private final Object type;

    public ToastInformation(String localeKey, @Nullable ISimpleDrawable icon, Object type) {
        this.localeKey = localeKey;
        this.icon = icon;
        this.type = type;
    }

    public ToastInformation(String localeKey, @Nullable ISimpleDrawable icon) {
        this(localeKey, icon, NO_TOKEN);
    }

    @Override
    public Visibility render(GuiGraphics graphics, ToastComponent toastComponent, long timeSinceLastVisible) {
        graphics.blitSprite(BACKGROUND, 0, 0, width(), height());
        int x = 10;
        if (icon != null) {
            icon.drawAt(graphics, 8, 8);
            x = 30;
        }
        graphics.drawString(toastComponent.getMinecraft().font, LocaleUtil.localize(localeKey), x, 12, -1, false);
        return timeSinceLastVisible >= 5000L * toastComponent.getNotificationDisplayTimeMultiplier()
            ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public Object getToken() {
        return type;
    }
}
