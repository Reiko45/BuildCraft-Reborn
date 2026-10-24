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

import net.minecraft.resources.ResourceLocation;

import buildcraftreborn.lib.client.guide.GuiGuide;
import buildcraftreborn.lib.client.guide.GuideManager;
import buildcraftreborn.lib.client.guide.parts.GuidePart;
import buildcraftreborn.lib.client.guide.parts.contents.PageLink;
import buildcraftreborn.lib.gui.ISimpleDrawable;
import buildcraftreborn.lib.misc.data.OptionallyDisabled;

/** A kind of thing that the guide has pages about. */
public abstract class PageValueType<T> {
    public abstract OptionallyDisabled<PageEntry<T>> deserialize(ResourceLocation name, JsonObject json);

    public abstract Class<T> getEntryClass();

    public boolean matches(T value, Object test) {
        return Objects.equals(test, value);
    }

    @Nullable
    public abstract ISimpleDrawable createDrawable(T value);

    /** @return A value to be added to {@link GuideManager#objectsAdded} so that
     *         {@link #iterateAllDefault(IEntryLinkConsumer)} can ignore similar entries. */
    public Object getBasicValue(T value) {
        return value;
    }

    public abstract String getTitle(T value);

    public abstract List<String> getTooltip(T value);

    /** Adds a link for every value of this type that doesn't have a page (to the "Others" part of the contents). */
    public abstract void iterateAllDefault(IEntryLinkConsumer consumer);

    /** @param to Something that identifies what this should link to.
     * @return Either the {@link PageLink}, or the error for why the given "to" doesn't result in a valid link. */
    public OptionallyDisabled<PageLink> createLink(String to) {
        return new OptionallyDisabled<>(getClass().getSimpleName() + " doesn't support links");
    }

    @Nullable
    public final PageValue<T> wrap(Object value) {
        T typed = getEntryClass().cast(value);
        if (isValid(typed)) {
            return new PageValue<>(this, typed);
        }
        return null;
    }

    protected boolean isValid(T typed) {
        return true;
    }

    /** Adds anything to the end of a page that every page of this type has (such as an item's recipes). */
    public void addPageEntries(T value, GuiGuide gui, List<GuidePart> parts) {}
}
