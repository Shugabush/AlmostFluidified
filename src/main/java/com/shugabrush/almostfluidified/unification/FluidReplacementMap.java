package com.shugabrush.almostfluidified.unification;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import com.almostreliable.unified.utils.UnifyTag;
import com.shugabrush.almostfluidified.AlmostFluidified;
import com.shugabrush.almostfluidified.FluidUnifyConfig;
import com.shugabrush.almostfluidified.unification.utils.FluidTagMap;
import com.shugabrush.almostfluidified.unification.utils.FluidTagOwnerships;

import java.util.*;
import java.util.function.Predicate;

public class FluidReplacementMap
{

    private final FluidUnifyConfig unifyConfig;
    private final FluidTagMap tagMap;
    private final FluidTagOwnerships tagOwnerships;
    private final Set< ResourceLocation> warnings;

    public FluidReplacementMap(FluidUnifyConfig unifyConfig, FluidTagMap tagMap,
                               FluidTagOwnerships tagOwnerships)
    {
        this.unifyConfig = unifyConfig;
        this.tagMap = tagMap;
        this.tagOwnerships = tagOwnerships;
        this.warnings = new HashSet<>();
    }

    public UnifyTag< Fluid> getPreferredTagForFluid(ResourceLocation fluid)
    {
        Collection< UnifyTag< Fluid>> tags = tagMap.getTagsByEntry(fluid);

        if (tags.isEmpty())
            return null;

        if (tags.size() > 1 && !warnings.contains(fluid))
        {
            AlmostFluidified.LOGGER.warn(
                    "Fluid '{}' has multiple preferred tags '{}' for recipe replacement. This needs to be manually fixed by the user.",
                    fluid,
                    tags.stream().map(UnifyTag::location).toList());
            warnings.add(fluid);
        }

        return tags.iterator().next();
    }

    public ResourceLocation getReplacementForFluid(ResourceLocation fluid)
    {
        UnifyTag< Fluid> t = getPreferredTagForFluid(fluid);
        if (t == null)
            return null;

        return getPreferredFluidForTag(t, i -> true);
    }

    public ResourceLocation getReplacementForBucket(ResourceLocation bucket)
    {
        if (bucket == null)
            return null;

        Optional< FluidStack> fluidStackOption = FluidUtil
                .getFluidContained(new ItemStack(BuiltInRegistries.ITEM.get(bucket)));

        if (fluidStackOption.isEmpty())
            return null;

        ResourceLocation unifiedFluidLocation = getReplacementForFluid(
                BuiltInRegistries.FLUID.getKey(fluidStackOption.get().getFluid()));

        if (unifiedFluidLocation == null)
            return null;

        Fluid unifiedFluid = BuiltInRegistries.FLUID.get(unifiedFluidLocation);

        if (unifiedFluid == fluidStackOption.get().getFluid())
            return null;

        return BuiltInRegistries.ITEM.getKey(unifiedFluid.getBucket());
    }

    public ResourceLocation getPreferredFluidForTag(UnifyTag< Fluid> tag, Predicate< ResourceLocation> fluidFilter)
    {
        var tagToLookup = tagOwnerships.getOwnerByTag(tag);
        if (tagToLookup == null)
            tagToLookup = tag;

        List< ResourceLocation> fluids = tagMap
                .getEntriesByTag(tagToLookup)
                .stream()
                .filter(fluidFilter)
                .toList();

        if (fluids.isEmpty())
            return null;

        ResourceLocation overrideFluid = getOverrideForTag(tagToLookup, fluids);
        if (overrideFluid != null)
            return overrideFluid;

        for (String modPriority : unifyConfig.getModPriorities())
        {
            ResourceLocation fluid = findFluidByNamespace(fluids, modPriority);
            if (fluid != null)
                return fluid;
        }

        return null;
    }

    private ResourceLocation getOverrideForTag(UnifyTag< Fluid> tag, List< ResourceLocation> fluids)
    {
        String priorityOverride = unifyConfig.getPriorityOverrides().get(tag.location());
        if (priorityOverride != null)
        {
            ResourceLocation fluid = findFluidByNamespace(fluids, priorityOverride);
            if (fluid != null)
                return fluid;
            AlmostFluidified.LOGGER.warn(
                    "Priority override mod '{}' for tag '{}' does not contain a valid fluid. Falling back to default priority.",
                    priorityOverride,
                    tag.location());
        }
        return null;
    }

    private ResourceLocation findFluidByNamespace(List< ResourceLocation> fluids, String namespace)
    {
        for (ResourceLocation fluid : fluids)
        {
            if (fluid.getNamespace().equals(namespace))
            {
                return fluid;
            }
        }
        return null;
    }

    public FluidTagOwnerships getTagOwnerships()
    {
        return tagOwnerships;
    }
}
