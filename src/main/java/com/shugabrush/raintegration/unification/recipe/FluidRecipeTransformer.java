package com.shugabrush.raintegration.unification.recipe;

import net.minecraft.resources.ResourceLocation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.shugabrush.raintegration.FluidUnifyConfig;
import com.shugabrush.raintegration.unification.FluidReplacementMap;
import com.shugabrush.raintegration.unification.recipe.unifier.FluidRecipeHandlerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class FluidRecipeTransformer
{

    private final FluidRecipeHandlerFactory factory;
    private final FluidReplacementMap replacementMap;
    private final FluidUnifyConfig unifyConfig;

    public FluidRecipeTransformer(FluidRecipeHandlerFactory factory, FluidReplacementMap replacementMap,
                                  FluidUnifyConfig unifyConfig)
    {
        this.factory = factory;
        this.replacementMap = replacementMap;
        this.unifyConfig = unifyConfig;
    }

    public boolean hasValidRecipeType(JsonObject json)
    {
        if (json.get("type") instanceof JsonPrimitive primitive)
        {
            ResourceLocation type = ResourceLocation.tryParse(primitive.getAsString());
            return type != null;
        }
        return false;
    }

    public static class Result
    {

        private final Multimap< ResourceLocation, FluidRecipeLink> allRecipesByType = HashMultimap.create();
        private final Multimap< ResourceLocation, FluidRecipeLink> unifiedRecipesByType = HashMultimap.create();

        private final Multimap< ResourceLocation, FluidRecipeLink.DuplicateLink> duplicatesByType = HashMultimap
                .create();

        private void add(FluidRecipeLink link)
        {
            if (allRecipesByType.containsEntry(link.getType(), link))
            {
                throw new IllegalStateException("Already tracking recipe type " + link.getType());
            }

            allRecipesByType.put(link.getType(), link);
            if (link.isUnified())
            {
                unifiedRecipesByType.put(link.getType(), link);
            }

            if (link.hasDuplicateLink())
            {
                duplicatesByType.put(link.getType(), link.getDuplicateLink());
            }
        }

        private void addAll(Collection< FluidRecipeLink> links)
        {
            links.forEach(this::add);
        }

        public Collection< FluidRecipeLink> getRecipes(ResourceLocation type)
        {
            return Collections.unmodifiableCollection(allRecipesByType.get(type));
        }

        public Collection< FluidRecipeLink> getUnifiedRecipes(ResourceLocation type)
        {
            return Collections.unmodifiableCollection(unifiedRecipesByType.get(type));
        }

        public Collection< FluidRecipeLink.DuplicateLink> getDuplicates(ResourceLocation type)
        {
            return Collections.unmodifiableCollection(duplicatesByType.get(type));
        }

        public int getRecipeCount()
        {
            return allRecipesByType.size();
        }

        public int getUnifiedRecipeCount()
        {
            return unifiedRecipesByType.size();
        }

        public int getDuplicatesCount()
        {
            return duplicatesByType.size();
        }

        public int getDuplicateRecipesCount()
        {
            return duplicatesByType.values().stream().mapToInt(l -> l.getRecipes().size()).sum();
        }

        public Set< ResourceLocation> getUnifiedRecipeTypes()
        {
            return unifiedRecipesByType.keySet();
        }
    }
}
