package com.shugabrush.raintegration.unification.utils;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import com.almostreliable.unified.utils.UnifyTag;
import com.google.common.collect.*;
import com.shugabrush.raintegration.RAIntegration;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class FluidTagOwnerships
{

    private final Map< UnifyTag< Fluid>, UnifyTag< Fluid>> refsToOwner;
    private final Multimap< UnifyTag< Fluid>, UnifyTag< Fluid>> ownerToRefs;

    public FluidTagOwnerships(Set< UnifyTag< Fluid>> unifyTags,
                              Map< ResourceLocation, Set< ResourceLocation>> tagOwnershipConfig)
    {
        ImmutableMap.Builder< UnifyTag< Fluid>, UnifyTag< Fluid>> refsToOwnerBuilder = ImmutableMap.builder();
        ImmutableMultimap.Builder< UnifyTag< Fluid>, UnifyTag< Fluid>> ownerToRefsBuilder = ImmutableMultimap.builder();

        tagOwnershipConfig.forEach((rawOwner, rawRefs) ->
        {
            for (ResourceLocation rawRef : rawRefs)
            {
                UnifyTag< Fluid> owner = FluidUnifyTag.fluid(rawOwner);
                UnifyTag< Fluid> ref = FluidUnifyTag.fluid(rawRef);
                if (!unifyTags.contains(owner))
                {
                    RAIntegration.LOGGER.warn(
                            "[FluidTagOwnerships] Owner tag '#{}' is not present in the unify tag list!",
                            owner);
                    continue;
                }

                if (unifyTags.contains(ref))
                {
                    RAIntegration.LOGGER.warn(
                            "[FluidTagOwnerships] Reference tag '#{}' of owner tag '#{}' is present in the unify tag list!",
                            ref,
                            owner);
                    continue;
                }

                refsToOwnerBuilder.put(ref, owner);
                ownerToRefsBuilder.put(owner, ref);
            }
        });

        this.refsToOwner = refsToOwnerBuilder.build();
        this.ownerToRefs = ownerToRefsBuilder.build();
    }

    public void applyOwnerships(Map< ResourceLocation, Collection< Holder< Fluid>>> rawTags)
    {
        Multimap< ResourceLocation, ResourceLocation> changedTags = HashMultimap.create();

        ownerToRefs.asMap().forEach((owner, refs) ->
        {
            var rawHolders = rawTags.get(owner.location());
            if (rawHolders == null)
            {
                RAIntegration.LOGGER.warn("[FluidTagOwnerships] Owner tag '#{}' does not exist!", owner.location());
                return;
            }

            ImmutableSet.Builder< Holder< Fluid>> holders = ImmutableSet.builder();
            holders.addAll(rawHolders);
            boolean changed = false;

            for (UnifyTag< Fluid> ref : refs)
            {
                var refHolders = rawTags.get(ref.location());
                if (refHolders == null)
                {
                    RAIntegration.LOGGER.warn(
                            "[FluidTagOwnerships] Reference tag '#{}' of owner tag '#{}' does not exist!",
                            ref.location(),
                            owner.location());
                    continue;
                }

                for (Holder< Fluid> holder : refHolders)
                {
                    holders.add(holder);
                    holder.unwrapKey().ifPresent(key -> changedTags.put(owner.location(), key.location()));
                    changed = true;
                }
            }

            if (changed)
            {
                rawTags.put(owner.location(), holders.build());
            }
        });

        if (!changedTags.isEmpty())
        {
            changedTags.asMap().forEach((tag, fluids) ->
            {
                RAIntegration.LOGGER.info("[FluidTagOwnerships] Modified tag '#{}', added {}", tag, fluids);
            });
        }
    }

    public UnifyTag< Fluid> getOwnerByTag(UnifyTag< Fluid> tag)
    {
        return refsToOwner.get(tag);
    }

    public Collection< UnifyTag< Fluid>> getRefsByOwner(UnifyTag< Fluid> tag)
    {
        return ownerToRefs.get(tag);
    }
}
