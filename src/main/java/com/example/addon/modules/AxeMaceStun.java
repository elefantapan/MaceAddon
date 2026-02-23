package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

public class AxeMaceStun extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // SETTINGS
    private final Setting<Integer> targetSlot = sgGeneral.add(new IntSetting.Builder()
        .name("target-slot")
        .description("Hotbar slot to swap to (1–9).")
        .defaultValue(1)
        .min(1)
        .max(9)
        .sliderRange(1, 9)
        .build()
    );

    private final Setting<Integer> delayTicks = sgGeneral.add(new IntSetting.Builder()
        .name("delay-ticks")
        .description("Ticks to wait before mace hit.")
        .defaultValue(3)
        .min(0)
        .max(20)
        .build()
    );

    private final Setting<Integer> spreadTicks = sgGeneral.add(new IntSetting.Builder()
        .name("spread-ticks")
        .description("Random +/- variation added to delay.")
        .defaultValue(0)
        .min(0)
        .max(10)
        .build()
    );

    private final Setting<Boolean> autoHit = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-hit")
        .description("Automatically trigger stun when looking at a shielded target.")
        .defaultValue(false)
        .build()
    );

    // STATE
    private LivingEntity pendingTarget;
    private int ticksUntilAttack;

    public AxeMaceStun() {
        super(AddonTemplate.CATEGORY, "auto-stunslam",
            "Automatically swaps to mace and stuns shielded targets.");
    }

    // =========================
    // MANUAL ATTACK
    // =========================
    @EventHandler
    private void onAttack(AttackEntityEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (!(event.entity instanceof LivingEntity target)) return;
        if (!isHoldingAxe()) return;

        tryScheduleAttack(target);
    }

    // =========================
    // TICK
    // =========================
    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;

        // AUTO HIT
        if (autoHit.get() && pendingTarget == null && isHoldingAxe()) {
            if (mc.crosshairTarget instanceof EntityHitResult ehr) {
                if (ehr.getEntity() instanceof LivingEntity target) {
                    double reach = mc.player.getEntityInteractionRange();
                    if (mc.player.distanceTo(target) <= reach) {
                        if(pendingTarget != null) {
                            mc.interactionManager.attackEntity(mc.player, pendingTarget);
                            mc.player.swingHand(Hand.MAIN_HAND);
                            tryScheduleAttack(target);
                        }
                    }
                }
            }
        }

        // DELAYED ATTACK
        if (ticksUntilAttack > 0) {
            ticksUntilAttack--;

            if (ticksUntilAttack == 0 && pendingTarget != null) {
                InvUtils.swap(targetSlot.get(), false);
                mc.interactionManager.attackEntity(mc.player, pendingTarget);
                mc.player.swingHand(Hand.MAIN_HAND);
                pendingTarget = null;
            }
        }
    }

    // =========================
    // HELPERS
    // =========================
    private boolean isHoldingAxe() {
        return mc.player.getMainHandStack().getItem().toString().contains("_axe");
    }

    private void tryScheduleAttack(LivingEntity target) {
        if (pendingTarget != null) return;

        if (target.getActiveItem().getItem() != Items.SHIELD) return;
        
        if (mc.player.getVelocity().y < 0) {
            pendingTarget = target;

        int variation = spreadTicks.get() == 0
            ? 0
            : mc.player.getRandom().nextInt(spreadTicks.get() * 2 + 1) - spreadTicks.get();

        ticksUntilAttack = Math.max(0, delayTicks.get() + variation);
        }
    }
}
