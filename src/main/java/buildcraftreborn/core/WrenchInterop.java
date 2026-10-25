/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.core;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent.UsePhase;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.blocks.CustomRotationHelper;
import buildcraftreborn.api.tools.IToolWrench;
import buildcraftreborn.lib.misc.AdvancementUtil;
import buildcraftreborn.lib.misc.SoundUtil;

/** Other mods' wrenches (anything in the common {@code c:tools/wrench} tag) on BuildCraft blocks: they rotate them as
 * BuildCraft's wrench does. A sneaking right click always rotates; a plain one rotates when the block has nothing else
 * to do (a machine with a GUI opens it instead, as with BuildCraft's wrench). Everything else that takes a wrench
 * (pipe modes, the iron engine, the creative engine, flood gate sides, construction marks, dismantling robots) checks
 * {@link buildcraftreborn.lib.misc.EntityUtil#isWrench}. BuildCraft's own wrench is in the tag too, for other mods. */
public final class WrenchInterop {
    private WrenchInterop() {}

    static void register() {
        NeoForge.EVENT_BUS.addListener(WrenchInterop::onUseItemOnBlock);
    }

    private static void onUseItemOnBlock(UseItemOnBlockEvent event) {
        ItemStack stack = event.getItemStack();
        Player player = event.getPlayer();
        if (player == null || stack.getItem() instanceof IToolWrench || !stack.is(Tags.Items.TOOLS_WRENCH)
            || event.getFace() == null) {
            return;
        }
        boolean sneaking = player.isSecondaryUseActive();
        UsePhase phase = event.getUsePhase();
        if (!(sneaking ? phase == UsePhase.ITEM_BEFORE_BLOCK : phase == UsePhase.ITEM_AFTER_BLOCK)) {
            return;
        }
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!BuildCraftReborn.MODID.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace())) {
            return;
        }
        InteractionResult result = CustomRotationHelper.INSTANCE.attemptRotateBlock(level, pos, state,
            event.getFace());
        if (result.consumesAction()) {
            if (!level.isClientSide) {
                SoundUtil.playSlideSound(level, pos, state, result);
                AdvancementUtil.unlockAdvancement(player, BuildCraftReborn.id("wrenched"));
            }
            event.cancelWithResult(ItemInteractionResult.sidedSuccess(level.isClientSide));
        }
    }
}
