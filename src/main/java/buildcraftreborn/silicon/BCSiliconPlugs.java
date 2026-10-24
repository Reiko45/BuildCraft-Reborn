/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.transport.pipe.PipeApi;
import buildcraftreborn.api.transport.pluggable.PluggableDefinition;
import buildcraftreborn.api.transport.pluggable.PluggableDefinition.IPluggableCreator;
import buildcraftreborn.api.transport.pluggable.PluggableDefinition.IPluggableNbtReader;
import buildcraftreborn.api.transport.pluggable.PluggableDefinition.IPluggableNetLoader;
import buildcraftreborn.silicon.plug.PluggableFacade;
import buildcraftreborn.silicon.plug.PluggableGate;
import buildcraftreborn.silicon.plug.PluggableLens;
import buildcraftreborn.silicon.plug.PluggableLightSensor;
import buildcraftreborn.silicon.plug.PluggablePulsar;
import buildcraftreborn.silicon.plug.PluggableTimer;

/** Silicon's pipe pluggables. Ids are 1.12's, in this mod's namespace (1.12's light sensor id was "daylight_sensor").
 * The facade registers itself with the facade code. */
public final class BCSiliconPlugs {
    public static PluggableDefinition gate;
    public static PluggableDefinition lens;
    public static PluggableDefinition pulsar;
    public static PluggableDefinition lightSensor;
    public static PluggableDefinition timer;
    public static PluggableDefinition facade;

    private BCSiliconPlugs() {}

    static void init() {
        gate = register("gate", PluggableGate::new, PluggableGate::new);
        lens = register("lens", PluggableLens::new, PluggableLens::new);
        pulsar = register("pulsar", PluggablePulsar::new, PluggablePulsar::new);
        lightSensor = register("daylight_sensor", PluggableLightSensor::new);
        timer = register("timer", PluggableTimer::new);
        facade = register("facade", PluggableFacade::new, PluggableFacade::new);
    }

    private static PluggableDefinition register(String name, IPluggableCreator creator) {
        return register(new PluggableDefinition(BuildCraftReborn.id(name), creator));
    }

    static PluggableDefinition register(String name, IPluggableNbtReader reader, IPluggableNetLoader loader) {
        return register(new PluggableDefinition(BuildCraftReborn.id(name), reader, loader));
    }

    private static PluggableDefinition register(PluggableDefinition def) {
        PipeApi.pluggableRegistry.register(def);
        return def;
    }
}
