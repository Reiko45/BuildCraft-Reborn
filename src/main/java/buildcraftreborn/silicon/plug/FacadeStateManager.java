/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.plug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableSet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import net.neoforged.fml.InterModComms;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.facades.FacadeAPI;
import buildcraftreborn.api.facades.IFacade;
import buildcraftreborn.api.facades.IFacadePhasedState;
import buildcraftreborn.api.facades.IFacadeRegistry;
import buildcraftreborn.api.facades.IFacadeState;

/** Works out which block states can be facades: every state with a normal full-cube model and no block entity (and
 * every state of glass blocks), unless another mod disabled the block. Runs once the mods have sent their facade
 * messages (see {@link FacadeAPI}), on both the client and the server. */
public enum FacadeStateManager implements IFacadeRegistry {
    INSTANCE;

    public static final boolean DEBUG = Boolean.getBoolean("buildcraftreborn.debug.facade");
    /** Every facade state, in block registry order. */
    public static final Map<BlockState, FacadeBlockStateInfo> validFacadeStates = new LinkedHashMap<>();
    /** The facade states each item makes. */
    public static final Map<Item, List<FacadeBlockStateInfo>> stackFacades = new HashMap<>();
    @Nullable
    private static FacadeBlockStateInfo defaultState;
    @Nullable
    private static FacadeBlockStateInfo previewState;

    private static final Map<Block, String> disabledBlocks = new HashMap<>();
    private static final Map<BlockState, ItemStack> customBlocks = new HashMap<>();

    /** The empty facade (air): what a facade whose block went missing turns into. */
    public static FacadeBlockStateInfo defaultState() {
        FacadeBlockStateInfo info = defaultState;
        if (info == null) {
            info = defaultState = new FacadeBlockStateInfo(Blocks.AIR.defaultBlockState(), ItemStack.EMPTY,
                ImmutableSet.of());
        }
        return info;
    }

    /** The facade the creative tab icon (and an item without a {@code facade} component) shows: bricks. */
    public static FacadeBlockStateInfo previewState() {
        FacadeBlockStateInfo info = previewState;
        return info == null ? defaultState() : info;
    }

    public static FacadeBlockStateInfo getInfo(BlockState state) {
        FacadeBlockStateInfo info = validFacadeStates.get(state);
        return info == null ? defaultState() : info;
    }

    @Nullable
    public static FacadeBlockStateInfo getInfoForBlock(Block block) {
        return validFacadeStates.get(block.defaultBlockState());
    }

