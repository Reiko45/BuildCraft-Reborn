/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.tile;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.recipes.AssemblyRecipe;
import buildcraftreborn.lib.misc.AdvancementUtil;
import buildcraftreborn.lib.misc.InventoryUtil;
import buildcraftreborn.lib.misc.data.IdAllocator;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.recipe.AssemblyRecipeRegistry;
import buildcraftreborn.lib.tile.TileBC_Neptune;
import buildcraftreborn.lib.tile.item.ItemHandlerManager;
import buildcraftreborn.lib.tile.item.ItemHandlerSimple;
import buildcraftreborn.silicon.BCSiliconBlocks;
import buildcraftreborn.silicon.EnumAssemblyRecipeState;
import buildcraftreborn.silicon.container.ContainerAssemblyTable;

/** The assembly table. Every recipe its 12 slots can make shows in its GUI; the player saves the ones to make, and
 * lasers power them one after another. Results go into neighbouring inventories (or drop). */
public class TileAssemblyTable extends TileLaserTableBase implements MenuProvider {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("assembly_table");
    public static final int NET_RECIPE_STATE = IDS.allocId("RECIPE_STATE");

    private static final ResourceLocation ADVANCEMENT = BuildCraftReborn.id("precision_crafting");

    public final ItemHandlerSimple inv = itemManager.addInvHandler("inv", 3 * 4, ItemHandlerManager.EnumAccess.BOTH,
        EnumPipePart.VALUES);
    public SortedMap<AssemblyInstruction, EnumAssemblyRecipeState> recipesStates = new TreeMap<>();

