package com.example.addon;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.addons.Addon;

import com.example.addon.modules.AxeMaceStun;

public class AddonTemplate extends Addon {

    public AddonTemplate() {
        super();
    }

    @Override
    public void onInitialize() {
        // Register your module in the Combat category
        Modules.get().add(new AxeMaceStun());
    }
}
