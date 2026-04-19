package com.shugabrush.raintegration.unification.utils;

import com.almostreliable.unified.utils.UnifyTag;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import java.util.*;
import java.util.function.Predicate;

public class FluidTagMap
{
    private final Map< UnifyTag<Fluid>, Set< ResourceLocation>> tagsToEntries = new HashMap<>();
    private final Map< ResourceLocation, Set< UnifyTag<Fluid>>> entriesToTags = new HashMap<>();

    public static FluidTagMap create(Set< UnifyTag<Fluid>> unifyTags)
    {
        FluidTagMap tagMap = new FluidTagMap();

        unifyTags.forEach(ut ->
        {
            TagKey< Fluid> asTagKey = TagKey.create(Registries.FLUID, ut.location());
            BuiltInRegistries.FLUID.getTagOrEmpty(asTagKey).forEach(holder ->
            {
                ResourceLocation key = BuiltInRegistries.FLUID.getKey(holder.value());
                tagMap.put(ut, key);
            });
        });

        return tagMap;
    }

    public static FluidTagMap createFromFluidTags(Map< ResourceLocation, Collection< Holder< Fluid>>> tags)
    {
        FluidTagMap tagMap = new FluidTagMap();

        for (var entry : tags.entrySet())
        {
            UnifyTag<Fluid> unifyTag = FluidUnifyTag.fluid(entry.getKey());
            fillEntries(tagMap, entry.getValue(), unifyTag, BuiltInRegistries.FLUID);
        }

        return tagMap;
    }

    private static void fillEntries(FluidTagMap tagMap, Collection< Holder< Fluid>> holders, UnifyTag<Fluid> unifyTag,
                                    Registry< Fluid> registry)
    {
        for (var holder : holders)
        {
            holder
                    .unwrapKey()
                    .map(ResourceKey::location)
                    .filter(registry::containsKey)
                    .ifPresent(id -> tagMap.put(unifyTag, id));
        }
    }

    public FluidTagMap filtered(Predicate< UnifyTag<Fluid>> tagFilter, Predicate< ResourceLocation> entryFilter)
    {
        FluidTagMap tagMap = new FluidTagMap();
        tagsToEntries.forEach((tag, fluids) ->
        {
            if (!tagFilter.test(tag))
                return;
            fluids.stream().filter(entryFilter).forEach(fluid -> tagMap.put(tag, fluid));
        });
        return tagMap;
    }

    public int tagSize()
    {
        return tagsToEntries.size();
    }

    public int fluidSize()
    {
        return entriesToTags.size();
    }

    public Set<ResourceLocation> getEntriesByTag(UnifyTag<Fluid> tag)
    {
        return Collections.unmodifiableSet(tagsToEntries.getOrDefault(tag, Collections.emptySet()));
    }

    public Set<UnifyTag<Fluid>> getTagsByEntry(ResourceLocation entry)
    {
        return Collections.unmodifiableSet(entriesToTags.getOrDefault(entry, Collections.emptySet()));
    }

    public Set<UnifyTag<Fluid>> getTags()
    {
        return Collections.unmodifiableSet(tagsToEntries.keySet());
    }

    protected void put(UnifyTag<Fluid> tag, ResourceLocation entry)
    {
        tagsToEntries.computeIfAbsent(tag, k -> new HashSet<>()).add(entry);
        entriesToTags.computeIfAbsent(entry, k -> new HashSet<>()).add(tag);
    }
}
