package com.example.addon;

import meteordevelopment.meteorclient.addons.MeteorAddon;
import com.example.addon.modules.AxeMaceStun;

public class AddonTemplate extends MeteorAddon {
    public static final String CATEGORY = "Combat"; // example category

    @Override
    public void onInitialize() {
        // Register your modules
        new AxeMaceStun();
    }

    @Override
    public String getPackage() {
        return "com.example.addon"; // your base package name
    }
}
