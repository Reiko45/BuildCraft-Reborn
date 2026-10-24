/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import net.minecraft.resources.ResourceLocation;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.client.guide.entry.PageEntry;
import buildcraftreborn.lib.client.guide.parts.GuidePageFactory;

/** Loads a guide page ({@code .md}): turns markdown headings ({@code #}, {@code ##}...) and 1.12's old
 * {@code $[special.*]} lines into {@link XmlPageLoader} tags, then loads that. */
public enum MarkdownPageLoader {
    INSTANCE;

    public GuidePageFactory loadPage(BufferedReader reader, ResourceLocation name, PageEntry<?> entry)
        throws IOException {
        StringBuilder replaced = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            // First replace special tags (at the start of a line) with xml ones
            replaced.append(replaceSpecialForXml(line)).append('\n');
        }
        return XmlPageLoader.INSTANCE.loadPage(new BufferedReader(new StringReader(replaced.toString())), name,
            entry);
    }

    private static String replaceSpecialForXml(String line) {
        if (line.startsWith("$[special.") && line.indexOf(']') > 0) {
            int end = line.indexOf(']');
            String post = line.substring("$[special.".length(), end);
            switch (post) {
                case "new_page" -> {
                    BuildCraftReborn.LOGGER.warn("[lib.guide.markdown] Found deprecated element '{}', it should be "
                        + "replaced with '<new_page/>'", line);
                    return "<new_page/>";
                }
                case "all_crafting" -> {
                    String stack = line.substring(end + 1);
                    if (stack.startsWith("\"") && stack.endsWith("\"")) {
                        stack = stack.substring(1, stack.length() - 1);
                    }
                    if (stack.startsWith("(") && stack.endsWith(")")) {
                        stack = stack.substring(1, stack.length() - 1);
                    } else if (stack.startsWith("{") && stack.contains("}")) {
                        stack = stack.substring(1, stack.indexOf('}')).split(",")[0];
                    }
                    String str = "<recipes_usages stack=\"" + stack.trim() + "\"/>";
                    BuildCraftReborn.LOGGER.warn("[lib.guide.markdown] Found deprecated element '{}', it should be "
                        + "replaced with '{}'", line, str);
                    return str;
                }
                default -> {}
            }
        } else if (line.startsWith("#")) {
            int level = -1;
            while (line.startsWith("#")) {
                line = line.substring(1);
                level++;
            }
            line = line.trim();
            if (level == 0) {
                return "<chapter name=\"" + line + "\"/>";
            }
            return "<chapter name=\"" + line + "\" level=\"" + level + "\"/>";
        }
        return line;
    }
}
