/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.item;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import buildcraftreborn.api.facades.FacadeType;
import buildcraftreborn.api.facades.IFacade;
import buildcraftreborn.api.facades.IFacadeItem;
import buildcraftreborn.api.transport.IItemPluggable;
import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.api.transport.pluggable.PipePluggable;
import buildcraftreborn.lib.item.IItemSubtypes;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.lib.misc.SoundUtil;
import buildcraftreborn.silicon.BCSiliconItems;
import buildcraftreborn.silicon.BCSiliconPlugs;
import buildcraftreborn.silicon.plug.FacadeBlockStateInfo;
import buildcraftreborn.silicon.plug.FacadeInstance;
import buildcraftreborn.silicon.plug.FacadePhasedState;
import buildcraftreborn.silicon.plug.FacadeStateManager;
import buildcraftreborn.silicon.plug.PluggableFacade;

/** The facade item. What it looks like is its {@code facade} data component (1.12: the {@code facade} NBT tag). */
public class ItemPluggableFacade extends Item implements IItemPluggable, IFacadeItem, IItemSubtypes {
    public ItemPluggableFacade(Properties properties) {
        super(properties);
    }

    public ItemStack createItemStack(FacadeInstance state) {
        ItemStack item = new ItemStack(this);
        item.set(BCSiliconItems.FACADE.get(), state);
        return item;
    }

    public static FacadeInstance getStates(ItemStack item) {
        FacadeInstance facade = item.get(BCSiliconItems.FACADE.get());
        return facade == null ? FacadeInstance.createSingle(FacadeStateManager.previewState(), false) : facade;
    }

    @Override
    public ItemStack getFacadeForBlock(BlockState state) {
        FacadeBlockStateInfo info = FacadeStateManager.validFacadeStates.get(state);
        if (info == null) {
            return ItemStack.EMPTY;
        }
        return createItemStack(FacadeInstance.createSingle(info, false));
    }

    @Override
    public PipePluggable onPlace(ItemStack stack, IPipeHolder holder, Direction side, Player player,
        InteractionHand hand) {
        FacadeInstance fullState = getStates(stack);
        SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos(),
            fullState.phasedStates[0].stateInfo.state);
        return new PluggableFacade(BCSiliconPlugs.facade, holder, side, fullState);
    }

    /** A phased facade to show what they are, then a solid and a hollow facade of every block that can be one. */
    @Override
    public void addSubtypes(Consumer<ItemStack> output) {
        FacadeBlockStateInfo stone = FacadeStateManager.getInfoForBlock(Blocks.STONE);
        FacadeBlockStateInfo planks = FacadeStateManager.getInfoForBlock(Blocks.OAK_PLANKS);
        FacadeBlockStateInfo log = FacadeStateManager.getInfoForBlock(Blocks.OAK_LOG);
        if (stone == null || planks == null || log == null) {
            return;
        }
        output.accept(createItemStack(new FacadeInstance(new FacadePhasedState[] { stone.createPhased(null),
            planks.createPhased(DyeColor.RED), log.createPhased(DyeColor.CYAN) }, false)));
        for (FacadeBlockStateInfo info : FacadeStateManager.validFacadeStates.values()) {
            if (info.isVisible) {
                output.accept(createItemStack(FacadeInstance.createSingle(info, false)));
                output.accept(createItemStack(FacadeInstance.createSingle(info, true)));
            }
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        FacadeInstance fullState = getStates(stack);
        if (fullState.type == FacadeType.Basic) {
            return Component.translatable(getDescriptionId()).append(": ")
                .append(getFacadeStateDisplayName(fullState.phasedStates[0]));
        }
        return Component.translatable("item.FacadePhased.name");
    }

    public static Component getFacadeStateDisplayName(FacadePhasedState state) {
        ItemStack assumedStack = state.stateInfo.requiredStack;
        return assumedStack.isEmpty() ? state.stateInfo.state.getBlock().getName() : assumedStack.getHoverName();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        FacadeInstance states = getStates(stack);
        if (states.type == FacadeType.Phased) {
            FacadePhasedState defaultState = null;
            for (FacadePhasedState state : states.phasedStates) {
                if (state.activeColour == null) {
                    defaultState = state;
                    continue;
                }
                tooltip.add(Component.translatable("item.FacadePhased.state",
                    LocaleUtil.localizeColour(state.activeColour), getFacadeStateDisplayName(state)));
            }
            if (defaultState != null) {
                tooltip.add(Math.min(1, tooltip.size()), Component.translatable("item.FacadePhased.state_default",
                    getFacadeStateDisplayName(defaultState)));
            }
        } else {
            FacadeBlockStateInfo info = states.phasedStates[0].stateInfo;
            if (flag.isAdvanced()) {
                tooltip.add(Component.literal(BuiltInRegistries.BLOCK.getKey(info.state.getBlock()).toString())
                    .withStyle(ChatFormatting.DARK_GRAY));
            }
            for (Map.Entry<Property<?>, Comparable<?>> entry : info.state.getValues().entrySet()) {
                if (info.varyingProperties.contains(entry.getKey())) {
                    tooltip.add(Component.literal(entry.getKey().getName() + " = " + valueName(entry.getKey(),
                        entry.getValue())).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String valueName(Property<T> property, Comparable<?> value) {
        return property.getName((T) value);
    }

    // IFacadeItem

    @Override
    public ItemStack createFacadeStack(IFacade facade) {
        return createItemStack((FacadeInstance) facade);
    }

    @Override
    public IFacade getFacade(ItemStack facade) {
        return getStates(facade);
    }
}
