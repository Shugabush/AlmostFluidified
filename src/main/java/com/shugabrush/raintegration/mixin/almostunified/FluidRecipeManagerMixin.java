package com.shugabrush.raintegration.mixin.almostunified;

import com.google.gson.JsonElement;
import com.shugabrush.raintegration.MoreUnification;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = RecipeManager.class)
public class FluidRecipeManagerMixin
{
    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"))
    private void runTransformation(Map<ResourceLocation, JsonElement> recipes, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci)
    {
        try
        {
            MoreUnification.onRecipeManagerReload(recipes);
        }
        catch (Exception e)
        {
            MoreUnification.LOGGER.error(e.getMessage(), e);
        }
    }
}
