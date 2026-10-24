/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.util;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

/** Runs an {@link IIterableAlgorithm} (a path finder or a block search) a step at a time.
 * <p>
 * 1.21: 7.1 ran each search on its own thread, reading blocks while the server changed them. Here the searches run
 * on the server thread at the end of each tick ({@link #tickAll()}), sharing a few milliseconds between every robot
 * that is searching; a robot whose search didn't get a turn goes first the next tick. Steps are a tenth of 7.1's, so
 * {@code maxIterations} is multiplied by ten to search as far. */
public class IterableAlgorithmRunner {
    private static final Logger LOGGER = LogUtils.getLogger();

    /** How long all the searches together may take each tick. */
    private static final long BUDGET_NANOS = 4_000_000;
    /** How many steps one search may take each tick, so a single long search doesn't take the whole budget. */
    private static final int MAX_STEPS_PER_TICK = 20;

    private static final List<IterableAlgorithmRunner> RUNNING = new ArrayList<>();
    private static int nextIndex = 0;

    private final IIterableAlgorithm pathFinding;
    private final int maxIterations;
    private int iterations = 0;

    private boolean stop = false;
    private boolean done = false;

    public IterableAlgorithmRunner(IIterableAlgorithm iPathFinding, int iMaxIterations) {
        pathFinding = iPathFinding;
        maxIterations = iMaxIterations * 10;
    }

    public IterableAlgorithmRunner(IIterableAlgorithm iPathFinding) {
        this(iPathFinding, 1000);
    }

    /** Queues the search; it runs over the next ticks. */
    public void start() {
        if (!RUNNING.contains(this)) {
            RUNNING.add(this);
        }
    }

    public void terminate() {
        stop = true;
    }

    public boolean isTerminated() {
        return stop;
    }

    public boolean isDone() {
        return done;
    }

    /** @return True if this has finished (or should stop), marking it done. */
    private boolean checkFinished() {
        if (stop || iterations >= maxIterations || pathFinding.isDone()) {
            done = true;
        }
        return done;
    }

    /** Runs the queued searches for this tick. */
    public static void tickAll() {
        if (RUNNING.isEmpty()) {
            return;
        }
        long deadline = System.nanoTime() + BUDGET_NANOS;
        int count = RUNNING.size();
        int start = nextIndex % count;
        int index = start;
        boolean outOfTime = false;
        for (int n = 0; n < count; n++) {
            index = (start + n) % count;
            IterableAlgorithmRunner runner = RUNNING.get(index);
            if (runner.done || runner.checkFinished()) {
                continue;
            }
            if (System.nanoTime() > deadline) {
                outOfTime = true;
                break;
            }
            try {
                for (int s = 0; s < MAX_STEPS_PER_TICK && !runner.checkFinished(); s++) {
                    runner.pathFinding.iterate();
                    runner.iterations++;
                    if (System.nanoTime() > deadline) {
                        break;
                    }
                }
            } catch (Throwable t) {
                LOGGER.error("A robot's search failed", t);
                runner.done = true;
            }
            runner.checkFinished();
        }
        // Whoever didn't get a turn this tick goes first next tick
        nextIndex = outOfTime ? index : 0;
        RUNNING.removeIf(r -> r.done);
        if (RUNNING.isEmpty()) {
            nextIndex = 0;
        } else {
            nextIndex = Math.min(nextIndex, RUNNING.size() - 1);
        }
    }

    /** Forgets every search (when the server stops). */
    public static void clearAll() {
        for (IterableAlgorithmRunner runner : RUNNING) {
            runner.stop = true;
            runner.done = true;
        }
        RUNNING.clear();
        nextIndex = 0;
    }
}
