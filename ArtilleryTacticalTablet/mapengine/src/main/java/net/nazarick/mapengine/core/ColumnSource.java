package net.nazarick.mapengine.core;

/**
 * Where surveyed ground comes from. The engine never knows.
 *
 * <p>This is the seam that keeps Minecraft out of the engine. Inside the mod it is implemented twice
 * over — from chunks the client already has loaded, which costs nothing, and from chunks saved on
 * disk, which is the only way to know ground nobody is standing on. Neither of those belongs here:
 * the engine's job starts once the columns exist.
 *
 * <p>It is also what makes the benchmarks honest. A synthetic implementation generates terrain in
 * memory, so every timing this engine reports is measured without a game, a world, or a disk full of
 * someone's save.
 */
public interface ColumnSource {
    /**
     * Fills {@code into} with the ground covering {@code into.width} blocks square, starting at the
     * given world block coordinate. A column the source knows nothing about is left as
     * {@link ColumnBuffer#NO_DATA} rather than guessed at — a map for laying guns must be able to
     * say it does not know.
     *
     * @return true when at least one column was filled
     */
    boolean fill(int blockX, int blockZ, ColumnBuffer into);

    /**
     * Whether this source could answer about the given region at all right now.
     *
     * <p>Asked before the work is queued, so a store does not spend a slot on ground the source
     * cannot reach — the client-side source, for instance, only ever knows chunks inside render
     * distance.
     */
    default boolean canAnswer(int regionX, int regionZ) {
        return true;
    }
}
