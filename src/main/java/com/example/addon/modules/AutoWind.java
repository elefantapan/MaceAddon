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
        .description("Delay before jumping after using wind charge (ticks)")
        .defaultValue(0)
        .range(0, 20)
        .build()
    );

    private int ticks;
    private boolean used;

    public AutoWind() {
        super(AddonTemplate.CATEGORY, "auto-wind", "Jumps when a wind charge is used downward");
    }

    @Override
    public void onActivate() {
        ticks = 0;
        used = false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        // Must be holding wind charge
        if (mc.player.getMainHandStack().getItem() != Items.WIND_CHARGE) {
            used = false;
            ticks = 0;
            return;
        }

        // Detect actual use (right-click)
        if (mc.options.useKey.isPressed()) {
            used = true;
        }

        if (!used) return;

        // Looking down
        if (mc.player.getPitch() <= 60f)  {
            used = false;
            return;
        }

        // Must be on ground
        if (!mc.player.isOnGround()) {
            used = false;
            return;
        }

        // Optional delay
        if (ticks++ < delay.get()) return;

        ticks = 0;
        used = false;

        mc.player.jump();
    }
}
