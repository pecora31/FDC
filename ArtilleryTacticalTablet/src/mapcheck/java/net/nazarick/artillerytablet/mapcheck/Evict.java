package net.nazarick.artillerytablet.mapcheck;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import static net.nazarick.artillerytablet.mapcheck.MapCheck.check;
import static net.nazarick.artillerytablet.mapcheck.MapCheck.readInt;

/**
 * Drives the sheet cache past its cap and makes it evict, outside Minecraft.
 *
 * <p>This is the path that crashed the game after several minutes of play, and neither a build nor a
 * boot could ever have reached it: it only runs once enough squares have accumulated to exceed the
 * cap, which takes a while of exploring. The fault was a fastutil map's entry set handing out one
 * recycled Entry, so a list copied from it held one invalid entry repeated.
 *
 * <p>No texture is created here — the sheets are inserted directly, with no pixels — so this runs
 * without a graphics context and still exercises the real eviction.
 */
final class Evict {
    private Evict() {
    }

    static void run() throws Exception {
        Class<?> imageClass = Class.forName("net.nazarick.artillerytablet.client.terrain.TerrainImage");
        Object image = imageClass.getDeclaredConstructor().newInstance();

        int cap = readInt(imageClass, "MAX_LIVE_TEXTURES");

        Field sheetsField = imageClass.getDeclaredField("sheets");
        sheetsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Long, Object> sheets = (Map<Long, Object>) sheetsField.get(image);

        Class<?> sheetClass = Class.forName("net.nazarick.artillerytablet.client.terrain.TerrainImage$Sheet");
        Constructor<?> sheetCtor = sheetClass.getDeclaredConstructor();
        sheetCtor.setAccessible(true);
        Field lastDrawn = sheetClass.getDeclaredField("lastDrawn");
        lastDrawn.setAccessible(true);

        Field drawClockField = imageClass.getDeclaredField("drawClock");
        drawClockField.setAccessible(true);
        long clock = 5000;
        drawClockField.set(image, clock);

        // Half again as many squares as the cap allows. The oldest third are long out of sight; the
        // rest were touched this frame and must survive however tight the cap is.
        int total = cap + cap / 2;
        int inView = cap;
        for (int i = 0; i < total; i++) {
            Object sheet = sheetCtor.newInstance();
            lastDrawn.setLong(sheet, i < total - inView ? i : clock);
            sheets.put((long) i, sheet);
        }
        System.out.println("evict: before " + sheets.size() + " sheets, cap " + cap);

        Method evict = imageClass.getDeclaredMethod("evictSurplus");
        evict.setAccessible(true);
        evict.invoke(image);

        int after = sheets.size();
        System.out.println("evict: after  " + after + " sheets");

        check(after <= cap || after == inView, "evicted down to the cap, or to what is in view");
        for (int i = total - inView; i < total; i++) {
            check(sheets.containsKey((long) i), "kept the square in view at " + i);
        }
        check(!sheets.containsKey(0L), "dropped the oldest square");

        // And again on a cache that is already inside the cap: it must do nothing and not throw.
        evict.invoke(image);
        check(sheets.size() == after, "second pass changed nothing");

        System.out.println("evict: OK  eviction keeps everything in view and drops the oldest");
    }
}
