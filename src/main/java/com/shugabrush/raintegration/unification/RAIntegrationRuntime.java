package com.shugabrush.raintegration.unification;

import com.google.gson.JsonElement;
import com.shugabrush.raintegration.FluidUnifyConfig;
import com.shugabrush.raintegration.RAIntegration;
import com.shugabrush.raintegration.unification.recipe.FluidRecipeTransformer;
import com.shugabrush.raintegration.unification.recipe.unifier.FluidRecipeHandlerFactory;
import com.shugabrush.raintegration.unification.utils.FluidTagMap;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class RAIntegrationRuntime
{

    private final FluidUnifyConfig unifyConfig;
    private final FluidTagMap tagMap;
    private final FluidReplacementMap replacementMap;
    private final FluidRecipeHandlerFactory recipeHandlerFactory;

    public RAIntegrationRuntime(FluidUnifyConfig unifyConfig, FluidTagMap tagMap, FluidReplacementMap replacementMap, FluidRecipeHandlerFactory recipeHandlerFactory)
    {
        this.unifyConfig = unifyConfig;
        this.tagMap = tagMap;
        this.replacementMap = replacementMap;
        this.recipeHandlerFactory = recipeHandlerFactory;
    }

    public void run(Map<ResourceLocation, JsonElement> recipes, boolean skipClientTracking)
    {
        FluidRecipeTransformer.Result result = new FluidRecipeTransformer(
                recipeHandlerFactory,
                replacementMap,
                unifyConfig
        ).transformRecipes(recipes, skipClientTracking);
    }
}
