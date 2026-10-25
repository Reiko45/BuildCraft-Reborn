/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.silicon.tile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.lib.misc.InventoryUtil;
import buildcraftreborn.lib.misc.StackUtil;
import buildcraftreborn.lib.tile.ITickable;
import buildcraftreborn.lib.tile.TileBC_Neptune;
import buildcraftreborn.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraftreborn.lib.tile.item.ItemHandlerSimple;
import buildcraftreborn.silicon.BCSiliconPackaging;
import buildcraftreborn.silicon.container.ContainerPackager;
import buildcraftreborn.silicon.item.ItemPackage;

/** The recipe packager. Each of the nine pattern slots can be off, set to an item, or set to "anything". Every quarter
 * of a second it takes one item for each set slot, from its own nine storage slots first and then from the
 * inventories beside it, and packs them into a piece of paper, which becomes a package with each item in its slot.
 * All the "anything" slots get the same item, one that no slot asks for by name. A package can go back in to have its
 * empty slots filled.
 * <p>
 * Pipes put ingredients in from the sides and paper or packages in from the top or bottom, and take packages out of
 * any side (as in 7.1). */
public class TilePackager extends TileBC_Neptune implements ITickable, MenuProvider, IDebuggable {
    private static final int PATTERN_SLOTS = 9;
    /** 7.1 looked at the inventories on its four horizontal sides, in this order. */
    private static final Direction[] NEIGHBOURS = { Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST };

    public final ItemHandlerSimple invPattern = itemManager.addInvHandler("pattern", PATTERN_SLOTS,
        EnumAccess.PHANTOM);
    public final ItemHandlerSimple invStorage = itemManager.addInvHandler("storage", 9, this::canStore,
        EnumAccess.INSERT, EnumPipePart.NORTH, EnumPipePart.SOUTH, EnumPipePart.WEST, EnumPipePart.EAST,
        EnumPipePart.CENTER);
    public final ItemHandlerSimple invPaper = itemManager.addInvHandler("paper", 1,
        (slot, stack) -> isPackageBase(stack), EnumAccess.INSERT, EnumPipePart.UP, EnumPipePart.DOWN,
        EnumPipePart.CENTER);
    public final ItemHandlerSimple invOutput = itemManager.addInvHandler("output", 1, EnumAccess.EXTRACT,
        EnumPipePart.VALUES);

    /** Bit i is set if pattern slot i is in use. */
    public int patternSet;
    private int updateTime;

    public TilePackager(BlockPos pos, BlockState state) {
        super(BCSiliconPackaging.PACKAGER_TILE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null) {
            updateTime = level.getRandom().nextInt(5);
        }
    }

    public static boolean isPackageBase(ItemStack stack) {
        return stack.is(Items.PAPER) || stack.getItem() instanceof ItemPackage;
    }

    public boolean isPatternSlotSet(int slot) {
        return (patternSet & (1 << slot)) != 0;
    }

    public void setPatternSlot(int slot, boolean set) {
        if (set) {
            patternSet |= 1 << slot;
        } else {
            patternSet &= ~(1 << slot);
        }
        setChanged();
    }

