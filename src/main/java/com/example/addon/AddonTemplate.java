package com.example.addon;

import com.example.addon.modules.AxeMaceStun;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.Category;

public class AddonTemplate {

    public static final Category CATEGORY = Category.Combat; // or your custom category

    public void onInitialize() {
        // You must get the Modules instance from the MeteorClient instance
        Modules.get().add(new AxeMaceStun());
    }
}
