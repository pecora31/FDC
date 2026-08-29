package net.nazarick.artillerytablet.client.screen;

import com.mojang.blaze3d.platform.NativeImage;
import net.nazarick.artillerytablet.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.nazarick.artillerytablet.client.screen.app.AppManager;
import net.nazarick.artillerytablet.client.screen.app.FireControlApp;
import net.nazarick.artillerytablet.client.screen.app.LogApp;
import net.nazarick.artillerytablet.client.screen.app.MapApp;
import net.nazarick.artillerytablet.client.screen.app.StatusApp;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.nazarick.artillerytablet.client.FireLog;
import net.nazarick.artillerytablet.client.TabletClientData;
import net.nazarick.artillerytablet.client.hud.FireMissionClientState;
import net.nazarick.artillerytablet.client.hud.TargetReachabilityCache;
import net.nazarick.artillerytablet.fire.FireMode;
import net.nazarick.artillerytablet.fire.MissionState;
import net.nazarick.artillerytablet.fire.TargetStatus;
import net.nazarick.artillerytablet.network.AbortMissionMessage;
import net.nazarick.artillerytablet.network.BindArtilleryMessage;
import net.nazarick.artillerytablet.tools.ArtilleryLabel;
import net.nazarick.artillerytablet.item.ArtilleryTacticalTabletItem;
import net.nazarick.artillerytablet.item.BoundArtillery;
import net.nazarick.artillerytablet.item.TargetEntry;
import net.nazarick.artillerytablet.network.FireCommandMessage;
import net.nazarick.artillerytablet.network.RequestAmmoOptionsMessage;
import net.nazarick.artillerytablet.network.NearbyArtilleryEntry;
import net.nazarick.artillerytablet.network.RequestNearbyArtilleryMessage;
import net.nazarick.artillerytablet.network.SetTabletSettingsMessage;
import net.nazarick.artillerytablet.network.SetTargetsMessage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The tablet itself: one screen holding the map, the information tabs and the fire controls.
 *
 * <p>Replaces the earlier arrangement of a screen-edge HUD plus separate roster and ammunition
 * screens. Those were the individual capabilities proven one at a time; this is the device they
 * were always meant to be assembled into.
 *
 * <p>The map runs edge to edge and everything else floats over it. Nothing is open by default: the
 * views are called up from the header, the battery and the reference material on the left and the
 * target list on the right, and dismissed again. Only the action box is always there, because what
 * a gun is about to do should never be behind a keypress.
 *
 * <p>What a gun is doing is drawn on the gun rather than in a column of its own — a fire mission
 * has no meaning apart from the piece carrying it out.
 *
 * <p>Sizes are clamped to the available screen so the tablet stays usable at large GUI scales
 * rather than spilling off the edges.
 */
@OnlyIn(Dist.CLIENT)
public class TabletScreen extends Screen {
    // Every measurement below is in Minecraft's interface pixels. See Ui for why the tablet went
    // back to them. Values here were divided by about two and a half on the way, which undoes the
    // multiplication applied when this briefly laid out in device pixels.

    // How much of the window the device takes, and how small it may get, now live in TabletFrame
    // along with the rest of the case. They were here, and leaving them here after the move would
    // have been the very thing that class exists to prevent: two places holding an opinion about
    // where the screen is, agreeing until the first change to one of them.

    private static final int KEY_GAP = 2;

    /**
     * Standard key height.
     *
     * <p>Named rather than written into the calls that use it. The change of units went wrong
     * precisely because sizes like this were literals: every constant with a name was converted and
     * every loose number was missed, so the frame grew and its contents did not.
     */
    private static final int KEY_ROW = 12;

    private static final int ROW = 10;

    /**
     * Width of a view.
     *
     * <p>Narrow on purpose, and narrowed again once the cards lost their own fire key: a column is
     * something laid over the map, and the map is the instrument. Anything the column cannot hold
     * belongs on the detail strip, which has the whole width to say it in.
     */
    private static final int PANEL_WIDTH = 126;

    /** A target card: status line, grid reference, range and bearing, with room for its controls. */
    private static final int CARD_HEIGHT = 30;

    /** One line inside a card. Three of these plus a margin is what CARD_HEIGHT has to hold. */
    private static final int CARD_LINE = 10;

    /** The coordinate entry above the card list. */
    private static final int ENTRY_HEIGHT = 38;

    /** The detail strip along the bottom of the map, shown only when a target is selected. */

    /**
     * The action box: two lines of fire order over FIRE beside a stack of mode and arc.
     *
     * <p>The order reads back what is about to happen, which is what a fire-direction centre does
     * before it says the word. It takes two lines because one line of this column's width forces
     * abbreviations, and abbreviations are what the header was rebuilt to get rid of.
     */
// The rail's measurements went with the rail. Key size and spacing on the case are the case's
    // to state, in TabletFrame, so there is one column of numbers rather than two that agree.

    /**
     * Square keys floated over the map for the view controls.
     *
     * <p>Odd on purpose. An even box has no middle pixel — twelve wide runs from the first pixel to
     * the twelfth and centres between the sixth and the seventh — so a mark drawn from whole pixels
     * always lands half a pixel off. At the scale this is magnified to, half a pixel is visible.
     */
    private static final int MAP_KEY = 13;

    /** Depth of the map's own corner readout, which is now just the scale. */
    private static final int MAP_CORNER_TEXT = 11;


    /**
     * Ticks between roster refreshes. Five is four a second — fast enough to watch a barrel come
     * onto its bearing, slow enough that it is not a packet per frame.
     */
    private static final int ROSTER_REFRESH_TICKS = 5;
    private int rosterAge;

    /** Which gun the detail strip describes, or null when a target is being read instead. */
    private UUID selectedGun;

    /** The gun whose menu is open, and where it was opened, or null for none. */
    private UUID menuGun;
    private int menuX;
    private int menuY;

    /** Entries in a gun's menu. Kept here because the window is sized from it. */
    private static final int MENU_ITEMS = 3;

    /**
     * Which target the controls act on, or -1.
     *
     * <p>Before this, firing always meant the first target in the queue and the detail figures had
     * nothing to describe. Selection is what lets one FIRE key mean something specific, and it is
     * what the reference interface builds its whole layout around.
     */
    private int selected = -1;

    private static final int COLOUR_FRAME = TabletTheme.FRAME;
    private static final int COLOUR_ACCENT = TabletTheme.FRIENDLY;
    private static final int COLOUR_PANEL = TabletTheme.SURFACE;
    private static final int COLOUR_OVERLAY = TabletTheme.OVERLAY;
    private static final int COLOUR_TEXT = TabletTheme.TEXT;
    private static final int COLOUR_MUTED = TabletTheme.MUTED;
    private static final int COLOUR_WARN = TabletTheme.WARNING;
    private static final int COLOUR_GOOD = TabletTheme.GOOD;

    private final InteractionHand hand;

    /**
     * The view currently open, or null for none.
     *
     * <p>Static because the screen is thrown away and built again every time the tablet is opened,
     * which made a view close itself the moment you looked away. It is not on the item either: this
     * is what the operator is looking at, not what the device holds, and putting it in NBT would
     * sync the whole stack across the network every time a panel opened.
     */
    private static TabletTab openTab;
    private List<TargetEntry> targets = new ArrayList<>();
    private FireMode fireMode = FireMode.SINGLE;
    private boolean depressed;

    private MapPanel map;

    /**
     * Read-only access to the tablet's own state for the full-screen apps in
     * {@code client/screen/app} — a different package, so a private field is invisible to them.
     * Every method here already exists for the case's own keys; these just let an app reach the
     * same facts and the same actions instead of duplicating them.
     */
    public List<TargetEntry> targets() {
        return targets;
    }

    public int selectedTarget() {
        return selected;
    }

    public FireMode fireModeValue() {
        return fireMode;
    }

    public boolean depressed() {
        return depressed;
    }

    public MapPanel map() {
        return map;
    }

    /**
     * Which full-screen app owns the glass, and the switch between them — replacing the panel
     * system domain key by domain key as each app is migrated (see the app-framework hand-off
     * plan). While an app's slot has nothing registered yet (or none is active), the screen well
     * shows the map exactly as it always has; only a genuinely active, non-map app hides it.
     */
    private final AppManager appManager = new AppManager(List.of(
            new MapApp(), new FireControlApp(this), new StatusApp(), new LogApp()));



    private UiField xBox;
    private UiField yBox;
    private UiField zBox;

    /** What was typed into the coordinate boxes, and which one had the caret, across a rebuild. */
    private String entryX = "";
    private String entryY;
    private String entryZ = "";
    private int entryFocus = -1;

    private int left;
    private int top;
    private int width0;
    private int height0;

    /** The case the screen sits in, and the only thing that knows where either of them is. */
    private TabletFrame frame;

    /**
     * The chassis art, baked once into a texture — for the whole game session, not per screen
     * open — rather than redrawn every frame.
     *
     * <p>Hundreds of lines of chamfer, collar and stipple geometry cost real time to run, and the
     * shape they produce never changes: it is baked at a fixed 980x630 design resolution and
     * stretched onto whatever rectangle the frame occupies (see {@link TabletChassisPaint#blit}),
     * so window size plays no part in it either. Static and never released, on the same reasoning
     * the user's own prototype uses it for — rebaking on every tablet open (or every resize, which
     * used to also retrigger this) bought nothing, since the pixels never differ, and cost a fresh
     * {@code NativeImage} allocation plus a GPU upload each time.
     */
    private static DynamicTexture chassisTexture;
    static ResourceLocation chassisTextureLocation;

    /**
     * Whether the boot display was up on the last frame.
     *
     * <p>The latch that ends it lives in the map and turns over during a render, so the controls
     * that belong on the glass have to be built again at that moment. Watched rather than polled by
     * whoever needs it, so the rebuild happens once, at the change, and before anything iterates the
     * list it rebuilds.
     */
    private boolean wasBooting = true;

    /**
     * Whether the display is lit.
     *
     * <p>Not whether the tablet is out. Putting it away is what the hotbar key does, and a second
     * control for the same thing would be a second way to lose your place. This is the display
     * itself: the device stays in hand, dark, and comes back with a boot screen the way a display
     * does.
     *
     * <p>Static, like the zoom and the centre and for the same reason — it is a state of the
     * device, not of the screen instance, which is built afresh every time the tablet is opened.
     */
    private static boolean screenOn = true;

