package com.example.addon;

import com.example.addon.modules.AxeMaceStun;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Modules;

public class AddonTemplate extends MeteorAddon {

    @Override
    public void onInitialize() {
        Modules.get().add(new AxeMaceStun());
    }

    @Override
    public String getPackage() {
        return "com.example.addon";
    }
}
