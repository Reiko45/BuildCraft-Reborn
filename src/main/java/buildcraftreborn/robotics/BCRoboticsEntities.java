/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics;

import java.util.List;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import buildcraftreborn.lib.BCRegistries;
import buildcraftreborn.robotics.entity.EntityRobot;
import buildcraftreborn.robotics.item.ItemRobot;

/** The robot entity (1.12: {@code bcRobot}), what it syncs to clients and its particles. */
public final class BCRoboticsEntities {

    /** A list of stacks (a robot's inventory and wearables), copied whenever it is set. It exists before it is
     * registered, as entity data has to be defined while the robot's class loads (and the data array is sized). */
    public static final EntityDataSerializer<List<ItemStack>> ITEM_LIST_SERIALIZER = new EntityDataSerializer<>() {
        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, List<ItemStack>> codec() {
            return ItemStack.OPTIONAL_LIST_STREAM_CODEC;
        }

        @Override
        public List<ItemStack> copy(List<ItemStack> value) {
            return value.stream().map(ItemStack::copy).toList();
        }
    };
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<List<ItemStack>>> ITEM_LIST =
        BCRegistries.ENTITY_DATA_SERIALIZERS.register("item_list", () -> ITEM_LIST_SERIALIZER);

    public static final DeferredHolder<EntityType<?>, EntityType<EntityRobot>> ROBOT =
        BCRegistries.ENTITY_TYPES.register("robot", () -> EntityType.Builder.<EntityRobot> of(EntityRobot::new,
            MobCategory.MISC).sized(0.5F, 0.5F).eyeHeight(0.25F).fireImmune().clientTrackingRange(8).updateInterval(1)
            .build("buildcraftreborn:robot"));

    /** The red puffs a robot gives off as it uses power. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ROBOT_ENERGY =
        BCRegistries.PARTICLE_TYPES.register("robot_energy", () -> new SimpleParticleType(false));

    private BCRoboticsEntities() {}

    static void init(IEventBus modBus) {
        modBus.addListener(BCRoboticsEntities::onAttributes);
        modBus.addListener(BCRoboticsEntities::onCapabilities);
    }

    private static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(ROBOT.get(), Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20)
            .add(Attributes.ATTACK_DAMAGE, 2).add(Attributes.FOLLOW_RANGE, 0).build());
    }

    private static void onCapabilities(RegisterCapabilitiesEvent event) {
        event.registerEntity(Capabilities.ItemHandler.ENTITY, ROBOT.get(), (robot, ctx) -> robot);
        event.registerEntity(Capabilities.ItemHandler.ENTITY_AUTOMATION, ROBOT.get(), (robot, ctx) -> robot);
        event.registerEntity(Capabilities.FluidHandler.ENTITY, ROBOT.get(), (robot, ctx) -> robot);
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, ctx) -> ItemRobot.createEnergyStorage(stack),
            BCRoboticsItems.ROBOT.get());
    }
}
