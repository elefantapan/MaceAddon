package com.example.addon.modules;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.settings.SettingGroup;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import meteordevelopment.meteorclient.utils.render.color.ShapeMode;

import java.util.*;

public class BetterESP extends Module {
    public BetterESP() {
        super(Categories.Render, "better-esp", "Highlights veins touching air or water with configurable rendering.");
    }

    public enum RenderMode {
        Fill,
        Wireframe,
        Both
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("blocks")
        .description("Blocks to highlight.")
        .defaultValue(Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE)
        .build()
    );

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("Scan range.")
        .defaultValue(32)
        .min(1)
        .sliderMax(128)
        .build()
    );

    private final Setting<Integer> leniency = sgGeneral.add(new IntSetting.Builder()
        .name("leniency")
        .description("How far from air/water blocks can be and still render.")
        .defaultValue(0)
        .min(0)
        .sliderMax(10)
        .build()
    );

    private final Setting<SettingColor> airColor = sgGeneral.add(new ColorSetting.Builder()
        .name("air-color")
        .description("Color when touching air.")
        .defaultValue(new SettingColor(255, 255, 255, 120))
        .build()
    );

    private final Setting<SettingColor> waterColor = sgGeneral.add(new ColorSetting.Builder()
        .name("water-color")
        .description("Color when touching water.")
        .defaultValue(new SettingColor(0, 100, 255, 120))
        .build()
    );

    private final Setting<RenderMode> renderMode = sgGeneral.add(new EnumSetting.Builder<RenderMode>()
        .name("render-mode")
        .description("How the boxes are rendered.")
        .defaultValue(RenderMode.Wireframe)
        .build()
    );

    private final Setting<Double> lineWidth = sgGeneral.add(new DoubleSetting.Builder()
        .name("line-width")
        .description("Width of wireframe lines.")
        .defaultValue(1.5)
        .min(0.1)
        .sliderMax(5)
        .visible(() -> renderMode.get() != RenderMode.Fill)
        .build()
    );

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.player == null || mc.world == null) return;

        BlockPos playerPos = mc.player.getBlockPos();
        int r = range.get();

        Set<BlockPos> visited = new HashSet<>();
        Map<BlockPos, SettingColor> renderMap = new HashMap<>();

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = playerPos.add(x, y, z);

                    if (visited.contains(pos)) continue;

                    BlockState state = mc.world.getBlockState(pos);
                    if (!blocks.get().contains(state.getBlock())) continue;

                    Set<BlockPos> vein = new HashSet<>();
                    Queue<BlockPos> queue = new LinkedList<>();
                    queue.add(pos);

                    boolean touchesAir = false;
                    boolean touchesWater = false;

                    Map<BlockPos, Integer> distance = new HashMap<>();
                    distance.put(pos, 0);

                    while (!queue.isEmpty()) {
                        BlockPos current = queue.poll();
                        if (visited.contains(current)) continue;

                        visited.add(current);
                        vein.add(current);

                        int dist = distance.get(current);

                        for (Direction dir : Direction.values()) {
                            BlockPos neighbor = current.offset(dir);
                            BlockState neighborState = mc.world.getBlockState(neighbor);

                            if (neighborState.isAir()) {
                                if (dist <= leniency.get()) touchesAir = true;
                            }

                            if (neighborState.getFluidState().getFluid() == Fluids.WATER) {
                                if (dist <= leniency.get()) touchesWater = true;
                            }

                            if (!visited.contains(neighbor)
                                && blocks.get().contains(neighborState.getBlock())) {

                                distance.put(neighbor, dist + 1);
                                queue.add(neighbor);
                            }
                        }
                    }

                    SettingColor color = null;
                    if (touchesWater) color = waterColor.get();
                    else if (touchesAir) color = airColor.get();

                    if (color != null) {
                        for (BlockPos p : vein) {
                            renderMap.put(p, color);
                        }
                    }
                }
            }
        }
        for (Map.Entry<BlockPos, SettingColor> entry : renderMap.entrySet()) {
    BlockPos pos = entry.getKey();
    SettingColor color = entry.getValue();

    switch (renderMode.get()) {
        case Fill -> {
            event.renderer.box(
                pos,
                color,
                color,
                ShapeMode.Sides,
                0
            );
        }

        case Wireframe -> {
            event.renderer.box(
                pos,
                color,
                color,
                ShapeMode.Lines,
                0
            );
        }

        case Both -> {
            event.renderer.box(
                pos,
                color,
                color,
                ShapeMode.Both,
                0
            );
        }
    }
}
    }
}
