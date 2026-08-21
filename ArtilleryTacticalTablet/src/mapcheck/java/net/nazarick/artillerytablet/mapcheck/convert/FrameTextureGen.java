package net.nazarick.artillerytablet.mapcheck.convert;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Path;

/**
 * Renders the tablet frame — shell, corner bumpers, screen well — to a standalone PNG.
 *
 * <p>Drawn through the real {@code Paint} seam, the same way {@code CaseView} draws the whole case
 * with its keys. Kept separate from that harness because this one also outlines every key slot the
 * frame's own geometry offers, which is the check for whether a slot's computed position still lands
 * on the shape {@link net.nazarick.artillerytablet.client.screen.TabletFrame#draw} put there.
 */
public final class FrameTextureGen {

    public static void main(String[] args) throws Exception {
        int w = args.length > 0 ? Integer.parseInt(args[0]) : 1024;
        int h = args.length > 1 ? Integer.parseInt(args[1]) : 576;
        Path out = Path.of(args.length > 2 ? args[2] : "build/mapcheck");
        out.toFile().mkdirs();

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0x1A1A1E));
        g.fillRect(0, 0, w, h);

        Class<?> frameClass = Class.forName(
                "net.nazarick.artillerytablet.client.screen.TabletFrame");
        Class<?> paintClass = Class.forName(
                "net.nazarick.artillerytablet.client.screen.Paint");

        int margin = 4;
        Constructor<?> ctor = frameClass.getDeclaredConstructor(
                int.class, int.class, int.class, int.class, boolean.class);
        ctor.setAccessible(true);
        Object frame = ctor.newInstance(margin, margin, w - margin * 2, h - margin * 2, true);

        Object paint = java.lang.reflect.Proxy.newProxyInstance(
                FrameTextureGen.class.getClassLoader(),
                new Class<?>[]{paintClass},
                (proxy, method, methodArgs) -> {
                    switch (method.getName()) {
                        case "fill" -> {
                            int x0 = Math.max(0, (int) methodArgs[0]);
                            int y0 = Math.max(0, (int) methodArgs[1]);
                            int x1 = Math.min(w, (int) methodArgs[2]);
                            int y1 = Math.min(h, (int) methodArgs[3]);
                            int argb = (int) methodArgs[4];
                            if (x1 > x0 && y1 > y0) {
                                g.setColor(new Color(argb, true));
                                g.fillRect(x0, y0, x1 - x0, y1 - y0);
                            }
                            return null;
                        }
                        case "batch" -> {
                            ((Runnable) methodArgs[0]).run();
                            return null;
                        }
                        case "label" -> {
                            return null;
                        }
                        default -> {
                            if (method.isDefault()) {
                                return java.lang.reflect.InvocationHandler
                                        .invokeDefault(proxy, method, methodArgs);
                            }
                            throw new UnsupportedOperationException(method.getName());
                        }
                    }
                });

        Method draw = frameClass.getDeclaredMethod("draw", paintClass);
        draw.setAccessible(true);
        draw.invoke(frame, paint);

        // Outline every slot the case offers, straight from the frame's own geometry — no UiButton
        // needed. This is the check for the thing that actually went wrong last round: whether the
        // code's idea of where a cap sits lines up with the cap the texture already drew there.
        Method rowKey = frameClass.getDeclaredMethod("rowKey", boolean.class, int.class, boolean.class);
        Method keyX = frameClass.getDeclaredMethod("keyX", boolean.class);
        Method keyY = frameClass.getDeclaredMethod("keyY", int.class);
        for (Method m : new Method[]{rowKey, keyX, keyY}) {
            m.setAccessible(true);
        }
        int rowW = (int) size(frameClass, frame, "rowKeyW");
        int rowWide = (int) size(frameClass, frame, "rowKeyWide");
        int rowH = (int) size(frameClass, frame, "rowKeyH");
        int rowBottomH = (int) size(frameClass, frame, "rowKeyBottomH");
        int keyW = (int) size(frameClass, frame, "keyW");
        int keyH = (int) size(frameClass, frame, "keyH");
        java.lang.reflect.Field rowKeysF = frameClass.getDeclaredField("ROW_KEYS");
        java.lang.reflect.Field colKeysF = frameClass.getDeclaredField("KEYS");
        rowKeysF.setAccessible(true);
        colKeysF.setAccessible(true);
        int rowKeys = rowKeysF.getInt(null);
        int colKeys = colKeysF.getInt(null);

        g.setColor(new Color(0x00FF66));
        for (int i = 0; i < rowKeys; i++) {
            boolean wide = i == 0 || i == rowKeys - 1;
            int[] top = (int[]) rowKey.invoke(frame, false, i, wide);
            g.drawRect(top[0], top[1], (wide ? rowWide : rowW) - 1, rowH - 1);
            int[] bottom = (int[]) rowKey.invoke(frame, true, i, i == 0);
            g.drawRect(bottom[0], bottom[1], ((i == 0) ? rowWide : rowW) - 1, rowBottomH - 1);
        }
        for (int i = 0; i < colKeys; i++) {
            g.drawRect((int) keyX.invoke(frame, false), (int) keyY.invoke(frame, i), keyW - 1, keyH - 1);
            g.drawRect((int) keyX.invoke(frame, true), (int) keyY.invoke(frame, i), keyW - 1, keyH - 1);
        }

        g.dispose();

        File file = out.resolve("frame-texture.png").toFile();
        ImageIO.write(img, "png", file);
        System.out.printf("frame texture: %dx%d -> %s%n", w, h, file);
    }

    private static Object size(Class<?> owner, Object frame, String name) throws Exception {
        Method m = owner.getDeclaredMethod(name);
        m.setAccessible(true);
        return m.invoke(frame);
    }

    private FrameTextureGen() {
    }
}
