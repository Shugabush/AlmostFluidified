package com.shugabrush.raintegration.unification;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

public class FluidUnification
{

    public static Fluid getFluid(ResourceLocation resourceLocation)
    {
        return BuiltInRegistries.FLUID.get(resourceLocation);
    }
}
