/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.gate;

import java.io.IOException;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.core.InvalidInputDataException;
import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.ITrigger;
import buildcraftreborn.api.statements.ITriggerInternal;
import buildcraftreborn.api.statements.StatementManager;

import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.statement.StatementType;
import buildcraftreborn.lib.statement.TriggerWrapper;
import buildcraftreborn.lib.statement.TriggerWrapper.TriggerWrapperInternal;

public class TriggerType extends StatementType<TriggerWrapper> {
    public static final TriggerType INSTANCE = new TriggerType();

    private TriggerType() {
        super(TriggerWrapper.class, null);
    }

    @Override
    public TriggerWrapper convertToType(Object value) {
        if (value instanceof ITriggerInternal) {
            return new TriggerWrapperInternal((ITriggerInternal) value);
        }
        // We cannot convert sided actions (as they require a side)
        return null;
    }

    @Override
    public TriggerWrapper readFromNbt(CompoundTag nbt, HolderLookup.Provider registries) {
        if (nbt == null) {
            return null;
        }
        String kind = nbt.getString("kind");
        if (kind == null || kind.isEmpty()) {
            return null;
        }
        EnumPipePart side = EnumPipePart.fromMeta(nbt.getByte("side"));
        IStatement statement = StatementManager.statements.get(kind);
        if (statement instanceof ITrigger) {
            return TriggerWrapper.wrap(statement, side.face);
        }
        BuildCraftReborn.LOGGER.warn("[gate.trigger] Couldn't find a trigger called '{}'! (found {})", kind, statement);
        return null;
    }

    @Override
    public CompoundTag writeToNbt(TriggerWrapper slot, HolderLookup.Provider registries) {
        CompoundTag nbt = new CompoundTag();
        if (slot == null) {
            return nbt;
        }
        nbt.putString("kind", slot.getUniqueTag());
        nbt.putByte("side", (byte) slot.sourcePart.getIndex());
        return nbt;
    }

    @Override
    public TriggerWrapper readFromBuffer(PacketBufferBC buffer) throws IOException {
        if (buffer.readBoolean()) {
            String name = buffer.readUtf();
            EnumPipePart part = buffer.readEnum(EnumPipePart.class);
            IStatement statement = StatementManager.statements.get(name);
            if (statement instanceof ITrigger) {
                return TriggerWrapper.wrap(statement, part.face);
            } else {
                throw new InvalidInputDataException("Unknown trigger '" + name + "'");
            }
        } else {
            return null;
        }
    }

    @Override
    public void writeToBuffer(PacketBufferBC buffer, TriggerWrapper slot) {
        if (slot == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeUtf(slot.getUniqueTag());
            buffer.writeEnum(slot.sourcePart);
        }
    }
}
