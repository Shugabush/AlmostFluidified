package com.shugabrush.raintegration.unification.recipe;

import net.minecraft.resources.ResourceLocation;

import com.almostreliable.unified.utils.JsonQuery;
import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.shugabrush.raintegration.FluidUnifyConfig;
import com.shugabrush.raintegration.RAIntegration;
import com.shugabrush.raintegration.api.FluidRecipeUnifierBuilder;
import com.shugabrush.raintegration.unification.FluidRecipeContext;
import com.shugabrush.raintegration.unification.FluidReplacementMap;
import com.shugabrush.raintegration.unification.recipe.unifier.FluidRecipeHandlerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

    public Result transformRecipes(Map< ResourceLocation, JsonElement> recipes, boolean skipClientTracking)
    {
        Stopwatch transformationTimer = Stopwatch.createStarted();
        RAIntegration.LOGGER.warn("Recipe count: " + recipes.size());

        Result result = new Result();
        Map< ResourceLocation, List< FluidRecipeLink>> byType = groupRecipesByType(recipes);

        ResourceLocation fcLocation = new ResourceLocation("forge:conditional");
        byType.forEach((type, recipeLinks) ->
        {
            if (type.equals(fcLocation))
            {
                recipeLinks.forEach(recipeLink -> handleForgeConditionals(recipeLink).ifPresent(json -> recipes.put(
                        recipeLink.getId(),
                        json)));
            }
            else
            {
                transformRecipes(recipeLinks, recipes);
            }
            result.addAll(recipeLinks);
        });
        RAIntegration.LOGGER.warn(
                "Recipe count afterwards: " + recipes.size() + " (done in " + transformationTimer.stop() + ")");
        return result;
    }

    private Optional< JsonObject> handleForgeConditionals(FluidRecipeLink recipeLink)
    {
        JsonObject copy = recipeLink.getOriginal().deepCopy();

        if (copy.get("recipes") instanceof JsonArray recipes)
        {
            for (JsonElement element : recipes)
            {
                JsonQuery
                        .of(element, "recipe")
                        .asObject()
                        .map(jsonObject -> FluidRecipeLink.of(recipeLink.getId(), jsonObject))
                        .ifPresent(temporaryLink ->
                        {
                            unifyRecipe(temporaryLink);
                            if (temporaryLink.isUnified())
                            {
                                element.getAsJsonObject().add("recipe", temporaryLink.getUnified());
                            }
                        });
            }

            if (!copy.equals(recipeLink.getOriginal()))
            {
                recipeLink.setUnified(copy);
                return Optional.of(copy);
            }
        }

        return Optional.empty();
    }

    private void transformRecipes(List< FluidRecipeLink> recipeLinks, Map< ResourceLocation, JsonElement> allRecipes)
    {
        var unified = unifyRecipes(recipeLinks, r -> allRecipes.put(r.getId(), r.getUnified()));
    }

    public Map< ResourceLocation, List< FluidRecipeLink>> groupRecipesByType(Map< ResourceLocation, JsonElement> recipes)
    {
        return recipes
                .entrySet()
                .stream()
                .filter(entry -> includeRecipe(entry.getKey(), entry.getValue()))
                .map(entry -> FluidRecipeLink.of(entry.getKey(), entry.getValue().getAsJsonObject()))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(entry -> entry.getId().toString()))
                .collect(Collectors.groupingByConcurrent(FluidRecipeLink::getType));
    }

    private boolean includeRecipe(ResourceLocation recipe, JsonElement json)
    {
        return json.isJsonObject() && hasValidRecipeType(json.getAsJsonObject());
    }

    private LinkedHashSet< FluidRecipeLink> unifyRecipes(List< FluidRecipeLink> recipeLinks,
                                                         Consumer< FluidRecipeLink> onUnified)
    {
        LinkedHashSet< FluidRecipeLink> unified = new LinkedHashSet<>(recipeLinks.size());
        for (FluidRecipeLink recipeLink : recipeLinks)
        {
            unifyRecipe(recipeLink);
            if (recipeLink.isUnified())
            {
                onUnified.accept(recipeLink);
                unified.add(recipeLink);
            }
        }
        return unified;
    }

    public void unifyRecipe(FluidRecipeLink recipe)
    {
        try
        {
            FluidRecipeContext ctx = new FluidRecipeContext(recipe.getOriginal(), replacementMap);
            FluidRecipeUnifierBuilder builder = new FluidRecipeUnifierBuilder();
            factory.fillUnifier(builder, ctx);
            JsonObject result = builder.unify(recipe.getOriginal(), ctx);
            if (result != null)
            {
                recipe.setUnified(result);
            }
        }
        catch (Exception e)
        {
            RAIntegration.LOGGER.warn("Error unifying recipe '{}': {}", recipe.getId(), e.getMessage());
            e.printStackTrace();
        }
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
