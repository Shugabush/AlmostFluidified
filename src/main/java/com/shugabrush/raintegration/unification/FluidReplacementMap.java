package com.shugabrush.raintegration.unification;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import com.almostreliable.unified.utils.TagMap;
import com.almostreliable.unified.utils.UnifyTag;
import com.shugabrush.raintegration.ConfigHolder;
import com.shugabrush.raintegration.MoreUnification;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

public class FluidReplacementMap {

    private final TagMap<Fluid> tagMap;
    private final Set<ResourceLocation> warnings;

    public FluidReplacementMap(TagMap<Fluid> tagMap, Set<ResourceLocation> warnings) {
        this.tagMap = tagMap;
        this.warnings = warnings;
    }

    @Nullable
    public UnifyTag<Fluid> getPreferredTagForFluid(ResourceLocation fluid) {
        Collection<UnifyTag<Fluid>> tags = tagMap.getTagsByEntry(fluid);

        if (tags.isEmpty()) return null;

        if (tags.size() > 1 && !warnings.contains(fluid)) {
            MoreUnification.LOGGER.warn(
                    "Fluid '{}' has multiple preferred tags '{}' for recipe replacement. This needs to be manually fixed by the user.",
                    fluid,
                    tags.stream().map(UnifyTag::location).toList());
            warnings.add(fluid);
        }

        return tags.iterator().next();
    }

    public ResourceLocation getReplacementForFluid(ResourceLocation fluid) {
        UnifyTag<Fluid> f = getPreferredTagForFluid(fluid);

        if (f == null) return fluid;

        return getPreferredFluidForTag(f, i -> true);
    }

    @Nullable
    public ResourceLocation getPreferredFluidForTag(UnifyTag<Fluid> tag, Predicate<ResourceLocation> fluidFilter) {
        List<ResourceLocation> fluids = tagMap
                .getEntriesByTag(tag)
                .stream()
                .filter(fluidFilter)
                .toList();

        if (fluids.isEmpty()) return null;

        ResourceLocation overrideFluid = getOverrideForTag(tag, fluids);
        if (overrideFluid != null) {
            return overrideFluid;
        }

        for (String modPriority : ConfigHolder.instance.fluidConfigs.modPriorities) {
            ResourceLocation fluid = findFluidByNameSpace(fluids, modPriority);
            if (fluid != null) return fluid;
        }

        return null;
    }

    @Nullable
    private ResourceLocation getOverrideForTag(UnifyTag<Fluid> tag, List<ResourceLocation> fluids) {
        for (String override : ConfigHolder.instance.fluidConfigs.priorityOverrides) {
            if (override == tag.location().getNamespace()) {
                ResourceLocation fluid = findFluidByNameSpace(fluids, override);
                if (fluid != null) return fluid;
                MoreUnification.LOGGER.warn(
                        "Priority override mod '{}' for tag '{}' does not contain a valid fluid. Falling back to default priority.",
                        override,
                        tag.location());
            }
        }
        return null;
    }

    @Nullable
    private ResourceLocation findFluidByNameSpace(List<ResourceLocation> fluids, String namespace) {
        for (ResourceLocation fluid : fluids) {
            if (fluid.getNamespace().equals(namespace)) {
                return fluid;
            }
        }
        return null;
    }
}
