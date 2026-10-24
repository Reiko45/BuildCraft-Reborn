/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.factory.tile;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.recipes.BuildcraftRecipeRegistry;
import buildcraftreborn.api.recipes.IRefineryRecipeManager;
import buildcraftreborn.api.recipes.IRefineryRecipeManager.ICoolableRecipe;
import buildcraftreborn.api.recipes.IRefineryRecipeManager.IHeatableRecipe;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.factory.BCFactoryBlocks;
import buildcraftreborn.factory.block.BlockHeatExchange;
import buildcraftreborn.factory.block.BlockHeatExchange.EnumExchangePart;
import buildcraftreborn.lib.block.BlockBCBase_Neptune;
import buildcraftreborn.lib.block.VanillaRotationHandlers;
import buildcraftreborn.lib.cap.CapabilityHelper;
import buildcraftreborn.lib.fluid.FluidSmoother;
import buildcraftreborn.lib.fluid.Tank;
import buildcraftreborn.lib.fluid.TankManager;
import buildcraftreborn.lib.misc.FluidUtilBC;
import buildcraftreborn.lib.misc.InventoryUtil;
import buildcraftreborn.lib.misc.MathUtil;
import buildcraftreborn.lib.misc.SoundUtil;
import buildcraftreborn.lib.misc.VecUtil;
import buildcraftreborn.lib.misc.data.IdAllocator;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.tile.ITickable;
import buildcraftreborn.lib.tile.TileBC_Neptune;

/** One block of a heat exchanger: 3 to 5 exchangers side by side, all facing the same way. The first one (the start)
 * takes the fluid to heat from below and gives the cooled fluid out of its side; the last one (the end) takes the hot
 * fluid in from its side and gives the heated fluid out of the top. The middle sections set how fast it runs.
 * <p>
 * 1.21: whether a block is the start, a middle or the end is stored in its block state ({@code part}), so the block
 * model can show it; 1.12 worked it out when drawing. 1.12 also forgot to save the start section's tanks, so they
 * emptied when the world reloaded; both sections are saved here. */
public class TileHeatExchange extends TileBC_Neptune implements ITickable, IDebuggable {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("HeatExchanger");
    public static final int NET_ID_CHANGE_SECTION = IDS.allocId("CHANGE_SECTION");
    public static final int NET_ID_TANK_IN = IDS.allocId("TANK_IN");
    public static final int NET_ID_TANK_OUT = IDS.allocId("TANK_OUT");
    public static final int NET_ID_STATE = IDS.allocId("STATE");

    /** the maximum amount of fluid that can be transferred per tick for each number of middle sections. numbers need
     * to be divisors of 1000 */
    private static final int[] FLUID_MULT = { 5, 10, 20 };

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    @Nullable
    protected ExchangeSection section;
    private boolean checkNeighbours = true;

    public TileHeatExchange(BlockPos pos, BlockState state) {
        super(BCFactoryBlocks.HEAT_EXCHANGE_TILE.get(), pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);

        CompoundTag nbtSection = nbt.getCompound("section");
        if (!nbtSection.isEmpty()) {
            if (nbtSection.getBoolean("start")) {
                section = new ExchangeSectionStart(this, nbtSection, registries);
            } else {
                section = new ExchangeSectionEnd(this, nbtSection, registries);
            }
        }
        checkNeighbours = true;
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        if (section != null) {
            nbt.put("section", section.writeToNbt(registries));
        }
    }

