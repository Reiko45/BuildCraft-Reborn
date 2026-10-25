/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.silicon.item;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

import buildcraftreborn.silicon.BCSiliconPackaging;
import buildcraftreborn.silicon.entity.EntityPackage;

/** A package: up to nine items, each in the crafting grid slot the recipe packager put it in. The stamping table
 * crafts it; thrown (by hand, a dispenser or a stripes pipe) it bursts open where it lands. Packages don't stack. */
public class ItemPackage extends Item implements ProjectileItem {
    public static final int SLOTS = 9;

    public ItemPackage(Properties properties) {
        super(properties.stacksTo(1));
    }

    /** @return The package's nine slots (empty stacks where there's nothing). */
    public static NonNullList<ItemStack> getContents(ItemStack pkg) {
        NonNullList<ItemStack> contents = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        ItemContainerContents data = pkg.get(BCSiliconPackaging.PACKAGE_CONTENTS.get());
        if (data != null) {
            data.copyInto(contents);
        }
        return contents;
    }

    public static ItemStack getStack(ItemStack pkg, int slot) {
        return getContents(pkg).get(slot);
    }

    public static void setContents(ItemStack pkg, List<ItemStack> contents) {
        pkg.set(BCSiliconPackaging.PACKAGE_CONTENTS.get(), ItemContainerContents.fromItems(contents));
    }

    /** @return How many slots of the package hold something. */
    public static int countFilledSlots(ItemStack pkg) {
        int count = 0;
        for (ItemStack stack : getContents(pkg)) {
            if (!stack.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // 7.1: random.bow, at half volume
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT,
            SoundSource.PLAYERS, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        if (!level.isClientSide) {
            EntityPackage thrown = new EntityPackage(level, player, stack.copyWithCount(1));
            thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(thrown);
        }
        stack.consume(1, player);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        return new EntityPackage(level, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1));
    }

    /** The package's grid, drawn in its tooltip (7.1 drew it with a special font renderer). */
    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        NonNullList<ItemStack> contents = getContents(stack);
        if (contents.stream().allMatch(ItemStack::isEmpty)) {
            return Optional.empty();
        }
        return Optional.of(new PackageTooltip(contents));
    }

    /** What a package holds, for its tooltip. */
    public record PackageTooltip(NonNullList<ItemStack> contents) implements TooltipComponent {}
}
