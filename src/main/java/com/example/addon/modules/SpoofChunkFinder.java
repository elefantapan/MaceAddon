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
import net.minecraft.block.entity.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.joml.Vector3d;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
public class SpoofChunkFinder extends Module {
	private final SettingGroup sgGeneral = settings.getDefaultGroup();
	private final Setting < Integer > scanRadius;
	private final Setting < Integer > minimumStorage;
	private final Setting < Boolean > detectSpawners;
	private final Setting < SettingColor > chunkColor;
	private final Setting < Boolean > fillChunk;
	private final Setting < Boolean > showLabel;
	private final Setting < SettingColor > labelColor;
	private final Set < ChunkPos > flaggedChunks = new CopyOnWriteArraySet < > ();
	private final Map < ChunkPos, String > labels = new HashMap < > ();
	private int ticks;
	public SpoofChunkFinder() {
		super(AddonTemplate.BASE_FIND, "spoof-chunk-finder", "Finds chunks containing storage.");
		scanRadius = sgGeneral.add(new IntSetting.Builder().name("scan-radius").defaultValue(4).min(1).max(16).build());
		minimumStorage = sgGeneral.add(new IntSetting.Builder().name("minimum-storage").description("Amount of storage blocks needed.").defaultValue(5).min(1).max(200).build());
		detectSpawners = sgGeneral.add(new BoolSetting.Builder().name("detect-spawners").description("Draw instantly if spawner exists.").defaultValue(true).build());
		chunkColor = sgGeneral.add(new ColorSetting.Builder().name("chunk-color").defaultValue(new SettingColor(255, 0, 0, 80)).build());
		fillChunk = sgGeneral.add(new BoolSetting.Builder().name("fill-chunk").defaultValue(false).build());
		showLabel = sgGeneral.add(new BoolSetting.Builder().name("show-label").defaultValue(true).build());
		labelColor = sgGeneral.add(new ColorSetting.Builder().name("label-color").defaultValue(new SettingColor(255, 80, 80)).build());
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
		if(ticks >= 40) {
			ticks = 0;
			scan();
		}
	}
	private void scan() {
		MinecraftClient mc = MinecraftClient.getInstance();
		if(mc.world == null || mc.player == null) return;
		flaggedChunks.clear();
		labels.clear();
		ChunkPos center = new ChunkPos(mc.player.getBlockPos());
		for(int x = center.x - scanRadius.get(); x <= center.x + scanRadius.get(); x++) {
			for(int z = center.z - scanRadius.get(); z <= center.z + scanRadius.get(); z++) {
				WorldChunk chunk = mc.world.getChunk(x, z, ChunkStatus.FULL, false);
				if(chunk == null) continue;
				int storage = 0;
				boolean spawner = false;
				for(BlockEntity entity: chunk.getBlockEntities().values()) {
					/*
					 Any inventory/storage block
					 */
					if(entity instanceof InventoryBlockEntity) {
						storage++;
					}
					/*
					 Some storages don't directly implement
					 InventoryBlockEntity in every version,
					 so include common ones.
					 */
					if(entity instanceof ChestBlockEntity || entity instanceof BarrelBlockEntity || entity instanceof ShulkerBoxBlockEntity || entity instanceof HopperBlockEntity || entity instanceof FurnaceBlockEntity || entity instanceof BlastFurnaceBlockEntity || entity instanceof SmokerBlockEntity || entity instanceof DispenserBlockEntity || entity instanceof DropperBlockEntity) {
						storage++;
					}
					if(entity instanceof MobSpawnerBlockEntity) {
						spawner = true;
					}
				}
				boolean found = storage >= minimumStorage.get() || (detectSpawners.get() && spawner);
				if(found) {
					ChunkPos pos = new ChunkPos(x, z);
					flaggedChunks.add(pos);
					labels.put(pos, spawner ? "Spawner" : "Storage: " + storage);
				}
			}
		}
	}
	@EventHandler
	private void onRender3D(Render3DEvent event) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if(mc.world == null) return;
		for(ChunkPos pos: flaggedChunks) {
			event.renderer.box(new net.minecraft.util.math.Box(pos.getStartX(), mc.world.getBottomY(), pos.getStartZ(), pos.getEndX() + 1, mc.world.getTopY(), pos.getEndZ() + 1), chunkColor.get(), chunkColor.get(), fillChunk.get() ? ShapeMode.Both : ShapeMode.Lines, 0);
		}
	}
	@EventHandler
	private void onRender2D(Render2DEvent event) {
		if(!showLabel.get()) return;
		MinecraftClient mc = MinecraftClient.getInstance();
		for(Map.Entry < ChunkPos, String > entry: labels.entrySet()) {
			ChunkPos pos = entry.getKey();
			Vector3d vec = new Vector3d(pos.getStartX() + 8, mc.world.getTopY() + 5, pos.getStartZ() + 8);
			if(NametagUtils.to2D(vec, 1)) {
				NametagUtils.begin(vec, event.drawContext);
				String text = entry.getValue();
				event.drawContext.drawText(mc.textRenderer, text, -mc.textRenderer.getWidth(text) / 2, 0, labelColor.get().getPacked(), true);
				NametagUtils.end(event.drawContext);
			}
		}
	}
}
