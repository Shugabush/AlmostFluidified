package com.shugabrush.raintegration.unification;

import com.google.gson.JsonElement;
import com.shugabrush.raintegration.unification.recipeunifiers.FluidRecipeUnifier;
import com.shugabrush.raintegration.unification.recipeunifiers.IndustrialForegoingFluidRecipeUnifier;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class FluidRecipeFactory
{
    private static final Map<String, FluidRecipeUnifier> unifiers = new HashMap<>();

    public FluidRecipeFactory()
    {
        // Mods that need their own custom logic for unifying their fluids
        unifiers.put("industrialforegoing", new IndustrialForegoingFluidRecipeUnifier());
    }

    // Unifies the recipe using the proper unifier
    public void unifyRecipe(String modId, JsonElement recipe)
    {
        unifiers.computeIfAbsent(modId, k -> new FluidRecipeUnifier()).unifyFluidRecipe(recipe);
    }
}