    public TabletScreen(ItemStack stack, InteractionHand hand) {
        super(Component.translatable("item.artillerytablet.artillery_tactical_tablet"));
        this.hand = hand;
        readFrom(stack);
    }

    private void readFrom(ItemStack stack) {
        this.targets = new ArrayList<>(ArtilleryTacticalTabletItem.getTargets(stack));
        this.fireMode = ArtilleryTacticalTabletItem.getFireMode(stack);
        this.depressed = ArtilleryTacticalTabletItem.isDepressed(stack);

        this.boundIds.clear();
        for (BoundArtillery bound : ArtilleryTacticalTabletItem.getBoundArtillery(stack)) {
            this.boundIds.add(bound.id);
        }
    }

    /**
     * What the tablet's stored state currently looks like, so the screen can notice the server
     * syncing a change back and re-read it.
     */
    private int stackSignature(ItemStack stack) {
        int hash = ArtilleryTacticalTabletItem.getFireMode(stack).ordinal();
        hash = hash * 31 + (ArtilleryTacticalTabletItem.isDepressed(stack) ? 1 : 0);
        for (TargetEntry target : ArtilleryTacticalTabletItem.getTargets(stack)) {
            hash = hash * 31 + target.x;
            hash = hash * 31 + target.y;
            hash = hash * 31 + target.z;
        }
        for (BoundArtillery bound : ArtilleryTacticalTabletItem.getBoundArtillery(stack)) {
            hash = hash * 31 + bound.id.hashCode();
        }
        return hash;
    }

