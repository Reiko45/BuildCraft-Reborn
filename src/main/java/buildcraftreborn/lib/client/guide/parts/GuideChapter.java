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
import buildcraftreborn.lib.client.guide.PageLine;
import buildcraftreborn.lib.client.guide.font.IFontRenderer;
import buildcraftreborn.lib.client.sprite.SpriteNineSliced;
import buildcraftreborn.lib.gui.pos.GuiRectangle;
import buildcraftreborn.lib.misc.GuiUtil;

/** A chapter heading on a page, and its bookmark at the side of the book (click it to go to the chapter). Chapters
 * nest by level. */
public abstract class GuideChapter extends GuidePart {
    public static final int[] COLOURS = { 0x9dd5c0, 0xfac174, 0x27a4dd };
    public static final int MAX_HOVER_PROGRESS = 5;
    public static final int MAX_HOVER_DISTANCE = 20;

    public final PageLine chapter;
    /** 0 is the minimum value, and corresponds to the top-level of chapter. */
    public final int level;
    private int hoverProgress = 0, hoverProgressLast = 0;
    protected EnumGuiSide lastDrawn = null;
    private boolean expanded = false;
    @Nullable
    protected GuideChapter parent;
    protected final List<GuideChapter> children = new ArrayList<>();
    protected int colourIndex = -1;

    public enum EnumGuiSide {
        LEFT,
        RIGHT
    }

    public GuideChapter(GuiGuide gui, String chapter) {
        this(gui, 0, chapter);
    }

    public GuideChapter(GuiGuide gui, int level, String text) {
        super(gui);
        this.level = Math.max(0, level);
        // (1.12 made chapter box icons, then didn't use them)
        this.chapter = new PageLine(null, null, this.level + 1, text, false);
    }

    private int getColour() {
        if (colourIndex < 0) {
            return chapter.text.hashCode();
        }
        return COLOURS[colourIndex % COLOURS.length];
    }

    public void reset() {
        lastDrawn = EnumGuiSide.LEFT;
    }

    public boolean hasParent() {
        return parent != null;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    void assignChildIndices() {
        int cIdx = 0;
        for (GuideChapter child : children) {
            if (cIdx % COLOURS.length == colourIndex) {
                cIdx++;
            }
            child.colourIndex = cIdx++ % COLOURS.length;
            if (child.hasChildren()) {
                child.assignChildIndices();
            }
        }
    }

    @Override
    public PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index) {
        current = current.guaranteeSpace(getFontRenderer().getMaxFontHeight() * 4, height);
        if (current.page == index) {
            int _x = x + 12;
            int _y = y + current.pixel;
            PagePosition n2 = renderLine(current, chapter, x, y, width, height, -1);
            int _height = n2.pixel - current.pixel;
            GuiUtil.setColour(graphics(), getColour());
            GuiGuide.CHAPTER_MARKER_9.draw(graphics(), _x - 5, _y - 4, width - 24, _height);
            GuiUtil.resetColour(graphics());
        }
        PagePosition n = renderLine(current, chapter, x, y, width, height, index);
        int spread = index / 2;
        if (n.page / 2 < spread) {
            lastDrawn = EnumGuiSide.LEFT;
        } else if (spread == current.page / 2) {
            lastDrawn = EnumGuiSide.LEFT;
        }
        if (lastDrawn != null && parent != null) {
            GuideChapter p = parent;
            while (p != null) {
                if (p.lastDrawn == null) {
                    p.lastDrawn = lastDrawn;
                }
                p = p.parent;
            }
        }
        return n;
    }

