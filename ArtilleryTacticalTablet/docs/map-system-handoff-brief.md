# Map/Terrain System — Redevelopment Brief

For an AI attempting to redevelop the map subsystem of ArtilleryTacticalTablet (a Minecraft
Forge 1.20.1 mod). Written from reading the current, working implementation — not written by
whoever originally designed it. Every claim below is sourced from the actual code; file:line
citations point at the current source so you can verify or dig deeper before changing anything.

## Core design decision — read this first

The map is **grid-first, imagery-second**. Three earlier approaches — using another mod's tile
API, sampling the client's own loaded chunks only, reading a rival minimap's internals — were all
tried and abandoned, because each depended on an external image source that could silently stop
supplying ground. The doc comment on `MapPanel` (client/screen/MapPanel.java:16-30) states the
reasoning directly: a grid "reads the same at 50 blocks and at 5000" and matches how artillery is
actually directed (grid reference and bearing, not a photograph). Terrain is treated as **a layer
that is allowed to be absent** — surveyed ground draws under the grid where known; elsewhere the
grid alone shows, and the two states must look visually distinct, because "a device that shoots
five kilometres must never dress up a gap as flat ground."

**If you reimplement this from scratch and default to photographic/imagery-first design, you are
repeating the approach this codebase already tried and rejected.** Keep the grid as the primary,
always-present layer; terrain imagery is an enhancement drawn under it, never a requirement.

## Layer map

```
ServerTerrainProvider (samples world) ─┐
                                        ├─→ ServerTileCache (2s memory) ─→ ServerTileStore (disk)
ServerSurveyBudget (rate limit) ───────┘                                        │
                                                                                  ▼
                                                              TerrainTileMessage (network)
                                                                                  │
                                                                                  ▼
                                          TerrainClientCache (tile store, fetch scheduling)
                                                                                  │
                                                        ┌─────────────────────────┤
                                                        ▼                         ▼
                                                  TerrainDisk                TerrainMips
                                              (client on-disk cache)   (mip chain + colour)
                                                                                  │
                                                                                  ▼
                                                                           TerrainImage
                                                                (GPU sheets: bake, cache, blit)
                                                                                  │
                                                                                  ▼
                                                                            MapPanel
                                                                (grid overlay, zoom/pan, boot)
                                                                                  │
                                                                                  ▼
                                                                          TabletScreen
                                                              (markers, header, panels, keys)
```

## Client side

### `MapPanel.java` — what `TabletScreen` actually talks to

Package-private, one instance per tablet screen, but its **view state (`zoomIndex`,
`centreX`/`centreZ`, `gridShown`) is `static`** — deliberately, because the screen is destroyed
and rebuilt every time the tablet GUI opens, and instance state would reset the player's zoom/pan
on every open (MapPanel.java:100-106).

Key API `TabletScreen` calls:
- `tick()`, `render(g, x, y, w, h)`, `reserve(int[]... rects)` (UI panels tell the map what
  rectangles to leave undrawn under)
- `restartBoot()`, `toggleGrid()`/`gridShown()`, `booting()`
- `worldToScreen`/`worldToScreenUnclipped`/`screenToWorld` — the coordinate seam
- `zoomBy(delta)`, `panByPixels(dx, dy, w, h)`, `recentreOnPlayer()`/`followsPlayer()`
- `spanLabel()`, `spanBlocks()`, `blocksPerPixel(w, h)` (used for click hit-tolerance)

**Zoom is 8 fixed round-number spans** — `{250, 500, 1000, 2000, 4000, 8000, 16000, 32000}`
blocks across the panel (MapPanel.java:46) — not powers of two, because artillery is worked in
metres and thousand-block grid squares (MapPanel.java:36-40). The top steps are effectively
grid-only, beyond where terrain is realistically surveyed.

`centreX`/`centreZ` are **fractional doubles**, not integers, specifically because rounding each
tile's edges independently during a slow drag produced visible "rippling" (MapPanel.java:112-124).
`panByPixels` accumulates directly into these doubles rather than converting per-axis per-step,
because small per-pixel drag deltas would otherwise round to zero or crab diagonally
(MapPanel.java:537-556).

`screenToWorld` uses **floor, not round** — rounding would place a block's midpoint in the wrong
neighbouring block (MapPanel.java:524-526).

