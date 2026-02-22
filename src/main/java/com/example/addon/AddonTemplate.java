package com.example.addon;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Category;
import com.example.addon.modules.AxeMaceStun;

public class AddonTemplate {

    // Custom category (optional, otherwise use Category.Combat)
    public static final Category CATEGORY = new Category("Custom Combat");

    public void onInitialize() {
        // Register your modules
        Modules.add(new AxeMaceStun());
    }
}
