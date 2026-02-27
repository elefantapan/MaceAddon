package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import com.example.addon.helper.SmoothAim;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AimAssist extends Module {

    public enum AxisMode {
        X_ONLY,
        Y_ONLY,
        BOTH
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<AxisMode> axis = sgGeneral.add(
        new EnumSetting.Builder<AxisMode>()
            .name("axis")
            .description("Which axis to assist")
            .defaultValue(AxisMode.BOTH)
            .build()
    );

    private final Setting<Double> strength = sgGeneral.add(
        new DoubleSetting.Builder()
            .name("strength")
            .description("How strong the aim assist is")
            .defaultValue(0.15)
            .range(0.01, 1.0)
            .build()
    );

    private final Setting<Double> range = sgGeneral.add(
        new DoubleSetting.Builder()
            .name("range")
            .description("Targeting range")
            .defaultValue(4.0)
            .range(1.0, 6.0)
            .build()
    );
    private final Setting<Boolean> oia = sgGeneral.add(
        new BoolSetting.Builder()
            .name("Only in air")
            .description("If only in air")
            .defaultValue(false)
            .build()
    );

    public AimAssist() {
        super(AddonTemplate.CATEGORY, "aim-assist",
            "Soft aim assist when holding a mace");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen != null) return;
        if (oia.get()) {
            if (mc.player.isOnGround()) return;
        }

        // Only when holding a mace
        if (mc.player.getMainHandStack().getItem() != Items.MACE) return;

        PlayerEntity target = getClosestTarget();
        if (target == null) return;

        Vec3d eyes = mc.player.getEyePos();
        Vec3d targetPos = target.getEyePos();

        Vec3d diff = targetPos.subtract(eyes);

        double dist = diff.length();
        if (dist > range.get()) return;

        // Calculate desired yaw & pitch
        double targetYaw = MathHelper.wrapDegrees(
            Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0
        );
        double targetPitch = MathHelper.wrapDegrees(
            -Math.toDegrees(Math.atan2(diff.y, Math.sqrt(diff.x * diff.x + diff.z * diff.z)))
        );

        SmoothAim.apply(
            mc.player,
            (float) targetYaw,
            (float) targetPitch,
            strength.get().floatValue(),
            axis.get() == AxisMode.X_ONLY || axis.get() == AxisMode.BOTH,
            axis.get() == AxisMode.Y_ONLY || axis.get() == AxisMode.BOTH
        );
    }

    private PlayerEntity getClosestTarget() {
        PlayerEntity best = null;
        double bestDist = Double.MAX_VALUE;

        for (var entity : mc.world.getPlayers()) {
            if (entity == mc.player) continue;
            if (entity.isDead()) continue;

            double d = mc.player.distanceTo(entity);
            if (d < bestDist) {
                bestDist = d;
                best = entity;
            }
        }

        return best;
    }
}
