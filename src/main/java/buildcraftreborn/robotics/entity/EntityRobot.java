/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import buildcraftreborn.api.boards.RedstoneBoardNBT;
import buildcraftreborn.api.boards.RedstoneBoardRegistry;
import buildcraftreborn.api.boards.RedstoneBoardRobot;
import buildcraftreborn.api.boards.RedstoneBoardRobotNBT;
import buildcraftreborn.api.core.IFluidFilter;
import buildcraftreborn.api.core.IZone;
import buildcraftreborn.api.events.RobotEvent;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.mj.MjBattery;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.DockingStation;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.api.robots.IRobotOverlayItem;
import buildcraftreborn.api.robots.RobotManager;
import buildcraftreborn.api.statements.StatementSlot;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.api.tools.IToolWrench;
import buildcraftreborn.lib.misc.FakePlayerProvider;
import buildcraftreborn.lib.misc.NBTUtilBC;
import buildcraftreborn.robotics.BCRoboticsEntities;
import buildcraftreborn.robotics.RobotRegistry;
import buildcraftreborn.robotics.ai.AIRobotMain;
import buildcraftreborn.robotics.ai.AIRobotShutdown;
import buildcraftreborn.robotics.ai.AIRobotSleep;
import buildcraftreborn.robotics.boards.RedstoneBoardRobotEmptyNBT;
import buildcraftreborn.robotics.client.RobotGogglesClient;
import buildcraftreborn.robotics.item.ItemRobot;
import buildcraftreborn.robotics.statements.ActionRobotWorkInArea;
import buildcraftreborn.robotics.statements.ActionRobotWorkInArea.AreaType;

/** A robot. It runs its board's AI on the server, flying through soft blocks; it docks at stations to charge, load
 * and unload, and it wears helmets, heads and other wearables for show.
 * <p>
 * 1.21: synced to clients through entity data (1.12 sent its inventory, held item and wearables in packets of its
 * own). The body is a half-block cube centred on the robot's position. Power is in micro MJ; damage drains 260 MJ per
 * point (2,600 RF in 7.1). */
