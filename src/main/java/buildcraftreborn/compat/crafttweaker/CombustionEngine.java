/*
 * Based on BuildCraftCompat, (c) the BuildCraft Team, http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraftCompat is distributed under the terms of the Minecraft Mod Public License 1.0.1, or MMPL (a copy is in
 * LICENSE-ASSETS).
 */
package buildcraftreborn.compat.crafttweaker;

import org.openzen.zencode.java.ZenCodeType;

import net.neoforged.neoforge.fluids.FluidStack;

import buildcraftreborn.api.fuels.ICoolant;
import buildcraftreborn.api.fuels.IFuel;
import buildcraftreborn.api.fuels.ISolidCoolant;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.lib.fluid.CoolantRegistry;
import buildcraftreborn.lib.fluid.FuelRegistry;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.item.IItemStack;

/** CraftTweaker's {@code mods.buildcraftreborn.CombustionEngine} (1.12: {@code mods.buildcraft.CombustionEngine}):
 * adds combustion engine fuels and (new) coolants. The built-in ones are data maps, which win over these; change those
 * with a data pack.
 *
 * <pre>
 * mods.buildcraftreborn.CombustionEngine.addCleanFuel(&lt;fluid:minecraft:lava&gt;, 2, 20000);
 * mods.buildcraftreborn.CombustionEngine.addCoolant(&lt;fluid:minecraft:milk&gt;, 0.001);
 * </pre> */
@ZenRegister
@ZenCodeType.Name("mods.buildcraftreborn.CombustionEngine")
public final class CombustionEngine {
    private CombustionEngine() {}

    /** A fuel that gives {@code powerPerTick} MJ every tick for {@code timePerBucket} ticks per bucket. */
    @ZenCodeType.Method
    public static void addCleanFuel(IFluidStack fuel, double powerPerTick, int timePerBucket) {
        FluidStack fluid = fuelFluid(fuel);
        addFuel(fluid, new FuelRegistry.Fuel(fluid, Math.round(powerPerTick * MjAPI.MJ), timePerBucket));
    }

    /** A fuel that also leaves {@code residue} behind (per bucket burnt). */
    @ZenCodeType.Method
    public static void addDirtyFuel(IFluidStack fuel, double powerPerTick, int timePerBucket, IFluidStack residue) {
        FluidStack fluid = fuelFluid(fuel);
        addFuel(fluid, new FuelRegistry.DirtyFuel(fluid, Math.round(powerPerTick * MjAPI.MJ), timePerBucket,
            ScriptActions.fluid(residue)));
    }

    private static FluidStack fuelFluid(IFluidStack fuel) {
        FluidStack fluid = ScriptActions.fluid(fuel);
        if (fluid.isEmpty()) {
            throw new IllegalArgumentException("A fuel can't be empty");
        }
        return fluid.copyWithAmount(1);
    }

    private static void addFuel(FluidStack fluid, IFuel entry) {
        CraftTweakerAPI.apply(ScriptActions.change(ScriptActions.key("fuel", fluid),
            "combustion engine fuel " + ScriptActions.name(fluid),
            () -> FuelRegistry.INSTANCE.addFuel(entry), () -> FuelRegistry.INSTANCE.removeFuel(entry)));
    }

    /** A coolant fluid: each mB cools the engine by {@code degreesPerMb} degrees. */
    @ZenCodeType.Method
    public static void addCoolant(IFluidStack coolant, float degreesPerMb) {
        FluidStack fluid = fuelFluid(coolant);
        ICoolant entry = new CoolantRegistry.Coolant(fluid, degreesPerMb);
        CraftTweakerAPI.apply(ScriptActions.change(ScriptActions.key("coolant", fluid),
            "combustion engine coolant " + ScriptActions.name(fluid),
            () -> CoolantRegistry.INSTANCE.addCoolant(entry), () -> CoolantRegistry.INSTANCE.removeCoolant(entry)));
    }

    /** An item the engine melts into {@code coolant} (one item gives that much). */
    @ZenCodeType.Method
    public static void addSolidCoolant(IItemStack solid, IFluidStack coolant) {
        var stack = solid.getInternal().copyWithCount(1);
        ISolidCoolant entry = new CoolantRegistry.SolidCoolant(stack, ScriptActions.fluid(coolant), 1);
        CraftTweakerAPI.apply(ScriptActions.change(ScriptActions.key("solid_coolant", stack),
            "combustion engine solid coolant " + ScriptActions.name(stack),
            () -> CoolantRegistry.INSTANCE.addSolidCoolant(entry),
            () -> CoolantRegistry.INSTANCE.removeSolidCoolant(entry)));
    }
}
