package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.world.TickEvent;
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
        .description("Delay between jumps (ticks)")
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
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        // Check held item safely
        if (mc.player.getMainHandStack().getItem() != Items.WIND_CHARGE) return;

        // Looking down
        if (mc.player.getPitch() <= 60f) return;

        // Must be on ground
        if (!mc.player.isOnGround()) return;

        // Tick delay
        if (ticks++ < delay.get()) return;
        ticks = 0;

        mc.player.jump();
    }
}
