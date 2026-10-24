/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.fluid;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.fluids.FluidStack;

import buildcraftreborn.api.core.SafeTimeTracker;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.lib.BCLibConfig;
import buildcraftreborn.lib.misc.MathUtil;
import buildcraftreborn.lib.net.IPayloadWriter;
import buildcraftreborn.lib.net.PacketBufferBC;

/** Sends a tank's contents to the client at most every {@code updateFactor} ticks, and smooths the amount between
 * updates so that a filling or draining tank moves gradually instead of jumping.
 * <p>
 * 1.21: the fluid is written as a {@link FluidStack} rather than an id into 1.12's networked fluid cache. */
public class FluidSmoother implements IDebuggable {
    final IFluidDataSender sender;
    final Tank tank;
    @Nullable
    _Side data;

    public FluidSmoother(IFluidDataSender sender, Tank tank) {
        this.sender = sender;
        this.tank = tank;
    }

    public void tick(@Nullable Level world) {
        if (data == null) {
            if (world == null) {
                return;
            }
            data = world.isClientSide ? new _Client() : new _Server();
        }
        data.tick(world);
    }

    public void handleMessage(Level world, PacketBufferBC buffer) {
        if (data == null) {
            data = new _Client();
        }
        if (data instanceof _Client client) {
            client.handleMessage(world, buffer);
        } else {
            throw new IllegalStateException("You can only call this on the client!");
        }
    }

    public void writeInit(PacketBufferBC buffer) {
        if (data == null) {
            data = new _Server();
        }
        if (data instanceof _Server server) {
            server.writeMessage(buffer);
        } else {
            throw new IllegalStateException("You can only call this on the server!");
        }
    }

    public void resetSmoothing(Level world) {
        if (data == null && world.isClientSide) {
            data = new _Client();
        }
        if (data instanceof _Client client) {
            client.resetSmoothing(world);
        } else {
            throw new IllegalStateException("You can only call this on the client!");
        }
    }

    /** @return The fluid to draw on the client (with its smoothed amount), or null if there is nothing to draw. */
    @Nullable
    public FluidStack getFluidForRender() {
        if (data instanceof _Client client) {
            if (client.fluid.isEmpty()) {
                return null;
            }
            return client.fluid.copyWithAmount(client.amount);
        }
        return null;
    }

    @Nullable
    public FluidStackInterp getFluidForRender(double partialTicks) {
        if (data instanceof _Client client) {
            if (client.fluid.isEmpty()) {
                return null;
            }
            double amount = client.amountLast * (1 - partialTicks) + client.amount * partialTicks;
            return new FluidStackInterp(client.fluid, amount);
        }
        return null;
    }

    /** Delegate for {@link Tank#getCapacity()} - useful if this is the only object exposed for rendering. */
    public int getCapacity() {
        return tank.getCapacity();
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        if (data != null) {
            data.getDebugInfo(left, right, side);
        }
    }

    @FunctionalInterface
    public interface IFluidDataSender {
        void writePacket(IPayloadWriter writer);
    }

    /** A fluid with a fractional (interpolated) amount. {@link #fluid}'s own amount is not meaningful. */
    public static class FluidStackInterp {
        public final FluidStack fluid;
        public final double amount;

        public FluidStackInterp(FluidStack fluid, double amount) {
            this.fluid = fluid;
            this.amount = amount;
        }
    }

    abstract class _Side implements IDebuggable {
        abstract void tick(Level world);
    }

    final class _Server extends _Side {
        private int sentAmount = -1;
        private boolean sentHasFluid = false;
        private final SafeTimeTracker tracker = new SafeTimeTracker(BCLibConfig.networkUpdateRate(), 4);

        @Override
        void tick(Level world) {
            boolean hasFluid = !tank.getFluid().isEmpty();
            if ((tank.getFluidAmount() != sentAmount || hasFluid != sentHasFluid)) {
                if (tracker.markTimeIfDelay(world)) {
                    sender.writePacket(this::writeMessage);
                }
            }
        }

        void writeMessage(PacketBufferBC buffer) {
            FluidStack fluid = tank.getFluid();
            boolean hasFluid = !fluid.isEmpty();

            sentAmount = tank.getFluidAmount();
            sentHasFluid = hasFluid;

            buffer.writeInt(sentAmount);
            if (hasFluid) {
                buffer.writeBoolean(true);
                FluidStack.STREAM_CODEC.encode(buffer, fluid.copyWithAmount(1));
            } else {
                buffer.writeBoolean(false);
            }
        }

        @Override
        public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
            String contents = tank.getFluid().isEmpty() ? "Nothing" : "Something";
            left.add("current = " + tank.getFluidAmount() + " of " + contents);
            left.add("lastSent = " + sentAmount + " of " + (sentHasFluid ? "Something" : "Nothing"));
        }
    }

    final class _Client extends _Side {
        private int target;
        int amount, amountLast;
        long lastMessage, lastMessageMinus1;
        /** As in 1.12, the last fluid received is kept when the tank empties (the amount drops to 0 instead). */
        FluidStack fluid = FluidStack.EMPTY;

        @Override
        void tick(Level world) {
            amountLast = amount;
            if (amount != target) {
                int delta = target - amount;
                long msgDelta = lastMessage - lastMessageMinus1;
                msgDelta = MathUtil.clamp((int) msgDelta, 1, 10);
                if (Math.abs(delta) < msgDelta) {
                    amount += delta;
                } else {
                    amount += delta / (int) msgDelta;
                }
            }
        }

        void handleMessage(Level world, PacketBufferBC buffer) {
            target = buffer.readInt();
            if (buffer.readBoolean()) {
                fluid = FluidStack.STREAM_CODEC.decode(buffer);
            }
            lastMessageMinus1 = lastMessage;
            lastMessage = world.getGameTime();
        }

        void resetSmoothing(Level world) {
            lastMessageMinus1 = lastMessage = world.getGameTime();
            lastMessageMinus1 -= 1;
        }

        @Override
        public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
            left.add("shown = " + amount + ", target = " + target);
            left.add("lastMsg = " + lastMessage + ", lastMsg-1 = " + lastMessageMinus1 + ", diff = "
                + (lastMessage - lastMessageMinus1));
        }
    }
}
