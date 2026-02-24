package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.ColorSetting;
import net.minecraft.item.Items;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.events.entity.player.InteractItemEvent
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class AutoWind extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgRender = this.settings.createGroup("Render");

    /**
     * Example setting.
     * The {@code name} parameter should be in kebab-case.
     * If you want to access the setting from another class, simply make the setting {@code public}, and use
     * {@link meteordevelopment.meteorclient.systems.modules.Modules#get(Class)} to access the {@link Module} object.
     */
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("tick-delay")
        .description("The delay between jumping (In ticks)")
        .defaultValue(2)
        .range(0, 20)
        .build()
    );

    /**
     * The {@code name} parameter should be in kebab-case.
     */
    public AutoWind() {
        super(AddonTemplate.CATEGORY, "Auto jump", "Auto jump when using windcharge downwards");
    }

    /**
     * Example event handling method.
     * Requires {@link AddonTemplate#getPackage()} to be setup correctly, otherwise the game will crash whenever the module is enabled.
     */
    @EventHandler
    private void onItemUse(ItemUseEvent event) {
        if (mc.player == null) return;
    
        if (event.itemStack.getItem() != Items.WIND_CHARGE) return;
    
        float pitch = mc.player.getPitch();
    
        if (pitch > 60f) {
            if (mc.player != null && mc.player.isOnGround()) {
               if (ticks < 20) {
                    ticks++;
                    return; // still waiting
                }
            
                ticks = 0; // reset
                mc.player.jump();
            }
        }
    }
}
