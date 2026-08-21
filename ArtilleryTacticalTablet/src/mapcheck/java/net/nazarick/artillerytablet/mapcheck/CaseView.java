package net.nazarick.artillerytablet.mapcheck;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Draws the tablet's case with the game's own code, outside the game, and writes it out to look at.
 *
 * <p><b>Why.</b> The case was being designed by drawing a picture of it by hand, writing code meant
 * to match, and asking somebody to launch Minecraft and look. That loop takes minutes per round and
 * it is unsound besides: a sketch and an implementation are two descriptions of one thing, and
 * several rounds of "not quite like the photograph" were exactly the gap between them. What comes
 * out of here is not an impression of the case — it is the case, drawn by the code the game runs.
 *
 * <p>Two files, because they answer different questions. The SVG opens in a browser and reloads on
 * a keystroke, which is the fast loop. The PNG is pixels, which is the honest one: an SVG viewer
 * will happily antialias a shape that the game will draw in hard steps, so the picture that shows
 * what a corner really looks like has to be rasterised the same way the game rasterises it.
 *
 * <p><b>What it cannot show.</b> Lettering. The face belongs to Minecraft and does not exist out
 * here, so words come out as blocks of the right width — see {@code Paint}. That is a deliberate
 * limit rather than an oversight: what is checked here is moulding, proportion and colour, and a
 * font shipped merely to preview them would be a dependency bought for nothing.
 */
final class CaseView {
    /** The way in from Gradle. Boots the registries first, as every harness here has to. */
    public static final class Main {
        public static void main(String[] args) throws Exception {
            net.minecraft.SharedConstants.tryDetectVersion();
            net.minecraft.server.Bootstrap.bootStrap();
            CaseView.run(java.nio.file.Path.of(args.length > 0 ? args[0] : "build/mapcheck"));
        }
    }

    /**
     * The window the case is fitted to.
     *
     * <p>The size the reference panel was photographed at, so the picture that comes out of here
     * and the picture it is being compared against can be laid on top of one another. It was a
     * quarter of this, which made every judgement about proportion a judgement about a shrunken
     * copy — and the case's own numbers are shares of the shell now, so any size gives the same
     * proportions and only this one gives the same pixels.
     */
    private static final int WINDOW_W = 1920;
    private static final int WINDOW_H = 1080;

    private CaseView() {
    }

    static void run(Path out) throws Exception {
        Class<?> frameClass = Class.forName("net.nazarick.artillerytablet.client.screen.TabletFrame");
        Method fit = frameClass.getDeclaredMethod("fit", int.class, int.class);
        fit.setAccessible(true);
        Object frame = fit.invoke(null, WINDOW_W, WINDOW_H);

        Class<?> paintClass = Class.forName("net.nazarick.artillerytablet.client.screen.Paint");
        Method draw = frameClass.getDeclaredMethod("draw", paintClass);
        draw.setAccessible(true);

        Recorder shapes = new Recorder();
        Object proxy = java.lang.reflect.Proxy.newProxyInstance(
                CaseView.class.getClassLoader(), new Class<?>[]{paintClass}, shapes);
        draw.invoke(frame, proxy);

        keys(frame, frameClass, paintClass, shapes);

        Files.createDirectories(out);
        writeSvg(out.resolve("case.svg"), shapes.shapes);
        writePng(out.resolve("case.png"), shapes.shapes, 1, 0, 0, WINDOW_W, WINDOW_H);
        // A corner, six times over. Detail work is done on parts rather than on the whole, and at
        // one pixel per pixel a moulding nine across is not something an eye can judge — which is
        // how a step that was never drawn survived being looked at four times.
        writePng(out.resolve("case-corner.png"), shapes.shapes, 4, 0, 0, 260, 260);
        writePng(out.resolve("case-keys.png"), shapes.shapes, 3, 180, 0, 460, 300);
        // The other end of the top row and the corner under it: the two keys that carry a symbol
        // and a sign together, and the one filled cap on the case. Both are places where two things
        // share one cap, which is where a layout goes wrong first.
        writePng(out.resolve("case-ends.png"), shapes.shapes, 3, 1370, 0, 550, 300);
        writePng(out.resolve("case-power.png"), shapes.shapes, 4, 1480, 900, 440, 180);

        System.out.printf("case: OK  %d shapes -> %s and case.png%n",
                shapes.shapes.size(), out.resolve("case.svg"));
    }

