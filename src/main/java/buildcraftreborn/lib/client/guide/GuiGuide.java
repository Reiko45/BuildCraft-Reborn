/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.core.BCCoreItems;
import buildcraftreborn.lib.client.ToastInformation;
import buildcraftreborn.lib.client.guide.font.FontManager;
import buildcraftreborn.lib.client.guide.font.IFontRenderer;
import buildcraftreborn.lib.client.guide.parts.GuideChapter;
import buildcraftreborn.lib.client.guide.parts.GuidePageBase;
import buildcraftreborn.lib.client.guide.parts.contents.GuidePageContents;
import buildcraftreborn.lib.client.sprite.SpriteNineSliced;
import buildcraftreborn.lib.client.sprite.SpriteRaw;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.GuiStack;
import buildcraftreborn.lib.gui.ISimpleDrawable;
import buildcraftreborn.lib.gui.pos.GuiRectangle;
import buildcraftreborn.lib.gui.pos.IGuiArea;
import buildcraftreborn.lib.gui.pos.MousePosition;
import buildcraftreborn.lib.misc.GuiUtil;
import buildcraftreborn.lib.misc.LocaleUtil;

/** The guide book. Click the cover to open it; then the contents page lists every page (sorted by type, by mod or
 * alphabetically, and searchable), the bookmarks at the side go to each chapter of a page, and the arrows (or the
 * left and right movement keys) turn the pages. F3+T reloads the pages.
 * <p>
 * 1.21: a {@link Screen}; the parts draw with {@link #graphics}, the current frame's {@link GuiGraphics}. */
public class GuiGuide extends Screen {
    public static final ResourceLocation ICONS_2 = BuildCraftReborn.id("textures/gui/guide/icons.png");
    public static final ResourceLocation COVER = BuildCraftReborn.id("textures/gui/guide/cover.png");
    public static final ResourceLocation LEFT_PAGE = BuildCraftReborn.id("textures/gui/guide/left_page.png");
    public static final ResourceLocation RIGHT_PAGE = BuildCraftReborn.id("textures/gui/guide/right_page.png");
    public static final ResourceLocation LEFT_PAGE_BACK = BuildCraftReborn.id("textures/gui/guide/left_page_back.png");
    public static final ResourceLocation RIGHT_PAGE_BACK =
        BuildCraftReborn.id("textures/gui/guide/right_page_back.png");
    public static final ResourceLocation LEFT_PAGE_FIRST =
        BuildCraftReborn.id("textures/gui/guide/left_page_first.png");
    public static final ResourceLocation RIGHT_PAGE_LAST =
        BuildCraftReborn.id("textures/gui/guide/right_page_last.png");

    public static final GuiIcon BOOK_COVER = new GuiIcon(COVER, 0, 0, 202, 248);
    public static final GuiIcon BOOK_BINDING = new GuiIcon(COVER, 204, 0, 11, 248);

    public static final GuiIcon PAGE_LEFT = new GuiIcon(LEFT_PAGE, 0, 0, 193, 248);
    public static final GuiIcon PAGE_RIGHT = new GuiIcon(RIGHT_PAGE, 0, 0, 193, 248);

    public static final GuiIcon PAGE_LEFT_BACK = new GuiIcon(LEFT_PAGE_BACK, 0, 0, 193, 248);
    public static final GuiIcon PAGE_RIGHT_BACK = new GuiIcon(RIGHT_PAGE_BACK, 0, 0, 193, 248);

    public static final GuiIcon PAGE_LEFT_FIRST = new GuiIcon(LEFT_PAGE_FIRST, 0, 0, 193, 248);
    public static final GuiIcon PAGE_RIGHT_LAST = new GuiIcon(RIGHT_PAGE_LAST, 0, 0, 193, 248);

    public static final int PAGE_WIDTH = 168;
    public static final int PAGE_HEIGHT = 190;

