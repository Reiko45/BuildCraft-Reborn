/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.fluid;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;

/** A bucket of a {@link BCFluid}, named "Bucket of &lt;fluid&gt;" so the heat level shows. */
public class BCBucketItem extends BucketItem {
    public final BCFluid definition;

    public BCBucketItem(BCFluid definition, Properties properties) {
        super(definition.source.get(), properties);
        this.definition = definition;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.buildcraftreborn.bucket_of", definition.type.get().getDescription());
    }
}
