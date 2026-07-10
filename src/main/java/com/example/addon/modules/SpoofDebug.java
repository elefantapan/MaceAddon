package com.example.addon.modules;

import com.example.addon.AddonTemplate;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;


public class SpoofDebug extends Module {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    private final Setting<Integer> triggerY;
    private final Setting<Integer> renderDistance;

    private boolean triggered = false;
    private boolean sequenceActive = false;
    private int sequenceTicks = 0;


    public SpoofDebug() {
        super(
            AddonTemplate.BASE_FIND,
            "spoof-debug",
            "Temporarily changes render distance when below a selected Y level."
        );


        triggerY = settings.getDefaultGroup().add(
            new IntSetting.Builder()
                .name("trigger-y")
                .description("Y level where Spoof Debug activates.")
                .defaultValue(2)
                .min(-64)
                .max(320)
                .sliderRange(-64, 100)
                .build()
        );
        renderDistance = settings.getDefaultGroup().add(
        new IntSetting.Builder()
            .name("render-distance")
            .description("Render distance to set to.")
            .defaultValue(8)
            .min(2)
            .max(32)
            .sliderRange(2, 32)
            .build()
        );
    }


    @Override
    public void onActivate() {
        triggered = false;
        sequenceActive = false;
        sequenceTicks = 0;
    }


    @Override
    public void onDeactivate() {
        disableSpawnerFinder();

        triggered = false;
        sequenceActive = false;
        sequenceTicks = 0;
    }



    @EventHandler
    private void onTick(TickEvent.Pre event) {

        if (mc.player == null)
            return;


        boolean below = mc.player.getY() < triggerY.get();


        if (!below) {
            triggered = false;
        }


        if (below && !triggered && !sequenceActive) {

            startSpoof();

            enableSpawnerFinder();


            toast(
                "Spoof Debug",
                "Base finding enabled"
            );


            triggered = true;
        }



        if (sequenceActive) {

            sequenceTicks++;


            if (sequenceTicks >= 20) {

                stopSpoof();

                sequenceActive = false;
            }
        }
    }



    private void startSpoof() {

        if (mc.options == null)
            return;


        // Lower render distance
        mc.options.getViewDistance().setValue(2);


        if (mc.worldRenderer != null)
            mc.worldRenderer.reload();


        sequenceActive = true;
        sequenceTicks = 0;
    }



    private void stopSpoof() {

        if (mc.options == null)
            return;


        // Restore render distance
        mc.options.getViewDistance().setValue(mc.options.getViewDistance().setValue(renderDistance.get());


        if (mc.worldRenderer != null)
            mc.worldRenderer.reload();
    }



    private void enableSpawnerFinder() {

        Module module = Modules.get().get("spoof-spawner-finder");


        if (module != null && !module.isActive()) {
            module.toggle();
        }
    }



    private void disableSpawnerFinder() {

        Module module = Modules.get().get("spoof-spawner-finder");


        if (module != null && module.isActive()) {
            module.toggle();
        }
    }



    private void toast(String title, String message) {

        mc.getToastManager().add(
            new SystemToast(
                SystemToast.Type.PERIODIC_NOTIFICATION,
                Text.literal(title),
                Text.literal(message)
            )
        );
    }
}
