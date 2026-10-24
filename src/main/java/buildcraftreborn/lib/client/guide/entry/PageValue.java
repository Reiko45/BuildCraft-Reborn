/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.entry;

import java.util.List;
import java.util.Objects;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.GsonHelper;

import buildcraftreborn.lib.client.guide.GuideManager;
import buildcraftreborn.lib.gui.ISimpleDrawable;
import buildcraftreborn.lib.misc.LocaleUtil;

/** Something that the guide has (or could have) a page about: an item, a statement, or an external page. */
public class PageValue<T> {
    public final PageValueType<T> type;
    public final String title;
    public final T value;

    public PageValue(PageValueType<T> type, T value) {
        this.type = type;
        this.title = type.getTitle(value);
        this.value = value;
    }

    /** @return The translated "title" of an entry (a translation key after {@code buildcraft.guide.page.}). */
    public static String getTitle(JsonObject json) {
        String title = GsonHelper.getAsString(json, "title", "");
        return LocaleUtil.localize("buildcraft.guide.page." + title);
    }

    /** @param test An unknown object.
     * @return True if it matches {@link #value} */
    public boolean matches(Object test) {
        return type.matches(value, test);
    }

    @Nullable
    public ISimpleDrawable createDrawable() {
        return type.createDrawable(value);
    }

    /** @return A value to be added to {@link GuideManager#objectsAdded} so that
     *         {@link PageValueType#iterateAllDefault(IEntryLinkConsumer)} can ignore similar entries. */
    public Object getBasicValue() {
        return type.getBasicValue(value);
    }

    public List<String> getTooltip() {
        return type.getTooltip(value);
    }

    public PageValue<T> copyToValue() {
        return new PageValue<>(type, value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        return Objects.equals(value, ((PageValue<?>) obj).value);
    }
}
