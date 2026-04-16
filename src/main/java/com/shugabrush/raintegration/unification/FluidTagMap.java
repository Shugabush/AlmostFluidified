package com.shugabrush.raintegration.unification;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import com.almostreliable.unified.utils.TagMap;
import com.almostreliable.unified.utils.UnifyTag;

import java.util.Collection;
import java.util.Map;

public class FluidTagMap extends TagMap< Fluid>
{

    static void fillFluidEntries(FluidTagMap tagMap, Collection< Holder< Fluid>> holders, UnifyTag< Fluid> unifyTag)
    {
        for (var holder : holders)
        {
            holder.unwrapKey().map(ResourceKey::location).ifPresent(id -> tagMap.put(unifyTag, id));
        }
    }
}
