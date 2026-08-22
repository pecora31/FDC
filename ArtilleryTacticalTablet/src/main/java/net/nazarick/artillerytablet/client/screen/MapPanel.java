package net.nazarick.artillerytablet.client.screen;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.nazarick.artillerytablet.client.terrain.TerrainClientCache;
import net.nazarick.artillerytablet.client.terrain.TerrainImage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * The tablet's map: a coordinate grid, with surveyed ground painted underneath it.
 *
 * <p>The grid is the map, not the decoration. Three earlier attempts made imagery the whole feature
 * and each collapsed when its image source said no — JourneyMap's tile call is documented as
 * unsupported for addons, sampling the client's own chunks only ever shows what is inside render
 * distance, and Xaero's internals stopped feeding us fresh ground with no way to find out why. A
 * grid has none of those failure modes: it reads the same at 50 blocks and at 5000, and it matches
 * how artillery is really directed, by grid reference and bearing rather than by photograph.
 *
 * <p>Terrain is therefore a layer that is allowed to be absent, and now comes from the server — the
 * only party that knows more ground than one player has walked over. Where the world has been
 * generated it shows; everywhere else the grid shows through, and the two cases look different on
 * purpose. A device that shoots five kilometres must never dress up a gap as flat ground.
 */
@OnlyIn(Dist.CLIENT)
class MapPanel {
    /**
     * Zoom steps, in blocks across the panel.
     *
     * <p>Round thousands rather than powers of two. A block is a metre here, and artillery is worked
     * in metres and in grid squares of a thousand of them — the target grid on a firing chart divides
     * a thousand-metre square into hundreds. Powers of two gave a grid stepping every 128 m, which is
     * not a number anyone in the trade reads, and turned every distance estimated off the map into
     * arithmetic. On these steps the graticule falls on hundreds and thousands at every zoom.
     *
     * <p>The top steps are far beyond where surveyed ground can still be drawn, so they are grid
     * only. That is the point of them: at sixteen kilometres the grid is the whole instrument, and a
     * gun that can throw five thousand metres needs to see where it sits in something larger.
     */
    private static final int[] ZOOM_SPANS = {250, 500, 1000, 2000, 4000, 8000, 16000, 32000};

    /**
     * Not this panel's to own. The sheets outlive the screen deliberately — see
     * {@link TerrainImage#shared()} for why keeping them is the whole fix for a map that started
     * black on every open.
     */
    private final TerrainImage terrain = TerrainImage.shared();

    private boolean terrainShown;

    /**
     * Screen rectangle the tablet has covered with something of its own, or null.
     *
     * <p>Labels inside it are skipped rather than drawn and painted over. Relying on draw order
     * failed: Minecraft batches text separately from filled quads and puts it on top whatever order
     * it was submitted in, and flushing between the two turned out not to be dependable either. Not
     * drawing a label at all is the one behaviour no rendering detail can undo.
     */
    private int[][] reserved = new int[0][];

    MapPanel() {
    }

    /** Any number of covered rectangles; nulls are ignored so callers can pass "not shown". */
    void reserve(int[]... rects) {
        this.reserved = rects;
    }

    private boolean isReserved(int x, int y) {
        for (int[] rect : reserved) {
            if (rect != null
                    && x >= rect[0] - 2 && x <= rect[0] + rect[2] + 2
                    && y >= rect[1] - 2 && y <= rect[1] + rect[3] + 2) {
                return true;
            }
        }
        return false;
    }

    private static final int COLOUR_BACKDROP = 0xFF0C1015;
    private static final int COLOUR_GRID = 0xFF1C232B;
    private static final int COLOUR_GRID_MAJOR = TabletTheme.LINE;
    /** The world origin lines. Cyan rather than the friendly blue — an axis is a reference, not a unit. */
    private static final int COLOUR_AXIS = 0xFF2E6B7A;
    private static final int COLOUR_LABEL = TabletTheme.MUTED;

