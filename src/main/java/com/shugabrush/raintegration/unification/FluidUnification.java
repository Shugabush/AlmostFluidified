package com.shugabrush.raintegration.unification;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import java.util.*;

public class FluidUnification {

    private final static Map<Fluid, List<TagKey<Fluid>>> fluidTagKeyMap = new HashMap<>();

    public static Fluid getFluid(Fluid fluid) {
        return fluid;
    }

    public static Fluid getFluid(ResourceLocation resourceLocation) {
        return getFluid(BuiltInRegistries.FLUID.get(resourceLocation));
    }
}
