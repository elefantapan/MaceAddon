package com.example.addon.modules;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.render.Camera;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.SkeletonEntity;

import net.minecraft.inventory.SimpleInventory;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import net.minecraft.particle.ParticleTypes;

import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import net.minecraft.component.DataComponentTypes;

import java.util.HashSet;
import java.util.Set;

public class FakeSpawner extends Module {

    private int stored = 0;
    private final Set<BlockPos> active = new HashSet<>();

    public FakeSpawner() {
        super(null, "fake-spawner", "Iron bar spawner system");
    }

    // ---------------- INTERACT ----------------
    @EventHandler
    private void onTick(TickEvent.Pre e) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.options.useKey.isPressed()) return;

        if (!(mc.crosshairTarget instanceof BlockHitResult hit)) return;

        BlockPos pos = hit.getBlockPos();
        BlockState state = mc.world.getBlockState(pos);

        if (!state.isOf(Blocks.IRON_BARS)) return;

        if (mc.player.isSneaking()) {
            convert(pos);
        } else {
            openGui();
            active.add(pos.toImmutable());
        }
    }

    // ---------------- CONVERT ----------------
    private void convert(BlockPos start) {
        World w = mc.world;
        int count = 0;

        BlockPos.Mutable pos = start.mutableCopy();

        while (w.getBlockState(pos).isOf(Blocks.IRON_BARS)) {
            w.breakBlock(pos, false);
            count++;
            pos.move(Direction.UP);
        }

        stored += count;
        info("Stored: " + stored);
    }

    // ---------------- GUI ----------------
    private void openGui() {
        SimpleInventory inv = new SimpleInventory(54);

        ItemStack gold = new ItemStack(Items.GOLD_INGOT);
        gold.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Upgrade").formatted(Formatting.GOLD));

        ItemStack skull = new ItemStack(Items.SKELETON_SKULL);
        skull.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Skeleton Spawner"));

        ItemStack dropper = new ItemStack(Items.DROPPER);
        dropper.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Collect").formatted(Formatting.GREEN));

        inv.setStack(48, gold);
        inv.setStack(49, skull);
        inv.setStack(50, dropper);

        mc.setScreen(new GenericContainerScreen(
                new GenericContainerScreenHandler(
                        ScreenHandlerType.GENERIC_9X6,
                        0,
                        mc.player.getInventory(),
                        inv,
                        6
                ),
                mc.player.getInventory(),
                Text.literal(stored + " Skeleton Spawners")
        ));
    }

    // ---------------- RENDER ----------------
    @EventHandler
    private void onRender(Render3DEvent e) {
        if (mc.world == null) return;

        for (BlockPos pos : active) {

            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.5;
            double z = pos.getZ() + 0.5;

            // ✅ YOUR VERSION ONLY SUPPORTS THIS OVERLOAD
            mc.world.addParticle(
                    ParticleTypes.SMOKE,
                    x, y, z,
                    0.0, 0.02, 0.0
            );

            mc.world.addParticle(
                    ParticleTypes.FLAME,
                    x, y, z,
                    0.0, 0.02, 0.0
            );

            renderSkeleton(e, pos);
        }
    }

    // ---------------- FAKE MOB ----------------
    private void renderSkeleton(Render3DEvent e, BlockPos pos) {
        if (mc.world == null) return;

        SkeletonEntity s = new SkeletonEntity(EntityType.SKELETON, mc.world);

        s.setPosition(
                pos.getX() + 0.5,
                pos.getY() + 0.1,
                pos.getZ() + 0.5
        );

        s.setYaw((System.currentTimeMillis() / 10f) % 360);

        Camera cam = mc.gameRenderer.getCamera();

        // ✅ YOUR VERSION: NO getPos(), use direct coords
        double cx = cam.getX();
        double cy = cam.getY();
        double cz = cam.getZ();

        mc.getEntityRenderDispatcher().render(
                s,
                s.getX() - cx,
                s.getY() - cy,
                s.getZ() - cz,
                s.getYaw(),
                1.0f,
                e.matrices,
                mc.getBufferBuilders().getEntityVertexConsumers(),
                15728880
        );
    }
}