/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.tile;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.ImmutableList;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.recipes.IngredientStack;
import buildcraftreborn.api.recipes.IntegrationRecipe;
import buildcraftreborn.lib.misc.StackUtil;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.recipe.IntegrationRecipeRegistry;
import buildcraftreborn.lib.tile.item.ItemHandlerManager;
import buildcraftreborn.lib.tile.item.ItemHandlerSimple;
import buildcraftreborn.silicon.BCSiliconBlocks;
import buildcraftreborn.silicon.container.ContainerIntegrationTable;

/** Combines the item in its centre slot with the items around it, using laser power. BuildCraft 8.0 has no recipes
 * for it; addons can add some through {@link IntegrationRecipeRegistry}. */
public class TileIntegrationTable extends TileLaserTableBase implements MenuProvider {
    public final ItemHandlerSimple invTarget = itemManager.addInvHandler("target", 1,
        ItemHandlerManager.EnumAccess.BOTH, EnumPipePart.VALUES);
    public final ItemHandlerSimple invToIntegrate = itemManager.addInvHandler("toIntegrate", 3 * 3 - 1,
        ItemHandlerManager.EnumAccess.BOTH, EnumPipePart.VALUES);
    /** 8.0 made this insert-only, so pipes couldn't take the results out. */
    public final ItemHandlerSimple invResult = itemManager.addInvHandler("result", 1,
        ItemHandlerManager.EnumAccess.EXTRACT, EnumPipePart.VALUES);
    @Nullable
    public IntegrationRecipe recipe;

    public TileIntegrationTable(BlockPos pos, BlockState state) {
        super(BCSiliconBlocks.INTEGRATION_TABLE_TILE.get(), pos, state);
    }

    private boolean extract(IngredientStack item, ImmutableList<IngredientStack> items, boolean simulate) {
        ItemStack targetStack = invTarget.getStackInSlot(0);
        if (targetStack.isEmpty()) return false;
        if (!item.ingredient.test(targetStack) || targetStack.getCount() < item.count) return false;
        if (!extract(invToIntegrate, items, simulate, true)) return false;
        if (!simulate) {
            targetStack = targetStack.copy();
            targetStack.setCount(targetStack.getCount() - item.count);
            invTarget.setStackInSlot(0, targetStack);
        }
        return true;
    }

    private boolean isSpaceEnough(ItemStack stack) {
        ItemStack output = invResult.getStackInSlot(0);
        return output.isEmpty()
            || (StackUtil.canMerge(stack, output) && stack.getCount() + output.getCount() <= stack.getMaxStackSize());
    }

    private void updateRecipe() {
        if (recipe != null) {
            ItemStack output = getOutput();
            if (!output.isEmpty() && extract(recipe.getCenterStack(), recipe.getRequirements(output), true)) {
                return;
            }
        }
        recipe = IntegrationRecipeRegistry.INSTANCE.getRecipeFor(level, invTarget.getStackInSlot(0),
            invToIntegrate.stacks);
    }

    public ItemStack getOutput() {
        return recipe != null ? recipe.getOutput(invTarget.getStackInSlot(0), invToIntegrate.stacks) : ItemStack.EMPTY;
    }

    @Override
    public long getTarget() {
        ItemStack output = getOutput();
        return recipe != null && isSpaceEnough(output) ? recipe.getRequiredMicroJoules(output) : 0;
    }

    @Override
    public void update() {
        super.update();
        if (level.isClientSide) {
            return;
        }
        updateRecipe();
        if (getTarget() > 0 && power >= getTarget()) {
            ItemStack output = getOutput();
            extract(recipe.getCenterStack(), recipe.getRequirements(output), false);
            ItemStack result = invResult.getStackInSlot(0);
            if (!result.isEmpty()) {
                result = result.copy();
                result.setCount(result.getCount() + output.getCount());
            } else {
                result = output.copy();
            }
            invResult.setStackInSlot(0, result);
            power -= getTarget();
        }
        sendNetworkGuiUpdate(NET_GUI_DATA);
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
        return new ContainerIntegrationTable(windowId, player, this);
    }

    // Saving

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        if (recipe != null && recipe.name != null) {
            nbt.putString("recipe", recipe.name.toString());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        recipe = nbt.contains("recipe") ? lookupRecipe(nbt.getString("recipe")) : null;
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (id == NET_GUI_DATA) {
            buffer.writeBoolean(recipe != null);
            if (recipe != null) {
                buffer.writeUtf(recipe.name.toString());
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
        throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (id == NET_GUI_DATA) {
            recipe = buffer.readBoolean() ? lookupRecipe(buffer.readUtf()) : null;
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        super.getDebugInfo(left, right, side);
        left.add("recipe - " + recipe);
        left.add("target - " + getTarget());
    }

    @Nullable
    private IntegrationRecipe lookupRecipe(String name) {
        ResourceLocation id = ResourceLocation.tryParse(name);
        return id == null ? null : IntegrationRecipeRegistry.INSTANCE.getRecipe(level, id);
    }
}
