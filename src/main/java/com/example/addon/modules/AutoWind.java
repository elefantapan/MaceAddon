package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.entity.player.InteractItemEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;

public class AutoWind extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("tick-delay")
        .description("The delay between jumping (in ticks)")
        .defaultValue(2)
        .range(0, 20)
        .build()
    );

    private int ticks;

    public AutoWind() {
        super(AddonTemplate.CATEGORY, "auto-wind", "Auto jump when using wind charge downward");
    }

    @Override
    public void onActivate() {
        ticks = 0;
    }

    @EventHandler
    private void onItemUse(InteractItemEvent event) {
        if (mc.player == null) return;

        if (event.item.getItem() != Items.WIND_CHARGE) return;
        if (mc.player.getPitch() <= 60f) return;
        if (!mc.player.isOnGround()) return;

        if (ticks++ < delay.get()) return;
        ticks = 0;

        mc.player.jump();
    }
}
