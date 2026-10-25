/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.silicon.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import buildcraftreborn.silicon.BCSiliconPackaging;
import buildcraftreborn.silicon.item.ItemPackage;

/** A thrown package. Where it lands it bursts open and drops what it holds; an entity it hits takes a little damage
 * for each item (twice as much for blocks). */
public class EntityPackage extends ThrowableItemProjectile {
    public EntityPackage(EntityType<? extends EntityPackage> type, Level level) {
        super(type, level);
    }

    public EntityPackage(Level level, LivingEntity thrower, ItemStack pkg) {
        super(BCSiliconPackaging.PACKAGE_ENTITY.get(), thrower, level);
        setItem(pkg);
    }

    public EntityPackage(Level level, double x, double y, double z, ItemStack pkg) {
        super(BCSiliconPackaging.PACKAGE_ENTITY.get(), x, y, z, level);
        setItem(pkg);
    }

    @Override
    protected Item getDefaultItem() {
        return BCSiliconPackaging.PACKAGE.get();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level().isClientSide) {
            return;
        }
        // 7.1 dropped the items inside the block it hit; this drops them on the side it hit instead
        Vec3 at;
        if (result instanceof EntityHitResult entityHit) {
            at = entityHit.getEntity().position();
        } else if (result instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos().relative(blockHit.getDirection());
            at = Vec3.atLowerCornerOf(pos);
        } else {
            at = position().subtract(0.5, 0.5, 0.5);
        }
        float damage = 0;
        RandomSource random = level().getRandom();
        for (ItemStack stack : ItemPackage.getContents(getItem())) {
            if (stack.isEmpty()) {
                continue;
            }
            damage += stack.getItem() instanceof BlockItem ? 0.28F : 0.14F;
            float spread = 0.7F;
            double dx = random.nextFloat() * spread + (1.0F - spread) * 0.5;
            double dy = random.nextFloat() * spread + (1.0F - spread) * 0.5;
            double dz = random.nextFloat() * spread + (1.0F - spread) * 0.5;
            ItemEntity item = new ItemEntity(level(), at.x + dx, at.y + dy, at.z + dz, stack.copy());
            item.setPickUpDelay(10);
            level().addFreshEntity(item);
        }
        if (result instanceof EntityHitResult entityHit && damage > 0) {
            entityHit.getEntity().hurt(damageSources().thrown(this, getOwner()), damage);
        }
        discard();
    }
}