public class EntityRobot extends EntityRobotBase implements IItemHandlerModifiable, IDebuggable {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final EntityDataAccessor<Vector3f> DATA_LASER_TAIL =
        SynchedEntityData.defineId(EntityRobot.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Boolean> DATA_LASER_VISIBLE =
        SynchedEntityData.defineId(EntityRobot.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> DATA_BOARD_ID =
        SynchedEntityData.defineId(EntityRobot.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> DATA_ITEM_AIM_YAW =
        SynchedEntityData.defineId(EntityRobot.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_ITEM_AIM_PITCH =
        SynchedEntityData.defineId(EntityRobot.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Long> DATA_POWER_PER_CYCLE =
        SynchedEntityData.defineId(EntityRobot.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Boolean> DATA_ASLEEP =
        SynchedEntityData.defineId(EntityRobot.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Long> DATA_BATTERY =
        SynchedEntityData.defineId(EntityRobot.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<ItemStack> DATA_ITEM_IN_USE =
        SynchedEntityData.defineId(EntityRobot.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> DATA_ITEM_ACTIVE =
        SynchedEntityData.defineId(EntityRobot.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Vector3f> DATA_STEAM_DIRECTION =
        SynchedEntityData.defineId(EntityRobot.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<List<ItemStack>> DATA_INVENTORY =
        SynchedEntityData.defineId(EntityRobot.class, BCRoboticsEntities.ITEM_LIST_SERIALIZER);
    private static final EntityDataAccessor<List<ItemStack>> DATA_WEARABLES =
        SynchedEntityData.defineId(EntityRobot.class, BCRoboticsEntities.ITEM_LIST_SERIALIZER);

    public static final int MAX_WEARABLES = 8;
    public static final int INVENTORY_SIZE = 4;
    public static final int MAX_FLUID = FluidType.BUCKET_VOLUME * 4;

    /** Power drained from the battery per point of damage (2,600 RF in 7.1). */
    private static final long POWER_PER_DAMAGE = 260 * MjAPI.MJ;

    private static final Set<Item> blacklistedItemsForUpdate = new HashSet<>();

    public Vec3 laserTail = Vec3.ZERO;
    public boolean laserVisible = false;

    @Nullable
    public DockingStation linkedDockingStation;
    @Nullable
    public BlockPos linkedDockingStationIndex;
    @Nullable
    public Direction linkedDockingStationSide;

    @Nullable
    public BlockPos currentDockingStationIndex;
    @Nullable
    public Direction currentDockingStationSide;

    public RedstoneBoardRobot board;
    public AIRobotMain mainAI;

    public ItemStack itemInUse = ItemStack.EMPTY;
    public float itemAimYaw = 0;
    public float itemAimPitch = 0;
    public boolean itemActive = false;
    public float itemActiveStage = 0;
    public long lastUpdateTime = 0;

    /** Client: the body's yaw, turning toward {@link #itemAimYaw}. */
    public float renderYaw = 0, prevRenderYaw = 0;
    private float targetYaw = 0;

    @Nullable
    private DockingStation currentDockingStation;
    private final List<ItemStack> wearables = new ArrayList<>();

    private boolean needsUpdate = false;
    private final NonNullList<ItemStack> inv = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private FluidStack tank = FluidStack.EMPTY;
    @Nullable
    private ResourceLocation texture;

    private final WeakHashMap<Entity, Long> unreachableEntities = new WeakHashMap<>();

    private final MjBattery battery = new MjBattery(MAX_POWER);

    private boolean firstUpdateDone = false;
    private boolean killed = false;

    private long robotId = EntityRobotBase.NULL_ROBOT_ID;

    private long powerPerCycle = 0;
    private int ticksCharging = 0;
    private float energyFX = 0;
    private Vec3 steamDirection = new Vec3(0, -1, 0);

    public EntityRobot(Level world, RedstoneBoardRobotNBT boardNBT) {
        this(BCRoboticsEntities.ROBOT.get(), world);

        board = boardNBT.create(this);
        entityData.set(DATA_BOARD_ID, board.getNBTHandler().getID());

        if (!world.isClientSide) {
            mainAI = new AIRobotMain(this);
            mainAI.start();
        }
    }

    public EntityRobot(EntityType<? extends EntityRobot> type, Level world) {
        super(type, world);
        noPhysics = true;
        noCulling = true;
        setNoGravity(true);
        setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_LASER_TAIL, new Vector3f());
        builder.define(DATA_LASER_VISIBLE, false);
        builder.define(DATA_BOARD_ID, "");
        builder.define(DATA_ITEM_AIM_YAW, 0F);
        builder.define(DATA_ITEM_AIM_PITCH, 0F);
        builder.define(DATA_POWER_PER_CYCLE, 0L);
        builder.define(DATA_ASLEEP, false);
        builder.define(DATA_BATTERY, 0L);
        builder.define(DATA_ITEM_IN_USE, ItemStack.EMPTY);
        builder.define(DATA_ITEM_ACTIVE, false);
        builder.define(DATA_STEAM_DIRECTION, new Vector3f(0, -1, 0));
        builder.define(DATA_INVENTORY, List.of());
        builder.define(DATA_WEARABLES, List.of());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (!level().isClientSide) {
            return;
        }
        if (key == DATA_ITEM_ACTIVE) {
            itemActive = entityData.get(DATA_ITEM_ACTIVE);
            itemActiveStage = 0;
            lastUpdateTime = System.currentTimeMillis();
        } else if (key == DATA_ITEM_IN_USE) {
            itemInUse = entityData.get(DATA_ITEM_IN_USE);
        } else if (key == DATA_INVENTORY) {
            List<ItemStack> list = entityData.get(DATA_INVENTORY);
            for (int i = 0; i < INVENTORY_SIZE; i++) {
                inv.set(i, i < list.size() ? list.get(i) : ItemStack.EMPTY);
            }
        } else if (key == DATA_WEARABLES) {
            wearables.clear();
            wearables.addAll(entityData.get(DATA_WEARABLES));
        } else if (key == DATA_STEAM_DIRECTION) {
            Vector3f dir = entityData.get(DATA_STEAM_DIRECTION);
            Vec3 vec = new Vec3(dir.x(), dir.y(), dir.z());
            steamDirection = vec.lengthSqr() < 1e-6 ? new Vec3(0, -1, 0) : vec.normalize();
        }
    }

    protected void updateDataClient() {
        Vector3f tail = entityData.get(DATA_LASER_TAIL);
        laserTail = new Vec3(tail.x(), tail.y(), tail.z());
        laserVisible = entityData.get(DATA_LASER_VISIBLE);

        RedstoneBoardNBT<?> boardNBT = RedstoneBoardRegistry.instance.getRedstoneBoard(entityData.get(DATA_BOARD_ID));

        if (boardNBT instanceof RedstoneBoardRobotNBT robotNBT) {
            texture = robotNBT.getRobotTexture();
        }

        itemAimYaw = entityData.get(DATA_ITEM_AIM_YAW);
        itemAimPitch = entityData.get(DATA_ITEM_AIM_PITCH);
        powerPerCycle = entityData.get(DATA_POWER_PER_CYCLE);
        long stored = entityData.get(DATA_BATTERY);
        battery.extractAll();
        battery.addPower(stored, false);
    }

    protected void updateDataServer() {
        entityData.set(DATA_LASER_TAIL, new Vector3f((float) laserTail.x, (float) laserTail.y, (float) laserTail.z));
        entityData.set(DATA_LASER_VISIBLE, laserVisible);
        entityData.set(DATA_ITEM_AIM_YAW, itemAimYaw);
        entityData.set(DATA_ITEM_AIM_PITCH, itemAimPitch);
    }

    /** @return True if the robot is asleep or shut down (1.12 called this "isActive"). On the client a robot that is
     *         charging doesn't count as asleep. */
    public boolean isAsleep() {
        if (level().isClientSide) {
            return entityData.get(DATA_ASLEEP);
        } else {
            AIRobot active = mainAI == null ? null : mainAI.getActiveAI();
            return active instanceof AIRobotSleep || active instanceof AIRobotShutdown;
        }
    }

    public void setLaserDestination(float x, float y, float z) {
        if (x != laserTail.x || y != laserTail.y || z != laserTail.z) {
            laserTail = new Vec3(x, y, z);
            needsUpdate = true;
        }
    }

    public void showLaser() {
        if (!laserVisible) {
            laserVisible = true;
            needsUpdate = true;
        }
    }

    public void hideLaser() {
        if (laserVisible) {
            laserVisible = false;
            needsUpdate = true;
        }
    }

    protected void firstUpdate() {
        if (!level().isClientSide) {
            getRegistry().registerRobot(this);
        }
    }

    @Override
    public void tick() {
        level().getProfiler().push("bcEntityRobot");
        if (!firstUpdateDone) {
            firstUpdate();
            firstUpdateDone = true;
        }

        if (ticksCharging > 0) {
            ticksCharging--;
        }

        if (!level().isClientSide) {
            if (mainAI == null) {
                mainAI = new AIRobotMain(this);
            }
            if (board == null) {
                board = RedstoneBoardRegistry.instance.getEmptyRobotBoard().create(this);
                entityData.set(DATA_BOARD_ID, board.getNBTHandler().getID());
            }
            // The client-side sleep indicator should also display if the robot is charging.
            // To not break gates and other things checking for sleep, this is done here.
            entityData.set(DATA_ASLEEP, isAsleep() && ticksCharging == 0);
            entityData.set(DATA_BATTERY, battery.getStored());

            if (needsUpdate) {
                updateDataServer();
                needsUpdate = false;
            }
        } else {
            updateDataClient();
            updateRotationYaw(60.0f);
            updateEnergyFX();
        }

        if (currentDockingStation != null) {
            setDeltaMovement(Vec3.ZERO);
            Vec3 pos = dockedPosition(currentDockingStation);
            setPos(pos.x, pos.y, pos.z);
        }

        if (!level().isClientSide) {
            if (linkedDockingStation == null) {
                if (linkedDockingStationIndex != null) {
                    linkedDockingStation = getRegistry().getStation(linkedDockingStationIndex,
                        linkedDockingStationSide);
                }

                if (linkedDockingStation == null) {
                    shutdown("no docking station");
                } else {
                    if (linkedDockingStation.robotTaking() != this) {
                        if (linkedDockingStation.robotIdTaking() == robotId) {
                            LOGGER.warn("A robot entity was not properly unloaded");
                            linkedDockingStation.invalidateRobotTakingEntity();
                        }
                        if (linkedDockingStation.robotTaking() != this) {
                            shutdown("wrong docking station");
                        }
                    }
                }
            }

            if (currentDockingStationIndex != null && currentDockingStation == null) {
                currentDockingStation = getRegistry().getStation(currentDockingStationIndex, currentDockingStationSide);
            }

            if (linkedDockingStation == null || linkedDockingStation.isInitialized()) {
                level().getProfiler().push("bcRobotAI");
                mainAI.cycle();
                level().getProfiler().pop();

                long cost = mainAI.getActiveAI().getPowerCost();
                if (powerPerCycle != cost) {
                    powerPerCycle = cost;
                    entityData.set(DATA_POWER_PER_CYCLE, powerPerCycle);
                }
            }
        }

        // tick all carried itemstacks
        for (int i = 0; i < inv.size(); i++) {
            updateItem(inv.get(i), i, false);
        }

        // tick the item the robot is currently holding
        updateItem(itemInUse, 0, true);

        // do not tick wearables or equipment from Mob

        super.tick();
        level().getProfiler().pop();
    }

    /** @return Where a robot docked at the station sits: half a block out from the side. */
    public static Vec3 dockedPosition(DockingStation station) {
        return Vec3.atCenterOf(station.getPos()).add(Vec3.atLowerCornerOf(station.side().getNormal()).scale(0.5));
    }

    /** Client only (the client classes are named in full, as this class also loads on servers). */
    private void updateEnergyFX() {
        // 7.1 counted RF: one puff every 100 RF used (more with fewer particles)
        energyFX += powerPerCycle * 10F / MjAPI.MJ;

        int particles = buildcraftreborn.robotics.client.BCRoboticsClient.getParticleSetting();
        if (energyFX >= (100 << (2 * particles))) {
            energyFX = 0;
            float size = powerPerCycle * 10F / MjAPI.MJ * 0.075F;
            buildcraftreborn.robotics.client.BCRoboticsClient.spawnEnergyParticle(level(),
                getX() + steamDirection.x * 0.25, getY() + steamDirection.y * 0.25, getZ() + steamDirection.z * 0.25,
                steamDirection.x * 0.05, steamDirection.y * 0.05, steamDirection.z * 0.05, Math.max(1, size));
        }
    }

    // Movement and collisions

    @Override
    protected AABB makeBoundingBox() {
        return new AABB(getX() - 0.25, getY() - 0.25, getZ() - 0.25, getX() + 0.25, getY() + 0.25, getZ() + 0.25);
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (!level().isClientSide) {
            Vec3 motion = getDeltaMovement();
            setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void pushEntities() {}

    @Override
    public void push(Entity entity) {}

    @Override
    protected void doPush(Entity entity) {}

    @Override
    public boolean isPushedByFluid(FluidType type) {
        return false;
    }

    @Override
    public boolean canDrownInFluidType(FluidType type) {
        return false;
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state,
        BlockPos pos) {}

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return null;
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return null;
    }

    private void shutdown(String reason) {
        if (!(mainAI.getDelegateAI() instanceof AIRobotShutdown)) {
            LOGGER.info("Shutting down robot {} - {}", this, reason);
            mainAI.startDelegateAI(new AIRobotShutdown(this));
        }
    }

    // Equipment: the robot holds its item in use and wears nothing a mob would

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? itemInUse : ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {}

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return NonNullList.withSize(4, ItemStack.EMPTY);
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    public ResourceLocation getTexture() {
        return texture != null ? texture : RedstoneBoardRobotEmptyNBT.ROBOT_BASE;
    }

    // Robot goggles: to a player wearing them, robots glow through walls, tinted by their battery. BuildCraft's
    // unfinished goggles drew robots as plain boxes in 1 - charge red and charge green instead.

    @Override
    public boolean isCurrentlyGlowing() {
        return super.isCurrentlyGlowing() || level().isClientSide && RobotGogglesClient.isWorn();
    }

    @Override
    public int getTeamColor() {
        if (level().isClientSide && RobotGogglesClient.isWorn()) {
            return getBatteryColour((double) battery.getStored() / MAX_POWER);
        }
        return super.getTeamColor();
    }

    /** The colour robot goggles show a robot in: red with an empty battery, through yellow, to green with a full one.
     * @param charge From 0 (empty) to 1 (full). */
    public static int getBatteryColour(double charge) {
        double c = Mth.clamp(charge, 0, 1);
        int red = c <= 0.5 ? 255 : (int) Math.round(255 * 2 * (1 - c));
        int green = c >= 0.5 ? 255 : (int) Math.round(255 * 2 * c);
        return red << 16 | green << 8;
    }

    // Saving

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);

        if (linkedDockingStationIndex != null) {
            CompoundTag linkedStationNBT = new CompoundTag();
            linkedStationNBT.put("index", NBTUtilBC.writeBlockPos(linkedDockingStationIndex));
            linkedStationNBT.putByte("side", (byte) linkedDockingStationSide.ordinal());
            nbt.put("linkedStation", linkedStationNBT);
        }

        if (currentDockingStationIndex != null) {
            CompoundTag currentStationNBT = new CompoundTag();
            currentStationNBT.put("index", NBTUtilBC.writeBlockPos(currentDockingStationIndex));
            currentStationNBT.putByte("side", (byte) currentDockingStationSide.ordinal());
            nbt.put("currentStation", currentStationNBT);
        }

        CompoundTag nbtLaser = new CompoundTag();
        nbtLaser.put("tail", NBTUtilBC.writeVec3d(laserTail));
        nbtLaser.putBoolean("isVisible", laserVisible);
        nbt.put("laser", nbtLaser);

        nbt.put("battery", battery.serializeNBT());

        if (!itemInUse.isEmpty()) {
            nbt.put("itemInUse", itemInUse.save(registryAccess()));
            nbt.putBoolean("itemActive", itemActive);
        }

        for (int i = 0; i < inv.size(); ++i) {
            if (!inv.get(i).isEmpty()) {
                nbt.put("inv[" + i + "]", inv.get(i).save(registryAccess()));
            }
        }

        if (!wearables.isEmpty()) {
            ListTag wearableList = new ListTag();

            for (ItemStack wearable : wearables) {
                wearableList.add(wearable.save(registryAccess()));
            }

            nbt.put("wearables", wearableList);
        }

        if (mainAI != null) {
            CompoundTag ai = new CompoundTag();
            mainAI.writeToNBT(ai);
            nbt.put("mainAI", ai);
        }

        if (board != null && (mainAI == null || mainAI.getDelegateAI() != board)) {
            CompoundTag boardNBT = new CompoundTag();
            board.writeToNBT(boardNBT);
            nbt.put("board", boardNBT);
        }

        nbt.putLong("robotId", robotId);

        if (!tank.isEmpty()) {
            nbt.put("tank", tank.save(registryAccess()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);

        if (nbt.contains("linkedStation")) {
            CompoundTag linkedStationNBT = nbt.getCompound("linkedStation");
            linkedDockingStationIndex = NBTUtilBC.readBlockPos(linkedStationNBT.get("index"));
            linkedDockingStationSide = Direction.from3DDataValue(linkedStationNBT.getByte("side"));
        }

        if (nbt.contains("currentStation")) {
            CompoundTag currentStationNBT = nbt.getCompound("currentStation");
            currentDockingStationIndex = NBTUtilBC.readBlockPos(currentStationNBT.get("index"));
            currentDockingStationSide = Direction.from3DDataValue(currentStationNBT.getByte("side"));
        }

        CompoundTag laser = nbt.getCompound("laser");
        laserTail = laser.contains("tail") ? NBTUtilBC.readVec3d(laser.get("tail")) : Vec3.ZERO;
        laserVisible = laser.getBoolean("isVisible");

        battery.deserializeNBT(nbt.getCompound("battery"));

        wearables.clear();
        if (nbt.contains("wearables")) {
            ListTag list = nbt.getList("wearables", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                ItemStack stack = ItemStack.parseOptional(registryAccess(), list.getCompound(i));
                if (!stack.isEmpty()) {
                    wearables.add(stack);
                }
            }
        }

        if (nbt.contains("itemInUse")) {
            itemInUse = ItemStack.parseOptional(registryAccess(), nbt.getCompound("itemInUse"));
            itemActive = nbt.getBoolean("itemActive");
        }

        for (int i = 0; i < inv.size(); ++i) {
            inv.set(i, ItemStack.parseOptional(registryAccess(), nbt.getCompound("inv[" + i + "]")));
        }

        AIRobot loaded = AIRobot.loadAI(nbt.getCompound("mainAI"), this);
        mainAI = loaded instanceof AIRobotMain main ? main : new AIRobotMain(this);

        if (nbt.contains("board")) {
            AIRobot loadedBoard = AIRobot.loadAI(nbt.getCompound("board"), this);
            board = loadedBoard instanceof RedstoneBoardRobot b ? b : null;
        } else {
            board = mainAI.getDelegateAI() instanceof RedstoneBoardRobot b ? b : null;
        }

        if (board == null) {
            board = RedstoneBoardRegistry.instance.getEmptyRobotBoard().create(this);
        }

        entityData.set(DATA_BOARD_ID, board.getNBTHandler().getID());

        if (nbt.contains("robotId")) {
            robotId = nbt.getLong("robotId");
        }

        tank = nbt.contains("tank") ? FluidStack.parseOptional(registryAccess(), nbt.getCompound("tank"))
            : FluidStack.EMPTY;

        setPersistenceRequired();
        syncInventory();
        syncWearablesToClient();
        entityData.set(DATA_ITEM_IN_USE, itemInUse.copy());
        entityData.set(DATA_ITEM_ACTIVE, itemActive);
        updateDataServer();
    }

    // Docking

    @Override
    public void dock(DockingStation station) {
        currentDockingStation = station;

        setSteamDirection(Vec3.atLowerCornerOf(station.side.getNormal()));

        currentDockingStationIndex = station.index();
        currentDockingStationSide = station.side();
    }

    @Override
    public void undock() {
        if (currentDockingStation != null) {
            currentDockingStation.release(this);
            currentDockingStation = null;

            setSteamDirection(new Vec3(0, -1, 0));

            currentDockingStationIndex = null;
            currentDockingStationSide = null;
        }
    }

    @Override
    @Nullable
    public DockingStation getDockingStation() {
        return currentDockingStation;
    }

    @Override
    public void setMainStation(@Nullable DockingStation station) {
        if (linkedDockingStation != null && linkedDockingStation != station) {
            linkedDockingStation.unsafeRelease(this);
        }

        linkedDockingStation = station;
        if (station != null) {
            linkedDockingStationIndex = station.index();
            linkedDockingStationSide = station.side();
        } else {
            linkedDockingStationIndex = null;
            linkedDockingStationSide = null;
        }
    }

    // IItemHandler

    @Override
    public int getSlots() {
        return inv.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inv.get(slot);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        inv.set(slot, stack);
        syncInventory();
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack existing = inv.get(slot);
        int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(stack, existing)) {
                return stack;
            }
            limit -= existing.getCount();
        }
        if (limit <= 0) {
            return stack;
        }
        boolean reachedLimit = stack.getCount() > limit;
        if (!simulate) {
            if (existing.isEmpty()) {
                inv.set(slot, reachedLimit ? stack.copyWithCount(limit) : stack.copy());
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            syncInventory();
        }
        return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack existing = inv.get(slot);
        if (amount <= 0 || existing.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int toExtract = Math.min(amount, existing.getMaxStackSize());
        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                inv.set(slot, ItemStack.EMPTY);
                syncInventory();
                return existing;
            }
            return existing.copy();
        }
        if (!simulate) {
            inv.set(slot, existing.copyWithCount(existing.getCount() - toExtract));
            syncInventory();
        }
        return existing.copyWithCount(toExtract);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    /** Sends the inventory to the clients, which draw it. */
    public void syncInventory() {
        if (!level().isClientSide) {
            entityData.set(DATA_INVENTORY, List.copyOf(inv));
        }
    }

    // Moving and aiming

    @Override
    public boolean isMoving() {
        return !getDeltaMovement().equals(Vec3.ZERO);
    }

    @Override
    public void setItemInUse(ItemStack stack) {
        itemInUse = stack;
        entityData.set(DATA_ITEM_IN_USE, stack.copy());
    }

    private void setSteamDirection(Vec3 direction) {
        if (!level().isClientSide) {
            entityData.set(DATA_STEAM_DIRECTION, new Vector3f((float) direction.x, (float) direction.y,
                (float) direction.z));
        } else {
            steamDirection = direction.normalize();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide) {
            return false;
        }
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            // 1.21: /kill drops the robot as items; falling out of the world loses it (7.1 ignored both)
            if (source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
                LOGGER.info("Destroying robot {} - Fallen into Void", this);
                killRobot();
                discard();
            } else {
                convertToItems();
            }
            return true;
        }
        // Ignore hits from mobs or when docked.
        Entity src = source.getDirectEntity();
        if (src != null && !(src instanceof FallingBlockEntity) && !(src instanceof Enemy)
            && currentDockingStation == null) {
            if (isInvulnerableTo(source)) {
                return false;
            }
            hurtTime = hurtDuration = 10;
            level().broadcastDamageEvent(this, source);

            long mul = POWER_PER_DAMAGE;
            for (ItemStack s : wearables) {
                if (s.getItem() instanceof ArmorItem armor) {
                    mul = mul * 2 / (2 + armor.getDefense());
                } else {
                    mul = mul * 7 / 10;
                }
            }

            long energy = Math.round(amount * (double) mul);
            if (battery.getStored() - energy > 0) {
                battery.extractPower(energy, energy);
            } else {
                onRobotHit(true);
            }
            return true;
        }
        return false;
    }

    @Override
    public void setHealth(float health) {
        // deactivate health management
        super.setHealth(getMaxHealth());
    }

    @Override
    public float getAimYaw() {
        return itemAimYaw;
    }

    @Override
    public float getAimPitch() {
        return itemAimPitch;
    }

    @Override
    public void aimItemAt(float yaw, float pitch) {
        itemAimYaw = yaw;
        itemAimPitch = pitch;

        updateDataServer();
    }

    @Override
    public void aimItemAt(BlockPos pos) {
        Vec3 delta = Vec3.atLowerCornerOf(pos).subtract(position());
        if (delta.x != 0 || delta.z != 0) {
            // 7.1's angle (the 1.12 rewrite swapped x and z, which turned the tool away from its target)
            itemAimYaw = (float) (Math.atan2(delta.z, delta.x) * 180f / Math.PI) + 180f;
        }

        double d3 = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        itemAimPitch = (float) (-(Math.atan2(delta.y, d3) * 180.0D / Math.PI));

        setSteamDirection(delta);

        updateDataServer();
    }

    private void updateRotationYaw(float maxStep) {
        float step = Mth.wrapDegrees(itemAimYaw - targetYaw);

        if (step > maxStep) {
            step = maxStep;
        }

        if (step < -maxStep) {
            step = -maxStep;
        }

        targetYaw = targetYaw + step;

        // 7.1's body turn: half way each tick
        prevRenderYaw = renderYaw;
        float f2 = Mth.wrapDegrees(targetYaw - renderYaw);
        renderYaw += f2 * 0.5F;
        float f3 = Mth.wrapDegrees(targetYaw - renderYaw);
        if (f3 * f3 > 2500.0F) {
            renderYaw += f3 * 0.2F;
        }
    }

    @Override
    public void setItemActive(boolean isActive) {
        if (isActive != itemActive) {
            itemActive = isActive;
            entityData.set(DATA_ITEM_ACTIVE, isActive);
            if (!isActive) {
                setSteamDirection(new Vec3(0, -1, 0));
            }
        }
    }

    @Override
    public RedstoneBoardRobot getBoard() {
        return board;
    }

    @Override
    @Nullable
    public DockingStation getLinkedStation() {
        return linkedDockingStation;
    }

    @Override
    public MjBattery getBattery() {
        return battery;
    }

    @Nullable
    public AIRobot getOverridingAI() {
        return mainAI.getOverridingAI();
    }

    public void overrideAI(AIRobot ai) {
        mainAI.setOverridingAI(ai);
    }

    /** Hits the entity with the item in use, as a player would (and as the robot's fake player, for protection
     * mods). 1.21: the damage, knockback and enchantments are a mob's (from the item's attributes, on top of 2 damage
     * as in 7.1). */
    public void attackTargetEntityWithCurrentItem(Entity target) {
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        Player fakePlayer = FakePlayerProvider.INSTANCE.getFakePlayer(server, FakePlayerProvider.NULL_PROFILE,
            target.blockPosition());
        if (NeoForge.EVENT_BUS.post(new AttackEntityEvent(fakePlayer, target)).isCanceled()) {
            return;
        }
        if (target.isAttackable() && !target.skipAttackInteraction(this)) {
            ItemStack weapon = itemInUse;
            if (doHurtTarget(target)) {
                if (!weapon.isEmpty() && target instanceof LivingEntity living) {
                    if (weapon.getItem().hurtEnemy(weapon, living, this)) {
                        weapon.getItem().postHurtEnemy(weapon, living, this);
                    }
                }

                if (itemInUse.isEmpty() || itemInUse.getCount() == 0) {
                    setItemInUse(ItemStack.EMPTY);
                } else {
                    setItemInUse(itemInUse);
                }
            }
        }
    }

    // Zones

    @Override
    @Nullable
    public IZone getZoneToWork() {
        return getZone(ActionRobotWorkInArea.AreaType.WORK);
    }

    @Override
    @Nullable
    public IZone getZoneToLoadUnload() {
        IZone zone = getZone(ActionRobotWorkInArea.AreaType.LOAD_UNLOAD);
        if (zone == null) {
            zone = getZoneToWork();
        }
        return zone;
    }

    @Nullable
    private IZone getZone(AreaType areaType) {
        if (linkedDockingStation != null) {
            for (StatementSlot s : linkedDockingStation.getActiveActions()) {
                if (s.statement instanceof ActionRobotWorkInArea action && action.getAreaType() == areaType) {
                    IZone zone = ActionRobotWorkInArea.getArea(s);

                    if (zone != null) {
                        return zone;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public boolean containsItems() {
        for (ItemStack element : inv) {
            if (!element.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean hasFreeSlot() {
        for (ItemStack element : inv) {
            if (element.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void unreachableEntityDetected(Entity entity) {
        unreachableEntities.put(entity, level().getGameTime() + 1200);
    }

    @Override
    public boolean isKnownUnreachable(Entity entity) {
        Long until = unreachableEntities.get(entity);
        if (until != null) {
            if (until >= level().getGameTime()) {
                return true;
            } else {
                unreachableEntities.remove(entity);
                return false;
            }
        } else {
            return false;
        }
    }

    protected void onRobotHit(boolean attacked) {
        if (!level().isClientSide) {
            if (attacked) {
                convertToItems();
            } else {
                if (!wearables.isEmpty()) {
                    spawnAtLocation(wearables.remove(wearables.size() - 1), 0);
                    syncWearablesToClient();
                } else if (!itemInUse.isEmpty()) {
                    spawnAtLocation(itemInUse, 0);
                    setItemInUse(ItemStack.EMPTY);
                } else {
                    convertToItems();
                }
            }
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (NeoForge.EVENT_BUS.post(new RobotEvent.Interact(this, player, stack)).isCanceled()) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown() && stack.getItem() instanceof IToolWrench wrench) {
            if (NeoForge.EVENT_BUS.post(new RobotEvent.Dismantle(this, player)).isCanceled()) {
                return InteractionResult.PASS;
            }

            onRobotHit(false);

            wrench.wrenchUsed(player, hand, stack, new EntityHitResult(this));
            return InteractionResult.sidedSuccess(level().isClientSide);
        } else if (wearables.size() < MAX_WEARABLES && isWearable(stack)) {
            if (!level().isClientSide) {
                wearables.add(stack.split(1));
                syncWearablesToClient();
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    /** @return True for helmets, heads (1.12: skulls) and {@link IRobotOverlayItem robot overlays}. */
    public static boolean isWearable(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem armor) {
            return armor.getEquipmentSlot() == EquipmentSlot.HEAD;
        }
        if (stack.getItem() instanceof IRobotOverlayItem overlay) {
            return overlay.isValidRobotOverlay(stack);
        }
        return stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof AbstractSkullBlock;
    }

    private void syncWearablesToClient() {
        if (!level().isClientSide) {
            entityData.set(DATA_WEARABLES, List.copyOf(wearables));
        }
    }

    private List<ItemStack> getDrops() {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(ItemRobot.createRobotStack(board.getNBTHandler(), battery.getStored()));
        if (!itemInUse.isEmpty()) {
            drops.add(itemInUse);
        }
        for (ItemStack element : inv) {
            if (!element.isEmpty()) {
                drops.add(element);
            }
        }
        drops.addAll(wearables);
        return drops;
    }

    /** Drops the robot (as an item with its board and power), its tools, inventory and wearables, and removes it. */
    public void convertToItems() {
        if (!level().isClientSide && !isRemoved()) {
            if (mainAI != null) {
                mainAI.abort();
            }
            for (ItemStack stack : getDrops()) {
                spawnAtLocation(stack, 0);
            }
            killRobot();
            discard();
        }
    }

    private void killRobot() {
        if (!killed && !level().isClientSide) {
            killed = true;
            getRegistry().killRobot(this);
        }
    }

    @Override
    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
        if (level().isClientSide) {
            return;
        }
        RemovalReason reason = getRemovalReason();
        if (reason != null && !reason.shouldDestroy()) {
            onChunkUnload();
        } else {
            killRobot();
        }
    }

    @Override
    public void onChunkUnload() {
        if (!killed) {
            getRegistry().unloadRobot(this);
        }
    }

    public void setUniqueRobotId(long iRobotId) {
        robotId = iRobotId;
    }

    @Override
    public long getRobotId() {
        return robotId;
    }

    @Override
    public RobotRegistry getRegistry() {
        return (RobotRegistry) RobotManager.registryProvider.getRegistry(level());
    }

    @Override
    public void releaseResources() {
        getRegistry().releaseResources(this);
    }

    /** Tries to receive items in parameters, return items that are left after the operation. */
    @Override
    public ItemStack receiveItem(BlockEntity tile, ItemStack stack) {
        if (currentDockingStation != null && currentDockingStation.index().distManhattan(tile.getBlockPos()) == 1
            && mainAI != null) {
            return mainAI.getActiveAI().receiveItem(stack);
        } else {
            return stack;
        }
    }

    // IFluidHandlerAdv

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int t) {
        return tank;
    }

    @Override
    public int getTankCapacity(int t) {
        return MAX_FLUID;
    }

    @Override
    public boolean isFluidValid(int t, FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || (!tank.isEmpty() && !FluidStack.isSameFluidSameComponents(tank, resource))) {
            return 0;
        }
        int space = MAX_FLUID - tank.getAmount();
        int filled = Math.min(space, resource.getAmount());
        if (filled > 0 && action.execute()) {
            if (tank.isEmpty()) {
                tank = resource.copyWithAmount(filled);
            } else {
                tank.grow(filled);
            }
        }
        return filled;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (!tank.isEmpty() && FluidStack.isSameFluidSameComponents(tank, resource)) {
            return drain(resource.getAmount(), action);
        } else {
            return FluidStack.EMPTY;
        }
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (tank.isEmpty() || maxDrain <= 0) {
            return FluidStack.EMPTY;
        }
        int drained = Math.min(maxDrain, tank.getAmount());
        FluidStack result = tank.copyWithAmount(drained);
        if (action.execute()) {
            tank.shrink(drained);
            if (tank.isEmpty()) {
                tank = FluidStack.EMPTY;
            }
        }
        return result;
    }

    @Override
    public FluidStack drain(IFluidFilter filter, int maxDrain, FluidAction action) {
        if (tank.isEmpty() || !filter.matches(tank)) {
            return FluidStack.EMPTY;
        }
        return drain(maxDrain, action);
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        left.add("Robot:");
        if (board != null) {
            if (board.getNBTHandler() != null) {
                left.add("  " + board.getNBTHandler().getID());
            } else {
                left.add("  " + board.getClass());
            }
        } else {
            left.add("  Unknown type");
        }
        left.add("  Battery " + battery.getDebugString());

        left.add(String.format("Position: %.2f, %.2f, %.2f", getX(), getY(), getZ()));
        left.add("AI tree:");
        AIRobot aiRobot = mainAI;
        while (aiRobot != null) {
            left.add("- " + RobotManager.getAIRobotName(aiRobot.getClass()) + " (" + MjAPI.formatMj(
                aiRobot.getPowerCost()) + " MJ/t)");
            if (aiRobot instanceof IDebuggable debuggable) {
                debuggable.getDebugInfo(left, right, side);
            }
            aiRobot = aiRobot.getDelegateAI();
        }
    }

    /** Called by the station the robot is docked at.
     *
     * @return The power that didn't fit. */
    public long receivePower(long microJoules, boolean simulate) {
        long space = MAX_POWER - battery.getStored();
        long received = Math.max(0, Math.min(space, microJoules));
        if (!simulate) {
            battery.addPower(received, false);
            // 5 RF/t is set as the "sleep threshold" for detecting charging.
            if (received > MjAPI.MJ / 2 && ticksCharging <= 25) {
                ticksCharging += 5;
            }
        }
        return microJoules - received;
    }

    public List<ItemStack> getWearables() {
        return wearables;
    }

    public FluidStack getTank() {
        return tank;
    }

    private void updateItem(ItemStack stack, int i, boolean held) {
        if (!stack.isEmpty() && !blacklistedItemsForUpdate.contains(stack.getItem())) {
            try {
                stack.inventoryTick(level(), this, i, held);
            } catch (Exception e) {
                // the item threw an exception, print it and do not let it update once more
                LOGGER.error("The item {} crashed while a robot held it",
                    BuiltInRegistries.ITEM.getKey(stack.getItem()), e);
                blacklistedItemsForUpdate.add(stack.getItem());
            }
        }
    }

    /** @return The stack a robot item for this robot would be (for gate parameters and lists). */
    public ItemStack createRobotStack() {
        return ItemRobot.createRobotStack(board.getNBTHandler(), battery.getStored());
    }

    /** Helper for AIs: puts a stack into the robot's inventory, returning what didn't fit. */
    public ItemStack addToInventory(ItemStack stack, boolean simulate) {
        return ItemHandlerHelper.insertItemStacked(this, stack, simulate);
    }
}
