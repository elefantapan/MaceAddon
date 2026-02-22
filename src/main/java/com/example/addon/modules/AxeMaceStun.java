package com.example.addon.modules;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.SettingGroup.Builder;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;

import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.concurrent.ThreadLocalRandom;

public class AxeMaceStun extends Module {

    private final SettingGroup sgGeneral = this.settings.createGroup("General");

    private final Setting<Integer> delayTicks = sgGeneral.add(new IntSetting.Builder()
        .name("delay-ticks")
        .description("Ticks to wait before swapping to mace and hitting again")
        .defaultValue(5)
        .min(1)
        .max(10)
        .sliderMin(1)
        .sliderMax(10)
        .build()
    );

    private final Setting<Integer> spreadTicks = sgGeneral.add(new IntSetting.Builder()
        .name("spread-ticks")
        .description("Random +/- ticks added to the delay")
        .defaultValue(0)
        .min(0)
        .max(5)
        .sliderMin(0)
        .sliderMax(5)
        .build()
    );

    private boolean awaitingSwap = false;
    private int timer = 0;
    private int maceSlot = -1;
    private Entity targetEntity = null;

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public AxeMaceStun() {
        super(AddonTemplate.CATEGORY, "axe-mace-stun", "Swaps to mace after hitting with an axe and hits again");
    }

    @Override
    public void onDeactivate() {
        awaitingSwap = false;
        timer = 0;
        maceSlot = -1;
        targetEntity = null;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!awaitingSwap) return;

        timer--;
        if (timer <= 0) {
            // Swap to mace
            if (maceSlot != -1) {
                mc.player.getInventory().selectedSlot = maceSlot;
            }

            // Attack entity if still valid
            if (targetEntity != null && targetEntity.isAlive()) {
                mc.interactionManager.attackEntity(mc.player, targetEntity);
            }

            awaitingSwap = false;
            maceSlot = -1;
            targetEntity = null;
        }
    }

    @Override
    public void onAttack(Entity entity) {
        if (mc.player == null || mc.world == null) return;

        // Only trigger if holding an axe
        switch (mc.player.getMainHandStack().getItem().toString()) {
            case "iron_axe":
            case "diamond_axe":
            case "netherite_axe":
                break;
            default:
                return;
        }

        // Find mace slot (replace with your actual mace item)
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.IRON_SWORD) { // example mace
                maceSlot = i;
                break;
            }
        }

        if (maceSlot == -1) return; // no mace found

        // Save target and calculate timer
        targetEntity = entity;
        int delay = delayTicks.get() + ThreadLocalRandom.current().nextInt(-spreadTicks.get(), spreadTicks.get() + 1);
        timer = Math.max(1, delay);
        awaitingSwap = true;
    }
}
