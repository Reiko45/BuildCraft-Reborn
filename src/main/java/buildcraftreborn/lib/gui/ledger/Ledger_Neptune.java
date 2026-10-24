/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui.ledger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.Mth;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.lib.client.BCLibSprites;
import buildcraftreborn.lib.client.sprite.SpriteNineSliced;
import buildcraftreborn.lib.expression.api.IVariableNode.IVariableNodeBoolean;
import buildcraftreborn.lib.gui.BuildCraftGui;
import buildcraftreborn.lib.gui.IContainingElement;
import buildcraftreborn.lib.gui.IGuiElement;
import buildcraftreborn.lib.gui.IInteractionElement;
import buildcraftreborn.lib.gui.ISimpleDrawable;
import buildcraftreborn.lib.gui.elem.GuiElementDrawable;
import buildcraftreborn.lib.gui.elem.GuiElementText;
import buildcraftreborn.lib.gui.elem.ToolTip;
import buildcraftreborn.lib.gui.help.ElementHelpInfo.HelpPosition;
import buildcraftreborn.lib.gui.pos.GuiRectangle;
import buildcraftreborn.lib.gui.pos.IGuiPosition;
import buildcraftreborn.lib.misc.GuiUtil;
import buildcraftreborn.lib.misc.GuiUtil.AutoScissor;

/** A ledger: a coloured tab on the side of a GUI that slides open when clicked. Client only. */
public class Ledger_Neptune implements IInteractionElement, IContainingElement {
    public static final ISprite SPRITE_EXP_NEG = BCLibSprites.LEDGER_LEFT;
    public static final ISprite SPRITE_EXP_POS = BCLibSprites.LEDGER_RIGHT;

    public static final SpriteNineSliced SPRITE_SPLIT_NEG = new SpriteNineSliced(SPRITE_EXP_NEG, 4, 4, 12, 12, 16);
    public static final SpriteNineSliced SPRITE_SPLIT_POS = new SpriteNineSliced(SPRITE_EXP_POS, 4, 4, 12, 12, 16);

    public static final int LEDGER_CHANGE_DIFF = 2;
    public static final int LEDGER_GAP = 4;

    public static final int CLOSED_WIDTH = 2 + 16 + LEDGER_GAP;
    public static final int CLOSED_HEIGHT = LEDGER_GAP + 16 + LEDGER_GAP;

    public final BuildCraftGui gui;
    public final int colour;
    public final boolean expandPositive;

    public final IGuiPosition positionLedgerStart;
    public final IGuiPosition positionLedgerIconStart;
    public final IGuiPosition positionLedgerInnerStart;

    protected double maxWidth = 96, maxHeight = 48;

    protected double currentWidth = CLOSED_WIDTH;
    protected double currentHeight = CLOSED_HEIGHT;
    protected double lastWidth = currentWidth;
    protected double lastHeight = currentHeight;
    protected double interpWidth = lastWidth;
    protected double interpHeight = lastHeight;

    protected final List<IGuiElement> closedElements = new ArrayList<>();
    protected final List<IGuiElement> openElements = new ArrayList<>();

    protected IGuiPosition positionAppending;

    protected String title = "unknown";

    /** -1 means shrinking, 0 no change, 1 expanding */
    private int currentDifference = 0;

    @Nullable
    private IVariableNodeBoolean isOpenProperty;

    public Ledger_Neptune(BuildCraftGui gui, int colour, boolean expandPositive) {
        this.gui = gui;
        this.colour = colour;
        this.expandPositive = expandPositive;

        if (expandPositive) {
            positionLedgerStart = gui.lowerRightLedgerPos;
            gui.lowerRightLedgerPos = getPosition(-1, 1).offset(0, 5);
            positionLedgerIconStart = positionLedgerStart.offset(2, LEDGER_GAP);
        } else {
            positionLedgerStart = gui.lowerLeftLedgerPos.offset(() -> -getWidth(), () -> 0);
            gui.lowerLeftLedgerPos = getPosition(1, 1).offset(0, 5);
            positionLedgerIconStart = positionLedgerStart.offset(LEDGER_GAP, LEDGER_GAP);
        }
        positionLedgerInnerStart = positionLedgerIconStart.offset(16 + LEDGER_GAP, 0);
        positionAppending = positionLedgerInnerStart.offset(0, 3);

        GuiRectangle iconRect = new GuiRectangle(0, 0, 16, 16);
        ISimpleDrawable drawable = this::drawIcon;
        closedElements.add(new GuiElementDrawable(gui, iconRect.offset(positionLedgerIconStart), drawable, false));
        appendText(this::getTitle, this::getTitleColour).setDropShadow(true);
        calculateMaxSize();
    }

