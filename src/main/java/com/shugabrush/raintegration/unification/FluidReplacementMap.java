package com.shugabrush.raintegration.unification;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import com.almostreliable.unified.utils.UnifyTag;
import com.shugabrush.raintegration.FluidUnifyConfig;
import com.shugabrush.raintegration.RAIntegration;
import com.shugabrush.raintegration.unification.utils.FluidTagMap;
import com.shugabrush.raintegration.unification.utils.FluidTagOwnerships;

import java.util.*;
import java.util.function.Predicate;

public class FluidReplacementMap
{

    private final FluidUnifyConfig unifyConfig;
    private final FluidTagMap tagMap;
    private final FluidTagMap flowingTagMap;
    private final FluidTagOwnerships tagOwnerships;
    private final Set< ResourceLocation> warnings;

    public FluidReplacementMap(FluidUnifyConfig unifyConfig, FluidTagMap tagMap, FluidTagMap flowingTagMap, FluidTagOwnerships tagOwnerships)
    {
        this.unifyConfig = unifyConfig;
        this.tagMap = tagMap;
        this.flowingTagMap = flowingTagMap;
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
            RAIntegration.LOGGER.warn(
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
            ResourceLocation item = findFluidByNamespace(fluids, priorityOverride);
            if (item != null)
                return item;
            RAIntegration.LOGGER.warn(
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
