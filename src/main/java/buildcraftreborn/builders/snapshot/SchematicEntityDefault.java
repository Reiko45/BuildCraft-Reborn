/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.snapshot;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.fluids.FluidStack;

import buildcraftreborn.api.core.InvalidInputDataException;
import buildcraftreborn.api.schematics.ISchematicEntity;
import buildcraftreborn.api.schematics.SchematicEntityContext;
import buildcraftreborn.lib.misc.NBTUtilBC;
import buildcraftreborn.lib.misc.RotationUtil;

/** Copies an entity that a rule says to capture (item frames, paintings, armour stands, minecarts and so on).
 * <p>
 * 1.21: a rotated entity is turned the way vanilla's structures turn them ({@code Entity.rotate}); 1.12 turned it the
 * other way. Item frames store their facing as a 3D direction now, paintings as a lower case {@code facing}. */
public class SchematicEntityDefault implements ISchematicEntity {
    private CompoundTag entityNbt;
    private Vec3 pos;
    private BlockPos hangingPos;
    private Direction hangingFacing;
    private Rotation entityRotation = Rotation.NONE;

    @Nullable
    private static CompoundTag saveEntity(Entity entity) {
        CompoundTag nbt = new CompoundTag();
        return entity.save(nbt) ? nbt : null;
    }

    public static boolean predicate(SchematicEntityContext context) {
        ResourceLocation registryName = BuiltInRegistries.ENTITY_TYPE.getKey(context.entity.getType());
        CompoundTag nbt = saveEntity(context.entity);
        return nbt != null && RulesLoader.READ_DOMAINS.contains(registryName.getNamespace())
            && RulesLoader.getRules(registryName, nbt).stream().anyMatch(rule -> rule.capture);
    }

    @Override
    public void init(SchematicEntityContext context) {
        entityNbt = saveEntity(context.entity);
        pos = context.entity.position().subtract(Vec3.atLowerCornerOf(context.basePos));
        if (context.entity instanceof HangingEntity hanging) {
            hangingPos = hanging.getPos().subtract(context.basePos);
            hangingFacing = hanging.getDirection();
        } else {
            hangingPos = BlockPos.containing(pos);
            hangingFacing = Direction.NORTH;
        }
    }

    @Override
    public Vec3 getPos() {
        return pos;
    }

    private Set<JsonRule> getRules() {
        return RulesLoader.getRules(ResourceLocation.parse(entityNbt.getString("id")), entityNbt);
    }

    @Override
    public List<ItemStack> computeRequiredItems() {
        Set<JsonRule> rules = getRules();
        if (rules.isEmpty()) {
            throw new IllegalArgumentException("Rules are empty");
        }
        return rules.stream()
            .map(rule -> rule.requiredExtractors)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .flatMap(requiredExtractor -> requiredExtractor.extractItemsFromEntity(entityNbt).stream())
            .filter(stack -> !stack.isEmpty())
            .collect(Collectors.toList());
    }

    @Override
    public List<FluidStack> computeRequiredFluids() {
        return getRules().stream()
            .map(rule -> rule.requiredExtractors)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .flatMap(requiredExtractor -> requiredExtractor.extractFluidsFromEntity(entityNbt).stream())
            .filter(fluid -> fluid != null && !fluid.isEmpty())
            .collect(Collectors.toList());
    }

    @Override
    public SchematicEntityDefault getRotated(Rotation rotation) {
        SchematicEntityDefault schematicEntity = SchematicEntityManager.createCleanCopy(this);
        schematicEntity.entityNbt = entityNbt;
        schematicEntity.pos = RotationUtil.rotateVec3d(pos, rotation);
        schematicEntity.hangingPos = hangingPos.rotate(rotation);
        schematicEntity.hangingFacing = rotation.rotate(hangingFacing);
        schematicEntity.entityRotation = entityRotation.getRotated(rotation);
        return schematicEntity;
    }

    @Override
    public Entity build(Level level, BlockPos basePos) {
        CompoundTag replaceNbt = getRules().stream()
            .map(rule -> rule.replaceNbt)
            .filter(Objects::nonNull)
            .map(Tag.class::cast)
            .reduce(NBTUtilBC::merge)
            .map(CompoundTag.class::cast)
            .orElse(null);
        Vec3 placePos = Vec3.atLowerCornerOf(basePos).add(pos);
        BlockPos placeHangingPos = basePos.offset(hangingPos);
        CompoundTag newEntityNbt = entityNbt.copy();
        newEntityNbt.put("Pos", NBTUtilBC.writeVec3d(placePos));
        newEntityNbt.putUUID("UUID", UUID.randomUUID());
        boolean rotate = false;
        if (newEntityNbt.contains("TileX") && newEntityNbt.contains("TileY") && newEntityNbt.contains("TileZ")) {
            newEntityNbt.putInt("TileX", placeHangingPos.getX());
            newEntityNbt.putInt("TileY", placeHangingPos.getY());
            newEntityNbt.putInt("TileZ", placeHangingPos.getZ());
            if (newEntityNbt.contains("Facing")) {
                newEntityNbt.putByte("Facing", (byte) hangingFacing.get3DDataValue());
            } else if (newEntityNbt.contains("facing")) {
                newEntityNbt.putByte("facing", (byte) hangingFacing.get2DDataValue());
            } else {
                rotate = true;
            }
        } else {
            rotate = true;
        }
        CompoundTag toLoad = replaceNbt != null ? (CompoundTag) NBTUtilBC.merge(newEntityNbt, replaceNbt) : newEntityNbt;
        Optional<Entity> created = EntityType.create(toLoad, level);
        if (created.isEmpty()) {
            return null;
        }
        Entity entity = created.get();
        if (rotate) {
            entity.moveTo(placePos.x, placePos.y, placePos.z, entity.rotate(entityRotation), entity.getXRot());
        }
        level.addFreshEntity(entity);
        return entity;
    }

    @Override
    public Entity buildWithoutChecks(Level level, BlockPos basePos) {
        return build(level, basePos);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("entityNbt", entityNbt);
        nbt.put("pos", NBTUtilBC.writeVec3d(pos));
        nbt.put("hangingPos", NbtUtils.writeBlockPos(hangingPos));
        nbt.put("hangingFacing", NBTUtilBC.writeEnum(hangingFacing));
        nbt.put("entityRotation", NBTUtilBC.writeEnum(entityRotation));
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) throws InvalidInputDataException {
        entityNbt = nbt.getCompound("entityNbt");
        pos = NBTUtilBC.readVec3d(nbt.get("pos"));
        hangingPos = NbtUtils.readBlockPos(nbt, "hangingPos").orElse(BlockPos.ZERO);
        hangingFacing = NBTUtilBC.readEnum(nbt.get("hangingFacing"), Direction.class);
        if (hangingFacing == null) {
            hangingFacing = Direction.NORTH;
        }
        Rotation rotation = NBTUtilBC.readEnum(nbt.get("entityRotation"), Rotation.class);
        entityRotation = rotation == null ? Rotation.NONE : rotation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SchematicEntityDefault that = (SchematicEntityDefault) o;
        return entityNbt.equals(that.entityNbt) && pos.equals(that.pos) && hangingPos.equals(that.hangingPos)
            && hangingFacing == that.hangingFacing && entityRotation == that.entityRotation;
    }

    @Override
    public int hashCode() {
        int result = entityNbt.hashCode();
        result = 31 * result + pos.hashCode();
        result = 31 * result + hangingPos.hashCode();
        result = 31 * result + hangingFacing.hashCode();
        result = 31 * result + entityRotation.hashCode();
        return result;
    }
}
