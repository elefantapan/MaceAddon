package com.example.addon;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Categories;

import com.example.addon.modules.AxeMaceStun;

public class AddonTemplate {
    public static final Category CATEGORY = Categories.Combat;

    public void onInitialize() {
        Modules.get().add(new AxeMaceStun());
    }
}
