/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.parts.contents;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import net.neoforged.fml.ModList;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.BCLibConfig;
import buildcraftreborn.lib.client.guide.GuiGuide;
import buildcraftreborn.lib.client.guide.GuideManager;
import buildcraftreborn.lib.client.guide.TypeOrder;
import buildcraftreborn.lib.client.guide.font.IFontRenderer;
import buildcraftreborn.lib.client.guide.loader.XmlPageLoader;
import buildcraftreborn.lib.client.guide.parts.GuideChapter;
import buildcraftreborn.lib.client.guide.parts.GuidePageBase;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.pos.GuiRectangle;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.lib.misc.search.ISuffixArray.SearchResult;

/** The contents of a book: its title and options, the mods it has pages from, then every page (sorted one of three
 * ways), with a search box. Should never be registered with any guide managers, this is special and controls them
 * all. */
public class GuidePageContents extends GuidePageBase {
    private static final int ORDER_OFFSET_X = -10;
    private static final int ORDER_OFFSET_Y = -10;

    private ContentsNodeGui contents;
    private final EditBox searchText;
    private String lastSearchText = "";
    /** -1 if all of the results can be displayed or the actual number of results if it's too many. */
    private int realResultCount = -1;

    public GuidePageContents(GuiGuide gui) {
        super(gui);
        loadMainGui();
        Minecraft mc = Minecraft.getInstance();
        searchText = new EditBox(mc.font, 0, 0, 80, mc.font.lineHeight + 5, Component.empty());
        searchText.setBordered(false);
        searchText.setTextColor(0xFF_00_00_00);
        setupChapters();
    }

    @Override
    public GuidePageBase createReloaded() {
        GuidePageContents newPage = new GuidePageContents(gui);
        newPage.searchText.setValue(searchText.getValue());
        newPage.searchText.setCursorPosition(searchText.getCursorPosition());
        newPage.searchText.setFocused(searchText.isFocused());
        newPage.numPages = numPages;
        newPage.goToPage(getIndex());
        return newPage;
    }

    public void loadMainGui() {
        contents = GuideManager.INSTANCE.getGuiContents(gui, gui.sortingOrder);
    }

    @Override
    public void setFontRenderer(IFontRenderer fontRenderer) {
        super.setFontRenderer(fontRenderer);
        contents.setFontRenderer(fontRenderer);
    }

    @Override
    public List<GuideChapter> getChapters() {
        return contents.getChapters();
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (lastSearchText.equals(searchText.getValue())) {
            if (numPages >= 3 && getPage() >= numPages) {
                goToPage(numPages);
            }
            return;
        }
        lastSearchText = searchText.getValue();
        numPages = -1;
        if (lastSearchText.isEmpty()) {
            realResultCount = -1;
            contents.node.resetVisibility();
            contents.invalidate();
            searchText.setTextColor(0xFF_00_00_00);
            setupChapters();
        } else {
            String text = lastSearchText.toLowerCase(Locale.ROOT);
            SearchResult<PageLink> ret = GuideManager.INSTANCE.quickSearcher.search(text,
                BCLibConfig.maxGuideSearchResults());
            realResultCount = ret.hasAllResults() ? -1 : ret.realResultCount;
            Set<PageLink> matches = new HashSet<>(ret.results);
            contents.node.setVisible(matches);
            contents.invalidate();
            searchText.setTextColor(contents.node.isVisible() ? 0xFF_00_00_00 : 0xFF_FF_00_00);
            if (getPage() < 2) {
                goToPage(2);
            }
            setupChapters();
        }
        gui.refreshChapters();
    }

