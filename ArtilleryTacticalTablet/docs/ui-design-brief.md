# Artillery Tactical Tablet — UI Design Brief

For an artist/designer AI to work from. Written by a coding-focused agent that got the
functional layout right but produced visually mediocre results across three rounds
(rectangle-merge from a photo, hand-pixel texture, plain flat code shapes). This brief hands
off the *visual* design; the *code integration* stays on the engineering side.

## What this is

A Minecraft (Forge 1.20.1) mod: a handheld artillery fire-control tablet. Opening it shows a
full-screen GUI — a device "case" (the bezel/frame) with a map filling the middle and 32
physical keys standing on the case around the edges.

## Two real references, two different philosophies — pick or blend

**Reference A — "RSD" concept** (fictional/AI-rendered, attached earlier): thin bezel, keys on
all four sides (top row, bottom row, left column, right column), text-labelled keys (3-letter
codes), icon keys for a few special functions (recentre, brightness, filter, power).

**Reference B — Leonardo DRS MRT104 II** (real hardware photo, attached earlier): tan/coyote
rugged tablet, keys on **left and right columns only** — no top or bottom row. Left column:
STATUS/power, a combined BRT+/VOL+ key, a combined BRT-/VOL- key, a round toggle, then FN1–FN4,
then a function-shift key. Right column: a red emergency key, then FN5–FN12. Keys sit in
individually-routed slots with visible gaps between them.

Current code follows Reference A's four-sided layout. Reference B is worth studying for its
*physical key language* (routed slots, combined dual-function keys, the red emergency key
placement) even if the four-sided arrangement is kept — pick whichever layout philosophy you
think reads better, or propose a blend. This is a genuine open choice, not a constraint.

## The 32 keys and what each one does

If keeping the four-sided layout, here is the current function assignment (positions can move,
but try to preserve which functions exist and roughly which edge/corner they cluster in —
left-edge keys are all gun-laying actions, right-edge and most of the top/bottom are
placeholder "spare" keys reserved for future features):

**Top row, left→right:** GRID (toggle map grid, icon) · BTY (Battery tab) · TGT (Targets tab) ·
AMO (Ammo tab) · STA (Status tab) · LOG (Log tab) · F17 (spare) · F18 (spare) ·
BRIGHT− (dim) · BRIGHT+ (brighten)

**Bottom row, left→right:** FLT (night/day display filter, icon) · F9–F16 (8 spares) ·
POWER (red, turns the screen on/off)

**Left column, top→bottom:** CFF (red — fires on the selected target) · ADJUST (lays guns on
target without firing) · MODE (cycles fire mode) · ARC (cycles ballistic arc) · F1 · F2 (spares)

**Right column, top→bottom:** F3–F8 (6 spares)

Every key (including spares) has a small square LED-style indicator near it that lights up to
show state — the *only* thing allowed to change colour. Key caps themselves stay one fixed
colour whether pressed, hovered, or active; this was an explicit, firm decision earlier in the
project (buttons look "dead" until you touch the LED-carrying state).

## Current visual style (code-drawn, flat, works but plain)

- Dark neutral gunmetal shell, `#222224`, flat rounded rectangle, no gradient
- Screen well: pure black, thin recessed bezel
- Every key: identical square size, flat fill (`#3A3A3E` normal / `#6C1B16` for the two red
  keys — CFF and POWER), 1px darker outline, small two-layer offset drop shadow
- Small square LED dot beside each key, dark when off
- Text labels drawn by the game's own bitmap font, magnified to fill about a third of the cap

## What's actually needed from you

Pick ONE of these two delivery shapes — either is fine, say which one you're doing:

**A. A texture** — a PNG of the case with every key cap, its label, and the screen cutout, at a
resolution around 1500–2000px wide, aspect ratio doesn't need to be exact (it gets stretched to
whatever window shape the player has, always close to 16:9 but not fixed). Screen area must be
fully transparent (alpha 0) so the live map shows through it. Key labels can be baked into the
image *only if* you also list, in a separate note, the pixel bounding box of every one of the 32
key caps (top-left corner + width/height) in the source image — without that, the engineering
side cannot line up click zones and LED positions with what's drawn, which is exactly what went
wrong twice already this project.

**B. A style spec, no image** — colours (hex), corner radius, shadow parameters, border weight,
LED dot size/position rule, described precisely enough that it gets redrawn in code. Cheaper to
integrate correctly, less visually rich than a real texture.

## Hard lessons from this project, worth reading before starting

- **A photo or AI-render is not clean source material.** Anti-aliased/soft edges from a
  photorealistic image do not downsample cleanly to Minecraft's blocky rendering — they come
  out as random staircase noise, not intentional pixel art. If working from Reference A/B,
  redraw shapes with deliberate hard edges rather than tracing soft ones.
- **Every key must report a precise bounding box if baked into a texture.** The engineering
  side computes click zones and LED positions from formulas, not by eye — a texture whose key
  positions cannot be stated as exact numbers cannot be wired up correctly.
- **Keep the key count at 32** (or say clearly how many you're changing it to and why) — the
  underlying game logic expects specific functions to exist somewhere on the case.