    /**
     * Stands a key on every slot the case offers.
     *
     * <p>Not the real thirty-two with their real functions — that would mean standing up the whole
     * screen, its map and its network. Every slot gets a plain key, which is enough to check that
     * the caps land in their wells, that the lamps line up with them, and that a row is spaced the
     * way a column is not. What is being looked at is where things are, and that is the part the
     * case decides on its own.
     */
    private static void keys(Object frame, Class<?> frameClass, Class<?> paintClass,
                             Recorder shapes) throws Exception {
        Class<?> buttonClass = Class.forName("net.nazarick.artillerytablet.client.screen.UiButton");
        Constructor<?> ctor = buttonClass.getDeclaredConstructor(
                int.class, int.class, int.class, int.class,
                Class.forName("net.minecraft.network.chat.Component"), Runnable.class);
        ctor.setAccessible(true);

        Method hard = buttonClass.getDeclaredMethod("hard", boolean.class);
        Method lamp = buttonClass.getDeclaredMethod("lamp", int[].class);
        Method render = buttonClass.getDeclaredMethod("render", paintClass, double.class, double.class);
        for (Method m : new Method[]{hard, lamp, render}) {
            m.setAccessible(true);
        }

        // The 3-arg overloads, not the 2-arg ones: rowKey/ledFor now take a "wide" flag for the two
        // bookend keys a row carries, and calling the old 2-arg pair here would silently keep
        // drawing every key at the normal width — no crash, just a preview lying about a feature
        // that only shows up once TabletScreen asks for it. Reflection finds a method by exact
        // signature, so it does not notice when the wrong overload still exists to be found.
        Method rowKey = frameClass.getDeclaredMethod("rowKey", boolean.class, int.class, boolean.class);
        Method keyX = frameClass.getDeclaredMethod("keyX", boolean.class);
        Method keyY = frameClass.getDeclaredMethod("keyY", int.class);
        Method ledFor = frameClass.getDeclaredMethod("ledFor", int.class, int.class, boolean.class);
        for (Method m : new Method[]{rowKey, keyX, keyY, ledFor}) {
            m.setAccessible(true);
        }

        // Sizes come off the frame instance now, not off static fields. They are shares of the
        // shell, so there is no such thing as "the" key width until a case has been fitted to a
        // window — and a harness that read a constant would have been reading a number that no
        // longer exists.
        int rowW = (int) size(frameClass, frame, "rowKeyW");
        int rowWide = (int) size(frameClass, frame, "rowKeyWide");
        int rowH = (int) size(frameClass, frame, "rowKeyH");
        int rowBottomH = (int) size(frameClass, frame, "rowKeyBottomH");
        int keyW = (int) size(frameClass, frame, "keyW");
        int keyH = (int) size(frameClass, frame, "keyH");
        int rowKeys = constant(frameClass, "ROW_KEYS");
        int colKeys = constant(frameClass, "KEYS");

        Object proxy = java.lang.reflect.Proxy.newProxyInstance(
                CaseView.class.getClassLoader(), new Class<?>[]{paintClass}, shapes);

        // Real labels, matching TabletScreen.buildFrameKeys() exactly — this is the layout preview,
        // so a stand-in "ABC" on every cap would hide the one thing worth checking by eye: whether
        // the right word landed on the right slot.
        String[] topLabels = {"GRID", "BTY", "TGT", "AMO", "STA", "LOG", "F17", "F18", "BRT", "BRT"};
        String[] topSubs = {null, null, null, null, null, null, null, null, "-", "+"};
        String[] bottomLabels =
                {"FLT", "F9", "F10", "F11", "F12", "F13", "F14", "F15", "F16", "PWR"};
        String[] leftLabels = {"CFF", "ADJ", "MODE", "ARC", "F1", "F2"};
        String[] rightLabels = {"F3", "F4", "F5", "F6", "F7", "F8"};

        for (int i = 0; i < rowKeys; i++) {
            // The same wide-bookend rule TabletScreen applies: grid and the last brightness key on
            // top, the night/filter key on the bottom.
            boolean wideTop = i == 0 || i == rowKeys - 1;
            boolean wideBottom = i == 0;

            int[] top = (int[]) rowKey.invoke(frame, false, i, wideTop);
            stand(ctor, hard, lamp, render, proxy, top[0], top[1], wideTop ? rowWide : rowW, rowH,
                    (int[]) ledFor.invoke(frame, 0, i, wideTop), i == 1,
                    topLabels[i], topSubs[i], i == rowKeys - 1);
            int[] bottom = (int[]) rowKey.invoke(frame, true, i, wideBottom);
            stand(ctor, hard, lamp, render, proxy, bottom[0], bottom[1],
                    wideBottom ? rowWide : rowW, rowBottomH,
                    (int[]) ledFor.invoke(frame, 1, i, wideBottom), false,
                    bottomLabels[i], null, i == rowKeys - 1);
        }
        for (int i = 0; i < colKeys; i++) {
            stand(ctor, hard, lamp, render, proxy, (int) keyX.invoke(frame, false),
                    (int) keyY.invoke(frame, i), keyW, keyH,
                    (int[]) ledFor.invoke(frame, 2, i, false), i == 0, leftLabels[i], null, i == 0);
            stand(ctor, hard, lamp, render, proxy, (int) keyX.invoke(frame, true),
                    (int) keyY.invoke(frame, i), keyW, keyH,
                    (int[]) ledFor.invoke(frame, 3, i, false), false, rightLabels[i], null, false);
        }
    }

