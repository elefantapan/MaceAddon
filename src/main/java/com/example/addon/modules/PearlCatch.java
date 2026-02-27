package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import meteordevelopment.meteorclient.utils.player.InvUtils;

public class PearlCatch extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> delayTicks = sgGeneral.add(
        new IntSetting.Builder()
            .name("delay")
            .description("Ticks between pearl throw and wind charge.")
            .defaultValue(7)
            .min(1)
            .max(20)
            .sliderMax(20)
            .build()
    );

    private int ticks = 0;
    private boolean active = false;
    private int previousSlot = -1;

    public PearlCatch() {
        super(AddonTemplate.CATEGORY,
            "pearl-catch",
            "Throws a pearl, then catches it with a wind charge.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null || mc.world == null) {
            toggle();
            return;
        }
    
        int pearlSlot = InvUtils.findInHotbar(Items.ENDER_PEARL).slot();
        if (pearlSlot == -1) {
            error("No ender pearl in hotbar.");
            toggle();
            return;
        }
    
        previousSlot = mc.player.getInventory().getSelectedSlot();
    
        InvUtils.swap(pearlSlot, false);
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
    
        ticks = 0;
        active = true;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!active || mc.player == null) return;

        ticks++;

        if (ticks < delayTicks.get()) return;

        int windSlot = InvUtils.findInHotbar(Items.WIND_CHARGE).slot();
        if (windSlot == -1) {
            error("No wind charge in hotbar.");
            toggle();
            return;
        }

        InvUtils.swap(windSlot, false);
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);

        if (previousSlot != -1) {
            InvUtils.swap(previousSlot, false);
        }

        active = false;
        toggle(); // auto-disable
    }
}