    private static final int COLOUR_GRID_OVER_TERRAIN = 0x33FFFFFF;
    private static final int COLOUR_GRID_MAJOR_OVER_TERRAIN = 0x66FFFFFF;
    private static final int COLOUR_AXIS_OVER_TERRAIN = 0xAA6FD4E8;
    private static final int COLOUR_LABEL_OVER_TERRAIN = 0xFFE3E9EF;

    // ---- where the map is looking ---------------------------------------------------------------
    //
    // Static, and for the same reason the sheets are: this belongs to the session, not to the screen.
    // A tablet is opened and shut dozens of times in a fight, and the screen is built anew each time,
    // so as instance state these reset the zoom and recentred the view on every open. That is not
    // only wrong for a instrument — a map that forgets where you were pointing it is a map you have
    // to re-aim every time — it is also what stopped reopening being free. The sheets were all still
    // there; the view had simply moved off them, so a different set of squares was wanted and the
    // ones that were held went unused.
    //
    // Reset on a change of world, below, because a centre in one world means nothing in another.

    private static int zoomIndex = 3;

    /**
     * Where the map is centred, to fractions of a block.
     *
     * <p>It was a whole block coordinate, and that is what made a slow drag ripple. A block is only
     * a couple of pixels across at a close zoom, so every step moved the whole picture by that much
     * at once — and since each tile rounds its own edges to whole pixels, one would round up while
     * its neighbour rounded down and they slid against each other. The image did not travel as one
     * thing. Carrying the fraction lets it move by less than a pixel and keeps the tiles rigid
     * relative to one another.
     */
    private static double centreX;
    private static double centreZ;
    private static boolean centreFollowsPlayer = true;

    /**
     * Whether the graticule is drawn.
     *
     * <p>Static like the zoom and the centre, and for the same reason: it is a setting of the
     * instrument, not of the screen that happens to be open. Turned off it takes the lines and their
     * labels together — half a graticule is a set of numbers with nothing to number.
     */
    private static boolean gridShown = true;

    /** How much smaller the graticule's numbers are drawn than the interface's own lettering. */
    private static final float LABEL_SCALE = 0.75f;

    /**
     * Puts the device back to the state it is in when a world is first entered.
     *
     * <p>For the power key. Switching the display on again should look like switching a display on
     * — the boot screen, then the map filling — rather than like uncovering a picture that was
     * there all along. The ground itself is not thrown away: this is the latch that says the map
     * has finished arriving once, not the map.
     */
    static void restartBoot() {
        booted = false;
        bootStarted = 0L;
    }

    void toggleGrid() {
        gridShown = !gridShown;
    }

    boolean gridShown() {
        return gridShown;
    }

    /**
     * Whether the device is still coming up.
     *
     * <p>Asked by the screen, which draws the boot display, because the boot display belongs to the
     * whole glass rather than to the map panel inside it. This panel keeps the latch — it is the
     * part that knows whether the ground has arrived — and hands out the answer.
     */
    boolean booting() {
        return !booted;
    }

    /** The world the remembered view belongs to. */
    private static int viewGeneration = -1;

    /**
     * Whether the map has finished filling in once, in this world.
     *
     * <p>A latch, not a live test. The boot screen has to stay up until the map is actually ready —
     * it used to go the moment one square out of eighty had been drawn — but it must not come back
     * every time panning reaches ground that has not been fetched yet. Starting up is a thing that
     * happens once; a gap at the edge of a drag is ordinary.
     */
    private static boolean booted;

    /** When the device started up in this world, or zero before the first frame of it. */
    private static long bootStarted;

