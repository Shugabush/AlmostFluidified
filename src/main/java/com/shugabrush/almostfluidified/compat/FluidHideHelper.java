package com.shugabrush.almostfluidified.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import com.almostreliable.unified.utils.UnifyTag;
import com.almostreliable.unified.utils.Utils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.shugabrush.almostfluidified.AlmostFluidified;
import com.shugabrush.almostfluidified.unification.AlmostFluidifiedRuntime;
import com.shugabrush.almostfluidified.unification.FluidReplacementMap;
import com.shugabrush.almostfluidified.unification.utils.FluidTagOwnerships;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

public final class FluidHideHelper
{

    private FluidHideHelper()
    {}

    public static Multimap< UnifyTag< Fluid>, ResourceLocation> createHidingMap()
    {
        AlmostFluidifiedRuntime runtime = AlmostFluidified.getRuntime();
        FluidReplacementMap repMap = runtime.getReplacementMap();
        var tagMap = runtime.getFilteredTagMap();

        Multimap< UnifyTag< Fluid>, ResourceLocation> hidingMap = HashMultimap.create();
        if (repMap == null || tagMap == null)
            return hidingMap;
        FluidTagOwnerships ownerships = repMap.getTagOwnerships();

        for (var unifyTag : tagMap.getTags())
        {
            var fluidsByTag = tagMap.getEntriesByTag(unifyTag);
            if (Utils.allSameNamespace(fluidsByTag))
                continue;

            Set< ResourceLocation> replacements = new HashSet<>();
            for (ResourceLocation fluid : fluidsByTag)
            {
                ResourceLocation replacement = repMap.getReplacementForFluid(fluid);
                replacements.add(replacement == null ? fluid : replacement);
            }

            Set< ResourceLocation> fluidsToHide = getFluidsToHide(unifyTag, fluidsByTag, replacements);
            if (fluidsToHide != null)
            {
                hidingMap.putAll(unifyTag, fluidsToHide);
            }

            Set< ResourceLocation> refFluidsToHide = getRefFluidsToHide(unifyTag, ownerships, replacements);
            if (!refFluidsToHide.isEmpty())
            {
                hidingMap.putAll(unifyTag, refFluidsToHide);
            }
        }

        return hidingMap;
    }

    public static Multimap< UnifyTag< Fluid>, ResourceLocation> createBucketHidingMap()
    {
        AlmostFluidifiedRuntime runtime = AlmostFluidified.getRuntime();
        FluidReplacementMap repMap = runtime.getReplacementMap();
        var tagMap = runtime.getFilteredTagMap();

        Multimap< UnifyTag<Fluid>, ResourceLocation> hidingMap = HashMultimap.create();
        if (repMap == null || tagMap == null)
            return hidingMap;
        FluidTagOwnerships ownerships = repMap.getTagOwnerships();

        for (var unifyTag : tagMap.getTags())
        {
            var fluidsByTag = tagMap.getEntriesByTag(unifyTag);
            if (Utils.allSameNamespace(fluidsByTag))
                continue;

            Set< ResourceLocation> replacements = new HashSet<>();
            for (ResourceLocation fluid : fluidsByTag)
            {
                ResourceLocation replacement = repMap.getReplacementForFluid(fluid);

                replacements.add(replacement == null ? fluid : replacement);
            }

            Set< ResourceLocation> fluidsToHide = getFluidsToHide(unifyTag, fluidsByTag, replacements);
            if (fluidsToHide != null)
            {
                Set<ResourceLocation> bucketsToHide = new HashSet<>();
                fluidsToHide.forEach(fluid ->
                {
                    bucketsToHide.add(BuiltInRegistries.ITEM.getKey(BuiltInRegistries.FLUID.get(fluid).getBucket()));
                });
                hidingMap.putAll(unifyTag, bucketsToHide);
            }

            Set< ResourceLocation> refFluidsToHide = getRefFluidsToHide(unifyTag, ownerships, replacements);
            if (!refFluidsToHide.isEmpty())
            {
                hidingMap.putAll(unifyTag, refFluidsToHide);
            }
        }

        return hidingMap;
    }

    @Nullable
    private static Set< ResourceLocation> getFluidsToHide(UnifyTag< Fluid> unifyTag, Set< ResourceLocation> fluidsByTag,
                                                          Set< ResourceLocation> replacements)
    {
        Set< ResourceLocation> fluidsToHide = new HashSet<>();
        for (ResourceLocation fluid : fluidsByTag)
        {
            if (!replacements.contains(fluid))
            {
                fluidsToHide.add(fluid);
            }
        }

        if (fluidsToHide.isEmpty())
            return null;

        return fluidsToHide;
    }

    private static Set< ResourceLocation> getRefFluidsToHide(UnifyTag< Fluid> unifyTag, FluidTagOwnerships ownerships,
                                                             Set< ResourceLocation> replacements)
    {
        var refTags = ownerships.getRefsByOwner(unifyTag);
        Set< ResourceLocation> refFluidsToHide = new HashSet<>();

        for (var refTag : refTags)
        {
            var asTagKey = TagKey.create(Registries.FLUID, refTag.location());

            BuiltInRegistries.FLUID.getTagOrEmpty(asTagKey).forEach(holder ->
            {
                ResourceLocation fluid = BuiltInRegistries.FLUID.getKey(holder.value());
                if (replacements.contains(fluid))
                    return;
                refFluidsToHide.add(fluid);
            });
        }

        return refFluidsToHide;
    }

    private static Set< ResourceLocation> getRefBucketsToHide(UnifyTag< Fluid> unifyTag, FluidTagOwnerships ownerships,
                                                             Set< ResourceLocation> replacements)
    {
        var refTags = ownerships.getRefsByOwner(unifyTag);
        Set< ResourceLocation> refBucketsToHide = new HashSet<>();

        for (var refTag : refTags)
        {
            var asTagKey = TagKey.create(Registries.FLUID, refTag.location());

            BuiltInRegistries.FLUID.getTagOrEmpty(asTagKey).forEach(holder ->
            {
                ResourceLocation bucket = BuiltInRegistries.ITEM.getKey(holder.value().getBucket());
                if (replacements.contains(bucket))
                    return;
                refBucketsToHide.add(bucket);
            });
        }

        return refBucketsToHide;
    }

    public static Collection< FluidStack> getStacksToHide()
    {
        Multimap< UnifyTag< Fluid>, ResourceLocation> hidingMap = createHidingMap();
        if (hidingMap.isEmpty())
            return List.of();

        return hidingMap
                .entries()
                .stream()
                .flatMap(rl -> BuiltInRegistries.FLUID.getOptional(rl.getValue()).stream())
                .map((fluid) -> new FluidStack(fluid, 1))
                .toList();
    }

    public static Collection<ItemStack> getBucketStacksToHide()
    {
        Multimap< UnifyTag< Fluid>, ResourceLocation> hidingMap = createBucketHidingMap();
        if (hidingMap.isEmpty())
            return List.of();

        return hidingMap
                .entries()
                .stream()
                .flatMap(rl -> BuiltInRegistries.ITEM.getOptional(rl.getValue()).stream())
                .map(ItemStack::new)
                .toList();
    }
}
