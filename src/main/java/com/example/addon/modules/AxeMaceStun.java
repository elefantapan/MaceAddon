package com.example.addon.modules;

import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import meteordevelopment.meteorclient.utils.player.InvUtils;

import java.util.Random;

public class AxeMaceStun extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> delayTicks = sgGeneral.add(new IntSetting.Builder()
            .name("delay-ticks")
            .description("Ticks to wait before swapping to mace.")
            .defaultValue(3)
            .min(1)
            .max(10)
            .build()
    );

    private final Setting<Integer> spreadTicks = sgGeneral.add(new IntSetting.Builder()
            .name("spread-ticks")
            .description("Random variation added to delay.")
            .defaultValue(0)
            .min(0)
            .max(5)
            .build()
    );

    private final Random random = new Random();

    private int timer = 0;
    private boolean pendingSwap = false;

    public AxeMaceStun() {
        super(Category.COMBAT, "axe-mace-stun", "Swaps to mace after hitting with an axe and hits again");
    }

    @EventHandler
    private void onAttack(AttackEntityEvent event) {
        Entity target = event.target;

        // Only trigger when holding an axe
        if (mc.player.getMainHandStack().getItem() == Items.IRON_AXE) {
            // Set timer with optional spread
            int spread = random.nextInt(spreadTicks.get() + 1); // 0 to spread inclusive
            timer = delayTicks.get() + spread;
            pendingSwap = true;
        }
    }

    @Override
    public void onTick() {
        if (pendingSwap) {
            timer--;

            if (timer <= 0) {
                // Find mace slot in hotbar (replace with your desired mace item)
                int maceSlot = InvUtils.findItemSlot(Items.IRON_AXE); // example: replace with mace item
                if (maceSlot != -1) {
                    InvUtils.swap(maceSlot, false);
                    // Attack again the current target under crosshair
                    if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == net.minecraft.util.hit.HitResult.Type.ENTITY) {
                        InvUtils.attack(mc.crosshairTarget);
                    }
                }

                // Reset
                pendingSwap = false;
            }
        }
    }
}