    /** One of the fitted case's own measurements, asked of it rather than copied beside it. */
    private static Object size(Class<?> owner, Object frame, String name) throws Exception {
        Method m = owner.getDeclaredMethod(name);
        m.setAccessible(true);
        return m.invoke(frame);
    }

    /** One of the case's own numbers, read from the class rather than copied beside it. */
    private static int constant(Class<?> owner, String name) throws Exception {
        java.lang.reflect.Field f = owner.getDeclaredField(name);
        f.setAccessible(true);
        return f.getInt(null);
    }

    private static void stand(Constructor<?> ctor, Method hard, Method lamp, Method render,
                              Object paint, int x, int y, int w, int h, int[] led, boolean on,
                              String label, String sub, boolean power) throws Exception {
        Class<?> component = Class.forName("net.minecraft.network.chat.Component");
        Method literal = component.getMethod("literal", String.class);
        Class<?> buttonClass = ctor.getDeclaringClass();
        Object key = ctor.newInstance(x, y, w, h, literal.invoke(null, label), (Runnable) () -> { });
        hard.invoke(key, on);
        lamp.invoke(key, (Object) led);
        if (sub != null) {
            Method setSub = buttonClass.getDeclaredMethod("sub", component);
            setSub.setAccessible(true);
            setSub.invoke(key, literal.invoke(null, sub));
        }
        if (power) {
            Method setPower = buttonClass.getDeclaredMethod("power");
            setPower.setAccessible(true);
            setPower.invoke(key);
        }
        render.invoke(key, paint, -1.0, -1.0);
    }