    /**
     * How long the boot screen stays up.
     *
     * <p>Pure animation, deliberately — a fixed length rather than a wait on the map being ready.
     * It used to hold until {@code !TerrainClientCache.isWaiting() && terrain.isComplete()}, with a
     * floor so switching the display off and straight back on (every tile already cached) still
     * showed a moment of "coming up" rather than looking like the device had never been off, and a
     * ceiling so one slow corner of the world didn't hide a map that was nine tenths drawn and
     * perfectly usable. Both of those were real problems, but tying the animation to data readiness
     * at all was the wrong fix for either: refilling from the on-disk cache after a restart could
     * still run past the ceiling on a widely-explored world (thousands of small tile files read one
     * at a time — see {@code TerrainDisk}'s split read pool), which read as "the map is reloading
     * from scratch" even though the ground was already known. A device switching on looks the same
     * regardless of how much there is to boot into; the map keeps filling in behind the animation
     * exactly as before; only what ends the animation has changed.
     */
    private static final long BOOT_DURATION_MS = 900L;

    void tick() {
        // A remembered centre is a set of coordinates in a world that may no longer be the one we
        // are in. Watched here the same way the terrain layer watches it, rather than by having the
        // cache reach up into the screen to tell it.
        int generation = TerrainClientCache.generation();
        if (generation != viewGeneration) {
            viewGeneration = generation;
            centreFollowsPlayer = true;
            // Another world is another map to fill in, so the device starts up again.
            booted = false;
            bootStarted = 0L;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && centreFollowsPlayer) {
            centreX = mc.player.getX();
            centreZ = mc.player.getZ();
        }
    }

    /**
     * Ground covered down the panel, for a panel that is not square.
     *
     * <p>{@link #span} is blocks across the <em>width</em> — it is the number the scale label quotes.
     * Dividing that same number into the height as well is what stretched the map: the panel is
     * close to twice as wide as it is tall, so every block was drawn twice as wide as it was high
     * and nothing on the map was the shape it is on the ground. The vertical coverage is derived
     * from the horizontal one instead, so a block is square whatever the panel is.
     */
    /**
     * The whole-block corner the layers are laid out from, and the fraction left over.
     *
     * <p>Everything that moves with the ground is drawn from this integer origin and then shifted
     * bodily by the remainder. Laying out from the true fractional corner instead would let each
     * piece round to whole pixels on its own account, and pieces that round separately slide against
     * each other as the view creeps — which is what a slow drag looked like before, and then looked
     * like again one layer up when only the terrain was fixed.
     */
    private int originX(int span) {
        return (int) Math.floor(centreX - span / 2.0);
    }

    private int originZ(int spanZ) {
        return (int) Math.floor(centreZ - spanZ / 2.0);
    }

    private float shiftX(int span, int width) {
        return (float) (-(centreX - span / 2.0 - originX(span)) / span * width);
    }

    private float shiftY(int spanZ, int height) {
        return (float) (-(centreZ - spanZ / 2.0 - originZ(spanZ)) / spanZ * height);
    }

    private int spanZ(int width, int height) {
        return Math.max(1, (int) Math.round(span() * (double) height / width));
    }

    private int span() {
        return ZOOM_SPANS[zoomIndex];
    }

    /**
     * Grid spacing, always a round military distance.
     *
     * <p>Roughly eight lines across the panel, but snapped to the 1-2-5 ladder every map in the world
     * uses — 100, 200, 500, 1000 and so on. {@code span / 8} on its own produced steps like 128 m and
     * 512 m, which are readable as numbers and useless as a scale: a grid exists so distance can be
     * counted off it by eye, and nobody counts in 128s.
     */
    private int gridStep() {
        int wanted = Math.max(100, span() / 8);
        int decade = 1;
        while (decade * 10 <= wanted) {
            decade *= 10;
        }
        for (int multiple : new int[]{1, 2, 5}) {
            if (decade * multiple >= wanted) {
                return decade * multiple;
            }
        }
        return decade * 10;
    }

