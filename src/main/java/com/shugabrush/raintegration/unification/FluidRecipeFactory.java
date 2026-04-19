package com.shugabrush.raintegration.unification;

import com.google.gson.JsonElement;
import com.shugabrush.raintegration.unification.recipeunifiers.FluidUnifier;
import com.shugabrush.raintegration.compat.GregTechFluidRecipeUnifier;
import com.shugabrush.raintegration.unification.recipeunifiers.IndustrialForegoingFluidRecipeUnifier;

import java.util.HashMap;
import java.util.Map;

public class FluidRecipeFactory
{

    private static final Map< String, FluidUnifier> unifiers = new HashMap<>();

    public FluidRecipeFactory()
    {

    }

    // Unifies the recipe using the proper unifier
    public void unifyRecipe(String modId, JsonElement recipe)
    {
        unifiers.computeIfAbsent(modId, k -> new FluidUnifier()).unifyFluidRecipe(recipe);
    }
}