    protected GuiElementText appendText(String text, int colour) {
        return appendText(() -> text, colour);
    }

    protected GuiElementText appendText(Supplier<String> text, int colour) {
        return appendText(text, () -> colour);
    }

    protected GuiElementText appendText(Supplier<String> text, IntSupplier colour) {
        return append(new GuiElementText(gui, positionAppending, text, colour));
    }

    protected <T extends IGuiElement> T append(T element) {
        openElements.add(element);
        positionAppending = positionAppending.offset(() -> 0, () -> 3 + element.getHeight());
        return element;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setOpenProperty(IVariableNodeBoolean prop) {
        this.isOpenProperty = prop;
        if (prop.evaluate()) {
            currentDifference = 1;
            lastWidth = currentWidth = maxWidth;
            lastHeight = currentHeight = maxHeight;
        } else {
            currentDifference = -1;
            lastWidth = currentWidth = CLOSED_WIDTH;
            lastHeight = currentHeight = CLOSED_HEIGHT;
        }
    }

    @Override
    public void calculateSizes() {
        calculateMaxSize();
        if (isOpenProperty != null) {
            setOpenProperty(isOpenProperty);
        }
    }

    /** The default implementation only works if all the elements are based around {@link #positionLedgerStart} */
    public void calculateMaxSize() {
        double w = CLOSED_WIDTH;
        double h = CLOSED_HEIGHT;

        for (IGuiElement element : openElements) {
            w = Math.max(w, element.getEndX());
            h = Math.max(h, element.getEndY());
        }
        w -= getX();
        h -= getY();
        maxWidth = w + LEDGER_GAP * 2;
        maxHeight = h + LEDGER_GAP * 2;
    }

    @Override
    public void tick() {
        lastWidth = currentWidth;
        lastHeight = currentHeight;

        double targetWidth;
        double targetHeight;
        if (currentDifference == 1) {
            targetWidth = maxWidth;
            targetHeight = maxHeight;
        } else if (currentDifference == -1) {
            targetWidth = CLOSED_WIDTH;
            targetHeight = CLOSED_HEIGHT;
        } else {
            return;
        }

        double maxDiff = Math.max(maxWidth - CLOSED_WIDTH, maxHeight - CLOSED_HEIGHT);
        double ldgDiff = Mth.clamp(maxDiff / 5, 1, 15);

        currentWidth = approach(currentWidth, targetWidth, ldgDiff);
        currentHeight = approach(currentHeight, targetHeight, ldgDiff);
    }

    private static double approach(double current, double target, double step) {
        if (current < target) {
            return Math.min(target, current + step);
        } else if (current > target) {
            return Math.max(target, current - step);
        }
        return current;
    }

    private static double interp(double past, double current, float partialTicks) {
        if (past == current) {
            return current;
        }
        if (partialTicks <= 0) {
            return past;
        }
        if (partialTicks >= 1) {
            return current;
        }
        return past * (1 - partialTicks) + current * partialTicks;
    }

    public final boolean shouldDrawOpen() {
        return currentWidth > CLOSED_WIDTH || currentHeight > CLOSED_HEIGHT;
    }

    @Override
    public List<IGuiElement> getChildElements() {
        return openElements;
    }

    @Override
    public IGuiPosition getChildElementPosition() {
        return positionLedgerInnerStart;
    }

    public List<IGuiElement> getClosedElements() {
        return closedElements;
    }

    @Override
    public void drawBackground(GuiGraphics graphics, float partialTicks) {
        double startX = getX();
        double startY = getY();
        interpWidth = interp(lastWidth, currentWidth, partialTicks);
        interpHeight = interp(lastHeight, currentHeight, partialTicks);

        SpriteNineSliced split = expandPositive ? SPRITE_SPLIT_POS : SPRITE_SPLIT_NEG;

        GuiUtil.setColour(graphics, colour);
        split.draw(graphics, startX, startY, interpWidth, interpHeight);
        GuiUtil.resetColour(graphics);

        IGuiPosition pos2 = positionLedgerIconStart;
        try (AutoScissor a = GuiUtil.scissor(graphics, pos2.getX(), pos2.getY(), interpWidth - 4, interpHeight - 8)) {
            for (IGuiElement element : closedElements) {
                element.drawBackground(graphics, partialTicks);
            }
            if (shouldDrawOpen()) {
                for (IGuiElement element : openElements) {
                    element.drawBackground(graphics, partialTicks);
                }
            }
        }
    }

    @Override
    public void drawForeground(GuiGraphics graphics, float partialTicks) {
        double scissorX = positionLedgerIconStart.getX();
        double scissorY = positionLedgerIconStart.getY();
        double scissorWidth = interpWidth - 8;
        double scissorHeight = interpHeight - 8;
        try (AutoScissor a = GuiUtil.scissor(graphics, scissorX, scissorY, scissorWidth, scissorHeight)) {
            for (IGuiElement element : closedElements) {
                element.drawForeground(graphics, partialTicks);
            }
            if (shouldDrawOpen()) {
                for (IGuiElement element : openElements) {
                    element.drawForeground(graphics, partialTicks);
                }
            }
        }
    }

    @Override
    public void onMouseClicked(int button) {
        boolean childClicked = false;
        for (IGuiElement elem : openElements) {
            if (elem instanceof IInteractionElement interaction) {
                interaction.onMouseClicked(button);
                childClicked |= elem.contains(gui.mouse);
            }
        }
        for (IGuiElement elem : closedElements) {
            if (elem instanceof IInteractionElement interaction) {
                interaction.onMouseClicked(button);
                childClicked |= elem.contains(gui.mouse);
            }
        }
        if (!childClicked && contains(gui.mouse)) {
            boolean nowOpen = false;
            if (currentDifference == 1) {
                currentDifference = -1;
            } else {
                currentDifference = 1;
                nowOpen = true;
            }
            if (isOpenProperty != null) {
                isOpenProperty.set(nowOpen);
            }
        }
    }

    @Override
    public void onMouseDragged(int button, long ticksSinceClick) {
        for (IGuiElement elem : openElements) {
            if (elem instanceof IInteractionElement interaction) {
                interaction.onMouseDragged(button, ticksSinceClick);
            }
        }
        for (IGuiElement elem : closedElements) {
            if (elem instanceof IInteractionElement interaction) {
                interaction.onMouseDragged(button, ticksSinceClick);
            }
        }
    }

    @Override
    public void onMouseReleased(int button) {
        for (IGuiElement elem : openElements) {
            if (elem instanceof IInteractionElement interaction) {
                interaction.onMouseReleased(button);
            }
        }
        for (IGuiElement elem : closedElements) {
            if (elem instanceof IInteractionElement interaction) {
                interaction.onMouseReleased(button);
            }
        }
    }

    protected void drawIcon(GuiGraphics graphics, double x, double y) {}

    @Override
    public double getX() {
        return positionLedgerStart.getX();
    }

    @Override
    public double getY() {
        return positionLedgerStart.getY();
    }

    @Override
    public double getWidth() {
        float partialTicks = gui.getLastPartialTicks();
        if (lastWidth == currentWidth) return currentWidth;
        else if (partialTicks <= 0) return lastWidth;
        else if (partialTicks >= 1) return currentWidth;
        else return lastWidth * (1 - partialTicks) + currentWidth * partialTicks;
    }

    @Override
    public double getHeight() {
        float partialTicks = gui.getLastPartialTicks();
        if (lastHeight == currentHeight) return currentHeight;
        else if (partialTicks <= 0) return lastHeight;
        else if (partialTicks >= 1) return currentHeight;
        else return lastHeight * (1 - partialTicks) + currentHeight * partialTicks;
    }

    public String getTitle() {
        return I18n.get(title);
    }

    public int getTitleColour() {
        return 0xFF_E1_C9_2F;
    }

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        for (IGuiElement element : closedElements) {
            element.addToolTips(tooltips);
        }
        if (shouldDrawOpen()) {
            for (IGuiElement element : openElements) {
                element.addToolTips(tooltips);
            }
        }
        if (currentWidth != maxWidth || currentHeight != maxHeight) {
            if (contains(gui.mouse)) {
                tooltips.add(new ToolTip(net.minecraft.network.chat.Component.literal(getTitle())));
            }
        }
    }

    @Override
    public void addHelpElements(List<HelpPosition> elements) {
        for (IGuiElement element : closedElements) {
            element.addHelpElements(elements);
        }
        if (currentWidth == maxWidth && currentHeight == maxHeight) {
            for (IGuiElement element : openElements) {
                element.addHelpElements(elements);
            }
        }
    }
}
