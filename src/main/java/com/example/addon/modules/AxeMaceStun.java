package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AxeMaceStun extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    // Settings
    private final Setting<Integer> targetSlot = sgGeneral.add(new IntSetting.Builder()
            .name("target-slot")
            .description("The hotbar slot to swap to when attacking.")
            .sliderRange(1, 9)
            .defaultValue(1)
            .build());

    private final Setting<Integer> delayTicks = sgGeneral.add(new IntSetting.Builder()
            .name("delay-ticks")
            .description("Base ticks to wait before swapping to mace.")
            .defaultValue(3)
            .min(1)
            .max(20)
            .build());

    private final Setting<Integer> spreadTicks = sgGeneral.add(new IntSetting.Builder()
            .name("spread-ticks")
            .description("Maximum random variation (+/-) added to delay.")
            .defaultValue(0)
            .min(0)
            .max(10)
            .build());

    // Pending attack info
    private LivingEntity pendingTarget = null;
    private int ticksUntilAttack = 0;

    public AxeMaceStun() {
        super(AddonTemplate.CATEGORY, "auto-stunslam", "Automatically stunslam with delay and variation");
    }

    @EventHandler
    private void onAttack(AttackEntityEvent event) {
        if (mc.player == null || mc.world == null) return;

        String heldItemId = mc.player.getMainHandStack().getItem().toString();
        boolean isAxe = heldItemId.contains("_axe");

        if (isAxe && mc.crosshairTarget instanceof LivingEntity target) {
            boolean shieldUp = target.getActiveItem().getItem() == Items.SHIELD;
            if (shieldUp) {
                pendingTarget = target;

                // Calculate plus-minus spread delay
                int variation = mc.player.getRandom().nextInt(spreadTicks.get() * 2 + 1) - spreadTicks.get();
                ticksUntilAttack = Math.max(0, delayTicks.get() + variation);
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (ticksUntilAttack > 0) {
            ticksUntilAttack--;

            if (ticksUntilAttack == 0 && pendingTarget != null) {
                int maceSlot = targetSlot.get();
                InvUtils.swap(maceSlot, false);
                mc.interactionManager.attackEntity(mc.player, pendingTarget);
                mc.player.swingHand(Hand.MAIN_HAND);

                // Reset
                pendingTarget = null;
            }
        }
    }
}
