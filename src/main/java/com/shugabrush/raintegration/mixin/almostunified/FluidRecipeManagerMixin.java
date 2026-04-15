package com.shugabrush.raintegration.mixin.almostunified;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;

import com.google.gson.JsonElement;
import com.shugabrush.raintegration.RAIntegration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

// inject after most mods but before KubeJS
@Mixin(value = RecipeManager.class, priority = 1_099)
public class FluidRecipeManagerMixin
{

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("HEAD"))
    private void runTransformation(Map<ResourceLocation, JsonElement> recipes, ResourceManager resourceManager,
                                   ProfilerFiller profiler, CallbackInfo ci)
    {
        try {
            RAIntegration.onRecipeManagerReload(recipes);
        } catch (Exception e) {
            RAIntegration.LOGGER.error(e.getMessage(), e);
        }
    }
}
