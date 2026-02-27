package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AutoElytraFly extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> onlyInAir = sgGeneral.add(
        new BoolSetting.Builder()
            .name("only-in-air")
            .description("Only activate when airborne")
            .defaultValue(true)
            .build()
    );

    public AutoElytraFly() {
        super(AddonTemplate.CATEGORY, "auto-elytra-fly",
            "Automatically jumps and starts elytra flight when equipped.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        ClientPlayerEntity player = mc.player;
        if (player == null) return;
        if (mc.currentScreen != null) return;

        // Elytra equipped
        if (player.getInventory().getArmorStack(2).getItem() != Items.ELYTRA) return;

        // Already flying
        if (player.isFallFlying()) return;

        // Only in air (optional)
        if (onlyInAir.get() && player.isOnGround()) return;

        // Must be falling a bit
        if (player.getVelocity().y >= 0) return;

        // Trigger jump (required by vanilla)
        player.jump();

        // Start elytra flight
        player.startFallFlying();
    }
}
