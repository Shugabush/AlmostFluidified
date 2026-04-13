package com.shugabrush.raintegration.unification;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import com.shugabrush.raintegration.MoreUnification;

import java.util.*;

public class FluidUnification
{

    private static final Map< Fluid, List< TagKey< Fluid>>> fluidTagKeyMap = new HashMap<>();

    public static Fluid getFluid(Fluid fluid)
    {
        return getFluid(BuiltInRegistries.FLUID.getKey(fluid));
    }

    public static Fluid getFluid(ResourceLocation resourceLocation)
    {
        return MoreUnification.getReplacementForFluid(resourceLocation);
    }
}
