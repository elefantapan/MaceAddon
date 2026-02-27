package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import com.example.addon.helper.SmoothAim;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class PearlCatch extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> strength = sgGeneral.add(
        new DoubleSetting.Builder()
            .name("aim-strength")
            .defaultValue(0.25)
            .range(0.05, 1.0)
            .build()
    );

    private final Setting<Integer> predictTicks = sgGeneral.add(
        new IntSetting.Builder()
            .name("predict-ticks")
            .description("How many ticks ahead to predict pearl position")
            .defaultValue(2)
            .range(1, 6)
            .build()
    );

    private EnderPearlEntity lastPearl;

    public PearlCatch() {
        super(AddonTemplate.CATEGORY,
            "pearl-catch",
            "Catches your last thrown pearl with a wind charge.");
    }

    @Override
    public void onDeactivate() {
        lastPearl = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        // Track latest pearl
        for (Entity e : mc.world.getEntities()) {
            if (e instanceof EnderPearlEntity pearl && pearl.getOwner() == mc.player) {
                lastPearl = pearl;
            }
        }

        if (lastPearl == null || !lastPearl.isAlive()) return;

        boolean holdingWindCharge =
            mc.player.getMainHandStack().getItem() == Items.WIND_CHARGE;

        boolean triedPearlOnCooldown =
            mc.player.getMainHandStack().getItem() == Items.ENDER_PEARL
            && mc.player.getItemCooldownManager().isCoolingDown(Items.ENDER_PEARL);

        if (!holdingWindCharge && !triedPearlOnCooldown) return;

        // Predict pearl position
        Vec3d predicted = lastPearl.getPos()
            .add(lastPearl.getVelocity().multiply(predictTicks.get()));

        // Smooth aim
        SmoothAim.lookAt(
            predicted,
            strength.get().floatValue(),
            true,
            true
        );

        // Throw wind charge
        if (!InvUtils.findInHotbar(Items.WIND_CHARGE).found()) return;

        InvUtils.swap(
            InvUtils.findInHotbar(Items.WIND_CHARGE).slot(),
            false
        );

        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
    }
}