    void render(GuiGraphics g, int x, int y, int width, int height) {
        Minecraft mc = Minecraft.getInstance();

        g.fill(x, y, x + width, y + height, COLOUR_BACKDROP);
        Ui.scissor(g, x, y, width, height);

        int span = span();
        int spanZ = spanZ(width, height);
        int step = gridStep();

        // The ground and the graticule are drawn at the DISPLAY's resolution, not the interface's.
        //
        // This is the last thing standing between this map and the ones it was measured against, and
        // the arithmetic is stark: the interface is laid out in its own pixels and magnified by the
        // GUI scale on the way to the screen, so at a scale of three a panel that looks seventeen
        // hundred pixels wide is really five hundred and sixty to draw into. A texel could never be
        // smaller than three screen pixels however much detail the data held — and at half the zoom
        // steps the data holds nearly twice what that ceiling allows, so it was being thrown away
        // before it was ever drawn. The map mods render straight to the screen grid and that, not
        // some cleverer renderer, is why they look sharper at the same zoom.
        //
        // Shapes only through here. The face is a fixed bitmap drawn for the interface grid and comes
        // out wrong in this one, which is the reason this project abandoned device pixels for layout
        // in the first place — so the graticule's labels are collected here and drawn afterwards, in
        // interface pixels, outside the matrix.
        double scale = Ui.deviceScale();
        int dx = (int) Math.round(x * scale);
        int dy = (int) Math.round(y * scale);
        int dw = (int) Math.round(width * scale);
        int dh = (int) Math.round(height * scale);

        // Grid lines keep the weight they have now rather than becoming hairlines. A line one
        // display pixel wide would be a third of its present thickness, which is a change to how the
        // instrument looks and not part of what this is for.
        int rule = Math.max(1, (int) Math.round(scale));

        List<int[]> labels = new ArrayList<>();

        g.pose().pushPose();
        g.pose().scale((float) (1 / scale), (float) (1 / scale), 1f);

        // One shift for the ground and the graticule together. Drawn outside it, the grid rounded
        // its own lines to whole pixels while the terrain slid smoothly underneath, and the two
        // crept apart and back by a pixel as the view moved — the same fault the terrain had, moved
        // up a layer rather than cured.
        g.pose().translate(shiftX(span, dw), shiftY(spanZ, dh), 0f);

        terrainShown = terrain.draw(g, dx, dy, dw, dh, originX(span), originZ(spanZ), span, spanZ);

        double minX = originX(span);
        double minZ = originZ(spanZ);
        double maxX = minX + span;
        double maxZ = minZ + spanZ;

        // Vertical lines (constant X), labelled along the top.
        if (gridShown) {
        int firstX = Math.floorDiv((int) Math.floor(minX), step) * step;
        for (int worldX = firstX; worldX <= maxX; worldX += step) {
            final int lineX = worldX;
            double exact = dx + (lineX - minX) / span * dw;
            int sx = (int) Math.floor(exact);
            if (sx < dx || sx > dx + dw) {
                continue;
            }
            // Placed to the fraction, like the ground under it. Rounding each line to a whole pixel
            // was the same fault the sheets had: the terrain moves continuously, so a graticule that
            // snapped would swim over it.
            drawFine(g, (float) (exact - sx), 0f,
                    () -> g.fill(sx, dy, sx + rule, dy + dh, lineColour(lineX, step)));
            if (major(lineX, step)) {
                labels.add(new int[]{lineX, (int) Math.round(sx / scale), y, 0});
            }
        }

        // Horizontal lines (constant Z), labelled down the left.
        int firstZ = Math.floorDiv((int) Math.floor(minZ), step) * step;
        for (int worldZ = firstZ; worldZ <= maxZ; worldZ += step) {
            final int lineZ = worldZ;
            double exact = dy + (lineZ - minZ) / spanZ * dh;
            int sy = (int) Math.floor(exact);
            if (sy < dy || sy > dy + dh) {
                continue;
            }
            drawFine(g, 0f, (float) (exact - sy),
                    () -> g.fill(dx, sy, dx + dw, sy + rule, lineColour(lineZ, step)));
            if (major(lineZ, step)) {
                labels.add(new int[]{lineZ, x, (int) Math.round(sy / scale), 1});
            }
        }
        }

        g.flush();
        g.pose().popPose();

        // Back in interface pixels, where the face belongs — but three quarters of the size, which
        // is as small as these can honestly go. The face is a bitmap cut for the interface grid, so
        // any scale at all softens it; a quarter off is small enough to stop the numbers competing
        // with the ground and large enough that they are still read rather than deciphered. Drawing
        // them in display pixels would make them a third of the size and illegible, which is the
        // whole reason they were pulled out of that matrix in the first place.
        g.pose().pushPose();
        g.pose().scale(LABEL_SCALE, LABEL_SCALE, 1f);
        for (int[] label : booted ? labels : java.util.List.<int[]>of()) {
            String text = String.valueOf(label[0]);
            int lx = label[1] + 2;
            int ly = label[2] + 2;
            // Both ends, not just the anchor. A label checked only where it starts still ran its
            // last two digits under the key rail standing at the top right.
            if (isReserved(lx, ly)
                    || (label[3] == 0 && isReserved(lx + TabletTheme.width(mc.font, text), ly))) {
                continue;
            }
            // Reserved space is tested where the label really is; only the drawing is scaled, so the
            // two do not have to agree about a factor.
            TabletTheme.draw(g, mc.font, text, Math.round(lx / LABEL_SCALE),
                    Math.round(ly / LABEL_SCALE), labelColour(), terrainShown);
        }
        g.pose().popPose();

        // The device starting up: a fixed-length animation, not a wait on the map. See
        // BOOT_DURATION_MS for why this is no longer gated on map readiness.
        if (!booted) {
            if (bootStarted == 0L) {
                // Timed from the first frame drawn rather than from the change of world, because a
                // world can be joined long before anyone reaches for the tablet.
                bootStarted = System.currentTimeMillis();
            }
            long up = System.currentTimeMillis() - bootStarted;
            booted = up >= BOOT_DURATION_MS;
        }
        // The boot display itself is drawn by the screen, over the whole glass. Drawn from here it
        // covered the map panel and nothing else, so the header above it and the view keys on it
        // stayed lit and legible through a screen that is meant to mean "not ready yet" — the
        // controls of the map, standing on top of the thing that says the map is not there.

        // Flush while the scissor still applies. Text is batched into its own render type and drawn
        // after every filled quad regardless of the order it was submitted in, so without this the
        // grid's coordinate labels escape both the clip and the layering — they end up painted over
        // whatever the tablet draws on top of the map afterwards.
        g.flush();
        g.disableScissor();
    }

