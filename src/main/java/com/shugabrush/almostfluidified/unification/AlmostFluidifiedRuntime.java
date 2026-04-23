package com.shugabrush.almostfluidified.unification;

import net.minecraft.resources.ResourceLocation;

import com.google.gson.JsonElement;
import com.shugabrush.almostfluidified.FluidUnifyConfig;
import com.shugabrush.almostfluidified.unification.recipe.FluidRecipeTransformer;
import com.shugabrush.almostfluidified.unification.recipe.unifier.FluidRecipeHandlerFactory;
import com.shugabrush.almostfluidified.unification.utils.FluidTagMap;

import java.util.Map;

public class AlmostFluidifiedRuntime
{

    private final FluidUnifyConfig unifyConfig;
    private final FluidTagMap tagMap;
    private final FluidReplacementMap replacementMap;
    private final FluidRecipeHandlerFactory recipeHandlerFactory;

    public AlmostFluidifiedRuntime(FluidUnifyConfig unifyConfig, FluidTagMap tagMap, FluidReplacementMap replacementMap,
                                   FluidRecipeHandlerFactory recipeHandlerFactory)
    {
        this.unifyConfig = unifyConfig;
        this.tagMap = tagMap;
        this.replacementMap = replacementMap;
        this.recipeHandlerFactory = recipeHandlerFactory;
    }

    public void run(Map< ResourceLocation, JsonElement> recipes, boolean skipClientTracking)
    {
        FluidRecipeTransformer.Result result = new FluidRecipeTransformer(
                recipeHandlerFactory,
                replacementMap,
                unifyConfig).transformRecipes(recipes, skipClientTracking);
    }

    public FluidReplacementMap getReplacementMap()
    {
        return replacementMap;
    }

    public FluidTagMap getFilteredTagMap() { return tagMap; }
}
