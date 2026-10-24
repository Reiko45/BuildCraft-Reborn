/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.boards;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.boards.RedstoneBoardRobot;
import buildcraftreborn.api.boards.RedstoneBoardRobotNBT;
import buildcraftreborn.api.robots.EntityRobotBase;

/** One of BuildCraft's boards: a name, the board class it makes and the colour of the board item.
 * <p>
 * 1.21: the id is {@code buildcraftreborn:<name>}, the robot texture is {@code textures/entity/robot/<name>.png} and
 * the names are {@code buildcraftreborn.board.<name>} (and {@code .desc}) in the language files. */
public class BCBoardNBT extends RedstoneBoardRobotNBT {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Map<String, BCBoardNBT> REGISTRY = new LinkedHashMap<>();

    private final ResourceLocation texture;
    private final ResourceLocation itemModel;
    private final String id, name, boardType;
    private final Constructor<? extends RedstoneBoardRobot> boardInit;

    /** @param name The board's name, such as {@code picker} or {@code fluid_carrier}.
     * @param boardType The colour of the board item: green, blue, red or yellow. */
    public BCBoardNBT(String name, Class<? extends RedstoneBoardRobot> board, String boardType) {
        this.id = BuildCraftReborn.id(name).toString();
        this.name = name;
        this.boardType = boardType;
        this.texture = BuildCraftReborn.id("textures/entity/robot/" + name + ".png");
        this.itemModel = BuildCraftReborn.id("item/redstone_board/" + boardType);

        Constructor<? extends RedstoneBoardRobot> boardInitLocal;
        try {
            boardInitLocal = board.getConstructor(EntityRobotBase.class);
        } catch (Exception e) {
            LOGGER.error("The board {} has no robot constructor", board.getName(), e);
            boardInitLocal = null;
        }
        this.boardInit = boardInitLocal;

        REGISTRY.put(name, this);
    }

    public String getName() {
        return name;
    }

    public String getBoardType() {
        return boardType;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public void addInformation(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
        TooltipFlag flag) {
        tooltip.add(getDisplayName().copy().withStyle(ChatFormatting.BOLD));
        tooltip.add(Component.translatable("buildcraftreborn.board." + name + ".desc").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public RedstoneBoardRobot create(EntityRobotBase robot) {
        try {
            return boardInit.newInstance(robot);
        } catch (Exception e) {
            LOGGER.error("Couldn't create the board {}", id, e);
            return RedstoneBoardRobotEmptyNBT.instance.create(robot);
        }
    }

    @Override
    public ResourceLocation getRobotTexture() {
        return texture;
    }

    @Override
    public ResourceLocation getItemModelLocation() {
        return itemModel;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("buildcraftreborn.board." + name);
    }
}