    public static final GuiRectangle PAGE_LEFT_TEXT = new GuiRectangle(23, 25, PAGE_WIDTH, PAGE_HEIGHT);
    public static final GuiRectangle PAGE_RIGHT_TEXT = new GuiRectangle(4, 25, PAGE_WIDTH, PAGE_HEIGHT);

    public static final GuiIcon TURN_BACK = new GuiIcon(ICONS_2, 23, 139, 18, 10);
    public static final GuiIcon TURN_BACK_HOVERED = new GuiIcon(ICONS_2, 23, 152, 18, 10);

    public static final GuiIcon TURN_FORWARDS = new GuiIcon(ICONS_2, 0, 139, 18, 10);
    public static final GuiIcon TURN_FORWARDS_HOVERED = new GuiIcon(ICONS_2, 0, 152, 18, 10);

    public static final GuiIcon BACK = new GuiIcon(ICONS_2, 48, 139, 17, 9);
    public static final GuiIcon BACK_HOVERED = new GuiIcon(ICONS_2, 48, 152, 17, 9);

    public static final SpriteRaw BOX_CODE_SPRITE = new SpriteRaw(ICONS_2, 80, 164, 16, 16, 256);
    public static final SpriteNineSliced BOX_CODE_SLICED = new SpriteNineSliced(BOX_CODE_SPRITE, 4, 4, 12, 12, 16);

    public static final GuiIcon BORDER_TOP_LEFT = new GuiIcon(ICONS_2, 0, 196, 13, 13);
    public static final GuiIcon BORDER_TOP_RIGHT = new GuiIcon(ICONS_2, 13, 196, 13, 13);
    public static final GuiIcon BORDER_BOTTOM_LEFT = new GuiIcon(ICONS_2, 0, 209, 13, 13);
    public static final GuiIcon BORDER_BOTTOM_RIGHT = new GuiIcon(ICONS_2, 13, 209, 13, 13);

    public static final GuiIcon ORDER_TYPE = new GuiIcon(ICONS_2, 0, 0, 14, 14);
    public static final GuiIcon ORDER_MOD_TYPE = new GuiIcon(ICONS_2, 14, 0, 14, 14);
    public static final GuiIcon ORDER_ALPHABETICAL = new GuiIcon(ICONS_2, 28, 0, 14, 14);

    public static final GuiIcon EXPANDED_ARROW = new GuiIcon(ICONS_2, 96, 164, 16, 16);
    public static final GuiIcon CLOSED_ARROW = new GuiIcon(ICONS_2, 96, 180, 16, 16);

    public static final GuiIcon CHAPTER_MARKER = new GuiIcon(ICONS_2, 0, 56, 32, 32);
    public static final GuiIcon CHAPTER_MARKER_LEFT = new GuiIcon(ICONS_2, 0, 56, 24, 32);
    public static final GuiIcon CHAPTER_MARKER_RIGHT = new GuiIcon(ICONS_2, 8, 56, 24, 32);

    public static final SpriteNineSliced CHAPTER_MARKER_9 =
        new SpriteNineSliced(CHAPTER_MARKER.sprite, 8, 8, 24, 24, 32);
    public static final SpriteNineSliced CHAPTER_MARKER_9_LEFT =
        new SpriteNineSliced(CHAPTER_MARKER_LEFT.sprite, 8, 8, 24, 24, 24, 32);
    public static final SpriteNineSliced CHAPTER_MARKER_9_RIGHT =
        new SpriteNineSliced(CHAPTER_MARKER_RIGHT.sprite, 0, 8, 16, 24, 24, 32);

    public static final GuiIcon SEARCH_ICON = new GuiIcon(ICONS_2, 26, 196, 12, 12);
    public static final GuiIcon SEARCH_TAB_CLOSED = new GuiIcon(ICONS_2, 58, 196, 14, 6);
    public static final GuiIcon SEARCH_TAB_OPEN = new GuiIcon(ICONS_2, 40, 209, 106, 14);

    public static final GuiIcon[] ORDERS = { ORDER_TYPE, ORDER_MOD_TYPE, ORDER_ALPHABETICAL };

