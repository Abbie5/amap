package cc.abbie.amap.mixin.client;

import net.minecraft.client.gui.Gui;

import cc.abbie.amap.client.minimap.MinimapHud;
import cc.abbie.amap.client.minimap.config.MinimapConfig;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @ModifyExpressionValue(method = "renderEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;guiWidth()I"))
    private int pushEffects(int original) {
        if (!MinimapConfig.INSTANCE.enable.value() || MinimapConfig.INSTANCE.minimap.position.value() != MinimapConfig.Minimap.Position.UPPER_RIGHT) return original;

        return original - MinimapHud.mapWidth - 2 * MinimapHud.offsetX;
    }
}
