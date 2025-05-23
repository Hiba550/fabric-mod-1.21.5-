package com.example.mixin;

import com.example.ExampleMod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.SmithingScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SmithingTemplateItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmithingScreen.class)
public class SmithingScreenMixin {
    /**
     * Inject into the render method with the correct signature
     * Note that DrawContext is the first parameter, followed by mouseX (I), mouseY (I), and delta (F)
     */
    @Inject(method = "render", at = @At("RETURN"))
    private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        try {
            // Your render enhancement logic goes here
            ExampleMod.LOGGER.debug("SmithingScreen render mixin injection successful");
        } catch (Exception e) {
            ExampleMod.LOGGER.error("Error in SmithingScreen render mixin", e);
        }
    }
}