    public static final GuiRectangle BACK_POSITION = new GuiRectangle(PAGE_LEFT.width - BACK.width / 2.0,
        PAGE_LEFT.height - BACK.height - 2, BACK.width, BACK.height);

    public static final TypeOrder[] SORTING_TYPES = {
        new TypeOrder("buildcraft.guide.order.type_subtype", ETypeTag.TYPE, ETypeTag.SUB_TYPE),
        new TypeOrder("buildcraft.guide.order.mod_type", ETypeTag.MOD, ETypeTag.TYPE),
        new TypeOrder("buildcraft.guide.order.alphabetical") };

    public static final IGuiArea FLOATING_CHAPTER_MENU = GuiUtil.moveRectangleToCentre(
        new GuiRectangle((PAGE_LEFT_TEXT.width + PAGE_RIGHT_TEXT.width) / 2, PAGE_LEFT.height - 20));

    private static final float BOOK_OPEN_TIME = 10f;

    public final MousePosition mouse = new MousePosition();

    @Nullable
    public final GuideBook book;
    public final GuideContentsData bookData;

    public TypeOrder sortingOrder = SORTING_TYPES[0];
    private boolean isOpen = false;
    private boolean isOpening = false;
    private boolean showingContentsMenu = false;

    /** Float between -90 and 90 */
    private float openingAngleLast = -90, openingAngleNext = -90;

    public int minX, minY;
    @Nullable
    public ItemStack tooltipStack = null;
    public final List<List<String>> tooltips = new ArrayList<>();
    /** What the parts draw with (while the screen renders). */
    public GuiGraphics graphics;

    private final Deque<GuidePageBase> pages = new ArrayDeque<>();
    private final List<GuideChapter> chapters = new ArrayList<>();
    private GuidePageBase currentPage;
    private final IFontRenderer currentFont = FontManager.INSTANCE.getOrLoadFont("SansSerif", 9);
    private float lastPartialTicks;

    public GuiGuide() {
        this((GuideBook) null);
    }

    public GuiGuide(@Nullable String bookName) {
        this(bookName == null ? null : GuideBookRegistry.INSTANCE.getBook(bookName));
    }

    private GuiGuide(@Nullable GuideBook book) {
        super(book == null ? Component.translatable("buildcraft.guide.everything") : book.title);
        this.book = book;
        this.bookData = book != null ? book.data : GuideManager.BOOK_ALL_DATA;
        GuideManager.INSTANCE.ensureLoaded();
        openPage(new GuidePageContents(this));
    }

    public void openPage(GuidePageBase page) {
        if (currentPage != null && currentPage.shouldPersistHistory()) {
            pages.push(currentPage);
        }
        setPageInternal(page);
    }

    public void closePage() {
        if (pages.isEmpty()) {
            onClose();
        } else {
            setPageInternal(pages.pop());
        }
    }

    public void goBackToMenu() {
        GuidePageBase newPage = currentPage;
        while (!pages.isEmpty()) {
            newPage = pages.pop();
        }
        setPageInternal(newPage);
    }

    private void setPageInternal(GuidePageBase page) {
        currentPage = page;
        refreshChapters();
    }

    public GuidePageBase getCurrentPage() {
        return currentPage;
    }

    public IFontRenderer getCurrentFont() {
        return this.currentFont;
    }

    public List<GuideChapter> getChapters() {
        return chapters;
    }

    public void refreshChapters() {
        chapters.clear();
        chapters.addAll(currentPage.getChapters());
    }

    @Override
    public void tick() {
        super.tick();
        if (isOpen) {
            currentPage.updateScreen();
            for (GuideChapter chapter : chapters) {
                chapter.updateScreen();
            }
        } else if (isOpening) {
            openingAngleLast = openingAngleNext;
            openingAngleNext += 180 / BOOK_OPEN_TIME;
        }
        if (currentPage != null) {
            setupFontRenderer();
            currentPage.tick();
        }
    }