    /** Draws something shifted by less than a whole pixel, so it sits where the ground does. */
    private static void drawFine(GuiGraphics g, float dx, float dy, Runnable drawing) {
        g.pose().pushPose();
        g.pose().translate(dx, dy, 0f);
        drawing.run();
        g.pose().popPose();
    }

    /** How wide the panel reaches, for whoever is drawing it. */
    String spanLabel() {
        return span() + "m";
    }

    /**
     * The grid inverts over terrain. Its normal colours are pale marks on a dark backdrop; laid on
     * ground imagery those same colours vanish into anything dark, so over terrain the lines become
     * translucent white instead and read against grass, stone and deep water alike.
     */
    private int lineColour(int world, int step) {
        if (world == 0) {
            return terrainShown ? COLOUR_AXIS_OVER_TERRAIN : COLOUR_AXIS;
        }
        if (major(world, step)) {
            return terrainShown ? COLOUR_GRID_MAJOR_OVER_TERRAIN : COLOUR_GRID_MAJOR;
        }
        return terrainShown ? COLOUR_GRID_OVER_TERRAIN : COLOUR_GRID;
    }

    private int labelColour() {
        return terrainShown ? COLOUR_LABEL_OVER_TERRAIN : COLOUR_LABEL;
    }

    private static boolean major(int world, int step) {
        return Math.floorMod(world, step * 2) == 0;
    }

