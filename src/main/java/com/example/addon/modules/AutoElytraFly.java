package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;

public class AutoElytraFly extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> onlyInAir = sgGeneral.add(
        new BoolSetting.Builder()
            .name("only-in-air")
            .defaultValue(true)
            .build()
    );

    public AutoElytraFly() {
        super(AddonTemplate.CATEGORY,
            "auto-elytra-fly",
            "Automatically starts elytra flight when airborne.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
    
        // Only if wearing elytra
        if (mc.player.getInventory().getArmorStack(2).getItem() != Items.ELYTRA) return;
    
        // Only start flying if in air and not already flying
        if (!mc.player.isFallFlying() && !mc.player.isOnGround()) {
            mc.player.startFallFlying();
        }
    }
}
