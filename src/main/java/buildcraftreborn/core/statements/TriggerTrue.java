/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.statements;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.ITriggerInternal;
import buildcraftreborn.core.BCCoreSprites;
import buildcraftreborn.lib.misc.LocaleUtil;

public class TriggerTrue extends BCStatement implements ITriggerInternal {
    public TriggerTrue() {
        super("buildcraftcore:trigger.true");
    }

    @Override
    public ISprite getSprite() {
        return BCCoreSprites.TRIGGER_TRUE;
    }

    @Override
    public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
        return true;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.true");
    }
}
