/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.tile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.recipes.BuildcraftRecipeRegistry;
import buildcraftreborn.api.recipes.IProgrammingRecipe;
import buildcraftreborn.lib.misc.data.IdAllocator;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.tile.TileBC_Neptune;
import buildcraftreborn.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraftreborn.lib.tile.item.ItemHandlerSimple;
import buildcraftreborn.robotics.BCRoboticsBlocks;
import buildcraftreborn.robotics.container.ContainerProgrammingTable;
import buildcraftreborn.silicon.tile.TileLaserTableBase;

/** The programming table: put in an item that a programming recipe accepts (a redstone board), pick one of the
 * recipe's options, and lasers power the table until it makes it.
 * <p>
 * 1.21: only items that a recipe accepts go in (7.1 took anything), and the server sends the options to the client, so
 * the two can't disagree when the board blacklist differs. */
public class TileProgrammingTable extends TileLaserTableBase implements MenuProvider {
    public static final int WIDTH = 6;
    public static final int HEIGHT = 4;

    protected static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("programming_table");
    public static final int NET_SELECT_OPTION = IDS.allocId("SELECT_OPTION");

    public final ItemHandlerSimple invInput = itemManager.addInvHandler("input", 1,
        (slot, stack) -> findRecipe(stack) != null, EnumAccess.INSERT, EnumPipePart.VALUES);
    public final ItemHandlerSimple invOutput = itemManager.addInvHandler("output", 1, EnumAccess.EXTRACT,
        EnumPipePart.VALUES);

    @Nullable
    public IProgrammingRecipe recipe;
    public List<ItemStack> options = List.of();
    /** The selected option, or -1. */
    public int optionId = -1;

    public TileProgrammingTable(BlockPos pos, BlockState state) {
        super(BCRoboticsBlocks.PROGRAMMING_TABLE_TILE.get(), pos, state);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    @Nullable
    private static IProgrammingRecipe findRecipe(ItemStack input) {
        if (input.isEmpty()) {
            return null;
        }
        for (IProgrammingRecipe r : BuildcraftRecipeRegistry.programmingRecipes.getRecipes()) {
            if (r.canCraft(input)) {
                return r;
            }
        }
        return null;
    }

    private void setRecipe(@Nullable IProgrammingRecipe recipe) {
        this.recipe = recipe;
        if (recipe == null) {
            options = List.of();
        } else {
            List<ItemStack> all = recipe.getOptions(WIDTH, HEIGHT);
            options = new ArrayList<>(all.subList(0, Math.min(all.size(), WIDTH * HEIGHT)));
        }
    }

    /** Picks the recipe for the input. The selected option is kept while the recipe stays the same (as in 7.1). */
    private void updateRecipe() {
        IProgrammingRecipe found = findRecipe(invInput.getStackInSlot(0));
        if (found != recipe) {
            setRecipe(found);
            optionId = -1;
            sendNetworkGuiUpdate(NET_GUI_DATA);
        }
    }

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, ItemStack before, ItemStack after) {
        super.onSlotChange(handler, slot, before, after);
        if (handler == invInput && level != null && !level.isClientSide) {
            updateRecipe();
        }
    }

    public boolean hasWork() {
        return recipe != null && optionId >= 0 && optionId < options.size()
            && !invInput.getStackInSlot(0).isEmpty() && invOutput.getStackInSlot(0).isEmpty();
    }

    @Override
    public long getTarget() {
        return hasWork() ? recipe.getPowerCost(options.get(optionId)) : 0;
    }

    @Override
    public void update() {
        super.update();
        if (level.isClientSide) {
            return;
        }
        long target = getTarget();
        if (target > 0 && power >= target) {
            ItemStack input = invInput.getStackInSlot(0);
            if (recipe.canCraft(input)) {
                ItemStack result = recipe.craft(input, options.get(optionId));
                if (!result.isEmpty()) {
                    power = 0;
                    ItemStack left = input.copy();
                    left.shrink(result.getCount());
                    invOutput.setStackInSlot(0, result);
                    invInput.setStackInSlot(0, left);
                }
            }
        }
    }

    /** Asks the server to select an option (-1 for none). */
    public void sendSelectOption(int option) {
        PacketDistributor.sendToServer(createMessage(NET_SELECT_OPTION, buffer -> buffer.writeByte(option)));
    }

    // GUI

    @Override
    public boolean onActivated(Player player, InteractionHand hand, ItemStack held) {
        if (!level.isClientSide) {
            openGui(player);
        }
        return true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        return new ContainerProgrammingTable(windowId, player, this);
    }

    // Saving and networking

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        if (recipe != null) {
            nbt.putString("recipeId", recipe.getId());
            nbt.putByte("optionId", (byte) optionId);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        if (nbt.contains("recipeId")) {
            setRecipe(BuildcraftRecipeRegistry.programmingRecipes.getRecipe(nbt.getString("recipeId")));
            optionId = recipe == null ? -1 : nbt.getByte("optionId");
        } else {
            setRecipe(null);
            optionId = -1;
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER && id == NET_GUI_DATA) {
            buffer.writeUtf(recipe == null ? "" : recipe.getId());
            buffer.writeByte(optionId);
            buffer.writeVarInt(options.size());
            for (ItemStack option : options) {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, option);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
        throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT && id == NET_GUI_DATA) {
            String recipeId = buffer.readUtf();
            recipe = recipeId.isEmpty() ? null : BuildcraftRecipeRegistry.programmingRecipes.getRecipe(recipeId);
            optionId = buffer.readByte();
            int count = buffer.readVarInt();
            List<ItemStack> read = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                read.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer));
            }
            options = read;
        } else if (side == LogicalSide.SERVER && id == NET_SELECT_OPTION) {
            int selected = buffer.readByte();
            optionId = selected < 0 || selected >= options.size() ? -1 : selected;
            sendNetworkGuiUpdate(NET_GUI_DATA);
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        super.getDebugInfo(left, right, side);
        left.add("recipe - " + (recipe == null ? "none" : recipe.getId()));
        left.add("option - " + optionId);
    }
}