    /** Reads the facade messages sent with {@link FacadeAPI#disableBlock} and {@link FacadeAPI#mapStateToStack}. */
    public static void receiveInterModComms() {
        InterModComms.getMessages(BuildCraftReborn.MODID, FacadeAPI::isFacadeMessageId).forEach(message -> {
            String id = message.method();
            Object value = message.messageSupplier().get();
            if (FacadeAPI.IMC_FACADE_DISABLE.equals(id)) {
                if (!(value instanceof ResourceLocation loc)) {
                    BuildCraftReborn.LOGGER.warn("[facade.imc] Received an invalid IMC message from {} - {} should "
                        + "have a ResourceLocation value, not {}", message.senderModId(), id, value);
                    return;
                }
                Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(loc);
                if (block.isEmpty() || block.get() == Blocks.AIR) {
                    BuildCraftReborn.LOGGER.warn("[facade.imc] Received an invalid IMC message from {} - {} should "
                        + "have a valid block target, not {}", message.senderModId(), id, loc);
                    return;
                }
                disabledBlocks.put(block.get(), message.senderModId());
            } else if (FacadeAPI.IMC_FACADE_CUSTOM.equals(id)) {
                if (!(value instanceof CompoundTag nbt)) {
                    BuildCraftReborn.LOGGER.warn("[facade.imc] Received an invalid IMC message from {} - {} should "
                        + "have an nbt value, not {}", message.senderModId(), id, value);
                    return;
                }
                BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(),
                    nbt.getCompound(FacadeAPI.NBT_CUSTOM_BLOCK_STATE));
                Optional<Item> item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.tryParse(
                    nbt.getString(FacadeAPI.NBT_CUSTOM_ITEM)));
                if (state.isAir() || item.isEmpty() || item.get() == Items.AIR) {
                    BuildCraftReborn.LOGGER.warn("[facade.imc] Received an invalid IMC message from {} - {} should "
                        + "have a block state and an item, not {}", message.senderModId(), id, nbt);
                    return;
                }
                customBlocks.put(state, new ItemStack(item.get()));
            }
        });
    }

    /** @return null if every state of the block can be a facade, "" if each state has to be checked, or why the block
     *         can't be one. */
    @Nullable
    private static String isValidFacadeBlock(Block block) {
        String disablingMod = disabledBlocks.get(block);
        if (disablingMod != null) {
            return "it has been disabled by " + disablingMod;
        }
        if (block instanceof LiquidBlock) {
            return "it is a fluid block";
        }
        if (block instanceof TransparentBlock) {
            // Glass (1.12: BlockGlass and BlockStainedGlass)
            return null;
        }
        return "";
    }

    /** @return null if the state can be a facade, or why not. */
    @Nullable
    private static String isValidFacadeState(BlockState state) {
        if (state.hasBlockEntity()) {
            return "it has a block entity";
        }
        if (state.getRenderShape() != RenderShape.MODEL) {
            return "it doesn't have a normal model";
        }
        if (!Block.isShapeFullBlock(state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO))) {
            return "it isn't a full cube";
        }
        return null;
    }

    private static ItemStack getRequiredStack(BlockState state) {
        ItemStack stack = customBlocks.get(state);
        if (stack != null) {
            return stack;
        }
        Item item = state.getBlock().asItem();
        // 1.12 fell back to the block's drop; loot tables aren't loaded this early, so those facades can't be made
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    public static void init() {
        validFacadeStates.clear();
        stackFacades.clear();
        for (Block block : BuiltInRegistries.BLOCK) {
            scanBlock(block);
        }
        FacadeBlockStateInfo bricks = validFacadeStates.get(Blocks.BRICKS.defaultBlockState());
        previewState = bricks == null ? defaultState() : bricks;
        BuildCraftReborn.LOGGER.info("[silicon.facade] {} block states can be facades", validFacadeStates.size());
    }

    private static void scanBlock(Block block) {
        ResourceLocation name = BuiltInRegistries.BLOCK.getKey(block);
        try {
            String blockResult = isValidFacadeBlock(block);
            if (blockResult != null && !blockResult.isEmpty()) {
                if (DEBUG) {
                    BuildCraftReborn.LOGGER.info("[silicon.facade] Disallowed block {} because {}", name, blockResult);
                }
                return;
            }
            Map<BlockState, ItemStack> usedStates = new LinkedHashMap<>();
            Map<Item, Map<Property<?>, Comparable<?>>> varyingProperties = new HashMap<>();
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                if (blockResult != null) {
                    String stateResult;
                    try {
                        stateResult = isValidFacadeState(state);
                    } catch (RuntimeException e) {
                        stateResult = "it threw " + e;
                    }
                    if (stateResult != null) {
                        if (DEBUG) {
                            BuildCraftReborn.LOGGER.info("[silicon.facade] Disallowed state {} because {}", state,
                                stateResult);
                        }
                        continue;
                    }
                }
                ItemStack requiredStack = getRequiredStack(state);
                usedStates.put(state, requiredStack);
                Map<Property<?>, Comparable<?>> vars = varyingProperties.get(requiredStack.getItem());
                if (vars == null) {
                    varyingProperties.put(requiredStack.getItem(), new HashMap<>(state.getValues()));
                } else {
                    for (Entry<Property<?>, Comparable<?>> entry : state.getValues().entrySet()) {
                        if (vars.get(entry.getKey()) != entry.getValue()) {
                            vars.put(entry.getKey(), null);
                        }
                    }
                }
            }
            // The properties left null are the ones that vary
            varyingProperties.values().forEach(vars -> vars.values().removeIf(Objects::nonNull));
            for (Entry<BlockState, ItemStack> entry : usedStates.entrySet()) {
                BlockState state = entry.getKey();
                ItemStack stack = entry.getValue();
                Map<Property<?>, Comparable<?>> vars = varyingProperties.get(stack.getItem());
                FacadeBlockStateInfo info = new FacadeBlockStateInfo(state, stack, ImmutableSet.copyOf(vars.keySet()));
                validFacadeStates.put(state, info);
                if (!info.requiredStack.isEmpty()) {
                    stackFacades.computeIfAbsent(info.requiredStack.getItem(), k -> new ArrayList<>()).add(info);
                }
                if (DEBUG) {
                    BuildCraftReborn.LOGGER.info("[silicon.facade]   Added {}", info);
                }
            }
        } catch (RuntimeException e) {
            BuildCraftReborn.LOGGER.warn("[silicon.facade] Skipping {} as something about it threw an exception!",
                name, e);
        }
    }

    // IFacadeRegistry

    @Override
    public Collection<? extends IFacadeState> getValidFacades() {
        return validFacadeStates.values();
    }

    @Override
    public IFacadePhasedState createPhasedState(IFacadeState state, @Nullable DyeColor activeColor) {
        return new FacadePhasedState((FacadeBlockStateInfo) state, activeColor);
    }

    @Override
    public IFacade createPhasedFacade(IFacadePhasedState[] states, boolean isHollow) {
        FacadePhasedState[] realStates = new FacadePhasedState[states.length];
        for (int i = 0; i < states.length; i++) {
            realStates[i] = (FacadePhasedState) states[i];
        }
        return new FacadeInstance(realStates, isHollow);
    }
}
