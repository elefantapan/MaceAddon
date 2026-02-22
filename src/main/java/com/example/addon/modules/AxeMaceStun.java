package com.example.modules;

import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.entity.player.DoAttackEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MaceItem;

import java.util.Random;

public class AxeMaceStun extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> delayTicks = sgGeneral.add(new IntSetting.Builder()
        .name("delay-ticks")
        .description("Base delay before swapping to Mace (1-10 ticks).")
        .defaultValue(3)
        .range(1, 10)
        .sliderRange(1, 10)
        .build()
    );

    private final Setting<Integer> spread = sgGeneral.add(new IntSetting.Builder()
        .name("spread")
        .description("Adds random +- ticks to delay to make swaps less perfect.")
        .defaultValue(0)
        .range(0, 5)
        .sliderRange(0, 5)
        .build()
    );

    private int timer = 0;
    private int targetSlot = -1;
    private boolean awaitingSwap = false;
    private final Random random = new Random();

    public AxeToMaceSwap() {
        super(Categories.Combat, "axe-to-mace", "Swaps from Axe to Mace after hitting.");
    }

    @Override
    public void onDeactivate() {
        timer = 0;
        awaitingSwap = false;
        targetSlot = -1;
    }

    @EventHandler
    private void onAttack(DoAttackEvent event) {
        ItemStack main = mc.player.getMainHandStack();
        if (!(main.getItem() instanceof AxeItem)) return;

        // Find a Mace in hotbar
        int maceSlot = InvUtils.findInHotbar(item -> item.getItem() instanceof MaceItem).slot();
        if (maceSlot == -1) return;

        targetSlot = maceSlot;

        // Calculate randomized delay
        int randomSpread = (spread.get() > 0) ? random.nextInt(spread.get() * 2 + 1) - spread.get() : 0;
        timer = delayTicks.get() + randomSpread;
        awaitingSwap = true;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!awaitingSwap || targetSlot == -1) return;

        timer--;
        if (timer <= 0) {
            // Swap to Mace
            InvUtils.swap(targetSlot, false);
            // Attack immediately after swap
            mc.interactionManager.attackEntity(mc.player, mc.crosshairTarget.getType() == null ? null : (Entity) mc.crosshairTarget);
            awaitingSwap = false;
            targetSlot = -1;
        }
    }
}
