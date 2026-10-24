/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.client.gui;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import buildcraftreborn.api.filler.IFillerPattern;
import buildcraftreborn.api.statements.containers.IFillerStatementContainer;
import buildcraftreborn.builders.container.IContainerFilling;
import buildcraftreborn.builders.filler.FillerStatementContext;
import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.lib.gui.BuildCraftGui;
import buildcraftreborn.lib.gui.ContainerBC_Neptune;
import buildcraftreborn.lib.gui.GuiBC8;
import buildcraftreborn.lib.gui.GuiElementSimple;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.IInteractionElement;
import buildcraftreborn.lib.gui.elem.ToolTip;
import buildcraftreborn.lib.gui.help.ElementHelpInfo;
import buildcraftreborn.lib.gui.help.ElementHelpInfo.HelpPosition;
import buildcraftreborn.lib.gui.pos.GuiRectangle;
import buildcraftreborn.lib.gui.pos.IGuiArea;
import buildcraftreborn.lib.gui.statement.GuiElementStatement;
import buildcraftreborn.lib.gui.statement.GuiElementStatementParam;
import buildcraftreborn.lib.gui.statement.GuiElementStatementSource;

/** The parts the filler and the filler planner GUIs share (1.12's {@code filler_base.json}): the pattern, its four
 * parameters, the panel of patterns to pick from, and the "invert" button.
 * <p>
 * 1.21: written in code; 1.12 described these GUIs in JSON. */
public abstract class GuiFillerBase<C extends ContainerBC_Neptune & IContainerFilling> extends GuiBC8<C> {
    protected final ResourceLocation texture;

    protected GuiFillerBase(C container, Inventory inventory, Component title, ResourceLocation texture,
        IFillerStatementContainer statementContainer, int width, int height) {
        super(container, inventory, title);
        this.texture = texture;
        imageWidth = width;
        imageHeight = height;

        mainGui.shownElements.add(new GuiElementStatementSource<>(mainGui, true, FillerStatementContext.CONTEXT_ALL));
        mainGui.shownElements.add(new GuiElementStatement<>(mainGui, area(12, 32, 32, 32),
            container.getPatternStatementClient(), FillerStatementContext.CONTEXT_ALL, false));
        for (int i = 0; i < 4; i++) {
            mainGui.shownElements.add(new GuiElementStatementParam(mainGui, area(53 + 18 * i, 39, 18, 18),
                statementContainer, container.getPatternStatementClient(), i, true));
        }
        mainGui.shownElements.add(new ToggleButton(mainGui, area(152, 40, 16, 16), 224, container::isInverted,
            container::sendInverted, on -> "tip.filler.invert." + (on ? "on" : "off"),
            new ElementHelpInfo("buildcraft.help.filler.invert.title", 0xFF_88_AA_CC,
                "buildcraft.help.filler.invert.desc")));
    }

    protected IGuiArea area(int x, int y, int width, int height) {
        return new GuiRectangle(x, y, width, height).offset(mainGui.rootElement);
    }

    @Override
    protected void drawBackgroundLayer(GuiGraphics graphics, float partialTicks) {
        new GuiIcon(texture, 0, 0, imageWidth, imageHeight).drawAt(graphics, leftPos, topPos);
        IFillerPattern pattern = container.getPatternStatementClient().get();
        ISprite sprite = pattern == null ? null : pattern.getSprite();
        if (sprite != null) {
            GuiIcon.draw(graphics, sprite, leftPos + 12, topPos + 32, leftPos + 44, topPos + 64);
        }
    }

    protected void drawCentredTitle(GuiGraphics graphics, Component text) {
        graphics.drawString(font, text, leftPos + (imageWidth - font.width(text)) / 2, topPos + 6, 0x404040, false);
    }

    /** A two state button drawn from the GUI texture (1.12's "four_button": up, hovered, down, down and hovered). */
    protected class ToggleButton extends GuiElementSimple implements IInteractionElement {
        private final int uStart;
        private final BooleanSupplier state;
        private final Consumer<Boolean> onChange;
        private final java.util.function.Function<Boolean, String> tooltip;
        private final ElementHelpInfo help;

        public ToggleButton(BuildCraftGui gui, IGuiArea area, int uStart, BooleanSupplier state,
            Consumer<Boolean> onChange, java.util.function.Function<Boolean, String> tooltip, ElementHelpInfo help) {
            super(gui, area);
            this.uStart = uStart;
            this.state = state;
            this.onChange = onChange;
            this.tooltip = tooltip;
            this.help = help;
        }

        @Override
        public void drawBackground(GuiGraphics graphics, float partialTicks) {
            boolean active = state.getAsBoolean();
            boolean hovered = contains(gui.mouse);
            new GuiIcon(texture, uStart + (active ? 16 : 0), hovered ? 16 : 0, 16, 16).drawAt(graphics, getX(),
                getY());
        }

        @Override
        public void onMouseClicked(int button) {
            if (button == 0 && contains(gui.mouse)) {
                onChange.accept(!state.getAsBoolean());
            }
        }

        @Override
        public void addToolTips(List<ToolTip> tooltips) {
            if (contains(gui.mouse)) {
                tooltips.add(ToolTip.createLocalized(tooltip.apply(state.getAsBoolean())));
            }
        }

        @Override
        public void addHelpElements(List<HelpPosition> elements) {
            elements.add(help.target(expand(2)));
        }
    }

    /** An icon that is only shown sometimes, with a tooltip and help. */
    protected static class StatusIcon extends GuiElementSimple {
        private final BooleanSupplier visible;
        private final Supplier<ISprite> sprite;
        private final Supplier<Component> tooltip;
        private final ElementHelpInfo help;

        public StatusIcon(BuildCraftGui gui, IGuiArea area, BooleanSupplier visible, Supplier<ISprite> sprite,
            Supplier<Component> tooltip, ElementHelpInfo help) {
            super(gui, area);
            this.visible = visible;
            this.sprite = sprite;
            this.tooltip = tooltip;
            this.help = help;
        }

        @Override
        public void drawBackground(GuiGraphics graphics, float partialTicks) {
            ISprite s = sprite.get();
            if (visible.getAsBoolean() && s != null) {
                GuiIcon.draw(graphics, s, getX(), getY(), getX() + 16, getY() + 16);
            }
        }

        @Override
        public void addToolTips(List<ToolTip> tooltips) {
            if (visible.getAsBoolean() && contains(gui.mouse)) {
                tooltips.add(new ToolTip(tooltip.get()));
            }
        }

        @Override
        public void addHelpElements(List<HelpPosition> elements) {
            elements.add(help.target(expand(2)));
        }
    }
}
