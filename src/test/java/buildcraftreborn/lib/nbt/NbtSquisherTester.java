/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.nbt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import buildcraftreborn.api.data.NbtSquishConstants;
import buildcraftreborn.lib.misc.HashUtil;

/** Round trips a large blueprint-like compound through the formats snapshots are saved in.
 * <p>
 * 1.21: only the vanilla formats exist now (see {@link NbtSquisher}), so 1.12's {@code BUILDCRAFT_V1} cases, its
 * timing output and its benchmark {@code main} are gone. */
public class NbtSquisherTester {
    private static final String[] IDS = {
        "minecraft:dirt", "minecraft:cooked_porkchop", "minecraft:cooked_beef", "minecraft:stick",
        "minecraft:diamond", "buildcraftreborn:gear_wood", "buildcraftreborn:gear_stone"
    };

    public static final CompoundTag nbt = genNbt(64 * 64 * 64);
    public static final CompoundTag nbtSmall = genNbt(10);

    @Test
    public void testVanillaCompressed() throws IOException {
        for (CompoundTag tag : new CompoundTag[] { nbtSmall, nbt }) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            NbtSquisher.squishVanilla(tag, out);
            byte[] bytes = out.toByteArray();
            Assert.assertEquals(NbtSquishConstants.BUILDCRAFT_MAGIC_1, Byte.toUnsignedInt(bytes[0]));
            Assert.assertEquals(NbtSquishConstants.BUILDCRAFT_MAGIC_2, Byte.toUnsignedInt(bytes[1]));
            checkEquality(tag, NbtSquisher.expand(new ByteArrayInputStream(bytes)));
            Assert.assertEquals(32, HashUtil.computeHash(bytes).length);
        }
    }

    @Test
    public void testVanillaUncompressed() throws IOException {
        for (CompoundTag tag : new CompoundTag[] { nbtSmall, nbt }) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            NbtSquisher.squishVanillaUncompressed(tag, new DataOutputStream(out));
            checkEquality(tag, NbtSquisher.expand(new ByteArrayInputStream(out.toByteArray())));
        }
    }

    @Test
    public void testCompressionHelps() throws IOException {
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        NbtSquisher.squishVanilla(nbt, compressed);
        ByteArrayOutputStream plain = new ByteArrayOutputStream();
        NbtSquisher.squishVanillaUncompressed(nbt, new DataOutputStream(plain));
        Assert.assertTrue(compressed.size() < plain.size());
    }

    private static CompoundTag genNbt(int bptSize) {
        Random rand = new Random(0x517123);

        CompoundTag nbt = new CompoundTag();
        nbt.putByte("primitive|byte", (byte) 1);
        nbt.putShort("primitive|short", (short) 2);
        nbt.putInt("primitive|int", 4);
        nbt.putLong("primitive|long", 6);
        nbt.putFloat("primitive|float", 10.01f);
        nbt.putDouble("primitive|double", 11.11010101010101001010);

        nbt.putByteArray("array|byte", new byte[] { 12, 13, 14 });
        nbt.putIntArray("array|int", new int[] { 15000, 160000, 17000, 180000 });

        nbt.putString("string", "OMG A VALUE");

        ListTag list = new ListTag();
        list.add(FloatTag.valueOf(19.91f));
        list.add(FloatTag.valueOf(19.45f));
        list.add(FloatTag.valueOf(19.41f));
        list.add(FloatTag.valueOf(19.32f));
        list.add(FloatTag.valueOf(19.76f));
        nbt.put("complex|list", list);

        nbt.put("complex|tag", new CompoundTag());

        CompoundTag compound = new CompoundTag();
        compound.putBoolean("a", false);
        compound.putDouble("b", 20.02);
        nbt.put("complex|compound", compound);

        String[] names = { "minecraft:air", "minecraft:oak_log", "minecraft:torch", "minecraft:stone",
            "minecraft:oak_fence" };
        int[] metas = { 1, 16, 5, 7, 4 };

        CompoundTag[] blocks = new CompoundTag[sum(metas)];
        int block = 0;
        for (int b = 0; b < names.length; b++) {
            CompoundTag blockNbt = new CompoundTag();
            blockNbt.putString("id", names[b]);
            blocks[block++] = blockNbt.copy();
            for (int m = 1; m < metas[b]; m++) {
                blockNbt.putByte("meta", (byte) m);
                blocks[block++] = blockNbt.copy();
            }
        }

        CompoundTag air = blocks[0];
        ListTag bpt = new ListTag();
        for (int i = 0; i < bptSize; i++) {
            double r = rand.nextDouble();
            final CompoundTag toUse;
            if (r < 0.4) {
                toUse = air;
            } else if (r < 0.9999) {
                toUse = blocks[rand.nextInt(blocks.length)];
            } else {
                toUse = genRandomChest(rand);
            }
            bpt.add(toUse);
        }
        nbt.put("bpt", bpt);
        return nbt;
    }

    private static int sum(int[] values) {
        int total = 0;
        for (int i : values) {
            total += i;
        }
        return total;
    }

    public static void checkEquality(CompoundTag from, CompoundTag to) {
        String error = compoundEqual(from, to);
        if (!error.isEmpty()) {
            Assert.fail("Tags were not equal! (" + error + ")");
        }
    }

    private static String compoundEqual(CompoundTag from, CompoundTag to) {
        Set<String> keysFrom = from.getAllKeys();
        Set<String> keysTo = to.getAllKeys();
        if (!keysFrom.equals(keysTo)) {
            return "keys " + keysFrom + " -> " + keysTo;
        }
        for (String key : keysFrom) {
            String err = nbtEquals(from.get(key), to.get(key));
            if (!err.isEmpty()) {
                return key + " = " + err;
            }
        }
        return "";
    }

    private static String listEquals(ListTag from, ListTag to) {
        if (from.size() != to.size()) {
            return "lengths " + from.size() + " -> " + to.size();
        }
        for (int i = 0; i < from.size(); i++) {
            String err = nbtEquals(from.get(i), to.get(i));
            if (!err.isEmpty()) {
                return "[" + i + "] = " + err;
            }
        }
        return "";
    }

    private static String nbtEquals(Tag valFrom, Tag valTo) {
        if (valFrom instanceof CompoundTag from && valTo instanceof CompoundTag to) {
            return compoundEqual(from, to);
        }
        if (valFrom instanceof ListTag from && valTo instanceof ListTag to) {
            return listEquals(from, to);
        }
        if (!valFrom.equals(valTo)) {
            return valFrom + " -> " + valTo;
        }
        return "";
    }

    private static CompoundTag genRandomChest(Random rand) {
        CompoundTag chest = new CompoundTag();
        chest.putString("block", "minecraft:chest");
        chest.putByte("meta", (byte) rand.nextInt(4));
        ListTag chestItems = new ListTag();

        CompoundTag itemB = genRandomItem(rand);
        int num = rand.nextInt(3) + 2;
        for (int i = 0; i < num; i++) {
            if (rand.nextInt(6) > 0) {
                chestItems.add(itemB);
            } else {
                chestItems.add(genRandomItem(rand));
            }
        }

        chest.put("items", chestItems);
        return chest;
    }

    private static CompoundTag genRandomItem(Random rand) {
        CompoundTag item = new CompoundTag();
        item.putString("id", IDS[rand.nextInt(IDS.length)]);
        item.putByte("count", (byte) (16 + rand.nextInt(3) * 2));
        return item;
    }
}
