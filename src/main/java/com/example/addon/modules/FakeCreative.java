package com.example.addon.modules;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Category;

public class FakeCreative extends Module {

    public FakeCreative() {
        super(
            Category.Misc,
            "fake-creative",
            "Makes creative mode appear client-side."
        );
    }
}