    private ItemStack liveStack() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return ItemStack.EMPTY;
        }
        return hand == InteractionHand.MAIN_HAND ? mc.player.getMainHandItem() : mc.player.getOffhandItem();
    }

    private boolean mainHand() {
        return hand == InteractionHand.MAIN_HAND;
    }

    private int seenDataVersion = -1;
    private int seenStackSignature;
    private int seenMissionSignature;

    /** Which guns are running what, so a change of either rebuilds the mission keys. */
    private int missionSignature() {
        int hash = 1;
        for (Map.Entry<UUID, FireMissionClientState.Entry> mission : missions()) {
            hash = hash * 31 + mission.getKey().hashCode();
            hash = hash * 31 + mission.getValue().state.ordinal();
        }
        return hash;
    }

    /**
     * Shown immediately when the player binds or unbinds, before the server has synced the item
     * back. Waiting for that round trip made every button feel broken — it appeared to do nothing
     * until the tablet was reopened. Re-read from the item whenever the real state arrives, so an
     * action the server rejects corrects itself rather than lingering as a lie.
     */
    private final Set<UUID> boundIds = new HashSet<>();

    /** Same idea for ammunition, which is answered by a server reply rather than by item NBT. */
    private String pendingAmmoId;

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        // The roster reply carries each gun's laying angles, so asking again on a slow tick is what
        // turns it from a list into a live readout. Pull rather than push, and only while the tablet
        // is open: the cost follows the one player looking at it, not the number online.
        if (++rosterAge >= ROSTER_REFRESH_TICKS) {
            rosterAge = 0;
            ModNetwork.toServer(new RequestNearbyArtilleryMessage(mainHand()));
        }

        // Server replies land after the layout was built, so the rows they describe would otherwise
        // render without their buttons. Rebuild when the data actually changes.
        int version = TabletClientData.version();
        if (version != seenDataVersion) {
            seenDataVersion = version;
            // A fresh ammunition list is authoritative; drop the optimistic choice.
            pendingAmmoId = null;
            rebuild();
        }

        if (map != null) {
            map.tick();
        }

        // Fire missions arrive on their own packet and touch neither the client data version nor the
        // item, so nothing here used to notice them. The cards drew — they are painted every frame —
        // but their Abort key is a widget, and widgets are only built on a rebuild. The key turned up
        // whenever something unrelated happened to trigger one, which for a distant target was often
        // after the gun had already fired: an abort key that arrives too late to abort anything.
        int missionSignature = missionSignature();
        if (missionSignature != seenMissionSignature) {
            seenMissionSignature = missionSignature;
            rebuild();
        }

        // The item changes for reasons outside this screen too — the server confirming a bind,
        // spotting a target by eye, or picking one off the map.
        ItemStack stack = liveStack();
        int signature = stackSignature(stack);
        if (signature != seenStackSignature) {
            seenStackSignature = signature;
            readFrom(stack);
            rebuild();
        }
    }

    public Set<UUID> boundIds() {
        return boundIds;
    }

    void setBoundOptimistically(UUID id, boolean bound) {
        if (bound) {
            boundIds.add(id);
        } else {
            boundIds.remove(id);
        }
    }

    String pendingAmmoId() {
        return pendingAmmoId;
    }

    void setPendingAmmo(String ammoId) {
        this.pendingAmmoId = ammoId;
    }

    @Override
    protected void init() {
        super.init();

        // Asked, not worked out. The case decides where the screen is, because it is the thing with
        // an opinion about it — see TabletFrame on why this is the only place that opinion lives.
        this.frame = TabletFrame.fit(this.width, this.height);
        this.width0 = frame.screenWidth();
        this.height0 = frame.screenHeight();
        this.left = frame.screenLeft();
        this.top = frame.screenTop();
        bakeChassisTexture();

        if (this.map == null) {
            this.map = new MapPanel();
        }

        // The roster is what tells the client where the guns are, and range and bearing are useless
        // without it. Asking on open means the target cards read properly straight away instead of
        // showing dashes until the battery view happens to be visited.
        ModNetwork.toServer(new RequestNearbyArtilleryMessage(mainHand()));

        rebuild();
    }

    private static synchronized void bakeChassisTexture() {
        if (chassisTextureLocation != null && chassisTexture != null) {
            return;
        }
        NativeImage image = TabletChassisPaint.bake();
        chassisTexture = new DynamicTexture(image);
        chassisTexture.upload();
        chassisTextureLocation = Minecraft.getInstance().getTextureManager()
                .register("artillerytablet_chassis", chassisTexture);
    }

    /** The tablet's own controls, laid out and hit-tested in device pixels. */
    private final List<UiButton> keys = new ArrayList<>();
    private final List<UiField> fields = new ArrayList<>();

    UiButton addKey(UiButton key) {
        keys.add(key);
        return key;
    }

    private void rebuild() {
        // The coordinate boxes are destroyed and remade by every rebuild, and the roster now asks
        // again four times a second — so anything typed, and the caret with it, was being wiped
        // before it could be finished. What was in them is kept here and put back below.
        if (xBox != null) {
            entryX = xBox.value();
            entryY = yBox.value();
            entryZ = zBox.value();
            entryFocus = xBox.focused() ? 0 : yBox.focused() ? 1 : zBox.focused() ? 2 : -1;
        }

        this.clearWidgets();
        keys.clear();
        fields.clear();

        if (selected >= targets.size()) {
            selected = -1;
        }

        buildFrameKeys();
        if (!screenOn || (map != null && map.booting())) {
            // Nothing on the glass while it is dark or still coming up. Skipping the drawing alone
            // would leave keys that cannot be seen and can still be clicked, which is worse than
            // either state on its own.
            return;
        }
        buildMapControls();

        if (openTab != null) {
            buildPanel();
        }
        if (menuGun != null) {
            buildGunMenu();
        }
    }

    /**
     * The rail: one key per view, marked by a shape rather than a two-letter code.
     *
     * <p>A rail of abbreviations was built once and thrown out for being unreadable. What makes this
     * one different is that the keys carry drawn marks with the full name on the tooltip, and the
     * open one is filled rather than merely underlined.
     */
    /**
     * The keys standing on the case, either side of the screen.
     *
     * <p>Every one of them reaches something the software rail already reaches, and that repetition
     * is the design rather than an oversight: on the equipment this is drawn after, the keys beside
     * the screen are hard routes to what is on it. Two ways to the same function, not two functions.
     *
     * <p>Which is why the red key is the one that fires and none of the numbered ones do. Firing
     * deserves a key that cannot be hit by aiming at its neighbour, and three routes to a gun going
     * off is two more than anyone needs. It carries the same refusal as the rail's own FIRE — no
     * target selected, no arming — because a second door into a room does not get a weaker lock.
     *
     * <p>The keys with nothing behind them yet are built disabled rather than left out. A gap in a
     * column of keys reads as a broken case; a key drawn dim reads as what it is, and it says in its
     * tooltip what it is waiting for.
     */
    private void buildFrameKeys() {
        if (!frame.present()) {
            return;
        }
        int kw = frame.keyW();
        int kh = frame.keyH();
        boolean armed = !targets.isEmpty() && selected >= 0 && selected < targets.size();

        // ---- top edge: grid, five real views spread among three not yet wired, and brightness --
        //
        // The new case has eight domain-labelled slots where the old one had five tab keys and two
        // spares — SA/WPN/SYS/COM/BMS carry TARGETS/AMMO/STATUS/LOG/BATTERY exactly as the five tab
        // keys always did; DEF/DRV/STR are not yet behind anything, same as any other spare.
        top(0, kw, kh, Component.translatable("gui.artillerytablet.frame.grid"),
                () -> map.toggleGrid(), map.gridShown(),
                Component.translatable("gui.artillerytablet.frame.grid.tip"))
                .mark(UiButton.Mark.GRID);
        top(1, kw, kh, Component.literal("SA"), () -> appManager.switchTo(MapApp.ID),
                appManager.isActive(MapApp.ID), Component.translatable("gui.artillerytablet.app.map"));
        top(2, kw, kh, Component.literal("WPN"), () -> appManager.switchTo(FireControlApp.ID),
                appManager.isActive(FireControlApp.ID), Component.literal("Fire Control"));
        spareTop(3, kw, kh, "DEF");
        top(4, kw, kh, Component.literal(TabletTab.STATUS.code), () -> toggleTab(TabletTab.STATUS),
                openTab == TabletTab.STATUS, Component.translatable(TabletTab.STATUS.titleKey));
        spareTop(5, kw, kh, "DRV");
        spareTop(6, kw, kh, "STR");
        top(7, kw, kh, Component.literal(TabletTab.LOG.code), () -> toggleTab(TabletTab.LOG),
                openTab == TabletTab.LOG, Component.translatable(TabletTab.LOG.titleKey));
        top(8, kw, kh, Component.literal(TabletTab.BATTERY.code), () -> toggleTab(TabletTab.BATTERY),
                openTab == TabletTab.BATTERY, Component.translatable(TabletTab.BATTERY.titleKey));
        top(9, kw, kh, Component.translatable("gui.artillerytablet.frame.brightness_up"),
                TabletDisplay::cycleBrightness, false,
                Component.translatable("gui.artillerytablet.frame.brightness.tip",
                        TabletDisplay.percent()))
                .mark(UiButton.Mark.BRIGHT);

        // ---- bottom edge: the filter, spares, and the way out ----------------------------------
        bottom(0, kw, kh, Component.translatable("gui.artillerytablet.frame.filter"),
                TabletDisplay::cycleFilter, TabletDisplay.filtered(),
                Component.translatable("gui.artillerytablet.frame.filter.tip",
                        Component.translatable(TabletDisplay.filterName())))
                .mark(UiButton.Mark.NIGHT);
        for (int i = 1; i <= 8; i++) {
            spare(TabletFrame.EDGE_BOTTOM, i, kw, kh, 12 + i);
        }
        int[] pwr = frame.rowKey(true, TabletFrame.ROW_KEYS - 1);
        addKey(new UiButton(pwr[0], pwr[1], frame.rowKeyW(), frame.rowKeyBottomH(),
                Component.translatable("gui.artillerytablet.frame.power"),
                () -> {
                    screenOn = !screenOn;
                    if (screenOn) {
                        MapPanel.restartBoot();
                    }
                    rebuild();
                })
                .hard(!screenOn)
                .power()
                .mark(UiButton.Mark.POWER)
                .lamp(frame.ledFor(TabletFrame.EDGE_BOTTOM, TabletFrame.ROW_KEYS - 1))
                .tooltip(Component.translatable("gui.artillerytablet.frame.power.tip")));

        // ---- left edge: laying the guns --------------------------------------------------------
        side(false, 0, kw, kh, Component.translatable("gui.artillerytablet.frame.cff"),
                () -> fire(selected >= 0 ? selected : 0), false,
                Component.translatable("gui.artillerytablet.frame.cff.tip")).danger().active(armed);
        side(false, 1, kw, kh, Component.translatable("gui.artillerytablet.key.adjust"),
                () -> lay(selected), armed && !boundIds.isEmpty(),
                Component.translatable("gui.artillerytablet.key.adjust.tip"))
                .active(armed && !boundIds.isEmpty());
        side(false, 2, kw, kh, Component.translatable("gui.artillerytablet.key.mode"),
                this::cycleFireMode, true, fireModeLabel());
        side(false, 3, kw, kh, Component.translatable("gui.artillerytablet.key.arc"),
                this::toggleTrajectory, true, trajectoryLabel());
        spare(TabletFrame.EDGE_LEFT, 4, kw, kh, 5);
        spare(TabletFrame.EDGE_LEFT, 5, kw, kh, 6);

        // ---- right edge: held for what comes next ----------------------------------------------
        for (int i = 0; i < TabletFrame.KEYS; i++) {
            spare(TabletFrame.EDGE_RIGHT, i, kw, kh, 7 + i);
        }
    }


    private UiButton top(int index, int kw, int kh, Component label, Runnable action,
                         boolean on, Component tip) {
        return top(index, kw, kh, label, action, on, tip, false);
    }

    /**
     * A key on the top edge. {@code wide} is kept for callers that still ask, but every cap on the
     * case is the same size now — see {@link TabletFrame#rowKeyWide()}.
     */
    private UiButton top(int index, int kw, int kh, Component label, Runnable action,
                         boolean on, Component tip, boolean wide) {
        int[] at = frame.rowKey(false, index, wide);
        int w = wide ? frame.rowKeyWide() : frame.rowKeyW();
        return addKey(new UiButton(at[0], at[1], w, frame.rowKeyH(),
                label, () -> {
            action.run();
            rebuild();
        })
                .hard(on)
                .lamp(frame.ledFor(TabletFrame.EDGE_TOP, index, wide))
                .tooltip(tip));
    }

    private UiButton bottom(int index, int kw, int kh, Component label, Runnable action,
                            boolean on, Component tip) {
        return bottom(index, kw, kh, label, action, on, tip, false);
    }

    private UiButton bottom(int index, int kw, int kh, Component label, Runnable action,
                            boolean on, Component tip, boolean wide) {
        int[] at = frame.rowKey(true, index, wide);
        int w = wide ? frame.rowKeyWide() : frame.rowKeyW();
        return addKey(new UiButton(at[0], at[1], w, frame.rowKeyBottomH(),
                label, () -> {
            action.run();
            rebuild();
        })
                .hard(on)
                .lamp(frame.ledFor(TabletFrame.EDGE_BOTTOM, index, wide))
                .tooltip(tip));
    }

    private UiButton side(boolean right, int index, int kw, int kh, Component label,
                          Runnable action, boolean on, Component tip) {
        int edge = right ? TabletFrame.EDGE_RIGHT : TabletFrame.EDGE_LEFT;
        return addKey(new UiButton(frame.keyX(right), frame.keyY(index), kw, kh, label, () -> {
            action.run();
            rebuild();
        })
                .hard(on)
                .lamp(frame.ledFor(edge, index))
                .tooltip(tip));
    }

    /**
     * A numbered position with nothing behind it yet.
     *
     * <p>Not a key that fails: a key that has not been fitted. The panel this is drawn after ships
     * with twenty of these, and they are numbered rather than named for the same reason — a
     * position with a number is a place to put something, where a position with a name and no
     * function is a claim that turns out to be untrue. It clicks like the rest, because the cap is
     * as real as any other, and its lamp stays dark because nothing is behind it to light.
     */
    private void spare(int edge, int index, int kw, int kh, int number) {
        int[] at;
        if (edge == TabletFrame.EDGE_TOP) {
            at = frame.rowKey(false, index);
        } else if (edge == TabletFrame.EDGE_BOTTOM) {
            at = frame.rowKey(true, index);
        } else if (edge == TabletFrame.EDGE_LEFT) {
            at = new int[]{frame.keyX(false), frame.keyY(index)};
        } else {
            at = new int[]{frame.keyX(true), frame.keyY(index)};
        }
        boolean row = edge == TabletFrame.EDGE_TOP || edge == TabletFrame.EDGE_BOTTOM;
        int w = row ? frame.rowKeyW() : kw;
        int h = !row ? kh
                : edge == TabletFrame.EDGE_BOTTOM ? frame.rowKeyBottomH() : frame.rowKeyH();
        addKey(new UiButton(at[0], at[1], w, h, Component.literal("F" + number), () -> { })
                .hard(false)
                .active(false)
                .lamp(frame.ledFor(edge, index))
                .tooltip(Component.translatable("gui.artillerytablet.frame.spare", number)));
    }

    /**
     * A top-row position with nothing behind it yet, named rather than numbered.
     *
     * <p>The eight domain slots along the top read as one family, so a spare among them keeps that
     * family's three-letter-code look instead of falling back to the case's own F-numbering, which
     * exists for the plain square blocks on the other three edges.
     */
    private void spareTop(int index, int kw, int kh, String label) {
        int[] at = frame.rowKey(false, index);
        addKey(new UiButton(at[0], at[1], frame.rowKeyW(), frame.rowKeyH(),
                Component.literal(label), () -> { })
                .hard(false)
                .active(false)
                .lamp(frame.ledFor(TabletFrame.EDGE_TOP, index))
                .tooltip(Component.translatable("gui.artillerytablet.frame.spare", label)));
    }

    // There is no lettering on the case any more. The strip above the glass carries the top row of
    // keys and nothing else on the panel this is drawn after, and a name plate set behind them was
    // a second thing claiming the same band — it survived only because the band used to be tall
    // enough for both. What the device is called is on the boot display, which is where the eye
    // already goes to find out whether it has come up.

    // The helper that drew a key with nothing behind it is gone with the last key that needed one.
    // Every control on this case now reaches something real.



    /**
     * How far each standing column has been scrolled.
     *
     * <p>Both lists outgrew the screen the moment they became permanent: eight targets of three
     * lines each is more than a column holds, and saying "+3 more" is only honest, not useful.
     */
    private int leftScroll;
    private int rightScroll;

    private int scrollLimit(int contentHeight, int viewHeight) {
        return Math.max(0, contentHeight - viewHeight);
    }

    /** Height the battery list wants, so its scroll can be bounded to real content. */
    private int batteryContentHeight() {
        return TabletPanels.batteryRows() * TabletPanels.rowPitch();
    }

    private int targetContentHeight() {
        return targets.size() * (CARD_HEIGHT + Ui.GAP_XS);
    }


    /** Missions worth showing, newest state first read straight from the client mirror. */
    private List<Map.Entry<UUID, FireMissionClientState.Entry>> missions() {
        if (this.minecraft == null || this.minecraft.level == null) {
            return List.of();
        }
        return new ArrayList<>(FireMissionClientState.active(this.minecraft.level.getGameTime()).entrySet());
    }

    private UiButton key(int x, int y, int w, int h, Component label, Component tip, Runnable action) {
        return addKey(new UiButton(x, y, w, h, label, action).tooltip(tip));
    }


    /** View controls, floated over the map's own bottom right rather than costing a bezel. */
    private void buildMapControls() {
        int[] a = mapArea();
        int x = a[0] + a[2] - Ui.GAP_SM - MAP_KEY;
        // Above the corner readout rather than on top of it: scale and terrain stack in this corner
        // and the keys were landing across them.
        //
        // Zoom stays on the glass and centring does not, which is not an inconsistency. Zoom is
        // read against the scale written directly beneath it — the number and the keys that change
        // it are one instrument. Centring is a single act with a key of its own on the case, and
        // having it in both places was the duplication the case was meant to end.
        int y = bodyBottom() - MAP_CORNER_TEXT - MAP_KEY;

        UiButton centre = key(x, y, MAP_KEY, MAP_KEY, Component.empty(),
                Component.translatable("gui.artillerytablet.recentre"),
                () -> {
                    map.recentreOnPlayer();
                    rebuild();
                }).mark(UiButton.Mark.CENTRE);
        centre.active(map != null && !map.followsPlayer());
        y -= MAP_KEY + Ui.GAP_XS;

        key(x, y, MAP_KEY, MAP_KEY, Component.empty(),
                Component.translatable("gui.artillerytablet.key.zoom_out"), () -> zoom(-1))
                .mark(UiButton.Mark.MINUS);
        y -= MAP_KEY + Ui.GAP_XS;

        key(x, y, MAP_KEY, MAP_KEY, Component.empty(),
                Component.translatable("gui.artillerytablet.key.zoom_in"), () -> zoom(1))
                .mark(UiButton.Mark.PLUS);
    }

    /** How far the map reaches, written under the keys that change it rather than in a far corner. */
    private void renderMapScale(GuiGraphics g) {
        int[] a = mapArea();
        String scale = map.spanLabel();
        int right = a[0] + a[2] - Ui.GAP_SM;
        TabletTheme.draw(g, font, scale, right - Ui.width(scale),
                bodyBottom() - MAP_CORNER_TEXT + Ui.GAP_XS, COLOUR_MUTED, true);
    }

    /**
     * The target queue, one card per target, with its own controls.
     *
     * <p>It used to be a strip of rows across the bottom that could only say a coordinate and a
     * colour. A card has room to say why a target cannot be serviced — inside minimum range, beyond
     * maximum, ground in the way at some distance — which is the part the player can act on.
     */
    private void buildTargetCards(int x, int y, int height) {
        int rowY = y + Ui.GAP_SM;
        for (int i = 0; i < targets.size(); i++) {
            if (rowY + CARD_HEIGHT > y + height) {
                break;
            }
            int index = i;

            // The card body selects; the keys act. A whole-card hit area means selecting does not
            // require aiming at anything in particular.
            UiButton pick = new UiButton(x, rowY, contentWidth() - 12, CARD_HEIGHT, Component.empty(), () -> select(index));
            addKey(pick.invisible());

            // No Fire key of its own. FIRE in the action box names the selected target, so a second
            // one per card was the same act said twice — and it was the thing overrunning the range
            // line, because three lines of figures and two keys do not fit a column this narrow.
            addKey(new UiButton(x + contentWidth() - 11, rowY + Ui.GAP_XS, 11, 11,
                    Component.literal("x"), () -> removeTarget(index)));

            rowY += CARD_HEIGHT + Ui.GAP_XS;
        }
    }

    /**
     * A nav entry opens its view over the map, and pressing the same one again closes it. Nothing is
     * open by default, so the map is whole until the player asks for something else.
     */
    private void toggleTab(TabletTab next) {
        if (openTab == next) {
            openTab = null;
            rebuild();
            return;
        }

        openTab = next;
        menuGun = null;
        if (next == TabletTab.BATTERY) {
            TabletClientData.clearRoster();
            ModNetwork.toServer(new RequestNearbyArtilleryMessage(mainHand()));
        } else if (next == TabletTab.AMMO) {
            TabletClientData.clearAmmo();
            ModNetwork.toServer(new RequestAmmoOptionsMessage(mainHand()));
        }
        rebuild();
    }

    public void select(int index) {
        this.selected = index >= 0 && index < targets.size() ? index : -1;
        if (this.selected >= 0) {
            this.selectedGun = null;
        }
        rebuild();
    }

    private void zoom(int delta) {
        if (map != null) {
            map.zoomBy(delta);
            rebuild();
        }
    }

    /**
     * Height of the band a panel's title sits in.
     *
     * <p>Not a scaled-down version of what it was: its floor is a line of text plus a little air, so
     * dividing it with the rest of the layout would have tucked the title under its own content.
     */
    private static final int PANEL_TITLE = 18;

    /** Inset from a panel's edge to its content. Read by the builder and the renderer alike. */
    private static final int PANEL_PAD = Ui.GAP_SM;

    private void buildPanel() {
        int[] p = panelArea();
        // The same origin the renderer uses. These two drifted apart once — the buttons were built
        // from the panel top while the labels were drawn below the title — and every row in the
        // roster and ammunition lists sat a title's height away from its own button.
        int contentX = p[0] + PANEL_PAD;
        int contentY = p[1] + PANEL_TITLE;
        int contentH = p[3] - PANEL_TITLE;

        buildCloseKey(p, () -> {
            openTab = null;
            rebuild();
        });

        switch (openTab) {
            case BATTERY -> TabletPanels.buildBattery(this, contentX, contentY - leftScroll, contentWidth(), contentH);
            case TARGETS -> {
                buildTargetsControls(contentX, contentY);
                buildTargetCards(contentX, contentY + ENTRY_HEIGHT - rightScroll,
                        contentH - ENTRY_HEIGHT + rightScroll);
            }
            case AMMO -> TabletPanels.buildAmmo(this, contentX, contentY, contentWidth(), contentH);
            default -> {
                // Status and log are read-only views; nothing to build.
            }
        }
    }

    private void buildTargetsControls(int x, int y) {
        // x already has the panel's padding taken off it. Adding another gap here shifted the whole
        // row right and pushed the key past the edge — the padding was being applied twice.
        int w = contentWidth();
        int boxW = (w - Ui.GAP_SM * 2) / 3;
        int step = boxW + Ui.GAP_SM;

        this.xBox = new UiField(x, y + Ui.GAP_MD, boxW, KEY_ROW);
        this.yBox = new UiField(x + step, y + Ui.GAP_MD, boxW, KEY_ROW);
        this.zBox = new UiField(x + step * 2, y + Ui.GAP_MD, boxW, KEY_ROW);
        fields.add(xBox);
        fields.add(yBox);
        fields.add(zBox);

        // The player's own height is only a starting suggestion, so it fills the box the first time
        // the view is opened and never again — otherwise it would overwrite a typed height on every
        // rebuild, which is to say several times a second.
        if (entryY == null) {
            entryY = this.minecraft != null && this.minecraft.player != null
                    ? String.valueOf(this.minecraft.player.getBlockY()) : "";
        }
        xBox.setValue(entryX);
        yBox.setValue(entryY);
        zBox.setValue(entryZ);
        if (entryFocus >= 0) {
            fields.get(entryFocus).setFocused(true);
        }

        addKey(new UiButton(x, y + Ui.GAP_MD + KEY_ROW + Ui.GAP_SM, w, KEY_ROW,
                Component.translatable("gui.artillerytablet.add_target"), this::addTyped));
        // Fire mode, trajectory and firing itself moved to the right bezel — they are the controls
        // used while looking at the map, so they should not need a panel open to reach.
    }

    public Component fireModeLabel() {
        return Component.translatable("gui.artillerytablet.fire_mode_prefix").append(fireMode.name());
    }

    public Component trajectoryLabel() {
        return Component.translatable("gui.artillerytablet.trajectory_prefix").append(
                Component.translatable(depressed
                        ? "gui.artillerytablet.trajectory_depressed"
                        : "gui.artillerytablet.trajectory_lofted"));
    }

    public void cycleFireMode() {
        FireMode[] modes = FireMode.values();
        this.fireMode = modes[(fireMode.ordinal() + 1) % modes.length];
        pushSettings();
        rebuild();
    }

    public void toggleTrajectory() {
        this.depressed = !this.depressed;
        pushSettings();
        rebuild();
    }

    private void addTyped() {
        Integer x = parse(xBox.value());
        Integer y = parse(yBox.value());
        Integer z = parse(zBox.value());
        if (x == null || y == null || z == null || targets.size() >= ArtilleryTacticalTabletItem.MAX_TARGETS) {
            return;
        }
        targets.add(new TargetEntry(x, y, z));
        pushTargets();
        rebuild();
    }

    void removeTarget(int index) {
        if (index < 0 || index >= targets.size()) {
            return;
        }
        targets.remove(index);

        // Deleting what was selected clears the selection rather than sliding it onto a neighbour.
        // Sliding was the first attempt and it was wrong: the fire key silently came to mean a
        // different grid reference than the one the player had been looking at.
        if (selected == index) {
            selected = -1;
        } else if (selected > index) {
            selected--;
        }

        pushTargets();
        rebuild();
    }

    public void fire(int index) {
        fire(index, fireMode, fireMode.name());
    }

    /**
     * Sends one gun's worth of fire at a target, whatever the battery's standing mode says.
     *
     * <p>This is what adjustment is: a single ranging round, watched, and the aim moved before the
     * battery is committed. It needs no mechanism of its own — the fire command already carries the
     * mode, so overriding it to SINGLE is the whole of it.
     */
    /**
     * Orders every gun onto the selected target and leaves them there.
     *
     * <p>Not a ranging round — the point is to have the battery already pointing at the right ground
     * when FFE is pressed, so the rounds leave together instead of trickling out as each barrel
     * finishes traversing. Firing does this laying itself; this is the half of it worth having on
     * its own key.
     */
    public void lay(int index) {
        if (index < 0 || index >= targets.size()) {
            return;
        }
        pushTargets();
        pushSettings();
        ModNetwork.toServer(new FireCommandMessage(index, fireMode, mainHand(), true));

        TargetEntry t = targets.get(index);
        FireLog.record("LAY -> " + t.x + " " + t.y + " " + t.z);
    }

    private void fire(int index, FireMode mode, String logAs) {
        if (index < 0 || index >= targets.size()) {
            return;
        }
        pushTargets();
        pushSettings();
        ModNetwork.toServer(new FireCommandMessage(index, mode, mainHand()));

        TargetEntry t = targets.get(index);
        FireLog.record(logAs + " -> " + t.x + " " + t.y + " " + t.z);
    }

    private void pushTargets() {
        ModNetwork.toServer(new SetTargetsMessage(mainHand(), targets));
    }

    private void pushSettings() {
        ModNetwork.toServer(new SetTabletSettingsMessage(
                mainHand(), fireMode, depressed,
                xBox == null ? "" : xBox.value(),
                yBox == null ? "" : yBox.value(),
                zBox == null ? "" : zBox.value()));
    }

    private static Integer parse(String value) {
        if (value == null || value.isEmpty() || value.equals("-")) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // No `removed()` override. It used to free the map's sheets when the screen went away, which
    // meant every open began from an empty cache and filled the panel in a piece at a time — the
    // sheets belong to the world now, not to the screen, and are freed when the world changes. The
    // chassis texture doesn't need a `removed()` either, for the opposite reason: it's static and
    // baked once for the whole session (see bakeChassisTexture above), so there is nothing this
    // screen should release when it closes — the next tablet open reuses the same GPU texture.

    @Override
    public void onClose() {
        pushTargets();
        pushSettings();
        super.onClose();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);

        renderDevice(g, mouseX, mouseY);

        for (UiButton key : keys) {
            if (key.tooltip() != null && key.contains(mouseX, mouseY)) {
                g.renderTooltip(this.font, TabletTheme.text(key.tooltip()), mouseX, mouseY);
                break;
            }
        }
    }

    /**
     * Whether the app currently on the glass is the map (or none is registered yet). The one test
     * both the render path and the input-routing paths share, so they can never disagree about
     * which one owns the screen well this frame.
     */
    private boolean mapAppActive() {
        var activeApp = appManager.active();
        return activeApp == null || activeApp.id().equals(MapApp.ID);
    }

    private void renderDevice(GuiGraphics g, double px, double py) {
        int[] area = mapArea();

        // The case goes down before anything on it. Drawn afterwards it would need to know where
        // the screen ends so as not to cover it, which is the screen's business, not the case's.
        if (chassisTextureLocation != null) {
            TabletChassisPaint.blit(g, frame, chassisTextureLocation);
        } else {
            frame.draw(new GuiPaint(g));
        }

        if (!screenOn) {
            // Off, and drawn rather than left out. The case and its keys are still there — that is
            // the whole point of the display being a thing that switches rather than the tablet
            // being a thing that closes — so what goes here is a dark panel, not an absence.
            g.fill(left, top, left + width0, top + height0, 0xFF04060A);
            g.fill(left, top, left + width0, top + 1, 0xFF0B1016);
            // And then the controls, which is the whole point and was the bug: the keys were built
            // while the display was off and never drawn, because the loop that draws them sits at
            // the end of this method and this branch left before reaching it. A case whose keys
            // vanish with its picture is not a case with a switch on it — it is a case that
            // disappeared.
            renderControls(g, px, py);
            return;
        }

        // Everything from here to the end of the glass goes through the display's own transfer —
        // brightness and the night filter. Cleared before the controls, because the keys are on the
        // case and a case does not dim.
        TabletDisplay.apply(g);

        boolean booting = map != null && map.booting();

        g.fill(left, top, left + width0, top + height0, COLOUR_FRAME);

        boolean showMap = mapAppActive();

        if (showMap) {
            // Tell the map what it must leave clear before it draws, rather than covering it
            // afterwards.
            map.reserve(openTab == null ? null : panelArea(),
                    menuGun == null ? null : menuArea());

            g.fill(area[0], area[1], area[0] + area[2], area[1] + area[3], COLOUR_PANEL);
            map.render(g, area[0], area[1], area[2], area[3]);
            renderMapMarkers(g, area[0], area[1], area[2], area[3]);

            if (!booting) {
                // Not drawn rather than drawn and covered. Lettering is batched under a render type
                // of its own and reaches the screen after every filled quad however it was
                // submitted, so the boot display's backdrop cannot get on top of it — which is
                // exactly how the scale and the graticule's numbers came to be legible through a
                // screen that means "not ready". Not drawing a label is the one behaviour no
                // rendering detail can undo.
                renderMapScale(g);
            }

            if (openTab != null) {
                renderPanel(g);
            }
            if (menuGun != null) {
                renderGunMenu(g);
            }
        } else if (!booting) {
            g.fill(area[0], area[1], area[0] + area[2], area[1] + area[3], COLOUR_PANEL);
            g.pose().pushPose();
            g.pose().translate(area[0], area[1], 0);
            appManager.render(g, (int) (px - area[0]), (int) (py - area[1]), 0f, area[2], area[3]);
            g.pose().popPose();
        }


        if (booting) {
            // Last thing on the glass, so it covers every part of the map layer rather than a
            // rectangle in the middle of it. The map still renders underneath — that render is what
            // asks for ground and bakes the pictures, so skipping it would leave the device unable
            // to finish coming up — but none of it is meant to be seen yet, the scale and the
            // header least of all: they are the map's own furniture standing on top of a screen
            // whose entire message is that the map is not there.
            BootSplash.draw(g, left, top, width0, height0);
        }

        // The latch turns over inside the map's render, so the moment it does, the controls that
        // live on the glass have to be built. Before the drawing below, because rebuild() empties
        // the very list that drawing walks.
        if (booting != wasBooting) {
            wasBooting = booting;
            rebuild();
        }

        maskWellCorners(g, area[0], area[1], area[2], area[3]);

        TabletDisplay.clear(g);
        renderControls(g, px, py);
    }

    /**
     * Restores the well's rounded corners after square content has been drawn into it.
     *
     * <p>The chassis bakes a rounded bezel around the well (see {@link TabletChassisPaint}), but
     * the map, every app and the boot splash all fill or draw a plain rectangle over it — the
     * simplest thing any of those draw paths can target. Painting the bezel's own dark tone back
     * over the four corner wedges a rounded rect would have excluded is far cheaper than teaching
     * every one of them to clip to a curve, and reads identically: the well looks rounded because
     * the last thing drawn into it says so.
     */
    private void maskWellCorners(GuiGraphics g, int x, int y, int w, int h) {
        int r = frame.toScreenW(10);
        if (r <= 0) {
            return;
        }
        // Matches TabletChassisPaint's own four corner tones for the same bezel band (darkest at
        // top-left, lightest at bottom-right) rather than one flat colour, so the patch disappears
        // into the baked chassis instead of standing out as a slightly different grey.
        maskCorner(g, x, y, r, false, false, 0xFF070809);
        maskCorner(g, x + w - r, y, r, true, false, 0xFF1A1C1F);
        maskCorner(g, x, y + h - r, r, false, true, 0xFF1A1C1F);
        maskCorner(g, x + w - r, y + h - r, r, true, true, 0xFF24262A);
    }

    private void maskCorner(GuiGraphics g, int x, int y, int r, boolean right, boolean bottom, int colour) {
        for (int cy = 0; cy < r; cy++) {
            for (int cx = 0; cx < r; cx++) {
                int dx = right ? cx : r - 1 - cx;
                int dy = bottom ? cy : r - 1 - cy;
                if (dx * dx + dy * dy > r * r) {
                    g.fill(x + cx, y + cy, x + cx + 1, y + cy + 1, colour);
                }
            }
        }
    }

    /**
     * Every control the device is currently carrying.
     *
     * <p>One place, called from both the lit path and the dark one. Written out twice, the day
     * would come when a control was added to one and not the other — which is exactly how the keys
     * came to disappear along with the picture.
     */
    private void renderControls(GuiGraphics g, double px, double py) {
        for (UiField field : fields) {
            field.render(g, px, py);
        }
        for (UiButton key : keys) {
            key.render(g, px, py, mouseDown);
        }
    }


    private int missionColour(MissionState state) {
        return switch (state) {
            case IN_FLIGHT -> COLOUR_GOOD;
            case ABORTED -> COLOUR_WARN;
            default -> COLOUR_ACCENT;
        };
    }

    /** Short name of a bound gun, falling back to the type recorded when it was bound. */
    private String gunLabel(UUID gunId) {
        for (BoundArtillery bound : ArtilleryTacticalTabletItem.getBoundArtillery(liveStack())) {
            if (bound.id.equals(gunId)) {
                return ArtilleryLabel.shorten(bound.typeId);
            }
        }
        return "—";
    }
    // The strip along the foot of the map is gone, and its renderers with it. It restated what the
    // target and battery views already say, in less room, over the one part of the display that is
    // the instrument — and an empty band across the bottom of a map is worse than no band at all.


    /**
     * The open view, drawn over the map rather than beside it. The backing is opaque so text stays
     * readable, but it covers only part of the map and closes with the same key that opened it.
     */
    /**
     * The cross beside a panel's title.
     *
     * <p>Pressing the view's own header entry closes it too, but that is a thing you have to be
     * told. A cross is not.
     */
    private void buildCloseKey(int[] p, Runnable onClose) {
        key(p[0] + p[2] - PANEL_PAD - MAP_KEY, p[1] + Ui.GAP_SM, MAP_KEY, MAP_KEY, Component.empty(),
                Component.translatable("gui.artillerytablet.key.close"), onClose)
                .mark(UiButton.Mark.MINUS);
    }

    /**
     * Opens a gun's menu at the pointer, or shuts the one already open.
     *
     * <p>Reached by right-clicking a gun's card, or by its dots — the right button alone would be a
     * thing nobody is told about, and a mark that can be left-clicked says the same without needing
     * to be explained.
     */
    void openGunMenu(UUID gun, int atX, int atY) {
        this.menuGun = gun.equals(menuGun) ? null : gun;
        this.menuX = atX;
        this.menuY = atY;
        rebuild();
    }

    private void buildGunMenu() {
        int[] m = menuArea();
        UUID gun = menuGun;
        int y = m[1] + PANEL_TITLE;

        menuItem(m, y, "gui.artillerytablet.menu.ammo", () -> {
            openTab = TabletTab.AMMO;
            menuGun = null;
            TabletClientData.clearAmmo();
            ModNetwork.toServer(new RequestAmmoOptionsMessage(mainHand()));
            rebuild();
        });
        menuItem(m, y + KEY_ROW, "gui.artillerytablet.menu.abort", () -> {
            ModNetwork.toServer(new AbortMissionMessage(mainHand(), gun));
            menuGun = null;
            rebuild();
        });
        menuItem(m, y + KEY_ROW * 2, "gui.artillerytablet.menu.unbind", () -> {
            ModNetwork.toServer(new BindArtilleryMessage(gun, false, mainHand()));
            boundIds.remove(gun);
            menuGun = null;
            rebuild();
        });
    }

    /**
     * The gun whose card is under the pointer, or null.
     *
     * <p>Worked out from the same row pitch the panel lays cards out with, so the two cannot drift
     * apart — the recurring fault in this file has been two places computing one position.
     */
    private UUID gunCardAt(double mx, double my) {
        if (openTab != TabletTab.BATTERY || !within(mx, my, panelArea())) {
            return null;
        }
        List<NearbyArtilleryEntry> roster = TabletClientData.roster();
        if (roster == null) {
            return null;
        }
        int[] p = panelArea();
        int top = p[1] + PANEL_TITLE + Ui.GAP_MD - leftScroll;
        int index = (int) ((my - top) / TabletPanels.rowPitch());
        return index >= 0 && index < roster.size() ? roster.get(index).id : null;
    }

    private void menuItem(int[] m, int y, String key, Runnable action) {
        addKey(new UiButton(m[0] + Ui.GAP_XS, y, m[2] - Ui.GAP_SM, KEY_ROW,
                Component.translatable(key), action).asMenuItem());
    }

    private void renderGunMenu(GuiGraphics g) {
        int[] m = menuArea();
        g.fill(m[0], m[1], m[0] + m[2], m[1] + m[3], COLOUR_OVERLAY);
        outline(g, m[0], m[1], m[2], m[3]);

        String name = "";
        List<NearbyArtilleryEntry> roster = TabletClientData.roster();
        if (roster != null) {
            for (NearbyArtilleryEntry entry : roster) {
                if (entry.id.equals(menuGun)) {
                    name = entry.label;
                    break;
                }
            }
        }
        TabletTheme.draw(g, font, name, m[0] + Ui.GAP_SM, m[1] + Ui.GAP_MD, COLOUR_ACCENT, false);
    }

    /** The surround every panel shares: ground, an accent rule along the top, and its name. */
    private void panelFrame(GuiGraphics g, int[] p, Component title) {
        g.fill(p[0], p[1], p[0] + p[2], p[1] + p[3], COLOUR_OVERLAY);
        g.fill(p[0], p[1], p[0] + p[2], p[1] + 1, COLOUR_ACCENT);
        TabletTheme.draw(g, font, title, p[0] + PANEL_PAD, p[1] + Ui.GAP_MD, COLOUR_ACCENT, false);
    }

    /**
     * Ground for the right-hand rail.
     *
     * <p>The gun count, mode and arc used to be written across the top of this box. A column two
     * dozen pixels wide has nowhere to put them, and it did not need to: the header already carries
     * all three, and the keys' own tooltips say what pressing them will select.
     */


    private void renderPanel(GuiGraphics g) {
        int[] p = panelArea();
        panelFrame(g, p, Component.translatable(openTab.titleKey));

        int x = p[0] + PANEL_PAD;
        int y = p[1] + PANEL_TITLE;
        Ui.scissor(g, p[0], y, p[2], p[1] + p[3] - y);
        switch (openTab) {
            case BATTERY -> TabletPanels.renderBattery(this, g, x, y - leftScroll);
            case TARGETS -> renderTargetCards(g, x, y + ENTRY_HEIGHT - rightScroll,
                    p[1] + p[3] - y - ENTRY_HEIGHT + rightScroll);
            // No caption over the coordinate boxes: the panel is called Targets and the key below
            // says Add Target, so a third label saying the same was only taking a line.
            case AMMO -> TabletPanels.renderAmmo(this, g, x, y);
            case STATUS -> TabletPanels.renderStatus(this, g, x, y);
            case LOG -> TabletPanels.renderLog(this, g, x, y);
            default -> {
                // Every view is handled; the arm exists because the switch is over an enum.
            }
        }
        g.flush();
        g.disableScissor();
    }

    /**
     * The queue as cards: a status word, the grid reference, and range and bearing to it.
     *
     * <p>The status word is the point of the change. A red row said only that something was wrong;
     * "inside minimum range" and "terrain at 1200 m" are different problems with different answers,
     * and the second one is fixed by switching arc rather than by moving the target.
     */
    private void renderTargetCards(GuiGraphics g, int x, int y, int height) {
        if (targets.isEmpty()) {
            Ui.textWrapped(g, Component.translatable("gui.artillerytablet.no_targets"), x, y,
                    contentWidth(), COLOUR_MUTED);
            TabletTheme.draw(g, font, Component.empty(),
                    x, y + 10, COLOUR_MUTED);
            return;
        }

        int signature = TargetReachabilityCache.currentSignature();
        BlockPos gun = firstGunPosition();
        int rowY = y + Ui.GAP_SM;
        int shown = 0;

        for (int i = 0; i < targets.size(); i++) {
            if (rowY + CARD_HEIGHT > y + height) {
                break;
            }
            TargetEntry target = targets.get(i);
            TargetStatus status = TargetReachabilityCache.statusOf(signature, i);
            int obstruction = TargetReachabilityCache.obstructionOf(signature, i, target);

            Component state = statusWord(status, obstruction);
            int colour = status == TargetStatus.OK && obstruction < 0 ? COLOUR_GOOD : COLOUR_WARN;

            g.fill(x, rowY, x + contentWidth(), rowY + CARD_HEIGHT,
                    i == selected ? TabletTheme.BEZEL : TabletTheme.SURFACE);
            g.fill(x, rowY, x + 2, rowY + CARD_HEIGHT, colour);
            if (i == selected) {
                outline(g, x, rowY, contentWidth(), CARD_HEIGHT, COLOUR_ACCENT);
            }

            Ui.textIn(g, "T" + (i + 1), x + Ui.GAP_MD, rowY, CARD_LINE, COLOUR_TEXT);
            Ui.textRight(g, state, x + contentWidth() - 13, rowY, CARD_LINE, colour);
            Ui.textIn(g, target.x + " " + target.y + " " + target.z, x + Ui.GAP_MD, rowY + CARD_LINE, CARD_LINE, COLOUR_TEXT);
            Ui.textIn(g, rangeAndBearing(gun, target), x + Ui.GAP_MD, rowY + CARD_LINE * 2, CARD_LINE, COLOUR_MUTED);

            rowY += CARD_HEIGHT + 2;
            shown++;
        }

        if (shown < targets.size()) {
            TabletTheme.draw(g, font, Component.translatable("gui.artillerytablet.more_targets",
                    targets.size() - shown), x + 6, rowY + 1, COLOUR_MUTED);
        }
    }

    /** Range and bearing from the first bound gun, the two figures a gunner asks for. */
    private String rangeAndBearing(BlockPos gun, TargetEntry target) {
        if (gun == null) {
            return "--";
        }
        double dx = target.x - gun.getX();
        double dz = target.z - gun.getZ();
        int range = (int) Math.round(Math.sqrt(dx * dx + dz * dz));
        int bearing = (int) Math.round(Math.toDegrees(Math.atan2(dx, -dz)));
        // Mils, not degrees. NATO gunnery divides the circle into 6400 of them, and a fire-direction
        // readout that says 95° is speaking the wrong language to anyone who has laid a gun.
        return range + " m  ·  " + Math.floorMod(bearing, 360) * 6400 / 360 + " mil";
    }

    private Component statusWord(TargetStatus status, int obstruction) {
        if (status != TargetStatus.OK) {
            return switch (status) {
                case MIN_RANGE -> Component.translatable("hud.artillerytablet.state.min_range");
                case MAX_RANGE -> Component.translatable("hud.artillerytablet.state.max_range");
                default -> Component.translatable(depressed
                        ? "hud.artillerytablet.state.use_lofted"
                        : "hud.artillerytablet.state.use_depressed");
            };
        }
        if (obstruction >= 0) {
            return Component.translatable("hud.artillerytablet.state.blocked", obstruction);
        }
        return Component.translatable("hud.artillerytablet.state.ready");
    }

    private BlockPos firstGunPosition() {
        for (BoundArtillery bound : ArtilleryTacticalTabletItem.getBoundArtillery(liveStack())) {
            BlockPos pos = TabletClientData.lastKnownGunPosition(bound.id);
            if (pos != null) {
                return pos;
            }
        }
        return null;
    }

    /**
     * Draws the fire plan over the grid: guns, targets, and a line from each gun to each target so
     * the geometry of the mission is visible at a glance — the point of a plotting board.
     */
    private void renderMapMarkers(GuiGraphics g, int x, int y, int width, int height) {
        List<BlockPos> gunPositions = new ArrayList<>();
        for (BoundArtillery gun : ArtilleryTacticalTabletItem.getBoundArtillery(liveStack())) {
            BlockPos pos = TabletClientData.lastKnownGunPosition(gun.id);
            if (pos != null) {
                gunPositions.add(pos);
            }
        }

        renderRangeRings(g, x, y, width, height);

        int signature = TargetReachabilityCache.currentSignature();

        // Firing lines first, so the markers sit on top of them. Batched: each line is one fill per
        // pixel of its length, and outside a batch each of those is a draw call of its own — a
        // battery of four laid on eight targets was twenty thousand of them a frame.
        Ui.batched(g, () -> {
            for (int i = 0; i < targets.size(); i++) {
                TargetEntry target = targets.get(i);
                double[] to = map.worldToScreen(target.x, target.z, x, y, width, height);
                if (to == null) {
                    continue;
                }
                boolean reachable = TargetReachabilityCache.statusOf(signature, i) == TargetStatus.OK;
                for (BlockPos gun : gunPositions) {
                    double[] from = map.worldToScreen(gun.getX(), gun.getZ(), x, y, width, height);
                    if (from != null) {
                        drawLine(g, from[0], from[1], to[0], to[1],
                                reachable ? 0x40FF5A52 : 0x40FFB454);
                    }
                }
            }

            for (BlockPos pos : gunPositions) {
                double[] screen = map.worldToScreen(pos.getX(), pos.getZ(), x, y, width, height);
                if (screen != null) {
                    int sx = (int) screen[0];
                    int sy = (int) screen[1];
                    // Friendly blue, hollow: our own guns, in the standard symbol colour.
                    g.fill(sx - 3, sy - 3, sx + 4, sy + 4, TabletTheme.FRIENDLY);
                    g.fill(sx - 1, sy - 1, sx + 2, sy + 2, 0xFF0C1015);
                }
            }
        });

        for (int i = 0; i < targets.size(); i++) {
            TargetEntry target = targets.get(i);
            double[] screen = map.worldToScreen(target.x, target.z, x, y, width, height);
            if (screen == null) {
                continue;
            }
            boolean reachable = TargetReachabilityCache.statusOf(signature, i) == TargetStatus.OK;
            // A target is red; one the battery cannot service turns amber, because that is a
            // decision to make rather than a thing to shoot.
            int colour = reachable ? TabletTheme.HOSTILE : TabletTheme.WARNING;
            int sx = (int) screen[0];
            int sy = (int) screen[1];
            g.fill(sx - 3, sy, sx + 4, sy + 1, colour);
            g.fill(sx, sy - 3, sx + 1, sy + 4, colour);
            // The selected target wears a box, so the card list and the map agree about which one
            // the controls are pointed at.
            if (i == selected) {
                outline(g, sx - 4, sy - 4, 9, 9, colour);
            }
            TabletTheme.draw(g, font, String.valueOf(i + 1), sx + 4, sy - 8, colour, false);
        }

        if (this.minecraft != null && this.minecraft.player != null) {
            BlockPos self = this.minecraft.player.blockPosition();
            double[] screen = map.worldToScreen(self.getX(), self.getZ(), x, y, width, height);
            if (screen != null) {
                drawHeading(g, screen[0], screen[1], this.minecraft.player.getYRot());
            }
        }

        // A range and bearing readout used to sit in this corner. It went: it was hardcoded to the
        // first target from before there was a selection, it was still in degrees after everything
        // else moved to mils, and it was drawn under the left column where it showed through the
        // panel as an unreadable smear. The card and the detail strip both say it properly.
    }

    /**
     * How far each gun can reach on the arc currently selected.
     *
     * <p>The outer ring is the range at forty-five degrees; the inner one is the near edge of the
     * arc — how far the barrel can depress on flat fire, how far it can elevate on lofted. Drawing
     * them turns reach from a warning read after a target is placed into something visible before
     * it is, and it makes the lofted arc's uselessness close in obvious rather than surprising.
     *
     * <p>Advisory, like everything derived from copied ballistics: they ignore the height difference
     * to the target and SBW's own aim offset. Whether a shot is taken is still decided by asking the
     * gun.
     */
    private void renderRangeRings(GuiGraphics g, int x, int y, int width, int height) {
        List<NearbyArtilleryEntry> roster = TabletClientData.roster();
        if (roster == null) {
            return;
        }

        Ui.scissor(g, x, y, width, height);
        // Batched: a ring is up to seven hundred points and each point drawn on its own is a draw
        // call, so a battery's worth of rings cost more than the entire terrain layer beneath them.
        Ui.batched(g, () -> {
            for (NearbyArtilleryEntry gun : roster) {
                if (!gun.located || !boundIds.contains(gun.id)) {
                    continue;
                }
                double[] reach = gun.reachOn(depressed);
                if (reach == null) {
                    continue;
                }
                double perBlock = width / (double) map.spanBlocks();
                double[] centre = map.worldToScreenUnclipped(gun.x, gun.z, x, y, width, height);
                ring(g, centre, reach[1] * perBlock, 0x664DA3FF, 1);
                if (reach[0] > 1) {
                    ring(g, centre, reach[0] * perBlock, 0x664DA3FF, 3);
                }
            }
        });
        g.flush();
        g.disableScissor();
    }

    /**
     * A circle plotted point by point, dashed by skipping steps.
     *
     * <p>Step count follows the radius so a big ring does not turn into a polygon and a small one
     * does not cost hundreds of quads it has no room to show.
     */
    private void ring(GuiGraphics g, double[] centre, double radius, int colour, int dash) {
        if (radius < 2 || radius > 4000) {
            return;
        }
        int steps = (int) Math.min(720, Math.max(48, radius));
        for (int i = 0; i < steps; i++) {
            if (dash > 1 && i % dash != 0) {
                continue;
            }
            double angle = i * 2 * Math.PI / steps;
            int px = (int) Math.round(centre[0] + Math.cos(angle) * radius);
            int py = (int) Math.round(centre[1] + Math.sin(angle) * radius);
            g.fill(px, py, px + 1, py + 1, colour);
        }
    }

    // Grid references were written under each mark for a while. They read well one at a time and
    // cluttered the picture taken as a whole, which is the opposite of what a map overlay is for —
    // the card and the detail strip both give the numbers when a mark is actually being worked on.

    /**
     * Where the player is, and which way they are looking.
     *
     * <p>A dot says where; on a fire-control map, which way you are facing is half of orienting
     * yourself against the grid, so the mark carries a stalk in the direction of view. Minecraft
     * measures yaw from south, and the map draws +Z downwards, which is what puts the sine on the
     * screen's x and the cosine on its y with no further correction.
     */
    private void drawHeading(GuiGraphics g, double cx, double cy, float yawDegrees) {
        double yaw = Math.toRadians(yawDegrees);
        double dx = -Math.sin(yaw);
        double dy = Math.cos(yaw);

        // Drawn at the display's own resolution. The steps on every earlier version of this mark
        // were not a fault in the shape: the whole interface is laid out in its own pixels and then
        // magnified three times, so a six-pixel chevron was built on a grid three times coarser than
        // the one it is shown on. In here the same chevron has eighteen real pixels to turn in.
        double scale = Ui.deviceScale();
        // The chevron is scan-filled a row at a time, and at device resolution that is three times
        // as many rows as it looks. Batched, since it is nothing but fills.
        Ui.inDevicePixels(g, () -> Ui.batched(g, () -> drawChevron(g, cx * scale, cy * scale, dx, dy, scale)));
    }

    private void drawChevron(GuiGraphics g, double cx, double cy, double dx, double dy, double scale) {
        double length = HEADING_LENGTH * scale;
        double halfWidth = HEADING_HALF_WIDTH * scale;
        double px = -dy;
        double py = dx;

        double tipX = cx + dx * length;
        double tipY = cy + dy * length;
        double leftX = cx - dx * length * 0.6 + px * halfWidth;
        double leftY = cy - dy * length * 0.6 + py * halfWidth;
        double rightX = cx - dx * length * 0.6 - px * halfWidth;
        double rightY = cy - dy * length * 0.6 - py * halfWidth;
        double notchX = cx - dx * length * 0.1;
        double notchY = cy - dy * length * 0.1;

        Ui.triangle(g, tipX, tipY, leftX, leftY, notchX, notchY, TabletTheme.TEXT);
        Ui.triangle(g, tipX, tipY, notchX, notchY, rightX, rightY, TabletTheme.TEXT);
    }

    /** Length of each arm of the chevron, and how far they open away from the direction of view. */
    private static final double HEADING_LENGTH = 6.0;
    private static final double HEADING_HALF_WIDTH = 3.5;

    /** Simple straight line, drawn as pixels since GuiGraphics has no line primitive. */
    private void drawLine(GuiGraphics g, double x1, double y1, double x2, double y2, int colour) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        int steps = (int) Math.ceil(Math.max(Math.abs(dx), Math.abs(dy)));
        if (steps <= 0 || steps > 4000) {
            return;
        }
        for (int i = 0; i <= steps; i++) {
            int px = (int) Math.round(x1 + dx * i / steps);
            int py = (int) Math.round(y1 + dy * i / steps);
            g.fill(px, py, px + 1, py + 1, colour);
        }
    }

    private boolean dragging;
    private double pressX;
    private double pressY;

    /**
     * Whether the press that started this gesture actually landed on the map.
     *
     * <p>Without it the release handler had to re-derive that from the stored press position — but
     * those coordinates were only ever written when a press <em>did</em> land on the map, so after
     * one map click they stayed there. Pressing a button afterwards then released against a stale
     * "yes, that was the map", and reaching for a control dropped a target on the ground under it.
     */
    private boolean mapPress;

    /** Mirrors {@link #mapPress} for a press the active non-map app consumed, so its own release
     * and drag handlers keep hearing about a gesture it started. */
    private boolean appGesture;

    /** The case key a press last landed on, so its release sound plays wherever the mouse comes
     * back up rather than only if it is still sitting on the same key. */
    private UiButton pressedKey;

    /**
     * Whether the left mouse button is currently held down.
     *
     * <p>Purely a visual signal — a key's action already fires the instant it is pressed (see
     * {@link UiButton#press}), not on release, so this cannot gate anything real. What it drives is
     * the moulded-key's own "sunk into its socket" look while a hand keeps it held: {@code lit &&
     * mouseDown} in {@link UiButton#render}, read fresh every frame rather than tied to any one key
     * object, because {@link #rebuild} replaces every key the instant its action runs — a flag
     * living on the button itself would already be gone by the next frame.
     */
    private boolean mouseDown;

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double dx = mouseX;
        double dy = mouseY;

        if (button == 0) {
            mouseDown = true;
        }

        // Controls always get first refusal. Whatever the map does with a click is the fallback for
        // clicks that landed on nothing else, so a key or a panel control can never be shadowed by
        // the map lying underneath it.
        if (button == 0) {
            for (UiField field : fields) {
                field.setFocused(field.contains(dx, dy));
            }
            for (int i = keys.size() - 1; i >= 0; i--) {
                UiButton key = keys.get(i);
                if (key.press(dx, dy)) {
                    pressedKey = key;
                    mapPress = false;
                    return true;
                }
            }
            for (UiField field : fields) {
                if (field.contains(dx, dy)) {
                    mapPress = false;
                    return true;
                }
            }
        }

        // A non-map app owns the well exclusively — nothing beneath it (map pan/target, gun menu)
        // should also see this click.
        if (screenOn && !mapAppActive() && overMap(dx, dy)) {
            int[] area = mapArea();
            if (appManager.mouseClicked(dx - area[0], dy - area[1], button, area[2], area[3])) {
                appGesture = true;
                return true;
            }
        }

        // Right button on a gun's card opens its menu; anywhere else it shuts one that is open.
        if (button == 1) {
            UUID under = gunCardAt(dx, dy);
            if (under != null) {
                openGunMenu(under, (int) dx, (int) dy);
                return true;
            }
            if (menuGun != null) {
                menuGun = null;
                rebuild();
                return true;
            }
        }
        if (button == 0 && menuGun != null && !within(dx, dy, menuArea())) {
            menuGun = null;
            rebuild();
        }

        if (screenOn && map != null && overMap(dx, dy)) {
            if (button == 0) {
                // Left press only ever pans; the decision waits for the release in case it drags.
                mapPress = true;
                dragging = false;
                pressX = dx;
                pressY = dy;
                return true;
            }
            if (button == 1) {
                int[] area = mapArea();
                toggleTargetAt(map.screenToWorld(dx, dy, area[0], area[1], area[2], area[3]));
                return true;
            }
        }

        mapPress = false;
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            mouseDown = false;
            if (pressedKey != null) {
                pressedKey.release();
                pressedKey = null;
            }
        }
        if (mapPress && button == 0) {
            mapPress = false;
            dragging = false;
            return true;
        }
        if (appGesture) {
            appGesture = false;
            int[] area = mapArea();
            appManager.mouseReleased(mouseX - area[0], mouseY - area[1], button, area[2], area[3]);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    /** Drag threshold below which a press still counts as a click, not a pan. */
    private static final double DRAG_SLOP = 3.0;

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (screenOn && mapPress && button == 0) {
            double dx = mouseX;
            double dy = mouseY;
            if (!dragging && Math.abs(dx - pressX) + Math.abs(dy - pressY) > DRAG_SLOP) {
                dragging = true;
            }
            if (dragging) {
                int[] a = mapArea();
                map.panByPixels(dragX, dragY, a[2], a[3]);
                rebuild();
            }
            return true;
        }
        if (screenOn && appGesture) {
            int[] area = mapArea();
            appManager.mouseDragged(mouseX - area[0], mouseY - area[1], button, dragX, dragY, area[2], area[3]);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (UiField field : fields) {
            if (field.keyPressed(keyCode)) {
                return true;
            }
        }
        if (screenOn && !mapAppActive() && appManager.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char typed, int modifiers) {
        for (UiField field : fields) {
            if (field.charTyped(typed)) {
                return true;
            }
        }
        return super.charTyped(typed, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!screenOn) {
            // Nothing under the pointer to scroll or zoom. The keys on the case still work; the
            // glass is dark, and dark glass is not a map that happens to be hidden.
            return false;
        }
        // A wheel over a list scrolls it; a wheel over the map zooms. Which one the pointer is on
        // decides, so neither has to be reached for with a modifier.
        int step = (int) (-delta * CARD_LINE * 2);

        int[] l = panelArea();
        if (openTab != null && within(mouseX, mouseY, l)) {
            if (openTab == TabletTab.TARGETS) {
                int view = l[3] - PANEL_TITLE - ENTRY_HEIGHT;
                rightScroll = Math.max(0, Math.min(rightScroll + step, scrollLimit(targetContentHeight(), view)));
            } else {
                int view = l[3] - PANEL_TITLE;
                leftScroll = Math.max(0, Math.min(leftScroll + step, scrollLimit(batteryContentHeight(), view)));
            }
            rebuild();
            return true;
        }

        if (map != null && overMap(mouseX, mouseY)) {
            map.zoomBy(delta > 0 ? 1 : -1);
            rebuild();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    /**
     * Right button places a target, or clears the one already there.
     *
     * <p>One button doing both directions keeps the left button free for panning and for the
     * controls — which is what stopped the two from being confused for each other in the first
     * place. It is also how map targeting worked before the tablet had its own map, so the gesture
     * is not new to anyone who used it then.
     */
    private void toggleTargetAt(BlockPos pos) {
        int existing = targetNear(pos);
        if (existing >= 0) {
            removeTarget(existing);
        } else {
            addTargetAt(pos);
        }
    }

    /** A one-pixel frame in the tablet's own idiom, for things that draw no border of their own. */
    private void outline(GuiGraphics g, int x, int y, int w, int h) {
        Ui.outline(g, x, y, w, h, TabletTheme.LINE);
    }

    private void outline(GuiGraphics g, int x, int y, int w, int h, int colour) {
        g.fill(x, y, x + w, y + 1, colour);
        g.fill(x, y + h - 1, x + w, y + h, colour);
        g.fill(x, y, x + 1, y + h, colour);
        g.fill(x + w - 1, y, x + w, y + h, colour);
    }

    /**
     * The map: everything below the header, left of the action bezel, out to the tablet's edges.
     *
     * <p>The queue no longer takes a strip along the bottom and the view keys no longer take a
     * column on the left, so the map now runs to both — which is the whole point of moving them.
     * The mission column overlays this rather than shrinking it, so the map does not resize every
     * time a gun starts or finishes.
     */
    /** The map runs to all four edges under the header. Panels float on it; nothing frames it. */
    private int[] mapArea() {
        return new int[]{left, top, width0, height0};
    }

    /**
     * The action box, under the header at the right: what the battery is about to do.
     *
     * <p>FIRE is the tall one and the only red thing on the device. Mode and arc stack beside it to
     * the same total height — read as often as they are pressed, and needed while looking at the
     * map, which is why none of the three live behind a panel any more.
     */
    // The rail is gone. Every key it carried now stands on the case, where a key belongs on a device
    // shaped like this one — and the map took back the twenty-six pixels the column was standing on.

    /**
     * The lowest ground the floating furniture may use.
     *
     * <p>The detail strip is always there, empty when nothing is selected, so this never moves. It
     * used to appear and disappear with the selection, which shifted every panel and every key on
     * the map by twenty pixels the moment you picked something.
     */
    private int bodyBottom() {
        int[] a = mapArea();
        return a[1] + a[3];
    }

    private boolean detailShown() {
        return selectedGun != null || (selected >= 0 && selected < targets.size());
    }

    /**
     * The window opened from a gun, holding what can be done to it.
     *
     * <p>Everything here used to be keys on the gun's own card, in a column barely wider than the
     * words. That is why text and keys kept colliding: the card was being asked to be a readout and
     * a control panel at once. It is a readout now, and this is the control panel — opened when it
     * is wanted, sized to what it holds rather than to the column.
     */
    private int[] menuArea() {
        int[] a = mapArea();
        int w = 92;
        int h = PANEL_TITLE + MENU_ITEMS * KEY_ROW + Ui.GAP_SM;
        int x = Math.min(menuX, a[0] + a[2] - w - Ui.GAP_XS);
        int y = Math.min(menuY, bodyBottom() - h - Ui.GAP_XS);
        return new int[]{x, y, w, h};
    }


    /** The target list, filling the right side under the action box. */



    /**
     * The open view's overlay, against the tablet's left edge.
     *
     * <p>This is the rectangle that gets drawn, the rectangle the map is told to keep clear, and the
     * rectangle the controls are laid out inside — one rectangle, not three. It used to be inset
     * from the map and then painted four pixels larger than itself, which put the panel two pixels
     * outside the tablet on three sides and left a band that was covered but never reserved, so grid
     * labels were drawn into it and then dimmed by the panel instead of being left out.
     */
    private int[] panelArea() {
        int[] a = mapArea();
        return new int[]{a[0], a[1], PANEL_WIDTH, bodyBottom() - a[1]};
    }


    /** Every standing rectangle the map must not be clicked through. */
    private boolean overAnyPanel(double mx, double my) {
        return (openTab != null && within(mx, my, panelArea()))
                || (menuGun != null && within(mx, my, menuArea()));
    }

    private static boolean within(double mx, double my, int[] r) {
        return mx >= r[0] && mx <= r[0] + r[2] && my >= r[1] && my <= r[1] + r[3];
    }

    /**
     * Stretches a covered area up to the top of the map.
     *
     * <p>A panel sits six pixels below the map's edge, and the grid's X labels are drawn two pixels
     * below it — so the labels fell in the sliver above the panel, escaped the covered area, and
     * printed across its header. The sliver is too narrow to hold a label anyway, so the honest
     * thing is to treat it as covered.
     */
    private boolean overMap(double mouseX, double mouseY) {
        int[] a = mapArea();
        boolean inMap = mouseX >= a[0] && mouseX <= a[0] + a[2] && mouseY >= a[1] && mouseY <= a[1] + a[3];
        // A press on anything standing over the map belongs to that, not to the map underneath —
        // otherwise reaching for a gun's key would also drop a target on the ground below it.
        return inMap && !overAnyPanel(mouseX, mouseY);
    }

    private boolean overPanel(double mouseX, double mouseY) {
        if (openTab == null) {
            return false;
        }
        return within(mouseX, mouseY, panelArea());
    }

    private void addTargetAt(BlockPos pos) {
        if (targets.size() >= ArtilleryTacticalTabletItem.MAX_TARGETS) {
            return;
        }
        targets.add(new TargetEntry(pos.getX(), pos.getY(), pos.getZ()));
        pushTargets();
        rebuild();
    }

    /**
     * Index of whichever target the click landed near, or -1. Tolerance is a screen distance
     * converted to blocks, so the hit area stays the same size however far the map is zoomed out.
     */
    private int targetNear(BlockPos pos) {
        int[] a = mapArea();
        int tolerance = Math.max(2, map.blocksPerPixel(a[2], a[3]) * 6);
        int toleranceSqr = tolerance * tolerance;

        int best = -1;
        int bestSqr = Integer.MAX_VALUE;
        for (int i = 0; i < targets.size(); i++) {
            TargetEntry t = targets.get(i);
            int dx = t.x - pos.getX();
            int dz = t.z - pos.getZ();
            int distSqr = dx * dx + dz * dz;
            if (distSqr <= toleranceSqr && distSqr < bestSqr) {
                best = i;
                bestSqr = distSqr;
            }
        }
        return best;
    }

    // Accessors for the panel renderers, which are split out only to keep this class readable.
    Minecraft mc() {
        return this.minecraft;
    }

    net.minecraft.client.gui.Font font() {
        return this.font;
    }

    /** Usable width inside an open panel, once its padding is taken off both edges. */
    static int contentWidth() {
        return PANEL_WIDTH - PANEL_PAD * 2;
    }

    int sideWidth() {
        return contentWidth();
    }

    boolean isMainHand() {
        return mainHand();
    }

    void addWidget(UiButton button) {
        addKey(button);
    }

    void refresh() {
        rebuild();
    }

    ItemStack stack() {
        return liveStack();
    }

    static int colourText() {
        return COLOUR_TEXT;
    }

    static int colourMuted() {
        return COLOUR_MUTED;
    }

    static int colourGood() {
        return COLOUR_GOOD;
    }

    static int colourWarn() {
        return COLOUR_WARN;
    }

    static int colourAccent() {
        return COLOUR_ACCENT;
    }

    static int colourSelected() {
        return TabletTheme.BEZEL;
    }

    UUID selectedGun() {
        return selectedGun;
    }

    /**
     * Picks a gun for the detail strip to describe.
     *
     * <p>Selecting a gun clears the selected target and the other way round. The strip describes one
     * thing at a time, and a strip that could be showing either without saying which would be the
     * worst of both.
     */
    void selectGun(UUID id) {
        this.selectedGun = id.equals(selectedGun) ? null : id;
        if (this.selectedGun != null) {
            this.selected = -1;
        }
        rebuild();
    }

    static int rowHeight() {
        return ROW;
    }
}
