/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.statements;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.core.IZone;
import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.api.items.IMapLocation;
import buildcraftreborn.api.statements.IActionInternal;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.StatementSlot;
import buildcraftreborn.core.statements.BCStatement;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.robotics.BCRoboticsSprites;

/** Gives the station's robots an area to work in, or to load and unload in: a map location (a spot, an area, or a
 * zone painted in the zone planner). */
public class ActionRobotWorkInArea extends BCStatement implements IActionInternal {

    public enum AreaType {
        WORK("work_in_area"),
        LOAD_UNLOAD("load_unload_area");

        private final String name;

        AreaType(String iName) {
            name = iName;
        }

        public String getTag() {
            return "buildcraft:robot." + name;
        }

        public String getUnlocalizedName() {
            return "gate.action.robot." + name;
        }
    }

    private final AreaType areaType;

    public ActionRobotWorkInArea(AreaType areaType) {
        super(areaType.getTag());
        this.areaType = areaType;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize(areaType.getUnlocalizedName());
    }

    @Override
    public ISprite getSprite() {
        return areaType == AreaType.WORK ? BCRoboticsSprites.ACTION_ROBOT_WORK_IN_AREA
            : BCRoboticsSprites.ACTION_ROBOT_LOAD_UNLOAD_AREA;
    }

    @Nullable
    public static IZone getArea(StatementSlot slot) {
        if (slot.parameters.length == 0 || slot.parameters[0] == null) {
            return null;
        }

        ItemStack stack = slot.parameters[0].getItemStack();

        if (!(stack.getItem() instanceof IMapLocation map)) {
            return null;
        }

        return map.getZone(stack);
    }

    @Override
    public int minParameters() {
        return 1;
    }

    @Override
    public int maxParameters() {
        return 1;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterMapLocation();
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {}

    public AreaType getAreaType() {
        return areaType;
    }
}
