/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics;

import java.util.function.Supplier;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.boards.RedstoneBoardNBT;
import buildcraftreborn.api.boards.RedstoneBoardRegistry;
import buildcraftreborn.api.boards.RedstoneBoardRobot;
import buildcraftreborn.api.boards.RedstoneBoardRobotNBT;
import buildcraftreborn.api.core.BuildCraftAPI;
import buildcraftreborn.api.crops.CropManager;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.recipes.BuildcraftRecipeRegistry;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.api.robots.RobotManager;
import buildcraftreborn.lib.BCRegistries;
import buildcraftreborn.lib.item.IItemSubtypes;
import buildcraftreborn.lib.recipe.IntegrationRecipeRegistry;
import buildcraftreborn.robotics.ai.AIRobotAttack;
import buildcraftreborn.robotics.ai.AIRobotBreak;
import buildcraftreborn.robotics.ai.AIRobotDeliverRequested;
import buildcraftreborn.robotics.ai.AIRobotDisposeItems;
import buildcraftreborn.robotics.ai.AIRobotFetchAndEquipItemStack;
import buildcraftreborn.robotics.ai.AIRobotFetchItem;
import buildcraftreborn.robotics.ai.AIRobotGoAndLinkToDock;
import buildcraftreborn.robotics.ai.AIRobotGotoBlock;
import buildcraftreborn.robotics.ai.AIRobotGotoSleep;
import buildcraftreborn.robotics.ai.AIRobotGotoStation;
import buildcraftreborn.robotics.ai.AIRobotGotoStationAndLoad;
import buildcraftreborn.robotics.ai.AIRobotGotoStationAndLoadFluids;
import buildcraftreborn.robotics.ai.AIRobotGotoStationAndUnload;
import buildcraftreborn.robotics.ai.AIRobotGotoStationAndUnloadFluids;
import buildcraftreborn.robotics.ai.AIRobotGotoStationToLoad;
import buildcraftreborn.robotics.ai.AIRobotGotoStationToLoadFluids;
import buildcraftreborn.robotics.ai.AIRobotGotoStationToUnload;
import buildcraftreborn.robotics.ai.AIRobotGotoStationToUnloadFluids;
import buildcraftreborn.robotics.ai.AIRobotHarvest;
import buildcraftreborn.robotics.ai.AIRobotLoad;
import buildcraftreborn.robotics.ai.AIRobotLoadFluids;
import buildcraftreborn.robotics.ai.AIRobotMain;
import buildcraftreborn.robotics.ai.AIRobotPlant;
import buildcraftreborn.robotics.ai.AIRobotPumpBlock;
import buildcraftreborn.robotics.ai.AIRobotRecharge;
import buildcraftreborn.robotics.ai.AIRobotSearchAndGotoBlock;
import buildcraftreborn.robotics.ai.AIRobotSearchAndGotoStation;
import buildcraftreborn.robotics.ai.AIRobotSearchBlock;
import buildcraftreborn.robotics.ai.AIRobotSearchEntity;
import buildcraftreborn.robotics.ai.AIRobotSearchRandomGroundBlock;
import buildcraftreborn.robotics.ai.AIRobotSearchStackRequest;
import buildcraftreborn.robotics.ai.AIRobotSearchStation;
import buildcraftreborn.robotics.ai.AIRobotShutdown;
import buildcraftreborn.robotics.ai.AIRobotSleep;
import buildcraftreborn.robotics.ai.AIRobotStraightMoveTo;
import buildcraftreborn.robotics.ai.AIRobotStripesHandler;
import buildcraftreborn.robotics.ai.AIRobotUnload;
import buildcraftreborn.robotics.ai.AIRobotUnloadFluids;
import buildcraftreborn.robotics.ai.AIRobotUseToolOnBlock;
import buildcraftreborn.robotics.boards.BCBoardNBT;
import buildcraftreborn.robotics.boards.BoardRobotBomber;
import buildcraftreborn.robotics.boards.BoardRobotBuilder;
import buildcraftreborn.robotics.boards.BoardRobotButcher;
import buildcraftreborn.robotics.boards.BoardRobotCarrier;
import buildcraftreborn.robotics.boards.BoardRobotDelivery;
import buildcraftreborn.robotics.boards.BoardRobotEmpty;
import buildcraftreborn.robotics.boards.BoardRobotFarmer;
import buildcraftreborn.robotics.boards.BoardRobotFluidCarrier;
import buildcraftreborn.robotics.boards.BoardRobotHarvester;
import buildcraftreborn.robotics.boards.BoardRobotKnight;
import buildcraftreborn.robotics.boards.BoardRobotLeaveCutter;
import buildcraftreborn.robotics.boards.BoardRobotLumberjack;
import buildcraftreborn.robotics.boards.BoardRobotMiner;
import buildcraftreborn.robotics.boards.BoardRobotPicker;
import buildcraftreborn.robotics.boards.BoardRobotPlanter;
import buildcraftreborn.robotics.boards.BoardRobotPump;
import buildcraftreborn.robotics.boards.BoardRobotShovelman;
import buildcraftreborn.robotics.boards.BoardRobotStripes;
import buildcraftreborn.robotics.boards.ImplRedstoneBoardRegistry;
import buildcraftreborn.robotics.boards.RedstoneBoardRobotEmptyNBT;
import buildcraftreborn.robotics.crops.CropHandlerPlantable;
import buildcraftreborn.robotics.crops.CropHandlerReeds;
import buildcraftreborn.robotics.item.ItemRobot;
import buildcraftreborn.robotics.properties.WorldPropertyIsDirt;
import buildcraftreborn.robotics.properties.WorldPropertyIsFarmland;
import buildcraftreborn.robotics.properties.WorldPropertyIsFluidSource;
import buildcraftreborn.robotics.properties.WorldPropertyIsFreePath;
import buildcraftreborn.robotics.properties.WorldPropertyIsHarvestable;
import buildcraftreborn.robotics.properties.WorldPropertyIsLeaf;
import buildcraftreborn.robotics.properties.WorldPropertyIsOre;
import buildcraftreborn.robotics.properties.WorldPropertyIsReplaceable;
import buildcraftreborn.robotics.properties.WorldPropertyIsShoveled;
import buildcraftreborn.robotics.properties.WorldPropertyIsSoft;
import buildcraftreborn.robotics.properties.WorldPropertyIsWood;
import buildcraftreborn.robotics.recipe.BoardProgrammingRecipe;
import buildcraftreborn.robotics.recipe.ProgrammingRecipeManager;
import buildcraftreborn.robotics.recipe.RobotIntegrationRecipe;
import buildcraftreborn.robotics.station.DockingStationPipe;
import buildcraftreborn.robotics.util.IterableAlgorithmRunner;
import buildcraftreborn.robotics.util.PathFindingSearch;
import buildcraftreborn.robotics.zone.ZonePlannerMapDataServer;
import buildcraftreborn.transport.BCTransportItems;

