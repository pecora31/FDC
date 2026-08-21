package net.nazarick.artillerytablet.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.nazarick.artillerytablet.client.terrain.TerrainClientCache;
import net.nazarick.artillerytablet.client.terrain.TerrainMips;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * What the screen does to the picture on its way out: brightness, and the night filter.
 *
 * <p><b>Why this is not part of deciding the colour of ground.</b> There is a standing rule in this
 * project that a column's colour is decided in exactly one place, and it is not here — it is
 * {@code TerrainMips.groundColour}, and the answer it gives is baked into the reduced copies and
 * from there into the pictures on the graphics card. Turning a knob must not reach back into any of
 * that. If it did, every press of a brightness key would invalidate several hundred sheets and set
 * the whole map rebuilding, which is precisely the treadmill that made a patch of it freeze for a
 * whole session.
 *
 * <p>So brightness does not touch what the ground <em>is</em>. It is applied at the moment of
 * drawing, as a multiplier on everything that reaches the glass, and costs nothing but a uniform.
 * That is also the honest model: on a real set the backlight is a property of the display, not an
 * edit to the map inside it.
 *
 * <p><b>The night filter is the exception, and it is not applied here.</b> Monochrome means adding
 * a colour's three channels together, and modulation cannot do that — it scales each channel on its
 * own, which tints an image and can never desaturate one. So the thermal palette lives where a
 * column's colour is decided, and switching it throws away what was coloured under the old one.
 * That costs a rebuild; it is the price of the picture being genuinely grey rather than merely
 * tinted, and it is paid on a key nobody presses twice in a fight.
 *
 * <p><b>What it covers.</b> The glass, and only the glass. The case and the keys standing on it are
 * physical and do not dim, which is both truer and more useful — the lettering on a control has to
 * stay readable at the setting where the map is dimmest, and that setting is the one used at night
 * when the map would otherwise light up the room.
 */
@OnlyIn(Dist.CLIENT)
final class TabletDisplay {
    /**
     * The brightness steps, dimmest first.
     *
     * <p>Eight, with unmodified output in the middle rather than at the top. A multiply above one
     * does brighten — the shader's modulator is a plain multiply and the clamp happens afterwards,
     * on the way to the screen — so the scale can run past normal, and the top steps read as a
     * backlight driven hard rather than as a picture washed out.
     *
     * <p>It runs to double. Worth saying what the top of that range costs, since it is not free:
     * a multiply clips, so from about 1.4 upwards the pale ground reaches flat white while the dark
     * ground is still climbing, and the picture trades detail for legibility. That is a real trade
     * rather than a fault — sand and snow stop being distinguishable, and a ridge line in shadow
     * becomes readable — so the steps are there to be used, with the understanding that the top of
     * the scale is for seeing something in the dark rather than for seeing everything.
     */
    private static final float[] LEVELS = {
            0.30f, 0.45f, 0.62f, 0.80f, 1.00f,
            1.15f, 1.30f, 1.45f, 1.60f, 1.80f, 2.00f,
    };

    /** Unmodified output, which is what a display that has never been touched is on. */
    private static final int NORMAL = 4;

    private static int level = NORMAL;

    private static TerrainMips.Filter filter = TerrainMips.Filter.NONE;

    private TabletDisplay() {
    }

    static void brighter() {
        level = Math.min(LEVELS.length - 1, level + 1);
    }

    static void dimmer() {
        level = Math.max(0, level - 1);
    }

    static boolean atBrightest() {
        return level == LEVELS.length - 1;
    }

    static boolean atDimmest() {
        return level == 0;
    }

    /**
     * Steps to the next brightness level, wrapping from the top back to the bottom.
     *
     * <p>For a single case key that replaces the old up/down pair — a hand finding one key in the
     * dark and pressing it repeatedly should cycle through every step rather than stall at either
     * end, the way {@link #cycleFilter()} already does for the display filter.
     */
    static void cycleBrightness() {
        level = (level + 1) % LEVELS.length;
    }

    /** The setting as a percentage, for saying out loud on a key's tooltip. */
    static int percent() {
        return Math.round(LEVELS[level] * 100f);
    }

    /**
     * Steps to the next way of colouring ground, and throws away everything coloured the old way.
     *
     * <p>A cycle rather than a switch, because there are three of these now and a device with one
     * key per palette would spend its case on modes. The order runs from the literal to the
     * abstract — ground as it looks, then heat, then height — so pressing on always moves further
     * from the photograph.
     *
     * <p>None of it is free, and none of it can be: a palette is a decision about what a column's
     * colour <em>is</em>, so it is made where that is decided and everything derived from the old
     * answer is dropped. Monochrome is the clearest case — it means adding a colour's channels
     * together, which nothing a draw call can do without its own shader will do. A second or two,
     * on a key nobody presses twice in a fight.
     */
    static void cycleFilter() {
        TerrainMips.Filter[] all = TerrainMips.Filter.values();
        filter = all[(filter.ordinal() + 1) % all.length];
        TerrainMips.filter(filter);
        TerrainClientCache.repaint();
    }

    /**
     * Whether any filter is in use — all the screen needs to know to light a key.
     *
     * <p>Asked instead of handing out the filter itself, so the screen never has to name a type
     * from the terrain package to draw a control. Which palette is in use is that package's
     * business; whether the key is lit is this one's.
     */
    static boolean filtered() {
        return filter != TerrainMips.Filter.NONE;
    }

    /** The translation key naming the filter in use, for a key's tooltip. */
    static String filterName() {
        return switch (filter) {
            case THERMAL -> "gui.artillerytablet.filter.thermal";
            case RELIEF -> "gui.artillerytablet.filter.relief";
            default -> "gui.artillerytablet.filter.none";
        };
    }

    /**
     * Turns the transfer on for everything drawn until {@link #clear} is called.
     *
     * <p>Flushed on the way in and out. Filled shapes are batched, and a batch is drawn with
     * whatever the uniform holds when it is finally submitted — so without this the setting would
     * reach backwards over whatever had been queued and not yet drawn, tinting the case as well as
     * the glass and doing it a frame late.
     */
    static void apply(GuiGraphics g) {
        if (level == NORMAL) {
            return;
        }
        g.flush();
        float b = LEVELS[level];
        g.setColor(b, b, b, 1f);
    }

    static void clear(GuiGraphics g) {
        if (level == NORMAL) {
            return;
        }
        g.flush();
        g.setColor(1f, 1f, 1f, 1f);
    }

    /**
     * Subtle Tactical MFD Optical AR Glass Glare & Micro Scanlines.
     * Rendered on top of the screen well to give authentic military instrument depth.
     */
    static void renderGlassOverlay(GuiGraphics g, int x, int y, int w, int h) {
        if (w <= 0 || h <= 0) {
            return;
        }
        // 1. Soft 45-degree diagonal AR glass reflection sheen (ultra faint ~2% alpha)
        int glareBand = Math.min(w / 2, h / 2);
        for (int row = 0; row < h; row++) {
            int gx1 = x + row;
            int gx2 = Math.min(x + w, gx1 + glareBand);
            if (gx1 < x + w) {
                g.fill(gx1, y + row, gx2, y + row + 1, 0x05FFFFFF);
            }
        }

        // 2. Micro Scanline Overlay (ultra-faint 1px interlaced scanlines)
        for (int sy = y; sy < y + h; sy += 2) {
            g.fill(x, sy, x + w, sy + 1, 0x04000000);
        }
    }
}