    @Override
    public void update() {
        if (checkNeighbours) {
            checkNeighbours = false;
            Deque<TileHeatExchange> exchangers = findAdjacentExchangers();
            if (level.isClientSide) {
                // Find the start + end sections and link them up
                if (exchangers.size() > 2) {
                    TileHeatExchange start = exchangers.getFirst();
                    TileHeatExchange end = exchangers.getLast();
                    if (start.isStart() && end.isEnd()) {
                        ((ExchangeSectionStart) start.section).endSection = (ExchangeSectionEnd) end.section;
                    }
                }
            } else {
                if (exchangers.isEmpty()) {
                    // Something went wrong when searching
                    // (as normally this deque will contain this)
                    checkNeighbours = true;
                } else if (exchangers.size() < 3) {
                    for (TileHeatExchange tile : exchangers) {
                        tile.removeSection();
                    }
                } else if (exchangers.size() > 5) {
                    // TODO: Remove all exchangers sections
                } else {
                    ExchangeSectionStart sectionStart = null;
                    ExchangeSectionEnd sectionEnd = null;
                    for (TileHeatExchange exchange : exchangers) {
                        // For efficiency, only run this check once.
                        exchange.checkNeighbours = false;
                        if (exchange.section instanceof ExchangeSectionStart start) {
                            if (sectionStart == null) {
                                sectionStart = start;
                            } else {
                                // TODO: Attempt to merge sections together!
                            }
                        } else if (exchange.section instanceof ExchangeSectionEnd end) {
                            if (sectionEnd == null) {
                                sectionEnd = end;
                            } else {
                                // TODO: Attempt to merge sections together!
                            }
                        }
                        exchange.section = null;
                    }
                    if (sectionStart == null) {
                        sectionStart = new ExchangeSectionStart(exchangers.getFirst());
                    }
                    if (sectionEnd == null) {
                        sectionEnd = new ExchangeSectionEnd(exchangers.getLast());
                    }
                    sectionStart.endSection = sectionEnd;
                    sectionStart.middleCount = exchangers.size() - 2;
                    exchangers.getFirst().setSection(sectionStart);
                    exchangers.getLast().setSection(sectionEnd);
                    for (TileHeatExchange exchange : exchangers) {
                        exchange.onSectionChanged();
                        exchange.sendNetworkUpdate(NET_ID_CHANGE_SECTION);
                    }
                }
            }
        }
        if (section != null) {
            section.tick();
        }
    }

    private void removeSection() {
        if (section == null) {
            return;
        }
        NonNullList<ItemStack> list = NonNullList.create();
        section.tankManager.addDrops(list);
        InventoryUtil.dropAll(level, worldPosition, list);
        section = null;
        onSectionChanged();
        sendNetworkUpdate(NET_ID_CHANGE_SECTION);
    }

    /** Server side: shows which part this is in the block state, and tells pipes that the fluid sides changed. */
    private void onSectionChanged() {
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState state = getCurrentStateForBlock(BCFactoryBlocks.HEAT_EXCHANGE.get());
        if (state != null) {
            EnumExchangePart part = isStart() ? EnumExchangePart.START
                : isEnd() ? EnumExchangePart.END : EnumExchangePart.MIDDLE;
            if (state.getValue(BlockHeatExchange.PROP_PART) != part) {
                level.setBlock(worldPosition, state.setValue(BlockHeatExchange.PROP_PART, part), Block.UPDATE_CLIENTS);
            }
        }
        level.invalidateCapabilities(worldPosition);
        setChanged();
    }

    private Deque<TileHeatExchange> findAdjacentExchangers() {
        Direction thisFacing = getFacing();
        if (thisFacing == null) {
            // Odd. This means that we are getting a property from a different block
            return new ArrayDeque<>();
        }
        Direction dirToStart = thisFacing.getClockWise();
        Direction dirToEnd = thisFacing.getCounterClockWise();
        Deque<TileHeatExchange> exchangers = new ArrayDeque<>();
        exchangers.add(this);
        for (int i = 1; i < 6; i++) {
            BlockEntity neighbour = getLocalTile(worldPosition.relative(dirToStart, i));
            if (neighbour instanceof TileHeatExchange other) {
                if (other.getFacing() != thisFacing) {
                    break;
                }
                exchangers.addFirst(other);
            } else {
                break;
            }
        }
        for (int i = 1; i < 6; i++) {
            BlockEntity neighbour = getLocalTile(worldPosition.relative(dirToEnd, i));
            if (neighbour instanceof TileHeatExchange other) {
                if (other.getFacing() != thisFacing) {
                    break;
                }
                exchangers.addLast(other);
            } else {
                break;
            }
        }
        return exchangers;
    }

