package net.nazarick.artillerytablet.mapcheck.convert;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;

/**
 * Runs a generated class through the real {@code Paint} seam and writes what it drew.
 *
 * <p><b>Why the preview inside the converter is not enough.</b> That one is drawn by the converter's
 * own idea of the rectangles. This one is drawn by the file that will be compiled into the mod,
 * through the same interface the game draws through — so it closes the last gap between "the tool
 * says this is what you get" and "this is what you get". It is the same argument {@code CaseView}
 * exists for, applied to generated code instead of hand-written code.
 *
 * <p>It also proves the coordinate mapping at a size other than the one the picture was measured at,
 * which is where a rounding mistake would show as hairline gaps.
 */
public final class GeneratedView {
    public static void main(String[] args) throws Exception {
        String className = args.length > 0 ? args[0]
                : "net.nazarick.artillerytablet.client.screen.ConvertedCase";
        Path out = Path.of(args.length > 1 ? args[1] : "build/mapcheck");
        int w = args.length > 2 ? Integer.parseInt(args[2]) : 1803;
        int h = args.length > 3 ? Integer.parseInt(args[3]) : 1014;

        Class<?> generated;
        try {
            generated = Class.forName(className);
        } catch (ClassNotFoundException missing) {
            System.out.println("generated: skipped, no class " + className);
            return;
        }

        Class<?> paintClass = Class.forName("net.nazarick.artillerytablet.client.screen.Paint");
        Method draw = generated.getDeclaredMethod("draw", paintClass,
                int.class, int.class, int.class, int.class);
        draw.setAccessible(true);

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0xFF00FF));
        g.fillRect(0, 0, w, h);

        // Magenta ground on purpose: anything the generated code fails to cover comes back as a
        // colour that is in no palette and cannot be mistaken for part of the design. A black
        // ground would hide exactly the gaps this is here to find.
        Object recorder = java.lang.reflect.Proxy.newProxyInstance(
                GeneratedView.class.getClassLoader(), new Class<?>[]{paintClass},
                (proxy, method, methodArgs) -> {
                    switch (method.getName()) {
                        case "fill" -> {
                            int x0 = (int) methodArgs[0];
                            int y0 = (int) methodArgs[1];
                            int x1 = (int) methodArgs[2];
                            int y1 = (int) methodArgs[3];
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
                            return java.lang.invoke.MethodHandles.lookup()
                                    .findSpecial(method.getDeclaringClass(), method.getName(),
                                            java.lang.invoke.MethodType.methodType(
                                                    method.getReturnType(),
                                                    method.getParameterTypes()),
                                            method.getDeclaringClass())
                                    .bindTo(proxy)
                                    .invokeWithArguments(methodArgs == null
                                            ? new Object[0] : methodArgs);
                        }
                    }
                });

        draw.invoke(null, recorder, 0, 0, w, h);
        g.dispose();

        int uncovered = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if ((img.getRGB(x, y) & 0xFFFFFF) == 0xFF00FF) {
                    uncovered++;
                }
            }
        }

        File file = out.resolve("generated-case.png").toFile();
        ImageIO.write(img, "png", file);
        System.out.printf("generated: OK  drew %s at %dx%d, %d uncovered pixels -> %s%n",
                generated.getSimpleName(), w, h, uncovered, file);
    }

    private GeneratedView() {
    }
}
