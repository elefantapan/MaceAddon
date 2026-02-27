package com.example.addon.modules.pvp;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.events.world.TickEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class PearlCatch extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> delayTicks = sgGeneral.add(new IntSetting.Builder()
            .name("delay")
            .description("Delay after throwing pearl before throwing wind charge.")
            .defaultValue(2)
            .range(0, 10)
            .build());

    private EnderPearlEntity lastPearl = null;
    private int tickCounter = 0;
    private boolean throwingPearl = false;

    public PearlCatch() {
        super(AddonTemplate.CATEGORY, "pearl-catch", "Throws pearl then wind charge and automatically aims at the pearl.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null || mc.world == null) {
            toggle();
            return;
        }

        int pearlSlot = findPearlSlot();
        if (pearlSlot == -1) {
            error("No Ender Pearl found in hotbar.");
            toggle();
            return;
        }

        // Swap to pearl and throw
        InvUtils.swap(pearlSlot, true);
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);

        // Start tracking
        tickCounter = 0;
        throwingPearl = true;
        lastPearl = null; // Will be set in onTick
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!throwingPearl || mc.player == null || mc.world == null) return;

        // Track the pearl entity
        if (lastPearl == null) {
            for (var entity : mc.world.getEntities()) {
                if (entity instanceof EnderPearlEntity pearl && pearl.getOwner() == mc.player) {
                    lastPearl = pearl;
                    break;
                }
            }
        }

        tickCounter++;

        // Wait delay ticks
        if (tickCounter < delayTicks.get()) return;

        if (lastPearl != null && lastPearl.isAlive()) {
            // Aim at the pearl
            Vec3d pearlPos = lastPearl.getPos();
            SmoothAim.lookAt(pearlPos, 0.5); // 0.5 = smooth factor
        }

        // Once we have a wind charge
        int windSlot = findWindChargeSlot();
        if (windSlot == -1) {
            error("No Wind Charge found.");
            toggle();
            return;
        }

        // Swap and use wind charge
        InvUtils.swap(windSlot, true);
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);

        InvUtils.swapBack();
        toggle();
    }

    private int findPearlSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isOf(Items.ENDER_PEARL)) return i;
        }
        return -1;
    }

    private int findWindChargeSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isOf(Items.WIND_CHARGE)) return i;
        }
        return -1;
    }
}
