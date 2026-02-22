package com.example.addon.modules;

import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AxeMaceStun extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> swapDelay = sgGeneral.add(
        new IntSetting.Builder()
            .name("swap-delay-ms")
            .description("Delay before swapping to mace.")
            .defaultValue(150)
            .min(0)
            .sliderMax(500)
            .build()
    );

    private final Setting<Boolean> swapBack = sgGeneral.add(
        new BoolSetting.Builder()
            .name("swap-back")
            .description("Swap back to axe after slam.")
            .defaultValue(true)
            .build()
    );

    private int delayTicks = 0;
    private int previousSlot = -1;
    private boolean pendingSlam = false;

    public AxeMaceStun() {
        super(com.example.addon.AddonTemplate.CATEGORY, "axe-mace-stun", "Auto mace slam after axe shield hit.");
    }

    // Detect axe hit on shield
    @EventHandler
    private void onAttack(AttackEntityEvent event) {
        if (!(event.entity instanceof PlayerEntity target)) return;
        if (mc.player == null) return;

        if (!(mc.player.getMainHandStack().getItem() instanceof AxeItem)) return;
        if (!target.isBlocking()) return;

        int maceSlot = findMaceSlot();
        if (maceSlot == -1) return;

        previousSlot = mc.player.getInventory().selectedSlot;
        delayTicks = swapDelay.get() / 50;
        pendingSlam = true;
    }

    // Handle delay + slam
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!pendingSlam || mc.player == null) return;

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

        // NO auto-jump — player must jump manually
        if (!mc.player.isOnGround() && mc.player.fallDistance > 1.5f) {
            if (mc.targetedEntity != null) {
                mc.interactionManager.attackEntity(mc.player, mc.targetedEntity);
                mc.player.swingHand(Hand.MAIN_HAND);
            }

            if (swapBack.get() && previousSlot != -1) {
                mc.player.getInventory().selectedSlot = previousSlot;
            }

            pendingSlam = false;
        }
    }

    private int findMaceSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.MACE) {
                return i;
            }
        }
        return -1;
    }
}
