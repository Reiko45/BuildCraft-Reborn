/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.datagen;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.imageio.ImageIO;

import com.google.common.hash.Hashing;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;

import net.neoforged.neoforge.common.data.ExistingFileHelper;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.energy.BCEnergyFluids;
import buildcraftreborn.lib.fluid.BCFluid;

/** Makes each fluid's still and flowing textures by recolouring the grey heat-level textures
 * ({@code textures/block/fluids/heat_<n>_still.png}) between the fluid's dark and light colours. 1.12 did this at
 * runtime ({@code AtlasSpriteFluid}); generating the files gives the same pictures with plain resource packs. */
public final class BCFluidTextures implements DataProvider {
    private final PackOutput output;
    private final ExistingFileHelper files;

    public BCFluidTextures(PackOutput output, ExistingFileHelper files) {
        this.output = output;
        this.files = files;
        // Let the model providers know these exist before they're written
        ExistingFileHelper.ResourceType texture = new ExistingFileHelper.ResourceType(PackType.CLIENT_RESOURCES,
            ".png", "textures");
        for (BCFluid fluid : BCEnergyFluids.allFluids) {
            files.trackGenerated(BuildCraftReborn.id("block/fluids/" + fluid.name + "_still"), texture);
            files.trackGenerated(BuildCraftReborn.id("block/fluids/" + fluid.name + "_flow"), texture);
        }
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        try {
            for (BCFluid fluid : BCEnergyFluids.allFluids) {
                for (String kind : new String[] { "still", "flow" }) {
                    String base = "textures/block/fluids/heat_" + fluid.heat + "_" + kind + ".png";
                    String target = "textures/block/fluids/" + fluid.name + "_" + kind + ".png";
                    BufferedImage image = read(BuildCraftReborn.id(base));
                    recolour(image, fluid.colourLight, fluid.colourDark);
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    ImageIO.write(image, "png", bytes);
                    futures.add(write(cache, target, bytes.toByteArray()));
                    futures.add(write(cache, target + ".mcmeta", readBytes(BuildCraftReborn.id(base + ".mcmeta"))));
                }
            }
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> write(CachedOutput cache, String path, byte[] data) {
        Path file = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK).resolve(BuildCraftReborn.MODID)
            .resolve(path);
        try {
            cache.writeIfNeeded(file, data, Hashing.sha1().hashBytes(data));
            return CompletableFuture.completedFuture(null);
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private BufferedImage read(ResourceLocation location) throws IOException {
        try (InputStream in = files.getResource(location, PackType.CLIENT_RESOURCES).open()) {
            BufferedImage image = ImageIO.read(in);
            BufferedImage argb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            argb.getGraphics().drawImage(image, 0, 0, null);
            return argb;
        }
    }

    private byte[] readBytes(ResourceLocation location) throws IOException {
        Resource resource = files.getResource(location, PackType.CLIENT_RESOURCES);
        try (InputStream in = resource.open()) {
            return in.readAllBytes();
        }
    }

    /** 1.12's {@code AtlasSpriteFluid.recolourPixel}: each channel goes from the dark colour (0) to the light (255). */
    static void recolour(BufferedImage image, int light, int dark) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = sub(rgb, light, dark, 16);
                int g = sub(rgb, light, dark, 8);
                int b = sub(rgb, light, dark, 0);
                image.setRGB(x, y, 0xFF_00_00_00 | (r << 16) | (g << 8) | b);
            }
        }
    }

    private static int sub(int rgb, int light, int dark, int offset) {
        int data = (rgb >>> offset) & 0xFF;
        int d = (dark >>> offset) & 0xFF;
        int l = (light >>> offset) & 0xFF;
        return (d * (256 - data) + l * data) / 256;
    }

    @Override
    public String getName() {
        return "BuildCraft Reborn fluid textures";
    }
}