MapPanel draws **display pixels**, not GUI-scaled interface pixels — at GUI scale 3, a
nominally-1700px panel is really only ~560 device pixels, and using interface-pixel space would
cap texel detail. Grid lines/graticule share the same transform as terrain so the two never round
independently and drift apart (MapPanel.java:306-343). Text labels are drawn back in interface
pixels afterward, since bitmap glyphs go soft under arbitrary scale (MapPanel.java:390-413).

### The "boot" concept

`booting()` gates whether `TabletScreen` shows a boot splash instead of the map. It is a **latch,
not a live test** — once a world has booted, it stays booted; it must not flip back just because a
pan reaches unsurveyed ground (MapPanel.java:173-181). Two bounds matter:
- `BOOT_MAX_MS = 8000` — a ceiling, because "fully surveyed" can genuinely never happen.
- `BOOT_MIN_MS = 900` — a floor, because reopening the tablet after closing it leaves the cache
  already warm; without a minimum the splash would never actually be seen.

Readiness = `!TerrainClientCache.isWaiting() && terrain.isComplete()` — both "nothing outstanding"
and "no square the draw wanted and couldn't have," and bare/never-surveyed ground satisfies both,
so it can't block boot forever (MapPanel.java:426).

### `TerrainImage.java` — the actual tile renderer (MapPanel delegates entirely here)

**What a "tile" is on GPU**: a "sheet." `TILES_PER_SHEET = 4`, so one 256×256 (+1px border for
filter sampling) texture covers a 4×4 block of the underlying 64-block server `TerrainTile`s.
Chosen because draw width was capped at 8 quads across the panel; anything smaller forced a
coarser mip level long before the panel actually ran out of screen pixels (TerrainImage.java:100-120).

**Why this exists**: the map used to rebuild one giant texture from cache on every view move — a
one-block pan resampled a quarter-million pixels every frame. Sheets are now built once and only
rebuilt when the *data* underneath them changes; panning/zooming costs only "where each quad goes"
(TerrainImage.java:29-51).

**Levelled regions**: live texture count is capped (`MAX_LIVE_TEXTURES = 256`, ~50MB VRAM); at
wide zoom, sheets are drawn at coarser "levels" covering `LEVEL_FACTOR = 2`× the ground per level
for the same texel budget (TerrainImage.java:141-177) — the same levelled-region architecture used
by JourneyMap/Xaero, confirmed by decompiling their bytecode, not an incidental resemblance
(TerrainImage.java:28-30).

**Background baking**: gather+shade math runs on a 2-thread daemon pool (`BAKE_POOL`); the render
thread only uploads finished RGBA buffers. Per-frame budgets: 4 first-time builds, 1 rebuild
(refresh of stale sheets — allowed to lag), 8 in-flight bakes, 48 cheap synchronous patches
(TerrainImage.java:288-292).

**Underlay/patch strategy**: a coarser level draws underneath the fine level first
(`UNDERLAY_STEPS = 2`). On a zoom change, the whole coarse view stands in while fine sheets bake
("arrives blurred, then sharpens"). On ordinary panning, only the specific missing edge squares
get patched from the coarse level, so a coarse sheet never smears under ground that's already
sharp (TerrainImage.java:479-520). `warmNextZoom` spends leftover per-frame budget pre-building
the next-finer zoom level over half the current view, so zooming in doesn't blur-then-sharpen
(TerrainImage.java:738-775).

**Change tracking**: a 4096-entry ring buffer of `(tileKey, version)` lets a stale sheet ask
"which tiles actually changed under me" and patch a small rectangle instead of a full rebuild —
unless the changed area exceeds 50% of the texture, where a full rebuild is cheaper
(TerrainImage.java:1174-1178).

### `TerrainMips.java` — the mip chain and the single point of truth for ground colour

A "mip" here is one of **6 pre-averaged reductions** of a 64×64 tile (32/16/8/4/2/1 texels/side),
built via strict 2×2 box-downsampling **in linear light** (gamma 2.2), lazily on first use by a
wide view — not eagerly on tile arrival, since eager reduction on the render thread once made
receiving a batch of tiles a frame-blocking cost that close-zoom players never even used
(TerrainClientCache.java:320-333).

**Rejected alternative**: sampling at a stride (reading 1-in-4 or 1-in-64 columns) instead of
averaging — this made each wide-zoom pixel "a coin toss" decided by under 1% of the ground under
it, producing speckle instead of coastlines (TerrainMips.java:14-19).