    public boolean isSmallScreen() {
        return width < 590;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.graphics = graphics;
        lastPartialTicks = partialTicks;
        minX = (width - PAGE_LEFT.width * 2) / 2;
        minY = (height - BOOK_COVER.height) / 2;
        mouse.setMousePosition(mouseX, mouseY);
        if (isOpen) {
            drawOpen(partialTicks);
        } else if (isOpening) {
            drawOpening(partialTicks);
        } else {
            drawCover();
        }
    }

    public float getLastPartialTicks() {
        return this.lastPartialTicks;
    }

    private void drawCover() {
        minX = (width - BOOK_COVER.width) / 2;
        minY = (height - BOOK_COVER.height) / 2;
        BOOK_COVER.drawAt(graphics, minX, minY);
    }

    private void drawOpening(float partialTicks) {
        minX = (width - BOOK_COVER.width) / 2;
        minY = (height - BOOK_COVER.height) / 2;

        float openingAngle = openingAngleLast * (1 - partialTicks) + openingAngleNext * partialTicks;
        float sin = Math.abs(Mth.sin((float) (openingAngle * Math.PI / 180)));
        if (openingAngle >= 90) {
            isOpen = true;
        }
        if (openingAngle < 0) {
            int coverWidth = (int) (sin * BOOK_COVER.width);
            sin = 1 - sin;
            float offset = sin * 50;
            int bindingWidth = (int) (sin * BOOK_BINDING.width);

            PAGE_RIGHT.drawAt(graphics, minX + BOOK_COVER.width - PAGE_RIGHT.width, minY);
            GuiUtil.drawCustomQuad(graphics, BOOK_COVER.sprite, minX, minY + BOOK_COVER.height, minX + coverWidth,
                minY + BOOK_COVER.height + offset, minX + coverWidth, minY - offset, minX, minY);
            BOOK_BINDING.drawScaledInside(graphics, (int) (minX + coverWidth - bindingWidth * 0.5),
                (int) (minY - offset), bindingWidth, (int) (BOOK_BINDING.height + offset * 2));
        } else if (openingAngle == 0) {
            PAGE_RIGHT.drawAt(graphics, minX + BOOK_COVER.width - PAGE_LEFT.width, minY);
            BOOK_COVER.drawAt(graphics, minX, minY);
        } else {
            int pageWidth = (int) (sin * PAGE_LEFT.width);
            int bindingWidth = (int) ((1 - sin) * BOOK_BINDING.width);
            float offset = (1 - sin) * 50;

            minX = (width - PAGE_LEFT.width - pageWidth) / 2;

            PAGE_RIGHT.drawAt(graphics, minX + pageWidth + bindingWidth, minY);
            GuiUtil.drawCustomQuad(graphics, PAGE_LEFT.sprite, minX + bindingWidth, minY + PAGE_LEFT.height + offset,
                minX + bindingWidth + pageWidth, minY + PAGE_LEFT.height, minX + bindingWidth + pageWidth, minY,
                minX + bindingWidth, minY - offset);
            BOOK_BINDING.drawScaledInside(graphics, (int) (minX + bindingWidth * 0.5), (int) (minY - offset),
                bindingWidth, (int) (BOOK_BINDING.height + offset * 2));
        }
    }

