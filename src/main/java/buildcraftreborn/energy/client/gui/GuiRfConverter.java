/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.energy.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.core.BCCoreItems;
import buildcraftreborn.energy.tile.TileEngineRF;
import buildcraftreborn.lib.gui.ContainerBCTile;
import buildcraftreborn.lib.gui.GuiBC8;
import buildcraftreborn.lib.gui.GuiElementSimple;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.elem.ToolTip;
import buildcraftreborn.lib.gui.help.DummyHelpElement;
import buildcraftreborn.lib.gui.help.ElementHelpInfo;
import buildcraftreborn.lib.gui.help.ElementHelpInfo.HelpPosition;
import buildcraftreborn.lib.gui.pos.GuiRectangle;
import buildcraftreborn.lib.misc.LocaleUtil;

/** The shared layout of 1.12's {@code GuiEngineRF} and {@code GuiDynamoMJ}: four upgrade slots under the two upgrade
 * types, and an RF battery gauge (on the left for the engine, on the right for the dynamo). */
public abstract class GuiRfConverter<C extends ContainerBCTile<?>> extends GuiBC8<C> {
    private static final int SIZE_X = 176, SIZE_Y = 177;

    private final ResourceLocation texture;
    private final GuiIcon iconGui, iconRf, iconOverlay;
    /** X of the upgrade area (and its overlay) and of the RF gauge. */
    private final int upgradesX, batteryX;

    protected GuiRfConverter(C container, Inventory inventory, Component title, ResourceLocation texture,
        int upgradesX, int batteryX) {
        super(container, inventory, title);
        this.texture = texture;
        this.upgradesX = upgradesX;
        this.batteryX = batteryX;
        iconGui = new GuiIcon(texture, 0, 0, SIZE_X, SIZE_Y);
        iconRf = new GuiIcon(texture, SIZE_X, 0, 16, 60);
        iconOverlay = new GuiIcon(texture, upgradesX - 3, 18, 80, 23);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;

        GuiRectangle upgrades = new GuiRectangle(upgradesX, 42, 74, 20);
        GuiRectangle upgradeTypes = new GuiRectangle(upgradesX, 20, 74, 20);
        GuiRectangle battery = new GuiRectangle(batteryX - 1, 17, 8, 62);

        mainGui.shownElements.add(new DummyHelpElement(upgrades.offset(mainGui.rootElement),
            new ElementHelpInfo("buildcraft.help.rf_engine.upgrades.title", 0xFF_FF_FF_FF,
                "buildcraft.help.rf_engine.upgrades.desc")));
        mainGui.shownElements.add(new GuiElementSimple(mainGui, upgradeTypes.offset(mainGui.rootElement)) {
            @Override
            public void addToolTips(List<ToolTip> tooltips) {
                if (contains(mainGui.mouse)) {
                    List<Component> lines = new ArrayList<>();
                    lines.add(Component.translatable("buildcraft.gui.rf_engine.upgrade_types"));
                    for (Map.Entry<Supplier<? extends Item>, Long> entry : TileEngineRF.RF_UPGRADE.entrySet()) {
                        Component name = new ItemStack(entry.getKey().get()).getHoverName();
                        lines.add(Component.literal(name.getString() + " = +" + upgradeValue(entry.getValue())));
                    }
                    tooltips.add(new ToolTip(lines));
                }
            }
        });
        mainGui.shownElements.add(new GuiElementSimple(mainGui, battery.offset(mainGui.rootElement)) {
            @Override
            public void addHelpElements(List<HelpPosition> elements) {
                long mjFlow = getMjPerTick();
                int rfFlow = (int) (mjFlow / MjAPI.getRfConversion().mjPerRf);
                elements.add(ElementHelpInfo.preTranslated(LocaleUtil.localize(getBatteryHelpKey() + ".title"),
                    0xFF_FF_FF_FF, conversionText(mjFlow, rfFlow).split("\n")).target(this));
            }

            @Override
            public void addToolTips(List<ToolTip> tooltips) {
                if (contains(mainGui.mouse)) {
                    tooltips.add(new ToolTip(Component.literal(LocaleUtil.formatRf(getCurrentRF()) + " / "
                        + LocaleUtil.localizeRf(TileEngineRF.MAX_RF).getString())));
                }
            }
        });
    }

    protected abstract long getMjPerTick();

    protected abstract int getCurrentRF();

    /** The help key for the RF gauge ({@code buildcraft.help.<rf_engine|mj_dynamo>.rf_battery}). */
    protected abstract String getBatteryHelpKey();

    /** The text of the RF gauge's help: what it converts into what. */
    protected abstract String conversionText(long mjFlow, int rfFlow);

    /** How one upgrade's value is shown in the upgrade types tooltip. */
    protected abstract String upgradeValue(long mj);

    @Override
    protected void drawBackgroundLayer(GuiGraphics graphics, float partialTicks) {
        iconGui.drawAt(graphics, mainGui.rootElement);
        double rfHeight = 60.0 * getCurrentRF() / TileEngineRF.MAX_RF;
        double scale = Minecraft.getInstance().getWindow().getGuiScale();
        rfHeight = Math.round(rfHeight * scale) / scale;
        iconRf.drawCutInside(graphics, new GuiRectangle(batteryX, 18 + 60 - rfHeight, 6, rfHeight)
            .offset(mainGui.rootElement));

        graphics.renderItem(new ItemStack(BCCoreItems.GEAR_IRON.get()), leftPos + upgradesX + 18, topPos + 22);
        graphics.renderItem(new ItemStack(BCCoreItems.GEAR_GOLD.get()), leftPos + upgradesX + 41, topPos + 22);

        // 1.12 drew the overlay over the gear icons with depth off at 65% alpha
        graphics.flush();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        graphics.setColor(1, 1, 1, 0.65f);
        iconOverlay.drawAt(graphics, mainGui.rootElement.getX() + upgradesX - 3, mainGui.rootElement.getY() + 18);
        graphics.flush();
        graphics.setColor(1, 1, 1, 1);
        RenderSystem.enableDepthTest();
    }

    @Override
    protected void drawForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY) {
        Component str = title;
        int strWidth = font.width(str);
        double titleX = mainGui.rootElement.getCenterX() - strWidth / 2.0;
        double titleY = mainGui.rootElement.getY() + 6;
        graphics.drawString(font, str, (int) titleX, (int) titleY, 0x404040, false);

        double invX = mainGui.rootElement.getX() + 8;
        double invY = mainGui.rootElement.getY() + SIZE_Y - 96;
        graphics.drawString(font, Component.translatable("container.inventory"), (int) invX, (int) invY, 0x404040,
            false);
    }

    protected ResourceLocation getTexture() {
        return texture;
    }
}