**One function decides every column's colour, always** — `TerrainMips.groundColour(...)`, quoted
directly: *"Here, and not at the moment of drawing, because a column's colour is decided in
exactly one place and this is it."* (TerrainMips.java:378-379). Both the finest draw level and
every mip level call this same function, so nothing changes discontinuously across a zoom-level
boundary (TerrainMips.java:272-278). It layers: base block colour → biome tint (two different
maths depending on whether the source texture is pre-tinted or raw grey) → water-depth blend
(murk increases with depth, opaque past 8 blocks) → filter dispatch.

**Three filter modes** (`TerrainMips.Filter`, cycled by `TabletDisplay.cycleFilter()`):
1. `NONE` — realistic colours.
2. `THERMAL` — contrast-stretched greyscale luminance so forest/water (a few % apart normally)
   separate; hazards forced near-white.
3. `RELIEF` — a 7-stop hypsometric ramp keyed on surface height alone (block colour discarded),
   banded every 12 blocks with alternating darkening to flatten canopy noise into contour-like
   steps; water flattened to one flat pale blue-grey.

**Why brightness and filter are two different mechanisms** — this is the clearest single lesson
in the whole subsystem, stated in `TabletDisplay.java:9-30`: brightness is a pure draw-time GL
colour multiply that never touches baked pixels, because "if [brightness] did [invalidate sheets],
every press of a brightness key would invalidate several hundred sheets and set the whole map
rebuilding, which is precisely the treadmill that made a patch of it freeze for a whole session."
Only the filter switch triggers a full repaint, and it's deliberately treated as expensive and
rare — "a key nobody presses twice in a fight."

### `TerrainClientCache.java` — client tile store & fetch scheduling

