/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.statements;


import buildcraftreborn.api.statements.IActionInternal;
import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.lib.misc.ColourUtil;
import buildcraftreborn.lib.misc.LocaleUtil;

import buildcraftreborn.core.statements.BCStatement;
import buildcraftreborn.transport.BCTransportSprites;
import buildcraftreborn.transport.BCTransportStatements;
import buildcraftreborn.transport.pipe.behaviour.PipeBehaviourEmzuli.SlotIndex;

public class ActionExtractionPreset extends BCStatement implements IActionInternal {

    public final SlotIndex index;

    public ActionExtractionPreset(SlotIndex index) {
        super("buildcraft:extraction.preset." + index.colour.getName(), "buildcraft.extraction.preset." + index.colour.getName());

        this.index = index;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.action.extraction", ColourUtil.getTextFullTooltip(index.colour));
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {
        // The pipe handles this
    }

    @Override
    public IStatement[] getPossible() {
        return BCTransportStatements.ACTION_EXTRACTION_PRESET;
    }

    @Override
    public ISprite getSprite() {
        return BCTransportSprites.ACTION_EXTRACTION_PRESET.get(index);
    }
}
