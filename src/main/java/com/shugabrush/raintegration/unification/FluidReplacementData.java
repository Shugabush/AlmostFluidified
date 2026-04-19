package com.shugabrush.raintegration.unification;

import com.shugabrush.raintegration.FluidUnifyConfig;
import com.shugabrush.raintegration.unification.recipeunifiers.FluidReplacementMap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import java.util.Collection;
import java.util.Map;

public record FluidReplacementData(FluidTagMap globalTagMap, FluidTagMap filteredTagMap, FluidReplacementMap replacementMap)
{
    public static FluidReplacementData load(Map<ResourceLocation, Collection<Holder<Fluid>>> tags, FluidUnifyConfig unifyConfig)
    {
        var globalTagMap = FluidTagMap.createFromFluidTags(tags);
        var unifyTags = unifyConfig.bakeAndValidateTags(tags);
        var filteredTagMap = globalTagMap.filtered(unifyTags::contains, e -> true);

        var replacementMap = new FluidReplacementMap();

        return new FluidReplacementData(globalTagMap, filteredTagMap, replacementMap);
    }
}
