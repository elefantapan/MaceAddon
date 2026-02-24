package com.example.addon.modules.pvp;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;

public class BreachSwap extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> autoSwap = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-swap-breach-mace")
        .description("Automatically finds and swaps to a breach mace.")
        .defaultValue(true)
        .build());

    private final Setting<Integer> targetSlot = sgGeneral.add(new IntSetting.Builder()
        .name("target-slot")
        .description("The hotbar slot to swap to when attacking.")
        .sliderRange(1, 9)
        .defaultValue(1)
        .min(1)
        .visible(() -> !autoSwap.get())
        .build());

    private final Setting<Boolean> debugMode = sgGeneral.add(new BoolSetting.Builder()
        .name("debug")
        .description("Print debug messages in chat.")
        .defaultValue(false)
        .build());

    private final Setting<Boolean> checkWeapon = sgGeneral.add(new BoolSetting.Builder()
        .name("check-weapon")
        .description("Only activate when holding a sword or axe.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> allowSword = sgGeneral.add(new BoolSetting.Builder()
        .name("allow-sword")
        .description("Allow activation when holding a sword.")
        .defaultValue(true)
        .visible(checkWeapon::get)
        .build());

    private final Setting<Boolean> allowAxe = sgGeneral.add(new BoolSetting.Builder()
        .name("allow-axe")
        .description("Allow activation when holding an axe.")
        .defaultValue(true)
        .visible(checkWeapon::get)
        .build());

    private final Setting<Boolean> swapBack = sgGeneral.add(new BoolSetting.Builder()
        .name("swap-back")
        .description("Swap back to the original slot after a short delay.")
        .defaultValue(true)
        .build());

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("swap-back-delay")
        .description("Delay in ticks before swapping back to the previous slot.")
        .sliderRange(1, 20)
        .defaultValue(8)
        .min(1)
        .visible(swapBack::get)
        .build());

    private int prevSlot = -1;
    private int dDelay = 0;

    public BreachSwap() {
        super(AddonTemplate.CATEGORY, "breach-swap", "Swaps with the breach mace in a target slot on attack");
    }

    private int findBreachMace() {
        int bestSlot = -1;
        int highestLevel = 0;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getEnchantments().size() > 0) {
                stack.getEnchantments().forEach(ench -> {
                    if (ench.id().equals("minecraft:breach")) {
                        int level = ench.level();
                        if (debugMode.get()) info("Found breach level " + level + " in slot " + i);
                        if (level > highestLevel) {
                            highestLevel = level;
                            bestSlot = i;
                        }
                    }
                });
            }
        }

        if (bestSlot != -1 && debugMode.get()) info("Selected slot " + bestSlot + " with level " + highestLevel);
        return bestSlot;
    }

    @EventHandler
    private void onAttack(AttackEntityEvent event) {
        if (mc.player == null || mc.world == null) return;

        if (checkWeapon.get()) {
            String id = mc.player.getMainHandStack().getItem().toString();
            boolean isSword = id.contains("sword");
            boolean isAxe = id.contains("_axe");
            if ((!allowSword.get() || !isSword) && (!allowAxe.get() || !isAxe)) return;
        }

        if (swapBack.get()) prevSlot = InvUtils.getSelected();

        int slotToSwap = autoSwap.get() ? findBreachMace() : targetSlot.get() - 1;

        if (slotToSwap != -1) {
            InvUtils.swap(slotToSwap, false);
            if (swapBack.get()) dDelay = delay.get();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (dDelay > 0) {
            dDelay--;
            if (dDelay == 0 && prevSlot != -1) {
                InvUtils.swap(prevSlot, false);
                prevSlot = -1;
            }
        }
    }
}
