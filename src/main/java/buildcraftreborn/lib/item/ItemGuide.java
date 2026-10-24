/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.item;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.misc.AdvancementUtil;

/** The guide book: opens the book it is (the {@link BCItemComponents#GUIDE_BOOK} component), and unlocks the "How does
 * this work again?" advancement.
 * <p>
 * 1.21: the books are client resources, so the client sets {@link #openGui} and {@link #bookTitle}. */
public class ItemGuide extends Item implements IItemSubtypes {
    public static final ResourceLocation DEFAULT_BOOK = BuildCraftReborn.id("main");
    public static final ResourceLocation META_BOOK = BuildCraftReborn.id("meta");
    private static final ResourceLocation ADVANCEMENT = BuildCraftReborn.id("guide");

    /** Opens the guide for a book (client only). */
    public static BiConsumer<Player, ResourceLocation> openGui = (player, book) -> {};
    /** The title of a book, or null if it isn't known (client only). */
    public static Function<ResourceLocation, Component> bookTitle = book -> null;

    public ItemGuide(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide) {
            openGui.accept(player, getBookName(stack));
        } else {
            AdvancementUtil.unlockAdvancement(player, ADVANCEMENT);
        }
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
    }

    /** One guide per book. (1.12 listed every loaded book; the books are only known on the client now, so these are
     * BuildCraft's two.) */
    @Override
    public void addSubtypes(Consumer<ItemStack> output) {
        output.accept(new ItemStack(this));
        ItemStack meta = new ItemStack(this);
        setBookName(meta, META_BOOK);
        output.accept(meta);
    }

    @Override
    public Component getName(ItemStack stack) {
        ResourceLocation book = getBookName(stack);
        if (!book.equals(DEFAULT_BOOK)) {
            Component title = bookTitle.apply(book);
            if (title != null) {
                return title;
            }
        }
        return super.getName(stack);
    }

    public static ResourceLocation getBookName(ItemStack stack) {
        // So that guide books without a book open the main one
        return stack.getOrDefault(BCItemComponents.GUIDE_BOOK.get(), DEFAULT_BOOK);
    }

    public static void setBookName(ItemStack stack, ResourceLocation book) {
        if (book.equals(DEFAULT_BOOK)) {
            stack.remove(BCItemComponents.GUIDE_BOOK.get());
        } else {
            stack.set(BCItemComponents.GUIDE_BOOK.get(), book);
        }
    }
}
