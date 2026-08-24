package net.nazarick.artillerytablet.mapcheck;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Renders the full tablet case running the ASTRA Frontline C2 software UI to build/mapcheck/astra-frontline.png and case.png.
 */
public final class AstraFrontlineView {
    public static final class Main {
        public static void main(String[] args) throws Exception {
            net.minecraft.SharedConstants.tryDetectVersion();
            net.minecraft.server.Bootstrap.bootStrap();
            AstraFrontlineView.run(Path.of(args.length > 0 ? args[0] : "build/mapcheck"));
        }
    }

    private AstraFrontlineView() {}

    public static void run(Path out) throws Exception {
        Files.createDirectories(out);
        try {
            Class<?> astraClass = Class.forName("net.nazarick.artillerytablet.client.screen.AstraFrontlinePaint");
            java.lang.reflect.Method bakeMethod = astraClass.getDeclaredMethod("bake");
            bakeMethod.setAccessible(true);
            com.mojang.blaze3d.platform.NativeImage bakedImg = (com.mojang.blaze3d.platform.NativeImage) bakeMethod.invoke(null);
            
            // Write to both astra-frontline.png and case.png so all active live viewers update automatically!
            bakedImg.writeToFile(out.resolve("astra-frontline.png"));
            bakedImg.writeToFile(out.resolve("case.png"));
            System.out.printf("ASTRA Frontline: OK -> %s and %s%n",
                    out.resolve("astra-frontline.png"), out.resolve("case.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
