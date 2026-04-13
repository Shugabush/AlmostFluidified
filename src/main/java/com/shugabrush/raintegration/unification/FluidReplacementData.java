package com.shugabrush.raintegration.unification;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import com.almostreliable.unified.utils.TagMap;
import com.shugabrush.raintegration.ConfigHolder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public record FluidReplacementData(TagMap<Fluid> globalTagMap, TagMap<Fluid> filteredTagMap,
                                   FluidReplacementMap replacementMap) {

    public static FluidReplacementData load(Map<ResourceLocation, Collection<Holder<Fluid>>> tags) {
        var globalTagMap = FluidTagMap.createFromFluidTags(tags);
        var unifyTags = ConfigHolder.instance.fluidConfigs.bakeAndValidateTags(tags);
        var filteredTagMap = globalTagMap.filtered(unifyTags::contains, r -> true);

        var replacementMap = new FluidReplacementMap(filteredTagMap, new HashSet<>());

        return new FluidReplacementData(globalTagMap, globalTagMap, replacementMap);
    }
}
