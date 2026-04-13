package com.shugabrush.raintegration.unification;

import com.almostreliable.unified.utils.UnifyTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

public record FluidUnifyTag(Class<Fluid> boundType, ResourceLocation location)
{
    public static UnifyTag<Fluid> fluid(ResourceLocation location)
    {
        return new UnifyTag<>(Fluid.class, location);
    }

    @Override
    public String toString() {
        return "UnifyTag[" + boundType.getSimpleName().toLowerCase() + " / " + location + "]";
    }
}
