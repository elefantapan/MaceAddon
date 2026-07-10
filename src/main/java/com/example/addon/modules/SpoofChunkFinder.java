package com.example.addon.modules;

import com.example.addon.AddonTemplate;

import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import org.joml.Vector3d;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;


public class SpoofChunkFinder extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> scanRadius;
    private final Setting<Integer> minInhabitedHours;
    private final Setting<Integer> playerRadius;
    private final Setting<Integer> rectangleY;

    private final Setting<SettingColor> chunkColor;
    private final Setting<Boolean> fillChunk;

    private final Setting<Boolean> showLabel;
    private final Setting<SettingColor> labelColor;


    private final Set<ChunkPos> flaggedChunks = new CopyOnWriteArraySet<>();
    private final Map<ChunkPos, String> labels = new HashMap<>();

    private int ticks;


    public SpoofChunkFinder() {
        super(
            AddonTemplate.BASE_FIND,
            "spoof-chunk-finder",
            "Finds chunks with high inhabited time."
        );


        scanRadius = sgGeneral.add(
            new IntSetting.Builder()
                .name("scan-radius")
                .description("Chunks to scan around you.")
                .defaultValue(4)
                .min(1)
                .max(16)
                .build()
        );


        minInhabitedHours = sgGeneral.add(
            new IntSetting.Builder()
                .name("min-inhabited-hours")
                .description("Minimum chunk activity time.")
                .defaultValue(12)
                .min(1)
                .max(100)
                .build()
        );


        playerRadius = sgGeneral.add(
            new IntSetting.Builder()
                .name("player-radius")
                .description("Only scan if players are nearby.")
                .defaultValue(6)
                .min(1)
                .max(32)
                .build()
        );


        rectangleY = sgGeneral.add(
            new IntSetting.Builder()
                .name("rectangle-y")
                .description("Height of chunk highlight.")
                .defaultValue(64)
                .min(-64)
                .max(320)
                .build()
        );


        chunkColor = sgGeneral.add(
            new ColorSetting.Builder()
                .name("chunk-color")
                .defaultValue(new SettingColor(255, 30, 30, 80))
                .build()
        );


        fillChunk = sgGeneral.add(
            new BoolSetting.Builder()
                .name("fill-chunk")
                .defaultValue(false)
                .build()
        );


        showLabel = sgGeneral.add(
            new BoolSetting.Builder()
                .name("show-label")
                .defaultValue(true)
                .build()
        );


        labelColor = sgGeneral.add(
            new ColorSetting.Builder()
                .name("label-color")
                .defaultValue(new SettingColor(255,80,80,255))
                .build()
        );
    }



    @Override
    public void onActivate() {
        flaggedChunks.clear();
        labels.clear();
    }



    @Override
    public void onDeactivate() {
        flaggedChunks.clear();
        labels.clear();
    }



    @EventHandler
    private void onTick(TickEvent.Pre event) {

        ticks++;

        if (ticks >= 100) {
            ticks = 0;
            scan();
        }
    }



    private void scan() {

        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.world == null || mc.player == null)
            return;


        flaggedChunks.clear();
        labels.clear();


        ChunkPos center = new ChunkPos(mc.player.getBlockPos());


        for (int x = center.x - scanRadius.get(); x <= center.x + scanRadius.get(); x++) {

            for (int z = center.z - scanRadius.get(); z <= center.z + scanRadius.get(); z++) {


                var chunk = mc.world.getChunk(x,z);


                long inhabited = chunk.getInhabitedTime();


                long needed =
                    minInhabitedHours.get()
                    * 60L
                    * 60L
                    * 20L;


                if (inhabited >= needed) {

                    ChunkPos pos = new ChunkPos(x,z);

                    flaggedChunks.add(pos);


                    double hours =
                        inhabited / 72000.0;


                    labels.put(
                        pos,
                        String.format(
                            "Possible Base %.1fh",
                            hours
                        )
                    );


                    toast(
                        "Spoof Chunk Finder",
                        "Possible base chunk found"
                    );
                }
            }
        }
    }




    @EventHandler
    private void onRender3D(Render3DEvent event) {

        MinecraftClient mc = MinecraftClient.getInstance();


        for (ChunkPos pos : flaggedChunks) {


            double x1 = pos.getStartX();
            double z1 = pos.getStartZ();

            double x2 = pos.getEndX()+1;
            double z2 = pos.getEndZ()+1;


            double y = rectangleY.get();


            event.renderer.box(
                new net.minecraft.util.math.Box(
                    x1,y,z1,
                    x2,y+1,z2
                ),
                chunkColor.get(),
                chunkColor.get(),
                fillChunk.get()
                    ? ShapeMode.Both
                    : ShapeMode.Lines,
                0
            );
        }
    }



    @EventHandler
    private void onRender2D(Render2DEvent event) {

        if (!showLabel.get())
            return;


        MinecraftClient mc = MinecraftClient.getInstance();


        for (Map.Entry<ChunkPos,String> entry : labels.entrySet()) {


            ChunkPos pos = entry.getKey();


            Vector3d vec = new Vector3d(
                pos.getStartX()+8,
                rectangleY.get()+2,
                pos.getStartZ()+8
            );


            if (NametagUtils.to2D(vec,1)) {

                NametagUtils.begin(
                    vec,
                    event.drawContext
                );


                String text = entry.getValue();


                event.drawContext.drawText(
                    mc.textRenderer,
                    text,
                    -mc.textRenderer.getWidth(text)/2,
                    0,
                    labelColor.get().getPacked(),
                    true
                );


                NametagUtils.end(event.drawContext);
            }
        }
    }



    private void toast(String title,String message) {

        MinecraftClient.getInstance()
            .getToastManager()
            .add(
                new SystemToast(
                    SystemToast.Type.PERIODIC_NOTIFICATION,
                    Text.literal(title),
                    Text.literal(message)
                )
            );
    }
}
