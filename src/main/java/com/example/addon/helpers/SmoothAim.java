package com.example.addon.helper;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;

public class SmoothAim {

    public static void apply(
        ClientPlayerEntity player,
        float targetYaw,
        float targetPitch,
        float strength,
        boolean assistYaw,
        boolean assistPitch
    ) {
        float yaw = player.getYaw();
        float pitch = player.getPitch();

        float yawDiff = MathHelper.wrapDegrees(targetYaw - yaw);
        float pitchDiff = MathHelper.wrapDegrees(targetPitch - pitch);

        // Exponential smoothing (feels legit)
        float yawStep = yawDiff * strength;
        float pitchStep = pitchDiff * strength;

        if (assistYaw) {
            yaw += yawStep;
        }

        if (assistPitch) {
            pitch += pitchStep;
        }

        player.setYaw(yaw);
        player.setPitch(MathHelper.clamp(pitch, -90f, 90f));
    }
}