    @Override
    protected void renderPage(int x, int y, int width, int height, int index) {
        IFontRenderer f = getFontRenderer();
        if (index == 0) {
            int xMiddle = x + width / 2;
            int _y = y;
            String text = gui.book == null ? "Everything" : gui.book.title.getString();
            float scale = 3f;
            for (String line : f.wrapString(text, width, false, scale)) {
                f.drawString(graphics(), line, xMiddle, _y, 0, false, true, scale);
                _y += (int) (f.getFontHeight(line) * scale);
            }
            String version = ModList.get().getModContainerById(BuildCraftReborn.MODID)
                .map(c -> c.getModInfo().getVersion().toString()).orElse("?");
            f.drawString(graphics(), "v" + version, xMiddle, _y, 0, false, true);
            _y = y + height - 80;
            f.drawString(graphics(), LocaleUtil.localize("options.title"), xMiddle, _y, 0, false, true, 2f);
            _y += 28;
            f.drawString(graphics(), "Show Lore " + (XmlPageLoader.SHOW_LORE ? "[x]" : "[ ]"), xMiddle, _y, 0, false,
                true);
            _y += 14;
            f.drawString(graphics(), "Show Hints " + (XmlPageLoader.SHOW_HINTS ? "[x]" : "[ ]"), xMiddle, _y, 0,
                false, true);
        } else if (index == 1) {
            int lines = gui.bookData.loadedMods.size() + 1;
            if (!gui.bookData.loadedOther.isEmpty()) {
                lines += 1 + gui.bookData.loadedOther.size();
            }
            int perLineHeight = f.getFontHeight("Ly") + 3;
            int _y = y + (height - lines * perLineHeight) / 2;
            if (!gui.bookData.loadedMods.isEmpty()) {
                drawCenteredText(ChatFormatting.BOLD + "Loaded Mods:", x, _y, width);
                _y += perLineHeight;
                for (String text : gui.bookData.loadedMods) {
                    drawCenteredText(text, x, _y, width);
                    _y += perLineHeight;
                }
            }
            if (!gui.bookData.loadedOther.isEmpty()) {
                drawCenteredText(ChatFormatting.BOLD + "Loaded Resource Packs:", x, _y, width);
                _y += perLineHeight;
                for (String text : gui.bookData.loadedOther) {
                    drawCenteredText(text, x, _y, width);
                    _y += perLineHeight;
                }
            }
        }
        if (index % 2 == 0) {
            searchText.setX(x + 23);
            searchText.setY(y - 16);
            if (!searchText.isFocused() && searchText.getValue().isEmpty()) {
                GuiGuide.SEARCH_TAB_CLOSED.drawAt(graphics(), x + 8, y - 20);
                GuiGuide.SEARCH_ICON.drawAt(graphics(), x + 8, y - 19);
            } else {
                GuiGuide.SEARCH_TAB_OPEN.drawAt(graphics(), x - 2, y - 22);
                GuiGuide.SEARCH_ICON.drawAt(graphics(), x + 8, y - 18);
            }
            searchText.render(graphics(), (int) gui.mouse.getX(), (int) gui.mouse.getY(), gui.getLastPartialTicks());
            if (realResultCount >= 0) {
                String text = LocaleUtil.localize("buildcraft.guide.too_many_results", realResultCount);
                getFontRenderer().drawString(graphics(), text, x + 105, y - 23, -1);
            }
        }
        PagePosition pos = contents.render(x, y, width, height, new PagePosition(2, 0), index);
        if (numPages == -1) {
            numPages = pos.page + 1;
        }
        super.renderPage(x, y, width, height, index);
        if (index != 0 && index % 2 == 0) {
            int oX = x + ORDER_OFFSET_X;
            int oY = y + ORDER_OFFSET_Y;
            for (int j = 0; j < GuiGuide.ORDERS.length; j++) {
                GuiIcon icon = GuiGuide.ORDERS[j];
                TypeOrder typeOrder = GuiGuide.SORTING_TYPES[j];
                if (gui.sortingOrder == typeOrder) {
                    icon = icon.offset(0, 14);
                }
                if (icon.containsGuiPos(oX, oY, gui.mouse)) {
                    icon = icon.offset(0, 28);
                    gui.tooltips.add(Collections.singletonList(LocaleUtil.localize(typeOrder.localeKey)));
                }
                icon.drawAt(graphics(), oX, oY);
                oY += 14;
            }
        }
    }

    private void drawCenteredText(String text, int x, int y, int width) {
        IFontRenderer f = getFontRenderer();
        f.drawString(graphics(), text, x + (width - f.getStringWidth(text)) / 2, y, 0);
    }

    @Override
    public void handleMouseClick(int x, int y, int width, int height, int mouseX, int mouseY, int mouseButton,
        int index) {
        super.handleMouseClick(x, y, width, height, mouseX, mouseY, mouseButton, index);
        if (index % 2 == 0) {
            int oX = x + ORDER_OFFSET_X;
            int oY = y + ORDER_OFFSET_Y;
            for (TypeOrder order : GuiGuide.SORTING_TYPES) {
                GuiRectangle rect = new GuiRectangle(oX, oY, 14, 14);
                if (index != 0 && rect.contains(gui.mouse)) {
                    gui.sortingOrder = order;
                    loadMainGui();
                    lastSearchText = "@@@@INVALID@@@";
                    gui.refreshChapters();
                    contents.setFontRenderer(getFontRenderer());
                    return;
                }
                oY += 14;
            }
            boolean inSearch = searchText.isMouseOver(mouseX, mouseY);
            if (inSearch) {
                searchText.mouseClicked(mouseX, mouseY, mouseButton);
                searchText.setFocused(true);
                if (mouseButton == 1) {
                    searchText.setValue("");
                }
            } else if (new GuiRectangle(x - 2, y - 34, 40, 34).contains(mouseX, mouseY)) {
                searchText.setFocused(true);
            } else {
                searchText.setFocused(false);
            }
        }
        if (mouseButton == 0 && index == 0) {
            IFontRenderer f = getFontRenderer();
            String text = XmlPageLoader.SHOW_LORE ? "Show Lore [x]" : "Show Lore [ ]";
            int fWidth = f.getStringWidth(text);
            GuiRectangle rect =
                new GuiRectangle(x + (width - fWidth) / 2.0, y + height - 52, fWidth, f.getFontHeight(text));
            if (rect.contains(mouseX, mouseY)) {
                XmlPageLoader.SHOW_LORE = !XmlPageLoader.SHOW_LORE;
            }
            text = XmlPageLoader.SHOW_HINTS ? "Show Hints [x]" : "Show Hints [ ]";
            fWidth = f.getStringWidth(text);
            rect = new GuiRectangle(x + (width - fWidth) / 2.0, y + height - 38, fWidth, f.getFontHeight(text));
            if (rect.contains(mouseX, mouseY)) {
                XmlPageLoader.SHOW_HINTS = !XmlPageLoader.SHOW_HINTS;
            }
        }
        if (new GuiRectangle(x, y, width, height).contains(mouseX, mouseY)) {
            contents.onClicked(x, y, width, height, new PagePosition(2, 0), index);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return searchText.isFocused() && (searchText.keyPressed(keyCode, scanCode, modifiers)
            || searchText.canConsumeInput());
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return searchText.isFocused() && searchText.charTyped(codePoint, modifiers);
    }
}
