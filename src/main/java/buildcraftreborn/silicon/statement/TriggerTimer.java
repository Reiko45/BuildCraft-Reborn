/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.statement;

import java.util.Locale;

import net.minecraft.world.level.Level;


import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.ITriggerInternal;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.lib.misc.LocaleUtil;

import buildcraftreborn.core.statements.BCStatement;
import buildcraftreborn.silicon.BCSiliconSprites;
import buildcraftreborn.silicon.BCSiliconStatements;

public class TriggerTimer extends BCStatement implements ITriggerInternal {

    public enum Duration {
        SHORT(5),
        MEDIUM(10),
        LONG(15);

        public final int duration;

        private Duration(int duration) {
            this.duration = duration;
        }
    }

    private final Duration duration;

    public TriggerTimer(Duration duration) {
        super("buildcraft:timer_" + duration.name().toLowerCase(Locale.ROOT));
        this.duration = duration;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.timer", duration.duration);
    }

    @Override
    public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
        Level level = source.getTile().getLevel();
        return level.getGameTime() % (20 * duration.duration) == 0;
    }

    @Override
    public IStatement[] getPossible() {
        return BCSiliconStatements.TRIGGER_TIMER;
    }

    @Override
    public ISprite getSprite() {
        switch (duration) {
            case SHORT: {
                return BCSiliconSprites.TRIGGER_TIMER_SHORT;
            }
            case MEDIUM: {
                return BCSiliconSprites.TRIGGER_TIMER_MEDIUM;
            }
            case LONG:
            default: {
                return BCSiliconSprites.TRIGGER_TIMER_LONG;
            }
        }
    }
}
