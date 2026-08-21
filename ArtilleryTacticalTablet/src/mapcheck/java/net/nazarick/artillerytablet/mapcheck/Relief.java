package net.nazarick.artillerytablet.mapcheck;

import java.lang.reflect.Method;

import static net.nazarick.artillerytablet.mapcheck.MapCheck.check;

/**
 * Checks that the table standing in for the shading's squashing curve really is that curve.
 *
 * <p>It was replaced because it was the whole cost of drawing a square — a hyperbolic tangent in
 * double precision, called twice for every texel, thirty-four thousand times per build. A table is
 * only a fair swap if it agrees with what it replaced, and "agrees" has to be a number rather than
 * an impression, because the way this would go wrong is not a crash or an obviously bad picture. It
 * is relief that is subtly the wrong shape, which nobody would catch by looking.
 *
 * <p>The tolerance is derived from the step, not chosen to make the test pass: with a thousand steps
 * across the range and a curve whose slope never exceeds one, rounding to the nearest entry cannot
 * be wrong by more than half a step.
 */
final class Relief {
    private Relief() {
    }

    static void run() throws Exception {
        Class<?> c = Class.forName("net.nazarick.artillerytablet.client.terrain.TerrainImage");
        Method squash = c.getDeclaredMethod("squash", float.class);
        squash.setAccessible(true);

        int steps = MapCheck.readInt(c, "SQUASH_STEPS");
        float limit = readFloat(c, "SQUASH_LIMIT");

        // Half a step of argument, and the curve's slope is at most one, so this is the most the
        // value can be out by. Nothing here is tuned; change the table's size and this follows.
        double tolerance = limit / steps;

        double worst = 0;
        float worstAt = 0;
        // Well past the ends on purpose: outside the table the answer is clamped, and a clamp is
        // only honest where the curve really has flattened.
        for (int i = -2000; i <= 2000; i++) {
            float x = i * (limit * 1.5f / 2000f);
            double got = (float) squash.invoke(null, x);
            double want = Math.tanh(x);
            double off = Math.abs(got - want);
            if (off > worst) {
                worst = off;
                worstAt = x;
            }
        }

        check(worst <= tolerance,
                "table is out by " + worst + " at " + worstAt + ", tolerance " + tolerance);
        check((float) squash.invoke(null, 0f) == 0f, "the curve passes through zero");
        check((float) squash.invoke(null, 100f) == 1f, "far above the range it is flat at one");
        check((float) squash.invoke(null, -100f) == -1f, "far below the range it is flat at minus one");

        System.out.printf("relief: OK  squash table is within %.5f of tanh (tolerance %.5f)%n",
                worst, tolerance);
    }

    private static float readFloat(Class<?> owner, String name) throws Exception {
        java.lang.reflect.Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        return field.getFloat(null);
    }
}
