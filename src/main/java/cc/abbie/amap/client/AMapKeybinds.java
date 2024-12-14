package cc.abbie.amap.client;

import cc.abbie.amap.client.minimap.MinimapHud;
import cc.abbie.amap.client.worldmap.WorldMapScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class AMapKeybinds implements ClientTickEvents.EndTick {

    private static final String worldMapCategory = "key.categories.amap.worldmap";
    private static final KeyMapping openWorldmap = new KeyMapping("key.amap.worldmap.open", GLFW.GLFW_KEY_M, worldMapCategory);

    private static final String minimapCategory = "key.categories.amap.minimap";
    private static final KeyMapping zoomOutMinimap = new KeyMapping("key.amap.minimap.zoom_out", GLFW.GLFW_KEY_MINUS, minimapCategory);
    private static final KeyMapping zoomInMinimap = new KeyMapping("key.amap.minimap.zoom_in", GLFW.GLFW_KEY_EQUAL, minimapCategory);

    public static void register() {
        KeyBindingHelper.registerKeyBinding(openWorldmap);
        KeyBindingHelper.registerKeyBinding(zoomOutMinimap);
        KeyBindingHelper.registerKeyBinding(zoomInMinimap);

        ClientTickEvents.END_CLIENT_TICK.register(new AMapKeybinds());
    }


    @Override
    public void onEndTick(Minecraft client) {
        if (zoomOutMinimap.consumeClick()) {
            MinimapHud.zoomOut();
        } else if (zoomInMinimap.consumeClick()) {
            MinimapHud.zoomIn();
        } else if (openWorldmap.consumeClick()) {
            client.setScreen(new WorldMapScreen());
        }
    }
}