/** Robotics module (1.12's {@code buildcraftrobotics} mod): the zone planner from BuildCraft 8.0, and robots, their
 * boards, robot stations and the requester from BuildCraft 7.1 (which 8.0 never ported). Initialised from
 * {@code BuildCraftReborn}'s constructor. */
public final class BCRobotics {
    public static final String TAB_ROBOTS_KEY = "itemGroup.buildcraftreborn.robots";

    /** Robots and redstone boards, programmed and not (7.1's "buildcraft.boards" tab). */
    public static final Supplier<CreativeModeTab> TAB_ROBOTS = BCRegistries.CREATIVE_TABS.register("robots",
        () -> CreativeModeTab.builder()
            .title(Component.translatable(TAB_ROBOTS_KEY))
            .icon(BCRobotics::createTabIcon)
            .displayItems((params, output) -> {
                IItemSubtypes.accept(output, BCRoboticsItems.ROBOT.get());
                IItemSubtypes.accept(output, BCRoboticsItems.REDSTONE_BOARD.get());
            })
            .build());

    private BCRobotics() {}

    private static ItemStack createTabIcon() {
        RedstoneBoardNBT<?> miner = RedstoneBoardRegistry.instance.getRedstoneBoard(BuildCraftReborn.id("miner")
            .toString());
        return miner instanceof RedstoneBoardRobotNBT robotNBT
            ? ItemRobot.createRobotStack(robotNBT, EntityRobotBase.MAX_POWER)
            : new ItemStack(BCRoboticsItems.ROBOT.get());
    }