    @Override
    public PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index,
        int mouseX, int mouseY) {
        current = current.guaranteeSpace(getFontRenderer().getMaxFontHeight() * 4, height);
        return renderLine(current, chapter, x, y, width, height, -1);
    }

    /** Draws this chapter's bookmark (and its children's, if expanded).
     * @return The number of bookmarks drawn. */
    public int draw(int yIndex, float partialTicks, boolean drawCentral) {
        int drawnCount = 1;
        IFontRenderer font = gui.getCurrentFont();
        String text = chapter.text;
        float hoverWidth = getHoverWidth(partialTicks);
        float width = font.getStringWidth(text) + hoverWidth;
        int colour = getColour();
        int baseY = drawCentral ? ((int) GuiGuide.FLOATING_CHAPTER_MENU.getY() + 6) : gui.minY;
        int y = baseY + (font.getMaxFontHeight() + 8) * (yIndex + 1);
        boolean hasChildren = !children.isEmpty();
        float _width = font.getStringWidth(text) + 12 + hoverWidth + (hasChildren ? 16 : 0);
        int fullHeight = font.getFontHeight(text) + 6;
        int childHeight = 0;
        if (hasChildren && expanded) {
            childHeight = fullHeight + getChildrenFullHeight();
        }
        if (drawCentral || lastDrawn == EnumGuiSide.RIGHT) {
            float x;
            if (drawCentral) {
                x = (float) GuiGuide.FLOATING_CHAPTER_MENU.getX() + 4 + hoverWidth;
                _width -= hoverWidth;
                hoverWidth = 0;
            } else {
                x = gui.minX + GuiGuide.PAGE_LEFT.width + GuiGuide.PAGE_RIGHT.width - 11;
            }
            x += level * 14;
            GuideChapter p = parent;
            while (p != null) {
                x += p.getHoverWidth(partialTicks);
                p = p.parent;
            }
            GuiUtil.setColour(graphics(), colour);
            SpriteNineSliced icon = drawCentral ? GuiGuide.CHAPTER_MARKER_9 : GuiGuide.CHAPTER_MARKER_9_RIGHT;
            if (childHeight > 0) {
                icon.draw(graphics(), x + 10, y + fullHeight - 12, _width - 16, childHeight);
            }
            icon.draw(graphics(), x, y - 4, _width, fullHeight);
            if (hasChildren) {
                (expanded ? GuiGuide.EXPANDED_ARROW : GuiGuide.CLOSED_ARROW).drawAt(graphics(), x + hoverWidth, y - 4);
                x += 16;
                if (expanded) {
                    for (GuideChapter child : children) {
                        EnumGuiSide old = child.lastDrawn;
                        child.lastDrawn = lastDrawn;
                        drawnCount += child.draw(yIndex + drawnCount, partialTicks, drawCentral);
                        child.lastDrawn = old;
                    }
                }
            }
            GuiUtil.resetColour(graphics());
            font.drawString(graphics(), text, (int) (x + 6 + hoverWidth), y, 0);
        } else if (lastDrawn == EnumGuiSide.LEFT) {
            float x = gui.minX - width + 5;
            if (hasChildren) {
                x -= 16;
            }
            GuiUtil.setColour(graphics(), colour);
            if (childHeight > 0) {
                GuiGuide.CHAPTER_MARKER_9_LEFT.draw(graphics(), x + 10, y + fullHeight - 12, _width - 16,
                    childHeight);
            }
            GuiGuide.CHAPTER_MARKER_9_LEFT.draw(graphics(), x - 6, y - 4, _width, fullHeight);
            if (hasChildren) {
                (expanded ? GuiGuide.EXPANDED_ARROW : GuiGuide.CLOSED_ARROW).drawAt(graphics(), x - 6, y - 4);
                x += 16;
                if (expanded) {
                    for (GuideChapter child : children) {
                        EnumGuiSide old = child.lastDrawn;
                        child.lastDrawn = lastDrawn;
                        drawnCount += child.draw(yIndex + drawnCount, partialTicks, drawCentral);
                        child.lastDrawn = old;
                    }
                }
            }
            GuiUtil.resetColour(graphics());
            font.drawString(graphics(), text, (int) x, y, 0);
        }
        return drawnCount;
    }

    private int getChildrenFullHeight() {
        if (!expanded) {
            return 0;
        }
        int fullHeight = 0;
        IFontRenderer font = gui.getCurrentFont();
        for (GuideChapter c : children) {
            fullHeight += font.getFontHeight(c.chapter.text) + 6;
            fullHeight += c.getChildrenFullHeight();
            fullHeight += 2;
        }
        return fullHeight;
    }

    /** @return 0 for not hovered, 1 for the main chapter, or 2 for the arrow (if present). */
    protected int getMousePart() {
        IFontRenderer font = gui.getCurrentFont();
        String text = chapter.text;
        float hoverWidth = getHoverWidth(0);
        final float realHoverWidth = hoverWidth;
        int width = (int) (font.getStringWidth(text) + hoverWidth) + (children.isEmpty() ? 0 : 16);
        int chapterIndex = 0;
        GuideChapter p = parent;
        while (p != null) {
            if (!p.expanded) {
                return 0;
            }
            p = p.parent;
        }
        for (GuideChapter c : gui.getChapters()) {
            chapterIndex++;
            if (c == this) {
                break;
            }
            p = c.parent;
            while (p != null) {
                if (!p.expanded) {
                    chapterIndex--;
                    break;
                }
                p = p.parent;
            }
        }
        boolean isCentral = gui.isSmallScreen();
        int baseY = isCentral ? ((int) GuiGuide.FLOATING_CHAPTER_MENU.getY() + 6) : gui.minY;
        int y = baseY + (font.getMaxFontHeight() + 8) * chapterIndex;
        if (isCentral || lastDrawn == EnumGuiSide.RIGHT) {
            int x;
            if (isCentral) {
                x = (int) GuiGuide.FLOATING_CHAPTER_MENU.getX() + 4 + (int) hoverWidth;
                width -= hoverWidth;
            } else {
                x = gui.minX + GuiGuide.PAGE_LEFT.width + GuiGuide.PAGE_RIGHT.width - 11;
            }
            x += level * 14;
            p = parent;
            while (p != null) {
                x += p.getHoverWidth(0);
                p = p.parent;
            }
            GuiRectangle drawRect = new GuiRectangle(x - realHoverWidth, y - 4, width + 16, 16);
            if (drawRect.contains(gui.mouse)) {
                GuiRectangle arrowRect = new GuiRectangle(x - realHoverWidth, y - 4, 24 + realHoverWidth, 16);
                if (hasChildren() && arrowRect.contains(gui.mouse)) {
                    return 2;
                }
                return 1;
            }
        } else if (lastDrawn == EnumGuiSide.LEFT) {
            int x = gui.minX - width - 5;
            GuiRectangle drawRect = new GuiRectangle(x, y - 4, width + 16, 16);
            if (drawRect.contains(gui.mouse)) {
                if (hasChildren() && new GuiRectangle(x, y - 4, 24, 16).contains(gui.mouse)) {
                    return 2;
                }
                return 1;
            }
        }
        return 0;
    }

    private float getHoverWidth(float partialTicks) {
        float prog = partialTicks * hoverProgress + (1 - partialTicks) * hoverProgressLast;
        return (prog * MAX_HOVER_DISTANCE) / MAX_HOVER_PROGRESS;
    }

    /** @return 0 if not clicked, 1 if the chapter was clicked (and went somewhere), or 2 if it was expanded or
     *         collapsed. */
    public int handleClick() {
        int part = getMousePart();
        if (part == 1) {
            return onClick() ? 1 : 0;
        } else if (part == 2) {
            expanded = !expanded;
            return 2;
        }
        return 0;
    }

    protected abstract boolean onClick();

    @Override
    public void updateScreen() {
        hoverProgressLast = hoverProgress;
        if (getMousePart() != 0) {
            hoverProgress = Math.min(MAX_HOVER_PROGRESS, hoverProgress + 2);
        } else {
            hoverProgress = Math.max(0, hoverProgress - 1);
        }
    }
}