    public TileAssemblyTable(BlockPos pos, BlockState state) {
        super(BCSiliconBlocks.ASSEMBLY_TABLE_TILE.get(), pos, state);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    private void updateRecipes() {
        // (1.12 also went through every recipe every tick)
        boolean changed = false;
        for (AssemblyRecipe recipe : AssemblyRecipeRegistry.getAll(level).values()) {
            Set<ItemStack> outputs = recipe.getOutputs(inv.stacks);
            for (ItemStack out : outputs) {
                AssemblyInstruction instruction = new AssemblyInstruction(recipe, out);
                if (!recipesStates.containsKey(instruction)) {
                    recipesStates.put(instruction, EnumAssemblyRecipeState.POSSIBLE);
                    changed = true;
                }
            }
        }

        boolean findActive = false;
        for (Iterator<Map.Entry<AssemblyInstruction, EnumAssemblyRecipeState>> iterator =
            recipesStates.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<AssemblyInstruction, EnumAssemblyRecipeState> entry = iterator.next();
            AssemblyInstruction instruction = entry.getKey();
            EnumAssemblyRecipeState state = entry.getValue();
            boolean enough = extract(inv, instruction.recipe.getInputsFor(instruction.output), true, false);
            if (state == EnumAssemblyRecipeState.POSSIBLE) {
                if (!enough) {
                    iterator.remove();
                    changed = true;
                }
            } else {
                if (enough) {
                    if (state == EnumAssemblyRecipeState.SAVED) {
                        state = EnumAssemblyRecipeState.SAVED_ENOUGH;
                        changed = true;
                    }
                } else {
                    if (state != EnumAssemblyRecipeState.SAVED) {
                        state = EnumAssemblyRecipeState.SAVED;
                        changed = true;
                    }
                }
            }
            if (state == EnumAssemblyRecipeState.SAVED_ENOUGH_ACTIVE) {
                findActive = true;
            }
            entry.setValue(state);
        }
        if (!findActive) {
            for (Map.Entry<AssemblyInstruction, EnumAssemblyRecipeState> entry : recipesStates.entrySet()) {
                if (entry.getValue() == EnumAssemblyRecipeState.SAVED_ENOUGH) {
                    entry.setValue(EnumAssemblyRecipeState.SAVED_ENOUGH_ACTIVE);
                    changed = true;
                    break;
                }
            }
        }
        if (changed) {
            sendNetworkGuiUpdate(NET_GUI_DATA);
        }
    }

    @Nullable
    private AssemblyInstruction getActiveRecipe() {
        return recipesStates.entrySet().stream()
            .filter(entry -> entry.getValue() == EnumAssemblyRecipeState.SAVED_ENOUGH_ACTIVE).map(Map.Entry::getKey)
            .findFirst().orElse(null);
    }

    private void activateNextRecipe() {
        AssemblyInstruction activeRecipe = getActiveRecipe();
        if (activeRecipe == null) {
            return;
        }
        int index = 0;
        int activeIndex = 0;
        boolean isActiveLast = false;
        long enoughCount = recipesStates.values().stream().filter(state -> state == EnumAssemblyRecipeState.SAVED_ENOUGH
            || state == EnumAssemblyRecipeState.SAVED_ENOUGH_ACTIVE).count();
        if (enoughCount <= 1) {
            return;
        }
        for (Map.Entry<AssemblyInstruction, EnumAssemblyRecipeState> entry : recipesStates.entrySet()) {
            EnumAssemblyRecipeState state = entry.getValue();
            if (state == EnumAssemblyRecipeState.SAVED_ENOUGH) {
                isActiveLast = false;
            }
            if (state == EnumAssemblyRecipeState.SAVED_ENOUGH_ACTIVE) {
                entry.setValue(EnumAssemblyRecipeState.SAVED_ENOUGH);
                activeIndex = index;
                isActiveLast = true;
            }
            index++;
        }
        index = 0;
        for (Map.Entry<AssemblyInstruction, EnumAssemblyRecipeState> entry : recipesStates.entrySet()) {
            AssemblyRecipe recipe = entry.getKey().recipe;
            if (entry.getValue() == EnumAssemblyRecipeState.SAVED_ENOUGH && recipe != activeRecipe.recipe
                && (index > activeIndex || isActiveLast)) {
                entry.setValue(EnumAssemblyRecipeState.SAVED_ENOUGH_ACTIVE);
                break;
            }
            index++;
        }
    }

    @Override
    public long getTarget() {
        return Optional.ofNullable(getActiveRecipe())
            .map(instruction -> instruction.recipe.getRequiredMicroJoulesFor(instruction.output)).orElse(0L);
    }

    @Override
    public void update() {
        super.update();
        if (level.isClientSide) {
            return;
        }
        updateRecipes();
        if (getTarget() > 0) {
            if (hasOwner()) {
                AdvancementUtil.unlockAdvancement(getOwner().getId(), ADVANCEMENT);
            }
            if (power >= getTarget()) {
                AssemblyInstruction instruction = getActiveRecipe();
                extract(inv, instruction.recipe.getInputsFor(instruction.output), false, false);
                InventoryUtil.addToBestAcceptor(getLevel(), getBlockPos(), null, instruction.output.copy());
                power -= getTarget();
                activateNextRecipe();
            }
            sendNetworkGuiUpdate(NET_GUI_DATA);
        }
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
        return new ContainerAssemblyTable(windowId, player, this);
    }

    // Saving

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        ListTag recipesStatesTag = new ListTag();
        recipesStates.forEach((instruction, state) -> {
            if (instruction.recipe.getRegistryName() == null) {
                return;
            }
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("recipe", instruction.recipe.getRegistryName().toString());
            entryTag.put("output", instruction.output.save(registries));
            entryTag.putInt("state", state.ordinal());
            recipesStatesTag.add(entryTag);
        });
        nbt.put("recipes_states", recipesStatesTag);
    }

