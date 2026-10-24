/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.registry;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;

import buildcraftreborn.api.filler.IFilledTemplate;
import buildcraftreborn.api.filler.IFillerPattern;
import buildcraftreborn.api.filler.IFillerRegistry;

import buildcraftreborn.builders.snapshot.Snapshot;
import buildcraftreborn.builders.snapshot.Template;

public enum FillerRegistry implements IFillerRegistry {
    INSTANCE;

    private final Map<String, IFillerPattern> patterns = new HashMap<>();

    @Override
    public void addPattern(IFillerPattern pattern) {
        patterns.put(pattern.getUniqueTag(), pattern);
    }

    @Override
    @Nullable
    public IFillerPattern getPattern(String name) {
        return patterns.get(name);
    }

    @Override
    public Collection<IFillerPattern> getPatterns() {
        return patterns.values();
    }

    @Override
    public IFilledTemplate createFilledTemplate(BlockPos pos, BlockPos size) {
        Template template = new Template();
        template.size = size;
        template.offset = pos;
        template.data = new BitSet(Snapshot.getDataSize(size));
        return template.getFilledTemplate();
    }
}
