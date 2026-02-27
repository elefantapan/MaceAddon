package com.example.addon;

import com.example.addon.modules.AxeMaceStun;
import com.example.addon.modules.AutoWind;
import com.example.addon.modules.ShieldBreaker;
import com.example.addon.modules.AutoElytraFly;
import com.example.addon.modules.AimAssist;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class AddonTemplate extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("Mace");
    public static final HudGroup HUD_GROUP = new HudGroup("Mace");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Meteor Addon Template");

        // Modules
        Modules.get().add(new AxeMaceStun());
        Modules.get().add(new AutoWind());
        Modules.get().add(new ShieldBreaker());
        Modules.get().add(new AimAssist());
        Modules.get().add(new AutoElytraFly());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.example.addon";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("MeteorDevelopment", "meteor-addon-template");
    }
}