    private void setSection(ExchangeSection section) {
        if (this.section != section) {
            this.section = section;
            section.setTile(this);
            sendNetworkUpdate(NET_ID_CHANGE_SECTION);
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
        throws IOException {
        if (side == LogicalSide.CLIENT) {
            if (id == NET_RENDER_DATA) {
                readPayload(NET_ID_CHANGE_SECTION, buffer, side, ctx);
            } else if (id == NET_ID_CHANGE_SECTION) {
                if (buffer.readBoolean()) {
                    boolean start = buffer.readBoolean();
                    if (start) {
                        section = section instanceof ExchangeSectionStart ? section : new ExchangeSectionStart(this);
                    } else {
                        section = section instanceof ExchangeSectionEnd ? section : new ExchangeSectionEnd(this);
                    }
                    section.readPayload(NET_ID_CHANGE_SECTION, buffer, side, ctx);
                } else {
                    section = null;
                }
                checkNeighbours = true;
            } else if (section != null) {
                section.readPayload(id, buffer, side, ctx);
            }
        } else {
            super.readPayload(id, buffer, side, ctx);
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
        if (side == LogicalSide.SERVER) {
            if (id == NET_RENDER_DATA) {
                writePayload(NET_ID_CHANGE_SECTION, buffer, side);
            } else if (id == NET_ID_CHANGE_SECTION) {
                if (section == null) {
                    buffer.writeBoolean(false);
                } else {
                    buffer.writeBoolean(true);
                    buffer.writeBoolean(section instanceof ExchangeSectionStart);
                    section.writePayload(id, buffer, side);
                }
            } else if (section != null) {
                section.writePayload(id, buffer, side);
            }
        } else {
            super.writePayload(id, buffer, side);
        }
    }

    @Override
    @Nullable
    public <T> T getCapability(BlockCapability<T, @Nullable Direction> capability, @Nullable Direction side) {
        if (section != null) {
            return section.caps.getCapability(capability, side);
        }
        return null;
    }

    @Override
    public boolean onActivated(Player player, InteractionHand hand, ItemStack held) {
        if (section != null) {
            return section.tankManager.onActivated(player, worldPosition, hand);
        }
        return false;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (section instanceof ExchangeSectionStart start) {
            start.endSection = null;
        }
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        checkNeighbours = true;
    }

    @Override
    public void onNeighbourBlockChanged(Block block, BlockPos neighbour) {
        if (neighbour.getY() != worldPosition.getY()) {
            // Heat exchange tiles can only be horizontally adjacent
            return;
        }
        checkNeighbours = true;
    }

    @Override
    public void addDrops(NonNullList<ItemStack> toDrop) {
        super.addDrops(toDrop);
        if (section != null) {
            section.tankManager.addDrops(toDrop);
        }
    }

    /** Called by the wrench (through {@code BlockHeatExchange}).
     * <p>
     * If this exchanger is not part of a larger structure then this will rotate this block 90 degrees. If this is
     * part of a larger structure then all adjacent heat exchangers will be rotated 180 degrees to swap the start and
     * end blocks. */
    public boolean rotate() {
        Direction thisFacing = getFacing();
        if (thisFacing == null) {
            return false;
        }
        Deque<TileHeatExchange> exchangers = findAdjacentExchangers();
        if (exchangers.size() == 1) {
            // Just this one tile, so rotate this by 90 degrees
            BlockState rotated = getCurrentState().setValue(BlockBCBase_Neptune.PROP_FACING,
                VanillaRotationHandlers.ROTATE_HORIZONTAL.next(thisFacing));
            level.setBlockAndUpdate(worldPosition, BlockHeatExchange.withConnections(rotated, level, worldPosition));
        } else {
            // Rotate every heat exchanger 180 degrees
            ExchangeSectionStart start = null;
            ExchangeSectionEnd end = null;
            for (TileHeatExchange exchange : exchangers) {
                if (exchange.section instanceof ExchangeSectionStart s) {
                    start = s;
                } else if (exchange.section instanceof ExchangeSectionEnd e) {
                    end = e;
                }
                exchange.section = null;
                BlockState rotated = exchange.getCurrentState()
                    .setValue(BlockBCBase_Neptune.PROP_FACING, thisFacing.getOpposite());
                level.setBlockAndUpdate(exchange.getBlockPos(),
                    BlockHeatExchange.withConnections(rotated, level, exchange.getBlockPos()));
                exchange.checkNeighbours = true;
                exchange.markChunkDirty();
            }
            if (start != null) {
                TileHeatExchange tile = exchangers.getLast();
                tile.section = start;
                start.setTile(tile);
                tile.markChunkDirty();
                tile.sendNetworkUpdate(NET_ID_CHANGE_SECTION);
            }

            if (end != null) {
                TileHeatExchange tile = exchangers.getFirst();
                tile.section = end;
                end.setTile(tile);
                tile.markChunkDirty();
                tile.sendNetworkUpdate(NET_ID_CHANGE_SECTION);
            }
            for (TileHeatExchange exchange : exchangers) {
                exchange.onSectionChanged();
            }
        }

        SoundUtil.playSlideSound(level, worldPosition);
        return true;
    }

    public boolean isStart() {
        return section instanceof ExchangeSectionStart;
    }

    public boolean isEnd() {
        return section instanceof ExchangeSectionEnd;
    }

    @Nullable
    public ExchangeSection getSection() {
        return section;
    }

    @Nullable
    Direction getFacing() {
        BlockState state = getCurrentStateForBlock(BCFactoryBlocks.HEAT_EXCHANGE.get());
        if (state == null) {
            return null;
        }
        return state.getValue(BlockBCBase_Neptune.PROP_FACING);
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        if (section == null) {
            left.add("section = null");
        } else {
            left.add("section = " + (section instanceof ExchangeSectionStart ? "start" : "end"));
            section.getDebugInfo(left, right, side);
        }
    }

    public static abstract class ExchangeSection {
        final Tank tankInput, tankOutput;
        final TankManager tankManager;
        public final FluidSmoother smoothedTankInput, smoothedTankOutput;
        public final CapabilityHelper caps = new CapabilityHelper();
        private TileHeatExchange tile;

        ExchangeSection(TileHeatExchange tile) {
            this.tile = tile;
            tankInput = new Tank("input", 2 * 1000, tile);
            tankOutput = new Tank("output", 2 * 1000, tile);
            tankOutput.setCanFill(false);
            tankManager = new TankManager(tankOutput, tankInput);
            smoothedTankInput = createFluidSmoother(tankInput, NET_ID_TANK_IN);
            smoothedTankOutput = createFluidSmoother(tankOutput, NET_ID_TANK_OUT);
            this.setTile(tile);
        }

        ExchangeSection(TileHeatExchange tile, CompoundTag nbt, HolderLookup.Provider registries) {
            this(tile);
            tankInput.deserializeNBT(registries, nbt.getCompound("input"));
            tankOutput.deserializeNBT(registries, nbt.getCompound("output"));
        }

        FluidSmoother createFluidSmoother(Tank tank, int netId) {
            return new FluidSmoother(w -> getTile().createAndSendMessage(netId, w), tank);
        }

        CompoundTag writeToNbt(HolderLookup.Provider registries) {
            CompoundTag nbt = new CompoundTag();
            nbt.put("input", tankInput.serializeNBT(registries));
            nbt.put("output", tankOutput.serializeNBT(registries));
            return nbt;
        }

        void tick() {
            Level world = getTile().level;
            smoothedTankInput.tick(world);
            smoothedTankOutput.tick(world);
        }

        void readPayload(int id, PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
            throws IOException {
            if (side == LogicalSide.CLIENT) {
                if (id == NET_ID_CHANGE_SECTION) {
                    readPayload(NET_ID_TANK_IN, buffer, side, ctx);
                    readPayload(NET_ID_TANK_OUT, buffer, side, ctx);
                    smoothedTankInput.resetSmoothing(getTile().level);
                    smoothedTankOutput.resetSmoothing(getTile().level);
                } else if (id == NET_ID_TANK_IN) {
                    smoothedTankInput.handleMessage(getTile().level, buffer);
                } else if (id == NET_ID_TANK_OUT) {
                    smoothedTankOutput.handleMessage(getTile().level, buffer);
                }
            }
        }

        void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
            if (side == LogicalSide.SERVER) {
                if (id == NET_ID_CHANGE_SECTION) {
                    writePayload(NET_ID_TANK_IN, buffer, side);
                    writePayload(NET_ID_TANK_OUT, buffer, side);
                } else if (id == NET_ID_TANK_IN) {
                    smoothedTankInput.writeInit(buffer);
                } else if (id == NET_ID_TANK_OUT) {
                    smoothedTankOutput.writeInit(buffer);
                }
            }
        }

        void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
            left.add("tank_input = " + tankInput.getDebugString());
            left.add("tank_output = " + tankOutput.getDebugString());
            left.add("smoothed_input: ");
            smoothedTankInput.getDebugInfo(left, right, side);
            left.add("smoothed_output: ");
            smoothedTankOutput.getDebugInfo(left, right, side);
        }

        public TileHeatExchange getTile() {
            return tile;
        }

        public void setTile(TileHeatExchange tile) {
            this.tile = tile;
            tankInput.setTileEntity(tile);
            tankOutput.setTileEntity(tile);
        }

        /** For tests and the debugger: the tank fluid enters this section by. */
        public Tank getTankInput() {
            return tankInput;
        }

        public Tank getTankOutput() {
            return tankOutput;
        }
    }

    public static class ExchangeSectionStart extends ExchangeSection {

        @Nullable
        private ExchangeSectionEnd endSection;
        public int middleCount;
        private int progress = 0;
        private int progressLast = 0;
        private EnumProgressState progressState = EnumProgressState.OFF;
        private EnumProgressState lastSentState = EnumProgressState.OFF;
        private int inputCoolantAmountCharge = 0;
        private int inputHeatantAmountCharge = 0;

        {
            tankInput.setFilter(this::isHeatant);
            caps.addCapabilityInstance(Capabilities.FluidHandler.BLOCK, tankInput, EnumPipePart.DOWN);
            caps.addCapability(Capabilities.FluidHandler.BLOCK, this::getTankForSide, EnumPipePart.HORIZONTALS);
        }

        ExchangeSectionStart(TileHeatExchange tile) {
            super(tile);
        }

        ExchangeSectionStart(TileHeatExchange tile, CompoundTag nbt, HolderLookup.Provider registries) {
            super(tile, nbt, registries);
            inputCoolantAmountCharge = nbt.getInt("coolantCharge");
            inputHeatantAmountCharge = nbt.getInt("heatantCharge");
        }

        @Override
        CompoundTag writeToNbt(HolderLookup.Provider registries) {
            CompoundTag nbt = super.writeToNbt(registries);
            nbt.putBoolean("start", true);
            nbt.putInt("coolantCharge", inputCoolantAmountCharge);
            nbt.putInt("heatantCharge", inputHeatantAmountCharge);
            return nbt;
        }

        @Override
        void readPayload(int id, PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
            throws IOException {
            super.readPayload(id, buffer, side, ctx);
            if (side == LogicalSide.CLIENT) {
                if (id == NET_ID_CHANGE_SECTION) {
                    middleCount = buffer.readUnsignedByte();
                } else if (id == NET_ID_STATE) {
                    progressState = buffer.readEnum(EnumProgressState.class);
                }
            }
        }

        @Override
        void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
            super.writePayload(id, buffer, side);
            if (side == LogicalSide.SERVER) {
                if (id == NET_ID_CHANGE_SECTION) {
                    buffer.writeByte(middleCount);
                } else if (id == NET_ID_STATE) {
                    buffer.writeEnum(progressState);
                }
            }
        }

        @Nullable
        public ExchangeSectionEnd getEndSection() {
            return endSection;
        }

        public EnumProgressState getProgressState() {
            return progressState;
        }

        public double getProgress(float partialTicks) {
            return MathUtil.interp(partialTicks, progressLast, progress) / 120.0;
        }

        private boolean isHeatant(FluidStack fluid) {
            return BuildcraftRecipeRegistry.refineryRecipes.getHeatableRegistry().getRecipeForInput(fluid) != null;
        }

        @Nullable
        private IFluidHandler getTankForSide(@Nullable Direction side) {
            Direction thisFacing = getTile().getFacing();
            if (thisFacing == null || side != thisFacing.getClockWise()) {
                return null;
            }
            return tankOutput;
        }

        @Override
        void tick() {
            super.tick();

            updateProgress();
            if (getTile().level.isClientSide) {
                spawnParticles();
                return;
            }
            if (endSection != null) {
                craft();
            } else if (progressState != EnumProgressState.OFF) {
                progressState = EnumProgressState.STOPPING;
            }
            output();
            if (progressState != lastSentState) {
                lastSentState = progressState;
                getTile().sendNetworkUpdate(NET_ID_STATE);
            }
        }

        private void updateProgress() {
            progressLast = progress;
            switch (progressState) {
                case STOPPING: {
                    progress--;
                    if (progress <= 0) {
                        progress = 0;
                        progressState = EnumProgressState.OFF;
                    }
                    return;
                }
                case PREPARING:
                case RUNNING: {
                    int lag = 120;
                    progress++;
                    if (progress >= lag) {
                        progress = lag;
                        progressState = EnumProgressState.RUNNING;
                    }
                    return;
                }
                default: {
                    return;
                }
            }
        }

        private void craft() {
            Tank c_in = endSection.tankInput;
            Tank c_out = tankOutput;
            Tank h_in = tankInput;
            Tank h_out = endSection.tankOutput;
            IRefineryRecipeManager reg = BuildcraftRecipeRegistry.refineryRecipes;
            ICoolableRecipe c_recipe = reg.getCoolableRegistry().getRecipeForInput(c_in.getFluid());
            IHeatableRecipe h_recipe = reg.getHeatableRegistry().getRecipeForInput(h_in.getFluid());
            if (h_recipe == null || c_recipe == null) {
                progressState = EnumProgressState.STOPPING;
                return;
            }
            if (c_recipe.heatFrom() <= h_recipe.heatFrom()) {
                progressState = EnumProgressState.STOPPING;
                return;
            }
            int c_diff = c_recipe.heatFrom() - c_recipe.heatTo();
            int h_diff = h_recipe.heatTo() - h_recipe.heatFrom();
            if (h_diff < 1 || c_diff < 1) {
                throw new IllegalStateException("Invalid recipe " + c_recipe + ", " + h_recipe);
            }

            // Find the minimum common amount that we can process from each tank up to `max_amount`
            // min_common_multiplier == 0 indicates that we can no longer process (tanks full/empty)
            int max_amount = FLUID_MULT[middleCount - 1];
            FluidStack c_in_f = setAmount(c_recipe.in(), max_amount);
            FluidStack c_out_f = setAmount(c_recipe.out(), max_amount);
            FluidStack h_in_f = setAmount(h_recipe.in(), max_amount);
            FluidStack h_out_f = setAmount(h_recipe.out(), max_amount);

            // empty => the fluid is consumed in the process (e.g. water, lava)
            int c_out_amount = c_out_f.isEmpty() ? max_amount : c_out.fillInternal(c_out_f, FluidAction.SIMULATE);
            int h_out_amount = h_out_f.isEmpty() ? max_amount : h_out.fillInternal(h_out_f, FluidAction.SIMULATE);

            int c_in_amount = drainableAmount(c_in, c_in_f);
            int h_in_amount = drainableAmount(h_in, h_in_f);

            final int min_common_multiplier =
                Math.min(Math.min(Math.min(c_out_amount, h_out_amount), c_in_amount), h_in_amount);

            if (min_common_multiplier > 0) {
                c_in_f = setAmount(c_recipe.in(), min_common_multiplier);
                c_out_f = setAmount(c_recipe.out(), min_common_multiplier);
                h_in_f = setAmount(h_recipe.in(), min_common_multiplier);
                h_out_f = setAmount(h_recipe.out(), min_common_multiplier);

                if (progressState == EnumProgressState.OFF) {
                    progressState = EnumProgressState.PREPARING;
                } else if (progressState == EnumProgressState.RUNNING) {
                    fill(c_out, c_out_f);
                    drain(c_in, c_in_f);

                    fill(h_out, h_out_f);
                    drain(h_in, h_in_f);
                }
            } else {
                progressState = EnumProgressState.STOPPING;
            }
        }

        private void spawnParticles() {
            if (progressState == EnumProgressState.RUNNING) {
                ExchangeSectionEnd end = endSection;
                if (end == null) {
                    return;
                }
                Vec3 from = VecUtil.convertCenter(getTile().getBlockPos());
                FluidStack c_in_f = end.smoothedTankInput.getFluidForRender();
                if (c_in_f != null && c_in_f.getFluid().isSame(Fluids.LAVA)) {
                    Direction facing = getTile().getFacing();
                    if (facing != null) {
                        spewForth(from, facing.getClockWise(), ParticleTypes.LARGE_SMOKE);
                    }
                }

                FluidStack h_in_f = smoothedTankInput.getFluidForRender();
                from = VecUtil.convertCenter(end.getTile().getBlockPos());
                if (h_in_f != null && h_in_f.getFluid().isSame(Fluids.WATER)) {
                    spewForth(from, Direction.UP, ParticleTypes.CLOUD);
                }
            }
        }

        private void spewForth(Vec3 from, Direction dir, ParticleOptions particle) {
            Vec3 vecDir = Vec3.atLowerCornerOf(dir.getNormal());
            from = from.add(vecDir);

            double x = from.x;
            double y = from.y;
            double z = from.z;

            Vec3 motion = VecUtil.scale(vecDir, 0.4);
            ParticleStatus setting = Minecraft.getInstance().options.particles().get();
            Level w = getTile().getLevel();
            if (setting == ParticleStatus.MINIMAL || w == null) {
                return;
            }
            int particleCount = setting == ParticleStatus.ALL ? 5 : 2;
            for (int i = 0; i < particleCount; i++) {
                double dx = motion.x + (Math.random() - 0.5) * 0.1;
                double dy = motion.y + (Math.random() - 0.5) * 0.1;
                double dz = motion.z + (Math.random() - 0.5) * 0.1;
                double interp = i / (double) particleCount;
                x -= dx * interp;
                y -= dy * interp;
                z -= dz * interp;

                w.addParticle(particle, x, y, z, dx, dy, dz);
            }
        }

        private void output() {
            IFluidHandler thisOut = getFluidAutoOutputTarget();
            FluidUtilBC.move(tankOutput, thisOut, 1000);

            if (endSection != null) {
                IFluidHandler endOut = endSection.getFluidAutoOutputTarget();
                FluidUtilBC.move(endSection.tankOutput, endOut, 1000);
            }
        }

        private static FluidStack setAmount(FluidStack fluid, int mult) {
            if (fluid.isEmpty()) {
                return FluidStack.EMPTY;
            }
            return fluid.copyWithAmount(mult);
        }

        private static int drainableAmount(Tank t, FluidStack fluid) {
            FluidStack f2 = t.drainInternal(fluid, FluidAction.SIMULATE);
            return f2.getAmount();
        }

        private static void fill(Tank t, FluidStack fluid) {
            if (fluid.isEmpty()) {
                return;
            }
            int a = t.fillInternal(fluid, FluidAction.EXECUTE);
            if (a != fluid.getAmount()) {
                String err = "Buggy transition! Failed to fill " + fluid.getFluid();
                throw new IllegalStateException(err + " x " + fluid.getAmount() + " into " + t);
            }
        }

        private static void drain(Tank t, FluidStack fluid) {
            FluidStack f2 = t.drainInternal(fluid, FluidAction.EXECUTE);
            if (f2.getAmount() != fluid.getAmount()) {
                String err = "Buggy transition! Failed to drain " + fluid.getFluid();
                throw new IllegalStateException(err + " x " + fluid.getAmount() + " from " + t);
            }
        }

        @Nullable
        private IFluidHandler getFluidAutoOutputTarget() {
            Direction facing = getTile().getFacing();
            if (facing == null) {
                return null;
            }
            BlockEntity neighbour = getTile().getNeighbourTile(facing.getClockWise());
            if (neighbour == null) {
                return null;
            }
            return getTile().level.getCapability(Capabilities.FluidHandler.BLOCK, neighbour.getBlockPos(),
                facing.getCounterClockWise());
        }

        @Override
        void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
            super.getDebugInfo(left, right, side);
            left.add("progress = " + progress);
            left.add("state = " + progressState);
            left.add("has_end = " + (endSection != null));
        }
    }

    public static class ExchangeSectionEnd extends ExchangeSection {

        {
            tankInput.setFilter(this::isCoolant);
            caps.addCapabilityInstance(Capabilities.FluidHandler.BLOCK, tankOutput, EnumPipePart.UP);
            caps.addCapability(Capabilities.FluidHandler.BLOCK, this::getTankForSide, EnumPipePart.HORIZONTALS);
        }

        ExchangeSectionEnd(TileHeatExchange tile) {
            super(tile);
        }

        ExchangeSectionEnd(TileHeatExchange tile, CompoundTag nbt, HolderLookup.Provider registries) {
            super(tile, nbt, registries);
        }

        private boolean isCoolant(FluidStack fluid) {
            return BuildcraftRecipeRegistry.refineryRecipes.getCoolableRegistry().getRecipeForInput(fluid) != null;
        }

        @Nullable
        private IFluidHandler getTankForSide(@Nullable Direction side) {
            Direction thisFacing = getTile().getFacing();
            if (thisFacing == null || side != thisFacing.getCounterClockWise()) {
                return null;
            }
            return tankInput;
        }

        @Override
        CompoundTag writeToNbt(HolderLookup.Provider registries) {
            CompoundTag nbt = super.writeToNbt(registries);
            nbt.putBoolean("start", false);
            return nbt;
        }

        @Nullable
        IFluidHandler getFluidAutoOutputTarget() {
            BlockEntity neighbour = getTile().getNeighbourTile(Direction.UP);
            if (neighbour == null) {
                return null;
            }
            return getTile().level.getCapability(Capabilities.FluidHandler.BLOCK, neighbour.getBlockPos(),
                Direction.DOWN);
        }
    }

    public enum EnumProgressState {
        /** Progress is at 0, not moving. */
        OFF,
        /** Progress is increasing from 0 to max */
        PREPARING,
        /** progress stays at max */
        RUNNING,
        /** Progress is decreasing from max to 0. */
        STOPPING;
    }
}
