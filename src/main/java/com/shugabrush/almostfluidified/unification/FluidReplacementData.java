package com.shugabrush.almostfluidified.unification;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import com.shugabrush.almostfluidified.FluidUnifyConfig;
import com.shugabrush.almostfluidified.unification.utils.FluidTagMap;
import com.shugabrush.almostfluidified.unification.utils.FluidTagOwnerships;

import java.util.Collection;
import java.util.Map;

public record FluidReplacementData(FluidTagMap globalTagMap, FluidTagMap filteredTagMap,
                                   FluidReplacementMap replacementMap)
{

    public static FluidReplacementData load(Map< ResourceLocation, Collection< Holder< Fluid>>> tags,
                                            FluidUnifyConfig unifyConfig, FluidTagOwnerships tagOwnerships)
    {
        var globalTagMap = FluidTagMap.createFromFluidTags(tags);
        var unifyTags = unifyConfig.bakeAndValidateTags(tags);
        var filteredTagMap = globalTagMap.filtered(unifyTags::contains, unifyConfig::includeFluid);

        var replacementMap = new FluidReplacementMap(unifyConfig, filteredTagMap, tagOwnerships);

        return new FluidReplacementData(globalTagMap, filteredTagMap, replacementMap);
    }
}
