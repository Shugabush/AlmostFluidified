package com.shugabrush.almostfluidified.unification.utils;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.shugabrush.almostfluidified.AlmostFluidified;
import com.shugabrush.almostfluidified.FluidUnifyConfig;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public final class FluidTagReloadHandler
{

    private static Map< ResourceLocation, Collection< Holder< Fluid>>> RAW_FLUID_TAGS;

    private FluidTagReloadHandler()
    {}

    public static void initFluidTags(Map< ResourceLocation, Collection< Holder< Fluid>>> rawFluidTags)
    {
        RAW_FLUID_TAGS = rawFluidTags;
    }

    public static void run()
    {
        if (RAW_FLUID_TAGS == null)
            return;

        AlmostFluidified.onTagLoaderReload(RAW_FLUID_TAGS);

        RAW_FLUID_TAGS = null;
    }

    public static void applyCustomTags(FluidUnifyConfig unifyConfig)
    {
        Preconditions.checkNotNull(RAW_FLUID_TAGS, "Fluid tags were not loaded correctly");

        Multimap< ResourceLocation, ResourceLocation> changedFluidTags = HashMultimap.create();

        for (var entry : unifyConfig.getCustomTags().entrySet())
        {
            ResourceLocation tag = entry.getKey();
            Set< ResourceLocation> fluidIds = entry.getValue();

            for (ResourceLocation fluidId : fluidIds)
            {
                if (!BuiltInRegistries.FLUID.containsKey(fluidId))
                {
                    AlmostFluidified.LOGGER.warn("[CustomTags] Custom tag '{}' contains invalid fluid '{}'", tag,
                            fluidId);
                    continue;
                }

                ResourceKey< Fluid> fluidKey = ResourceKey.create(Registries.FLUID, fluidId);
                Holder< Fluid> fluidHolder = BuiltInRegistries.FLUID.getHolder(fluidKey).orElse(null);
                if (fluidHolder == null)
                    continue;

                ImmutableSet.Builder< Holder< Fluid>> newHolders = ImmutableSet.builder();
                var currentHolders = RAW_FLUID_TAGS.get(tag);

                if (currentHolders != null)
                {
                    if (currentHolders.contains(fluidHolder))
                    {
                        AlmostFluidified.LOGGER.warn("[CustomTags] Custom tag '{}' already contains fluid '{}'", tag,
                                fluidId);
                        continue;
                    }

                    newHolders.addAll(currentHolders);
                }
                newHolders.add(fluidHolder);

                RAW_FLUID_TAGS.put(tag, newHolders.build());
                changedFluidTags.put(tag, fluidId);
            }
        }

        if (!changedFluidTags.isEmpty())
        {
            changedFluidTags.asMap().forEach((tag, fluids) ->
            {
                AlmostFluidified.LOGGER.info("[CustomTags] Modified tag '#{}', added {}", tag, fluids);
            });
        }
    }
}
