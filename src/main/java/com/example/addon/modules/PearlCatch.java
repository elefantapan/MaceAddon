package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import com.example.addon.helper.SmoothAim;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class PearlCatch extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> windDelay = sgGeneral.add(new IntSetting.Builder()
            .name("wind-delay")
            .description("Ticks to wait after catching pearl before using Wind Charge.")
            .defaultValue(3)
            .min(1)
            .max(20)
            .build());

    private EnderPearlEntity lastPearl = null;
    private int tickCounter = 0;
    private boolean thrownPearl = false;
    private int previousSlot = -1;

    public PearlCatch() {
        super(AddonTemplate.CATEGORY, "pearl-catch",
                "Throws pearl and automatically catches it, optionally using Wind Charge.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null || mc.world == null) return;

        int pearlSlot = findHotbarItem(Items.ENDER_PEARL);
        if (pearlSlot == -1) {
            error("No Ender Pearl in hotbar!");
            toggle();
            return;
        }

        // Save current slot
        previousSlot = mc.player.getInventory().selectedSlot;

        // Swap and throw pearl
        InvUtils.swap(pearlSlot, true);
        throwPearl();
        thrownPearl = true;
        tickCounter = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!thrownPearl || mc.player == null || mc.world == null) return;

        // Track the last thrown pearl
        if (lastPearl == null) {
            for (var entity : mc.world.getEntities()) {
                if (entity instanceof EnderPearlEntity pearl) {
                    if (pearl.getOwner() == mc.player) lastPearl = pearl;
                }
            }
        }

        // Smooth aim at the pearl
        if (lastPearl != null && !lastPearl.isRemoved()) {
            Vec3d pearlPos = new Vec3d(lastPearl.getX(), lastPearl.getY(), lastPearl.getZ());
            aimAt(pearlPos, 0.5f); // 0.5 smooth factor
        }

        tickCounter++;

        // If pearl has landed or cooldown reached, use Wind Charge
        if ((lastPearl != null && lastPearl.isRemoved()) || tickCounter >= windDelay.get()) {
            int windSlot = findHotbarItem(Items.WIND_CHARGE);
            if (windSlot != -1) {
                InvUtils.swap(windSlot, true);
                useWindCharge();
            }

            // Swap back to previous slot
            if (previousSlot != -1) InvUtils.swap(previousSlot, true);

            // Reset state
            thrownPearl = false;
            lastPearl = null;
            toggle(); // auto-disable module
        }
    }

    private void throwPearl() {
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
    }

    private void useWindCharge() {
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
    }

    private int findHotbarItem(Items item) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isOf(item)) return i;
        }
        return -1;
    }

    private void aimAt(Vec3d pos, float smoothFactor) {
        Vec3d eyes = mc.player.getEyePos();
        Vec3d diff = pos.subtract(eyes);

        double dx = diff.x;
        double dy = diff.y;
        double dz = diff.z;

        double distXZ = Math.sqrt(dx * dx + dz * dz);
        float targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float targetPitch = (float) -Math.toDegrees(Math.atan2(dy, distXZ));

        SmoothAim.apply(mc.player, targetYaw, targetPitch, smoothFactor, true, true);
    }
}
