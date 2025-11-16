package com.cope.meteorwebgui.mixin;

import com.cope.meteorwebgui.hud.HudPreviewCapture;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Hud.class)
public class HudMixin {
    @Redirect(method = "onRender(Lmeteordevelopment/meteorclient/events/render/Render2DEvent;)V",
        at = @At(value = "INVOKE", target = "Lmeteordevelopment/meteorclient/systems/hud/HudElement;render(Lmeteordevelopment/meteorclient/systems/hud/HudRenderer;)V"))
    private void meteorwebgui$wrapRender(HudElement element, HudRenderer renderer) {
        HudPreviewCapture.begin(element);
        element.render(renderer);
        HudPreviewCapture.end();
    }
}
