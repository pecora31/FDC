package net.nazarick.artillerytablet.item;

import com.atsuishio.superbwarfare.item.ItemScreenProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.nazarick.artillerytablet.client.screen.TabletScreen;
import net.nazarick.artillerytablet.fire.FireMode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Opened via SBW's own "Edit Mode" key (default H) — {@link ItemScreenProvider} is SBW's hook for
 * that, picked up automatically by SBW's ClickEventHandler for any item implementing it.
 */
public class ArtilleryTacticalTabletItem extends Item implements ItemScreenProvider {
    public static final String TAG_TARGETS = "Targets";
    public static final int MAX_TARGETS = 32;

    public static final String TAG_BOUND = "BoundArtillery";
    public static final int MAX_BOUND = 8;

    public static final String TAG_FIRE_MODE = "FireMode";
    public static final String TAG_DEPRESSED = "Depressed";
    public static final String TAG_INPUT_X = "InputX";
    public static final String TAG_INPUT_Y = "InputY";
    public static final String TAG_INPUT_Z = "InputZ";

    /** How far the spotting trace reaches, in blocks. */
    private static final double SPOT_RANGE = 512.0;

    public ArtilleryTacticalTabletItem(Properties properties) {
        super(properties);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Screen getItemScreen(ItemStack stack, Player player, InteractionHand hand) {
        return new TabletScreen(stack, hand);
    }

    /**
     * Right-click marks whatever the player is sighting on as a target — spotting by eye instead of
     * typing coordinates.
     *
     * <p>The trace runs server-side. Doing it on the client would cap spotting at render distance,
     * since blocks past that simply aren't there to hit; the server has the whole loaded world. That
     * also means no packet is needed — {@code use} already runs on both sides, so the client half
     * just declines to act.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        List<TargetEntry> targets = getTargets(stack);
        if (targets.size() >= MAX_TARGETS) {
            player.displayClientMessage(
                    Component.translatable("tips.artillerytablet.queue_full", MAX_TARGETS)
                            .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1f).scale(SPOT_RANGE));
        BlockHitResult hit = level.clip(
                new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK) {
            player.displayClientMessage(
                    Component.translatable("tips.artillerytablet.nothing_in_sight")
                            .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        BlockPos pos = hit.getBlockPos();
        targets.add(new TargetEntry(pos.getX(), pos.getY(), pos.getZ()));
        setTargets(stack, targets);

        player.displayClientMessage(
                Component.translatable("tips.artillerytablet.target_marked",
                        targets.size(), pos.getX(), pos.getY(), pos.getZ())
                        .withStyle(ChatFormatting.GREEN), true);
        level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_PLING.value(),
                SoundSource.PLAYERS, 0.6f, 1.6f);
        return InteractionResultHolder.success(stack);
    }

    public static List<TargetEntry> getTargets(ItemStack stack) {
        List<TargetEntry> result = new ArrayList<>();
        if (!stack.hasTag()) {
            return result;
        }

        ListTag list = stack.getOrCreateTag().getList(TAG_TARGETS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            result.add(new TargetEntry(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z")));
        }
        return result;
    }

    public static void setTargets(ItemStack stack, List<TargetEntry> targets) {
        ListTag list = new ListTag();
        int count = Math.min(targets.size(), MAX_TARGETS);
        for (int i = 0; i < count; i++) {
            TargetEntry entry = targets.get(i);
            CompoundTag tag = new CompoundTag();
            tag.putInt("X", entry.x);
            tag.putInt("Y", entry.y);
            tag.putInt("Z", entry.z);
            list.add(tag);
        }
        stack.getOrCreateTag().put(TAG_TARGETS, list);
    }

    public static List<BoundArtillery> getBoundArtillery(ItemStack stack) {
        List<BoundArtillery> result = new ArrayList<>();
        if (!stack.hasTag()) {
            return result;
        }

        ListTag list = stack.getOrCreateTag().getList(TAG_BOUND, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            result.add(new BoundArtillery(tag.getUUID("Id"), tag.getString("Type")));
        }
        return result;
    }

    /** Server must re-verify ownership before calling this — see BindArtilleryMessage. */
    public static boolean bindArtillery(ItemStack stack, UUID id, String typeId) {
        List<BoundArtillery> current = getBoundArtillery(stack);
        if (current.size() >= MAX_BOUND) {
            return false;
        }
        for (BoundArtillery bound : current) {
            if (bound.id.equals(id)) {
                return false;
            }
        }

        current.add(new BoundArtillery(id, typeId));
        writeBoundArtillery(stack, current);
        return true;
    }

    public static boolean unbindArtillery(ItemStack stack, UUID id) {
        List<BoundArtillery> current = getBoundArtillery(stack);
        boolean removed = current.removeIf(bound -> bound.id.equals(id));
        if (removed) {
            writeBoundArtillery(stack, current);
        }
        return removed;
    }

    private static void writeBoundArtillery(ItemStack stack, List<BoundArtillery> list) {
        ListTag tag = new ListTag();
        for (BoundArtillery bound : list) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Id", bound.id);
            entry.putString("Type", bound.typeId);
            tag.add(entry);
        }
        stack.getOrCreateTag().put(TAG_BOUND, tag);
    }

    public static FireMode getFireMode(ItemStack stack) {
        if (!stack.hasTag()) {
            return FireMode.SINGLE;
        }
        int ordinal = stack.getOrCreateTag().getInt(TAG_FIRE_MODE);
        FireMode[] modes = FireMode.values();
        return ordinal >= 0 && ordinal < modes.length ? modes[ordinal] : FireMode.SINGLE;
    }

    public static void setFireMode(ItemStack stack, FireMode mode) {
        stack.getOrCreateTag().putInt(TAG_FIRE_MODE, mode.ordinal());
    }

    public static boolean isDepressed(ItemStack stack) {
        return stack.hasTag() && stack.getOrCreateTag().getBoolean(TAG_DEPRESSED);
    }

    public static void setDepressed(ItemStack stack, boolean depressed) {
        stack.getOrCreateTag().putBoolean(TAG_DEPRESSED, depressed);
    }

    /** Remembers the half-typed coordinate boxes so reopening the screen doesn't wipe them. */
    public static String getInput(ItemStack stack, String tag) {
        return stack.hasTag() ? stack.getOrCreateTag().getString(tag) : "";
    }

    public static void setInputs(ItemStack stack, String x, String y, String z) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_INPUT_X, x);
        tag.putString(TAG_INPUT_Y, y);
        tag.putString(TAG_INPUT_Z, z);
    }
}