    /** Screen position of a world column, or null when it falls outside the panel. */
    double[] worldToScreen(int worldX, int worldZ, int x, int y, int width, int height) {
        int span = span();
        int spanZ = spanZ(width, height);
        double minX = centreX - span / 2.0;
        double minZ = centreZ - spanZ / 2.0;

        // Half a block over, so the mark sits in the middle of its block rather than on the corner
        // where four of them meet. A block coordinate names the block, not the grid line beside it.
        double sx = x + (worldX + 0.5 - minX) / span * width;
        double sy = y + (worldZ + 0.5 - minZ) / spanZ * height;
        if (sx < x || sx > x + width || sy < y || sy > y + height) {
            return null;
        }
        return new double[]{sx, sy};
    }

    /** Blocks across the panel at the current zoom. */
    int spanBlocks() {
        return span();
    }

    /**
     * Screen position of a world column, without the off-panel test.
     *
     * <p>{@link #worldToScreen} returns null outside the panel, which is right for a marker — there
     * is nothing to draw. A range ring is centred on its gun and drawn far beyond it, so its centre
     * being off the panel is ordinary rather than a reason to skip it.
     */
    double[] worldToScreenUnclipped(int worldX, int worldZ, int x, int y, int width, int height) {
        int span = span();
        int spanZ = spanZ(width, height);
        double minX = centreX - span / 2.0;
        double minZ = centreZ - spanZ / 2.0;
        return new double[]{x + (worldX + 0.5 - minX) / span * width,
                y + (worldZ + 0.5 - minZ) / spanZ * height};
    }

    /** World column under a screen position. */
    BlockPos screenToWorld(double mouseX, double mouseY, int x, int y, int width, int height) {
        int span = span();
        int spanZ = spanZ(width, height);
        double minX = centreX - span / 2.0;
        double minZ = centreZ - spanZ / 2.0;

        // Floor, not round. Rounding puts the halfway point of a block into the next one along, so
        // clicking a block's middle picked its neighbour — the other half of why marks and clicks
        // disagreed with the ground under them.
        int worldX = (int) Math.floor(minX + (mouseX - x) / width * span);
        int worldZ = (int) Math.floor(minZ + (mouseY - y) / height * spanZ);
        Minecraft mc = Minecraft.getInstance();
        return new BlockPos(worldX, mc.player == null ? 64 : mc.player.getBlockY(), worldZ);
    }

    void zoomBy(int delta) {
        zoomIndex = Math.max(0, Math.min(ZOOM_SPANS.length - 1, zoomIndex - delta));
    }

    /**
     * Drags the view, which also stops it snapping back to the player each tick.
     *
     * <p>The leftover fraction of a block is carried over between calls, and that carry is the whole
     * point. A drag arrives as a stream of one- and two-pixel steps; zoomed in close a pixel is a
     * fifth of a block, so rounding each step on its own sent almost all of them to zero. Worse, it
     * did so per axis — a step of three pixels across and one down rounded to a whole block across
     * and nothing down, so the map crabbed along the axis the mouse happened to favour instead of
     * following it. Accumulating makes every pixel of movement count.
     */
    void panByPixels(double dx, double dy, int width, int height) {
        centreFollowsPlayer = false;
        int span = span();

        // Straight onto the centre. There is no leftover to carry any more: the centre holds
        // fractions itself, so every pixel of movement lands rather than being banked until it adds
        // up to a whole block.
        centreX -= dx / width * span;
        centreZ -= dy / height * spanZ(width, height);
    }

    void recentreOnPlayer() {
        centreFollowsPlayer = true;
    }

    boolean followsPlayer() {
        return centreFollowsPlayer;
    }

    BlockPos centre() {
        return BlockPos.containing(centreX, 0, centreZ);
    }

    int blocksPerPixel(int width, int height) {
        return Math.max(1, span() / Math.max(1, Math.max(width, height)));
    }

}
