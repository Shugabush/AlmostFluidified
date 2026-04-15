package com.shugabrush.raintegration.unification;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import com.shugabrush.raintegration.RAIntegration;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class FluidUnification
{

    public static Fluid getFluid(ResourceLocation resourceLocation)
    {
        return BuiltInRegistries.FLUID.get(resourceLocation);
    }

    public static String getUnifiedFluidString(String string)
    {
        for (Map.Entry< String, String> entry : RAIntegration.fluids.entrySet())
        {
            String tag = entry.getKey();
            String fluid = entry.getValue();

            Collection<Holder<Fluid>> fluidList = RAIntegration.getFluids(new ResourceLocation(tag));
            for (Holder<Fluid> fluidHolder : fluidList)
            {
                String fluidStr = BuiltInRegistries.FLUID.getKey(fluidHolder.get()).toString();
                if (string.contains(fluidStr))
                {
                    return string.replaceAll(fluidStr, fluid);
                }
            }
            if (string.contains(tag))
            {
                return string.replaceAll(tag, fluid);
            }
        }
        return string;
    }
}
