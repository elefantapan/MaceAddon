package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffects;

public class AntiBlindness extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public AntiBlindness() {
        super(Category.Render, "anti-blindness", "Removes blindness and nausea effects automatically");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        // Remove blindness
        if (mc.player.hasStatusEffect(StatusEffects.BLINDNESS)) {
            mc.player.removeStatusEffect(StatusEffects.BLINDNESS);
        }

        // Optional: Remove nausea (confusion) as well
        if (mc.player.hasStatusEffect(StatusEffects.NAUSEA)) {
            mc.player.removeStatusEffect(StatusEffects.NAUSEA);
        }
    }
}
