package com.shugabrush.raintegration.unification.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import com.almostreliable.unified.utils.UnifyTag;

public final class FluidUnifyTag
{

    public static UnifyTag< Fluid> fluid(ResourceLocation location)
    {
        return new UnifyTag<>(Fluid.class, location);
    }
}
