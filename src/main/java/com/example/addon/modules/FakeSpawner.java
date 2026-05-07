package com.example.addon.modules;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
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
import net.minecraft.client.render.Camera;

import java.util.HashSet;
import java.util.Set;

public class FakeSpawner extends Module {

    private int storedSpawners = 0;
    private final Set<BlockPos> activeSpawners = new HashSet<>();

    public FakeSpawner() {
        super(null, "fake-spawner", "Chain → fake spawner system");
    }

    // -----------------------------
    // INTERACTION LOGIC
    // -----------------------------
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.options.useKey.isPressed()) return;

        if (!(mc.crosshairTarget instanceof BlockHitResult hit)) return;

        BlockPos pos = hit.getBlockPos();
        BlockState state = mc.world.getBlockState(pos);

        if (!state.isOf(Blocks.CHAIN)) return;

        if (mc.player.isSneaking()) {
            convertChainStack(pos);
        } else {
            openGui();
            activeSpawners.add(pos.toImmutable());
        }
    }

    // -----------------------------
    // CONVERT CHAIN STACK → STORAGE
    // -----------------------------
    private void convertChainStack(BlockPos start) {
        if (mc.world == null) return;

        World world = mc.world;
        int count = 0;

        BlockPos.Mutable pos = start.mutableCopy();

        while (world.getBlockState(pos).isOf(Blocks.CHAIN)) {
            world.breakBlock(pos, false);
            count++;
            pos.move(Direction.UP);
        }

        storedSpawners += count;
        info("Stored spawners: " + storedSpawners);
    }

    // -----------------------------
    // FAKE GUI
    // -----------------------------
    private void openGui() {
        SimpleInventory inv = new SimpleInventory(54);

        ItemStack gold = new ItemStack(Items.GOLD_INGOT);
        gold.setCustomName(Text.literal("Upgrade").formatted(Formatting.GOLD));

        ItemStack skull = new ItemStack(Items.SKELETON_SKULL);
        skull.setCustomName(Text.literal("Skeleton Spawner"));

        ItemStack dropper = new ItemStack(Items.DROPPER);
        dropper.setCustomName(Text.literal("Collect").formatted(Formatting.GREEN));

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
            Text.literal(storedSpawners + " Skeleton Spawners")
        ));
    }

    // -----------------------------
    // RENDER LOOP
    // -----------------------------
    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.world == null) return;

        for (BlockPos pos : activeSpawners) {

            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.5;
            double z = pos.getZ() + 0.5;

            mc.world.addParticle(
                ParticleTypes.SMOKE,
                x, y, z,
                0, 0.02, 0
            );

            mc.world.addParticle(
                ParticleTypes.FLAME,
                x, y, z,
                0, 0.02, 0
            );

            renderSkeleton(event, pos);
        }
    }

    // -----------------------------
    // FAKE SPINNER SKELETON
    // -----------------------------
    private void renderSkeleton(Render3DEvent event, BlockPos pos) {
        if (mc.world == null) return;

        SkeletonEntity skeleton = new SkeletonEntity(EntityType.SKELETON, mc.world);

        skeleton.setPosition(
            pos.getX() + 0.5,
            pos.getY() + 0.1,
            pos.getZ() + 0.5
        );

        skeleton.setYaw((System.currentTimeMillis() / 10f) % 360);

        Camera cam = mc.gameRenderer.getCamera();
        var camPos = cam.getPos();

        mc.getEntityRenderDispatcher().render(
            skeleton,
            skeleton.getX() - camPos.x,
            skeleton.getY() - camPos.y,
            skeleton.getZ() - camPos.z,
            skeleton.getYaw(),
            1.0f,
            event.matrices,
            mc.getBufferBuilders().getEntityVertexConsumers(),
            15728880
        );
    }
}