    /** 7.1: an ingredient goes in if it tops up the stack in that slot, or if a pattern slot could use it. */
    private boolean canStore(int slot, ItemStack stack) {
        ItemStack inSlot = invStorage.getStackInSlot(slot);
        if (!inSlot.isEmpty() && StackUtil.canMerge(stack, inSlot)) {
            return true;
        }
        for (int i = 0; i < PATTERN_SLOTS; i++) {
            if (isPatternSlotSet(i)) {
                ItemStack filter = invPattern.getStackInSlot(i);
                if (filter.isEmpty() || ItemStack.isSameItem(filter, stack)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void update() {
        if (level.isClientSide) {
            return;
        }
        if ((updateTime++) % 5 == 0 && patternSet != 0) {
            tryPacking();
        }
    }

    /** Somewhere an ingredient can be taken from: a slot of this packager's storage, or of a neighbour. */
    private record Source(IItemHandler handler, int slot) {
        ItemStack stack() {
            return handler.getStackInSlot(slot);
        }

        int available() {
            ItemStack stack = stack();
            return stack.isEmpty() ? 0 : handler.extractItem(slot, stack.getCount(), true).getCount();
        }
    }

    private List<Source> findSources() {
        List<Source> sources = new ArrayList<>();
        for (int i = 0; i < invStorage.getSlots(); i++) {
            sources.add(new Source(invStorage, i));
        }
        for (Direction dir : NEIGHBOURS) {
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, worldPosition.relative(dir),
                dir.getOpposite());
            if (handler != null) {
                for (int i = 0; i < handler.getSlots(); i++) {
                    sources.add(new Source(handler, i));
                }
            }
        }
        return sources;
    }

    /** Packs one package, if it can.
     *
     * @return True if a package was made. */
    public boolean tryPacking() {
        if (!invOutput.getStackInSlot(0).isEmpty()) {
            return false;
        }
        ItemStack base = invPaper.getStackInSlot(0);
        if (!isPackageBase(base)) {
            return false;
        }
        boolean isPackage = base.getItem() instanceof ItemPackage;
        NonNullList<ItemStack> contents = isPackage ? ItemPackage.getContents(base)
            : NonNullList.withSize(ItemPackage.SLOTS, ItemStack.EMPTY);
        // A package going round again can't already have something where the pattern wants to put something
        for (int i = 0; i < PATTERN_SLOTS; i++) {
            if (isPatternSlotSet(i) && !contents.get(i).isEmpty()) {
                return false;
            }
        }

        List<Source> sources = findSources();
        Map<Source, Integer> used = new HashMap<>();
        Source[] chosen = new Source[PATTERN_SLOTS];
        int anything = 0;

        // The slots that ask for an item by name
        for (int i = 0; i < PATTERN_SLOTS; i++) {
            if (!isPatternSlotSet(i)) {
                continue;
            }
            ItemStack filter = invPattern.getStackInSlot(i);
            if (filter.isEmpty()) {
                anything++;
                continue;
            }
            for (Source source : sources) {
                if (ItemStack.isSameItem(filter, source.stack())
                    && source.available() > used.getOrDefault(source, 0)) {
                    chosen[i] = source;
                    used.merge(source, 1, Integer::sum);
                    break;
                }
            }
            if (chosen[i] == null) {
                return false;
            }
        }

        // The "anything" slots: all from one stack that isn't being used, of an item no slot names
        if (anything > 0) {
            Source wild = null;
            for (Source source : sources) {
                if (!used.containsKey(source) && source.available() >= anything && !isNamedInPattern(source.stack())) {
                    wild = source;
                    break;
                }
            }
            if (wild == null) {
                return false;
            }
            for (int i = 0; i < PATTERN_SLOTS; i++) {
                if (isPatternSlotSet(i) && invPattern.getStackInSlot(i).isEmpty()) {
                    chosen[i] = wild;
                }
            }
        }

        // Everything's there: take it
        for (int i = 0; i < PATTERN_SLOTS; i++) {
            if (chosen[i] == null) {
                continue;
            }
            ItemStack taken = chosen[i].handler().extractItem(chosen[i].slot(), 1, false);
            if (taken.isEmpty()) {
                // The inventory said it could give it but didn't: put back what was taken
                for (int j = 0; j < i; j++) {
                    if (chosen[j] != null && !contents.get(j).isEmpty()) {
                        ItemStack left = invStorage.insert(contents.get(j), false, false);
                        InventoryUtil.drop(level, worldPosition.above(), left);
                        contents.set(j, ItemStack.EMPTY);
                    }
                }
                return false;
            }
            contents.set(i, taken);
        }
        ItemStack pkg = isPackage ? base.copyWithCount(1) : new ItemStack(BCSiliconPackaging.PACKAGE.get());
        ItemPackage.setContents(pkg, contents);
        invPaper.setStackInSlot(0, base.copyWithCount(base.getCount() - 1));
        invOutput.setStackInSlot(0, pkg);
        return true;
    }

    private boolean isNamedInPattern(ItemStack stack) {
        for (int i = 0; i < PATTERN_SLOTS; i++) {
            if (isPatternSlotSet(i) && ItemStack.isSameItem(invPattern.getStackInSlot(i), stack)) {
                return true;
            }
        }
        return false;
    }

    // GUI

    @Override
    public boolean onActivated(Player player, InteractionHand hand, ItemStack held) {
        if (!level.isClientSide) {
            openGui(player);
        }
        return true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        return new ContainerPackager(windowId, player, this);
    }

    // Saving

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.putShort("patternSet", (short) patternSet);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        patternSet = nbt.getShort("patternSet") & 0x1FF;
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        StringBuilder pattern = new StringBuilder();
        for (int i = 0; i < PATTERN_SLOTS; i++) {
            pattern.append(!isPatternSlotSet(i) ? '-' : invPattern.getStackInSlot(i).isEmpty() ? '*' : 'X');
        }
        left.add("pattern - " + pattern);
    }
}
