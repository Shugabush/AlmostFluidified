package com.shugabrush.raintegration.unification.utils;

import com.almostreliable.unified.utils.UnifyTag;
import com.shugabrush.raintegration.RAIntegration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nullable;

public final class FluidUtils
{
    public static ResourceLocation UNUSED_ID = new ResourceLocation(RAIntegration.MOD_ID, "unused_id");
    public static final UnifyTag<Fluid> UNUSED_TAG = FluidUnifyTag.fluid(UNUSED_ID);

    private FluidUtils() {}

    public static UnifyTag<Fluid> toFluidTag(@Nullable String tag)
    {
        if (tag == null) return UNUSED_TAG;

        ResourceLocation rl = ResourceLocation.tryParse(tag);
        if (rl == null) return UNUSED_TAG;

        return FluidUnifyTag.fluid(rl);
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object o) {
        return (T) o;
    }

    public static ResourceLocation getRL(String path)
    {
        return new ResourceLocation(RAIntegration.MOD_ID, path);
    }

    public static String prefix(String path)
    {
        return RAIntegration.MOD_ID + "." + path;
    }
}
