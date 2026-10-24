/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.client;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.Level;

/** The red puffs a robot gives off as it uses power (1.12: {@code EntityRobotEnergyParticle}), with the smoke
 * sprites. */
public class RobotEnergyParticle extends TextureSheetParticle {
    @Nullable
    private static SpriteSet sprites;

    private final float smokeParticleScale;
    private final SpriteSet spriteSet;

    protected RobotEnergyParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz,
        float size, SpriteSet spriteSet) {
        super(level, x, y, z, vx, vy, vz);
        this.spriteSet = spriteSet;
        this.xd = this.xd * 0.1 + vx;
        this.yd = this.yd * 0.1 + vy;
        this.zd = this.zd * 0.1 + vz;
        this.rCol = (float) (Math.random() * 0.6);
        this.gCol = 0;
        this.bCol = 0;
        this.quadSize *= 0.75F * size;
        this.smokeParticleScale = this.quadSize;
        this.lifetime = (int) (16.0D / (Math.random() * 0.8D + 0.2D));
        this.lifetime = (int) (this.lifetime * size);
        this.hasPhysics = true;
        setSpriteFromAge(spriteSet);
    }

    @Override
    public float getQuadSize(float partialTicks) {
        float f6 = (this.age + partialTicks) / this.lifetime * 32.0F;
        f6 = Math.max(0, Math.min(1, f6));
        return this.smokeParticleScale * f6;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            remove();
            return;
        }

        setSpriteFromAge(spriteSet);
        move(this.xd, this.yd, this.zd);

        this.xd *= 0.98;
        this.yd += 0.0005;
        this.zd *= 0.98;

        if (this.onGround) {
            this.xd *= 0.7;
            this.zd *= 0.7;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    static void spawn(Level level, double x, double y, double z, double vx, double vy, double vz, float size) {
        if (sprites != null && level instanceof ClientLevel client) {
            Minecraft.getInstance().particleEngine.add(new RobotEnergyParticle(client, x, y, z, vx, vy, vz, size,
                sprites));
        }
    }

    /** Registered for the {@code buildcraftreborn:robot_energy} particle type. */
    public static ParticleProvider<SimpleParticleType> provider(SpriteSet set) {
        sprites = set;
        return (type, level, x, y, z, vx, vy, vz) -> new RobotEnergyParticle(level, x, y, z, vx, vy, vz, 1, set);
    }
}
