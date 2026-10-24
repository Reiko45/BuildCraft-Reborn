/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import buildcraftreborn.builders.client.BCBuildersClient;
import buildcraftreborn.core.client.BCCoreClient;
import buildcraftreborn.energy.client.BCEnergyClient;
import buildcraftreborn.factory.client.BCFactoryClient;
import buildcraftreborn.lib.client.guide.GuideClient;
import buildcraftreborn.lib.client.model.ModelHolderRegistry;
import buildcraftreborn.lib.client.sprite.SpriteHolderRegistry;
import buildcraftreborn.lib.misc.RegistryAccessUtil;
import buildcraftreborn.robotics.client.BCRoboticsClient;
import buildcraftreborn.silicon.client.BCSiliconClient;
import buildcraftreborn.test.BCTestContent;
import buildcraftreborn.test.client.GuiTestMachine;
import buildcraftreborn.transport.client.BCTransportClient;

/**
 * Client-only entry point. This class never loads on a dedicated server, so client classes (renderers, screens,
 * model loaders) are registered from here as modules are ported.
 */
@Mod(value = BuildCraftReborn.MODID, dist = Dist.CLIENT)
public final class BuildCraftRebornClient {
    public BuildCraftRebornClient(IEventBus modBus, ModContainer container) {
        // Mods screen > BuildCraft Reborn > Config
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modBus.addListener(BuildCraftRebornClient::registerScreens);
        // Registries for reading item stacks from saved NBT when there is no integrated server
        RegistryAccessUtil.clientRegistries = () -> {
            var level = net.minecraft.client.Minecraft.getInstance().level;
            return level == null ? null : level.registryAccess();
        };
        GuideClient.init(modBus);
        BCCoreClient.init(modBus);
        BCEnergyClient.init(modBus);
        BCFactoryClient.init(modBus);
        BCTransportClient.init(modBus);
        BCSiliconClient.init(modBus);
        BCBuildersClient.init(modBus);
        BCRoboticsClient.init(modBus);
        // BuildCraft JSON models and block atlas sprite holders reload with the models
        modBus.addListener(SpriteHolderRegistry::onModelBake);
        modBus.addListener(ModelHolderRegistry::onModelBake);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        if (BCTestContent.ENABLED) {
            event.register(BCTestContent.TEST_MACHINE_MENU.get(), GuiTestMachine::new);
        }
    }
}
