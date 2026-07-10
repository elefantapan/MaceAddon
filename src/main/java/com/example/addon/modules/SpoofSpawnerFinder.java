package com.example.addon.modules;
import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import org.joml.Vector3d;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
public class SpoofSpawnerFinder extends Module {
	private final SettingGroup sgGeneral = settings.getDefaultGroup();
	private final Setting < SettingColor > spawnerColor;
	private final Setting < Boolean > fillChunk;
	private final Setting < Boolean > showNametag;
	private final Setting < SettingColor > nametagColor;
	private final Setting < Double > nametagScale;
	private final Set < ChunkPos > spawnerChunks = new CopyOnWriteArraySet < > ();
	private final Set < ChunkPos > alertedChunks = new HashSet < > ();
	private final Map < BlockPos, String > spawnerLabels = new HashMap < > ();
	private int ticks;
	public SpoofSpawnerFinder() {
		super(AddonTemplate.BASE_FIND, "spoof-spawner-finder", "Finds mob spawners nearby.");
		spawnerColor = sgGeneral.add(new ColorSetting.Builder().name("spawner-color").description("Spawner chunk color.").defaultValue(new SettingColor(255, 0, 0, 100)).build());
		fillChunk = sgGeneral.add(new BoolSetting.Builder().name("fill-chunk").description("Fill chunk instead of outline.").defaultValue(false).build());
		showNametag = sgGeneral.add(new BoolSetting.Builder().name("show-nametag").description("Show spawner mob name.").defaultValue(true).build());
		nametagColor = sgGeneral.add(new ColorSetting.Builder().name("nametag-color").defaultValue(new SettingColor(255, 255, 255, 255)).build());
		nametagScale = sgGeneral.add(new DoubleSetting.Builder().name("nametag-scale").defaultValue(1).min(0.1).sliderMax(5).build());
	}
	@Override
	public void onActivate() {
		spawnerChunks.clear();
		alertedChunks.clear();
		spawnerLabels.clear();
	}
	@Override
	public void onDeactivate() {
		spawnerChunks.clear();
		alertedChunks.clear();
		spawnerLabels.clear();
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
		ChunkPos playerChunk = new ChunkPos(mc.player.getBlockPos());
		for(int cx = playerChunk.x - 4; cx <= playerChunk.x + 4; cx++) {
			for(int cz = playerChunk.z - 4; cz <= playerChunk.z + 4; cz++) {
				ChunkPos chunkPos = new ChunkPos(cx, cz);
				if(alertedChunks.contains(chunkPos)) continue;
				for(int x = cx * 16; x < cx * 16 + 16; x++) {
					for(int z = cz * 16; z < cz * 16 + 16; z++) {
						for(int y = mc.world.getBottomY(); y < mc.world.getBottomY() + mc.world.getHeight(); y++) {
							BlockPos pos = new BlockPos(x, y, z);
							if(mc.world.getBlockState(pos).isOf(Blocks.SPAWNER)) {
								BlockEntity be = mc.world.getBlockEntity(pos);
								String mob = "Unknown";
								if(be instanceof MobSpawnerBlockEntity spawner) {
									EntityType < ? > type = spawner.getLogic().getRenderedEntity(mc.world, pos) != null ? spawner.getLogic().getRenderedEntity(mc.world, pos).getType() : null;
									if(type != null) {
										mob = type.getName().getString();
									}
								}
								spawnerChunks.add(chunkPos);
								spawnerLabels.put(pos, mob + " Spawner");
								if(!alertedChunks.contains(chunkPos)) {
									alertedChunks.add(chunkPos);
									toast("Spoof Spawner Finder", "Found " + mob + " spawner");
									info("Found " + mob + " spawner at " + pos);
								}
							}
						}
					}
				}
			}
		}
	}
	@EventHandler
	private void onRender3D(Render3DEvent event) {
		for(ChunkPos pos: spawnerChunks) {
			Box box = new Box(pos.getStartX(), 0, pos.getStartZ(), pos.getEndX() + 1, 128, pos.getEndZ() + 1);
			event.renderer.box(box, spawnerColor.get(), spawnerColor.get(), fillChunk.get() ? ShapeMode.Both : ShapeMode.Lines, 0);
		}
	}
	@EventHandler
	private void onRender2D(Render2DEvent event) {
		if(!showNametag.get()) return;
		MinecraftClient mc = MinecraftClient.getInstance();
		for(Map.Entry < BlockPos, String > entry: spawnerLabels.entrySet()) {
			BlockPos pos = entry.getKey();
			Vector3d vec = new Vector3d(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5);
			if(NametagUtils.to2D(vec, nametagScale.get())) {
				NametagUtils.begin(vec, event.drawContext);
				String text = entry.getValue();
				event.drawContext.drawText(mc.textRenderer, text, -mc.textRenderer.getWidth(text) / 2, 0, nametagColor.get().getPacked(), true);
				NametagUtils.end(event.drawContext);
			}
		}
	}
	private void toast(String title, String message) {
		MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, Text.literal(title), Text.literal(message)));
	}
}
