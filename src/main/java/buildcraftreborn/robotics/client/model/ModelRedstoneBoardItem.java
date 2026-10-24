/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.client.model;

import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;

import buildcraftreborn.api.boards.RedstoneBoardNBT;
import buildcraftreborn.api.boards.RedstoneBoardRegistry;
import buildcraftreborn.robotics.boards.ImplRedstoneBoardRegistry;
import buildcraftreborn.robotics.item.ItemRedstoneBoard;

/** The redstone board item shows the model its board asks for ({@link RedstoneBoardNBT#getItemModelLocation()}),
 * so addon boards can have their own (1.12's mesh definition). */
public class ModelRedstoneBoardItem extends BakedModelWrapper<BakedModel> {
    private static final ItemOverrides OVERRIDES = new ItemOverrides() {
        @Override
        public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level,
            @Nullable LivingEntity entity, int seed) {
            ResourceLocation location = ItemRedstoneBoard.getBoardNBT(stack).getItemModelLocation();
            return Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(location));
        }
    };

    public ModelRedstoneBoardItem(BakedModel original) {
        super(original);
    }

    @Override
    public ItemOverrides getOverrides() {
        return OVERRIDES;
    }

    /** Every model a board might ask for, so they're loaded with the item models. */
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        Set<ResourceLocation> locations = ((ImplRedstoneBoardRegistry) RedstoneBoardRegistry.instance)
            .getEveryBoardNBT().stream().map(RedstoneBoardNBT::getItemModelLocation).collect(Collectors.toSet());
        locations.add(RedstoneBoardRegistry.instance.getEmptyRobotBoard().getItemModelLocation());
        for (ResourceLocation location : locations) {
            event.register(ModelResourceLocation.standalone(location));
        }
    }
}