    private void drawOpen(float partialTicks) {
        int cp = currentPage.getPage();
        int pc = currentPage.getPageCount();
        boolean isHalfPageShown = cp + 1 == pc;
        (cp == 0 ? PAGE_LEFT_FIRST : PAGE_LEFT).drawAt(graphics, minX, minY);
        final GuiIcon lastPageIcon;
        if (cp + 2 == pc) {
            lastPageIcon = PAGE_RIGHT_LAST;
        } else if (isHalfPageShown) {
            lastPageIcon = PAGE_RIGHT_BACK;
        } else {
            lastPageIcon = PAGE_RIGHT;
        }
        lastPageIcon.drawAt(graphics, minX + PAGE_LEFT.width, minY);

        // Now draw the actual contents of the book
        String title = currentPage.getTitle();
        if (title != null) {
            int titleWidth = currentFont.getStringWidth(title);
            int x = isHalfPageShown ? (int) (minX + PAGE_LEFT_TEXT.x + (PAGE_LEFT_TEXT.width - titleWidth) / 2)
                : (width - titleWidth) / 2;
            currentFont.drawString(graphics, title, x, minY + 12, 0x90816a);
        }

        tooltipStack = null;
        tooltips.clear();
        setupFontRenderer();
        for (GuideChapter chapter : chapters) {
            chapter.reset();
        }

        currentPage.renderFirstPage(minX + (int) PAGE_LEFT_TEXT.x, minY + (int) PAGE_LEFT_TEXT.y,
            (int) PAGE_LEFT_TEXT.width, (int) PAGE_LEFT_TEXT.height);
        int secondPageX = minX + PAGE_LEFT.width + (int) PAGE_RIGHT_TEXT.x;
        if (!isHalfPageShown) {
            currentPage.renderSecondPage(secondPageX, minY + (int) PAGE_RIGHT_TEXT.y, (int) PAGE_RIGHT_TEXT.width,
                (int) PAGE_RIGHT_TEXT.height);
        }

        boolean drawContents = true;
        boolean smallScreen = isSmallScreen();
        if (smallScreen) {
            drawContents = showingContentsMenu;
            String str = LocaleUtil.localize("buildcraft.guide.chapter_list");
            if (showingContentsMenu) {
                CHAPTER_MARKER_9.draw(graphics, FLOATING_CHAPTER_MENU);
                currentFont.drawString(graphics, str, (int) FLOATING_CHAPTER_MENU.getX() + 7,
                    (int) FLOATING_CHAPTER_MENU.getY() + 7, 0);
            } else {
                boolean isHovered = new GuiRectangle(secondPageX, minY, 80, 10).contains(mouse);
                int y = minY + (isHovered ? -5 : 0);
                int strWidth = currentFont.getStringWidth(str);
                try (GuiUtil.AutoScissor scissor = GuiUtil.scissor(graphics, secondPageX, 0, strWidth + 20,
                    minY + 10)) {
                    CHAPTER_MARKER_9.draw(graphics, secondPageX, y, strWidth + 20, 100);
                    currentFont.drawString(graphics, str, secondPageX + 10, y + 3, 0);
                }
            }
        }

        if (drawContents) {
            int chapterIndex = 0;
            for (GuideChapter chapter : chapters) {
                if (chapter.hasParent()) {
                    continue;
                }
                chapterIndex += chapter.draw(chapterIndex, partialTicks, smallScreen);
            }
        }

        // Draw the back button if there are any pages on the stack
        if (!pages.isEmpty()) {
            IGuiArea position = BACK_POSITION.offset(minX, minY);
            (position.contains(mouse) ? BACK_HOVERED : BACK).drawAt(graphics, position);
        }

        if (tooltipStack != null) {
            graphics.renderTooltip(font, tooltipStack, (int) mouse.getX(), (int) mouse.getY());
        } else if (!tooltips.isEmpty()) {
            int y = (int) mouse.getY();
            for (List<String> tooltip : tooltips) {
                List<Component> lines = new ArrayList<>();
                for (String line : tooltip) {
                    lines.add(Component.literal(line));
                }
                graphics.renderComponentTooltip(font, lines, (int) mouse.getX(), y);
                y += tooltip.size() * font.lineHeight + 10;
            }
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 1.12 drew the book straight over the world, without darkening it
        if (minecraft == null || minecraft.level == null) {
            super.renderBackground(graphics, mouseX, mouseY, partialTick);
        }
    }

    public void setupFontRenderer() {
        currentPage.setFontRenderer(currentFont);
    }

    @Override
    public boolean mouseClicked(double mouseXd, double mouseYd, int mouseButton) {
        int mouseX = (int) mouseXd;
        int mouseY = (int) mouseYd;
        mouse.setMousePosition(mouseX, mouseY);
        if (mouseButton != 0 && mouseButton != 1) {
            return super.mouseClicked(mouseXd, mouseYd, mouseButton);
        }
        if (!isOpen) {
            if (mouseButton == 0 && mouseX >= minX && mouseY >= minY && mouseX <= minX + BOOK_COVER.width
                && mouseY <= minY + BOOK_COVER.height) {
                if (isOpening) {
                    // So you can double-click to open it instantly
                    isOpen = true;
                }
                isOpening = true;
            }
            return true;
        }
        int page0xMin = this.minX + (int) PAGE_LEFT_TEXT.x;
        int page0xMax = page0xMin + (int) PAGE_LEFT_TEXT.width;
        int page1xMin = this.minX + PAGE_LEFT.width + (int) PAGE_RIGHT_TEXT.x;
        int page1xMax = page1xMin + (int) PAGE_RIGHT_TEXT.width;
        int pageYMin = this.minY + (int) PAGE_RIGHT_TEXT.y;
        int pageYMax = pageYMin + (int) PAGE_RIGHT_TEXT.height;

        GuidePageBase current = currentPage;
        current.setFontRenderer(currentFont);

        if (mouseButton == 0) {
            for (GuideChapter chapter : chapters) {
                int clickResult = chapter.handleClick();
                if (clickResult > 0) {
                    if (showingContentsMenu && clickResult == 1) {
                        showingContentsMenu = false;
                    }
                    return true;
                }
            }

            if (isSmallScreen()) {
                if (showingContentsMenu) {
                    if (!FLOATING_CHAPTER_MENU.contains(mouse)) {
                        showingContentsMenu = false;
                    }
                    return true;
                }
                int secondPageX = minX + PAGE_LEFT.width + (int) PAGE_RIGHT_TEXT.x;
                if (new GuiRectangle(secondPageX, minY, 80, 10).contains(mouse)) {
                    showingContentsMenu = true;
                    return true;
                }
            }
        }

        current.handleMouseClick(page0xMin, pageYMin, page0xMax - page0xMin, pageYMax - pageYMin, mouseX, mouseY,
            mouseButton, currentPage.getPage());
        current.handleMouseClick(page1xMin, pageYMin, page1xMax - page1xMin, pageYMax - pageYMin, mouseX, mouseY,
            mouseButton, currentPage.getPage() + 1);

        if (mouseButton == 0 && !pages.isEmpty() && BACK_POSITION.offset(minX, minY).contains(mouse)) {
            closePage();
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (currentPage.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_T && InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(),
            GLFW.GLFW_KEY_F3)) {
            reloadPages();
            return true;
        }
        InputConstants.Key key = InputConstants.getKey(keyCode, scanCode);
        if (Minecraft.getInstance().options.keyLeft.isActiveAndMatches(key)) {
            currentPage.lastPage();
            return true;
        } else if (Minecraft.getInstance().options.keyRight.isActiveAndMatches(key)) {
            currentPage.nextPage();
            return true;
        } else if (Minecraft.getInstance().options.keyInventory.isActiveAndMatches(key)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return currentPage.charTyped(codePoint, modifiers) || super.charTyped(codePoint, modifiers);
    }

    /** Loads the guide again, and every page in the history (that still exists). */
    private void reloadPages() {
        GuideManager.INSTANCE.reload();
        while (true) {
            currentPage = currentPage.createReloaded();
            if (currentPage != null) {
                break;
            }
            currentPage = pages.poll();
            if (currentPage == null) {
                currentPage = new GuidePageContents(this);
                break;
            }
        }
        GuidePageBase[] history = pages.toArray(new GuidePageBase[0]);
        pages.clear();
        for (GuidePageBase old : history) {
            GuidePageBase page = old.createReloaded();
            if (page != null) {
                pages.add(page);
            }
        }
        refreshChapters();
        GuiStack stackIcon = new GuiStack(new ItemStack(BCCoreItems.GUIDE.get()));
        ISimpleDrawable icon = stackIcon::drawAt;
        Minecraft.getInstance().getToasts().addToast(new ToastInformation("buildcraft.guide_book.reloaded", icon));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