    /** Read in {@link #loadAdditional} (before the level is known), resolved on the first tick. */
    @Nullable
    private ListTag unresolvedStates;

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        recipesStates.clear();
        unresolvedStates = nbt.getList("recipes_states", Tag.TAG_COMPOUND);
        if (level != null) {
            resolveLoadedStates(registries);
        }
    }

    private void resolveLoadedStates(HolderLookup.Provider registries) {
        ListTag list = unresolvedStates;
        unresolvedStates = null;
        if (list == null) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            if (entryTag.contains("output")) {
                ItemStack output = ItemStack.parseOptional(registries, entryTag.getCompound("output"));
                AssemblyInstruction instruction = lookupRecipe(entryTag.getString("recipe"), output);
                int state = entryTag.getInt("state");
                if (instruction != null && state >= 0 && state < EnumAssemblyRecipeState.values().length) {
                    recipesStates.put(instruction, EnumAssemblyRecipeState.values()[state]);
                }
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && unresolvedStates != null) {
            resolveLoadedStates(level.registryAccess());
        }
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (id == NET_GUI_DATA) {
            buffer.writeInt(recipesStates.size());
            recipesStates.forEach((instruction, state) -> {
                ResourceLocation name = instruction.recipe.getRegistryName();
                buffer.writeUtf(name == null ? "" : name.toString());
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, instruction.output);
                buffer.writeInt(state.ordinal());
            });
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
        throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (id == NET_GUI_DATA) {
            recipesStates.clear();
            int count = buffer.readInt();
            for (int i = 0; i < count; i++) {
                String name = buffer.readUtf();
                ItemStack output = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
                EnumAssemblyRecipeState state = readState(buffer);
                AssemblyInstruction instruction = lookupRecipe(name, output);
                if (instruction != null && state != null) {
                    recipesStates.put(instruction, state);
                }
            }
        }
        if (id == NET_RECIPE_STATE && side == LogicalSide.SERVER) {
            AssemblyInstruction recipe = lookupRecipe(buffer.readUtf(), ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer));
            EnumAssemblyRecipeState state = readState(buffer);
            // The client only picks between possible and saved; the table works out the rest
            if (recipe != null && state != null && recipesStates.containsKey(recipe)) {
                recipesStates.put(recipe, state);
                sendNetworkGuiUpdate(NET_GUI_DATA);
            }
        }
    }

    @Nullable
    private static EnumAssemblyRecipeState readState(PacketBufferBC buffer) {
        int ordinal = buffer.readInt();
        EnumAssemblyRecipeState[] values = EnumAssemblyRecipeState.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : null;
    }

    /** Client side: asks the server to save (or forget) a recipe. */
    public void sendRecipeStateToServer(AssemblyInstruction instruction, EnumAssemblyRecipeState state) {
        ResourceLocation name = instruction.recipe.getRegistryName();
        if (name == null) {
            return;
        }
        PacketDistributor.sendToServer(createMessage(NET_RECIPE_STATE, buffer -> {
            buffer.writeUtf(name.toString());
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, instruction.output);
            buffer.writeInt(state.ordinal());
        }));
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        super.getDebugInfo(left, right, side);
        left.add("recipes - " + recipesStates.size());
    }

    @Nullable
    private AssemblyInstruction lookupRecipe(String name, ItemStack output) {
        ResourceLocation id = ResourceLocation.tryParse(name);
        if (id == null || level == null) {
            return null;
        }
        AssemblyRecipe recipe = AssemblyRecipeRegistry.get(level, id);
        return recipe != null ? new AssemblyInstruction(recipe, output) : null;
    }

    /** A recipe and one of its outputs. */
    public static final class AssemblyInstruction implements Comparable<AssemblyInstruction> {
        public final AssemblyRecipe recipe;
        public final ItemStack output;

        private AssemblyInstruction(AssemblyRecipe recipe, ItemStack output) {
            this.recipe = recipe;
            this.output = output;
        }

        @Override
        public int compareTo(AssemblyInstruction o) {
            int c = recipe.compareTo(o.recipe);
            if (c != 0) {
                return c;
            }
            return outputKey().compareTo(o.outputKey());
        }

        private String outputKey() {
            return output.getItem() + "@" + output.getComponentsPatch() + "x" + output.getCount();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof AssemblyInstruction instruction)) return false;
            return Objects.equals(recipe.getRegistryName(), instruction.recipe.getRegistryName())
                && ItemStack.matches(output, instruction.output);
        }

        @Override
        public int hashCode() {
            return Objects.hash(recipe.getRegistryName(), ItemStack.hashItemAndComponents(output), output.getCount());
        }
    }
}
