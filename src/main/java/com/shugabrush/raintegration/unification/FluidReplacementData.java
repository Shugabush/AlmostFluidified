package com.shugabrush.raintegration.unification;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import com.shugabrush.raintegration.FluidUnifyConfig;
import com.shugabrush.raintegration.unification.utils.FluidTagMap;
import com.shugabrush.raintegration.unification.utils.FluidTagOwnerships;

import java.util.Collection;
import java.util.Map;

public record FluidReplacementData(FluidTagMap globalTagMap, FluidTagMap filteredTagMap,
                                   FluidReplacementMap replacementMap)
{

    public static FluidReplacementData load(Map< ResourceLocation, Collection< Holder< Fluid>>> tags,
                                            FluidUnifyConfig unifyConfig, FluidTagOwnerships tagOwnerships)
    {
        var globalTagMaps = FluidTagMap.createFromFluidTags(tags);
        var globalTagMap = globalTagMaps.get(0);
        var globalFlowingTagMap = globalTagMaps.get(1);
        var unifyTags = unifyConfig.bakeAndValidateTags(tags);
        var filteredTagMap = globalTagMap.filtered(unifyTags::contains, e -> true);
        var filteredFlowingTagMap = globalFlowingTagMap.filtered(unifyTags::contains, e -> true);

        var replacementMap = new FluidReplacementMap(unifyConfig, filteredTagMap, filteredFlowingTagMap, tagOwnerships);

        return new FluidReplacementData(globalTagMap, filteredTagMap, replacementMap);
    }
}
