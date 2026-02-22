package com.example.addon;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.addons.MeteorAddon;

import com.example.addon.modules.AxeMaceStun;

public class AddonTemplate extends MeteorAddon {
    public static final Category CATEGORY = new Category("Combat");

    public void onInitialize() {
        Modules.get().add(new AxeMaceStun());
    }
}
