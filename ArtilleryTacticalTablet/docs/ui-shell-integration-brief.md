# Tactical Tablet UI Shell — Integration Requirements

For an AI coding assistant producing the *visual shell* of a Minecraft (Forge 1.20.1, Java 17)
mod screen. Written by the engineer who will take your output and wire it to real game logic —
read this before writing code, it exists because two earlier attempts couldn't be integrated
without a rewrite.

## What "shell only" means here

You are producing **rendering + layout only**. No game state, no fire-control logic, no data
binding. Every button's click handler can be a no-op or a debug print — it will be deleted and
replaced. What must survive into the final mod is: the drawing code, and the exact geometry of
every interactive element.

## Hard constraints

1. **Pure Java draw calls only — no image/texture loading.** Everything (chassis, bezel, keys,
   LEDs, screws, grid) is drawn with primitive fill/rect/line/text calls against Minecraft's
   `GuiGraphics`. No `ResourceLocation` to a PNG, no ImageIO, nothing loaded from disk.
2. **Exactly 32 physical keys**, laid out as: **10 top row, 10 bottom row, 6 left column,
   6 right column.** This count and arrangement is fixed by existing game logic — do not add,
   remove, or merge keys.
3. **One rectangular cutout in the centre** for the live map/screen content, with **exact pixel
   bounds you must report** (see Deliverable). Draw nothing inside that rectangle — it will be
   painted over by a separate live layer (map, targeting HUD, tab content) that your code never
   sees.
4. **Key caps never change fill colour on press/hover/active.** Only a small square LED-style
   indicator beside each key changes colour to show state. This was an explicit, firm decision
   earlier in the project — keys must look "dead" until you look at the LED, not at the cap.
   If your design changes this rule, say so explicitly rather than silently deviating.
5. **Design at one fixed reference resolution** (pick anything reasonable, e.g. 1920×1080 logical
   units) and report that resolution. Do not attempt Minecraft-specific responsive scaling
   yourself — the integration side already has scaling logic and will scale your geometry into
   it. Just be internally consistent at your chosen reference size.

## The 32 keys and their current labels

**Top row, left→right (10):** `[icon-A]` `SA` `WPN` `DEF` `SYS` `DRV` `STR` `COM` `BMS`
`[icon-B]`

**Bottom row, left→right (10):** `[icon-C]` then 8 unlabelled keys, then `PWR` (red)

**Left column, top→bottom (6):** `CFF` (red) then 5 unlabelled keys

**Right column, top→bottom (6):** 6 unlabelled keys

"Unlabelled" keys must still be drawn as physical keycaps with an LED — they carry **no fixed
text**, because their function label is drawn dynamically elsewhere depending on what's on
screen (line-select-key convention, like an aircraft MFD bezel). Leave their cap blank; do not
invent placeholder text like "F9".

`[icon-A]`, `[icon-B]`, `[icon-C]` are small icon-only keys whose function isn't finalised yet —
draw them as plain blank keycaps too, same as the unlabelled ones.

`CFF` and `PWR` are red; every other key is the same neutral colour regardless of label or
position.

## Deliverable

One self-contained `.java` file (a `Screen` subclass is fine, following the existing prototype
pattern in `client/screen/uitest/TacticalTabletScreen.java` if you want a reference for how
`GuiGraphics` primitives are used in this codebase). Alongside the class, include, as a plain
data structure (a `List`/array of a small record/POJO — id, label-or-null, x, y, width, height),
**the exact bounding box of all 32 keys plus the map cutout rectangle**, at the reference
resolution you chose. Without this list, the click zones and LED positions cannot be wired up
correctly — this exact gap is why the previous two hand-offs had to be redone.

## Style

Free choice — match whatever visual direction you and the user already agreed on (dark tactical
console, monochrome status screen, etc.). The only hard rule is constraint 4 above (no colour
change on the keycap itself).
