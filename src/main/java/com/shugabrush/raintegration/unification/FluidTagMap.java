package com.shugabrush.raintegration.unification;

import com.almostreliable.unified.utils.TagMap;
import com.almostreliable.unified.utils.UnifyTag;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import java.util.Collection;
import java.util.Map;

public class FluidTagMap extends TagMap<Fluid>
{
    public static TagMap<Fluid> createFromFluidTags(Map<ResourceLocation, Collection<Holder<Fluid>>> tags) {
        FluidTagMap tagMap = new FluidTagMap();

        for (var entry : tags.entrySet()) {
            UnifyTag<Fluid> unifyTag = FluidUnifyTag.fluid(entry.getKey());
            fillFluidEntries(tagMap, entry.getValue(), unifyTag);
        }

        return tagMap;
    }

    static void fillFluidEntries(FluidTagMap tagMap, Collection<Holder<Fluid>> holders, UnifyTag<Fluid> unifyTag)
    {
        for (var holder : holders) {
            holder
                    .unwrapKey()
                    .map(ResourceKey::location)
                    .ifPresent(id -> tagMap.put(unifyTag, id));
        }
    }
}
