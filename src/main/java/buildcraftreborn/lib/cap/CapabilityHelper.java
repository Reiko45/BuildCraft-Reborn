/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.cap;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;

import net.neoforged.neoforge.capabilities.BlockCapability;

import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.mj.MjCapabilityHelper;

/** Provides a simple way of mapping capabilities to instances, per side. Also allows for additional providers.
 * <p>
 * 1.21: NeoForge registers capability providers per block entity <i>type</i> (in {@code RegisterCapabilitiesEvent})
 * instead of asking each block entity. {@link BCCapabilities} registers one provider per BuildCraft block entity type
 * that forwards to this helper, so tiles keep adding their capabilities per instance exactly as they did in 1.12. */
public class CapabilityHelper implements ICapabilityProviderBC {
    private final Map<EnumPipePart, Map<BlockCapability<?, ?>, Supplier<?>>> caps = new EnumMap<>(EnumPipePart.class);
    private final List<ICapabilityProviderBC> additional = new ArrayList<>();

    public CapabilityHelper() {
        for (EnumPipePart face : EnumPipePart.VALUES) {
            caps.put(face, new HashMap<>());
        }
    }

    public <T> void addCapabilityInstance(BlockCapability<T, @Nullable Direction> cap, T instance, EnumPipePart... parts) {
        Supplier<T> supplier = () -> instance;
        addCapability(cap, supplier, parts);
    }

    public <T> void addCapability(BlockCapability<T, @Nullable Direction> cap, Supplier<T> getter, EnumPipePart... parts) {
        for (EnumPipePart part : parts) {
            caps.get(part).put(cap, getter);
        }
    }

    public <T> void addCapability(BlockCapability<T, @Nullable Direction> cap, Function<@Nullable Direction, T> getter,
        EnumPipePart... parts) {
        for (EnumPipePart part : parts) {
            caps.get(part).put(cap, () -> getter.apply(part.face));
        }
    }

    /** Exposes every MJ capability (and the Forge Energy view, if enabled) of the given helper on all sides. This is
     * 1.12's {@code caps.addProvider(new MjCapabilityHelper(receiver))}. */
    public MjCapabilityHelper addMj(MjCapabilityHelper mj) {
        addProvider(new ICapabilityProviderBC() {
            @Override
            @Nullable
            public <T> T getCapability(BlockCapability<T, @Nullable Direction> capability, @Nullable Direction side) {
                return mj.getCapability(capability);
            }
        });
        return mj;
    }

    public <T extends ICapabilityProviderBC> T addProvider(T provider) {
        if (provider != null) {
            additional.add(provider);
        }
        return provider;
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getCapability(BlockCapability<T, @Nullable Direction> capability, @Nullable Direction side) {
        Supplier<?> supplier = caps.get(EnumPipePart.fromFacing(side)).get(capability);
        if (supplier != null) {
            return (T) supplier.get();
        }
        for (ICapabilityProviderBC provider : additional) {
            T value = provider.getCapability(capability, side);
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
