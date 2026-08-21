package net.nazarick.artillerytablet.tools;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nazarick.artillerytablet.ArtilleryTablet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Minimal server-tick task runner: fixed delays (ripple stagger) and per-tick polling (waiting for
 * a turret to finish slewing). Self-contained rather than reusing SBW's own tick queue, which is
 * internal to SBW rather than public API.
 */
@Mod.EventBusSubscriber(modid = ArtilleryTablet.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FireScheduler {
    private static final List<DelayedTask> DELAYED = new ArrayList<>();
    private static final List<PollingTask> POLLING = new ArrayList<>();

    public static void schedule(int delayTicks, Runnable task) {
        DELAYED.add(new DelayedTask(delayTicks, task));
    }

    /**
     * Runs {@code onReady} on the first tick {@code condition} holds. If it never holds within
     * {@code timeoutTicks}, runs {@code onTimeout} instead. Both callbacks run at most once.
     */
    public static void pollUntil(BooleanSupplier condition, int timeoutTicks, Runnable onReady, Runnable onTimeout) {
        POLLING.add(new PollingTask(condition, timeoutTicks, onReady, onTimeout));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!DELAYED.isEmpty()) {
            List<DelayedTask> due = new ArrayList<>();
            for (DelayedTask task : DELAYED) {
                if (--task.ticksRemaining <= 0) {
                    due.add(task);
                }
            }
            DELAYED.removeAll(due);
            for (DelayedTask task : due) {
                task.runnable.run();
            }
        }

        if (!POLLING.isEmpty()) {
            // Collect first, run after removal: a callback may itself schedule new work.
            List<Runnable> toRun = new ArrayList<>();
            Iterator<PollingTask> it = POLLING.iterator();
            while (it.hasNext()) {
                PollingTask task = it.next();
                if (task.condition.getAsBoolean()) {
                    toRun.add(task.onReady);
                    it.remove();
                } else if (--task.ticksRemaining <= 0) {
                    toRun.add(task.onTimeout);
                    it.remove();
                }
            }
            for (Runnable runnable : toRun) {
                runnable.run();
            }
        }
    }

    /**
     * Throws away everything still pending when the server stops.
     *
     * <p>Lesson 24, in the half of the mod where it costs more than a smear on a map. These lists
     * are static, so a ripple that was still staggering and a turret still being waited on outlive
     * the world they were ordered in — and a single-player client loads the next world in the same
     * process. Their tasks hold an artillery entity from a world that no longer exists and a player
     * who is no longer connected, and the first tick of the next world runs them.
     *
     * <p>Most of them would fail harmlessly: the aim tracker checks {@code isRemoved} and aborts.
     * "Most" is not a property worth relying on for code whose job is firing a gun.
     */
    @SubscribeEvent
    public static void onServerStopping(net.minecraftforge.event.server.ServerStoppingEvent event) {
        DELAYED.clear();
        POLLING.clear();
    }

    private static final class DelayedTask {
        int ticksRemaining;
        final Runnable runnable;

        DelayedTask(int ticksRemaining, Runnable runnable) {
            this.ticksRemaining = ticksRemaining;
            this.runnable = runnable;
        }
    }

    private static final class PollingTask {
        final BooleanSupplier condition;
        int ticksRemaining;
        final Runnable onReady;
        final Runnable onTimeout;

        PollingTask(BooleanSupplier condition, int ticksRemaining, Runnable onReady, Runnable onTimeout) {
            this.condition = condition;
            this.ticksRemaining = ticksRemaining;
            this.onReady = onReady;
            this.onTimeout = onTimeout;
        }
    }
}
