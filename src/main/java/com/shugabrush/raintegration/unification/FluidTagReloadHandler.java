package com.shugabrush.raintegration.unification;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import com.shugabrush.raintegration.MoreUnification;

import java.util.Collection;
import java.util.Map;

public class FluidTagReloadHandler {

    private static Map<ResourceLocation, Collection<Holder<Fluid>>> RAW_FLUID_TAGS;

    public static void initFluidTags(Map<ResourceLocation, Collection<Holder<Fluid>>> rawFluidTags) {
        RAW_FLUID_TAGS = rawFluidTags;
    }

    public static void run() {
        if (RAW_FLUID_TAGS == null) return;

        MoreUnification.onTagLoaderReload(RAW_FLUID_TAGS);
    }
}
