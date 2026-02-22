package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Items;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;

public class AxeMaceStun extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> swapDelay = sgGeneral.add(
        new IntSetting.Builder()
            .name("swap-delay-ms")
            .description("Delay before performing the slam (in milliseconds).")
            .defaultValue(150)
            .min(0)
            .sliderMax(500)
            .build()
    );

    private final Setting<Boolean> swapBack = sgGeneral.add(
        new BoolSetting.Builder()
            .name("swap-back")
            .description("Swap back to your original slot after the slam.")
            .defaultValue(true)
            .build()
    );

    private int delayTicks;
    private int previousSlot;
    private boolean pendingSlam;

    public AxeMaceStun() {
        super(AddonTemplate.CATEGORY, "axe-mace-stun", "Auto mace slam after axe shield hit.");
    }

    @EventHandler
    private void onAttack(AttackEntityEvent event) {
        if (!(event.entity instanceof PlayerEntity target)) return;
        if (mc.player == null) return;

        // Only trigger if holding an axe
        if (!(mc.player.getMainHandStack().getItem() instanceof AxeItem)) return;

        // Only trigger if target is blocking
        if (!target.isBlocking()) return;

        int maceSlot = findMaceSlot();
        if (maceSlot == -1) return;

        previousSlot = mc.player.getInventory().selectedSlot;
        delayTicks = swapDelay.get() / 50; // convert ms → ticks
        pendingSlam = true;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!pendingSlam || mc.player == null) return;

        // Wait for delay
        if (delayTicks > 0) {
            delayTicks--;
            return;
        }

        int maceSlot = findMaceSlot();
        if (maceSlot == -1) {
            pendingSlam = false;
            return;
        }

        mc.player.getInventory().selectedSlot = maceSlot;

        if (mc.targetedEntity != null) {
            mc.interactionManager.attackEntity(mc.player, mc.targetedEntity);
            mc.player.swingHand(Hand.MAIN_HAND);
        }

        if (swapBack.get()) {
            mc.player.getInventory().selectedSlot = previousSlot;
        }

        pendingSlam = false;
    }

    private int findMaceSlot() {
        for (int i = 0; i < 9; i++) {
            Item stackItem = mc.player.getInventory().getStack(i).getItem();
            if (stackItem == Items.IRON_AXE) continue; // ignore axes
            if (stackItem == Items.MACE) return i;     // replace with your mace item
        }
        return -1;
    }
}
