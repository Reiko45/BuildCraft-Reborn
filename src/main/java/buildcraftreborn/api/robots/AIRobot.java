/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.robots;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.mj.MjAPI;

/** One step of a robot's behaviour. An AI can hand work to a delegate AI, which runs until it ends (or is aborted)
 * and then tells its parent. The redstone board's AI is the root of this chain.
 * <p>
 * 1.21: NBT is a {@link CompoundTag}; anything that needs the registries (item stacks) can get them from
 * {@code robot.registryAccess()}. */
public class AIRobot {
    private static final Logger LOGGER = LogUtils.getLogger();

    public EntityRobotBase robot;

    private AIRobot delegateAI;
    private AIRobot parentAI;

    private boolean success;

    public AIRobot(EntityRobotBase iRobot) {
        robot = iRobot;
        success = true;
    }

    public void start() {

    }

    public void preempt(@Nullable AIRobot ai) {

    }

    public void update() {
        // Update should always handle terminate. Some AI are not using update
        // at all, their code being in start() and end(). In these case,
        // calling update is a malfunction, the ai should be terminated.
        terminate();
    }

    public void end() {

    }

    /** This gets called when a delegate AI ends work naturally.
     *
     * @param ai The delegate AI which ended work. */
    public void delegateAIEnded(AIRobot ai) {

    }

    /** This gets called when a delegate AI is forcibly aborted.
     *
     * @param ai The delegate AI which was aborted. */
    public void delegateAIAborted(AIRobot ai) {

    }

    public void writeSelfToNBT(CompoundTag nbt) {

    }

    public void loadSelfFromNBT(CompoundTag nbt) {

    }

    public boolean success() {
        return success;
    }

    protected void setSuccess(boolean iSuccess) {
        success = iSuccess;
    }

    /** @return The power (in micro MJ) this AI uses each tick it runs. */
    public long getPowerCost() {
        return MjAPI.MJ / 10;
    }

    public boolean canLoadFromNBT() {
        return false;
    }

    /** Tries to receive items in parameters, return items that are left after the operation. */
    public ItemStack receiveItem(ItemStack stack) {
        return stack;
    }

    public final void terminate() {
        abortDelegateAI();
        end();

        if (parentAI != null) {
            parentAI.delegateAI = null;
            parentAI.delegateAIEnded(this);
        }
    }

    public final void abort() {
        abortDelegateAI();

        try {
            end();

            if (parentAI != null) {
                parentAI.delegateAI = null;
                parentAI.delegateAIAborted(this);
            }
        } catch (Throwable e) {
            LOGGER.error("Robot AI {} failed to end", getClass().getName(), e);
            delegateAI = null;

            if (parentAI != null) {
                parentAI.delegateAI = null;
            }
        }
    }

    public final void cycle() {
        try {
            preempt(delegateAI);

            if (delegateAI != null) {
                delegateAI.cycle();
            } else {
                robot.getBattery().extractPower(0, getPowerCost());
                update();
            }
        } catch (Throwable e) {
            LOGGER.error("Robot AI {} crashed, aborting it", getClass().getName(), e);
            abort();
        }
    }

    public final void startDelegateAI(AIRobot ai) {
        abortDelegateAI();
        delegateAI = ai;
        ai.parentAI = this;
        delegateAI.start();
    }

    public final void abortDelegateAI() {
        if (delegateAI != null) {
            delegateAI.abort();
        }
    }

    public final AIRobot getActiveAI() {
        if (delegateAI != null) {
            return delegateAI.getActiveAI();
        } else {
            return this;
        }
    }

    @Nullable
    public final AIRobot getDelegateAI() {
        return delegateAI;
    }

    public final void writeToNBT(CompoundTag nbt) {
        nbt.putString("aiName", RobotManager.getAIRobotName(getClass()));

        CompoundTag data = new CompoundTag();
        writeSelfToNBT(data);
        nbt.put("data", data);

        if (delegateAI != null && delegateAI.canLoadFromNBT()) {
            CompoundTag sub = new CompoundTag();

            delegateAI.writeToNBT(sub);
            nbt.put("delegateAI", sub);
        }
    }

    public final void loadFromNBT(CompoundTag nbt) {
        loadSelfFromNBT(nbt.getCompound("data"));

        if (nbt.contains("delegateAI")) {
            CompoundTag sub = nbt.getCompound("delegateAI");

            try {
                Class<? extends AIRobot> aiRobotClass = RobotManager.getAIRobotByName(sub.getString("aiName"));
                if (aiRobotClass != null) {
                    delegateAI = aiRobotClass.getConstructor(EntityRobotBase.class).newInstance(robot);
                    delegateAI.parentAI = this;

                    if (delegateAI.canLoadFromNBT()) {
                        delegateAI.loadFromNBT(sub);
                    }
                }
            } catch (Throwable e) {
                LOGGER.error("Couldn't load the robot AI {}", sub.getString("aiName"), e);
            }
        }
    }

    @Nullable
    public static AIRobot loadAI(CompoundTag nbt, EntityRobotBase robot) {
        AIRobot ai = null;

        try {
            Class<? extends AIRobot> aiRobotClass = RobotManager.getAIRobotByName(nbt.getString("aiName"));
            if (aiRobotClass != null) {
                ai = aiRobotClass.getConstructor(EntityRobotBase.class).newInstance(robot);
                ai.loadFromNBT(nbt);
            }
        } catch (Throwable e) {
            LOGGER.error("Couldn't load the robot AI {}", nbt.getString("aiName"), e);
        }

        return ai;
    }
}
