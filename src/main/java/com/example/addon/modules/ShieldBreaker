package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class ShieldBreaker extends Module {

    public ShieldBreaker() {
        super(AddonTemplate.CATEGORY, "shield-breaker",
            "Auto attacks players blocking with shields while airborne using a non-wind-burst mace");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen != null) return;

        // Must be in air
        if (mc.player.isOnGround()) return;

        ItemStack stack = mc.player.getMainHandStack();

        // Must be holding a mace
        if (stack.isEmpty() || stack.getItem() != Items.MACE) return;

        // Must NOT have Wind Burst enchant
        if (EnchantmentHelper.getEnchantments(stack)
            .toString().contains("wind_burst")) return;

        // Raycast target
        HitResult hit = mc.crosshairTarget;
        if (!(hit instanceof EntityHitResult ehr)) return;

        if (!(ehr.getEntity() instanceof PlayerEntity target)) return;

        // Target must be blocking with shield
        if (!target.isBlocking()) return;

        // ✅ All conditions met → attack
        mc.doAttack();
    }
}
