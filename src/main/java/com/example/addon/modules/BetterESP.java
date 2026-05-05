package com.example.addon.modules;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.orbit.EventHandler;

import meteordevelopment.meteorclient.renderer.ShapeMode;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.*;

import net.minecraft.world.chunk.WorldChunk;

import java.util.*;

public class BetterESP extends Module {

    public BetterESP() {
        super(Categories.Render, "better-esp", "Optimized vein ESP using chunk caching.");
    }

    // ---------------- SETTINGS ----------------

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("blocks")
        .defaultValue(Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE)
        .build()
    );

    private final Setting<SettingColor> airColor = sgGeneral.add(new ColorSetting.Builder()
        .name("air-color")
        .defaultValue(new SettingColor(255, 255, 255, 120))
        .build()
    );

    private final Setting<SettingColor> waterColor = sgGeneral.add(new ColorSetting.Builder()
        .name("water-color")
        .defaultValue(new SettingColor(0, 100, 255, 120))
        .build()
    );

    private final Setting<Integer> maxVeinSize = sgGeneral.add(new IntSetting.Builder()
        .name("max-vein-size")
        .defaultValue(80)
        .min(1)
        .sliderMax(500)
        .build()
    );

    private final Setting<Boolean> showAir = sgGeneral.add(new BoolSetting.Builder()
        .name("air")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showWater = sgGeneral.add(new BoolSetting.Builder()
        .name("water")
        .defaultValue(true)
        .build()
    );

    // ---------------- CACHE ----------------

    private final Set<BlockPos> airVeins = new HashSet<>();
    private final Set<BlockPos> waterVeins = new HashSet<>();

    private final Set<ChunkPos> scannedChunks = new HashSet<>();

    // ---------------- EVENTS ----------------

    @Override
    public void onActivate() {
        airVeins.clear();
        waterVeins.clear();
        scannedChunks.clear();

        // scan all loaded chunks once
        if (mc.world != null) {
            for (net.minecraft.world.chunk.Chunk chunk : meteordevelopment.meteorclient.utils.Utils.chunks()) {
                if (chunk instanceof WorldChunk wc) {
                    scanChunk(wc);
                }
            }
        }
    }

    @Override
    public void onDeactivate() {
        airVeins.clear();
        waterVeins.clear();
        scannedChunks.clear();
    }

    // Chunk load → scan ONCE
    @EventHandler
    private void onChunkLoad(ChunkDataEvent event) {
        if (!(event.chunk() instanceof WorldChunk chunk)) return;
        scanChunk(chunk);
    }

    // Block update → rescan chunk (only affected chunk)
    @EventHandler
    private void onBlockUpdate(BlockUpdateEvent event) {
        ChunkPos cp = new ChunkPos(event.pos);
        WorldChunk chunk = mc.world.getChunk(cp.x, cp.z);
        if (chunk != null) scanChunk(chunk);
    }

    // ---------------- SCANNING ----------------

    private void scanChunk(WorldChunk chunk) {
        ChunkPos cp = chunk.getPos();

        if (scannedChunks.contains(cp)) return;
        scannedChunks.add(cp);

        int startX = cp.getStartX();
        int startZ = cp.getStartZ();

        for (int y = mc.world.getBottomY(); y < mc.world.getTopY(); y++) {
            for (int dx = 0; dx < 16; dx++) {
                for (int dz = 0; dz < 16; dz++) {

                    BlockPos start = new BlockPos(startX + dx, y, startZ + dz);
                    BlockState state = mc.world.getBlockState(start);

                    if (!blocks.get().contains(state.getBlock())) continue;

                    classifyVein(start);
                }
            }
        }
    }

    // BFS but HEAVILY capped
    private void classifyVein(BlockPos start) {
        if (airVeins.contains(start) || waterVeins.contains(start)) return;

        Set<BlockPos> vein = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        boolean touchesAir = false;
        boolean touchesWater = false;

        queue.add(start);

        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            if (vein.contains(pos)) continue;

            vein.add(pos);

            if (vein.size() > maxVeinSize.get()) break;

            for (Direction dir : Direction.values()) {
                BlockPos n = pos.offset(dir);
                BlockState s = mc.world.getBlockState(n);

                if (s.isAir()) touchesAir = true;
                if (s.getFluidState().getFluid() == Fluids.WATER) touchesWater = true;

                if (blocks.get().contains(s.getBlock())) {
                    queue.add(n);
                }
            }
        }

        if (touchesAir && showAir.get()) {
            airVeins.addAll(vein);
        }

        if (touchesWater && showWater.get()) {
            waterVeins.addAll(vein);
        }
    }

    // ---------------- RENDER ----------------

    @EventHandler
    private void onRender(Render3DEvent event) {

        if (showAir.get()) {
            for (BlockPos pos : airVeins) {
                event.renderer.box(
                    pos,
                    airColor.get(),
                    airColor.get(),
                    ShapeMode.Lines,
                    0
                );
            }
        }

        if (showWater.get()) {
            for (BlockPos pos : waterVeins) {
                event.renderer.box(
                    pos,
                    waterColor.get(),
                    waterColor.get(),
                    ShapeMode.Lines,
                    0
                );
            }
        }
    }
}
