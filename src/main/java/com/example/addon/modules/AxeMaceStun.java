package com.example.addon.modules;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;

import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.EntityHitResult;

import java.util.concurrent.ThreadLocalRandom;

public class AxeMaceStun extends Module {

    private final IntSetting delayTicks = this.settings.add(new IntSetting.Builder()
        .name("delay-ticks")
        .description("Ticks to wait before swapping to mace and hitting again")
        .defaultValue(5)
        .min(1)
        .max(10)
        .sliderMin(1)
        .sliderMax(10)
        .build()
    );

    private final IntSetting spreadTicks = this.settings.add(new IntSetting.Builder()
        .name("spread-ticks")
        .description("Random +/- ticks added to the delay to make it less perfect")
        .defaultValue(0)
        .min(0)
        .max(5)
        .sliderMin(0)
        .sliderMax(5)
        .build()
    );

    private boolean awaitingSwap = false;
    private int timer = 0;
    private int targetSlot = -1;

    public AxeMaceStun() {
        super(AddonTemplate.CATEGORY, "axe-mace-stun", "Swaps to mace after hitting with an axe and hits again");
    }

    @Override
    public void onActivate() {
        awaitingSwap = false;
        timer = 0;
        targetSlot = -1;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!awaitingSwap) return;

        timer--;
        if (timer <= 0) {
            // Swap to Mace slot
            if (targetSlot != -1) InvUtils.swap(targetSlot, false);

            // Attack target again if valid
            HitResult target = mc.crosshairTarget;
            if (target instanceof EntityHitResult entityHit) {
                mc.interactionManager.attackEntity(mc.player, entityHit.getEntity());
            }

            awaitingSwap = false;
            targetSlot = -1;
        }
    }

    @Override
    public void onAttack(Entity target) {
        if (mc.player == null || mc.world == null) return;

        // Only trigger if holding an axe
        if (mc.player.getMainHandStack().getItem() != Items.IRON_AXE &&
            mc.player.getMainHandStack().getItem() != Items.DIAMOND_AXE &&
            mc.player.getMainHandStack().getItem() != Items.NETHERITE_AXE) return;

        // Find Mace slot (example: pick any slot with sword)
        targetSlot = InvUtils.findItemSlot(Items.IRON_AXE); // replace with your "mace" item
        if (targetSlot == -1) return;

        // Calculate timer with spread
        int delay = delayTicks.get() + ThreadLocalRandom.current().nextInt(-spreadTicks.get(), spreadTicks.get() + 1);
        timer = Math.max(1, delay); // min 1 tick

        awaitingSwap = true;
    }
}
