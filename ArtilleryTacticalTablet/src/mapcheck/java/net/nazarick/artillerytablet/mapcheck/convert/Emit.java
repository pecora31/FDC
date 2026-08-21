package net.nazarick.artillerytablet.mapcheck.convert;

import java.util.List;

/**
 * Writes the rectangles out as Java that draws through {@code Paint}.
 *
 * <p><b>Why the generated method builds two boundary arrays instead of doing the arithmetic at every
 * rectangle.</b> This is the one place the converter could quietly produce a picture full of
 * hairline gaps, and it is a mistake this project has already paid for more than once under a
 * different name: a region of geometry defined in more than one place stops agreeing with itself.
 * Two rectangles that share an edge in the grid must share it on screen, and the only way to
 * guarantee that is for both to read the same number rather than each computing its own from a
 * multiply and a divide. Built once into {@code cx} and {@code cy}, a shared edge is the same array
 * element by construction, at any destination size, under either rounding rule.
 *
 * <p>The loops that fill those two arrays are the only loops in the output, and they draw nothing.
 * Every rectangle is still an explicit {@code fill} — which is what makes the generated file
 * something a person can open, find the chassis in, and edit by hand.
 */
final class Emit {
    /**
     * How many rectangles one generated method may hold.
     *
     * <p><b>Because a Java method cannot exceed 64KB of bytecode</b>, and a picture of any
     * complexity blows straight past it: the first real run came out at 2,846 rectangles and
     * {@code javac} refused the file outright with "code too large". That is a limit of the class
     * format, not of the drawing — the same calls split across several methods compile and run
     * identically — but it is invisible until something actually tries to compile the output, which
     * is exactly why generating code and never building it is not a check.
     *
     * <p>Five hundred is well inside the limit at roughly forty bytes of bytecode per call, and
     * leaves room for the calls to grow longer as the logical grid does.
     */
    private static final int RECTS_PER_METHOD = 500;

    private Emit() {
    }

    /**
     * How a logical edge becomes a screen coordinate.
     *
     * <p>Two rules rather than one because they fail in opposite directions and which failure
     * matters depends on the destination. {@code FLOOR} keeps the picture inside its box and lets
     * the last cell come up a pixel short; {@code ROUND} distributes the error evenly so no single
     * cell is visibly thinner, at the cost of the outer edge landing a pixel outside now and then.
     */
    enum Rounding {
        FLOOR("(int) Math.floor(i * (double) %s / %s)"),
        ROUND("(int) Math.round(i * (double) %s / %s)");

        private final String expression;

        Rounding(String expression) {
            this.expression = expression;
        }

        String forAxis(String size, String logical) {
            return String.format(expression, size, logical);
        }
    }

    static String source(List<RectMerge.Rect> rects, MedianCut palette, Pixels pixels,
                         String packageName, String className, String sourceName,
                         Rounding rounding) {
        StringBuilder b = new StringBuilder();
        int[] entries = palette.palette();

        b.append("package ").append(packageName).append(";\n\n");
        b.append("/**\n");
        b.append(" * The case, as rectangles taken off a reference picture.\n");
        b.append(" *\n");
        b.append(" * <p>Generated — do not hand-edit the run of {@code fill} calls unless you mean\n");
        b.append(" * to stop regenerating it. Produced from ").append(sourceName).append("\n");
        b.append(" * on a ").append(pixels.w).append(" by ").append(pixels.h)
                .append(" logical grid, ").append(entries.length).append(" colours,\n");
        b.append(" * ").append(rects.size()).append(" rectangles.\n");
        b.append(" *\n");
        b.append(" * <p>Every coordinate is a share of the destination box rather than a pixel\n");
        b.append(" * count, so this draws at the same proportions in any window.\n");
        b.append(" */\n");
        b.append("public final class ").append(className).append(" {\n");
        b.append("    private static final int LOGICAL_W = ").append(pixels.w).append(";\n");
        b.append("    private static final int LOGICAL_H = ").append(pixels.h).append(";\n\n");
        b.append("    private ").append(className).append("() {\n    }\n\n");
        b.append("    public static void draw(Paint p, int left, int top, int width, int height) {\n");
        b.append("        // Shared edges, built once. Two rectangles that meet in the grid read the\n");
        b.append("        // same array element here, so they cannot part company on screen.\n");
        b.append("        final int[] cx = new int[LOGICAL_W + 1];\n");
        b.append("        for (int i = 0; i <= LOGICAL_W; i++) {\n");
        b.append("            cx[i] = left + ").append(rounding.forAxis("width", "LOGICAL_W"))
                .append(";\n");
        b.append("        }\n");
        b.append("        final int[] cy = new int[LOGICAL_H + 1];\n");
        b.append("        for (int i = 0; i <= LOGICAL_H; i++) {\n");
        b.append("            cy[i] = top + ").append(rounding.forAxis("height", "LOGICAL_H"))
                .append(";\n");
        b.append("        }\n\n");
        int parts = (rects.size() + RECTS_PER_METHOD - 1) / RECTS_PER_METHOD;
        b.append("        p.batch(() -> {\n");
        for (int i = 0; i < parts; i++) {
            b.append("            part").append(i).append("(p, cx, cy);\n");
        }
        b.append("        });\n");
        b.append("    }\n");

        for (int i = 0; i < parts; i++) {
            int from = i * RECTS_PER_METHOD;
            int to = Math.min(rects.size(), from + RECTS_PER_METHOD);
            b.append("\n    private static void part").append(i)
                    .append("(Paint p, int[] cx, int[] cy) {\n");
            int lastColour = Integer.MIN_VALUE;
            for (RectMerge.Rect r : rects.subList(from, to)) {
                int argb = entries[r.colour()];
                if (argb != lastColour) {
                    b.append('\n');
                    lastColour = argb;
                }
                b.append("        p.fill(cx[").append(r.x0()).append("], cy[").append(r.y0())
                        .append("], cx[").append(r.x1()).append("], cy[").append(r.y1())
                        .append("], 0x").append(String.format("%08X", argb)).append(");\n");
            }
            b.append("    }\n");
        }

        b.append("}\n");
        return b.toString();
    }
}