    public static void init(IEventBus modBus, ModContainer container) {
        RedstoneBoardRegistry.instance = new ImplRedstoneBoardRegistry();
        RobotManager.registryProvider = RobotRegistryProvider.INSTANCE;
        BuildcraftRecipeRegistry.programmingRecipes = ProgrammingRecipeManager.INSTANCE;

        BCRoboticsBlocks.init();
        BCRoboticsItems.init();
        BCRoboticsEntities.init(modBus);
        BCRoboticsPlugs.init();
        BCRoboticsStatements.init();
        BCRegistries.NOT_IN_MAIN_TAB.add(BCRoboticsItems.ROBOT.getId());
        BCRegistries.NOT_IN_MAIN_TAB.add(BCRoboticsItems.REDSTONE_BOARD.getId());
        BCTransportItems.addToPlugTab(BCRoboticsItems.ROBOT_STATION);

        registerBoards();
        registerAIs();
        RobotManager.registerDockingStation(DockingStationPipe.class, "dockingStationPipe");
        registerWorldProperties();

        CropManager.setDefaultHandler(new CropHandlerPlantable());
        CropManager.registerHandler(new CropHandlerReeds());
        BuildCraftAPI.softBlocks.add(Blocks.SNOW);
        BuildCraftAPI.softBlocks.add(Blocks.VINE);
        BuildCraftAPI.softBlocks.add(Blocks.FIRE);

        ProgrammingRecipeManager.INSTANCE.addRecipe(new BoardProgrammingRecipe());
        IntegrationRecipeRegistry.INSTANCE.addRecipe(new RobotIntegrationRecipe());

        container.registerConfig(ModConfig.Type.COMMON, BCRoboticsConfig.SPEC,
            BuildCraftReborn.MODID + "-robotics.toml");
        modBus.addListener((ModConfigEvent.Loading event) -> BCRoboticsConfig.onConfigLoad(event));
        modBus.addListener((ModConfigEvent.Reloading event) -> BCRoboticsConfig.onConfigLoad(event));

        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> IterableAlgorithmRunner.tickAll());
        NeoForge.EVENT_BUS.addListener((ServerStartingEvent event) -> BoardRobotPicker.onServerStart());
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> {
            ZonePlannerMapDataServer.INSTANCE.clear();
            IterableAlgorithmRunner.clearAll();
            PathFindingSearch.clearReservations();
            BoardRobotPicker.targettedItems.clear();
        });
    }

    /** 7.1's boards, with 7.1's programming costs (RF converted at 10 RF to the MJ). */
    private static void registerBoards() {
        RedstoneBoardRegistry registry = RedstoneBoardRegistry.instance;
        registry.setEmptyRobotBoard(RedstoneBoardRobotEmptyNBT.instance);

        registerBoard("picker", BoardRobotPicker.class, "green", 800);
        registerBoard("carrier", BoardRobotCarrier.class, "green", 800);
        registerBoard("fluid_carrier", BoardRobotFluidCarrier.class, "green", 800);

        registerBoard("lumberjack", BoardRobotLumberjack.class, "blue", 3200);
        registerBoard("harvester", BoardRobotHarvester.class, "blue", 3200);
        registerBoard("miner", BoardRobotMiner.class, "blue", 3200);
        registerBoard("planter", BoardRobotPlanter.class, "blue", 3200);
        registerBoard("farmer", BoardRobotFarmer.class, "blue", 3200);
        registerBoard("leave_cutter", BoardRobotLeaveCutter.class, "blue", 3200);
        registerBoard("butcher", BoardRobotButcher.class, "blue", 3200);
        registerBoard("shovelman", BoardRobotShovelman.class, "blue", 3200);
        registerBoard("pump", BoardRobotPump.class, "blue", 3200);

        registerBoard("delivery", BoardRobotDelivery.class, "green", 12_800);
        registerBoard("knight", BoardRobotKnight.class, "red", 12_800);
        registerBoard("bomber", BoardRobotBomber.class, "red", 12_800);
        registerBoard("stripes", BoardRobotStripes.class, "yellow", 12_800);
        // 7.1: 512,000 RF
        registerBoard("builder", BoardRobotBuilder.class, "yellow", 51_200);
    }

    private static void registerBoard(String name, Class<? extends RedstoneBoardRobot> board, String colour,
        long mj) {
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT(name, board, colour), mj * MjAPI.MJ);
    }

    /** The names robots save their AIs under (7.1's). */
    private static void registerAIs() {
        RobotManager.registerAIRobot(AIRobotMain.class, "aiRobotMain");
        RobotManager.registerAIRobot(BoardRobotEmpty.class, "boardRobotEmpty");
        RobotManager.registerAIRobot(BoardRobotBomber.class, "boardRobotBomber");
        RobotManager.registerAIRobot(BoardRobotButcher.class, "boardRobotButcher");
        RobotManager.registerAIRobot(BoardRobotCarrier.class, "boardRobotCarrier");
        RobotManager.registerAIRobot(BoardRobotDelivery.class, "boardRobotDelivery");
        RobotManager.registerAIRobot(BoardRobotFarmer.class, "boardRobotFarmer");
        RobotManager.registerAIRobot(BoardRobotFluidCarrier.class, "boardRobotFluidCarrier");
        RobotManager.registerAIRobot(BoardRobotHarvester.class, "boardRobotHarvester");
        RobotManager.registerAIRobot(BoardRobotKnight.class, "boardRobotKnight");
        RobotManager.registerAIRobot(BoardRobotLeaveCutter.class, "boardRobotLeaveCutter");
        RobotManager.registerAIRobot(BoardRobotLumberjack.class, "boardRobotLumberjack");
        RobotManager.registerAIRobot(BoardRobotMiner.class, "boardRobotMiner");
        RobotManager.registerAIRobot(BoardRobotPicker.class, "boardRobotPicker");
        RobotManager.registerAIRobot(BoardRobotPlanter.class, "boardRobotPlanter");
        RobotManager.registerAIRobot(BoardRobotPump.class, "boardRobotPump");
        RobotManager.registerAIRobot(BoardRobotShovelman.class, "boardRobotShovelman");
        RobotManager.registerAIRobot(BoardRobotStripes.class, "boardRobotStripes");
        RobotManager.registerAIRobot(BoardRobotBuilder.class, "boardRobotBuilder");
        RobotManager.registerAIRobot(AIRobotAttack.class, "aiRobotAttack");
        RobotManager.registerAIRobot(AIRobotBreak.class, "aiRobotBreak");
        RobotManager.registerAIRobot(AIRobotDeliverRequested.class, "aiRobotDeliverRequested");
        RobotManager.registerAIRobot(AIRobotDisposeItems.class, "aiRobotDisposeItems");
        RobotManager.registerAIRobot(AIRobotFetchAndEquipItemStack.class, "aiRobotFetchAndEquipItemStack");
        RobotManager.registerAIRobot(AIRobotFetchItem.class, "aiRobotFetchItem");
        RobotManager.registerAIRobot(AIRobotGoAndLinkToDock.class, "aiRobotGoAndLinkToDock");
        RobotManager.registerAIRobot(AIRobotGotoBlock.class, "aiRobotGotoBlock");
        RobotManager.registerAIRobot(AIRobotGotoSleep.class, "aiRobotGotoSleep");
        RobotManager.registerAIRobot(AIRobotGotoStation.class, "aiRobotGotoStation");
        RobotManager.registerAIRobot(AIRobotGotoStationAndLoad.class, "aiRobotGotoStationAndLoad");
        RobotManager.registerAIRobot(AIRobotGotoStationAndLoadFluids.class, "aiRobotGotoStationAndLoadFluids");
        RobotManager.registerAIRobot(AIRobotGotoStationAndUnload.class, "aiRobotGotoStationAndUnload");
        RobotManager.registerAIRobot(AIRobotGotoStationAndUnloadFluids.class, "aiRobotGotoStationAndUnloadFluids");
        RobotManager.registerAIRobot(AIRobotGotoStationToLoad.class, "aiRobotGotoStationToLoad");
        RobotManager.registerAIRobot(AIRobotGotoStationToLoadFluids.class, "aiRobotGotoStationToLoadFluids");
        RobotManager.registerAIRobot(AIRobotGotoStationToUnload.class, "aiRobotGotoStationToUnload");
        RobotManager.registerAIRobot(AIRobotGotoStationToUnloadFluids.class, "aiRobotGotoStationToUnloadFluids");
        RobotManager.registerAIRobot(AIRobotHarvest.class, "aiRobotHarvest");
        RobotManager.registerAIRobot(AIRobotLoad.class, "aiRobotLoad");
        RobotManager.registerAIRobot(AIRobotLoadFluids.class, "aiRobotLoadFluids");
        RobotManager.registerAIRobot(AIRobotPlant.class, "aiRobotPlant");
        RobotManager.registerAIRobot(AIRobotPumpBlock.class, "aiRobotPumpBlock");
        RobotManager.registerAIRobot(AIRobotRecharge.class, "aiRobotRecharge");
        RobotManager.registerAIRobot(AIRobotSearchAndGotoBlock.class, "aiRobotSearchAndGoToBlock");
        RobotManager.registerAIRobot(AIRobotSearchAndGotoStation.class, "aiRobotSearchAndGotoStation");
        RobotManager.registerAIRobot(AIRobotSearchBlock.class, "aiRobotSearchBlock");
        RobotManager.registerAIRobot(AIRobotSearchEntity.class, "aiRobotSearchEntity");
        RobotManager.registerAIRobot(AIRobotSearchRandomGroundBlock.class, "aiRobotSearchRandomGroundBlock");
        RobotManager.registerAIRobot(AIRobotSearchStackRequest.class, "aiRobotSearchStackRequest");
        RobotManager.registerAIRobot(AIRobotSearchStation.class, "aiRobotSearchStation");
        RobotManager.registerAIRobot(AIRobotShutdown.class, "aiRobotShutdown");
        RobotManager.registerAIRobot(AIRobotSleep.class, "aiRobotSleep");
        RobotManager.registerAIRobot(AIRobotStraightMoveTo.class, "aiRobotStraightMoveTo");
        RobotManager.registerAIRobot(AIRobotStripesHandler.class, "aiRobotStripesHandler");
        RobotManager.registerAIRobot(AIRobotUnload.class, "aiRobotUnload");
        RobotManager.registerAIRobot(AIRobotUnloadFluids.class, "aiRobotUnloadFluids");
        RobotManager.registerAIRobot(AIRobotUseToolOnBlock.class, "aiRobotUseToolOnBlock");
    }

    private static void registerWorldProperties() {
        BuildCraftAPI.registerWorldProperty("replaceable", new WorldPropertyIsReplaceable());
        BuildCraftAPI.registerWorldProperty("soft", new WorldPropertyIsSoft());
        BuildCraftAPI.registerWorldProperty("wood", new WorldPropertyIsWood());
        BuildCraftAPI.registerWorldProperty("leaves", new WorldPropertyIsLeaf());
        BuildCraftAPI.registerWorldProperty("ore", new WorldPropertyIsOre(-1));
        for (int i = 0; i <= WorldPropertyIsOre.MAX_HARVEST_LEVEL; i++) {
            BuildCraftAPI.registerWorldProperty("ore@hardness=" + i, new WorldPropertyIsOre(i));
        }
        BuildCraftAPI.registerWorldProperty("harvestable", new WorldPropertyIsHarvestable());
        BuildCraftAPI.registerWorldProperty("farmland", new WorldPropertyIsFarmland());
        BuildCraftAPI.registerWorldProperty("shoveled", new WorldPropertyIsShoveled());
        BuildCraftAPI.registerWorldProperty("dirt", new WorldPropertyIsDirt());
        BuildCraftAPI.registerWorldProperty("fluidSource", new WorldPropertyIsFluidSource());
        BuildCraftAPI.registerWorldProperty("freepath", new WorldPropertyIsFreePath());
    }
}
