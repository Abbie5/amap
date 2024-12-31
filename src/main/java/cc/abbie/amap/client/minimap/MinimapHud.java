package cc.abbie.amap.client.minimap;

import cc.abbie.amap.AMap;
import cc.abbie.amap.client.ChunkRenderer;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

public class MinimapHud implements HudRenderCallback {
    public static boolean enable = true;
    public static int scale = 0;
    public static final int minScale = -2;
    public static final int maxScale = 2;
    public static boolean rotate = true;
    public static boolean renderBackground = false;
    public static boolean renderFrame = true;
    public static boolean renderCrosshair = true;
    private static final int crosshairColour = 0xa0a0a0a0;
    public static Position position = Position.TOP_RIGHT;
    public static boolean renderArrowWhenRotate = false;
    public static int mapWidth = 100;
    public static int mapHeight = 100;
    public static int offsetX = 5;
    public static int offsetY = 5;
    public static boolean renderCompass = true;
    public static boolean roundMap = true;

    @Override
    public void onHudRender(GuiGraphics gui, float tickDelta) {
        if (!enable) return;

        PoseStack pose = gui.pose();
        Minecraft client = Minecraft.getInstance();
        Window window = client.getWindow();
        int windowHeight = window.getGuiScaledHeight();
        int windowWidth = window.getGuiScaledWidth();

        int minX, minY;

        int infoLinesHeight = InfoRenderer.getHeight();

        switch (position) {
            case TOP_LEFT -> {
                minX = offsetX;
                minY = offsetY;
            }
            case TOP_RIGHT -> {
                minX = windowWidth - mapWidth - offsetX;
                minY = offsetY;
            }
            case BOTTOM_LEFT -> {
                minX = offsetX;
                minY = windowHeight - mapHeight - 2 * offsetY - infoLinesHeight; // extra added for info lines
            }
            case BOTTOM_RIGHT -> {
                minX = windowWidth - mapWidth - offsetX;
                minY = windowHeight - mapHeight - 2 * offsetY - infoLinesHeight;
            }
            default -> throw new AssertionError();
        }

        int maxX = minX + mapWidth;
        int maxY = minY + mapHeight;

        int mapCentreX = minX + mapWidth / 2;
        int mapCentreY = minY + mapHeight / 2;

        float realScale = (float) Math.pow(2, scale);
        int renderRadius = (int) (4 / realScale);

        pose.pushPose();
        {
            pose.translate(minX, minY, -100);

            if (renderBackground) {
                gui.fill(0, 0, mapWidth, mapHeight, 0xff000000);
            }

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

            // scissor: prevent chunks from rendering outside map bounds
            gui.enableScissor(minX, minY, maxX, maxY);
            pose.pushPose();
            {
                pose.translate(mapWidth / 2f, mapHeight / 2f, 0);
                pose.scale(realScale, realScale, 1);
                if (rotate) {
                    pose.rotateAround(Axis.ZN.rotationDegrees(rot + 180f), 0, 0, 0);
                }
                pose.translate(-playerPos.x % 16, -playerPos.z % 16, 0);
                for (int x = -renderRadius - 1; x < renderRadius; x++) {
                    for (int y = -renderRadius - 1; y < renderRadius; y++) {
                        pose.pushPose();
                        {
                            pose.translate(x * 16, y * 16, 0);
                            ChunkRenderer.renderChunk(gui, new ChunkPos(playerChunkPos.x + x, playerChunkPos.z + y));
                        }
                        pose.popPose();
                    }
                }
            }
            pose.popPose();
            gui.disableScissor();

            if (renderCrosshair) {
                pose.pushPose();
                {
                    pose.translate(-0.5f, 0, 0);
                    gui.vLine(mapWidth / 2, -1, mapHeight, crosshairColour);
                }
                pose.popPose();
                pose.pushPose();
                {
                    pose.translate(0, -0.5f, 0);
                    gui.hLine(0, mapWidth - 1, mapHeight / 2, crosshairColour);
                }
                pose.popPose();
            }

            Font font = Minecraft.getInstance().font;
            int lineHeight = font.lineHeight;

            if (renderCompass) {
                if (rotate) {
                    if (roundMap) {
                        pose.pushPose();

                        pose.translate(mapWidth / 2f, mapHeight / 2f - lineHeight / 2f + 1f, 0);

                        float x = Mth.sin(rot * Mth.DEG_TO_RAD) * (mapWidth - lineHeight - 1) / 2f;
                        float y = Mth.cos(rot * Mth.DEG_TO_RAD) * (mapHeight - lineHeight - 1) / 2f;

                        pose.pushPose();
                        pose.translate(x, y, 0);
                        gui.drawCenteredString(font, "N", 0, 0, 0xffff0000);
                        pose.popPose();

                        pose.pushPose();
                        pose.translate(-y, x, 0);
                        gui.drawCenteredString(font, "E", 0, 0, -1);
                        pose.popPose();

                        pose.pushPose();
                        pose.translate(-x, -y, 0);
                        gui.drawCenteredString(font, "S", 0, 0, -1);
                        pose.popPose();

                        pose.pushPose();
                        pose.translate(y, -x, 0);
                        gui.drawCenteredString(font, "W", 0, 0, -1);
                        pose.popPose();

                        pose.popPose();
                    } else {
                        // TODO
                    }
                } else {
                    gui.drawCenteredString(font, "N", mapWidth / 2, 1, -1);
                    gui.drawString(font, "E", mapWidth - font.width("E") - 1, (mapHeight - lineHeight) / 2 + 1, -1);
                    gui.drawCenteredString(font, "S", mapWidth / 2, mapHeight - lineHeight, -1);
                    gui.drawString(font, "W", 1, (mapHeight - lineHeight) / 2 + 1, -1);
                }
            }

            if (!rotate || renderArrowWhenRotate) {
                pose.pushPose();
                {
                    pose.translate(mapWidth / 2f, mapHeight / 2f, 0);
                    if (!rotate) {
                        pose.rotateAround(Axis.ZP.rotationDegrees(rot + 180f), 0, 0, 0);
                    }
                    pose.translate(-3.5f, -4.5f, 0);
                    gui.blit(AMap.id("textures/gui/minimap/arrow.png"), 0, 0, 0, 0, 7, 7, 8, 8);
                }
                pose.popPose();
            }

            if (renderFrame) {
                ResourceLocation frameId = AMap.id("textures/gui/minimap/frame.png");
                int bw = 2; // border width
                int iw = mapWidth; // inner width
                int ih = mapHeight; // inner height
                int rx = mapWidth; // right x
                int by = mapHeight; // bottom y

                // 4 corners
                gui.blit(frameId, -2, -2, 0, 0, bw, bw, 16, 16); // top left
                gui.blit(frameId, rx, -2, 14, 0, bw, bw, 16, 16); // top right
                gui.blit(frameId, -2, by, 0, 14, bw, bw, 16, 16); // bottom left
                gui.blit(frameId, rx, by, 14, 14, bw, bw, 16, 16); // bottom right

                // 4 sides
                gui.blit(frameId, 0, -2, iw, bw, bw, 0, 12, bw, 16, 16); // top
                gui.blit(frameId, 0, by, iw, bw, bw, 14, 12, bw, 16, 16); // bottom
                gui.blit(frameId, -2, 0, bw, ih, 0, bw, bw, 12, 16, 16); // left
                gui.blit(frameId, rx, 0, bw, ih, 14, bw, bw, 12, 16, 16); // right

            }

            InfoRenderer.renderInfo(gui, mapWidth / 2, 0, mapWidth, mapHeight + 5, tickDelta);

        }
        pose.popPose();
    }

    public static void zoomOut() {
        if (scale > minScale) scale--;
    }

    public static void zoomIn() {
        if (scale < maxScale) scale++;
    }

    public enum Position {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }
}
