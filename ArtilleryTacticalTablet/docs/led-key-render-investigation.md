# LED/Key rendering mismatch — investigation report (Claude → Gemini)

## Symptom
In-game, the LED and key visuals still look flat/plain — no visible glow or bevel — even though
the label text is confirmed correct (`WPN`, not the old stale `AMO`).

## What was ruled out
This was suspected to be a build/classpath problem (stale compiled bytecode shadowing the real
one), so it was investigated end-to-end on the backend/build side:

1. **Source is correct.** `git diff HEAD -- UiButton.java` against commit `888f553` is empty —
   the working tree exactly matches your latest pushed commit. No local drift.
2. **Build is fresh.** `build/classes/java/main/.../UiButton.class` postdates the source file's
   own mtime; the in-game WPN label proves the running JAR reflects this exact build.
3. **The stale `bin/` directory is not the cause.** There is a second, older compiled-class tree
   at `ArtilleryTacticalTablet/bin/` (5 days stale — looks like VS Code's Java Language Server
   writing its own incremental-compile output there, unrelated to Gradle). Checked
   `build.gradle` directly: no `sourceSets`, no custom task, nothing references `bin/` at all.
   Forge's `runClient` task classpath comes entirely from Gradle's own
   `build/classes/java/main` + `build/resources/main`. `bin/` is never consulted.

**Conclusion: this is not a stale-build or classpath issue.** The exact code you wrote is what's
compiling and running in-game.

## What's left
Since source/build are both confirmed correct, the remaining explanation is a genuine difference
between your **standalone preview/renderer tool** and Minecraft's **real `GuiGraphics` render
pipeline** at runtime — most likely something like:
- Alpha value on the glow/bevel `fill()` calls not actually blending the way it does in the
  preview tool (e.g. blend state, layering order, or `GuiGraphics` batching eating the alpha).
- The glow/bevel draw calls happening but being immediately overdrawn by something else in
  `UiButton.render()`/`drawLamp()`.
- A scale/coordinate mismatch making the effect render at near-zero size in-game.

This needs someone looking at `UiButton.java`'s actual draw order and blend calls against real
`GuiGraphics` behavior — that's rendering-code territory, so handing this back to you rather than
digging further on this side.

## Where to look
- `UiButton.java` — `render()`/`drawLamp()`, the glow/bevel `fill()` calls specifically.
- Compare draw order: is anything drawn after the glow that could cover it (dish base, border)?
- Confirm the alpha channel of glow colors survives however `GuiGraphics.fill()` is being called
  (RenderType, blend function).

## Update (after `bbe90ca`) — root cause of the lit/unlit size mismatch found

`bbe90ca` fixed the "LED core never renders" bug (the old inset `fill(x1+1, y1+1, x2-1, y2-1, ...)`
collapsed to empty/inverted at real in-game LED sizes). Confirmed fixed — lit LEDs now render.

New symptom after that fix: **lit LEDs look normal-sized, unlit LEDs look like a barely-visible
speck**, much smaller than the lit ones (see attached screenshot — SA's lit green LED vs WPN's
unlit LED).

This is **not** a coordinate/size bug — checked `TabletFrame.rect()`/`toScreenW()`/`toScreenH()`:
LED screen width/height comes from `Math.round(designW * scale)` independent of position, so every
LED at the same `LED_SHORT`×`LED_LONG` (4×8) design size gets an identical screen-pixel size
regardless of which key it belongs to. Lit and unlit sockets are drawn from the exact same
`x1,y1,x2,y2` rect in `drawLamp()` — same box, no size difference in the geometry.

**The real cause is contrast, not geometry.** The unlit socket colour is `0xFF1C1F26`. The chassis
bezel immediately around the LED strip (`TabletChassisPaint.java`) uses colours in the same
`0xFF19..`–`0xFF2A..` luminance band (e.g. `0xFF1E2023`, `0xFF24262A`, `0xFF222428`). Against the
real baked chassis, the unlit socket nearly disappears into the bezel — only a faint smudge is
visible. The lit socket, by contrast, gets a saturated colour *plus* a glow halo
(`fill(x1-1, y1-1, x2+1, y2+1, glow)`), so it visually reads as much bigger even though the
underlying rect is the same size as the unlit one.

This likely didn't show up in your own preview/test tool if it renders LEDs against a flatter or
lighter background than the actual baked chassis art — worth checking.

**Suggested fix (your call, this is still rendering territory):** give the unlit socket enough
contrast to read as a visible dark lens against the real bezel — e.g. a distinct rim/highlight
colour (the existing top/bottom glint lines already do this a little, but the base fill colour
itself needs to separate more clearly from `TabletChassisPaint`'s surrounding tones), or darken it
further so it reads as an intentional "black glass" rather than blending into the bezel.

## Same class of problem on the case keys themselves

The keys also look flatter/less 3D in-game than intended. Traced through `UiButton.render()`'s
`hard` branch (the moulded-cap path): every decorative band — border, `wallThickness`, the 1px
shoulder-light line, `dishBevel`, `innerMargin` — is computed as `Math.max(1, Math.round(w *
(fraction / 44f)))`, where `w` is the key's *actual on-screen* pixel width from
`TabletFrame.keySize()` (`toScreenW(KEY_SIZE)`, `KEY_SIZE = 44` design px).

At a typical real window size and default GUI scale, `keySize()` comes out well under 44 real
screen pixels (design canvas gets scaled down to fit the window — see `TabletFrame.fit()`,
`scale = min(windowW/980, windowH/630) * 0.95`). At that size, nearly every one of those bands
(`wallThickness`, `dishBevel`, the shoulder line) rounds down to the same **1px minimum** —
meaning border, wall, shoulder-light, rim-top, and dish-bevel are each only a single pixel ring
around the key face, all fighting for the same handful of pixels. The result is a much flatter,
less-graduated look than a large preview canvas would show, where those same fractions produce
several pixels per band instead of one.

This isn't a bug exactly — the math is doing what it's told — but it means the bevel was likely
tuned/eyeballed at a canvas size much bigger than what real gameplay actually renders at. Worth
deciding whether to: give the thin bands (wall, shoulder line, dish bevel) a higher pixel floor
than 1 at small key sizes, lean harder on colour contrast between bands instead of width to carry
the 3D read, or just confirm this is intended at typical GUI scales.
