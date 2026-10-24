/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.Fluid;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.item.ItemBlockBC_Neptune;
import buildcraftreborn.lib.recipe.ConfigCondition;
import buildcraftreborn.lib.tile.TileBC_Neptune;

/**
 * Every registry BuildCraft Reborn writes to. Replaces 1.12's {@code RegistrationHelper} and
 * {@code GameRegistry.registerTileEntity}. Modules add entries to these from their own classes (for example
 * {@code BCCoreItems}); this class only owns the registers and attaches them to the mod bus.
 */
public final class BCRegistries {
    private static final String MODID = BuildCraftReborn.MODID;

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MODID);
    /** Replaces item NBT and metadata from 1.12 (pipe colour, facade state, list filters, gate variant, ...). */
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
        DeferredRegister.create(Registries.RECIPE_TYPE, MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, MODID);
    public static final DeferredRegister<FluidType> FLUID_TYPES =
        DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    /** World generation features (oil wells). Their configured and placed forms are data, see
     * {@code data/buildcraftreborn/worldgen}. */
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(Registries.ENTITY_TYPE, MODID);
    public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS =
        DeferredRegister.create(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, MODID);
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
        DeferredRegister.create(Registries.PARTICLE_TYPE, MODID);
    /** Data conditions ({@code buildcraftreborn:config}). */
    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS =
        DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, MODID);

    static {
        CONDITION_CODECS.register("config", () -> ConfigCondition.CODEC);
    }

    /** Every BuildCraft tile type, so {@code BCCapabilities} can register capability providers for them. */
    public static final List<DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<? extends TileBC_Neptune>>>
        TILE_TYPES = new ArrayList<>();

    /** Items shown in a creative tab of their own (pipes, plugs...) rather than the main tab. */
    public static final Set<ResourceLocation> NOT_IN_MAIN_TAB = new HashSet<>();

    /** Items left out of the creative tabs while the named config switch (see {@link ConfigCondition}) is off. */
    public static final Map<ResourceLocation, String> CREATIVE_TAB_SWITCHES = new HashMap<>();

    /** @return Whether the item should be shown in the creative tabs (its config switch, if any, is on). */
    public static boolean isShownInCreative(ResourceLocation item) {
        String flag = CREATIVE_TAB_SWITCHES.get(item);
        return flag == null || ConfigCondition.isEnabled(flag);
    }

    private BCRegistries() {}

    /** Registers a BuildCraft tile type for the given blocks (replaces 1.12's {@code registerTile}). */
    @SafeVarargs
    public static <T extends TileBC_Neptune> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> registerTile(
        String name, BlockEntityType.BlockEntitySupplier<T> factory, Supplier<? extends Block>... blocks) {
        DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> holder = BLOCK_ENTITY_TYPES.register(name,
            () -> BlockEntityType.Builder.of(factory, Arrays.stream(blocks).map(Supplier::get).toArray(Block[]::new))
                .build(null));
        TILE_TYPES.add(holder);
        return holder;
    }

    /** Registers the item for a block, named like the block, with {@link ItemBlockBC_Neptune} (which shows the
     * block's {@code .tip} tooltip). */
    public static DeferredItem<ItemBlockBC_Neptune> registerBlockItem(DeferredBlock<? extends Block> block) {
        return ITEMS.register(block.getId().getPath(), () -> new ItemBlockBC_Neptune(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITY_TYPES.register(modBus);
        MENU_TYPES.register(modBus);
        DATA_COMPONENTS.register(modBus);
        RECIPE_TYPES.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        FLUID_TYPES.register(modBus);
        FLUIDS.register(modBus);
        CREATIVE_TABS.register(modBus);
        FEATURES.register(modBus);
        CONDITION_CODECS.register(modBus);
        ENTITY_TYPES.register(modBus);
        ENTITY_DATA_SERIALIZERS.register(modBus);
        PARTICLE_TYPES.register(modBus);
    }
}
