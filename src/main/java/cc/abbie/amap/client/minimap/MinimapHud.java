package cc.abbie.amap.client.minimap;

import cc.abbie.amap.client.FillBatcher;
import cc.abbie.amap.client.MapStorage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import folk.sisby.surveyor.terrain.LayerSummary;
import folk.sisby.surveyor.terrain.RegionSummary;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public class MinimapHud implements HudRenderCallback {
    public static int scale = 0;
    public static final int minScale = -2;
    public static final int maxScale = 2;
    public static boolean rotate = true;

    @Override
    public void onHudRender(GuiGraphics gui, float tickDelta) {
        PoseStack pose = gui.pose();
        Minecraft client = Minecraft.getInstance();
        Window window = client.getWindow();
        int windowHeight = window.getGuiScaledHeight();
        int windowWidth = window.getGuiScaledWidth();

        int mapWidth = 100;
        int mapHeight = 100;

        int offsetX = 5;
        int offsetY = 5;

        int minX = windowWidth - mapWidth - offsetX;
        int minY = offsetY;
        int maxX = windowWidth - offsetX;
        int maxY = mapHeight + offsetY;

        int mapCentreX = minX + mapWidth / 2;
        int mapCentreY = minY + mapHeight / 2;

        float realScale = (float) Math.pow(2, scale);
        int renderRadius = (int) (4 / realScale);

        gui.enableScissor(minX, minY, maxX, maxY);
        pose.pushPose();
            pose.translate(minX, minY, 0);

            gui.fill(0, 0, mapWidth, mapHeight, 0xff000000);

            float rot;
            ChunkPos playerChunkPos;
            Vec3 playerPos;
            if (client.player != null) {
                rot = client.player.getViewYRot(tickDelta);
                playerPos = client.player.getEyePosition(tickDelta);
                playerChunkPos = new ChunkPos((int) (playerPos.x / 16), (int) (playerPos.z / 16));
            } else {
                rot = 0f;
                playerChunkPos = ChunkPos.ZERO;
                playerPos = Vec3.ZERO;
            }

            pose.pushPose();
                pose.translate(mapWidth / 2f, mapHeight / 2f, 0);
                pose.scale(realScale, realScale, 1);
                if (rotate) {
                    pose.rotateAround(Axis.ZN.rotationDegrees(rot + 180f), 0, 0, 0);
                }
                pose.translate(-playerPos.x % 16, -playerPos.z % 16, 0);
                for (int x = -renderRadius; x < renderRadius; x++) {
                    for (int y = -renderRadius; y < renderRadius; y++) {
                        pose.pushPose();
                            pose.translate(x * 16, y * 16, 0);
                            renderChunk(gui, new ChunkPos(playerChunkPos.x + x, playerChunkPos.z + y));
                        pose.popPose();
                    }
                }
            pose.popPose();

            pose.pushPose();
                pose.translate(mapWidth / 2f, mapHeight / 2f, 0);
                if (!rotate) {
                    pose.rotateAround(Axis.ZP.rotationDegrees(rot + 180f), 0, 0, 0);
                }
                pose.translate(-0.5f, 0, 0);
                gui.blit(new ResourceLocation("textures/map/map_icons.png"), -4, -4, 16, 0, 8, 8, 128, 128);
            pose.popPose();


        pose.popPose();
        gui.disableScissor();
    }

    public static void zoomOut() {
        if (scale > minScale) scale--;
    }

    public static void zoomIn() {
        if (scale < maxScale) scale++;
    }

    private void renderChunk(GuiGraphics gui, ChunkPos chunkPos) {
        ChunkPos regionPos = new ChunkPos(RegionSummary.chunkToRegion(chunkPos.x), RegionSummary.chunkToRegion(chunkPos.z));
        ChunkPos regionRelativePos = new ChunkPos(RegionSummary.regionRelative(chunkPos.x), RegionSummary.regionRelative(chunkPos.z));

        LayerSummary.Raw[][] terr = MapStorage.INSTANCE.terrain.get(regionPos);
        var blockPalette = MapStorage.INSTANCE.blockPalettes.get(chunkPos);
        var biomePalette = MapStorage.INSTANCE.biomePalettes.get(chunkPos);

        if (terr == null || blockPalette == null || biomePalette == null) return;

        var summ = terr[regionRelativePos.x][regionRelativePos.z];
        if (summ == null) return;

        var blocks = summ.blocks();
        var biomes = summ.biomes();
        if (blocks == null || biomes == null) return;

        FillBatcher fillBatcher = new FillBatcher(gui);
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                int idx = 16 * x + y;
                Block block = blockPalette.byId(blocks[idx]);
                Biome biome = biomePalette.byId(biomes[idx]);
                if (block == null || biome == null) continue;
                int colour;
                if (summ.waterDepths()[idx] > 0) {
                    colour = biome.getWaterColor() | 0xff000000;
                } else {
                    colour = block.defaultMapColor().col | 0xff000000;
                }
                fillBatcher.add(x, y, x + 1, y + 1, 0, colour);
            }
        }
        fillBatcher.close();
    }
}
