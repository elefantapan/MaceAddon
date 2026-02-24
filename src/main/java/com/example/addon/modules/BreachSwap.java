package com.example.addon.modules.pvp;

import com.example.addon.AddonTemplate; // your addon main class
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtCompound;

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
        // Use your own PvP category from AddonTemplate
        super(AddonTemplate.CATEGORY, "breach-swap", "Swaps with the breach mace in a target slot on attack");
    }

    private int findBreachMace() {
        int bestSlot = -1;
        int highestLevel = 0;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.hasEnchantments()) {
                NbtList enchantments = stack.getEnchantments();
                for (int j = 0; j < enchantments.size(); j++) {
                    NbtCompound ench = enchantments.getCompound(j);
                    if ("minecraft:breach".equals(ench.getString("id"))) {
                        int level = ench.getInt("lvl");
                        if (debugMode.get()) info("Found breach level " + level + " in slot " + i);
                        if (level > highestLevel) {
                            highestLevel = level;
                            bestSlot = i;
                        }
                    }
                }
            }
        }

        if (bestSlot != -1 && debugMode.get()) info("Selected slot " + bestSlot + " with level " + highestLevel);
        return bestSlot;
    }

    @EventHandler
    private void onAttack(AttackEntityEvent event) {
        if (mc.player == null || mc.world == null) return;

        // Weapon type check
        if (checkWeapon.get()) {
            String id = mc.player.getMainHandStack().getItem().toString();
            boolean isSword = id.contains("sword");
            boolean isAxe = id.contains("_axe");
            if ((!allowSword.get() || !isSword) && (!allowAxe.get() || !isAxe)) return;
        }

        if (swapBack.get()) prevSlot = mc.player.getInventory().selectedSlot;

        int slotToSwap;
        if (autoSwap.get()) {
            slotToSwap = findBreachMace();
        } else {
            slotToSwap = targetSlot.get() - 1;
        }

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
