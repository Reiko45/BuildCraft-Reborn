/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.parts;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import buildcraftreborn.lib.client.guide.GuiGuide;
import buildcraftreborn.lib.client.guide.GuideManager;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.pos.GuiRectangle;

/** A page of the guide (really a spread of pages, two at a time), with page turning and numbers. */
public abstract class GuidePageBase extends GuidePart {
    /** The current page that is being rendered */
    private int index = 0;
    protected int numPages = -1;

    public GuidePageBase(GuiGuide gui) {
        super(gui);
    }

    protected void setupChapters() {
        List<GuideChapter> lastChapterAtLevel = new ArrayList<>();
        List<GuideChapter> chapters = getChapters();
        for (GuideChapter chapter : chapters) {
            chapter.parent = null;
            chapter.children.clear();
        }
        for (GuideChapter chapter : chapters) {
            int gap = chapter.level - lastChapterAtLevel.size();
            if (gap < 0) {
                // There's some previous children that we need to clean up
                lastChapterAtLevel.subList(chapter.level, lastChapterAtLevel.size()).clear();
            }
            for (int g = Math.min(chapter.level, lastChapterAtLevel.size()) - 1; g >= 0; g--) {
                GuideChapter parent = lastChapterAtLevel.get(g);
                if (parent != null) {
                    parent.children.add(chapter);
                    chapter.parent = parent;
                    break;
                }
            }
            for (int g = 1; g < gap; g++) {
                lastChapterAtLevel.add(null);
            }
            lastChapterAtLevel.add(chapter);
        }
        int idx = 0;
        for (GuideChapter c : chapters) {
            if (c.hasParent()) {
                continue;
            }
            c.colourIndex = idx++ % GuideChapter.COLOURS.length;
            if (c.hasChildren()) {
                c.assignChildIndices();
            }
        }
    }

    protected final int getIndex() {
        return index;
    }

    public final void nextPage() {
        if (index + 2 < numPages) {
            index += 2;
        }
    }

    public final void lastPage() {
        index -= 2;
        if (index < 0) {
            index = 0;
        }
    }

    protected final void goToPage(int page) {
        if (numPages > 0 && page >= numPages) {
            page = numPages - 1;
        }
        // Make it a multiple of 2
        index = Math.max(0, page / 2 * 2);
    }

    public int getPage() {
        return index;
    }

    public int getPageCount() {
        return numPages;
    }

    public void tick() {}

    @Override
    public final PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index) {
        // NO-OP
        return current;
    }

    @Nullable
    public abstract String getTitle();

    public boolean shouldPersistHistory() {
        return true;
    }

    /** Called when the {@link GuideManager} is reloaded.
     *
     * @return A page that can be shown and is valid after the reload, or null if this page cannot continue through a
     *         reload. */
    @Nullable
    public GuidePageBase createReloaded() {
        return null;
    }

    public abstract List<GuideChapter> getChapters();

    public void renderFirstPage(int x, int y, int width, int height) {
        renderPage(x, y, width, height, index);
    }

    public void renderSecondPage(int x, int y, int width, int height) {
        renderPage(x, y, width, height, index + 1);
    }

    protected void renderPage(int x, int y, int width, int height, int index) {
        if (index % 2 == 0) {
            // Even => first page, draw page back button and first page index
            if (index != 0) {
                GuiIcon icon = GuiGuide.TURN_BACK;
                GuiRectangle turnBox = new GuiRectangle(x - 30, y + height, icon.width + 30, icon.height + 30);
                if (turnBox.contains(gui.mouse)) {
                    icon = GuiGuide.TURN_BACK_HOVERED;
                }
                icon.drawAt(graphics(), turnBox.offset(30, 0));
            }
            String text = (index + 1) + " / " + numPages;
            double textX = x + GuiGuide.PAGE_LEFT_TEXT.width / 2 - getFontRenderer().getStringWidth(text) / 2.0;
            getFontRenderer().drawString(graphics(), text, (int) textX, y + height + 6, 0x90816a);
        } else {
            // Odd => second page, draw forward button and second page index
            if (index + 1 < numPages) {
                GuiIcon icon = GuiGuide.TURN_FORWARDS;
                GuiRectangle turnBox =
                    new GuiRectangle(x + width - icon.width, y + height, icon.width + 30, icon.height + 30);
                if (turnBox.contains(gui.mouse)) {
                    icon = GuiGuide.TURN_FORWARDS_HOVERED;
                }
                icon.drawAt(graphics(), turnBox);
            }
            if (index + 1 <= numPages) {
                String text = (index + 1) + " / " + numPages;
                double textX = x + (GuiGuide.PAGE_RIGHT_TEXT.width - getFontRenderer().getStringWidth(text)) / 2;
                getFontRenderer().drawString(graphics(), text, (int) textX, y + height + 6, 0x90816a);
            }
        }
    }

    @Override
    public final PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index,
        int mouseX, int mouseY) {
        // NO-OP, use the below!
        return current;
    }

    public void handleMouseClick(int x, int y, int width, int height, int mouseX, int mouseY, int mouseButton,
        int index) {
        if (index % 2 == 0) {
            // Even => first page, test page back button
            if (index != 0) {
                GuiIcon icon = GuiGuide.TURN_BACK;
                GuiRectangle turnBox = new GuiRectangle(x - 30, y + height, icon.width + 30, icon.height + 30);
                if (turnBox.contains(gui.mouse)) {
                    lastPage();
                }
            }
        } else if (index + 1 < numPages) {
            // Odd => second page, test forward page button
            GuiIcon icon = GuiGuide.TURN_FORWARDS;
            GuiRectangle turnBox =
                new GuiRectangle(x + width - icon.width, y + height, icon.width + 30, icon.height + 30);
            if (turnBox.contains(gui.mouse)) {
                nextPage();
            }
        }
    }

    /** @return True if the key was used. */
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    /** @return True if the character was used. */
    public boolean charTyped(char codePoint, int modifiers) {
        return false;
    }
}
