package com.example.addon;

import com.example.addon.modules.AxeMaceStun;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.Category;

public class AddonTemplate {

    public static final Category CATEGORY = Category.COMBAT;

    public void onInitialize() {
        Modules.get().add(new AxeMaceStun());
    }
}
