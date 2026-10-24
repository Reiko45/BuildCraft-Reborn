/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.robotics.client;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;

import buildcraftreborn.api.transport.pipe.PipeApiClient;
import buildcraftreborn.lib.client.model.ModelLazy;
import buildcraftreborn.lib.client.model.ModelPluggableItem;
import buildcraftreborn.robotics.BCRoboticsBlocks;
import buildcraftreborn.robotics.BCRoboticsEntities;
import buildcraftreborn.robotics.BCRoboticsItems;
import buildcraftreborn.robotics.client.model.KeyPlugRobotStation;
import buildcraftreborn.robotics.client.model.ModelRedstoneBoardItem;
import buildcraftreborn.robotics.client.model.PlugBakerRobotStation;
import buildcraftreborn.robotics.client.gui.GuiChargingTable;
import buildcraftreborn.robotics.client.gui.GuiProgrammingTable;
import buildcraftreborn.robotics.client.gui.GuiRequester;
import buildcraftreborn.robotics.client.gui.GuiZonePlanner;
import buildcraftreborn.robotics.client.render.RenderRobot;
import buildcraftreborn.robotics.client.render.RenderZonePlanner;
import buildcraftreborn.robotics.client.render.RobotItemRenderer;
import buildcraftreborn.robotics.client.render.RobotModels;
import buildcraftreborn.robotics.client.render.ZonePlannerMapRenderer;
import buildcraftreborn.robotics.zone.ZonePlannerMapChunkKey;
import buildcraftreborn.robotics.station.PluggableRobotStation.EnumRobotStationState;
import buildcraftreborn.robotics.zone.ZonePlannerMapDataClient;

/** Client side of the robotics module: the screens, the zone planner's map, the robot (entity, item and particles),
 * the redstone board models and the robot station. */
public final class BCRoboticsClient {
    private static long mapLevelTime = Long.MIN_VALUE;
    private static int mapLevel;

    private BCRoboticsClient() {}

    public static void init(IEventBus modBus) {
        PlugBakerRobotStation.init();
        PipeApiClient.registry.registerBaker(KeyPlugRobotStation.class, PlugBakerRobotStation.INSTANCE);

        modBus.addListener(BCRoboticsClient::registerScreens);
        modBus.addListener(BCRoboticsClient::registerRenderers);
        modBus.addListener(RobotModels::registerLayers);
        modBus.addListener(BCRoboticsClient::registerParticles);
        modBus.addListener(BCRoboticsClient::registerClientExtensions);
        modBus.addListener(ModelRedstoneBoardItem::registerModels);
        modBus.addListener(BCRoboticsClient::onModifyBakingResult);
        modBus.addListener(BCRoboticsClient::onBakingCompleted);
        NeoForge.EVENT_BUS.addListener(BCRoboticsClient::onClientLogout);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(BCRoboticsBlocks.ZONE_PLANNER_MENU.get(), GuiZonePlanner::new);
        event.register(BCRoboticsBlocks.REQUESTER_MENU.get(), GuiRequester::new);
        event.register(BCRoboticsBlocks.CHARGING_TABLE_MENU.get(), GuiChargingTable::new);
        event.register(BCRoboticsBlocks.PROGRAMMING_TABLE_MENU.get(), GuiProgrammingTable::new);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BCRoboticsBlocks.ZONE_PLANNER_TILE.get(), RenderZonePlanner::new);
        event.registerEntityRenderer(BCRoboticsEntities.ROBOT.get(), RenderRobot::new);
    }

    private static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(BCRoboticsEntities.ROBOT_ENERGY.get(), RobotEnergyParticle::provider);
    }

    private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(RobotItemRenderer.EXTENSIONS, BCRoboticsItems.ROBOT.get());
    }

    private static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        ModelResourceLocation board = ModelResourceLocation.inventory(BCRoboticsItems.REDSTONE_BOARD.getId());
        BakedModel original = models.get(board);
        if (original != null) {
            models.put(board, new ModelRedstoneBoardItem(original));
        }
        models.put(ModelResourceLocation.inventory(BCRoboticsItems.ROBOT_STATION.getId()), new ModelLazy(
            () -> new ModelPluggableItem(PlugBakerRobotStation.getQuads(EnumRobotStationState.AVAILABLE))));
    }

    private static void onBakingCompleted(ModelEvent.BakingCompleted event) {
        PlugBakerRobotStation.onModelBake();
    }

    private static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ZonePlannerMapDataClient.INSTANCE.clear();
        ZonePlannerMapRenderer.INSTANCE.clear();
        RenderZonePlanner.clear();
    }

    /** The map level that the player sees from: the first block above them that is solid underneath (or the top of
     * the world), in steps of {@link ZonePlannerMapChunkKey#LEVEL_HEIGHT} blocks. Worked out once a tick (1.12:
     * every time it was asked for). */
    public static int getMapLevel() {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null || mc.player == null) {
            return 0;
        }
        long time = level.getGameTime();
        if (time != mapLevelTime) {
            mapLevelTime = time;
            BlockPos.MutableBlockPos pos = mc.player.blockPosition().mutable();
            int top = level.getMaxBuildHeight() - 1;
            while (pos.getY() < top && !level.getBlockState(pos).isFaceSturdy(level, pos, Direction.DOWN)) {
                pos.move(Direction.UP);
            }
            mapLevel = Math.floorDiv(pos.getY(), ZonePlannerMapChunkKey.LEVEL_HEIGHT);
        }
        return mapLevel;
    }

    /** @return The particle setting (0 all, 1 decreased, 2 minimal). */
    public static int getParticleSetting() {
        return Minecraft.getInstance().options.particles().get().getId();
    }

    /** Spawns the red puff a robot gives off as it uses power. */
    public static void spawnEnergyParticle(Level level, double x, double y, double z, double vx, double vy, double vz,
        float size) {
        RobotEnergyParticle.spawn(level, x, y, z, vx, vy, vz, size);
    }
}
