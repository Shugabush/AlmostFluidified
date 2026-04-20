package com.shugabrush.raintegration.mixin.sgjourney;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.povstalec.sgjourney.StargateJourney;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = StargateJourney.ClientModEvents.class, remap = false)
public class StargateJourneyClientModEventsMixin
{

    @ModifyArg(method = "onClientSetup",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/client/renderer/ItemBlockRenderTypes;setRenderLayer(Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/client/renderer/RenderType;)V"),
               index = 0)
    private static Fluid getUnifiedRenderLayer(Fluid fluid)
    {
        var str = BuiltInRegistries.FLUID.getKey(fluid).toString();
        if (str.contains("liquid_naquadah"))
        {
            if (str.contains("heavy_liquid_naquadah"))
            {

            }
            else
            {

            }
        }
        return fluid;
    }
}