    /** Every fill the drawing made, in order, as it was made. */
    private static final class Recorder implements java.lang.reflect.InvocationHandler {
        final List<int[]> shapes = new ArrayList<>();

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            switch (method.getName()) {
                case "fill": {
                    int x0 = (int) args[0];
                    int y0 = (int) args[1];
                    int x1 = (int) args[2];
                    int y1 = (int) args[3];
                    int argb = (int) args[4];
                    if (x1 > x0 && y1 > y0) {
                        shapes.add(new int[]{x0, y0, x1, y1, argb});
                    }
                    return null;
                }
                case "label": {
                    // A block where the word goes. A character of the game's face is six pixels of
                    // advance and seven tall, so a block of that size magnified by whatever the
                    // caller asked for keeps the preview honest about how much of a cap a label
                    // covers — which is the one thing about lettering this harness can be honest
                    // about, having no font of its own.
                    int x = (int) args[1];
                    int y = (int) args[2];
                    int w = (int) args[3];
                    int h = (int) args[4];
                    int argb = (int) args[5];
                    // The harness's own answer to "how big does this go". Six pixels of advance
                    // per character and seven tall is the game's face closely enough to judge how
                    // much of a cap a label covers, which is the only question about lettering this
                    // preview is entitled to answer.
                    int advance = 6 * ((String) args[0]).length() - 1;
                    int scale = 1;
                    if (args.length > 7) {
                        int byHeight = (int) Math.floor(h * (double) args[6] / 7);
                        int byWidth = (w - (int) args[7]) / Math.max(1, advance);
                        scale = Math.max(1, Math.min(byHeight, byWidth));
                    }
                    int ink = Math.min(w - 4, advance * scale);
                    int lx = x + (w - ink) / 2;
                    int ly = y + (h - 7 * scale) / 2;
                    shapes.add(new int[]{lx, ly, lx + Math.max(1, ink), ly + 7 * scale,
                            (argb & 0x00FFFFFF) | 0x99000000});
                    return null;
                }
                case "batch":
                    ((Runnable) args[0]).run();
                    return null;
                default:
                    // Every default method on the seam — rect, outline, rounded, roundedShaded —
                    // resolves through fill, so invoking it here runs the real one rather than a
                    // second copy of the curve.
                    return java.lang.invoke.MethodHandles.lookup()
                            .findSpecial(method.getDeclaringClass(), method.getName(),
                                    java.lang.invoke.MethodType.methodType(method.getReturnType(),
                                            method.getParameterTypes()),
                                    method.getDeclaringClass())
                            .bindTo(proxy)
                            .invokeWithArguments(args == null ? new Object[0] : args);
            }
        }
    }

    private static void writeSvg(Path file, List<int[]> shapes) throws IOException {
        StringBuilder b = new StringBuilder();
        b.append("<svg xmlns='http://www.w3.org/2000/svg' viewBox='-8 -8 ")
                .append(WINDOW_W + 16).append(' ').append(WINDOW_H + 16)
                .append("' width='").append((WINDOW_W + 16) * 2)
                .append("' shape-rendering='crispEdges'>\n");
        b.append("<rect x='-8' y='-8' width='").append(WINDOW_W + 16).append("' height='")
                .append(WINDOW_H + 16).append("' fill='#0a0a0b'/>\n");
        for (int[] s : shapes) {
            int a = (s[4] >>> 24) & 0xFF;
            b.append("<rect x='").append(s[0]).append("' y='").append(s[1])
                    .append("' width='").append(s[2] - s[0]).append("' height='").append(s[3] - s[1])
                    .append("' fill='#").append(String.format("%06X", s[4] & 0xFFFFFF)).append('\'');
            if (a < 255) {
                b.append(" fill-opacity='").append(String.format("%.3f", a / 255f)).append('\'');
            }
            b.append("/>\n");
        }
        b.append("</svg>\n");
        Files.write(file, b.toString().getBytes(StandardCharsets.UTF_8));
    }

    /** The same shapes, composited the way the card would composite them. */
    private static void writePng(Path file, List<int[]> shapes, int scale,
                                 int cropX, int cropY, int cropW, int cropH) throws IOException {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
                cropW * scale, cropH * scale, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D gfx = img.createGraphics();
        gfx.setColor(new java.awt.Color(0x0A0A0B));
        gfx.fillRect(0, 0, cropW * scale, cropH * scale);
        gfx.translate(-cropX * scale, -cropY * scale);
        for (int[] s : shapes) {
            int argb = s[4];
            int a = (argb >>> 24) & 0xFF;
            if (a == 0) {
                continue;
            }
            gfx.setColor(new java.awt.Color((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, a));
            gfx.fillRect(s[0] * scale, s[1] * scale, (s[2] - s[0]) * scale, (s[3] - s[1]) * scale);
        }
        gfx.dispose();
        javax.imageio.ImageIO.write(img, "png", file.toFile());
    }
}