Three states per tile: held, asked-and-waiting, answered-empty (a final answer, never retried as
failure). Sources ground in priority order: (1) the client's own loaded chunks — free, instant,
covers "the part being looked at most"; (2) client's on-disk `TerrainDisk` cache; (3) a server
request packet, nearest-to-view-centre first. Fully-surveyed tiles are never re-asked about ("no
fire-direction map updates itself with battle damage — an observer reports it"); incomplete tiles
get a throttled staleness refresh so it can't starve genuinely new requests. Capped at 8192
remembered tiles, silent eviction (no version bump — nothing visible changed) for far-away ground.

### `TerrainDisk.java` — client's persistent tile cache

One file per tile, same binary format as the network wire format, one background IO thread. Keyed
by save-folder name rather than display name (two worlds can share a display name), specifically
guarded against a `LevelResource.ROOT` `"."` path-segment bug that once filed every single-player
world's tiles under one shared folder (TerrainDisk.java:231-247).

## Server side

### Data flow
`ServerTerrainProvider.buildTile` samples 16 chunks per 64-block tile — either live from memory or
via `ChunkNbtSampler`/`TerrainChunkReader` from disk — through a 2s in-memory `ServerTileCache`,
optionally persisted by `ServerTileStore`, sent as `TerrainTileMessage`.

### `TerrainTile` wire format
64×64 columns: block id (ushort), surface height (short, sentinel for "no data"), water depth
(byte), biome (short). **Height is always the surface** (water top where wet); depth +
(height − depth) recovers the floor — kept separate because ballistics needs the surface (a shell
is stopped by water) while the map needs the floor for shading; conflating them gives false
clearance answers (TerrainTile.java:80-90). Fields are packed hi-byte-run / lo-byte-run rather than
interleaved per-column, because block ids cluster in the low thousands so the high byte is almost
always identical across thousands of columns — interleaving would break that run and defeat
deflate (TerrainTile.java:141-160).

### `ServerTileStore` — why it exists
Measured cost: drawing a viewful ≈0.04s; **fetching** it ≈10-20s (each tile = 16 chunk
reads+decompress), and the server used to resurvey on every request. `ServerTileStore` persists
only fully-complete tiles to disk beside the world save, indexed in memory via a
directory-walk-once set so a miss costs nothing. Served only when none of a tile's chunks are
currently loaded (i.e., nobody could be actively changing that ground) — loaded chunks always take
the live-sample path instead, which is both fresher and already fast.

### `ServerSurveyBudget` — server-owned rate limiting
Moves throttling from client-side-only (N players = N× uncapped server cost) to a server admission
system: 256 concurrent surveys total, 96/player cap, half the total reserved for background
warming. Refused requests are **queued, not dropped**, oldest-drop-first on overflow ("the newest
request is closest to what's on their screen now") — the earlier refuse-and-rely-on-client-retry
design produced a visibly speckled ring of missing tiles. `SurveyLimits.java` centralizes every
numeric relationship between client batch size, packet capacity, and server queue depth with
runtime assertions, because **six separate bugs in one session** all traced back to two numbers
that had to agree but were hand-written independently in two files.

### `ServerTerrainWarmer` — proactive pre-survey
Spirals outward from each player on the server tick, nearest-ring-first, skipping tiles already
stored, through the same budget/cache path a real request uses. Rate adapts: full speed when
nobody's actively panning (`anyoneWatching()` false), throttled otherwise — modeled on
Dynmap/BlueMap-style pre-rendering but continuous/adaptive rather than an offline batch job.

### `TerrainChunkReader` — dedicated I/O
Opens the world's region files a second time through Minecraft's own `ChunkStorage` (explicitly
not a custom region-file parser), giving terrain reads their own worker thread(s) instead of
contending with the game's single ordinary-chunk-save thread. Took survey throughput from ~4 to
~118 tiles/s. Two readers on one file can race, but Minecraft's write-then-update-header pattern
means a reader sees old-or-new, never torn — worst case one tile fails to decode and reports
unsurveyed. Read-only, cannot corrupt the world.

### Network packets
- `RequestTerrainTilesMessage` (client→server) — batched coordinates + each tile's currently-held
  content hash, capped at 32 tiles/request, requires holding the tablet item.
- `TerrainTileMessage` (server→client) — three distinct shapes: full tile, empty-marker
  (surveyed-and-bare), or unchanged (coordinates + confirmation only) — to avoid resending ~12KB
  for either "nothing there" or "nothing changed."

## Test harness — `src/mapcheck`

A separate Gradle source set (`./gradlew mapCheck`), boots only vanilla registries (no window, no
world) and runs, **via reflection into the real production classes** (not reimplemented copies —
"a harness that keeps its own copy of the loop it is checking drifts away from the real one"):
assertion-style checks against `TerrainImage`/`TerrainMips`/`TerrainClientCache` internals, plus
`TileRender`, which feeds a synthetic tile through the actual gather-and-shade pipeline and writes
a real PNG — so colour bugs are visible at a glance instead of requiring in-game reproduction.

**Why this matters for a redevelopment**: it proves the production gather/shade math is
side-effect-free and independent of a running client wherever possible. Keeping that property is
what makes this kind of headless, screenshot-diffable testing possible at all — worth preserving
deliberately, not by accident.

(The `mapcheck/convert/` subpackage — `Convert`, `MedianCut`, `RectMerge`, `Emit`, etc. — is
unrelated to terrain; it's an image-to-vector-rectangle asset pipeline for the tablet's physical
case art, not the map. Ignore it for map redevelopment purposes.)

## Lessons already learned — carry these over explicitly

- Grid-first, imagery-optional. Terrain absence must look different from terrain presence.
- View state must survive the screen being destroyed/rebuilt on every open (use a session-scoped
  store, not per-instance state).
- Fractional (not integer) pan/zoom coordinates avoid visible rippling during drags.
- One function decides a column's colour, called from every code path that needs it (draw + every
  mip level) — never let two places compute colour independently.
- Cosmetic adjustments (brightness) must never invalidate cached/baked data; only a genuine palette
  change (filter) should trigger a rebuild, and that should be treated as rare/expensive on purpose.
- Rebuild only what changed (tile-version tracking + small-rectangle patching), not the whole
  visible area, on every small data change.
- Background compute (bake/shade) on worker threads; GPU upload and any `Minecraft.getInstance()`-
  touching resolution (biomes, block models) stays on the render thread only.
- Stamp background work with a "world generation" token; discard results computed for a world that
  has since changed, or you'll paint stale content into a new world.
- Rate-limit server-side, not just client-side, or player count multiplies server cost with no cap.
- Queue refused/throttled work rather than dropping it, and prefer dropping the *oldest* queued
  item over the newest.
- Keep every numeric relationship between two independently-maintained protocol constants
  (batch size vs. packet cap vs. queue depth) in one shared source file with a runtime assertion,
  not duplicated by hand in two places.
- Give expensive I/O (terrain chunk reads) its own worker thread(s) via the game's own chunk
  storage API rather than a hand-rolled region-file reader — reuse, don't reimplement, file
  parsing that the game already gets right.
- Persist only fully-complete survey results to disk; never cache and later serve a partial tile
  as if it were final.
