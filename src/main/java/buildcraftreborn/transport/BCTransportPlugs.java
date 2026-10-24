/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.transport.pipe.PipeApi;
import buildcraftreborn.api.transport.pluggable.PluggableDefinition;
import buildcraftreborn.api.transport.pluggable.PluggableDefinition.IPluggableCreator;
import buildcraftreborn.transport.plug.PluggableBlocker;
import buildcraftreborn.transport.plug.PluggablePowerAdaptor;

/** Transport's pipe pluggables: the blocker and the power adaptor. Ids are 1.12's, in this mod's namespace. */
public final class BCTransportPlugs {
    public static PluggableDefinition blocker;
    public static PluggableDefinition powerAdaptor;

    private BCTransportPlugs() {}

    public static void init() {
        blocker = register("blocker", PluggableBlocker::new);
        powerAdaptor = register("power_adaptor", PluggablePowerAdaptor::new);
    }

    private static PluggableDefinition register(String name, IPluggableCreator creator) {
        PluggableDefinition def = new PluggableDefinition(BuildCraftReborn.id(name), creator);
        PipeApi.pluggableRegistry.register(def);
        return def;
    }
}
