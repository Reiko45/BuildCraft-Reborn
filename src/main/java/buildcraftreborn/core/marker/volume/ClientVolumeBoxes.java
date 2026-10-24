/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.marker.volume;

import java.util.ArrayList;
import java.util.List;

/** The volume boxes of the client's level, as last sent by the server. */
public enum ClientVolumeBoxes {
    INSTANCE;

    public final List<VolumeBox> volumeBoxes = new ArrayList<>();
}
