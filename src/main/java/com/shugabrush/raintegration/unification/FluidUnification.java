package com.shugabrush.raintegration.unification;

import com.shugabrush.raintegration.RAIntegration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

public class FluidUnification
{
    public static Fluid getFluid(ResourceLocation resourceLocation)
    {
        return BuiltInRegistries.FLUID.get(resourceLocation);
    }

    public static String getUnifiedFluidString(String string)
    {
        RAIntegration.fluids.forEach((tag, fluid) ->
        {
            if (string.contains(tag))
            {
                RAIntegration.LOGGER.info("HERE");
            }
        });
        return string;
    }
}
