package com.shugabrush.raintegration.unification.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import com.shugabrush.raintegration.RAIntegration;

public class FluidUnification
{

    public static Fluid getFluid(ResourceLocation resourceLocation)
    {
        ResourceLocation unifiedResourceLocation = RAIntegration.getRuntime().getReplacementMap()
                .getReplacementForFluid(resourceLocation);

        if (unifiedResourceLocation != null)
        {
            return BuiltInRegistries.FLUID.get(unifiedResourceLocation);
        }
        return BuiltInRegistries.FLUID.get(resourceLocation);
    }

    public static Fluid getFluid(Fluid fluid)
    {
        return getFluid(BuiltInRegistries.FLUID.getKey(fluid));
    }
}
