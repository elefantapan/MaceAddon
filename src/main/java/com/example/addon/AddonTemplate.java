package com.example.addon;

import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.item.Items;

public class AddonTemplate extends MeteorAddon {
    public static final Category CATEGORY = new Category("Mace", Items.MACE.getDefaultStack());

    @Override
    public void onInitialize() {
        Modules.get().add(new com.example.addon.modules.AxeMaceStun());
    }

    @Override
    public String getPackage() {
        return "com.example.addon";
    }
}
