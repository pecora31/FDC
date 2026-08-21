package net.nazarick.artillerytablet.mapcheck;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Prints, for every zoom step, which level the map picks, how many texels that gives across the
 * panel, and how many texels the panel could actually show. Reads the real constants and calls the
 * real level-picking method, so it cannot drift from the code.
 *
 * <p>A report rather than an assertion, on purpose. There is one hard rule in it — data coarser than
 * the screen looks soft however it is filtered — and everything else is a judgement about how much
 * memory a level costs, which is not the sort of thing to fail a build over.
 */
final class Zooms {
    /** Roughly what the map area is in interface pixels at GUI scale 3 on a 1920-wide window. */
    private static final int PANEL_PX = 563;

    private Zooms() {
    }

    static void run() throws Exception {
        Class<?> panel = Class.forName("net.nazarick.artillerytablet.client.screen.MapPanel");
        Field spansField = panel.getDeclaredField("ZOOM_SPANS");
        spansField.setAccessible(true);
        int[] spans = (int[]) spansField.get(null);

        Class<?> image = Class.forName("net.nazarick.artillerytablet.client.terrain.TerrainImage");
        Method levelFor = image.getDeclaredMethod("levelFor", int.class);
        Method strideFor = image.getDeclaredMethod("strideFor", int.class);
        levelFor.setAccessible(true);
        strideFor.setAccessible(true);

        int devicePx = Math.round(PANEL_PX * 3f);   // GUI scale 3
        System.out.printf("%nzooms: panel %d interface px, %d display px (GUI scale 3)%n", PANEL_PX, devicePx);
        System.out.printf("%8s %6s %8s %8s %11s %8s%n",
                "span", "level", "stride", "texels", "blocks/px", "verdict");

        int soft = 0;
        for (int span : spans) {
            int level = (int) levelFor.invoke(null, span);
            int stride = (int) strideFor.invoke(null, level);
            int texels = span / stride;

            // The one test that separates sharp from soft: data coarser than the screen looks soft
            // however it is filtered. A stride at or below the blocks a display pixel covers means
            // every pixel has at least one sample of its own.
            double blocksPerPixel = span / (double) devicePx;
            // Stride one is one texel per block, the finest the world itself offers — it is not "too
            // coarse", it is the floor. Calling it soft would be the harness accusing the code of a
            // fault it cannot avoid, which is how a bad test once wasted a day.
            String verdict = stride == 1 ? "finest"
                    : stride <= blocksPerPixel ? "sharp" : "SOFT";
            if (verdict.equals("SOFT")) {
                soft++;
            }
            System.out.printf("%7dm %6d %8d %8d %11.2f %8s%n",
                    span, level, stride, texels, blocksPerPixel, verdict);
        }

        System.out.printf("zooms: %d of %d steps carry data coarser than the screen%n", soft, spans.length);

        levelsAndStridesAgree(image);
    }

    /**
     * Checks that a level's stride maps back to that same level in the reduction chain, for every
     * level the drawing can ask for — including the coarse underlay, which reaches further up than
     * any zoom step does on its own.
     *
     * <p>Two places turn between levels and strides and they have to be exact inverses. If they are
     * not, a sheet is built from a reduction finer or coarser than the one it thinks it has, which
     * does not throw and does not look broken — it looks like ordinary ground at the wrong scale.
     */
    private static void levelsAndStridesAgree(Class<?> image) throws Exception {
        int maxLevel = MapCheck.readInt(image, "MAX_LEVEL");
        Method strideFor = image.getDeclaredMethod("strideFor", int.class);
        strideFor.setAccessible(true);

        Class<?> mips = Class.forName("net.nazarick.artillerytablet.client.terrain.TerrainMips");
        Method levelForStride = mips.getDeclaredMethod("levelForStride", int.class);
        levelForStride.setAccessible(true);

        for (int level = 0; level <= maxLevel; level++) {
            int stride = (int) strideFor.invoke(null, level);
            int back = (int) levelForStride.invoke(null, stride);
            MapCheck.check(back == level,
                    "level " + level + " asks for stride " + stride + " which reduces to level " + back);
        }
        System.out.printf("zooms: OK  levels 0..%d and their strides are exact inverses%n", maxLevel);
    }
}
