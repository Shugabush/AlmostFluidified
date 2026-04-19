package com.shugabrush.raintegration.unification;

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
    private final Map<ResourceLocation, Set<ResourceLocation>> tagsToEntries = new HashMap<>();
    private final Map<ResourceLocation, Set<ResourceLocation>> entriesToTags = new HashMap<>();

    public static FluidTagMap create(Set<ResourceLocation> unifyTags)
    {
        FluidTagMap tagMap = new FluidTagMap();

        unifyTags.forEach(ut ->
        {
            TagKey<Fluid> asTagKey = TagKey.create(Registries.FLUID, ut);
            BuiltInRegistries.FLUID.getTagOrEmpty(asTagKey).forEach(holder ->
            {
                ResourceLocation key = BuiltInRegistries.FLUID.getKey(holder.value());
                tagMap.put(ut, key);
            });
        });

        return tagMap;
    }

    public static FluidTagMap createFromFluidTags(Map<ResourceLocation, Collection<Holder<Fluid>>> tags)
    {
        FluidTagMap tagMap = new FluidTagMap();

        for (var entry : tags.entrySet())
        {
            ResourceLocation unifyTag = entry.getKey();
            fillEntries(tagMap, entry.getValue(), unifyTag, BuiltInRegistries.FLUID);
        }

        return tagMap;
    }

    private static void fillEntries(FluidTagMap tagMap, Collection<Holder<Fluid>> holders, ResourceLocation unifyTag, Registry<Fluid> registry) {
        for (var holder : holders) {
            holder
                    .unwrapKey()
                    .map(ResourceKey::location)
                    .filter(registry::containsKey)
                    .ifPresent(id -> tagMap.put(unifyTag, id));
        }
    }

    protected void put(ResourceLocation tag, ResourceLocation entry) {
        tagsToEntries.computeIfAbsent(tag, k -> new HashSet<>()).add(entry);
        entriesToTags.computeIfAbsent(entry, k -> new HashSet<>()).add(tag);
    }

    public FluidTagMap filtered(Predicate<ResourceLocation> tagFilter, Predicate<ResourceLocation> entryFilter)
    {
        FluidTagMap tagMap = new FluidTagMap();
        tagsToEntries.forEach((tag, fluids) ->
        {
            if (!tagFilter.test(tag)) return;
            fluids.stream().filter(entryFilter).forEach(fluid -> tagMap.put(tag, fluid));
        });
        return tagMap;
    }
}
