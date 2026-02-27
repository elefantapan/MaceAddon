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
        if (mc.currentScreen != null) return;

        // Elytra equipped
        if (mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() != Items.ELYTRA) return;

        // Already flying
        if (mc.player.isGliding()) return;

        // Only activate when airborne
        if (onlyInAir.get() && mc.player.isOnGround()) return;

        // Send vanilla start-flying packet
        mc.player.networkHandler.sendPacket(
            new ClientCommandC2SPacket(
                mc.player,
                ClientCommandC2SPacket.Mode.START_FALL_FLYING
            )
        );
    }
}
